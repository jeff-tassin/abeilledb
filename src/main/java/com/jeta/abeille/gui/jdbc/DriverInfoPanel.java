package com.jeta.abeille.gui.jdbc;

import java.awt.BorderLayout;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import java.lang.reflect.Method;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays JDBC driver/connection properties in a JTable. This
 * information is useful for a developer or when debugging the program
 * 
 * @author Jeff Tassin
 */
public class DriverInfoPanel extends JPanel {
	private DefaultTableModel m_model; // the model for holding the JDBC info
	private JTable m_table; // the table that displays the JDBC info

	/** for sorting during refresh */
	private TreeMap m_values = new TreeMap();

	public DriverInfoPanel() {
		initialize();
	}

	/**
	 * Adds a row to the table
	 * 
	 * @param property
	 *            the JDBC property
	 * @param value
	 *            the value that corresonds to the property
	 */
	void addRow(String property, String value) {
		m_values.put(property, value);
	}

	/**
	 * Deletes all existing rows in the table
	 */
	void clear() {
		m_model.setRowCount(0);
		m_values.clear();
	}

	/**
	 * Creates the JTable that displays the JDBC driver properties
	 * 
	 * @returns the table component
	 */
	private JComponent createInfoTable() {
		String[] cols = new String[2];
		cols[0] = I18N.getLocalizedMessage("Property");
		cols[1] = I18N.getLocalizedMessage("Value");

		m_model = new DefaultTableModel(cols, 0) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		TSTablePanel tablepanel = TableUtils.createSimpleTable(m_model, true);
		m_table = tablepanel.getTable();
		JScrollPane scrollpane = new JScrollPane(m_table);
		m_table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		m_table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		m_table.setShowGrid(false);
		return scrollpane;
	}

	/**
	 * Creates the components on this panel
	 */
	private void initialize() {
		setLayout(new BorderLayout());
		add(createInfoTable(), BorderLayout.CENTER);
	}

