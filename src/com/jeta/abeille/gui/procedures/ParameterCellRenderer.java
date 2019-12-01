package com.jeta.abeille.gui.procedures;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for a procedure parameter
 * 
 * @author Jeff Tassin
 */
public class ParameterCellRenderer extends JLabel implements TableCellRenderer {

	/**
	 * ctor
	 */
	public ParameterCellRenderer() {
	}

	/**
	 * TableCellRender implementation
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		return this;
	}
}
