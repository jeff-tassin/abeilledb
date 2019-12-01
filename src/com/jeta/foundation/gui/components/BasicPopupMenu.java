/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.jeta.foundation.i18n.I18N;

/**
 * This class provides a simple popup menu with cut, copy, and paste items
 */
public class BasicPopupMenu extends JPopupMenu {
	/**
	 * The panel our commands will be routed through
	 */
	private TSPanel m_view;

	/** the popup location in invoker coordinates */
	private Point m_popuplocation = new Point(0, 0);

	/**
	 * ctor
	 */
	public BasicPopupMenu(TSPanel panel) {
		m_view = panel;

		add(m_view.i18n_createMenuItem("Cut", TSComponentNames.ID_CUT, null));
		add(m_view.i18n_createMenuItem("Copy", TSComponentNames.ID_COPY, null));
		add(m_view.i18n_createMenuItem("Paste", TSComponentNames.ID_PASTE, null));
	}

	/**
	 * @return the popup location
	 */
	public Point getPopupLocation() {
		return m_popuplocation;
	}

	/**
	 * Removes the menu item that has the given action command
	 */
	public void removeItem(String actionCommand) {
		if (actionCommand == null)
			return;

		for (int index = 0; index < getComponentCount(); index++) {
			Component comp = getComponent(index);
			if (comp instanceof JMenuItem) {
				JMenuItem menuitem = (JMenuItem) comp;
				if (actionCommand.equals(menuitem.getActionCommand())) {
					remove(index);
					index--;
				}
			}
		}
	}

	/**
	 * We override here so we can store the popup location
	 */
	public void show(Component invoker, int x, int y) {
		super.show(invoker, x, y);
		m_popuplocation = new Point(x, y);
	}

}
