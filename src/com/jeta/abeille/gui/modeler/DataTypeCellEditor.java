package com.jeta.abeille.gui.modeler;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * 
 * @author Jeff Tassin
 */
public class DataTypeCellEditor extends AbstractCellEditor implements TableCellEditor {
	private TSComboBox m_datatypecombo;

	/**
	 * ctor
	 */
	public DataTypeCellEditor(TSConnection conn) {
		m_datatypecombo = com.jeta.abeille.gui.common.DbGuiUtils.createDataTypeCombo(conn);
	}

	/**
	 * @return the editor value
	 */
	public Object getCellEditorValue() {
		return m_datatypecombo.getText();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (value == null || value.toString() == null)
			m_datatypecombo.setSelectedItem("");
		else
			m_datatypecombo.setSelectedItem(value.toString());

		Runnable gui_update = new Runnable() {
			public void run() {
				/*
				 * java.awt.Container c = m_datatypecombo; do { c =
				 * c.getParent(); if (c instanceof java.awt.Frame || c
				 * instanceof java.awt.Dialog ) { break; } } while (c != null);
				 * 
				 * if ( c != null ) c.requestFocus();
				 */

				m_datatypecombo.requestFocus();
				m_datatypecombo.selectEditorText();
				m_datatypecombo.repaint();
			}
		};

		javax.swing.SwingUtilities.invokeLater(gui_update);
		m_datatypecombo.repaint();
		return m_datatypecombo;
	}

	public boolean isCellEditable(EventObject anEvent) {
		if (anEvent instanceof MouseEvent) {
			return ((MouseEvent) anEvent).getClickCount() >= 2;
		}
		return true;
	}

	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}

}
