/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import java.lang.reflect.Method;

import javax.swing.AbstractButton;
import javax.swing.JTextField;

import java.util.EventListener;

/**
 * This class recursively searches a container for child components. It searches
 * for all registered listeners on those components that are part of the
 * com.jeta package and removes those listeners. This is used to assist in
 * garbage collection. Hopefully, it ensures that a frame or container does not
 * linger because of registered listeners that are never removed.
 * 
 * @author Jeff Tassin
 */
public class JETAComponentCleanser {
	Object[] m_params = new Object[1];
	Class[] m_types = new Class[1];

	/**
	 * Recursively searches all Components owned by this container. If the
	 * Component has a name, we store it in the m_components hash table
	 * 
	 * @param container
	 *            the container to search
	 */
	public void cleanse(Container container) {
		int count = container.getComponentCount();
		for (int index = 0; index < count; index++) {
			Component comp = container.getComponent(index);

			if (comp instanceof Container)
				cleanse((Container) comp);

			removeJETAListeners(comp, ActionListener.class, "removeActionListener");
			removeJETAListeners(comp, ContainerListener.class, "removeContainerListener");
			removeJETAListeners(comp, ChangeListener.class, "removeChangeListener");
			removeJETAListeners(comp, ItemListener.class, "removeItemListener");
			removeJETAListeners(comp, ComponentListener.class, "removeComponentListener");
			removeJETAListeners(comp, FocusListener.class, "removeFocusListener");
			removeJETAListeners(comp, HierarchyBoundsListener.class, "removeHierarchyBoundsListener");
			removeJETAListeners(comp, HierarchyListener.class, "removeHierarchyListener");
			removeJETAListeners(comp, InputMethodListener.class, "removeInputMethodListener");
			removeJETAListeners(comp, KeyListener.class, "removeKeyListener");
			removeJETAListeners(comp, MouseListener.class, "removeMouseListener");
			removeJETAListeners(comp, MouseMotionListener.class, "removeMouseMotionListener");
			removeJETAListeners(comp, MouseWheelListener.class, "removeMouseWheelListener");
		}
	}

	public void removeJETAListeners(Component comp, Class listenerClass, String methodName) {
		m_types[0] = listenerClass;
		Method method = null;

		EventListener[] listeners = comp.getListeners(listenerClass);
		for (int index = 0; index < listeners.length; index++) {
			EventListener listener = listeners[index];
			String lclass = listener.getClass().getName();
			if (lclass.indexOf("com.jeta") >= 0) {
				if (method == null) {
					try {
						if (listenerClass == ActionListener.class || listenerClass == ChangeListener.class
								|| listenerClass == ItemListener.class) {
							if (comp instanceof AbstractButton || comp instanceof JTextField) {
								method = comp.getClass().getMethod(methodName, m_types);
							}
						} else {
							method = comp.getClass().getMethod(methodName, m_types);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (method != null) {
					try {
						m_params[0] = listener;
						method.invoke(comp, m_params);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
