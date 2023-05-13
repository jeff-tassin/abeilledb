package com.jeta.abeille.gui.main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.layouts.TableLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;
import com.jeta.foundation.utils.TSUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Shows the databse preferences
 * 
 * @author Jeff Tassin
 */
public class DatabasePreferencesView extends TSPanel {
	/** the data store for our properties */
	private TSUserProperties m_userprops;

	/** the transaction isolation combo box */
	private JComboBox m_isolationcombo;

	/** the resultset scroll type combo box */
	private JComboBox m_rsetcombo;

	private JRadioButton m_auto_on;
	private JRadioButton m_auto_off;

	private JCheckBox m_query_table_counts;

	/** the database connection */
	private TSConnection m_connection;

	/** component ids */
	public static final String ID_ISOLATION_COMBO = "isolation.field";
	public static final String ID_RESULT_SET_TYPE = "resultset.type";

	/**
	 * ctor
	 */
	public DatabasePreferencesView(TSConnection connection, TSUserProperties userprops) {
		m_connection = connection;
		m_userprops = userprops;
		initialize();
		loadData();
		setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(7, 5);
	}

	public String getTransactionIsolation() {
		return (String) m_isolationcombo.getSelectedItem();
	}

	/**
	 * Creates and initializes the components in the view.
	 */
	private void initialize() {
		m_isolationcombo = new JComboBox();

		try {
			DatabaseMetaData metadata = m_connection.getMetaData();
			if (metadata.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED)) {
				m_isolationcombo.addItem(TSConnection.READ_UNCOMMITTED);
			}

			if (metadata.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED)) {
				m_isolationcombo.addItem(TSConnection.READ_COMMITTED);
			}

			if (metadata.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ)) {
				m_isolationcombo.addItem(TSConnection.REPEATABLE_READ);
			}

			if (metadata.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE)) {
				m_isolationcombo.addItem(TSConnection.SERIALIZABLE);
			}
		} catch (SQLException se) {
			TSUtils.printException(se);
		}

		m_rsetcombo = new JComboBox();
		try {
			DatabaseMetaData metadata = m_connection.getMetaData();
			if (metadata.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)) {
				m_rsetcombo.addItem(TSConnection.TYPE_SCROLL_INSENSITIVE);
			}

			if (metadata.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE)) {
				m_rsetcombo.addItem(TSConnection.TYPE_SCROLL_SENSITIVE);
			}
		} catch (SQLException se) {
			TSUtils.printException(se);
		}

		m_query_table_counts = new JCheckBox(I18N.getLocalizedMessage("Enabled"));
		m_query_table_counts.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		setLayout(new com.jeta.foundation.gui.layouts.ColumnLayout());
		add(createView());
	}

	private JPanel createView() {
		FormLayout layout = new FormLayout("pref, 10dlu, pref", // columns
				"pref, 10dlu, pref, 1dlu, pref, 4dlu, pref"); // rows
		// Create a reusable CellConstraints instance.
		CellConstraints cc = new CellConstraints();

		JPanel panel = new JPanel();
		panel.setLayout(layout);

		panel.add(new JLabel(I18N.getLocalizedDialogLabel("Transaction Isolation")), cc.xy(1, 1));
		panel.add(m_isolationcombo, cc.xy(3, 1));

		panel.add(new JLabel(I18N.getLocalizedDialogLabel("Auto Commit")), cc.xy(1, 3));

		javax.swing.ButtonGroup grp = new javax.swing.ButtonGroup();
		m_auto_on = TSGuiToolbox.createRadioButton(I18N.getLocalizedMessage("On"));
		grp.add(m_auto_on);

		m_auto_off = TSGuiToolbox.createRadioButton(I18N.getLocalizedMessage("Off"));
		grp.add(m_auto_off);

		panel.add(m_auto_on, cc.xy(3, 3));
		panel.add(m_auto_off, cc.xy(3, 5));

		panel.add(new JLabel(I18N.getLocalizedDialogLabel("Query Table Counts")), cc.xy(1, 7));
		panel.add(m_query_table_counts, cc.xy(3, 7));

		return panel;
	}

	/**
	 * Loads the data from the user properties into the model
	 */
	private void loadData() {
		int isolation = m_connection.getTransactionIsolation();

		if (isolation == Connection.TRANSACTION_READ_UNCOMMITTED)
			m_isolationcombo.setSelectedItem(TSConnection.READ_UNCOMMITTED);
		else if (isolation == Connection.TRANSACTION_READ_COMMITTED)
			m_isolationcombo.setSelectedItem(TSConnection.READ_COMMITTED);
		else if (isolation == Connection.TRANSACTION_REPEATABLE_READ)
			m_isolationcombo.setSelectedItem(TSConnection.REPEATABLE_READ);
		else if (isolation == Connection.TRANSACTION_SERIALIZABLE)
			m_isolationcombo.setSelectedItem(TSConnection.SERIALIZABLE);

		int rset_type = m_connection.getResultSetScrollType();
		if (rset_type == ResultSet.TYPE_SCROLL_INSENSITIVE)
			m_rsetcombo.setSelectedItem(TSConnection.TYPE_SCROLL_INSENSITIVE);
		else if (rset_type == ResultSet.TYPE_SCROLL_SENSITIVE)
			m_rsetcombo.setSelectedItem(TSConnection.TYPE_SCROLL_SENSITIVE);

		try {
			boolean is_auto_commit = m_connection.isAutoCommit();
			m_auto_off.setSelected(!is_auto_commit);
			m_auto_on.setSelected(is_auto_commit);
			m_query_table_counts.setSelected(m_connection.isQueryTableCounts());
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Saves the preferences
	 */
	void save() {
		String item = (String) m_isolationcombo.getSelectedItem();
		if (item != null) {
			if (item.equals(TSConnection.READ_UNCOMMITTED))
				m_connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			else if (item.equals(TSConnection.READ_COMMITTED))
				m_connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			else if (item.equals(TSConnection.REPEATABLE_READ))
				m_connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			else if (item.equals(TSConnection.SERIALIZABLE))
				m_connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		}

		String scroll_type = (String) m_rsetcombo.getSelectedItem();
		if (scroll_type != null) {
			if (scroll_type.equals(TSConnection.TYPE_SCROLL_INSENSITIVE))
				m_connection.setResultSetScrollType(ResultSet.TYPE_SCROLL_INSENSITIVE);
			else if (scroll_type.equals(TSConnection.TYPE_SCROLL_SENSITIVE))
				m_connection.setResultSetScrollType(ResultSet.TYPE_SCROLL_SENSITIVE);
		}

		try {
			m_connection.setAutoCommit(m_auto_on.isSelected());
			m_connection.setDefaultAutoCommit(m_auto_on.isSelected());
			m_connection.setQueryTableCounts(m_query_table_counts.isSelected());
		} catch (java.sql.SQLException e) {
			TSUtils.printException(e);
		}
	}
}
