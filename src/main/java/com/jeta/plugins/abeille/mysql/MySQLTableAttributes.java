package com.jeta.plugins.abeille.mysql;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.TableAttributes;

import com.jeta.foundation.common.JETAExternalizable;

public class MySQLTableAttributes extends TableAttributes implements JETAExternalizable, Cloneable {
	static final long serialVersionUID = -3045455241105166690L;

	public static int VERSION = 1;

	public static final String DEFAULT_TABLE_TYPE = "mysql.default.table.type";

	/** the table type */
	private MySQLTableType m_tabletype;

	/**
	 * ctor
	 */
	public MySQLTableAttributes() {

	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		MySQLTableAttributes attr = new MySQLTableAttributes();
		attr.m_tabletype = m_tabletype;
		return attr;
	}

	public MySQLTableType getTableType() {
		return m_tabletype;
	}

	public void setTableType(MySQLTableType tableType) {
		m_tabletype = tableType;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_tabletype = (MySQLTableType) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_tabletype);
	}

}
