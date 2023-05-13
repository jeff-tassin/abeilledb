package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.*;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class represents the list of columns that make up a key in a database.
 * It can either be a primary key or a foreign key.
 * 
 * @author Jeff Tassin
 */
public class DbKey implements Cloneable, JETAExternalizable {
	/** serialize id */
	static final long serialVersionUID = -8653184905139850249L;

	public static int VERSION = 1;

	/** an array of column names (String objects) that make up the key. */
	private ArrayList m_columns = new ArrayList();
	private String m_keyName;

	public DbKey() {

	}

	/**
	 * Adds a column to this key
	 */
	public void addColumn(String fName) {
		addField(fName);
	}

	/**
	 * Adds a field to this key
	 */
	public void addField(String fName) {
		if (!m_columns.contains(fName)) {
			m_columns.add(fName);
		} else {
			assert (false);
		}
	}

	/**
	 * Clears the columns in this key
	 */
	public void clear() {
		m_columns.clear();
	}

	/**
	 * cloneable implementation
	 */
	public Object clone() {
		DbKey key = new DbKey();
		key.m_columns = (ArrayList) m_columns.clone();
		key.m_keyName = m_keyName;
		return key;
	}

	/**
	 * @return true if this key contains the specified field name
	 */
	public boolean containsField(String fieldName) {
		Iterator iter = m_columns.iterator();
		while (iter.hasNext()) {
			String fname = (String) iter.next();
			if (fieldName.equalsIgnoreCase(fname)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the name of the column at the given index in this key
	 */
	public String getColumnName(int index) {
		return (String) m_columns.get(index);
	}

	/**
	 * @return the name of the column at the given index in this key
	 * @deprecated (call getColumnName instead)
	 */
	public String getColumn(int index) {
		return (String) m_columns.get(index);
	}

	/**
	 * @return the number of columns in the key
	 */
	public int getColumnCount() {
		return m_columns.size();
	}

	public String getKeyName() {
		return m_keyName;
	}

	/**
	 * @return a collection of column names (String objects) that make up this
	 *         key
	 */
	public Collection getColumns() {
		return m_columns;
	}

	public void setKeyName(String keyName) {
		m_keyName = keyName;
	}

	/**
	 * Prints this key to the system.out for debugging purposes
	 */
	public void print() {
		System.out.println("dbkey." + m_keyName);
		Iterator iter = m_columns.iterator();
		while (iter.hasNext()) {
			String fname = (String) iter.next();
			System.out.println("  field... " + fname);
		}
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_columns = (ArrayList) in.readObject();
		m_keyName = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_columns);
		out.writeObject(m_keyName);
	}

}
