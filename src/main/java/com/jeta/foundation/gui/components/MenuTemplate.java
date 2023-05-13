/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import com.jeta.open.support.ComponentFinder;

public interface MenuTemplate {
	/**
	 * Appends the menu at the end of this template
	 */
	public void add(MenuDefinition menu);

	/**
	 * Inserts a menu before the specified index Note that indexes are zero
	 * based.
	 * 
	 * @param index
	 *            the index before which to insert the menu
	 * @param menu
	 *            the menu to insert.
	 * @deprecated
	 */
	public void add(MenuDefinition menu, int index);

	/**
	 * Returns the component finder for the menus in this template
	 */
	public ComponentFinder getComponentFinder();

	/**
	 * Clears all menus from this template
	 */
	public void clear();

	/**
	 * Returns the menu at the given index
	 */
	public MenuDefinition getMenuAt(int index);

	/**
	 * Returns the number of menus in this template
	 */
	public int getMenuCount();
}
