/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.util.HashMap;
import java.awt.Component;
import java.awt.Container;

/**
 * This class recursively searches a container for all name components. It
 * caches component names and provides a convienent lookup utility for client
 * classes. This class is intended as a convenience for controllers to update
 * components on dialogs and frames.
 * 
 * @author Jeff Tassin
 */
public class TSComponentFinder {
	private HashMap m_components = new HashMap();
	private Container m_container; // the container that we search for
									// components

	/** components that are explicitly registered with this finder */
	private HashMap m_registered = new HashMap();

	public TSComponentFinder(Container container) {
		m_container = container;
	}

	public void clear() {
		m_components.clear();
		m_container = null;
	}

	/**
	 * Recursively searches all Components owned by this container. If the
	 * Component has a name, we store it in the m_components hash table
	 * 
	 * @param container
	 *            the container to search
	 */
	private void buildNames(Container container) {
		int count = container.getComponentCount();
		for (int index = 0; index < count; index++) {
			Component comp = container.getComponent(index);

			if (comp instanceof Container)
				buildNames((Container) comp);

			String name = comp.getName();
			if (name != null && name.length() > 0) {
				m_components.put(name, comp);
			}
		}
	}

	/*
	 * This method looks at all components owned by a container. It will
	 * recursively search into child containers as well.
	 * 
	 * @param componentName the name of the component to search for
	 * 
	 * @return the named component
	 */
	public Component getComponentByName(String componentName) {
		Component comp = (Component) m_components.get(componentName);
		if (comp == null) {
			comp = (Component) m_registered.get(componentName);
			if (comp == null) {
				rebuild();
				comp = (Component) m_components.get(componentName);
			}
		}
		return comp;
	}

	/**
	 * This method searches all components owned by a container for a first
	 * child component that has the given type. This method is recursive and
	 * will search into child containers as well. Null is returned if no
	 * component is found.
	 */
	public static Component getComponentByType(Container container, Class c) {
		int count = container.getComponentCount();
		for (int index = 0; index < count; index++) {
			// first try the top level
			Component comp = container.getComponent(index);

			if (c.isInstance(comp)) {
				return comp;
			} else {
				if (comp instanceof Container) {
					comp = getComponentByType((Container) comp, c);
					if (comp != null)
						return comp;
				}
			}
		}
		return null;
	}

	/**
	 * Explicitly registers a component with the finder
	 */
	public void registerComponent(Component comp) {
		assert (comp.getName() != null);
		m_registered.put(comp.getName(), comp);
	}

	/**
	 * Reloads the hashmap with all compnents
	 */
	void rebuild() {
		m_components = new HashMap();
		buildNames(m_container);
	}
}
