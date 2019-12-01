package com.jeta.abeille.gui.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.ext.CompletionQuery;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DatabaseObject;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.DbObjectNameComparator;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.foundation.utils.TSUtils;

/**
 * SQL completion support finder
 * 
 * @author Jeff Tassin
 */
public class MetaDataCompletionQuery2 implements CompletionQuery {
	/** The completion object that created this object */
	private SQLCompletion m_completion;

	/** This is the model of tables that we are allowed to provide completion on */
	private TableSelectorModel m_model;

	private JTextComponent m_editor;

	private SearchResult m_search = new SearchResult(0, null);

	private static final int NO_TOKENS = 1;
	private static final int MATCH = 2;
	private static final int NO_MATCH = 3;

	public MetaDataCompletionQuery2(SQLCompletion completion, TableSelectorModel model) {
		m_completion = completion;
		m_model = model;
	}

	private JTextComponent getEditor() {
		return m_editor;
	}

	private SQLCompletion getCompletion() {
		return m_completion;
	}

	private TableSelectorModel getTableSelectorModel() {
		return m_model;
	}

	/**
	 * Builds the list of items to be displayed in the popup based on the parsed
	 * items found in the token processor.
	 * 
	 * @param processor
	 *            the token processor that contains the parsed tokens that we
	 *            need to analyze
	 */
	private SQLCompletionResult buildCompletionResult(JTextComponent component, int offset, SQLTokenProcessor processor) {

		m_editor = component;

		SQLCompletionResult result = null;
		SQLToken objtoken = null; // this can be partially completed

		/**
		 * this is a list of parsed tokens that we found in the following format
		 * token1.token2.token3...tokenN We are looking for this form because we
		 * support completions for the following general form:
		 * catalog.schema.table.column.
		 */
		ArrayList dbtokens = new ArrayList();

		TokenID lasttoken = null;
		int count = 0;
		LinkedList list = processor.getTokens();

		ListIterator iter = list.listIterator(list.size());
		while (iter.hasPrevious()) {
			SQLToken token = (SQLToken) iter.previous();

			TokenID tokenid = token.getTokenID();
			if (tokenid == SQLTokenContext.DOT) {
				if (count == 0 || (count > 0 && lasttoken == SQLTokenContext.IDENTIFIER))
					lasttoken = tokenid;
				else {
					break;
				}
			} else if (tokenid == SQLTokenContext.IDENTIFIER) {
				if (count == 0) {
					objtoken = token;
					dbtokens.add(0, token);
				} else if (lasttoken == SQLTokenContext.DOT) {
					dbtokens.add(0, token);
				}
				lasttoken = tokenid;
				/** for Sybase which supports catalogs and schemas */
				if (count > 6)
					break;
			} else {
				break;
			}
			count++;
		}

		/** this means that we have a DOT . with now text after the dot */
		if (objtoken == null && dbtokens.size() > 0) {
			objtoken = new SQLToken(offset);
			dbtokens.add(objtoken);
		}

		TSConnection tsconn = m_completion.getConnection();
		if (tsconn.supportsCatalogs()) {
			if (tsconn.supportsSchemas()) {
				return handleCatalogSchemaSupportedDatabase(tsconn, dbtokens);
			} else {
				return handleCatalogSupportedDatabase(tsconn, dbtokens);
			}
		} else {
			if (tsconn.supportsSchemas()) {
				return handleSchemaSupportedDatabase(tsconn, dbtokens);
			} else {
				return handleSimpleDatabase(tsconn, dbtokens);
			}
		}
	}

