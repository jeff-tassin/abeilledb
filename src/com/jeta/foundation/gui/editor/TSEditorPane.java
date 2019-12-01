/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import javax.swing.JEditorPane;

/**
 * We provide a specialization of JEditorPane.
 * 
 * @author Jeff Tassin
 */
public class TSEditorPane extends JEditorPane {

	public TSEditorPane() {

	}

	/**
	 * ctor Creates a JEditorPane that has been initialized to the given text.
	 * This is a convenience constructor that calls the setContentType and
	 * setText methods.
	 * 
	 * @param contentType
	 *            mime type of the given texttext
	 * @param txt
	 *            the text to initialize with
	 */
	public TSEditorPane(String contentType, String txt) {
		super(contentType, txt);
	}

	public void requestFocus() {
		super.requestFocus();
	}

}
