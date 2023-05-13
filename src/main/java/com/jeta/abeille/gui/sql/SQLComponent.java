package com.jeta.abeille.gui.sql;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.foundation.gui.editor.TSEditorUtils;

/**
 * This class is used primarly as a holder for returning components when
 * creating a SQL editor.
 * 
 * @author Jeff Tassin
 */
public class SQLComponent {
	private JEditorPane m_editor;
	private JComponent m_extcomp;
	private SQLKit m_kit;

	public SQLComponent(SQLKit kit, JEditorPane editor, JComponent extComp) {
		m_kit = kit;
		m_editor = editor;
		m_extcomp = extComp;
	}

	public JEditorPane getEditor() {
		return m_editor;
	}

	public JComponent getExtComponent() {
		return m_extcomp;
	}

	public SQLKit getKit() {
		return m_kit;
	}

	/**
	 * Sets the connection for ths editor. This is needed for metadata object
	 * name completion.
	 */
	public void setConnection(TSConnection conn) {
		m_kit.setConnection(conn);
	}

	/**
	 * Enables/Disables line numbering for this component
	 */
	public void setLineNumberEnabled(boolean lineNumberEnabled) {
		TSEditorUtils.setLineNumberEnabled(m_editor, lineNumberEnabled);
	}

	/**
	 * Shows the statusbar on the given editor
	 */
	public void showStatusBar(boolean bShow) {
		TSEditorUtils.showStatusBar(m_editor, bShow);
	}

}
