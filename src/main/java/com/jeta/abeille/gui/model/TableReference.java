package com.jeta.abeille.gui.model;

import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

/**
 * this class associates a tableid with a connection
 */
public class TableReference implements JETAExternalizable {
	static final long serialVersionUID = -6399391726283492111L;

	public static int VERSION = 1;

	private TSConnection m_connection;
	private TableId m_tableid;

	/**
	 * ctor for serialization
	 */
	public TableReference() {

	}

	/**
	 * ctor
	 */
	public TableReference(TSConnection tsconn, TableId tableid) {
		m_connection = tsconn;
		m_tableid = tableid;
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	public String getFullyQualifiedName() {
		return m_tableid.getFullyQualifiedName();
	}

	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_connection = (TSConnection) in.readObject();
		m_tableid = (TableId) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_connection);
		out.writeObject(m_tableid);
	}

}