	/**
	 * Handle completion for databases that support catalogs but not schemas
	 * 
	 * @param dbtokens
	 *            an array of tokens from 0 to N that represent the tokens in
	 *            the SQL from left to right.
	 */
	private SQLCompletionResult handleCatalogSupportedDatabase(TSConnection tsconn, ArrayList dbtokens) {
		SQLCompletionResult result = null;
		SearchResult search = null;
		Schema current_schema = Schema.VIRTUAL_SCHEMA;
		Catalog current_catalog = tsconn.getCurrentCatalog();
		if (dbtokens.size() == 1) {
			/** try complete on tablename then schema name */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			// do a tryAlias here if no match

			TreeSet tables = tsconn.getTables(current_catalog, current_schema);
			if (searchCollection(tables, token0.getToken()) != null) {
				result = new TableCompletionResult(getEditor(), getCompletion(), getTableSelectorModel(),
						current_catalog, current_schema, token0);
			} else {
				Collection catalogs = tsconn.getCatalogs();
				if (searchCollection(catalogs, token0.getToken()) != null) {
					result = new SQLCompletionResult(getEditor(), getCompletion(), catalogs, token0,
							DbObjectNameComparator.getInstance());
				}
			}
		} else if (dbtokens.size() == 2) {
			/** try complete on tablename.column then catalog.table */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			SQLToken token1 = (SQLToken) dbtokens.get(1);
			search = tryTableColumn(tsconn, current_catalog, current_schema, token0, token1);
			if (search.getCode() == MATCH) {
				result = search.getResult();
			} else if (search.getCode() == NO_TOKENS) {
				search = tryQualifiedTable(tsconn, null, current_schema, token0, token1);
				if (search.getCode() == MATCH) {
					result = search.getResult();
				}
			}
		} else if (dbtokens.size() == 3) {
			/** try complete on catalog.tablename.column */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			SQLToken token1 = (SQLToken) dbtokens.get(1);
			SQLToken token3 = (SQLToken) dbtokens.get(2);
			search = tryQualifiedTableColumn(tsconn, null, current_schema, token0, token1, token3);
			if (search.getCode() == MATCH) {
				result = search.getResult();
			}
		}

		if (result == null) {
			// handleNoCompletion();
		}
		return result;
	}

	/**
	 * Handle completion for databases that support catalogs and schemas
	 * 
	 * @param dbtokens
	 *            an array of tokens from 0 to N that represent the tokens in
	 *            the SQL from left to right.
	 */
	private SQLCompletionResult handleCatalogSchemaSupportedDatabase(TSConnection tsconn, ArrayList dbtokens) {

		SQLCompletionResult result = null;
		SearchResult search = null;
		Catalog current_catalog = tsconn.getCurrentCatalog();
		Schema current_schema = tsconn.getCurrentSchema(current_catalog);
		if (dbtokens.size() == 1) {
			/** try complete on tablename then schema name then catalog name */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			// do a tryAlias here if no match

			TreeSet tables = tsconn.getTables(current_catalog, current_schema);
			if (searchCollection(tables, token0.getToken()) != null) {
				result = new TableCompletionResult(getEditor(), getCompletion(), getTableSelectorModel(),
						current_catalog, current_schema, token0);
			} else {
				Collection schemas = tsconn.getSchemas(current_catalog);
				if (searchCollection(schemas, token0.getToken()) != null) {
					result = new SQLCompletionResult(getEditor(), getCompletion(), schemas, token0,
							DbObjectNameComparator.getInstance());
				} else {
					Collection catalogs = tsconn.getCatalogs();
					if (searchCollection(catalogs, token0.getToken()) != null) {
						result = new SQLCompletionResult(getEditor(), getCompletion(), catalogs, token0,
								DbObjectNameComparator.getInstance());
					}
				}
			}
		} else if (dbtokens.size() == 2) {
			/**
			 * try complete on tablename.column then schema.table then
			 * catalog.schema
			 */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			SQLToken token1 = (SQLToken) dbtokens.get(1);
			search = tryTableColumn(tsconn, current_catalog, current_schema, token0, token1);
			if (search.getCode() == MATCH) {
				result = search.getResult();
			} else if (search.getCode() == NO_TOKENS) {
				/** try schema.table */
				search = tryQualifiedTable(tsconn, current_catalog, null, token0, token1);
				if (search.getCode() == MATCH) {
					result = search.getResult();
				} else {
					/** try catalog.schema */
					search = tryQualifiedSchema(tsconn, token0, token1);
					if (search.getCode() == MATCH) {
						result = search.getResult();
					}
				}
			}
		} else if (dbtokens.size() == 3) {
			/**
			 * try complete on schema.tablename.column then
			 * catalog.schema.tablename
			 */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			SQLToken token1 = (SQLToken) dbtokens.get(1);
			SQLToken token2 = (SQLToken) dbtokens.get(2);
			search = tryQualifiedTableColumn(tsconn, current_catalog, null, token0, token1, token2);
			if (search.getCode() == MATCH) {
				result = search.getResult();
			} else {
				/** try catalog.schema.tablename */
				search = tryQualifiedCatalogSchemaTable(tsconn, token0, token1, token2);
				if (search.getCode() == MATCH) {
					result = search.getResult();
				}
			}
		} else if (dbtokens.size() == 4) {
			/** try complete on catalog.schema.tablename.column */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			SQLToken token1 = (SQLToken) dbtokens.get(1);
			SQLToken token2 = (SQLToken) dbtokens.get(2);
			SQLToken token3 = (SQLToken) dbtokens.get(3);
			search = tryQualifiedCatalogSchemaTableColumn(tsconn, token0, token1, token2, token3);
			if (search.getCode() == MATCH) {
				result = search.getResult();
			}
		}

		if (result == null) {
			// handleNoCompletion();
		}
		return result;
	}

