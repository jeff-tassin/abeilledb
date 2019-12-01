package com.jeta.plugins.abeille.pointbase.gui.constraints;

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
 * This class is the table model for PointBase check constraints for a given
 * table.
 * 
 * @author Jeff Tassin
 */
public class PointBaseConstraintsModel extends MetaDataTableModel {
	/** column definitions */
	static final int DEFINITION_COLUMN = 0; // definition

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 */
	public PointBaseConstraintsModel(TSConnection connection, TableId tableId) {
		super(connection, tableId);

		String[] names = { I18N.getLocalizedMessage("Definition") };

		Class[] types = { String.class };

		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		PointBaseConstraint c = (PointBaseConstraint) getRow(row);
		switch (column) {
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

			StringBuffer sqlbuff = new StringBuffer();

			Connection conn = getConnection().getMetaDataConnection();
			Statement stmt = null;
			try {
				stmt = conn.createStatement();

				sqlbuff.append("SELECT SYSSCHEMATA.SCHEMANAME, SYSTABLES.TABLENAME, SYSCHECKCONSTRAINTS.CHECKCLAUSE FROM POINTBASE.SYSCHECKCONSTRAINTS, POINTBASE.SYSSCHEMATA, POINTBASE.SYSTABLES\n");
				sqlbuff.append("WHERE SYSSCHEMATA.SCHEMAID=SYSTABLES.SCHEMAID\n");
				sqlbuff.append("AND SYSSCHEMATA.SCHEMAID=SYSCHECKCONSTRAINTS.SCHEMAID\n");
				sqlbuff.append("AND SYSTABLES.TABLEID=SYSCHECKCONSTRAINTS.TABLEID\n");
				sqlbuff.append("AND POINTBASE.SYSSCHEMATA.SCHEMANAME = '");
				sqlbuff.append(tableid.getSchema().getName());
				sqlbuff.append("' AND POINTBASE.SYSTABLES.TABLENAME = '");
				sqlbuff.append(tableid.getTableName());
				sqlbuff.append("'");

				ResultSet rset = stmt.executeQuery(sqlbuff.toString());
				while (rset.next()) {
					String def = rset.getString("CHECKCLAUSE");
					PointBaseConstraint check = new PointBaseConstraint();
					check.setDefinition(def);
					addRow(check);
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
