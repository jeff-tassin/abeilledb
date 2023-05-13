package com.jeta.abeille.gui.model;

import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

/**
 * this class associates a tableid with a connection
 */
public class TableMetaDataReference implements JETAExternalizable {
	static final long serialVersionUID = -5538387405881489174L;

	public static int VERSION = 1;

	private TSConnection m_connection;
	private TableMetaData m_tmd;

	/**
	 * ctor for serialization
	 */
	public TableMetaDataReference() {

	}

	/**
	 * ctor
	 */
	public TableMetaDataReference(TSConnection tsconn, TableMetaData tmd) {
		m_connection = tsconn;
		m_tmd = tmd;
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	public TableId getTableId() {
		return m_tmd.getTableId();
	}

	public TableMetaData getTableMetaData() {
		return m_tmd;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_connection = (TSConnection) in.readObject();
		m_tmd = (TableMetaData) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_connection);
		out.writeObject(m_tmd);
	}

}
