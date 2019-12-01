package com.jeta.abeille.gui.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTabbedPane;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.jdbc.DataTypeInfoModel;
import com.jeta.abeille.gui.jdbc.DriverInfoPanel;
import com.jeta.abeille.gui.queryresults.QueryUtils;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.java.SystemPropertiesPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This panel shows database information in a tabbed pane. Some databases will
 * have different tabs.
 * 
 * @author Jeff Tassin
 */
public class SystemInfoView extends TSPanel {
	/**
	 * The database connection
	 */
	private TSConnection m_connection;

	/**
	 * The main component in the view
	 */
	private JTabbedPane m_tabpane = new JTabbedPane();

	/**
	 * Reference the view/panel that displays the system/user tables in the
	 * database. We do this so we can reload the view.
	 */
	private AbstractTablePanel m_tablespanel;
	private JPanel m_tablesview;

	/**
	 * ctor
	 */
	private SystemInfoView(TSConnection conn) {
		m_connection = conn;
		setLayout(new BorderLayout());
		add(createView(), BorderLayout.CENTER);
	}

	/**
	 * Adds the given view to the tab pane
	 */
	private void addView(String tabLabel, Icon icon, JPanel view) {
		if (view != null) {
			m_tabpane.addTab(tabLabel, icon, view);
		}
	}

	/**
	 * Creates a view of the data types supported by this connection
	 */
	private AbstractTablePanel createDataTypesView(TSConnection conn) {
		try {
			DataTypeInfoModel tmodel = new DataTypeInfoModel(conn);
			AbstractTablePanel panel = TableUtils.createSimpleTable(tmodel, true);
			JTable table = panel.getTable();
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			TableUtils.setColumnWidths(table);
			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			return panel;
		} catch (Exception e) {
			TSUtils.printException(e);
			return null;
		}
	}

	/**
	 * Creates the JDBC driver info panel
	 */
	private TSPanel createDriverInfoView(TSConnection conn) {
		DriverInfoPanel dipanel = new DriverInfoPanel();
		TSPanel panel = new TSPanel(new BorderLayout());
		panel.add(dipanel, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dipanel.refresh(conn.getMetaDataConnection());
		return panel;
	}

	/**
	 * Runs a query and displaysp the result set
	 */
	private AbstractTablePanel createResultsView(String viewId, TSConnection conn, String sql) {
		try {
			AbstractTablePanel panel = QueryUtils.createMetaDataResultsView(viewId, conn, sql);
			panel.setName(viewId);
			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			return panel;
		} catch (Exception e) {
			TSUtils.printException(e);
			return null;
		}
	}

	/**
	 * Creates a view of the tables supported by this connection
	 */
	private JPanel createTablesView(TSConnection conn) {
		try {
			m_tablesview = new JPanel(new BorderLayout());
			JPanel btn_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			JButton reloadbtn = new JButton(I18N.getLocalizedMessage("Reload"));
			btn_panel.add(reloadbtn);
			m_tablesview.add(btn_panel, BorderLayout.NORTH);
			DatabaseMetaData metadata = conn.getMetaData();
			ResultSet rset = metadata.getTables(null, null, null, null);
			m_tablespanel = QueryUtils.createMetaDataResultsView(conn, rset);
			m_tablesview.add(m_tablespanel, BorderLayout.CENTER);
			m_tablesview.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			reloadbtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						m_tablesview.remove(m_tablespanel);
						DatabaseMetaData metadata = m_connection.getMetaData();
						ResultSet rset = metadata.getTables(null, null, null, null);
						m_tablespanel = QueryUtils.createMetaDataResultsView(m_connection, rset);
						m_tablesview.add(m_tablespanel, BorderLayout.CENTER);
					} catch (SQLException e) {
						TSUtils.printException(e);
					}
				}
			});

			return m_tablesview;
		} catch (Exception e) {
			TSUtils.printException(e);
			return null;
		}
	}

	/**
	 * Creates the main view
	 */
	private JComponent createView() {
		return m_tabpane;
	}

	/**
	 * Creates the view
	 */
	public static TSPanel createView(TSConnection conn) {
		SystemInfoView view = new SystemInfoView(conn);
		view.addView(I18N.getLocalizedMessage("JDBC Driver Info"), TSGuiToolbox.loadImage("incors/16x16/jar_view.png"),
				view.createDriverInfoView(conn));
		view.addView(I18N.getLocalizedMessage("Tables"), TSGuiToolbox.loadImage("incors/16x16/table_sql.png"),
				view.createTablesView(conn));
		view.addView(I18N.getLocalizedMessage("Data Types"), TSGuiToolbox.loadImage("incors/16x16/cubes.png"),
				view.createDataTypesView(conn));

		if (conn.getDatabase().equals(Database.MYSQL)) {
			view.addView(I18N.getLocalizedMessage("MySQL Status"), null,
					view.createResultsView("mysql.status.view", conn, "show status"));
			view.addView(I18N.getLocalizedMessage("MySQL Variables"), null,
					view.createResultsView("mysql.variables.view", conn, "show variables"));
		}

		TSPanel sys_props = new SystemPropertiesPanel();
		sys_props.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		view.addView(I18N.getLocalizedMessage("Java Properties"), TSGuiToolbox.loadImage("incors/16x16/cup.png"),
				sys_props);
		return view;
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(8, 20);
	}

	/**
	 * Override Component.removeNotify so we can save any table settings.
	 */
	public void removeNotify() {
		for (int index = 0; index < m_tabpane.getTabCount(); index++) {
			Component comp = m_tabpane.getComponentAt(index);
			if (comp instanceof AbstractTablePanel) {
				AbstractTablePanel tpanel = (AbstractTablePanel) comp;
				String viewid = tpanel.getName();
				if (viewid != null && viewid.length() > 0) {
					QueryUtils.storeTableSettings(viewid, m_connection, tpanel);
				}
			}
		}
		super.removeNotify();
	}
}
