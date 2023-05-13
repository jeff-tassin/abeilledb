package com.jeta.abeille.gui.modeler;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

import com.jeta.abeille.database.utils.*;
import com.jeta.abeille.database.model.DataTypeInfo;

public class DataTypeRenderer extends JLabel implements TableCellRenderer {
	/**
	 * ctor
	 */
	public DataTypeRenderer() {
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object aValue, boolean bSelected, boolean bFocus,
			int row, int column) {
		if (aValue instanceof DataTypeInfo) {
			DataTypeInfo info = (DataTypeInfo) aValue;
			setText(info.getTypeName());
			setIcon(info.getIcon());
		}

		if (bSelected) {
			setBackground(UIManager.getColor("Table.selectionBackground"));
			setForeground(UIManager.getColor("Table.selectionForeground"));
		} else {
			// setBackground(UIManager.getColor("Table.background"));
			// setForeground(UIManager.getColor("Table.foreground"));
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		return this;
	}
}
