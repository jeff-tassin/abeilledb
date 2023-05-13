package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class stores the serial number/version number for the connection.
 * 
 * @author Jeff Tassin
 */
public class ConnectionTag implements JETAExternalizable {
	static final long serialVersionUID = -6982564483702984845L;

	public static int VERSION = 1;

	private String m_usn;

	public static final String COMPONENT_ID = "jeta.table.info.cache";
	public static final int CLASS_VERSION = 1;

	/**
	 * ctor only for serialization
	 */
	public ConnectionTag() {

	}

	/**
	 * ctor
	 */
	public ConnectionTag(String usn) {
		m_usn = usn;
	}

	public String getUsn() {
		return m_usn;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_usn = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_usn);
	}

}
