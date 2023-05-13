package com.jeta.plugins.abeille.pointbase.gui.triggers;

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
 * This class is the table model for PointBase Triggers for a given table.
 * 
 * @author Jeff Tassin
 */
public class PointBaseTriggersModel extends MetaDataTableModel {
	/** column definitions */
	static final int NAME_COLUMN = 0; // trigger name

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 */
	public PointBaseTriggersModel(TSConnection connection, TableId tableId) {
		super(connection, tableId);

		String[] names = { I18N.getLocalizedMessage("Name") };

		Class[] types = { String.class };

		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		PointBaseTrigger t = (PointBaseTrigger) getRow(row);
		switch (column) {
		case NAME_COLUMN:
			return t.getName();

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

		if (true)
			return;

		try {
			// now get the index
			StringBuffer sqlbuff = new StringBuffer();

			Connection conn = getConnection().getMetaDataConnection();
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				ResultSet rset = stmt.executeQuery(sqlbuff.toString());
				while (rset.next()) {

				}
				getConnection().jetaCommit(conn);
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
