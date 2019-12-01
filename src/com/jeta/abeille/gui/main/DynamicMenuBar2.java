package com.jeta.abeille.gui.main;

import java.awt.Component;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.DynamicMenuTemplate;

public class DynamicMenuBar2 extends JMenuBar {
	private HashSet m_dyn_menus = new HashSet();
	private HashSet m_dyn_items = new HashSet();
	private HashMap m_app_menus = new HashMap();

	public DynamicMenuBar2() {

	}

	public void addApplicationMenu(MenuDefinition menu) {
		if (menu.getName() != null) {
			m_app_menus.put(menu.getName(), menu);
		} else {
			assert (false);
		}
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
			if (m_dyn_menus.contains(menu)) {
				remove(menu);
				m_dyn_menus.remove(menu);
				menu.removeAll();
			} else {
				pos++;

				int item_total = menu.getItemCount();
				int item_pos = 0;
				for (int item = 0; item < item_total; item++) {
					Object menuitem = menu.getItem(item_pos);
					if (m_dyn_items.contains(menuitem)) {
						if (menuitem instanceof Component)
							menu.remove((Component) menuitem);
						m_dyn_items.remove(menuitem);
					} else {
						item_pos++;
					}
				}

				item_total = menu.getComponentCount();
				item_pos = 0;
				for (int item = 0; item < item_total; item++) {
					Component comp = menu.getComponent(item_pos);
					if (m_dyn_items.contains(comp)) {
						if (comp instanceof Component)
							menu.remove((Component) comp);
						m_dyn_items.remove(comp);
					} else {
						item_pos++;
					}
				}
			}
		}
	}

	private void markDynamic(JMenu menu) {
		m_dyn_menus.add(menu);
	}

	private void markDynamicItems(MenuDefinition menu) {
		for (int item = 0; item < menu.getItemCount(); item++) {
			Object menuitem = menu.getItem(item);
			// if ( menuitem instanceof JMenuItem )
			m_dyn_items.add(menuitem);
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
					for (int sub_index = 0; sub_index < menu_def.getItemCount(); sub_index++) {
						Object item = menu_def.getItem(sub_index);
						if (item instanceof JMenuItem)
							app_menu.insert((JMenuItem) item, sub_index);
						else if (item instanceof JSeparator) {
							app_menu.insertSeparator(sub_index);
						} else
							assert (false);
					}
					markDynamicItems(menu_def);
				} else {
					JMenu menu = menu_def.createMenu();
					add(menu, menu_index);
					markDynamic(menu);
					menu_index++;
				}
			}
			revalidate();
			repaint();
		}
	}

}
