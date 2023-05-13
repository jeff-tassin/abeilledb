package com.jeta.plugins.abeille.oracle.gui.constraints;

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
 * This class is the table model for Oracle check constraints for a given table.
 * 
 * @author Jeff Tassin
 */
public class OracleConstraintsModel extends MetaDataTableModel {
	/** column definitions */
	static final int NAME_COLUMN = 0; // check name
	static final int TYPE_COLUMN = 1; // type of constriant (e.g. unique, check,
										// etc. )
	static final int DEFINITION_COLUMN = 2; // definition
	static final int LAST_MODIFIED = 3; // last modified timestamp

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 */
	public OracleConstraintsModel(TSConnection connection, TableId tableId) {
		super(connection, tableId);

		String[] names = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Type"),
				I18N.getLocalizedMessage("Definition"), I18N.getLocalizedMessage("Modified") };

		Class[] types = { String.class, String.class, String.class, String.class };

		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		OracleConstraint c = (OracleConstraint) getRow(row);
		switch (column) {
		case NAME_COLUMN:
			return c.getName();

		case TYPE_COLUMN:
			return OracleConstraint.getTypeDescription(c.getType());

		case DEFINITION_COLUMN:
			return c.getDefinition();

		case LAST_MODIFIED:
			Timestamp ts = c.getLastModified();
			if (ts == null)
				return "";
			else
				return ts.toString();

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
			// select * from SYS.ALL_CONSTRAINTS where OWNER='JETA' and
			// TABLE_NAME='IMAGES' and (CONSTRAINT_TYPE='C' or CONSTRAINT_TYPE =
			// 'V')

			sqlbuff.append("select * from SYS.ALL_CONSTRAINTS where OWNER='");
			sqlbuff.append(tableid.getSchema().getName());
			sqlbuff.append("' and TABLE_NAME = '");
			sqlbuff.append(tableid.getTableName());
			sqlbuff.append("' and (CONSTRAINT_TYPE='C' or CONSTRAINT_TYPE = 'V' or CONSTRAINT_TYPE = 'U' or CONSTRAINT_TYPE = 'O' )");

			Connection conn = getConnection().getMetaDataConnection();
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				ResultSet rset = stmt.executeQuery(sqlbuff.toString());
				while (rset.next()) {
					String cname = rset.getString("CONSTRAINT_NAME");
					String def = rset.getString("SEARCH_CONDITION");
					Timestamp ts = rset.getTimestamp("LAST_CHANGE");
					char ctype = TSUtils.getChar(rset.getString("CONSTRAINT_TYPE"));

					OracleConstraint check = new OracleConstraint(cname);
					check.setDefinition(def);
					check.setLastModified(ts);
					check.setType(ctype);
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
