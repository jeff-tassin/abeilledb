package com.jeta.plugins.abeille.mysql;

import java.io.StringReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.ForeignKeyConstraints;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.abeille.parsers.InputParser;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.standard.DefaultTableImplementation;

/**
 * This is the MySQL implementation for the database interface
 * 
 * @author Jeff Tassin
 */
public class MySQLTableImplementation extends DefaultTableImplementation {

	/**
	 * ctor
	 */
	public MySQLTableImplementation() {

	}

	/**
	 * ctor
	 */
	public MySQLTableImplementation(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * Creates the given column in the given table
	 */
	public void createColumn(TableId tableId, ColumnMetaData newColumn) throws SQLException {
		// ALTER TABLE tbl_name ADD [COLUMN] create_definition [FIRST | AFTER
		// column_name ]

		MySQLDatabaseImplementation dbimpl = (MySQLDatabaseImplementation) getConnection().getImplementation(
				TSDatabase.COMPONENT_ID);

		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(tableId.getFullyQualifiedName());
		sql.append(" ADD COLUMN ");
		sql.append(getCreateColumnDefinition(dbimpl, newColumn, true));
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Add Column"), sql.toString());
	}

	/**
	 * Creates the SQL to create the given table in MySQL
	 */
	public String createTableSQL(TableMetaData tmd) throws SQLException {

		// col_name type [NOT NULL | NULL] [DEFAULT default_value]
		// [AUTO_INCREMENT]
		MySQLDatabaseImplementation dbimpl = (MySQLDatabaseImplementation) getConnection().getImplementation(
				TSDatabase.COMPONENT_ID);

		String tablename = tmd.getTableName();
		tablename = convertCase(tablename);
		tmd.setTableName(tablename);

		StringBuffer sql = new StringBuffer("CREATE TABLE ");
		sql.append(convertCase(tmd.getFullyQualifiedName()));
		sql.append(" (\n");
		Collection cols = tmd.getColumns();
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			sql.append(getCreateColumnDefinition(dbimpl, cmd, false));
			if (iter.hasNext())
				sql.append(",\n");
		}

		DbKey pk = tmd.getPrimaryKey();
		boolean haspk = (pk != null && pk.getColumnCount() > 0);
		if (haspk) {
			sql.append(",\n");
			sql.append(getCreatePrimaryKey(pk));
		}

		/** now add foreign key definitions */
		Collection fkeys = tmd.getForeignKeys();
		if (fkeys.size() > 0) {
			sql.append(",\n");
		}
		iter = fkeys.iterator();
		while (iter.hasNext()) {
			DbForeignKey fkey = (DbForeignKey) iter.next();

			/**
			 * MySQL requires that any columns in the local table that are part
			 * of a foreign key MUST be indexed. Since the modeler does not
			 * allow creation of indexes, we need to automatically do it here
			 */
			sql.append(getCreateIndex(fkey));
			sql.append(getCreateForeignKey(fkey));
			if (iter.hasNext()) {
				sql.append(",\n");
			}
		}
		sql.append(" )");

		MySQLTableAttributes attr = (MySQLTableAttributes) tmd.getAttributes();
		MySQLTableType ttype = MySQLTableType.MyISAM;
		if (attr != null) {
			sql.append("\nTYPE = ");
			ttype = attr.getTableType();
			if (ttype != null && ttype != MySQLTableType.Unknown) {
				sql.append(ttype.getName());
			} else {
				sql.append(MySQLTableType.MyISAM.getName());
			}
		}

		sql.append(";");
		return sql.toString();
	}

	/**
	 * Drops the given column from the given table
	 */
	public void dropColumn(TableId tableId, ColumnMetaData dropColumn, boolean cascade) throws SQLException {
		// ALTER [IGNORE] TABLE tbl_name alter_spec [, alter_spec ...]
		// alter_specification:
		// DROP [COLUMN] col_name

		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(tableId.getFullyQualifiedName());
		sql.append(" DROP COLUMN ");
		sql.append(dropColumn.getColumnName());
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Drop Column"), sql.toString());
	}

