package com.jeta.abeille.gui.store;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * Class that manages the user information for the various connections
 */
public class UserInfo implements JETAExternalizable {
	static final long serialVersionUID = 6301680607709708786L;

	public static int VERSION = 1;

	/** user entered data that we store for the next login */
	private LinkedList m_data = new LinkedList();

	private static final int MAX_USER_INFO_SIZE = 50;

	public UserInfo() {

	}

	/**
	 * Adds a LoginInfo object to the list
	 */
	public void add(LoginInfo info) {
		m_data.remove(info);
		if (m_data.size() > MAX_USER_INFO_SIZE)
			m_data.removeLast();

		m_data.addFirst(info);
	}

	/**
	 * @return the login info for the given uid
	 */
	public LoginInfo get(String uid) {
		if (uid == null)
			return null;

		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			LoginInfo info = (LoginInfo) iter.next();
			if (uid.equals(info.getUID()))
				return info;
		}

		return null;
	}

	public LoginInfo getFirst() {
		if (size() > 0) {
			return (LoginInfo) m_data.getFirst();
		} else
			return null;
	}

	/** @return the number of items in the list */
	public int size() {
		return m_data.size();
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_data = (LinkedList) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_data);
	}

}
