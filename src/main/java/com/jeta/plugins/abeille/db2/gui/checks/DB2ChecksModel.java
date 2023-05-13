package com.jeta.plugins.abeille.db2.gui.checks;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.common.MetaDataTableModel;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is the table model for DB2 check constraints for a given table.
 * 
 * @author Jeff Tassin
 */
public class DB2ChecksModel extends MetaDataTableModel {
	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int DEFINER_COLUMN = 1;
	static final int CREATED_COLUMN = 2;
	static final int PATH_COLUMN = 3;
	static final int DEFINITION_COLUMN = 4;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 */
	public DB2ChecksModel(TSConnection connection, TableId tableId) {
		super(connection, tableId);

		String[] names = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Definer"),
				I18N.getLocalizedMessage("Created"), I18N.getLocalizedMessage("Path"),
				I18N.getLocalizedMessage("Definition") };

		Class[] types = { String.class, String.class, String.class, String.class, String.class };

		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		DB2CheckConstraint c = (DB2CheckConstraint) getRow(row);
		switch (column) {
		case NAME_COLUMN:
			return c.getName();

		case DEFINER_COLUMN:
			return c.getDefiner();

		case CREATED_COLUMN:
			Timestamp ts = c.getTimestamp();
			if (ts == null)
				return "";
			else
				return ts.toString();

		case PATH_COLUMN:
			return c.getFunctionPath();

		case DEFINITION_COLUMN:
			return c.getDefinition();

		default:
			return "";
		}
	}

	/**
	 * Reloads the model
	 */
	public void reload() {
		removeAll();

		TableId tableid = getTableId();

		if (tableid == null) {
			fireTableDataChanged();
			return;
		}

		try {
			// now get the index
			StringBuffer sqlbuff = new StringBuffer();
			// select * from SYSCAT.CHECKS where TABSCHEMA = 'JETA' and
			// TABNAME='IMAGES' and TYPE='C';
			sqlbuff.append("select * from SYSCAT.CHECKS where TABSCHEMA = '");
			sqlbuff.append(tableid.getSchema().getName());
			sqlbuff.append("' and TABNAME = '");
			sqlbuff.append(tableid.getTableName());
			sqlbuff.append("' and TYPE = 'C'");

			Connection conn = getConnection().getMetaDataConnection();
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				ResultSet rset = stmt.executeQuery(sqlbuff.toString());
				while (rset.next()) {
					String name = rset.getString("CONSTNAME");
					String definer = rset.getString("DEFINER");
					Timestamp ts = rset.getTimestamp("CREATE_TIME");
					String func_path = rset.getString("FUNC_PATH");
					String def = DbUtils.getCharacterData(rset.getClob("TEXT"));

					DB2CheckConstraint check = new DB2CheckConstraint(name);
					check.setDefiner(definer);
					check.setTimestamp(ts);
					check.setFunctionPath(func_path);
					check.setDefinition(def);

					addRow(check);
				}
				conn.commit();
			} finally {
				if (stmt != null)
					stmt.close();
			}
		} catch (SQLException se) {
			TSUtils.printException(se);
		}
		fireTableDataChanged();
	}

	/**
	 * Sets the current table id for the model. Reloads the triggers.
	 */
	public void setTableId(TableId tableId) {
		super.setTableId(tableId);
		reload();

	}
}
