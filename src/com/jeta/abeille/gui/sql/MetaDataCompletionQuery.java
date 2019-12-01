package com.jeta.abeille.gui.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.ext.CompletionQuery;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
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
public class MetaDataCompletionQuery implements CompletionQuery {
	/** The completion object that created this object */
	private SQLCompletion m_completion;

	/** This is the model of tables that we are allowed to provide completion on */
	private TableSelectorModel m_model;

	private static int TABLE_COMPLETION = 1;
	private static int COLUMN_COMPLETION = 2;

	private int m_completioncode = 0;

	public MetaDataCompletionQuery(SQLCompletion completion, TableSelectorModel model) {
		m_completion = completion;
		m_model = model;
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
				else
					break;
			} else if (tokenid == SQLTokenContext.IDENTIFIER) {
				if (count == 0) {
					objtoken = token;
				} else if (lasttoken == SQLTokenContext.DOT) {
					dbtokens.add(token);
				}

				lasttoken = tokenid;

				/** this probabaly does not work in Sybase */
				if (count > 3)
					break;
			} else {
				break;
			}
			count++;
		}

		String tablename = null;
		String schemaname = null;
		String catalogname = null;

		TSConnection tsconn = m_completion.getConnection();
		if (dbtokens.size() > 0) {
			SQLToken token = (SQLToken) dbtokens.get(0);
			tablename = token.getToken();
		}

		if (dbtokens.size() > 1) {
			SQLToken token = (SQLToken) dbtokens.get(1);
			if (tsconn.supportsSchemas()) {
				schemaname = token.getToken();
			} else {
				catalogname = token.getToken();
			}
		}

		if (dbtokens.size() > 2) {
			SQLToken token = (SQLToken) dbtokens.get(2);
			if (tsconn.supportsSchemas() && tsconn.supportsCatalogs()) {
				catalogname = token.getToken();
			}
		}

		Catalog catalog = m_model.getCurrentCatalog();
		if (catalogname != null && catalogname.length() > 0) {
			catalog = Catalog.createInstance(catalogname);
			if (!tsconn.contains(catalog)) {
				catalog = m_model.getCurrentCatalog();
			}
		}

		Schema schema = m_model.getCurrentSchema(catalog);
		if (schemaname != null && schemaname.length() > 0) {
			schema = tsconn.getSchema(catalog, schemaname);
			if (schema == null) {
				schema = m_model.getCurrentSchema(catalog);
			}
		}

		/** this means that we have a DOT . with now text after the dot */
		if (objtoken == null)
			objtoken = new SQLToken(offset);

		SQLCompletionResult result = null;

		if (tablename != null) {
			try {

				TableId tableid = new TableId(catalog, schema, tablename);
				TableMetaData tmd = m_model.getTable(tableid);
				if (tmd == null) {
					// then we potentially have a case where the user has typed
					// a select with an AS clause
					// for the table. e.g. select * from table AS t1 where t1...
					// so, we need to parse further back to see if there is
					// anything with an AS declaration
					tablename = tryParseAlias(iter, tablename);
					if (tablename != null) {
						tableid = new TableId(catalog, schema, tablename);
						tmd = m_model.getTable(tableid);
					}
				}

				if (tmd != null) {
					result = new ColumnCompletionResult(component, m_completion, tmd, objtoken);
				}

				// table was not found, so try schema or catalog completion
				/** if the token size is 1, then first assume it is a table */
				if (dbtokens.size() == 1) {
					SQLToken token = (SQLToken) dbtokens.get(0);
					String objname = token.getToken();
					// now try schema or catalog
					if (tsconn.supportsCatalogs()) {
						Catalog testcat = Catalog.createInstance(objname);
						if (tsconn.contains(testcat)) {
							if (tsconn.supportsSchemas()) {
								// result = new SchemaCompletionResult(
								// compnent, m_completion, testcat, objtoken );
							} else {
								result = new TableCompletionResult(component, m_completion, m_model, testcat,
										Schema.VIRTUAL_SCHEMA, objtoken);
							}
						}
					} else if (tsconn.supportsSchemas()) {
						Schema testschema = tsconn.getSchema(catalog, objname);
						if (testschema != null) {
							result = new TableCompletionResult(component, m_completion, m_model, catalog, testschema,
									objtoken);
						}
					}
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}
		return result;
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

}
