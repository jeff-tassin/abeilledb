package com.jeta.plugins.abeille.standard;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.model.TSTable;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.utils.TSUtils;

/**
 * DefaultDatabase implementation
 * 
 * @author Jeff Tassin
 */
public abstract class DefaultTableImplementation implements TSTable {
	private TSConnection m_connection; // the connection to the database

	/**
	 * ctor
	 */
	public DefaultTableImplementation() {
	}

	/**
	 * Converts the case for the given object name to the case supported by the
	 * database. This is needed because some databases require exact case for
	 * table names (HSQLDB). So, if the user types in a table name, but the case
	 * does not match, we still want to be able to get the table from the db.
	 * This call converts to the case needed.
	 */
	public String convertCase(String objName) {
		TSDatabase db = (TSDatabase) m_connection.getImplementation(TSDatabase.COMPONENT_ID);
		return db.convertCase(objName);
	}

	/**
	 * Creates the given column in the given table
	 */
	public void createColumn(TableId tableId, ColumnMetaData newColumn) throws SQLException {
		// noop
	}

	/**
	 * Drops the given column from the given table
	 */
	public void dropColumn(TableId tableId, ColumnMetaData dropColumn, boolean cascade) throws SQLException {
		// noop
	}

	/**
	 * Drops the primary key for the given table
	 */
	public void dropPrimaryKey(TableId tableid, boolean cascade) throws SQLException {
		// no op
	}

	/**
	 * @return the underlying database connnection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the SQL used for a column in a CREATE TABLE statement
	 */
	public String getCreateColumnSQL(ColumnMetaData cmd) {
		StringBuffer sql = new StringBuffer();
		sql.append(cmd.getFieldName());
		sql.append(" ");

		String typename = cmd.getTypeName();

		if (typename == null)
			typename = DbUtils.getJDBCTypeName(cmd.getType());

		sql.append(typename);
		if (cmd.getColumnSize() > 0 || cmd.getScale() > 0) {
			sql.append("(");
			sql.append(cmd.getColumnSize());
			if (cmd.getScale() > 0) {
				sql.append(",");
				sql.append(cmd.getScale());
			}
			sql.append(")");
		}

		if (!cmd.isNullable())
			sql.append(" NOT NULL");

		if (Database.SYBASE.equals(m_connection.getDatabase()) && cmd.isNullable()) {
			sql.append(" NULL");
		}

		return sql.toString();
	}

	/**
	 * Creates the sql command to add a foreign key to an *existing* table. We
	 * assume that SQL is in English only.
	 * 
	 * @param fk
	 *            the foreign key
	 * @throws SQLException
	 *             is a database error occurs.
	 */
	public String getCreateForeignKeySQL(DbForeignKey fk) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(fk.getLocalTableId().getFullyQualifiedName());

		String keyname = fk.getName();
		if (keyname != null)
			keyname = keyname.trim();

		if (keyname == null || keyname.length() == 0) {
			sql.append(" ADD FOREIGN KEY (");
		} else {
			keyname = TSUtils.strip(keyname, "\"");
			sql.append(" ADD CONSTRAINT \"");
			sql.append(keyname);
			sql.append("\" FOREIGN KEY (");
		}

		DbKey localkey = fk.getLocalKey();
		Collection fields = localkey.getColumns();
		Iterator iter = fields.iterator();
		while (iter.hasNext()) {
			String field = (String) iter.next();
			sql.append(field);
			if (iter.hasNext())
				sql.append(", ");
		}
		sql.append(") REFERENCES ");

		TableId reftableid = fk.getReferenceTableId();
		sql.append(reftableid.getFullyQualifiedName());

