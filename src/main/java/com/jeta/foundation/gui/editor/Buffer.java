/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.Container;

import java.io.File;

import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.text.Document;

/**
 * Represents a buffer in the JEmacs frame
 * 
 * @author Jeff Tassin
 */
public class Buffer {
	private JEditorPane editor; // the editor pane that displays this buffer
	private Container panel; // the panel that contains the editor

	private String m_name; // the name of this buffer (if it is tied to a file,
							// this is simply the file name )
	private JMenuItem menuitem; // the menu item in the Buffers menu for this
								// buffer

	/** the underyling file for this buffer (if this buffer is tied to a file) */
	private File m_file;
	/** the last modified time for the file */
	private long m_lastmodified;

	/**
	 * the title for this buffer. this is the name of the buffer + the instance
	 * number of the buffer with the same name as other buffer
	 */
	private String m_title;

	/**
	 * Clears the contents of this buffer
	 */
	public void clear() {
		editor.setText("");
	}

	/**
	 * Called when the buffer is closed.
	 */
	void close() {
		notifyClose();
	}

	/**
	 * @return the component that contains the editor
	 */
	public Container getContentPanel() {
		return panel;
	}

	/**
	 * @return the underlying editor component
	 */
	public JEditorPane getEditor() {
		return editor;
	}

	/**
	 * @return the underlying file fro this buffer. Null is returned if this
	 *         buffer has not been saved to a file
	 */
	public File getFile() {
		return m_file;
	}

	/**
	 * @return the name of this buffer
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return the text in the editor that is associated with this buffer
	 */
	public String getText() {
		return editor.getText();
	}

	/**
	 * @return the title for this buffer. This is used when we have two or more
	 *         buffers with the same name. In this case, the title is the name +
	 *         (#), where number is the instance of this buffer with the same
	 *         name
	 */
	public String getTitle() {
		return m_title;
	}

	/**
	 * Gets the file lastmodified time and compares to the time that this buffer
	 * was last loaded from that file. If the file has a more recent modified
	 * time, we return true.
	 */
	public boolean isFileModified() {
		if (m_file == null)
			return false;

		return (m_file.lastModified() != m_lastmodified);
	}

	/**
	 * @return true if the underlying document is modified
	 */
	public boolean isModified() {
		Document doc = editor.getDocument();
		Boolean bmod = (Boolean) doc.getProperty(TSTextNames.MODIFIED);
		return bmod.booleanValue();
	}

	/**
	 * Inform this buffer that it is being closed
	 */
	protected void notifyClose() {
		// no op
	}

	public void requestFocus() {
		if (editor != null)
			editor.requestFocus();
	}

	/**
	 * Sets the main container for the editor
	 */
	public void setContentPanel(Container c) {
		panel = c;
	}

	/**
	 * Sets the editable flag on the editor
	 */
	public void setEditable(boolean bEditable) {
		this.editor.setEditable(bEditable);
	}

	/**
	 * Sets the editor that is associated with this buffer
	 */
	public void setEditor(JEditorPane editor) {
		this.editor = editor;
	}

	/**
	 * Sets the enabled flag on the editor
	 */
	public void setEnabled(boolean bEnabled) {
		this.editor.setEnabled(bEnabled);
	}

	/**
	 * Sets the underlying file object
	 */
	public void setFile(File f) {
		m_file = f;
		if (m_file == null)
			m_name = "Untitled";
		else {
			m_name = m_file.getName();
			m_lastmodified = m_file.lastModified();
		}
		setTitle(m_name);
	}

	/**
	 * Sets the name for this buffer
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * Sets the title for this buffer
	 */
	public void setTitle(String title) {
		m_title = title;
	}
}
