/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.Component;
import java.awt.Font;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;
import javax.swing.JTable;

/**
 * Renderer for editor objects
 * 
 * @author Jeff Tassin
 */
public class EditorObjectRenderer extends JLabel implements TableCellRenderer {
	public EditorObjectRenderer(Font f) {
		// must set or the background color won't show
		setOpaque(true);
		setFont(f);
	}

	public Component getTableCellRendererComponent(JTable table, Object aValue, boolean bSelected, boolean bFocus,
			int row, int col) {
		if (aValue instanceof EditorObjectWrapper) {
			EditorObjectWrapper wrapper = (EditorObjectWrapper) aValue;
			if (col == 0) {
				setText(wrapper.displayTxt);
				setIcon(wrapper.icon);
			}
		} else if (aValue instanceof String)
			setText((String) aValue);

		if (bSelected) {
			setForeground(Color.white);
			setBackground(Color.blue);
		} else {
			setForeground(Color.black);
			setBackground(Color.white);
		}
		return this;
	}
}
