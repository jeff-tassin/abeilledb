package com.jeta.abeille.gui.common;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.table.JETATableModel;

/**
 * This class is used as a base class for table models that display information
 * about a given table.
 * 
 * @author Jeff Tassin
 */
public abstract class MetaDataTableModel extends JETATableModel {
	/** The database connection */
	private TSConnection m_connection;

	/** The id of the table we are showing the properties for */
	private TableId m_tableid;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying information for
	 */
	public MetaDataTableModel(TSConnection connection, TableId tableId) {
		m_connection = connection;
		m_tableid = tableId;
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the current table id
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * Sets the table id
	 */
	public void setTableId(TableId tableId) {
		m_tableid = tableId;
	}

	/**
	 * @return true if the given postgresql instance supports schemas
	 */
	public boolean supportsSchemas() {
		return m_connection.supportsSchemas();
	}
}
