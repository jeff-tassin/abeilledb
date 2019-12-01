/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * This class represents a selected row in the table view. It contains all
 * columns that are selected for that row (in order)
 * 
 * @author Jeff Tassin
 */
public class RowSelection {
	/** the row that this selection is based on */
	private int m_row;

	/** the selected columns in this row */
	private ArrayList m_cols = new ArrayList();

	private TreeSet m_colset = new TreeSet();

	private int[] m_colindices;

	public RowSelection(int row) {
		m_row = row;
	}

	public void addColumns(int[] cols) {
		for (int index = 0; index < cols.length; index++) {
			Integer col = new Integer(cols[index]);
			if (!m_colset.contains(col)) {
				m_colset.add(col);
				m_cols.add(col);
			}
		}
		m_colindices = null;
	}

	/**
	 * @return true if this row selection contains the given column
	 */
	public boolean contains(int col) {
		return m_colset.contains(new Integer(col));
	}

	/**
	 * @return the set of columns spaned by the row selection
	 */
	public int[] getColumns() {
		if (m_colindices == null) {
			int[] cols = new int[m_cols.size()];
			int index = 0;
			Iterator iter = m_cols.iterator();
			while (iter.hasNext()) {
				Integer col = (Integer) iter.next();
				cols[index] = col.intValue();
				index++;
			}
			m_colindices = cols;
		}
		return m_colindices;
	}

	/**
	 * @return the underlying row that this selection is associated with
	 */
	public int getRow() {
		return m_row;
	}

	/**
	 * Prints this object to the console
	 */
	public void print() {
		int[] cols = getColumns();
		System.out.println("RowSelection  cols.length = " + cols.length);
		for (int index = 0; index < cols.length; index++) {
			System.out.print("  col[" + index + "] = " + cols[index]);
		}
	}

	/**
	 * Sets the underlying row that this selection is associated with
	 */
	public void setRow(int row) {
		m_row = row;
	}
}
