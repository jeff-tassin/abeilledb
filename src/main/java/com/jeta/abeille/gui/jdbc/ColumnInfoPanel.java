package com.jeta.abeille.gui.jdbc;

import java.util.*;
import java.sql.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSDatabaseUsers;
import com.jeta.abeille.database.model.TSForeignKeys;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays raw JDBC information about columns in a table. This
 * information is useful for a developer or when debugging the program/driver
 * 
 * @author Jeff Tassin
 */
public class ColumnInfoPanel extends TSPanel {
	private DefaultTableModel m_model; // the model for holding the JDBC info
	private JTable m_table; // the table that displays the JDBC info

	public ColumnInfoPanel() {
		initialize();
	}

	/**
	 * Adds a row to the table
	 */
	void addRow(String colName, int dataType, String typeName, int colSize, int decimalDigits, String nullable, int ord) {

		Object[] row = new Object[8];
		row[0] = colName;
		row[1] = TSUtils.getInteger(dataType);
		row[2] = typeName;
		row[3] = DbUtils.getJDBCTypeName(dataType);
		row[4] = TSUtils.getInteger(colSize);
		row[5] = TSUtils.getInteger(decimalDigits);
		row[6] = nullable;
		row[7] = new Integer(ord);
		m_model.addRow(row);
	}

	/**
	 * Deletes all existing rows in the table
	 */
	void clear() {
		m_model.setRowCount(0);
	}

	/**
	 * Creates the JTable that displays the JDBC driver properties
	 * 
	 * @returns the table component
	 */
	private JComponent createInfoTable() {
		String[] cols = new String[8];

		cols[0] = "COLUMN_NAME";
		cols[1] = "DATA_TYPE";
		cols[2] = "TYPE_NAME";
		cols[3] = "JDBC_TYPE";
		cols[4] = "COLUMN_SIZE";
		cols[5] = "DECIMAL_DIGITS";
		cols[6] = "NULLABLE";
		cols[7] = "ORDINAL_POSITION";

		m_model = new DefaultTableModel(cols, 0) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		TSTablePanel tpanel = TableUtils.createSimpleTable(m_model, true);
		m_table = tpanel.getTable();
		m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JScrollPane scrollpane = new JScrollPane(m_table);
		// m_table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		TableUtils.setColumnWidths(m_table);
		return scrollpane;
		// return m_table;
	}

	/**
	 * Creates the components on this panel
	 */
	private void initialize() {
		setLayout(new BorderLayout());
		add(createInfoTable(), BorderLayout.CENTER);
	}

	/**
	 * Updates the view with the information about the columns of a given
	 * database table
	 * 
	 * @param conn
	 *            the connection to display information about
	 * @param tableId
	 *            the id of the table whose columns we want to display
	 */
	public void refresh(Connection conn, TableId tableId) {
		clear();

		if (tableId == null)
			return;

		String tablename = tableId.getTableName();
		if (tablename == null)
			return;

		tablename = tablename.trim();
		if (tablename.length() == 0)
			return;

		ResultSet colset = null;
		try {
			DatabaseMetaData metadata = conn.getMetaData();
			// @todo format the results here
			colset = metadata.getColumns(tableId.getCatalog().getName(), tableId.getSchema().getName(),
					tableId.getTableName(), null);
			while (colset.next()) {
				addRow(colset.getString("COLUMN_NAME"), colset.getInt("DATA_TYPE"), colset.getString("TYPE_NAME"),
						colset.getInt("COLUMN_SIZE"), colset.getInt("DECIMAL_DIGITS"),
						String.valueOf(colset.getBoolean("NULLABLE")), colset.getInt("ORDINAL_POSITION"));

			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (colset != null)
					colset.close();
			} catch (Exception e) {
				// eat it
			}
		}
	}

}
