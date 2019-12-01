package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This object is used to identify a column in a table or result set
 * 
 * @author Jeff Tassin
 */
public class ColumnId extends DbObjectId implements Comparable, JETAExternalizable {
	/** column id */
	static final long serialVersionUID = 3035478566448589066L;

	public static int VERSION = 1;

	private String m_columnname;
	private TableId m_tableid;

	private String m_stringid;

	/**
	 * ctor only for serialization
	 */
	public ColumnId() {

	}

	/**
	 * ctor
	 */
	public ColumnId(String fieldName, TableId tableId) {
		super(DbObjectType.COLUMN, (tableId == null ? Catalog.EMPTY_CATALOG : tableId.getCatalog()),
				(tableId == null ? Schema.VIRTUAL_SCHEMA : tableId.getSchema()), fieldName);

		m_columnname = fieldName;
		m_tableid = tableId;
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object o) {
		int result = -1;
		if (o instanceof ColumnId) {
			ColumnId id = (ColumnId) o;
			if (m_tableid == null) {
				if (id.m_tableid == null)
					result = super.compareTo(id);
				else
					result = -1;
			} else {
				result = m_tableid.compareTo(id.m_tableid);
				if (result == 0)
					result = super.compareTo(id);
			}

			if (result == 0) {
				if (m_columnname != null)
					result = m_columnname.compareTo(id.m_columnname);
			}
			return result;
		}

		return result;

	}

	public boolean equals(Object obj) {
		if (obj instanceof ColumnId) {
			ColumnId id = (ColumnId) obj;
			return (compareTo(id) == 0);
		}
		return false;
	}

	public String getColumnName() {
		return m_columnname;
	}

	public TableId getTableId() {
		return m_tableid;
	}

	public int hashCode() {
		// @todo probabaly can make this more efficient
		StringBuffer sbuff = new StringBuffer();
		sbuff.append(getHashValue());
		sbuff.append(m_columnname);
		return sbuff.toString().hashCode();
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_columnname = (String) in.readObject();
		m_tableid = (TableId) in.readObject();
		m_stringid = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_columnname);
		out.writeObject(m_tableid);
		out.writeObject(m_stringid);
	}

}
