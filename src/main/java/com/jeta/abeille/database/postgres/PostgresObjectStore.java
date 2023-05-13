package com.jeta.abeille.database.postgres;

import java.util.HashMap;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * This class is the datamodel for postgres specific options For example, in
 * Postgres, the user might like to see the OID for a given table. We store
 * those options in this model
 * 
 * @author Jeff Tassin
 */
public class PostgresObjectStore implements JETAExternalizable {
	static final long serialVersionUID = -5424949123784320548L;

	public static int VERSION = 1;

	/**
	 * the database connection. this is needed so we can store userproperties
	 * that are specific for this connection
	 */
	private transient TSConnection m_connection;

	/** the data */
	private HashMap m_data = new HashMap();

	private static PostgresObjectStore m_singleton = null;

	public static final String COMPONENT_ID = "PostgresOptionsModel";
	public static final String OID_KEY = COMPONENT_ID + "." + "SHOWOID";

	/**
	 * ctor only for serialization
	 */
	public PostgresObjectStore() {

	}

	/**
	 * ctor
	 */
	private PostgresObjectStore(TSConnection connection) {
		m_connection = connection;
	}

	/**
	 * Creates an instance of a postgres options model
	 */
	public static PostgresObjectStore getInstance(TSConnection connection) {
		if (m_singleton == null) {
			ObjectStore ostore = connection.getObjectStore();

			try {
				m_singleton = (PostgresObjectStore) ostore.load(PostgresObjectStore.COMPONENT_ID);
			} catch (java.io.IOException ioe) {
				ioe.printStackTrace();
			}

			if (m_singleton == null) {
				m_singleton = new PostgresObjectStore(connection);
				m_singleton.save();
			}
			m_singleton.m_connection = connection;

		}

		return m_singleton;
	}

	public static boolean isShowOID(TSConnection connection, TableId tableId) {
		PostgresObjectStore postgresos = PostgresObjectStore.getInstance(connection);
		return postgresos.isShowOID(tableId);
	}

	/**
	 * @return true if the oid is visible for the given table
	 */
	public boolean isShowOID(TableId tableId) {
		Boolean val = (Boolean) m_data.get(tableId);
		if (val != null)
			return val.booleanValue();
		else
			return false;
	}

	/**
	 * Override serialize so we can initialize
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

	/**
	 * Sets the flag that indicates whether an oid is visible for a table
	 */
	public void setShowOID(TableId tableId, boolean showoid) {
		m_data.put(tableId, Boolean.valueOf(showoid));
		save();
	}

	/**
	 * Saves this object to the perstient store
	 */
	public void save() {
		try {
			ObjectStore ostore = m_connection.getObjectStore();
			ostore.store(PostgresObjectStore.COMPONENT_ID, this);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_data = (HashMap) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_data);
	}

}
