/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.jeta.foundation.i18n.I18N;

public class TableOptionsModel extends AbstractTableModel {
	/**
	 * this is an array of column names in THIS table (i.e. Column Name, Visible
	 * )
	 */
	private String[] m_colnames;

	/** an array of column types in THIS table */
	private Class[] m_coltypes;

	/** the data (rows) in our table */
	private ArrayList m_data = new ArrayList();

	/** the table model we are displaying options for */
	private TableModel m_tablemodel;

	/** table columns */
	public static final int COLUMN_NAME_COLUMN = 0;
	public static final int VISIBLE_COLUMN = 1;

	/**
	 * ctor.
	 */
	public TableOptionsModel(Collection tableColumns, TableModel model) {
		super();

		String[] values = new String[2];
		values[COLUMN_NAME_COLUMN] = I18N.getLocalizedMessage("Column Name");
		values[VISIBLE_COLUMN] = I18N.getLocalizedMessage("Visible");
		m_colnames = values;

		Class[] types = { String.class, Boolean.class };
		m_coltypes = types;

		m_tablemodel = model;
		m_data.addAll(tableColumns);
	}

	/**
	 * Fires an event that the metadata model has changed
	 */
	public void fireModelChanged() {
		fireTableDataChanged();
	}

	/**
	 * @return the number of columns in the JTable
	 */
	public int getColumnCount() {
		return m_colnames.length;
	}

	/**
	 * @return the column info object for a given row. Null is returned if the
	 *         row is invalid
	 */
	public TableColumnInfo getColumnInfo(int row) {
		return getRow(row);
	}

	/**
	 * @return the column info object for a given row. Null is returned if the
	 *         row is invalid
	 */
	public TableColumnInfo getRow(int row) {
		if (row < 0 || row >= m_data.size())
			return null;

		return (TableColumnInfo) m_data.get(row);
	}

	/**
	 * @return the number of rows in the JTable
	 */
	public int getRowCount() {
		return m_data.size();
	}

	public String getColumnName(int column) {
		return m_colnames[column];
	}

	public Class getColumnClass(int column) {
		return m_coltypes[column];
	}

	/**
	 * @return the value at the given row/column
	 */
	public Object getValueAt(int row, int column) {
		TableColumnInfo co = getRow(row);
		if (co == null)
			return null;

		if (column == COLUMN_NAME_COLUMN) {
			return m_tablemodel.getColumnName(co.getModelIndex());
		} else if (column == VISIBLE_COLUMN) {
			return co.isVisible() ? Boolean.TRUE : Boolean.FALSE;
		} else
			return "";
	}

	/**
	 * @return true/false if a cell is editable or not. For this model, we only
	 *         allow the visibility column to be edited.
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex == VISIBLE_COLUMN);
	}

	/**
	 * Moves the object found at the old index to the new index
	 */
	public void reorder(int newIndex, int oldIndex) {
		Object obj = m_data.remove(oldIndex);
		if (obj != null) {
			m_data.add(newIndex, obj);
			fireTableChanged(new TableModelEvent(this));
		}
	}

	/**
	 * Sets the value at the given column. We only allow setting the visible
	 * column
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == VISIBLE_COLUMN) {
			TableColumnInfo co = getRow(rowIndex);
			if (aValue instanceof Boolean) {
				Boolean bval = (Boolean) aValue;
				co.setVisible(bval.booleanValue());
			}
		}
	}
}
