package com.jeta.abeille.gui.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * An object that represents a tree folder in the TreeView
 * 
 * @author Jeff Tassin
 */
public class TreeFolder implements JETAExternalizable, Comparable {
	static final long serialVersionUID = 4721298030684663979L;

	public static int VERSION = 1;

	/**
	 * the name
	 */
	private String m_name;

	/**
	 * the identifier for this folder
	 */
	private String m_uid;

	/**
	 * ctor only for serialization
	 */
	public TreeFolder() {

	}

	/**
	 * ctor
	 */
	public TreeFolder(String name) {
		m_name = name;
		if (m_name == null)
			m_name = "";
	}

	/**
	 * Comparable interface. Tree folder always come before other objects
	 */
	public int compareTo(Object obj) {
		if (obj instanceof TreeFolder) {
			TreeFolder folder = (TreeFolder) obj;
			if (m_name == null)
				m_name = "";

			return m_name.compareToIgnoreCase(folder.m_name);
		} else
			return -1;
	}

	/**
	 * @returns the name of this folder
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return the uid for this folder
	 */
	public String getUID() {
		return m_uid;
	}

	/**
	 * Sets the name of this folder
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * Sets the UID for this folder
	 */
	public void setUID(String uid) {
		m_uid = uid;
	}

	/**
	 * toString
	 */
	public String toString() {
		return m_name;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_name = (String) in.readObject();
		m_uid = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_name);
		out.writeObject(m_uid);
	}

}
