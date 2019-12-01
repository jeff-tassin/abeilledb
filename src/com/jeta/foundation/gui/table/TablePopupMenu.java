/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 * This is a specialization of a Swing popup menu. It has behavior for dealing
 * with the TSTablePanel class. Specifically, it stores the table that was the
 * source of the popup as well as the row and column of the cell the popup
 * occurred over. This information is useful for operations like select row or
 * column
 * 
 * @author Jeff Tassin
 */
public class TablePopupMenu extends JPopupMenu {
	/**
	 * the table that the mouse was over when the user invoked the popup
	 */
	private JTable m_table;

	/**
	 * the column that the mouse was over when the user invoked the popup
	 */
	private int m_column;

	/**
	 * the row that the mouse was over when the user invoked the popup
	 */
	private int m_row;

	/**
	 * ctor
	 */
	public TablePopupMenu() {

	}

	/**
	 * @return the column that the mouse was over when the user invoked the
	 *         popup
	 */
	public int getColumn() {
		return m_column;
	}

	/**
	 * @return the row that the mouse was over when the user invoked the popup
	 */
	public int getRow() {
		return m_row;
	}

	/**
	 * @return the table that the mouse was over when the user invoked the
	 *         popup. This is either the free or fixed table
	 */
	public JTable getTable() {
		return m_table;
	}

	/**
	 * set the column that the mouse was over when the user invoked the popup
	 */
	public void setColumn(int column) {
		m_column = column;
	}

	/**
	 * set the row that the mouse was over when the user invoked the popup
	 */
	public void setRow(int row) {
		m_row = row;
	}

	/**
	 * set the table that the mouse was over when the user invoked the popup
	 */
	public void setTable(JTable table) {
		m_table = table;
	}

}
