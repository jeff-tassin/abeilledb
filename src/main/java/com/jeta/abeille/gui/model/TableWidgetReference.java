package com.jeta.abeille.gui.model;

import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.utils.TSUtils;

/**
 * this class associates a 'existing' table widget with a connection. This is
 * only used for copy/paste and drag/drop table widget instances.
 */
public class TableWidgetReference implements JETAExternalizable {
	static final long serialVersionUID = -530594909003158842L;

	public static int VERSION = 1;

	private TSConnection m_connection;
	private TableWidget m_tablewidget;

	/**
	 * ctor for serialization
	 */
	public TableWidgetReference() {

	}

	/**
	 * ctor
	 */
	public TableWidgetReference(TSConnection tsconn, TableWidget tablewidget) {
		m_connection = tsconn;
		m_tablewidget = tablewidget;
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	public TableWidget getTableWidget() {
		return m_tablewidget;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_connection = (TSConnection) in.readObject();
		m_tablewidget = (TableWidget) in.readObject();
		try {
			m_tablewidget.getModel().setModeler(ModelerModel.getDefaultInstance(m_connection));
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_connection);
		out.writeObject(m_tablewidget);
	}

}