	/**
	 * Handle completion for databases that support schemas but not catalogs
	 * 
	 * @param dbtokens
	 *            an array of tokens from 0 to N that represent the tokens in
	 *            the SQL from left to right.
	 */
	private SQLCompletionResult handleSchemaSupportedDatabase(TSConnection tsconn, ArrayList dbtokens) {
		SQLCompletionResult result = null;
		SearchResult search = null;
		Schema current_schema = tsconn.getCurrentSchema(Catalog.VIRTUAL_CATALOG);
		if (dbtokens.size() == 1) {
			/** try complete on tablename then schema name */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			// do a tryAlias here if no match

			TreeSet tables = tsconn.getTables(Catalog.VIRTUAL_CATALOG, current_schema);
			if (searchCollection(tables, token0.getToken()) != null) {

				result = new TableCompletionResult(getEditor(), getCompletion(), getTableSelectorModel(),
						Catalog.VIRTUAL_CATALOG, current_schema, token0);
			} else {
				Collection schemas = tsconn.getSchemas(Catalog.VIRTUAL_CATALOG);
				if (searchCollection(schemas, token0.getToken()) != null) {

					result = new SQLCompletionResult(getEditor(), getCompletion(), schemas, token0,
							DbObjectNameComparator.getInstance());
				}
			}
		} else if (dbtokens.size() == 2) {
			/** try complete on tablename.column then schemaname.table */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			SQLToken token1 = (SQLToken) dbtokens.get(1);
			search = tryTableColumn(tsconn, Catalog.VIRTUAL_CATALOG, current_schema, token0, token1);
			if (search.getCode() == MATCH) {
				result = search.getResult();
			} else if (search.getCode() == NO_TOKENS) {
				search = tryQualifiedTable(tsconn, Catalog.VIRTUAL_CATALOG, null, token0, token1);
				if (search.getCode() == MATCH) {
					result = search.getResult();
				}
			}
		} else if (dbtokens.size() == 3) {
			/** try complete on schema.tablename.column */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			SQLToken token1 = (SQLToken) dbtokens.get(1);
			SQLToken token3 = (SQLToken) dbtokens.get(2);
			search = tryQualifiedTableColumn(tsconn, Catalog.VIRTUAL_CATALOG, null, token0, token1, token3);
			if (search.getCode() == MATCH) {
				result = search.getResult();
			}
		}

		if (result == null) {
			// handleNoCompletion();
		}
		return result;
	}

	/**
	 * Handle completion for databases that don't support schemas nor catalogs
	 * (e.g. HSQLDB and JDataStore)
	 * 
	 * @param dbtokens
	 *            an array of tokens from 0 to N that represent the tokens in
	 *            the SQL from left to right.
	 */
	private SQLCompletionResult handleSimpleDatabase(TSConnection tsconn, ArrayList dbtokens) {
		SQLCompletionResult result = null;
		SearchResult search = null;
		Schema current_schema = Schema.VIRTUAL_SCHEMA;
		if (dbtokens.size() == 1) {
			/** try complete on tablename only */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			// do a tryAlias here if no match

			TreeSet tables = tsconn.getTables(Catalog.VIRTUAL_CATALOG, current_schema);
			if (searchCollection(tables, token0.getToken()) != null) {
				result = new TableCompletionResult(getEditor(), getCompletion(), getTableSelectorModel(),
						Catalog.VIRTUAL_CATALOG, current_schema, token0);
			}
		} else if (dbtokens.size() == 2) {
			/** try complete on tablename.column only */
			SQLToken token0 = (SQLToken) dbtokens.get(0);
			SQLToken token1 = (SQLToken) dbtokens.get(1);
			search = tryTableColumn(tsconn, Catalog.VIRTUAL_CATALOG, current_schema, token0, token1);
			if (search.getCode() == MATCH) {
				result = search.getResult();
			}
		}
		return result;
	}

	/**
	 * Searchs for catalog.schema.table match (or partial match on table)
	 */
	private SearchResult tryQualifiedCatalogSchemaTable(TSConnection tsconn, SQLToken cattoken, SQLToken schematoken,
			SQLToken partialTable) {
		assert (tsconn.supportsSchemas() && tsconn.supportsCatalogs());
		Catalog catalog = tsconn.getCatalogInstance(cattoken.getToken());
		if (catalog == null) {
			m_search.set(NO_MATCH, null);
		} else {
			m_search = tryQualifiedTable(tsconn, catalog, null, schematoken, partialTable);
		}
		return m_search;
	}

