/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.*;
import java.io.*;
import java.net.URL;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import com.jeta.foundation.gui.components.*;

/**
 * 
 * @author Jeff Tassin
 */
public class TSTextFrame extends TSInternalFrame {
	private TSEditorMgr m_editormgr;

	/**
	 * Constructor
	 */
	public TSTextFrame(String frameTitle) {
		super(frameTitle);
		initialize();
		// setController( new TSTextController( this, getEditorPane() ) );
	}

	/**
	 * Creates the menu used for this frame window
	 */
	protected void createMenu() {
		TSEditorUtils.buildMenu(this);

	}

	/**
	 * Creates the toolbar used for this frame window
	 */
	protected void createToolBar() {
		TSEditorUtils.buildToolBar(this);
	}

	/**
	 * Removes the underlying editor component from the frame
	 */
	public void closeEditor() {
		JComponent c = org.netbeans.editor.Utilities.getEditorUI(getEditorPane()).getExtComponent();
		getContentPane().remove(c);
	}

	/**
	 * @return the editor window for this frame
	 */
	public JEditorPane getEditorPane() {
		return m_editormgr.getEditorPane();
	}

	/**
	 * Initializes the frame window
	 */
	protected void initialize() {
		m_editormgr = new TSEditorMgr();

		createMenu();
		createToolBar();

		Component c = org.netbeans.editor.Utilities.getEditorUI(getEditorPane()).getExtComponent();
		getContentPane().add(c);

		setLocation(150, 150);
		pack();
	}

	/**
	 * Sets the text for the sql window. Any existing text is replaced
	 * 
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		m_editormgr.setText(text);
	}
}
