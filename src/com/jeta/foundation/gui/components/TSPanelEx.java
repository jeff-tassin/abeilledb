/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Iterator;

import javax.swing.JPopupMenu;

import com.jeta.open.support.ComponentFinder;
import com.jeta.open.support.CompositeComponentFinder;
import com.jeta.open.support.DefaultComponentFinder;

/**
 * A panel that also has a popup menu.
 * 
 * @author Jeff Tassin
 */
public class TSPanelEx extends TSPanel {
	private Component m_popup_owner;
	private JPopupMenu m_popup_menu;
	private PopupListener m_popup_listener = new PopupListener();

	public TSPanelEx() {

	}

	public TSPanelEx(LayoutManager layout) {
		super(layout);
	}

	/**
	 * Creates a component finder that is used to locate child components in
	 * this panel by name.
	 */
	protected ComponentFinder createComponentFinder() {
		CompositeComponentFinder finder = new CompositeComponentFinder(new DefaultComponentFinder(this));
		/** must add menus here because they are not owned by this container */
		if (m_popup_menu != null)
			finder.add(new DefaultComponentFinder(m_popup_menu));

		finder.add(new DefaultComponentFinder(this));
		return finder;
	}

	protected MouseListener getPopupHandler() {
		return m_popup_listener;
	}

	protected void setPopupMenu(JPopupMenu popup, Component owner) {
		CompositeComponentFinder cmpfinder = (CompositeComponentFinder) getComponentFinder();
		Iterator iter = cmpfinder.getFinders().iterator();
		while (iter.hasNext()) {
			ComponentFinder finder = (ComponentFinder) iter.next();
			if (finder instanceof DefaultComponentFinder) {
				DefaultComponentFinder deff = (DefaultComponentFinder) finder;
				if (popup == deff.getContainer()) {
					iter.remove();
				}
			}
		}

		if (m_popup_owner != null)
			m_popup_owner.removeMouseListener(m_popup_listener);

		m_popup_menu = popup;
		m_popup_owner = owner;
		m_popup_owner.addMouseListener(m_popup_listener);
		cmpfinder.add(new DefaultComponentFinder(popup));
	}

	public JPopupMenu getPopupMenu() {
		return m_popup_menu;
	}

	public class PopupListener extends MouseAdapter {
		void showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				updateComponents(null);
				if (m_popup_menu != null)
					m_popup_menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void mousePressed(MouseEvent e) {
			showPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}
	}

}
