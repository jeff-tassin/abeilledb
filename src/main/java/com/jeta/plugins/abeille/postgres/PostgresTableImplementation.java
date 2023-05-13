package com.jeta.plugins.abeille.postgres;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.ForeignKeyConstraints;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.model.TSForeignKeys;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.plugins.abeille.standard.DefaultTableImplementation;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the Postgres implementation for the TSTable interface
 * 
 * @author Jeff Tassin
 */
public class PostgresTableImplementation extends DefaultTableImplementation {

	/**
	 * ctor
	 */
	public PostgresTableImplementation() {
	}

	/**
	 * ctor
	 */
	public PostgresTableImplementation(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * Creates the given column in the given table
	 */
	public void createColumn(TableId tableId, ColumnMetaData newColumn) throws SQLException {
		/*
		 * ALTER TABLE [ ONLY ] table [ * ] ADD [ COLUMN ] column type [
		 * column_constraint [ ... ] ]
		 */

		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(tableId.getFullyQualifiedName());
		sql.append(" ADD COLUMN ");
		sql.append(getCreateColumnSQL(newColumn));
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Alter Table"), sql.toString());
	}

	/**
	 * @return the SQL command to create the table represented by the table
	 *         metadata object. This is database dependent
	 */
	public String createTableSQL(TableMetaData tmd) throws SQLException {
		assert (tmd.getCatalog() != null);

		String tablename = tmd.getTableName();
		tablename = convertCase(tablename);
		tmd.setTableName(tablename);

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
		sql.append("\n");
		sql.append(getCreatePrimaryKeySQL(tmd));

		iter = tmd.getForeignKeys().iterator();
		while (iter.hasNext()) {
			DbForeignKey fk = (DbForeignKey) iter.next();
			sql.append(getCreateForeignKeySQL(fk));
			sql.append("\n");
		}
		return sql.toString();

		// translate BINARY types to bytea
		// Collection cols = tmd.getColumns();
		// Iterator iter = cols.iterator();
		// while( iter.hasNext() )
		// {
		// ColumnMetaData cmd = (ColumnMetaData)iter.next();
		// if ( cmd.getType() == Types.BINARY )
		// {
		// cmd.setTypeName( "bytea" );
		// cmd.setType( Types.BINARY );
		// }
		// }
	}

	/**
	 * Drops the given column from the given table
	 */
	public void dropColumn(TableId tableId, ColumnMetaData dropColumn, boolean cascade) throws SQLException {
		/*
		 * ALTER TABLE [ ONLY ] table [ * ] DROP [ COLUMN ] column [ RESTRICT |
		 * CASCADE ]
		 */
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(tableId.getFullyQualifiedName());
		sql.append(" DROP COLUMN ");
		sql.append(dropColumn.getName());

		if (cascade)
			sql.append(" CASCADE");

		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Alter Table"), sql.toString());
	}

	/**
	 * @return the SQL used for a column in a CREATE TABLE statement
	 */
	public String getCreateColumnSQL(ColumnMetaData cmd) {
		StringBuffer sql = new StringBuffer();

		sql.append(cmd.getColumnName());
		sql.append(" ");

		TSDatabase dbimpl = (TSDatabase) getConnection().getImplementation(TSDatabase.COMPONENT_ID);

		String typename = TSUtils.fastTrim(cmd.getTypeName());
		DataTypeInfo typeinfo = dbimpl.getDataTypeInfo(typename);
		if (typeinfo == null) {
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
		} else {
			if (typeinfo.supportsCustomPrecision() || typeinfo.supportsCustomScale()) {
				sql.append(typename);
				if (cmd.getColumnSize() > 0) {
					sql.append("(");
					sql.append(cmd.getColumnSize());
					if (typeinfo.supportsCustomScale() && cmd.getScale() > 0) {
						sql.append(",");
						sql.append(cmd.getScale());
					}
					sql.append(")");
				}
			} else {
				sql.append(typename);
			}
		}

		String def_value = TSUtils.fastTrim(cmd.getDefaultValue());
		if (def_value.length() > 0) {
			sql.append(" DEFAULT ");
			def_value = TSUtils.strip(def_value, "\'");
			sql.append(DbUtils.toSQL(def_value, '\''));
		}

		if (!cmd.isNullable())
			sql.append(" NOT NULL");

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
		sql.append(super.getCreateForeignKeySQL(fk));

		/*
		 * FOREIGN KEY ( column_name [, ... ] ) REFERENCES reftable [ (
		 * refcolumn [, ... ] ) ] [ MATCH FULL | MATCH PARTIAL ] [ ON DELETE
		 * action ] [ ON UPDATE action ] } [ DEFERRABLE | NOT DEFERRABLE ] [
		 * INITIALLY DEFERRED | INITIALLY IMMEDIATE ]
		 */

		ForeignKeyConstraints fc = (ForeignKeyConstraints) fk.getConstraints();
		if (fc != null) {
			int deletea = fc.getDeleteAction();
			sql.append("ON DELETE ");
			sql.append(ForeignKeyConstraints.toActionSQL(deletea));

			int updatea = fc.getUpdateAction();
			sql.append(" ON UPDATE ");
			sql.append(ForeignKeyConstraints.toActionSQL(updatea));
			sql.append(" ");

			if (fc.isDeferrable()) {
				sql.append("DEFERRABLE ");
				if (fc.isInitiallyDeferred())
					sql.append("INITIALLY DEFERRED ");
				else
					sql.append("INITIALLY IMMEDIATE ");
			}
		}
		sql.append(";");
		return sql.toString();
	}

	/**
	 * Loads columns information such as constraints and default values.
	 */
	public void loadColumnsEx_Deprecated(TableMetaData tmd) throws SQLException {
		TSConnection tsconn = getConnection();
		StringBuffer sql = new StringBuffer();
		if (tsconn.supportsSchemas()) {
			sql.append("select * from pg_catalog.pg_attrdef where pg_catalog.pg_attrdef.adrelid = pg_catalog.pg_class.oid and pg_catalog.pg_class.relkind = 'r' and pg_catalog.pg_class.relname = '");
			sql.append(tmd.getTableName());
			sql.append("' and pg_catalog.pg_class.relnamespace = pg_catalog.pg_namespace.oid and pg_catalog.pg_namespace.nspname = '");
			sql.append(tmd.getTableId().getSchemaName());
			sql.append("'");
		} else {
			sql.append("select * from pg_attrdef where pg_attrdef.adrelid = pg_class.oid and pg_class.relkind = 'r' and pg_class.relname = '");
			sql.append(tmd.getTableName());
			sql.append("'");
		}

		Connection conn = null;
		Statement stmt = null;
		try {
			// boolean oid_table = false;
			// ColumnMetaData oidcmd = tmd.getColumn(0);
			// if ( I18N.equalsIgnoreCase( oidcmd.getColumnName(), "oid" ) )
			// {
			// oid_table = true;
			// }

			conn = tsconn.getMetaDataConnection();
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql.toString());
			while (rset.next()) {
				// 1-based columns
				int col = rset.getInt("adnum");
				String default_value = rset.getString("adsrc");

				// if the table is showing the oid column in the metadata, then
				// we need
				// to ignore
				// if ( oid_table )
				// col++;

				ColumnMetaData cmd = tmd.getColumn(col - 1);
				cmd.setDefaultValue(default_value);

				if (DbUtils.isIntegral(cmd)) {
					if (default_value != null && default_value.indexOf("nextval(") == 0) {
						cmd.setAutoIncrement(true);
					}
				}
			}
			conn.commit();
		} catch (SQLException se) {
			conn.rollback();
			throw se;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * Find the foreign keys in other tables that reference the primary key in
	 * this table.
	 * 
	 * @return a collection of DbForeignKey objects
	 */
	/*
	public Collection loadExportedKeys(TableMetaData tmd) throws SQLException {
		ForeignKeysImplementation fkeysimpl = (ForeignKeysImplementation) getConnection().getImplementation(
				TSForeignKeys.COMPONENT_ID);
		Collection tableids = fkeysimpl.loadExportedTables(tmd);
		return addExportedKeys(tmd, tableids);
	}
	*/

	/**
	 * Modifies the given column to take on the attributes of the new column for
	 * the given table. Postgres supports altering 3 attributes of a column: 1.
	 * column name 2. nullable 3. default value So, we check for changes to
	 * these three attributes and generate/execute the SQL accordingly
	 * 
	 * @param tableId
	 *            the id of the table we are altering
	 * @param newColumn
	 *            the new column attributes
	 * @param oldColumn
	 *            the old column attributes.
	 */
	public void modifyColumn(TableId tableId, ColumnMetaData newColumn, ColumnMetaData oldColumn) throws SQLException {
		LinkedList sqlcmds = new LinkedList();

		// alter the default value
		String newdefault = newColumn.getDefaultValue();
		if (newdefault == null)
			newdefault = "";
		else
			newdefault = newdefault.trim();

		String olddefault = oldColumn.getDefaultValue();
		if (olddefault == null)
			olddefault = "";
		else
			olddefault = olddefault.trim();

		if (!I18N.equals(newdefault, olddefault)) {

			StringBuffer sqlbuff = new StringBuffer();
			sqlbuff.append("ALTER TABLE ");
			sqlbuff.append(tableId.getFullyQualifiedName());
			sqlbuff.append(" ALTER COLUMN ");
			sqlbuff.append(oldColumn.getName());
			if (newdefault.length() == 0) {
				sqlbuff.append(" DROP DEFAULT");
			} else {
				sqlbuff.append(" SET DEFAULT ");
				sqlbuff.append(newdefault);
			}

			sqlbuff.append(';');
			sqlcmds.add(sqlbuff.toString());
		}

		/** SET/DROP NOT NULL is only supporter in Postgres 7.3 or better */
		if (getConnection().supportsSchemas()) {
			if (newColumn.isNullable() != oldColumn.isNullable()) {
				// alter the nullable field
				StringBuffer sqlbuff = new StringBuffer();
				sqlbuff.append("ALTER TABLE ");
				sqlbuff.append(tableId.getFullyQualifiedName());
				sqlbuff.append(" ALTER COLUMN ");
				sqlbuff.append(oldColumn.getName());
				if (oldColumn.isNullable()) {
					sqlbuff.append(" SET NOT NULL ");
				} else {
					sqlbuff.append(" DROP NOT NULL ");
				}
				sqlbuff.append(';');
				sqlcmds.add(sqlbuff.toString());
			}
		}

		if (!I18N.equals(newColumn.getName(), oldColumn.getName())) {
			// alter column name
			StringBuffer sqlbuff = new StringBuffer();
			sqlbuff.append("ALTER TABLE ");
			sqlbuff.append(tableId.getFullyQualifiedName());
			sqlbuff.append(" RENAME COLUMN ");
			sqlbuff.append(oldColumn.getName());
			sqlbuff.append(" TO ");
			sqlbuff.append(newColumn.getName());
			sqlcmds.add(sqlbuff.toString());
		}

		if (sqlcmds.size() > 0) {
			SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Alter Table"), sqlcmds);
		}
	}

	/**
	 * Renames a table to a new table name. This is database dependent
	 */
	public void renameTable(TableId newName, TableId oldName) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(oldName.getFullyQualifiedName());
		sql.append(" RENAME TO ");
		sql.append(newName.getFullyQualifiedName());
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Rename Table"), sql.toString());
	}
}
