package com.jeta.abeille.gui.main;

import java.awt.Component;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.DynamicMenuTemplate;

public class DynamicMenuBar extends JMenuBar {
	private LinkedList m_app_menus = new LinkedList();

	public DynamicMenuBar() {

	}

	public void addApplicationMenu(MenuDefinition menu) {
		m_app_menus.add(menu);
	}

	public JMenu getApplicationMenu(String name) {
		if (name == null)
			return null;

		for (int index = 0; index < getMenuCount(); index++) {
			JMenu menu = getMenu(index);
			if (name.equals(menu.getName()))
				return menu;
		}
		return null;
	}

	public void clearMenus() {
		int pos = 0;
		int total = getMenuCount();
		for (int index = 0; index < total; index++) {
			JMenu menu = getMenu(pos);
			menu.removeAll();
		}
		removeAll();
		Iterator iter = m_app_menus.iterator();
		while (iter.hasNext()) {
			MenuDefinition menu_def = (MenuDefinition) iter.next();
			add(menu_def.createMenu());
		}
	}

	public void setMenus(MenuTemplate mnutemplate) {
		if (mnutemplate instanceof DynamicMenuTemplate) {
			DynamicMenuTemplate template = (DynamicMenuTemplate) mnutemplate;
			clearMenus();
			int menu_index = 1;
			for (int index = 0; index < template.getMenuCount(); index++) {
				MenuDefinition menu_def = template.getMenuAt(index);
				JMenu app_menu = getApplicationMenu(menu_def.getName());
				if (app_menu != null) {
					if (menu_def.getItemCount() > 0)
						app_menu.insertSeparator(0);

					for (int sub_index = 0; sub_index < menu_def.getItemCount(); sub_index++) {
						Object item = menu_def.getItem(sub_index);
						if (item instanceof JMenuItem)
							app_menu.insert((JMenuItem) item, sub_index);
						else if (item instanceof JSeparator) {
							app_menu.insertSeparator(sub_index);
						} else
							assert (false);
					}
				} else {
					JMenu menu = menu_def.createMenu();
					add(menu, menu_index);
					menu_index++;
				}
			}
			revalidate();
			repaint();
		}
	}

}
