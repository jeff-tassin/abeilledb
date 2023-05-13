/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.io.ObjectStreamException;
import java.io.IOException;

import javax.swing.table.TableColumn;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class desribes the column settings for a column in this table model We
 * have to do this so we can store the settings in the application state store
 */
class TableColumnInfo implements JETAExternalizable, Comparable {
	static final long serialVersionUID = 3563595844114171430L;

	public static int VERSION = 1;

	private transient TableColumn m_column;

	private int m_columnwidth;
	private String m_columnname;
	private int m_modelindex;
	private boolean m_visible;

	/**
	 * ctor for serialization
	 */
	public TableColumnInfo() {

	}

	/**
	 * ctor
	 */
	public TableColumnInfo(TableColumn col, String name) {
		m_column = col;
		m_columnwidth = 72;
		m_columnname = name;
		m_modelindex = m_column.getModelIndex();
		m_visible = true;
	}

	/**
	 * Implementation of Comparable interface. In this case, we compare the view
	 * index so we can order columns according to there location on the table
	 */
	public int compareTo(Object obj) {
		TableColumnInfo info = (TableColumnInfo) obj;
		if (m_modelindex < info.m_modelindex)
			return -1;
		else if (m_modelindex == info.m_modelindex)
			return 0;
		else
			return 1;
	}

	public TableColumn getColumn() {
		return m_column;
	}

	/**
	 * @return the column name
	 */
	public String getName() {
		return m_columnname;
	}

	/**
	 * @return the width of this column
	 */
	public int getWidth() {
		return m_columnwidth;
	}

	/**
	 * @return the index of the table model that this column is located
	 */
	public int getModelIndex() {
		return m_modelindex;
	}

	/**
	 * @return a flag indicating if this column is visible or not
	 */
	public boolean isVisible() {
		return m_visible;
	}

	/**
	 * Prints the TableColumnInfo attributes to the console
	 */
	public void print() {
		System.out.println(" colname = " + m_columnname + "  modelindex = " + m_modelindex + "   width = "
				+ m_columnwidth);
	}

	/**
	 * Sets the flag that indicates whether this column is visible or not
	 */
	public void setVisible(boolean bVisible) {
		m_visible = bVisible;
	}

	/**
	 * Sets the width for this column
	 */
	public void setWidth(int width) {
		m_columnwidth = width;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_columnwidth = in.readInt();
		m_columnname = (String) in.readObject();
		m_modelindex = in.readInt();
		m_visible = in.readBoolean();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeInt(m_columnwidth);
		out.writeObject(m_columnname);
		out.writeInt(m_modelindex);
		out.writeBoolean(m_visible);
	}

}
