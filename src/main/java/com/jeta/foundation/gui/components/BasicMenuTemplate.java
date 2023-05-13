/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.support.ComponentFinder;
import com.jeta.open.support.CompositeComponentFinder;
import com.jeta.open.support.DefaultComponentFinder;
import com.jeta.open.support.EmptyCollection;

public class BasicMenuTemplate implements MenuTemplate {
	private ArrayList m_menus = new ArrayList();
	private CompositeComponentFinder m_finder = new CompositeComponentFinder();

	public BasicMenuTemplate() {
	}

	public void add(MenuDefinition menudef) {
		JMenu menu = menudef.createMenu();
		m_finder.add(new DefaultComponentFinder(menu));
		m_menus.add(menudef);
	}

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
	public void add(MenuDefinition menudef, int index) {
		JMenu menu = menudef.createMenu();
		m_finder.add(new DefaultComponentFinder(menu));
		m_menus.add(index, menudef);
	}

	public ComponentFinder getComponentFinder() {
		return m_finder;
	}

	public void clear() {
		m_menus.clear();
		m_finder.reset();
	}

	public int getMenuCount() {
		return m_menus.size();
	}

	public MenuDefinition getMenuAt(int index) {
		return (MenuDefinition) m_menus.get(index);
	}

}
