package com.jeta.abeille.gui.update;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.foundation.gui.editor.TSPlainKit;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.editor.TSTextDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a text component in the UpdateFrame This component has
 * an icon and an editor pane [icon][JEditorPane]
 * 
 * @author Jeff Tassin
 */
public class EditorComponent extends TextComponentBase {
	int m_width = 0; // this component is resized according to the size of the
						// parent container, so we store the
	// desired width to use for our layout manager. I guess I could have used
	// setPreferredSize as well.
	int m_rowsize = 5;
	InstanceTextArea m_editor;

	/**
	 * Constructor for EditorComponent
	 * 
	 * @param fieldName
	 *            the name of the column associated with this component
	 * @param rowsize
	 *            the height of this component (in rows of text)
	 * 
	 */
	public EditorComponent(String fieldName, int dataType, int rowsize) {
		super(fieldName, dataType);
		m_rowsize = rowsize;

		m_editor = new InstanceTextArea(this);
		m_editor.setRows(rowsize);
		m_editor.setLineWrap(true);
		m_editor.setWrapStyleWord(true);
		JScrollPane scroll = new EditorScrollPane(m_editor);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setComponent(scroll);

		setIcon("incors/16x16/document_edit.png");
		setLayout(new EditorLayoutManager());

		JButton btn = getIconButton();
		btn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				TSTextDialog dlg = (TSTextDialog) TSGuiToolbox.createDialog(TSTextDialog.class, EditorComponent.this,
						true);
				dlg.setTitle(getFieldName());
				TSGuiToolbox.centerFrame(dlg, 0.75f, 0.5f);
				dlg.initialize(com.jeta.abeille.gui.sql.SQLKit.class);
				dlg.setText(EditorComponent.this.toString());
				dlg.showCenter();
				if (dlg.isOk()) {
					setValue(dlg.getText());
					setModified(true);
				}
			}
		});
	}

	/**
	 * @return the editor
	 */
	public JTextComponent getEditor() {
		return m_editor;
	}

	/**
	 * @return the preferred size of this component
	 */
	public Dimension getPreferredSize() {
		JComponent comp = getComponent();
		Dimension d = comp.getPreferredSize();
		d.width = d.width + getComponentX() + 2;
		return d;
	}

	/**
	 * Sets the value represented by this component into the prepared statement
	 * 
	 * @param count
	 *            the parameter index to set
	 * @param pstmt
	 *            the prepared statement to act on
	 */
	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		if (isNull()) {
			pstmt.setNull(count, java.sql.Types.VARCHAR);
		} else {
			pstmt.setString(count, toString());
		}
	}

	/**
	 * Sets the width of this component.
	 */
	public void setWidth(int width) {
		JComponent comp = getComponent();
		Dimension d = comp.getPreferredSize();
		d.width = width - getComponentX();
		m_width = d.width;
		comp.setPreferredSize(d);
		comp.setSize(d);
		comp.repaint();
		comp.validate();
		m_editor.repaint();
	}

	/**
	 * @return a sql representation of this component value
	 */
	public String toSQLString(SQLFormatter formatter) {
		if (isModified() && isNull())
			return "null";
		else if (isNull())
			return "null";
		else {
			String value = toString();
			StringBuffer buff = new StringBuffer();
			buff.append('\'');
			for (int index = 0; index < value.length(); index++) {
				char c = value.charAt(index);
				if (c == '\'') {
					buff.append("\'\'");
				} else
					buff.append(c);
			}
			buff.append('\'');
			return buff.toString();
		}
	}

	class EditorLayoutManager implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			JComponent component = getComponent();
			JComponent iconbtn = getIconButton();
			Dimension d = component.getPreferredSize();
			d.width = m_width;
			component.setSize(d);
			component.setLocation(getComponentX(), 0);
			component.doLayout();
			iconbtn.setLocation(0, 0);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(12, 12);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return getPreferredSize();
		}

		public void removeLayoutComponent(Component comp) {
		}

	}

	/**
	 * Specialize ScrollPane so we can implement InstanceTextAdapter
	 */
	public class EditorScrollPane extends JScrollPane implements InstanceTextAdapter {
		/**
		 * ctor
		 */
		public EditorScrollPane(Component comp) {
			super(comp);
		}

		/**
		 * @return the underlying text component
		 */
		public JTextComponent getTextComponent() {
			return m_editor;
		}
	}

	/**
	 * Specialization of JTextField that we need for this component
	 * 
	 * @author Jeff Tassin
	 */
	public static class InstanceTextArea extends JTextArea {
		private InstanceComponent m_instancecomp;

		public InstanceTextArea(InstanceComponent comp) {
			m_instancecomp = comp;

			//java.util.TreeSet keyset = new java.util.TreeSet();
			//keyset.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0, false));
			//setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keyset);
		}

		/**
		 * Intercept commands to set field to null
		 */
		public void processKeyEvent(KeyEvent evt) {
			if (evt.getKeyCode() == KeyEvent.VK_DELETE && evt.isControlDown()) {
				m_instancecomp.setModified(true);
				m_instancecomp.setValue(null);
				/**
				 * you need this to force a repaint of the overlay window in the
				 * instance view. If the editor has a zero-length string and you
				 * hit ctrl-delete, the view will not be repainted
				 */
				m_instancecomp.getComponent().repaint();
			} else
				super.processKeyEvent(evt);
		}
	}
}
