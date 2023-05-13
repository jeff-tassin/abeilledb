package com.jeta.foundation.gui.editor.macros;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;

import javax.swing.table.TableCellRenderer;

/**
 * TableCell Renderer for macro objects
 * 
 * @author Jeff Tassin
 */
public class MacroRenderer extends JLabel implements TableCellRenderer {
	/**
	 * ctor
	 */
	public MacroRenderer() {
		// must set or the background color won't show
		setOpaque(true);
		setFont(UIManager.getFont("Table.font"));
	}

	/**
	 * TableCellRenderer Implementation
	 */
	public Component getTableCellRendererComponent(JTable table, Object aValue, boolean bSelected, boolean bFocus,
			int row, int col) {
		if (aValue instanceof Macro) {
			Macro macro = (Macro) aValue;
			if (col == MacroMgrModel.NAME_COLUMN) {
				setText(macro.getName());
				setIcon(macro.getIcon());
			}
		} else if (aValue instanceof String)
			setText((String) aValue);

		if (bSelected) {
			setBackground(UIManager.getColor("Table.selectionBackground"));
			setForeground(UIManager.getColor("Table.selectionForeground"));
		} else {
			setBackground(UIManager.getColor("Table.background"));
			setForeground(UIManager.getColor("Table.foreground"));
		}
		return this;
	}
}
