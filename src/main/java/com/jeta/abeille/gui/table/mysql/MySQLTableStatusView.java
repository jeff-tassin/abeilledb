package com.jeta.abeille.gui.table.mysql;

import java.awt.BorderLayout;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.queryresults.QueryUtils;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.table.AbstractTablePanel;

import com.jeta.foundation.utils.TSUtils;

/**
 * Displays the table status for the selected table. In MySQL, this is simply a
 * query: select column status for table
 * 
 * @author Jeff Tassin
 */
public class MySQLTableStatusView extends TSPanel {
	/** the table id we are showing options for */
	private TableId m_tableid;

	/** the database connection */
	private TSConnection m_connection;

	private AbstractTablePanel m_tablepanel;

	public static final String VIEW_ID = "column.status.view";

	/**
	 * ctor for viewing an existing table
	 */
	public MySQLTableStatusView(TSConnection connection) {
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
		Statement stmt = null;

		m_tableid = tableId;
		try {
			if (tableId != null) {
				StringBuffer sql = new StringBuffer();
				sql.append("show table status from ");
				sql.append(tableId.getCatalog().getName());
				sql.append(" like '");
				sql.append(tableId.getTableName());
				sql.append("'");
				Connection conn = m_connection.getMetaDataConnection();
				stmt = conn.createStatement();
				ResultSet rset = stmt.executeQuery(sql.toString());
				while (rset.next()) {
					String tablename = rset.getString("Name");
					if (tableId.getTableName().equals(tablename)) {
						saveViewSettings();
						//m_tablepanel = QueryUtils.createTransposedResultView(VIEW_ID, m_connection, rset);
						//add(m_tablepanel, BorderLayout.CENTER);
						break;
					}
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {

			}
		}
		revalidate();
		repaint();
	}

}
