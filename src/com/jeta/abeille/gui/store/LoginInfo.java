package com.jeta.abeille.gui.store;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * Keeps track of last user for a given connection
 * 
 * @author Jeff Tassin
 */
public class LoginInfo implements JETAExternalizable {
	static final long serialVersionUID = -2921966632044936396L;

	public static int VERSION = 1;

	/** the connection uid */
	private String m_uid;

	/** the last user name entered for the given connection */
	private String m_username;

	/**
	 * ctor only for serialization
	 */
	public LoginInfo() {

	}

	public LoginInfo(String uid, String username) {
		m_uid = uid;
		m_username = username;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof LoginInfo) {
			LoginInfo linfo = (LoginInfo) obj;
			return m_uid.equals(linfo.m_uid);
		} else if (obj instanceof String) {
			return m_uid.equals((String) obj);
		} else
			return false;
	}

	/**
	 * @return the last UID
	 */
	public String getUID() {
		return m_uid;
	}

	/**
	 * @return the last user for the associated connection
	 */
	public String getUser() {
		return m_username;
	}

	/**
	 * Sets the last user for the associated connection
	 */
	public void setUser(String user) {
		m_username = user;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_uid = (String) in.readObject();
		m_username = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeObject(m_uid);
		out.writeObject(m_username);
	}

}
