/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.BorderFactory;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JButton;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import com.jeta.foundation.gui.components.*;

/**
 * 
 * @author Jeff Tassin
 */
public class TSTextDialog extends TSDialog implements BufferListener {
	private JEditorPane m_editor;

	private JPanel m_editorpanel;

	private BufferMgr m_buffermgr;

	/**
	 * Constructor
	 */
	public TSTextDialog(Frame owner, boolean bModal) {
		super(owner, bModal);
	}

	/**
	 * Event that occurs when a buffer has changed
	 */
	public void bufferChanged(BufferEvent evt) {
		Buffer buffer = evt.getBuffer();
		int id = evt.getID();
		if (id == BufferEvent.BUFFER_CREATED) {
			JEditorPane editor = buffer.getEditor();
			JComponent comp = TSEditorUtils.getExtComponent(editor);

			m_editorpanel.removeAll();
			m_editorpanel.add(comp, BorderLayout.CENTER);

			m_editorpanel.revalidate();
			m_editor = editor;
		}
	}

	/**
	 * Creates the menu used for this frame window
	 */
	protected void createMenu() {
		// TSEditorUtils.buildMenu( this );
		// MenuTemplate template = getMenuTemplate();

		// JMenuBar menubar = getMenuBar();
		// menubar.remove( template.getMenuAt(
		// TSEditorUtils.MENUBAR_OPTIONS_INDEX ) );
	}

	/**
	 * Creates the toolbar used for this frame window
	 */
	protected void createToolBar() {
		// TSEditorUtils.buildToolBar( this );
	}

	/**
	 * @return the editor window for this frame
	 */
	public JEditorPane getCurrentEditor() {
		return m_editor;
	}

	/**
	 * @return the text in the editor
	 */
	public String getText() {
		JEditorPane editor = getCurrentEditor();
		if (editor != null)
			return editor.getText();
		else
			return null;
	}

	/**
	 * Initializes the frame window
	 */
	public void initialize(Class kitclass) {
		enableToolBar();
		enableMenuBar();

		createMenu();
		createToolBar();

		// let's decrease the standard font size of the ok and close buttons on
		// the dialog
		// to save some screen real estate
		JButton btn = new JButton("test");
		Font f = btn.getFont();
		f = f.deriveFont(f.getSize() - 2.0f);
		setFont(f);

		m_editor = TSEditorUtils.createEditor(kitclass, null);
		JComponent comp = TSEditorUtils.getExtComponent(m_editor);

		m_editorpanel = new JPanel(new BorderLayout());
		m_editorpanel.add(comp, BorderLayout.CENTER);
		setPrimaryPanel(m_editorpanel);

		Buffer buff = new Buffer();
		buff.setEditor(m_editor);
		m_buffermgr = new BufferMgr(buff, true);
		m_buffermgr.addBufferListener(this);

		// EditorController controller = new EditorController(this, m_buffermgr
		// );
		// controller.setUIDirector( new EditorFrameUIDirector(this,
		// m_buffermgr) );
		// setController( controller );
	}

	/**
	 * Sets the editor for this dialog
	 */
	public void setEditor(JEditorPane editor) {
		JComponent comp = TSEditorUtils.getExtComponent(m_editor);
		m_editorpanel.remove(comp);
		m_editor = editor;
		comp = TSEditorUtils.getExtComponent(editor);
		m_editorpanel.add(comp, BorderLayout.CENTER);
	}

	/**
	 * Sets the text for the sql window. Any existing text is replaced
	 * 
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		if (text == null)
			text = "";

		JEditorPane editor = getCurrentEditor();
		if (editor != null)
			editor.setText(text);

	}
}
