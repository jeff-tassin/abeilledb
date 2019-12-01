/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class is used to for rendering an icon next to a String value in a
 * table.
 * 
 * @author Jeff Tassin
 */
public class IconDecoratorRenderer extends JLabel implements TableCellRenderer {
	private ImageIcon m_icon;

	/**
	 * ctor
	 */
	public IconDecoratorRenderer(ImageIcon icon) {
		setOpaque(true);
		setFont(UIManager.getFont("Table.font"));
		m_icon = icon;
	}

	public Component getTableCellRendererComponent(JTable table, Object aValue, boolean bSelected, boolean bFocus,
			int row, int column) {
		if (bSelected) {
			setBackground(UIManager.getColor("Table.selectionBackground"));
			setForeground(UIManager.getColor("Table.selectionForeground"));
		} else {
			setBackground(UIManager.getColor("Table.background"));
			setForeground(UIManager.getColor("Table.foreground"));
		}

		if (m_icon != null)
			setIcon(m_icon);

		if (aValue == null) {
			setText("");
		} else {
			setText(aValue.toString());
		}
		return this;
	}
}
