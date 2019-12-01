package com.jeta.abeille.gui.jdbc;

import java.awt.BorderLayout;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.Database;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays raw JDBC information about result set parameters
 * 
 * @author Jeff Tassin
 */
public class ResultSetParametersView extends TSPanel {
	private TSConnection m_connection;
	private Database m_database;

	private JTextField m_fetchsize;
	private JTextField m_type;
	private JTextField m_concurrency;
	private JTextField m_cursorname;
	private JTextField m_holdability;
	private JTextField m_maxfieldsize;
	private JTextField m_maxrows;
	private JTextField m_querytimeout;

	public ResultSetParametersView(TSConnection tsconn) {
		m_connection = tsconn;
		m_database = m_connection.getDatabase();
		createView();
	}

	/**
	 * Clears the view
	 */
	private void clear() {
		m_fetchsize.setText("");
		m_type.setText("");
		m_concurrency.setText("");
		m_cursorname.setText("");
		m_holdability.setText("");
		m_maxfieldsize.setText("");
		m_maxrows.setText("");
		m_querytimeout.setText("");
	}

	/**
	 * Creates the components on this panel
	 */
	private void createView() {
		setLayout(new BorderLayout());

		m_fetchsize = new JTextField();
		m_type = new JTextField();
		m_concurrency = new JTextField();
		m_cursorname = new JTextField();
		m_holdability = new JTextField();
		m_maxfieldsize = new JTextField();
		m_maxrows = new JTextField();
		m_querytimeout = new JTextField();

		m_fetchsize.setEditable(false);
		m_type.setEditable(false);
		m_concurrency.setEditable(false);
		m_cursorname.setEditable(false);
		m_holdability.setEditable(false);
		m_maxfieldsize.setEditable(false);
		m_maxrows.setEditable(false);
		m_querytimeout.setEditable(false);

		JLabel[] labels = new JLabel[8];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Fetch Size"));
		labels[1] = new JLabel(I18N.getLocalizedDialogLabel("Scroll Type"));
		labels[2] = new JLabel(I18N.getLocalizedDialogLabel("Concurrency"));
		labels[3] = new JLabel(I18N.getLocalizedDialogLabel("Cursor Name"));
		labels[4] = new JLabel(I18N.getLocalizedDialogLabel("Holdability"));
		labels[5] = new JLabel(I18N.getLocalizedDialogLabel("Max Field Size"));
		labels[6] = new JLabel(I18N.getLocalizedDialogLabel("Max Rows"));
		labels[7] = new JLabel(I18N.getLocalizedDialogLabel("Query Time Out"));

		JComponent[] controls = new JComponent[8];
		controls[0] = m_fetchsize;
		controls[1] = m_type;
		controls[2] = m_concurrency;
		controls[3] = m_cursorname;
		controls[4] = m_holdability;
		controls[5] = m_maxfieldsize;
		controls[6] = m_maxrows;
		controls[7] = m_querytimeout;

		com.jeta.foundation.gui.utils.ControlsAlignLayout layout = new com.jeta.foundation.gui.utils.ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_fetchsize, 20);
		layout.setMaxTextFieldWidth(m_type, 20);
		layout.setMaxTextFieldWidth(m_concurrency, 20);
		layout.setMaxTextFieldWidth(m_cursorname, 20);
		layout.setMaxTextFieldWidth(m_holdability, 20);
		layout.setMaxTextFieldWidth(m_maxfieldsize, 20);
		layout.setMaxTextFieldWidth(m_maxrows, 20);
		layout.setMaxTextFieldWidth(m_querytimeout, 20);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, labels, controls);
		add(panel, BorderLayout.NORTH);
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
	public void refresh(ResultSet rset) throws SQLException {
		clear();
		try {
			Statement stmt = rset.getStatement();
			ResultSetMetaData metadata = rset.getMetaData();

			try {
				m_type.setText(DbUtils.getResultSetScrollTypeString(rset.getType()));
			} catch (Error e) {
				TSUtils.printStackTrace(e);
			} catch (Exception e) {
				TSUtils.printStackTrace(e);
			}

			try {
				m_concurrency.setText(DbUtils.getConcurrencyString(rset.getConcurrency()));
			} catch (Error e) {
				TSUtils.printStackTrace(e);
			} catch (Exception e) {
				TSUtils.printStackTrace(e);
			}

			try {
				// MySQL driver can return a null stmt here
				if (!Database.DB2.equals(m_database) && !Database.ORACLE.equals(m_database)
						&& !Database.POSTGRESQL.equals(m_database)) {
					if (stmt != null) {
						m_holdability.setText(DbUtils.getHoldabilityString(stmt.getResultSetHoldability()));
					}
				}
			} catch (Error e) {
				TSUtils.printStackTrace(e);
			} catch (Exception e) {
				TSUtils.printStackTrace(e);
			}

			try {
				if (stmt != null) {
					m_fetchsize.setText(String.valueOf(stmt.getFetchSize()));
				}
			} catch (Error e) {
				TSUtils.printStackTrace(e);
			} catch (Exception e) {
				TSUtils.printStackTrace(e);
			}

			try {
				if (!Database.MYSQL.equals(m_database) && !Database.ORACLE.equals(m_database)
						&& !Database.MCKOI.equals(m_database)) {
					m_cursorname.setText(rset.getCursorName());
				}
			} catch (Error e) {
				TSUtils.printStackTrace(e);
			} catch (Exception e) {
				TSUtils.printStackTrace(e);
			}

			try {
				if (stmt != null) {
					m_maxfieldsize.setText(String.valueOf(stmt.getMaxFieldSize()));
				}
			} catch (Error e) {
				TSUtils.printStackTrace(e);
			} catch (Exception e) {
				TSUtils.printStackTrace(e);
			}

			try {
				if (stmt != null) {
					m_maxrows.setText(String.valueOf(stmt.getMaxRows()));
				}
			} catch (Error e) {
				TSUtils.printStackTrace(e);
			} catch (Exception e) {
				TSUtils.printStackTrace(e);
			}

			try {
				if (stmt != null) {
					m_querytimeout.setText(String.valueOf(stmt.getQueryTimeout()));
				}
			} catch (Error e) {
				TSUtils.printStackTrace(e);
			} catch (Exception e) {
				TSUtils.printStackTrace(e);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
