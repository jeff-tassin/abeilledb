package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class abstracts an identifier for a database connection
 */
public class ConnectionId implements Comparable, JETAExternalizable {
	static final long serialVersionUID = -6577053862175151486L;

	public static int VERSION = 1;

	/** the id */
	private String m_uid;

	/**
	 * ctor only for serialization
	 */
	public ConnectionId() {

	}

	/**
	 * ctor
	 */
	public ConnectionId(String uid) {
		m_uid = uid;
		assert (m_uid != null);
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;
		else if (o instanceof ConnectionId) {
			ConnectionId id = (ConnectionId) o;
			return m_uid.compareTo(id.m_uid);
		} else
			return -1;
	}

	/**
	 * Comparable implementation
	 */
	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	/**
	 * For hashmaps
	 */
	public int hashCode() {
		return m_uid.hashCode();
	}

	/**
	 * @return the unique identifier
	 */
	public String getUID() {
		return m_uid;
	}

	public String toString() {
		return getUID();
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_uid = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_uid);
	}

}