	/**
	 * Drops the primary key for the given table
	 */
	public void dropPrimaryKey(TableId tableid, boolean cascade) throws SQLException {
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Drop Primary Key"),
				getDropPrimaryKey(tableid, cascade));
	}

	/**
	 * @param cmd
	 *            the definition of the column we are adding
	 * @param alter
	 *            set to true if we are altering an existing table.
	 * @return the SQL used for creating a column
	 */
	private String getCreateColumnDefinition(MySQLDatabaseImplementation dbimpl, ColumnMetaData cmd, boolean alter) {
		StringBuffer sql = new StringBuffer();

		sql.append(cmd.getColumnName());
		sql.append(" ");

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
			if (typename.equalsIgnoreCase("CHAR BINARY")) {
				if (cmd.getColumnSize() > 0) {
					sql.append("CHAR(");
					sql.append(cmd.getColumnSize());
					sql.append(") BINARY");
				} else {
					sql.append(typename);
				}
			} else if (typename.equalsIgnoreCase("VARCHAR BINARY")) {
				if (cmd.getColumnSize() > 0) {
					sql.append("VARCHAR(");
					sql.append(cmd.getColumnSize());
					sql.append(") BINARY");
				} else {
					sql.append(typename);
				}
			} else if (typename.equalsIgnoreCase("ENUM") || typename.equalsIgnoreCase("SET")) {
				MySQLColumnAttributes attr = (MySQLColumnAttributes) cmd.getAttributes();
				sql.append(typename);
				sql.append('(');
				if (attr != null) {
					try {
						/**
						 * enum and set parameters are stored as a single
						 * comma-delimited string in the colum attributes. so,
						 * we parse the string here
						 */
						String params = attr.getParameters();
						if (params != null) {
							StringReader reader = new StringReader(params);
							InputParser parser = new InputParser(reader);
							parser.parse();
							Collection tokens = parser.getTokens();
							Iterator iter = tokens.iterator();
							while (iter.hasNext()) {
								String token = (String) iter.next();
								if (token.charAt(0) != '\'' || token.charAt(token.length() - 1) != '\'') {
									token = DbUtils.toSQL(token, '\'');
								}
								sql.append(token);
								if (iter.hasNext())
									sql.append(',');
							}
						}
					} catch (Exception e) {
						TSUtils.printException(e);
						String params = attr.getParameters();
						if (params != null)
							sql.append(params);
					}
				}
				sql.append(')');
			} else if (typeinfo.supportsCustomPrecision() || typeinfo.supportsCustomScale()) {
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

		MySQLColumnAttributes attr = (MySQLColumnAttributes) cmd.getAttributes();
		if (attr != null) {
			if (attr.isUnsigned()) {
				sql.append(" UNSIGNED");
			}

			if (attr.isZeroFill()) {
				sql.append(" ZEROFILL");
			}
		}

		if (cmd.isNullable())
			sql.append(" NULL");
		else
			sql.append(" NOT NULL");

		String def_value = TSUtils.fastTrim(cmd.getDefaultValue());
		if (def_value.length() > 0) {
			sql.append(" DEFAULT ");
			def_value = TSUtils.strip(def_value, "\'");
			sql.append(DbUtils.toSQL(def_value, '\''));
		}

		if (cmd.isAutoIncrement())
			sql.append(" AUTO_INCREMENT");

		if (alter && attr != null) {
			String aftercol = TSUtils.fastTrim(attr.getAlterColumnPosition());
			if (aftercol.equalsIgnoreCase("FIRST")) {
				sql.append(" FIRST");
			} else if (aftercol.length() > 0) {
				sql.append(" AFTER ");
				sql.append(aftercol);
			}
		}
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
	public String getCreatePrimaryKey(DbKey pk) throws SQLException {
		if (pk != null && pk.getColumnCount() > 0) {
			StringBuffer pkbuff = new StringBuffer();
			pkbuff.append("PRIMARY KEY( ");

			Collection cols = pk.getColumns();
			Iterator iter = cols.iterator();
			while (iter.hasNext()) {
				String colname = (String) iter.next();
				pkbuff.append(colname);
				if (iter.hasNext()) {
					pkbuff.append(", ");
				} else {
					pkbuff.append(" )");
				}
			}
			return pkbuff.toString();
		} else {
			return "";
		}
	}

	/**
	 * Creates the foreign key SQL for use in an ALTER TABLE or CREATE TABLE
	 * 
	 * @param tmd
	 *            the table metadata definition to create
	 * @throws SQLException
	 *             is a database error occurs.
	 */
	public String getCreateForeignKey(DbForeignKey fkey) throws SQLException {
		// FOREIGN KEY (col1,col2) REFERENCES table_naem (col1,col2) [ON DELETE
		// action][ON UPDATE action][MATCH FULL | MATCH PARTIAL]
		StringBuffer sbuff = new StringBuffer();

		DbKey localkey = fkey.getLocalKey();
		sbuff.append("FOREIGN KEY (");
		Iterator iter = localkey.getColumns().iterator();
		while (iter.hasNext()) {
			String colname = (String) iter.next();
			sbuff.append(colname);
			if (iter.hasNext())
				sbuff.append(",");
		}

		sbuff.append(") REFERENCES ");
		sbuff.append(fkey.getReferenceTableId().getFullyQualifiedName());
		sbuff.append("(");
		iter = localkey.getColumns().iterator();
		while (iter.hasNext()) {
			String colname = (String) iter.next();
			String refname = fkey.getAssignedPrimaryKeyColumnName(colname);
			sbuff.append(refname);
			if (iter.hasNext())
				sbuff.append(",");
		}

		sbuff.append(")");

		ForeignKeyConstraints fc = (ForeignKeyConstraints) fkey.getConstraints();
		if (fc != null) {
			int deletea = fc.getDeleteAction();
			sbuff.append(" ON DELETE ");
			sbuff.append(ForeignKeyConstraints.toActionSQL(deletea));

			int updatea = fc.getUpdateAction();
			sbuff.append(" ON UPDATE ");
			sbuff.append(ForeignKeyConstraints.toActionSQL(updatea));
			sbuff.append(" ");
		}
		return sbuff.toString();
	}

	/**
	 * MySQL requires that any columns in the local table that are part of a
	 * foreign key MUST be indexed. Since the modeler does not allow creation of
	 * indexes, we need to automatically do it here
	 */
	private String getCreateIndex(DbForeignKey fkey) {
		StringBuffer sql = new StringBuffer();
		sql.append("INDEX (");

		DbKey localkey = fkey.getLocalKey();
		Iterator iter = localkey.getColumns().iterator();
		while (iter.hasNext()) {
			sql.append(iter.next());
			if (iter.hasNext())
				sql.append(",");
		}
		sql.append("),\n");
		return sql.toString();
	}

	/**
	 * @return the sql that Drops the primary key for the given table
	 */
	public String getDropPrimaryKey(TableId tableid, boolean cascade) {
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(tableid.getFullyQualifiedName());
		sql.append(" DROP PRIMARY KEY;");
		return sql.toString();
	}

	/**
	 * Loads columns information such as constraints and default values.
	 */
	public void loadColumnsEx(TableMetaData tmd) throws SQLException {
		TSConnection tsconn = getConnection();
		StringBuffer sql = new StringBuffer();

		Catalog catalog = tmd.getCatalog();
		assert (catalog != null);

		sql.append("show table status from ");
		sql.append(catalog.getName());
		sql.append(" like '");
		sql.append(tmd.getTableName());
		sql.append("'");

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = tsconn.getMetaDataConnection();
			stmt = conn.createStatement();
			TSUtils.printDebugMessage( "MySQLTableImpl: " +sql.toString() );
			ResultSet rset = stmt.executeQuery(sql.toString());
			while (rset.next()) {
				// Name,Type,Row_format,Rows,Avg_row_length,Data_length,Max_data_length,Index_length,Data_free,Auto_increment,Create_time,Update_time,Check_time,Create_options,Comment
				// albums,MyISAM,Dynamic,5,48,240,4294967295,4096,0,6,2003-02-19
				// 13:18:54.0,2003-02-19 13:18:54.0,2003-02-19 13:18:54.0,,

				String tname = rset.getString("Name");
				//String ttype = rset.getString("Type");
				String ttype = null;

				if (tname.equals(tmd.getTableName())) {
					MySQLTableAttributes attr = (MySQLTableAttributes) tmd.getAttributes();
					if (attr == null) {
						attr = new MySQLTableAttributes();
						tmd.setAttributes(attr);
					}

					MySQLTableType mysqltt = MySQLTableType.fromString(ttype);
					if (mysqltt == MySQLTableType.Unknown) {
						TSUtils.printMessage("Unknown MySQL table type encounterd for: " + tmd.getTableName()
								+ "  type: " + ttype);
					}
					attr.setTableType(mysqltt);
					break;
				}

			}

			Collection columns = tmd.getColumns();
			Iterator iter = columns.iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				MySQLColumnAttributes attr = (MySQLColumnAttributes) cmd.getAttributes();
				if (attr == null) {
					attr = new MySQLColumnAttributes();
					cmd.setAttributes(attr);
				}

				String typename = TSUtils.fastTrim(cmd.getTypeName()).toLowerCase();
				if (typename.indexOf("unsigned") > 0) {
					attr.setUnsigned(true);
				}

				if (typename.indexOf("zerofill") > 0) {
					attr.setZeroFill(true);
				}
			}
			tsconn.jetaCommit(conn);
		} catch (SQLException se) {
			throw se;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

		/** now call show column status */
		readColumnStatus(tmd);
	}

	/**
	 * Modifies the given column to take on the attributes of the new column for
	 * the given table.
	 */
	public void modifyColumn(TableId tableId, ColumnMetaData newColumn, ColumnMetaData oldColumn) throws SQLException {
		// MODIFY [COLUMN] create_definition [FIRST | AFTER column_name]
		MySQLDatabaseImplementation dbimpl = (MySQLDatabaseImplementation) getConnection().getImplementation(
				TSDatabase.COMPONENT_ID);
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(tableId.getFullyQualifiedName());

		String newname = TSUtils.fastTrim(newColumn.getColumnName());
		String oldname = TSUtils.fastTrim(oldColumn.getColumnName());
		if (newname.equals(oldname)) {
			sql.append(" MODIFY COLUMN ");
		} else {
			sql.append(" CHANGE COLUMN ");
			sql.append(oldname);
			sql.append(" ");
		}
		sql.append(getCreateColumnDefinition(dbimpl, newColumn, true));
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Add Column"), sql.toString());

	}

	/**
	 * Modifies the given primary key.
	 */
	public void modifyPrimaryKey(TableId tableid, DbKey newpk, DbKey oldpk) throws SQLException {
		String sql1 = getDropPrimaryKey(tableid, false);

		String sql2 = null;
		if (newpk != null && newpk.getColumnCount() > 0) {
			StringBuffer sqlbuff = new StringBuffer();
			sqlbuff.append("ALTER TABLE ");
			sqlbuff.append(tableid.getFullyQualifiedName());
			sqlbuff.append(" ADD ");
			sqlbuff.append(getCreatePrimaryKey(newpk));
			sqlbuff.append(';');
			sql2 = sqlbuff.toString();
		}

		if (sql2 == null) {
			SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Modify Primary Key"), sql1);
		} else {
			SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Modify Primary Key"), sql1, sql2);
		}
	}

	/**
	 * Loads additional columns information such as enum and set values
	 */
	public void readColumnStatus(TableMetaData tmd) throws SQLException {
		TSConnection tsconn = getConnection();
		StringBuffer sql = new StringBuffer();

		Catalog catalog = tmd.getCatalog();
		assert (catalog != null);

		TableId tableid = tmd.getTableId();
		sql.append("show columns from ");
		sql.append(tableid.getFullyQualifiedName());

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = tsconn.getMetaDataConnection();
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql.toString());
			while (rset.next()) {
				String tname = rset.getString("Field");
				String ttype = TSUtils.fastTrim(rset.getString("Type"));

				try {
					/**
					 * the format of ttype for MySQL enum/set: enum('val1',
					 * 'val2', ..., 'valN' ) or set('val1, 'val2', ..., 'valN' )
					 */
					int trim_len = 0;
					int pos = ttype.indexOf("enum(");
					if (pos >= 0) {
						trim_len = "enum(".length();
					} else {
						pos = ttype.indexOf("set(");
						if (pos >= 0) {
							trim_len = "set(".length();
						}
					}

					if (pos >= 0) {
						ttype = ttype.substring(trim_len, ttype.length());
						if (ttype.charAt(ttype.length() - 1) == ')') {
							ttype = ttype.substring(0, ttype.length() - 1);
						}
						ColumnMetaData cmd = tmd.getColumn(tname);
						if (cmd != null) {
							MySQLColumnAttributes attr = MySQLUtils.getAttributes(cmd);
							attr.setParameters(ttype);
						}
					}
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}
			tsconn.jetaCommit(conn);
		} catch (SQLException se) {
			throw se;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
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
