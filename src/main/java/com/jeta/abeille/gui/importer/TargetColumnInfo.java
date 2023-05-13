package com.jeta.abeille.gui.importer;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;

import com.jeta.foundation.common.JETAExternalizable;

public class TargetColumnInfo implements Comparable, JETAExternalizable {
	static final long serialVersionUID = 8400379905236175523L;

	public static int VERSION = 1;

	private ColumnMetaData m_target;

	private transient SourceColumn m_source;

	public TargetColumnInfo() {

	}

	public TargetColumnInfo(ColumnMetaData target) {
		m_target = target;
	}

	public int compareTo(Object obj) {
		if (obj instanceof TargetColumnInfo) {
			TargetColumnInfo tobj = (TargetColumnInfo) obj;
			if (m_target == null) {
				return -1;
			} else {
				return m_target.compareTo(tobj.m_target);
			}
		} else {
			return -1;
		}
	}

	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	public SourceColumn getSourceColumn() {
		return m_source;
	}

	public TableId getTableId() {
		if (m_target == null)
			return null;
		else
			return m_target.getParentTableId();
	}

	public ColumnMetaData getTarget() {
		return m_target;
	}

	public void setSourceColumn(SourceColumn sc) {
		m_source = sc;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_target = (ColumnMetaData) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_target);
	}

}
