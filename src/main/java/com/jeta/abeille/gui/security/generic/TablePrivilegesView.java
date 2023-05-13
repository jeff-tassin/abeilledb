package com.jeta.abeille.gui.security.generic;

import java.awt.BorderLayout;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.swing.BorderFactory;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.queryresults.QueryUtils;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays the privileges for a table. The privileges are retrieved
 * from the database metadata object by calling getTablePrivileges.
 * 
 * @author Jeff Tassin
 */
public class TablePrivilegesView extends TSPanel {
	/** the underyling database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public TablePrivilegesView(TSConnection conn) {
		m_connection = conn;
		setLayout(new BorderLayout());
	}

	public void setTableId(TableId tableId) {
		removeAll();
		if (tableId == null)
			return;

		try {
			DatabaseMetaData metadata = m_connection.getMetaData();
			ResultSet rset = metadata.getTablePrivileges(tableId.getCatalog().getMetaDataSearchParam(), tableId
					.getSchema().getMetaDataSearchParam(), tableId.getTableName());

			AbstractTablePanel panel = QueryUtils.createMetaDataResultsView(m_connection, rset);
			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			add(panel, BorderLayout.CENTER);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}
}
