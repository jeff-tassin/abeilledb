package com.jeta.abeille.gui.jdbc;

import java.awt.BorderLayout;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableSorter;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays raw JDBC information about columns in a result set. This
 * information is useful for a developer or when debugging the program/driver
 * 
 * @author Jeff Tassin
 */
public class ResultSetInfoPanel extends JPanel {
	private DefaultTableModel m_model; // the model for holding the JDBC info
	private JTable m_table; // the table that displays the JDBC info
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public ResultSetInfoPanel(TSConnection tsconn) {
		m_connection = tsconn;
		initialize();
	}

	/**
	 * Adds a row to the table
	 */
	void addRow(ResultSetMetaData metadata, int column) throws SQLException {
		final Object[] row = new Object[21];
		row[0] = metadata.getColumnName(column);

		try {
			row[1] = String.valueOf(metadata.getColumnType(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			/**
			 * for some reason the Sybase driver has problems here and never
			 * returns from this call
			 */
			if (!Database.SYBASE.equals(m_connection.getDatabase())) {
				row[2] = metadata.getColumnTypeName(column);
			}
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[3] = DbUtils.getJDBCTypeName((short) metadata.getColumnType(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		row[4] = metadata.getColumnLabel(column);
		try {
			row[5] = String.valueOf(metadata.getPrecision(column));
		} catch (Throwable e) {
			TSUtils.printException(e);
		}

		try {
			row[6] = String.valueOf(metadata.getScale(column));
		} catch (Throwable e) {
			TSUtils.printException(e);
		}

		try {
			row[7] = metadata.getCatalogName(column);
		} catch (Throwable e) {
		}

		try {
			row[8] = metadata.getSchemaName(column);
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[9] = metadata.getTableName(column);
		} catch (Throwable e) {

		}

		try {
			row[10] = metadata.getColumnClassName(column);
		} catch (Exception sqle) {
			row[10] = I18N.getLocalizedMessage("not implemented");
		}

		try {
			row[11] = String.valueOf(metadata.getColumnDisplaySize(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[12] = String.valueOf(metadata.isAutoIncrement(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[13] = String.valueOf(metadata.isCaseSensitive(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[14] = String.valueOf(metadata.isCurrency(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[15] = String.valueOf(metadata.isDefinitelyWritable(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[16] = String.valueOf(metadata.isNullable(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[17] = String.valueOf(metadata.isReadOnly(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[18] = String.valueOf(metadata.isSearchable(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[19] = String.valueOf(metadata.isSigned(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		try {
			row[20] = String.valueOf(metadata.isWritable(column));
		} catch (Throwable e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

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
		String[] cols = new String[21];
		cols[0] = I18N.getLocalizedMessage("Column Name"); // getColumnName
		cols[1] = I18N.getLocalizedMessage("Data Type"); // getColumnType
		cols[2] = I18N.getLocalizedMessage("Type Name"); // getColumnTypeName
		cols[3] = I18N.getLocalizedMessage("JDBC Type"); // getColumnType
		cols[4] = I18N.getLocalizedMessage("Column Label"); // getColumnLabel
		cols[5] = I18N.getLocalizedMessage("Precision"); // getPrecision
		cols[6] = I18N.getLocalizedMessage("Scale"); // getScale
		cols[7] = I18N.getLocalizedMessage("Catalog"); // getCatalogName
		cols[8] = I18N.getLocalizedMessage("Schema"); // getSchemaName
		cols[9] = I18N.getLocalizedMessage("Table"); // getTableName
		cols[10] = I18N.getLocalizedMessage("Java Class"); // getColumnClassName
		cols[11] = I18N.getLocalizedMessage("Column Display Size"); // getColumnDisplaySize
		cols[12] = I18N.getLocalizedMessage("Auto Increment"); // isAutoIncrement
		cols[13] = I18N.getLocalizedMessage("Case Sensitive"); // isCaseSensitive
		cols[14] = I18N.getLocalizedMessage("Currency"); // isCurrency
		cols[15] = I18N.getLocalizedMessage("Definitely Writable"); // isDefinitelyWritable
		cols[16] = I18N.getLocalizedMessage("Nullable"); // int isNullable
		cols[17] = I18N.getLocalizedMessage("Read Only"); // isReadOnly
		cols[18] = I18N.getLocalizedMessage("Searchable"); // isSearchable
		cols[19] = I18N.getLocalizedMessage("Signed"); // isSigned
		cols[20] = I18N.getLocalizedMessage("Writable"); // isWritable

		m_model = new DefaultTableModel(cols, 0) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		// TableSorter sorter = new TableSorter( m_model );
		// m_table = new JTable( sorter );
		// sorter.addMouseListenerToHeaderInTable( m_table );

		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_model, true);
		m_table = tpanel.getTable();
		TableUtils.setRowHeader(m_table);
		m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableUtils.setColumnWidths(m_table);
		return tpanel;

		// JScrollPane scrollpane = new JScrollPane( m_table );
		// return scrollpane;
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
	 * @throws SQLException
	 *             if an error occurs
	 */
	public void refresh(ResultSetMetaData metadata) throws SQLException {
		clear();
		try {
			int count = metadata.getColumnCount();
			for (int column = 1; column <= count; column++) {
				addRow(metadata, column);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