	/**
	 * Updates the table with the information about the driver and connection
	 * 
	 * @param conn
	 *            the connection to display information about
	 */
	public void refresh(Connection conn) {
		clear();

		try {
			TreeSet ignoremethods = new TreeSet();
			ignoremethods.add("getDefaultTransactionIsolation");
			ignoremethods.add("supportsResultSetConcurrency");
			ignoremethods.add("supportsResultSetHoldability");
			ignoremethods.add("supportsResultSetType");

			DatabaseMetaData metadata = conn.getMetaData();
			Method[] methods = DatabaseMetaData.class.getMethods();
			for (int index = 0; index < methods.length; index++) {
				Object[] params = new Object[0];
				Method m = methods[index];
				Class rtype = m.getReturnType();
				if (m.getParameterTypes().length == 0 && (rtype.isPrimitive() || rtype == String.class)) {
					try {
						Object obj = m.invoke(metadata, params);
						if (obj == null)
							obj = "null";
						else if (!ignoremethods.contains(m.getName())) {
							addRow(m.getName(), obj.toString());
						}
					} catch (Exception e) {

					} catch (AbstractMethodError ae) {

					}
				}
			}

			getDefaultTransactionIsolation(metadata);
			supportsResultSetConcurrency(metadata);
			supportsResultSetHoldability(metadata);
			supportsResultSetType(metadata);
			supportsTransactionIsolation(metadata);

			Object[] row = new Object[2];
			Iterator iter = m_values.keySet().iterator();
			while (iter.hasNext()) {
				String prop = (String) iter.next();
				String value = (String) m_values.get(prop);

				row[0] = prop;
				row[1] = value;
				m_model.addRow(row);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// specific metadata methods
	private void getDefaultTransactionIsolation(DatabaseMetaData metadata) {

		try {
			String name = "getDefaultTransactionIsolation";
			int isolation = metadata.getDefaultTransactionIsolation();
			switch (isolation) {
			case Connection.TRANSACTION_NONE:
				addRow(name, "Connection.TRANSACTION_NONE");
				break;

			case Connection.TRANSACTION_READ_COMMITTED:
				addRow(name, "Connection.TRANSACTION_READ_COMMITTED");
				break;

			case Connection.TRANSACTION_READ_UNCOMMITTED:
				addRow(name, "Connection.TRANSACTION_READ_UNCOMMITTED");
				break;

			case Connection.TRANSACTION_REPEATABLE_READ:
				addRow(name, "Connection.TRANSACTION_REPEATABLE_READ");
				break;

			case Connection.TRANSACTION_SERIALIZABLE:
				addRow(name, "Connection.TRANSACTION_SERIALIZABLE");
				break;

			default:
				addRow(name, String.valueOf(isolation));
			}
		} catch (Exception e) {
			// noop
		} catch (Error e) {

		}
	}

	private void supportsResultSetConcurrency(DatabaseMetaData metadata) {
		try {
			String[] rset_type_label = { "TYPE_FORWARD_ONLY", "TYPE_SCROLL_INSENSITIVE", "TYPE_SCROLL_SENSITIVE" };
			int[] rset_type = { ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.TYPE_SCROLL_SENSITIVE };

			for (int index = 0; index < rset_type.length; index++) {
				boolean bresult = metadata.supportsResultSetConcurrency(rset_type[index], ResultSet.CONCUR_READ_ONLY);
				addRow("supportsResultSetConcurrency(" + rset_type_label[index] + ",CONCUR_READ_ONLY)",
						String.valueOf(bresult));
			}

			for (int index = 0; index < rset_type.length; index++) {
				boolean bresult = metadata.supportsResultSetConcurrency(rset_type[index], ResultSet.CONCUR_UPDATABLE);
				addRow("supportsResultSetConcurrency(" + rset_type_label[index] + ",CONCUR_UPDATABLE)",
						String.valueOf(bresult));
			}
		} catch (Exception e) {

		} catch (Error e) {

		}
	}

	private void supportsResultSetHoldability(DatabaseMetaData metadata) {
		try {
			boolean bresult = metadata.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
			addRow("supportsResultSetHoldability(HOLD_CURSORS_OVER_COMMIT)", String.valueOf(bresult));
			bresult = metadata.supportsResultSetHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
			addRow("supportsResultSetHoldability(CLOSE_CURSORS_AT_COMMIT)", String.valueOf(bresult));
		} catch (Exception e) {
		} catch (Error e) {

		}
	}

	private void supportsResultSetType(DatabaseMetaData metadata) {
		try {
			String[] rset_type_label = { "TYPE_FORWARD_ONLY", "TYPE_SCROLL_INSENSITIVE", "TYPE_SCROLL_SENSITIVE" };
			int[] rset_type = { ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.TYPE_SCROLL_SENSITIVE };
			for (int index = 0; index < rset_type.length; index++) {
				boolean bresult = metadata.supportsResultSetType(rset_type[index]);
				addRow("supportsResultSetType(" + rset_type_label[index] + ")", String.valueOf(bresult));
			}
		} catch (Exception e) {

		} catch (Error e) {

		}

	}

	private void supportsTransactionIsolation(DatabaseMetaData metadata) {
		try {
			String[] iso_label = { "TRANSACTION_READ_UNCOMMITTED", "TRANSACTION_READ_COMMITTED",
					"TRANSACTION_REPEATABLE_READ", "TRANSACTION_SERIALIZABLE" };

			int[] iso_type = { Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED,
					Connection.TRANSACTION_REPEATABLE_READ, Connection.TRANSACTION_SERIALIZABLE };

			for (int index = 0; index < iso_type.length; index++) {
				boolean bresult = metadata.supportsTransactionIsolationLevel(iso_type[index]);
				addRow("supportsTransactionIsolation(" + iso_label[index] + ")", String.valueOf(bresult));
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

}