		sql.append(" (");
		iter = fields.iterator();
		while (iter.hasNext()) {
			String pkcolname = fk.getAssignedPrimaryKeyColumnName((String) iter.next());
			sql.append(pkcolname);
			if (iter.hasNext())
				sql.append(", ");
		}
		sql.append(") ");
		return sql.toString();
	}

	/**
	 * Creates the primary key ALTER table sql. ALTER TABLE tablename ADD
	 * CONSTRAINT pkname PRIMARY KEY (columnname);
	 * 
	 * @param tmd
	 *            the table metadata definition to create
	 * @throws SQLException
	 *             is a database error occurs.
	 */
	public String getCreatePrimaryKeySQL(TableMetaData tmd) throws SQLException {
		DbKey pk = tmd.getPrimaryKey();
		if (pk == null || pk.getColumnCount() == 0)
			return "";

		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(tmd.getFullyQualifiedName());
		sql.append(" ADD CONSTRAINT ");

		sql.append(getSafePrimaryKeyName(tmd));
		sql.append(" PRIMARY KEY( ");

		Collection cols = pk.getColumns();
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			sql.append((String) iter.next());
			if (iter.hasNext())
				sql.append(",");
		}

		sql.append(" );\n");
		return sql.toString();
	}

	/**
	 * Creates the given table in the database. We assume that SQL is in English
	 * only.
	 * 
	 * @param tmd
	 *            the table metadata definition to create
	 * @throws SQLException
	 *             is a database error occurs.
	 */
	public String getCreateTableSQL(TableMetaData tmd) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE ");
		sql.append(convertCase(tmd.getFullyQualifiedName()));
		sql.append(" (\n");
		Collection cols = tmd.getColumns();
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			sql.append(getCreateColumnSQL(cmd));

			if (iter.hasNext())
				sql.append(",\n");
		}
		sql.append(");");

		return sql.toString();
	}

	/**
	 * @return the name for the primary key. If the name does not exist, we
	 *         automatically generate one
	 */
	public String getSafePrimaryKeyName(TableMetaData tmd) {
		DbKey pk = tmd.getPrimaryKey();
		String keyname = pk.getKeyName();
		// if the primary key name has not been set, we need to generate one
		if (keyname == null || keyname.trim().length() == 0) {
			StringBuffer buffer = new StringBuffer();
			keyname = TSUtils.createUID();
			keyname = keyname.substring(0, 4);
			// DB2 has a length limit on key names of 18 chars
			if (m_connection.getDatabase() != Database.DB2) {
				buffer.append(tmd.getTableName());
			}
			buffer.append("pk_");
			buffer.append(keyname.substring(0, 4));
			keyname = buffer.toString();
			pk.setKeyName(keyname);
		}
		return keyname;
	}

	/**
	 * Loads columns information such as constraints and default values.
	 */
	public void loadColumnsEx(TableMetaData tmd) throws SQLException {
		// no op
	}

	/**
	 * Find the foreign keys in other tables that reference the primary key in
	 * this table.
	 * 
	 * @return a collection of DbForeignKey objects
	 */
	public Collection loadExportedKeys(TableMetaData tmd) throws SQLException {
		TreeSet ref_tables = new TreeSet();
		TSConnection tsconn = getConnection();
		DatabaseMetaData metadata = tsconn.getMetaData();
		ResultSet rs = null;
		try {
			if (tsconn.supportsSchemas()) {
				rs = metadata.getExportedKeys(tmd.getCatalogName(), tmd.getSchemaName(), tmd.getTableName());
			} else {
				rs = metadata.getExportedKeys(tmd.getCatalogName(), "", tmd.getTableName());
			}
			// System.out.println( "DefaultTableImpl.loadExportedKeys: " +
			// tmd.getCatalogName() + "   table: " + tmd.getTableName() );
			while (rs != null && rs.next()) {
				// ref table
				String pk_catalog = rs.getString("PKTABLE_CAT");
				String pk_schema = rs.getString("PKTABLE_SCHEM");
				String pk_tablename = rs.getString("PKTABLE_NAME");
				String pk_colname = rs.getString("PKCOLUMN_NAME");

				// local table
				String local_catalog = rs.getString("FKTABLE_CAT");
				String local_schema = rs.getString("FKTABLE_SCHEM");
				String local_colname = rs.getString("FKCOLUMN_NAME");
				String local_tablename = rs.getString("FKTABLE_NAME");

				assert (tmd.getTableName().equals(pk_tablename));

				Schema schema = Schema.VIRTUAL_SCHEMA;
				if (tsconn.supportsSchemas()) {
					schema = new Schema(local_schema);
				}
				TableId tableid = new TableId(Catalog.createInstance(local_catalog), schema, local_tablename);

				ref_tables.add(tableid);
			}
		} finally {
			if (rs != null)
				rs.close();
		}

		return addExportedKeys(tmd, ref_tables);
	}

	public Collection addExportedKeys(TableMetaData tmd, Collection ref_tables) {
		LinkedList results = new LinkedList();
		tmd.clearExportedKeys();
		TableId tableid = tmd.getTableId();
		DbModel model = m_connection.getModel(tmd.getCatalog());
		Iterator iter = ref_tables.iterator();
		while (iter.hasNext()) {
			TableId refid = (TableId) iter.next();
			TableMetaData reftmd = model.getTableEx(refid, TableMetaData.LOAD_FOREIGN_KEYS);
			assert (reftmd != null);
			if (reftmd != null) {
				Collection fkeys = reftmd.getForeignKeys();

				Iterator fkiter = fkeys.iterator();
				while (fkiter.hasNext()) {
					DbForeignKey fkey = (DbForeignKey) fkiter.next();

					if (tableid.equals(fkey.getReferenceTableId())) {
						results.add(fkey);
						tmd.addExportedKey(fkey);
					}
				}
			}
		}
		return results;
	}

	/**
	 * Modifies the given column to take on the attributes of the new column for
	 * the given table.
	 */
	public void modifyColumn(TableId tableId, ColumnMetaData newColumn, ColumnMetaData oldColumn) throws SQLException {
		// no op
	}

	/**
	 * Modifies the given primary key.
	 */
	public void modifyPrimaryKey(TableId tableid, DbKey newpk, DbKey oldpk) throws SQLException {

	}

	/**
	 * Renames a table to a new table name. This is database dependent
	 */
	public void renameTable(TableId newName, TableId oldName) throws SQLException {

	}

	/**
	 * Sets the connection which this interface will operate on
	 * 
	 * @param conn
	 *            the connection to set
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
