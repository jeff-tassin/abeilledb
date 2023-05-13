/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * This class is used to render the headings for the TSTable panel (both fixed
 * and free tables). The main purpose of this class is to display an icon in the
 * header cell that shows how the column is currently sorted. There can be one
 * of three modes for this: ASCENDING - show up arrow DESCENDING - show down
 * arrow NONE - no image
 * 
 * @author Jeff Tassin
 */

class SortedColumnHeaderRenderer extends JLabel implements TableCellRenderer {
	static final ImageIcon m_upimage;
	static final ImageIcon m_downimage;
	static final ImageIcon m_emptyheaderimage;

	private SortMode m_sortmode; // the current sorting for a given column

	static {
		m_upimage = com.jeta.foundation.gui.utils.TSGuiToolbox.loadImage("incors/16x16/navigate_up.png");
		m_downimage = com.jeta.foundation.gui.utils.TSGuiToolbox.loadImage("incors/16x16/navigate_down.png");
		m_emptyheaderimage = com.jeta.foundation.gui.utils.TSGuiToolbox.loadImage("emptytableheader16.gif");

	}

	public SortedColumnHeaderRenderer() {
		super();
		this.setHorizontalAlignment(JLabel.CENTER);
	}

	/**
	 * Gets the sort mode for this renderer. Each column has a separate
	 * renderer. When the user sorts a given column, we put an icon in the
	 * header that shows whether the column is ascending, descending, or natural
	 * ordering.
	 * 
	 * @return mode the sort mode to set. We get the sort mode from the SortMode
	 *         class: ASCENDING, DESCENDING, or NONE
	 */
	public SortMode getSortMode() {
		return m_sortmode;
	}

	/**
	 * TableCellRenderer implementation
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		// Try to set default fore- and background colors
		if (table != null) {
			JTableHeader header = table.getTableHeader();
			if (header != null) {
				setForeground(header.getForeground());
				setBackground(header.getBackground());
				setFont(header.getFont());
				AbstractTableModel model = (AbstractTableModel) table.getModel();

				column = table.convertColumnIndexToModel(column);
				setText(model.getColumnName(column));
				// set normal border
				setBorder(javax.swing.UIManager.getBorder("TableHeader.cellBorder"));

				if (m_sortmode == SortMode.ASCENDING)
					setIcon(m_upimage);
				else if (m_sortmode == SortMode.DESCENDING)
					setIcon(m_downimage);
				else
					// if NONE
					setIcon(m_emptyheaderimage);
			}
		}
		return this;
	}

	/**
	 * Sets the sort mode for this renderer. Each column has a separate
	 * renderer. When the user sorts a given column, we put an icon in the
	 * header that shows whether the column is ascending, descending, or natural
	 * ordering.
	 * 
	 * @param mode
	 *            the sort mode to set. We get the sort mode from the SortMode
	 *            class: ASCENDING, DESCENDING, or NONE
	 */
	public void setSortMode(SortMode mode) {
		m_sortmode = mode;
	}
}
