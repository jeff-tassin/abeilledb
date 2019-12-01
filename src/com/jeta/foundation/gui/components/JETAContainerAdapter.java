/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Component;
import java.awt.Container;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.UIDirector;
import com.jeta.open.support.DefaultComponentFinder;

public class JETAContainerAdapter implements JETAContainer {
	private Container m_cc;
	private DefaultComponentFinder m_finder;
	private UIDirector m_delegate;

	public JETAContainerAdapter(Container cc, UIDirector delegate) {
		m_cc = cc;
		m_delegate = delegate;
		m_finder = new DefaultComponentFinder(m_cc);
	}

	/**
	 * Enables/Disables the menu/toolbar button associated with the commandid
	 * 
	 * @param commandId
	 *            the id of the command whose button to enable/disable
	 * @param bEnable
	 *            true/false to enable/disable
	 */
	public void enableComponent(String commandId, boolean bEnable) {
		Collection comps = m_finder.getComponentsByName(commandId);
		Iterator iter = comps.iterator();
		while (iter.hasNext()) {
			Component comp = (Component) iter.next();
			boolean en = comp.isEnabled();
			if (en != bEnable)
				comp.setEnabled(bEnable);
		}
	}

	/**
	 * Locates the first component found in this container hierarchy that has
	 * the given name. This will recursively search into child containers as
	 * well. If no component is found with the given name, null is returned.
	 * 
	 * @param compName
	 *            the name of the component to search for
	 * @return the named component
	 */
	public Component getComponentByName(String compName) {
		return m_finder.getComponentByName(compName);
	}

	/**
	 * Locates all components found in this container hierarchy that has the
	 * given name. This will recursively search into child containers as well.
	 * This method is useful for frame windows that can have multiple components
	 * with the same name. For example, a menu item and toolbar button for the
	 * same command would have the same name.
	 * 
	 * @param compName
	 *            the name of the components to search for
	 * @return a collection of @see Component objects that have the given name.
	 */
	public Collection getComponentsByName(String compName) {
		return m_finder.getComponentsByName(compName);

	}

	/**
	 * Returns the UIDirector for this container. UIDirectors are part of this
	 * framework and are responsible for enabling/disabling components based on
	 * the program state. For example, menu items and toolbar buttons must be
	 * enabled or disabled depending on the current state of the frame window.
	 * UIDirectors handle this logic.
	 * 
	 * @return the UIDirector
	 */
	public UIDirector getUIDirector() {
		return null;
	}
}
