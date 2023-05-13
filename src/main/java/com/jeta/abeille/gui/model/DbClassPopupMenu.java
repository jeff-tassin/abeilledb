package com.jeta.abeille.gui.model;

import java.awt.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class DbClassPopupMenu extends JPopupMenu {
	private HashSet m_cache = new HashSet();

	public void clearMenus() {
		Iterator iter = m_cache.iterator();
		while (iter.hasNext()) {
			remove((JMenuItem) iter.next());
		}
		m_cache.clear();
		if (getComponentCount() > 0) {
			Component comp = getComponent(getComponentCount() - 1);
			if (comp instanceof JSeparator) {
				remove(comp);
			}
		}

	}

	public void addMenuItems(DbObjectClassTree tree) {
		addSeparator();
		Collection menuitems = tree.getContextMenuItems();
		Iterator iter = menuitems.iterator();
		while (iter.hasNext()) {
			JMenuItem item = (JMenuItem) iter.next();
			add(item);
			m_cache.add(item);
		}
	}
}
