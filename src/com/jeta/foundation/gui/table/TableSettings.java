/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This is a helper class used to persist table settings. We should probably
 * have a TSTableModel class, but that would require a redesign. This will
 * suffice for now
 * 
 * @author Jeff Tassin
 */
public class TableSettings implements JETAExternalizable {
	static final long serialVersionUID = 7463556499391045580L;

	public static int VERSION = 1;

	/** a collection of TableColumnInfo objects */
	private ArrayList m_columns = new ArrayList();

	/**
	 * ctor for serialization
	 */
	public TableSettings() {

	}

	/**
	 * ctor
	 * 
	 * @param cols
	 *            a collection of TableColumnInfo objects
	 */
	public TableSettings(Collection cols) {
		if (cols.size() > 0) {
			Iterator iter = cols.iterator();
			assert (iter.next() instanceof TableColumnInfo);
		}

		m_columns.addAll(cols);
	}

	/**
	 * @return an array of TableColumnInfo objects
	 */
	public Collection getTableColumns() {
		return m_columns;
	}

	/**
	 * Prints the settings to the console
	 */
	public void print() {
		System.out.println(" ------------------------ printing TableSettings -------------------- ");
		Iterator iter = m_columns.iterator();
		while (iter.hasNext()) {
			TableColumnInfo info = (TableColumnInfo) iter.next();
			info.print();
		}
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_columns = (ArrayList) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_columns);
	}

}
