/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

import javax.swing.event.TableModelEvent;

/**
 * This is a test model for testing the table classes in the application This is
 * used only for development/debugging purposes
 * 
 * @author Jeff Tassin
 */
class TestTableModel extends AbstractTableModel {
	ArrayList m_data = new ArrayList();
	String[] m_colNames; // an array of column names of the GUI table
	Class[] m_colTypes; // this is the types of columns of the GUI table

	/**
	 * Constructor
	 */
	public TestTableModel() {
		String[] values = { "Entry Id", "Entry Text", "Animal" };
		m_colNames = values;
		Class[] types = { Integer.class, String.class, String.class };
		m_colTypes = types;

		m_data.add(new TestInfo(1, "one", "dog_one"));
		m_data.add(new TestInfo(2, "two", "cat_two"));
		m_data.add(new TestInfo(3, "three", "fish_three"));
		m_data.add(new TestInfo(4, "four", "bird_four"));
	}

	public void add(TestInfo info) {
		m_data.add(info);
		fireTableChanged(new TableModelEvent(this, m_data.size() - 1, m_data.size() - 1, 0, TableModelEvent.INSERT));
	}

	/**
	 * @return the number of columns in the table
	 */
	public int getColumnCount() {
		return m_colNames.length;
	}

	/**
	 * @return the number of rows in the table
	 */
	public int getRowCount() {
		return m_data.size();
	}

	/**
	 * @param column
	 *            the colum index
	 * @return the column name at the given column index
	 */
	public String getColumnName(int column) {
		return m_colNames[column];
	}

	/**
	 * @param column
	 *            the colum index
	 * @return the column class at the given column index
	 */
	public Class getColumnClass(int column) {
		return m_colTypes[column];
	}

	/**
	 * @return true if the given table cell is editable
	 */
	public boolean isCellEditable(Object node, int column) {
		return false;
	}

	/**
	 * Clears the model of data
	 */
	public void removeAll() {

	}

	/**
	 * @return the Object at the given row and column
	 * @param row
	 *            the row which to get the object from
	 * @param column
	 *            the column which to get the object from
	 */
	public Object getValueAt(int row, int column) {
		TestInfo info = (TestInfo) m_data.get(row);
		if (column == 0)
			return new Integer(info.m_id);
		else if (column == 1)
			return info.m_text;
		else if (column == 2)
			return info.m_animal;
		else
			return "";
	}

	public void setValueAt(Object aValue, int row, int column) {
		// only needed if we enable editing in the table
	}

	public class TestInfo {
		TestInfo(int id, String txt, String animal) {
			m_id = id;
			m_text = txt;
			m_animal = animal;
		}

		int m_id;
		String m_text;
		String m_animal;
	}
}
