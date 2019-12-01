/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;

/**
 * This class implements a Row header for a given table
 * 
 * @author Jeff Tassin
 */
public class TableRowHeader extends JList {
	private JTable table;
	private int m_width;

	/**
	 * flag that indicates if we are currently resizing a row by dragging the
	 * mouse
	 */
	private boolean m_resizing = false;

	public TableRowHeader(JTable table) {
		this(table, -1);
	}

	/**
	 * ctor
	 * 
	 * @param table
	 *            the table to set the header for
	 * @parma width the width (in characters) to size the header column
	 */
	public TableRowHeader(JTable table, int width) {
		super(new TableRowHeaderModel(table));
		m_width = width;

		this.table = table;
		setFixedCellHeight(table.getRowHeight());
		setFixedCellWidth(preferredHeaderWidth());

		setCellRenderer(new RowHeaderRenderer(table));
		setFocusable(false);
	}

	/**
	 * @return the table
	 */
	public JTable getTable() {
		return table;
	}

	public void setSelectedIndex(int index) {
		super.setSelectedIndex(index);
	}

	public void setSelectedIndices(int[] indices) {
		super.setSelectedIndices(indices);
	}

	/**
	 * Returns the bounds of the specified range of items in JList coordinates.
	 * Returns null if index isn't valid.
	 * 
	 * @param index0
	 *            the index of the first JList cell in the range
	 * @param index1
	 *            the index of the last JList cell in the range
	 * @return the bounds of the indexed cells in pixels
	 */
	public Rectangle getCellBounds(int index0, int index1) {
		Rectangle rect0 = table.getCellRect(index0, 0, true);
		Rectangle rect1 = table.getCellRect(index1, 0, true);
		int y, height;
		if (rect0.y < rect1.y) {
			y = rect0.y;
			height = rect1.y + rect1.height - y;
		} else {
			y = rect1.y;
			height = rect0.y + rect0.height - y;
		}
		return new Rectangle(0, y, getFixedCellWidth(), height);
	}

	// assume that row header width should be big enough to display row number
	// Integer.MAX_VALUE completely

	private int preferredHeaderWidth() {
		JLabel longestRowLabel = new JLabel("65356#");
		if (m_width == 1)
			longestRowLabel = new JLabel("#");
		else if (m_width == 2)
			longestRowLabel = new JLabel("##");
		else if (m_width == 3)
			longestRowLabel = new JLabel("###");
		else if (m_width == 4)
			longestRowLabel = new JLabel("####");
		else if (m_width == 5)
			longestRowLabel = new JLabel("#####");

		JTableHeader header = table.getTableHeader();
		longestRowLabel.setBorder(header.getBorder());
		// UIManager.getBorder("TableHeader.cellBorder"));
		longestRowLabel.setHorizontalAlignment(JLabel.CENTER);
		longestRowLabel.setFont(header.getFont());
		return longestRowLabel.getPreferredSize().width;
	}

	public void updateUI() {
		super.updateUI();

		if (table != null) {
			setCellRenderer(new RowHeaderRenderer(table));
		}
		setOpaque(true);
		setBackground(UIManager.getColor("panel.background"));
		revalidate();
		repaint();
	}

}
