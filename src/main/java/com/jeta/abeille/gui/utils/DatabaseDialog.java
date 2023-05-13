package com.jeta.abeille.gui.utils;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSDialog;

public abstract class DatabaseDialog extends TSDialog {
	/**
	 * The underlying database connection
	 */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public DatabaseDialog(java.awt.Frame owner, boolean bModal) {
		super(owner, bModal);
	}

	/**
	 * ctor
	 */
	public DatabaseDialog(java.awt.Dialog owner, boolean bModal) {
		super(owner, bModal);
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * Sets the database connection
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}
}
