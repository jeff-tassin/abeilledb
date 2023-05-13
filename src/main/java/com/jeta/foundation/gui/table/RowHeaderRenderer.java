/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;

/**
 * Renderer for the row header for a given table
 * 
 * @author Jeff Tassin
 */
public class RowHeaderRenderer extends JLabel implements ListCellRenderer {
	private JTable m_table;
	private Border m_selectedborder;
	private Border m_normalborder;
	private Font m_selectedfont;
	private Font m_normalfont;

	RowHeaderRenderer(JTable table) {
		this.m_table = table;
		m_normalborder = UIManager.getBorder("TableHeader.cellBorder");
		m_selectedborder = BorderFactory.createRaisedBevelBorder();
		final JTableHeader header = m_table.getTableHeader();
		m_normalfont = header.getFont();
		m_selectedfont = m_normalfont.deriveFont(m_normalfont.getStyle() | Font.BOLD);
		setForeground(header.getForeground());
		setBackground(header.getBackground());
		setOpaque(true);
		setHorizontalAlignment(CENTER);
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		if (list.getSelectionModel().isSelectedIndex(index)) {
			setFont(m_selectedfont);
			setBorder(m_selectedborder);
		} else {
			setFont(m_normalfont);
			setBorder(m_normalborder);
		}
		String label = String.valueOf(index + 1);
		setText(label);
		return this;
	}
}
