/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class MenuDefinition {
	private String m_label;
	private String m_name;

	/**
	 * An array of JMenuItem objects. This includes separators.
	 */
	private ArrayList m_items = new ArrayList();

	public MenuDefinition(String label) {
		m_label = label;
	}

	public void add(JMenuItem item) {
		m_items.add(item);
	}

	public void add(MenuDefinition item) {
		m_items.add(item);
	}

	public JMenu createMenu() {
		JMenu menu = new JMenu(m_label);
		menu.setName(m_name);
		for (int index = 0; index < m_items.size(); index++) {
			Object obj = m_items.get(index);
			if (obj instanceof JSeparator)
				menu.addSeparator();
			else if (obj instanceof JMenuItem)
				menu.add((JMenuItem) obj);
			else if (obj instanceof MenuDefinition)
				menu.add(((MenuDefinition) obj).createMenu());
		}
		return menu;
	}

	public void addSeparator() {
		m_items.add(new JPopupMenu.Separator());
	}

	public String getLabel() {
		return m_label;
	}

	public int getItemCount() {
		return m_items.size();
	}

	public Object getItem(int pos) {
		return m_items.get(pos);
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}
}
