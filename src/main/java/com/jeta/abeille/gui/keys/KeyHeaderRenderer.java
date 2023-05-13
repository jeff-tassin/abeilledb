package com.jeta.abeille.gui.keys;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This renderer displays a primary key in a table cell
 * 
 * @author Jeff Tassin
 */
public class KeyHeaderRenderer extends JLabel implements TableCellRenderer {
	private static ImageIcon m_pkimage = TSGuiToolbox.loadImage("primarykey16.gif");

	public KeyHeaderRenderer() {
		super();
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		// Try to set default fore- and background colors
		if (table != null) {
			JTableHeader header = table.getTableHeader();
			if (header != null) {
				setForeground(header.getForeground());
				setBackground(header.getBackground());
				setFont(header.getFont());
			}
		}

		if (value != null)
			setText(value.toString());

		// set normal border
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		setIcon(m_pkimage);
		return this;
	}
}
