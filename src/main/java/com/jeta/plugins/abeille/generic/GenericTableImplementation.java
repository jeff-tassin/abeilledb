package com.jeta.plugins.abeille.generic;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Database;
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

import com.jeta.foundation.i18n.I18N;
import com.jeta.plugins.abeille.standard.DefaultTableImplementation;

/**
 * This is the HSQL implementation for the TSTable interface
 * 
 * @author Jeff Tassin
 */
public class GenericTableImplementation extends DefaultTableImplementation {
	/**
	 * ctor
	 */
	public GenericTableImplementation() {

	}

	/**
	 * ctor
	 */
	public GenericTableImplementation(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * @return the SQL command to create the table represented by the table
	 *         metadata object. This is database dependent
	 */
	public String createTableSQL(TableMetaData tmd) throws SQLException {
		// first, primary keys don't allow the NOT NULL flag, so let's override
		// NOT NULL if this is set
		// for a primary key column
		String tablename = tmd.getTableName();
		tablename = convertCase(tablename);
		tmd.setTableName(tablename);

		DbKey pk = tmd.getPrimaryKey();
		if (pk != null) {
			Collection cols = pk.getColumns();
			Iterator iter = cols.iterator();
			while (iter.hasNext()) {
				String colname = (String) iter.next();
				ColumnMetaData cmd = tmd.getColumn(colname);
				if (cmd != null) {
					cmd.setNullable(false);
				}
			}
		}

		StringBuffer sql = new StringBuffer();
		sql.append(getCreateTableSQL(tmd));
		sql.append("\n");
		return sql.toString();
	}

	/**
	 * oracle only supports ON DELETE - cascade, set null
	 * 
	 * /** Creates the given table in the database. We assume that SQL is in
	 * English only.
	 * 
	 * @param tmd
	 *            the table metadata definition to create
	 * @throws SQLException
	 *             is a database error occurs.
	 */
	public String getCreateTableSQL(TableMetaData tmd) throws SQLException {
		DbKey pk = tmd.getPrimaryKey();
		boolean haspk = (pk != null && pk.getColumnCount() > 0);

		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE ");
		sql.append(tmd.getFullyQualifiedName());
		sql.append(" (\n");
		Collection cols = tmd.getColumns();
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();

			sql.append(getCreateColumnSQL(cmd));

			if (iter.hasNext())
				sql.append(",\n");
		}

		if (haspk) {
			StringBuffer pkbuff = new StringBuffer();

			pkbuff.append(",\nCONSTRAINT ");
			pkbuff.append(getSafePrimaryKeyName(tmd));
			pkbuff.append(" PRIMARY KEY( ");

			cols = pk.getColumns();
			iter = cols.iterator();
			while (iter.hasNext()) {
				String colname = (String) iter.next();
				pkbuff.append(colname);
				if (iter.hasNext()) {
					pkbuff.append(", ");
				} else {
					pkbuff.append(" )");
				}
			}

			sql.append(pkbuff.toString());
		}

		// need to do foreign keys here as well
		// FOREIGN KEY ( column [,column...] ) REFERENCES refTable ( column
		// [,column...])
		StringBuffer fkbuff = new StringBuffer();
		iter = tmd.getForeignKeys().iterator();
		while (iter.hasNext()) {
			DbForeignKey fk = (DbForeignKey) iter.next();

			fkbuff.append(",\nFOREIGN KEY (");

			DbKey localkey = fk.getLocalKey();
			Collection fields = localkey.getColumns();
			Iterator fiter = fields.iterator();
			while (fiter.hasNext()) {
				String field = (String) fiter.next();
				fkbuff.append(field);
				if (fiter.hasNext())
					fkbuff.append(", ");
			}

			fkbuff.append(") REFERENCES ");
			TableId reftableid = fk.getReferenceTableId();
			fkbuff.append(reftableid.getFullyQualifiedName());
			fkbuff.append(" (");

			fiter = fields.iterator();
			while (fiter.hasNext()) {
				String pkcolname = fk.getAssignedPrimaryKeyColumnName((String) fiter.next());
				fkbuff.append(pkcolname);
				if (fiter.hasNext())
					fkbuff.append(", ");
			}
			fkbuff.append(")");

			ForeignKeyConstraints fc = (ForeignKeyConstraints) fk.getConstraints();
			if (fc != null) {
				fkbuff.append(" ");
				int deletea = fc.getDeleteAction();
				if (deletea != java.sql.DatabaseMetaData.importedKeyNoAction) {
					fkbuff.append("ON DELETE ");
					fkbuff.append(ForeignKeyConstraints.toActionSQL(deletea));
				}

				int updatea = fc.getUpdateAction();
				if (updatea != java.sql.DatabaseMetaData.importedKeyNoAction) {
					fkbuff.append(" ON UPDATE ");
					fkbuff.append(ForeignKeyConstraints.toActionSQL(updatea));
				}
				fkbuff.append(" ");
				if (fc.isDeferrable()) {
					fkbuff.append(" DEFERRABLE ");
					if (fc.isInitiallyDeferred())
						fkbuff.append("INITIALLY DEFERRED ");
					else
						fkbuff.append("INITIALLY IMMEDIATE ");
				}
			}
		}

		sql.append(fkbuff.toString());
		sql.append(" );");
		return sql.toString();
	}

	/**
	 * Renames a table to a new table name. This is database dependent
	 */
	public void renameTable(TableId newName, TableId oldName) throws SQLException {
		/*
		 * StringBuffer sql = new StringBuffer(); Database db = getDatabase();
		 * if ( Database.ORACLE.equals( db ) || Database.DB2.equals( db ) ) {
		 * sql.append( "RENAME TO " ); sql.append(
		 * oldName.getFullyQualifiedName() ); sql.append( " TO " ); sql.append(
		 * newName.getFullyQualifiedName() ); } else { sql.append(
		 * "ALTER TABLE " ); sql.append( oldName.getFullyQualifiedName() );
		 * sql.append( " RENAME TO " ); sql.append(
		 * newName.getFullyQualifiedName() ); } SQLCommand.runMetaDataCommand(
		 * getConnection(), I18N.getLocalizedMessage("Rename Table"),
		 * sql.toString() );
		 */
	}

}
