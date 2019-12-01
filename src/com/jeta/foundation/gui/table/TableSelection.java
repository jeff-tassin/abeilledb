/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * This class represents a single
 * 
 * @author Jeff Tassin
 */
public class TableSelection implements TableModel {
	/** the ordered set of selected rows */
	// private TreeMap m_rows = new TreeMap();
	ArrayList m_rows = new ArrayList();
	HashMap m_rowhash = new HashMap();

	/** the array of rows in the selection */
	private int[] m_rowindices;

	/**
	 * the array of outer-most columns that make up the selection We allow
	 * discontinous selections, so this is merely the max and min columns along
	 * with all selected columns in between
	 */
	private int[] m_colindices;

	/** the underlying table model */
	private TableModel m_delegate;

	/**
	 * if true, then we merge multiple selections for the same row into the same
	 * selection. this is used when we have a split-vertical view with
	 * discontinous selections on each view for the same row
	 */
	private boolean m_uniqueRows;

	/**
	 * ctor
	 */
	public TableSelection(TableModel delegate, boolean uniqueRows) {
		m_delegate = delegate;
		m_uniqueRows = uniqueRows;
	}

	/**
	 * Adds the given row selection to this selection. If a given row selection
	 * is already in the set it is overwritten
	 */
	public void add(int row, RowSelection selection) {
		Integer irow = new Integer(row);
		if (m_uniqueRows) {
			Integer index = (Integer) m_rowhash.get(irow);
			if (index == null) {
				index = new Integer(m_rows.size());
				m_rows.add(selection);
				m_rowhash.put(irow, index);
			} else {
				m_rows.set(index.intValue(), selection);
			}
		} else {
			m_rows.add(selection);
		}

		m_rowindices = null;
		m_colindices = null;
	}

	/**
	 * This method finds all the columns in the selection. The selection might
	 * be discontinous, but this method will include the total columns even if
	 * some of those columns are not included in some selections.
	 */
	private int[] buildColumnSpan() {
		ArrayList result = new ArrayList();

		TreeSet cols = new TreeSet();
		Iterator iter = m_rows.iterator();
		while (iter.hasNext()) {
			RowSelection selection = (RowSelection) iter.next();
			int[] scols = selection.getColumns();
			for (int index = 0; index < scols.length; index++) {

				Integer col = new Integer(scols[index]);
				if (!cols.contains(col)) {
					cols.add(col);
					result.add(col);
				}
			}
		}

		int[] rcols = new int[result.size()];
		for (int index = 0; index < result.size(); index++) {
			Integer icol = (Integer) result.get(index);
			rcols[index] = icol.intValue();
		}
		return rcols;
	}

	/**
	 * @param rowindex
	 *            The index in this object. NOTE, the index is NOT the row in
	 *            model, but the index from 0 to getRowCount
	 * @return a RowSelection from a given index.
	 */
	public RowSelection fromIndex(int rowindex) {
		int[] rows = getRowSpan();
		int row = rows[rowindex];
		return get(row);
	}

	/**
	 * @param row
	 *            this is the actual row in the model, NOT the row index based
	 *            on getRowCount
	 * @return the row selection at the given row. Null is returned if the
	 *         selection is not in the set.
	 */
	private RowSelection get(int row) {
		assert (m_uniqueRows);
		Integer index = (Integer) m_rowhash.get(new Integer(row));
		if (index == null)
			return null;
		else
			return (RowSelection) m_rows.get(index.intValue());
	}

	/**
	 * @return the span of columns from that this selection spans. This will not
	 *         necessarily be in order The selection might be discontinous, but
	 *         this method will include the total columns even if some of those
	 *         columns are not included in some selections.
	 */
	public int[] getColumnSpan() {
		if (m_colindices == null) {
			m_colindices = buildColumnSpan();
		}

		return m_colindices;
	}

	/**
	 * @return the span of rows from min to max that this selection spans. This
	 *         holds even if the selection is discontinuous
	 */
	private int[] getRowSpan() {
		if (m_rowindices == null) {
			int[] rows = new int[m_rows.size()];
			int index = 0;
			Iterator iter = m_rows.iterator();
			while (iter.hasNext()) {
				RowSelection selection = (RowSelection) iter.next();
				rows[index] = selection.getRow();
				index++;
			}
			m_rowindices = rows;
		}

		return m_rowindices;
	}

	public void addTableModelListener(TableModelListener l) {
		// no op
		assert (false);
	}

	public Class getColumnClass(int columnIndex) {
		int[] cols = getColumnSpan();
		return m_delegate.getColumnClass(cols[columnIndex]);
	}

	/**
	 * @return the number of columns in the selection
	 */
	public int getColumnCount() {
		return getColumnSpan().length;
	}

	public String getColumnName(int columnIndex) {
		int[] cols = getColumnSpan();
		return m_delegate.getColumnName(cols[columnIndex]);
	}

	/**
	 * @return the number of rows in the selection
	 */
	public int getRowCount() {
		return getRowSpan().length;

	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		RowSelection rowsel = null;
		int[] cols = getColumnSpan();
		int col = cols[columnIndex];
		int[] rows = getRowSpan();
		int row = rows[rowIndex];

		if (m_uniqueRows) {
			rowsel = get(row);
		} else {
			rowsel = (RowSelection) m_rows.get(rowIndex);
		}

		if (rowsel != null) {
			if (rowsel.contains(col)) {
				return m_delegate.getValueAt(row, col);
			} else
				return null;
		} else {
			return null;
		}

	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public void removeTableModelListener(TableModelListener l) {
		// no op
		assert (false);
	}

	/**
	 * Prints this selection to the console
	 */
	public void print() {
		int[] rows = getRowSpan();
		for (int index = 0; index < rows.length; index++) {
			RowSelection rowsel = get(rows[index]);
			System.out.print("\nrow = " + rows[index] + " rowselection: ");
			assert (rowsel != null);
			rowsel.print();
		}

		System.out.println();

		System.out.println(" printing columnspan ");
		int[] cols = getColumnSpan();
		for (int index = 0; index < cols.length; index++) {
			int col = cols[index];
			System.out.println("  col[" + index + "] = " + col);
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// no op
		assert (false);
	}

	/**
	 * Sets the delegate for this selection. Normally, you would never call this
	 * method except in very special circumstances. Use the constructor instead.
	 */
	public void setDelegate(TableModel delegate) {
		m_delegate = delegate;
	}
}
