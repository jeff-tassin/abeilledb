package com.jeta.abeille.gui.table;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableId;
import com.jeta.foundation.gui.components.TSPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class TableView extends TSPanel {
	/** the table id */
	private TableId m_tableid;

	/** the underlying database connection */
	private TSConnection m_connection;

	/**
	 * the tab window for this view. The tab pane contains all the sub-views for
	 * the table
	 */
	private JTabbedPane m_tabpane;

	/** id of the tab pane */
	public static final String ID_TAB_PANE = "tab.pane";

	/**
	 * A map of containers to their titles
	 */
	private HashMap m_titles = new HashMap();

	/**
	 * ctor
	 */
	public TableView(TSConnection conn) {
		m_connection = conn;
		m_tabpane = new JTabbedPane();
		m_tabpane.setName(ID_TAB_PANE);
		m_tabpane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		setLayout(new BorderLayout());
		add(m_tabpane, BorderLayout.CENTER);
	}


	/**
	 * Adds a view to the tab component
	 */
	public void addView(String title, TSPanel view, javax.swing.Icon icon) {
		m_titles.put(view, title);
		m_tabpane.addTab(title, null, view);
	}

	/**
	 * Adds a view to the tab component
	 */
	public void insertView(String title, javax.swing.Icon icon, TSPanel view, int index) {
		m_titles.put(view, title);
		m_tabpane.insertTab(title, null, view, null, index);
	}

	/**
	 * @return the underlying connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the view in the currently selected tab
	 */
	public TSPanel getCurrentView() {
		return (TSPanel) m_tabpane.getSelectedComponent();
	}

	public JTabbedPane getTabbedPane() {
		return m_tabpane;
	}

	/**
	 * @return the table id for the view
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	public String getTitle(Container cc) {
		return (String) m_titles.get(cc);
	}

	/**
	 * Sets the table id for the view
	 */
	public void setTableId(TableId tableId) {
		m_tableid = tableId;
	}

}