	/**
	 * Searchs for catalog.schema.table match (or partial match on table)
	 */
	private SearchResult tryQualifiedCatalogSchemaTableColumn(TSConnection tsconn, SQLToken cattoken,
			SQLToken schematoken, SQLToken tabletoken, SQLToken partialColumn) {
		assert (tsconn.supportsSchemas() && tsconn.supportsCatalogs());
		Catalog catalog = tsconn.getCatalogInstance(cattoken.getToken());
		if (catalog == null) {
			m_search.set(NO_MATCH, null);
		} else {
			m_search = tryQualifiedTableColumn(tsconn, catalog, null, schematoken, tabletoken, partialColumn);
		}
		return m_search;
	}

	/**
	 * Queries the connection to determine if it contains the given schema or
	 * catalog. If so, then queries the connection to determine if the tables
	 * from the given schema match the partial tableName
	 */
	private SearchResult tryQualifiedTable(TSConnection tsconn, Catalog catalog, Schema schema, SQLToken sname,
			SQLToken partialTable) {
		if (tsconn.supportsSchemas()) {
			schema = tsconn.getSchema(catalog, sname.getToken());
		} else {
			catalog = tsconn.getCatalogInstance(sname.getToken());
		}

		if (schema == null || catalog == null) {
			m_search.set(NO_MATCH, null);
		} else {
			TreeSet tables = tsconn.getTables(catalog, schema);
			if (searchCollection(tables, partialTable.getToken()) != null) {
				m_search.set(MATCH, new TableCompletionResult(getEditor(), getCompletion(), getTableSelectorModel(),
						catalog, schema, partialTable));
			} else {
				m_search.set(NO_TOKENS, null);
			}
		}
		return m_search;
	}

	/**
	 * Searchs for a match to catalog.schema
	 */
	private SearchResult tryQualifiedSchema(TSConnection tsconn, SQLToken cattoken, SQLToken partialSchemaToken) {
		System.out.println("MetaDataCompletionQuery2.tryQualifiedSchema");
		assert (tsconn.supportsSchemas() && tsconn.supportsCatalogs());
		Catalog catalog = tsconn.getCatalogInstance(cattoken.getToken());
		if (catalog == null) {
			m_search.set(NO_MATCH, null);
		} else {
			Collection schemas = tsconn.getSchemas(catalog);
			if (searchCollection(schemas, partialSchemaToken.getToken()) != null) {
				m_search.set(MATCH, new SQLCompletionResult(getEditor(), getCompletion(), schemas, partialSchemaToken,
						DbObjectNameComparator.getInstance()));
			} else {
				m_search.set(NO_TOKENS, null);
			}
		}
		return m_search;
	}

	/**
	 * Queries the connection to determine if it contains the given schema. If
	 * so, then queries the connection to determine if the tables from the given
	 * schema match the partial tableName
	 */
	private SearchResult tryQualifiedTableColumn(TSConnection tsconn, Catalog catalog, Schema schema, SQLToken sname,
			SQLToken table, SQLToken partialColumn) {
		if (tsconn.supportsSchemas()) {
			schema = tsconn.getSchema(catalog, sname.getToken());
		} else {
			catalog = tsconn.getCatalogInstance(sname.getToken());
		}

		if (schema == null || catalog == null) {
			m_search.set(NO_MATCH, null);
			return m_search;
		} else {
			return tryTableColumn(tsconn, catalog, schema, table, partialColumn);
		}
	}

	/**
	 * Determines if two given tokens match a table.column pattern for the given
	 * catalog and schema
	 */
	private SearchResult tryTableColumn(TSConnection tsconn, Catalog catalog, Schema schema, SQLToken table,
			SQLToken partialColumn) {
		TreeSet tables = tsconn.getTables(catalog, schema);

		TableId tableid = (TableId) searchCollection(tables, table.getToken());
		TableMetaData tmd = tsconn.getTable(tableid);
		if (tmd != null) {
			if (searchCollection(tmd.getColumns(), partialColumn.getToken()) != null) {
				m_search.set(MATCH, new ColumnCompletionResult(getEditor(), getCompletion(), tmd, partialColumn));
			} else {
				m_search.set(NO_TOKENS, null);
			}
		} else {
			m_search.set(NO_TOKENS, null);
		}
		return m_search;
	}

	public CompletionQuery.Result query(JTextComponent component, int offset, SyntaxSupport support) {
		return query(component, offset, support, false);
	}

