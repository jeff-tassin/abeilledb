package com.jeta.abeille.gui.query;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Caret;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.foundation.gui.editor.TSEditorUtils;

/**
 * Renderer for the constraint
 * 
 * @author Jeff Tassin
 */
public class ConstraintCellRenderer implements TableCellRenderer {

	/** the cell editor */
	private JEditorPane m_editor;

	public ConstraintCellRenderer(TSConnection conn, TableSelectorModel model) {
		m_editor = TSEditorUtils.createEditor(new ConstraintKit(conn, model));
		JComponent comp = TSEditorUtils.getExtComponent(m_editor);

		TSEditorUtils.showStatusBar(m_editor, false);
		TSEditorUtils.setLineNumberEnabled(m_editor, false);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (value == null || value.toString() == null)
			m_editor.setText("");
		else
			m_editor.setText(value.toString());

		m_editor.getCaret().setVisible(isSelected);

		if (isSelected) {
			m_editor.setBackground(UIManager.getColor("Table.selectionBackground"));
			m_editor.setForeground(UIManager.getColor("Table.selectionForeground"));
		} else {
			m_editor.setBackground(UIManager.getColor("Table.background"));
			m_editor.setForeground(UIManager.getColor("Table.foreground"));
		}

		return m_editor;
	}
}
