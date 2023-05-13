/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Color;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JList;
import javax.swing.text.JTextComponent;

/**
 * This class manages text completion for a editor pane. This includes popup
 * management
 */
public abstract class CompletionHandler extends KeyAdapter {
	private PopupList m_inputPopup;
	private JTextComponent m_editor;

	/**
	 * ctor
	 */
	public CompletionHandler(JTextComponent editor, PopupList inputPopup) {
		m_editor = editor;
		m_inputPopup = inputPopup;
		// the completor will now get all key events from editor and handle the
		// popup management
		editor.addKeyListener(this);

	}

	public void showPopup() {

	}
}