	/**
	 * Perform the query on the given component. The query usually gets the
	 * component's document, the caret position and searches back to find the
	 * last command start. Then it inspects the text up to the caret position
	 * and returns the result.
	 * 
	 * @param component
	 *            the component to use in this query.
	 * @param offset
	 *            position in the component's document to which the query will
	 *            be performed. Usually it's a caret position.
	 * @param support
	 *            syntax-support that will be used during resolving of the
	 *            query.
	 * @param sourceHelp
	 *            whether the help is retrieved to open the source file. The
	 *            query behavior is slightly modified if this flag is true
	 * @return result of the query or null if there's no result.
	 */
	public CompletionQuery.Result query(JTextComponent component, int offset, SyntaxSupport support, boolean sourceHelp) {
		BaseDocument doc = (BaseDocument) component.getDocument();
		SQLSyntaxSupport sup = (SQLSyntaxSupport) support.get(SQLSyntaxSupport.class);
		SQLCompletionResult result = null;

		try {
			// find last separator position
			int lastSepOffset = sup.getLastCommandSeparator(offset);
			// System.out.println(
			// "SQLCompletionQuery.query  lastcommandsepposition: " +
			// lastSepOffset );

			SQLTokenProcessor processor = new SQLTokenProcessor();
			sup.tokenizeText(processor, lastSepOffset + 1, offset, true);

			// Check whether there's a comment under the cursor
			boolean inComment = false;
			TokenID lastValidTokenID = processor.getLastValidTokenID();
			if (lastValidTokenID != null) {
				switch (lastValidTokenID.getNumericID()) {
				case SQLTokenContext.BLOCK_COMMENT_ID:
					if (processor.getLastValidTokenText() == null || !processor.getLastValidTokenText().endsWith("*/")) {
						inComment = true;
					}
					break;

				case SQLTokenContext.LINE_COMMENT_ID:
					inComment = true;
					break;
				}
			}

			if (!inComment) {
				result = buildCompletionResult(component, offset, processor);
			}

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		return result;
	}

	private Object searchCollection(Collection c, String token) {

		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			/** if token has zero length, then default match to first object */
			if (token.length() == 0)
				return object;

			String obj_name = null;
			if (object instanceof DatabaseObject) {
				DbObjectId id = ((DatabaseObject) object).getObjectId();
				if (id != null)
					obj_name = id.getObjectName();
			} else if (object instanceof Schema) {
				obj_name = ((Schema) object).getName();
			} else if (object != null) {
				obj_name = object.toString();
			}

			if (obj_name != null) {

				if (obj_name.length() >= token.length() && obj_name.regionMatches(true, 0, token, 0, token.length())) {
					return object;
				}
			}
		}

		return null;
	}

	/**
	 * Parse backwards in the sql looking for an alias for the given table name.
	 * We potentially have a case where the user has typed a select with an AS
	 * clause for the table. e.g. select * from table AS t1 where t1... so, we
	 * need to parse further back to see if there is anything with an AS
	 * declaration
	 * 
	 * @param iter
	 *            a list iterator that points to a collection of SQLToken
	 *            objects
	 * @param tablename
	 *            the name of the table that we are looking for in an AS
	 *            assignment
	 **/
	private String tryParseAlias(ListIterator iter, String tablename) {
		// System.out.println( "tryParseAlias:  hasPrev: " + iter.hasPrevious()
		// + "  tablename = " + tablename );
		if (tablename == null)
			return null;

		tablename = tablename.trim();
		if (tablename.length() == 0)
			return null;

		String alias = null;
		TokenID lasttokenid = null;
		while (iter.hasPrevious()) {
			SQLToken token = (SQLToken) iter.previous();

			TokenID tokenid = token.getTokenID();
			if (tokenid == SQLTokenContext.AS) {
				lasttokenid = tokenid;
			} else if (tokenid == SQLTokenContext.IDENTIFIER) {
				if (lasttokenid == SQLTokenContext.AS) {
					String foundtable = token.getToken();
					if (alias != null && alias.equalsIgnoreCase(tablename)) {
						return foundtable;
					}
				}
				alias = token.getToken();
				lasttokenid = tokenid;
			} else if (tokenid == SQLTokenContext.WHITESPACE) {
				// ignore
			} else {
				lasttokenid = tokenid;
			}
		}

		return null;
	}

	private class SearchResult {
		private int m_code;
		private SQLCompletionResult m_result;

		/**
		 * ctor
		 */
		public SearchResult(int code, SQLCompletionResult result) {
			m_code = code;
			m_result = result;
		}

		public int getCode() {
			return m_code;
		}

		public SQLCompletionResult getResult() {
			return m_result;
		}

		public void set(int code, SQLCompletionResult result) {
			m_code = code;
			m_result = result;
		}
	}
}
