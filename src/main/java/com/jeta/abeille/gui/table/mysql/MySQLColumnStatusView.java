package com.jeta.abeille.gui.table.mysql;

import java.awt.BorderLayout;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.queryresults.QueryUtils;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.utils.TSUtils;

/**
 * Displays the column status for the selected table. In MySQL, this is simply a
 * query: select column status for table
 * 
 * @author Jeff Tassin
 */
public class MySQLColumnStatusView extends TSPanel {
	/** the table id we are showing options for */
	private TableId m_tableid;

	/** the database connection */
	private TSConnection m_connection;

	private AbstractTablePanel m_tablepanel;

	public static final String VIEW_ID = "column.status.view";

	/**
	 * ctor for viewing an existing table
	 */
	public MySQLColumnStatusView(TSConnection connection) {
		m_connection = connection;
		setLayout(new BorderLayout());
	}

	/**
	 * Override Component.removeNotify so we can save any table settings.
	 */
	public void removeNotify() {
		saveViewSettings();
		super.removeNotify();
	}

	/**
	 * Saves the column settings for the table
	 */
	private void saveViewSettings() {
		if (m_tablepanel != null) {
			QueryUtils.storeTableSettings(VIEW_ID, m_connection, m_tablepanel);
		}
	}

	/**
	 * Sets the table id for this view
	 */
	public void setTableId(TableId tableId) {
		removeAll();
		m_tableid = tableId;
		try {
			if (tableId != null) {
				StringBuffer sql = new StringBuffer();
				sql.append("show columns from ");
				sql.append(tableId.getFullyQualifiedName());
				saveViewSettings();
				m_tablepanel = QueryUtils.createMetaDataResultsView(VIEW_ID, m_connection, sql.toString());
				add(m_tablepanel, BorderLayout.CENTER);
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		revalidate();
		repaint();
	}

}
