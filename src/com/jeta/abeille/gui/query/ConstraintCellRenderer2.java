package com.jeta.abeille.gui.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.query.QueryConstraint;

import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.quickedit.QuickEditView;

/**
 * Renderer for the constraint
 * 
 * @author Jeff Tassin
 */
public class ConstraintCellRenderer2 extends JPanel implements TableCellRenderer {
	private JEditorPane m_editor;
	private Color INVALID_COLOR = new Color(255, 198, 198);

	public ConstraintCellRenderer2(TSConnection conn, TableSelectorModel selmodel) {
		m_editor = TSEditorUtils.createEditor(new ConstraintKit(conn, selmodel));

		setLayout(new BorderLayout());
		add(m_editor, BorderLayout.CENTER);
		// setOpaque( true );
		// setFont( UIManager.getFont( "Table.font" ) );
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (value == null || value.toString() == null)
			setText("");
		else
			setText(value.toString());

		if (isSelected) {
			setBackground(UIManager.getColor("Table.selectionBackground"));
			setForeground(UIManager.getColor("Table.selectionForeground"));
		} else {
			setBackground(UIManager.getColor("Table.background"));
			setForeground(UIManager.getColor("Table.foreground"));
		}

		if (value instanceof QueryConstraint) {
			QueryConstraint constraint = (QueryConstraint) value;
			if (!constraint.isValid() && !isSelected)
				setBackground(INVALID_COLOR);
		}
		return this;
	}

	public void setBackground(Color c) {
		if (m_editor != null) {
			m_editor.setBackground(c);
		}
		super.setBackground(c);
	}

	public void setForeground(Color c) {
		if (m_editor != null) {
			m_editor.setForeground(c);
		}

		super.setForeground(c);
	}

	public void setText(String txt) {
		m_editor.setText(txt);
	}
}
