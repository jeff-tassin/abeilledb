package com.jeta.abeille.gui.query;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.foundation.gui.editor.TSEditorUtils;

import org.netbeans.editor.BaseAction;

/**
 * 
 * @author Jeff Tassin
 */
public class ConstraintCellEditor extends AbstractCellEditor implements TableCellEditor {
	/** the cell editor */
	private JEditorPane m_editor;

	private JComponent m_comp;

	/**
	 * ctor
	 */
	public ConstraintCellEditor(TSConnection conn, TableSelectorModel model) {
		m_editor = TSEditorUtils.createEditor(new ConstraintKit(conn, model));
		// m_comp = TSEditorUtils.getExtComponent( m_editor );
		JScrollPane scroll = new JScrollPane(m_editor);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		m_comp = scroll;

		TSEditorUtils.showStatusBar(m_editor, false);
		TSEditorUtils.setLineNumberEnabled(m_editor, false);

		m_editor.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER)
					stopCellEditing();
			}
		});
		// m_editor.getInputMap().put( KeyStroke.getKeyStroke(
		// KeyEvent.VK_ENTER, 0, false ), "entereaction" );
		// m_editor.getActionMap().put( "enteraction", new EnterAction() );

	}

	/**
	 * @return the editor value
	 */
	public Object getCellEditorValue() {
		return m_editor.getText();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (value == null || value.toString() == null)
			m_editor.setText("");
		else
			m_editor.setText(value.toString());

		return m_comp;
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
