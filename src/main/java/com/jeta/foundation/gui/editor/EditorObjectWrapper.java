/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import javax.swing.Icon;

/**
 * Wrapper class that is used for rendering objects in the various editor
 * manager classes (KeyBinding, Macros, Abbreviations )
 * 
 * @author Jeff Tassin
 */
public class EditorObjectWrapper {
	public EditorObjectWrapper(Object object, String displayTxt, Icon icon) {
		this.object = object;
		this.icon = icon;
		this.displayTxt = displayTxt;
	}

	public Object object;
	public Icon icon;
	public String displayTxt;

	public String toString() {
		return displayTxt;
	}
}
