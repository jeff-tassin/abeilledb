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

public class DynamicMenuTemplate implements MenuTemplate {
	private ArrayList m_menus = new ArrayList();

	private MenuFinder m_finder = new MenuFinder();

	public void add(MenuDefinition menu) {
		m_finder.addMenu(menu);
		m_menus.add(menu);
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
	public void add(MenuDefinition menu, int index) {
		m_menus.add(index, menu);
	}

	public ComponentFinder getComponentFinder() {
		return m_finder;
	}

	public void clear() {
		assert (false);
		m_menus.clear();
		m_finder.reset();
	}

	/**
    */
	public MenuDefinition getMenuAt(int index) {
		return (MenuDefinition) m_menus.get(index);
	}

	public int getMenuCount() {
		return m_menus.size();
	}

	class MenuFinder implements ComponentFinder {
		private HashMap m_menu_items = new HashMap();

		void addItem(JMenuItem menuitem) {
			LinkedList list = (LinkedList) m_menu_items.get(menuitem.getName());
			if (list == null) {
				list = new LinkedList();
				m_menu_items.put(menuitem.getName(), list);
			}
			list.add(menuitem);
		}

		public void addMenu(MenuDefinition menu) {
			if (menu == null)
				return;

			for (int index = 0; index < menu.getItemCount(); index++) {
				Object defitem = menu.getItem(index);
				if (defitem instanceof JMenuItem) {
					JMenuItem item = (JMenuItem) defitem;
					String item_name = TSUtils.fastTrim(item.getName());
					if (item_name.length() > 0)
						addItem(item);
				}
			}
		}

		public Component getComponentByName(String compName) {
			LinkedList list = (LinkedList) m_menu_items.get(compName);
			if (list == null || list.size() == 0)
				return null;
			else
				return (Component) list.getFirst();
		}

		public Collection getComponentsByName(String compName) {
			LinkedList list = (LinkedList) m_menu_items.get(compName);
			if (list == null)
				return EmptyCollection.getInstance();
			else
				return list;
		}

		public void reset() {
			assert (false);
			m_menu_items.clear();
		}
	}

}
