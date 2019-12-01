package com.jeta.plugins.abeille.mysql;

import java.io.StringReader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.ForeignKeyConstraints;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSForeignKeys;

import com.jeta.abeille.gui.command.SQLCommand;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.standard.AbstractForeignKeysImplementation;
import com.jeta.plugins.abeille.mysql.parsers.MySQLForeignKeyParser;

/**
 * This is the implementation for foreign key retrieval for MySQL.
 * 
 * @author Jeff Tassin
 */
public class MySQLForeignKeysImplementation extends AbstractForeignKeysImplementation {
	/**
	 * Ctor
	 */
	public MySQLForeignKeysImplementation() {

	}

	/**
	 * Ctor
	 */
	public MySQLForeignKeysImplementation(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * Creates a new foreign key in the database
	 */
	public void createForeignKey(DbForeignKey newkey) throws SQLException {
		// ALTER [IGNORE] TABLE tbl_name alter_spec
		// ADD [CONSTRAINT symbol] FOREIGN KEY [index_name] (index_col_name,...)
		// [reference_definition]
		MySQLTableImplementation impl = (MySQLTableImplementation) getConnection().getImplementation(
				MySQLTableImplementation.COMPONENT_ID);

		String fkeydef = impl.getCreateForeignKey(newkey);
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(newkey.getLocalTableId().getFullyQualifiedName());
		sql.append(" ADD ");
		sql.append(fkeydef);
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Create Foreign Key"), sql.toString());
	}

	/**
	 * Setting the cache will cause the next call to getForeignKeys to cache all
	 * the keys for all tables in a given schema. This improves performance
	 * significantly and is used when loading the model. Once the model is
	 * loaded, turn the cache off. This cause all metadat requests to be
	 * forwarded directly to the database.
	 */
	public synchronized void enableCache(boolean bcache) throws SQLException {
	}

	/**
	 * Note: The standard method (getImportedKeys) does not work with the MySQL
	 * JDBC driver <= 3.0. So, we parse the definitions manually
	 * 
	 * @param tableId
	 *            the id of the table whose foreign keys we wish to retrieve
	 * @return a collection of DbForeignKey objects for the given table
	 */
	public Collection getForeignKeys(TableId tableId) throws SQLException {
		LinkedList results = new LinkedList();

		Statement stmt = null;
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("show table status from ");
			sql.append(tableId.getCatalog().getName());
			sql.append(" like '");
			sql.append(tableId.getTableName());
			sql.append("'");
			Connection conn = getConnection().getMetaDataConnection();
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql.toString());
			while (rset.next()) {
				String tablename = rset.getString("Name");
				if (tableId.getTableName().equals(tablename)) {
					String comment = rset.getString("COMMENT");
					StringTokenizer st = new StringTokenizer(comment, ";");
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (token.indexOf(") REFER ") > 0) {
							DbForeignKey fkey = parseForeignKey(tableId, token);
							if (fkey != null)
								results.add(fkey);
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {

			}
		}
		return results;
	}

	/**
	 * Note: this method does not work with the MySQL JDBC driver <= 3.0. This
	 * driver returns the wrong value for KEY_SEQ so we need to parse the fkey
	 * definition manually.
	 * 
	 * @param tableId
	 *            the id of the table whose foreign keys we wish to retrieve
	 * @return a collection of DbForeignKey objects for the given table
	 */
	public Collection getForeignKeys2(TableId tableid) throws SQLException {

		TSConnection tsconn = getConnection();
		DbModel model = tsconn.getModel(tableid.getCatalog());
		HashMap fkeys = new HashMap();

		DatabaseMetaData metadata = tsconn.getMetaDataConnection().getMetaData();
		ResultSet rs = null;
		try {

			Schema schema = tableid.getSchema();
			if (schema == Schema.VIRTUAL_SCHEMA)
				rs = metadata.getImportedKeys(tableid.getCatalog().getName(), "", tableid.getTableName());
			else
				rs = metadata.getImportedKeys(tableid.getCatalog().getName(), schema.getName(), tableid.getTableName());

			DbForeignKey fkey = null;
			TableMetaData localtmd = tsconn.getTable(tableid);
			while (rs != null && rs.next()) {
				// ref table
				String ref_tablename = rs.getString("PKTABLE_NAME");
				String ref_colname = rs.getString("PKCOLUMN_NAME");
				String ref_catalog = rs.getString("PKTABLE_CAT");
				if (ref_catalog == null)
					ref_catalog = tableid.getCatalog().getName();

				// local table
				String local_colname = rs.getString("FKCOLUMN_NAME");
				String local_tablename = rs.getString("FKTABLE_NAME");
				String local_keyname = rs.getString("FK_NAME");

				short key_seq = rs.getShort("KEY_SEQ");
				if (key_seq == 0)
					fkey = null;

				if (fkey == null) {
					fkey = new DbForeignKey();
					fkeys.put(local_keyname, fkey);
					fkey.setKeyName(local_keyname);
					fkey.setLocalTableId(tableid);

					TableId refid = new TableId(Catalog.createInstance(ref_catalog), schema, ref_tablename);
					fkey.setReferenceTableId(refid);

					TableMetaData reftmd = model.getTable(refid);
					if (reftmd != null) {
						DbKey pk = reftmd.getPrimaryKey();
						assert (pk != null);
						if (pk != null) {
							fkey.setReferenceKeyName(pk.getKeyName());
						}
					}
				}
				fkey.assignForeignKeyColumn(local_colname, ref_colname);
			}
		} finally {
			if (rs != null)
				rs.close();
		}

		return fkeys.values();
	}

	/**
	 * Parses the foreign key token for MySQL (col1 col2 colN) REFER
	 * local_table/ref_table(col1 col2 colN)
	 * 
	 */
	public DbForeignKey parseForeignKey(TableId tableid, String keyToken) {
		try {
			DbForeignKey fkey = null;

			StringReader reader = new StringReader(keyToken);
			MySQLForeignKeyParser parser = new MySQLForeignKeyParser(reader);
			parser.parse();

			String[] localcols = parser.getLocalColumns();
			String[] refcols = parser.getReferenceColumns();
			if ((localcols.length == refcols.length) && localcols.length > 0) {
				fkey = new DbForeignKey();
				fkey.setLocalTableId(tableid);
				Catalog refcat = Catalog.createInstance(parser.getReferenceCatalog());
				TableId refid = new TableId(refcat, Schema.VIRTUAL_SCHEMA, parser.getReferenceTable());
				fkey.setReferenceTableId(refid);
				for (int index = 0; index < localcols.length; index++) {
					String local_colname = localcols[index];
					String ref_colname = refcols[index];
					fkey.assignForeignKeyColumn(local_colname, ref_colname);
				}

				ForeignKeyConstraints fc = new ForeignKeyConstraints();
				fc.setDeleteAction(ForeignKeyConstraints.fromString(parser.getDeleteAction()));
				fc.setUpdateAction(ForeignKeyConstraints.fromString(parser.getUpdateAction()));

				fkey.setConstraints(fc);

			}
			return fkey;
		} catch (Exception e) {
			TSUtils.printException(e);
		} catch (Error e) {
			TSUtils.printException(e);
		}
		return null;
	}

}
