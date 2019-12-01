/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * This interface is used to provide specialized implementations for building
 * menus. It allows builders to be developed to manage complex menus and share
 * those menus with various classes.
 * 
 * @author Jeff Tassin
 */
public interface TSMenuBuilder {
	public JMenuItem createMenuItem(String itemText, String actionCmd, KeyStroke keyStroke);

	public MenuTemplate getMenuTemplate();
}
