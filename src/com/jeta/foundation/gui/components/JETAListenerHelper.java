/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import java.lang.reflect.Method;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * 
 * @author Jeff Tassin
 */
public class JETAListenerHelper {

	/**
	 * The container we are adding and removing listeners for.
	 */
	private Container m_container;

	/**
	 * A list of ListenerInfo objects
	 */
	private LinkedList m_listeners = new LinkedList();

	private TSComponentFinder m_finder;

	/**
	 * ctor
	 */
	public JETAListenerHelper(Container c) {
		m_container = c;
		m_finder = new TSComponentFinder(c);
	}

	public void registerListener(String compName, Object listener) {
		if (listener instanceof ActionListener) {
			m_listeners.add(new ListenerInfo(compName, ActionListener.class, "addActionListener",
					"removeActionListener", listener));
		}

		if (listener instanceof ChangeListener) {
			m_listeners.add(new ListenerInfo(compName, ChangeListener.class, "addChangeListener",
					"removeChangeListener", listener));
		}

		if (listener instanceof ItemListener) {
			m_listeners.add(new ListenerInfo(compName, ItemListener.class, "addItemListener", "removeItemListener",
					listener));
		}

		if (listener instanceof ComponentListener) {
			m_listeners.add(new ListenerInfo(compName, ComponentListener.class, "addComponentListener",
					"removeComponentListener", listener));
		}

		if (listener instanceof FocusListener) {
			m_listeners.add(new ListenerInfo(compName, FocusListener.class, "addFocusListener", "removeFocusListener",
					listener));
		}

		if (listener instanceof HierarchyBoundsListener) {
			m_listeners.add(new ListenerInfo(compName, HierarchyBoundsListener.class, "addHierarchyBoundsListener",
					"removeHierarchyBoundsListener", listener));
		}

		if (listener instanceof HierarchyListener) {
			m_listeners.add(new ListenerInfo(compName, HierarchyListener.class, "addHierarchyListener",
					"removeHierarchyListener", listener));
		}

		if (listener instanceof InputMethodListener) {
			m_listeners.add(new ListenerInfo(compName, InputMethodListener.class, "addInputMethodListener",
					"removeInputMethodListener", listener));
		}

		if (listener instanceof KeyListener) {
			m_listeners.add(new ListenerInfo(compName, KeyListener.class, "addKeyListener", "removeKeyListener",
					listener));
		}

		if (listener instanceof MouseListener) {
			m_listeners.add(new ListenerInfo(compName, MouseListener.class, "addMouseListener", "removeMouseListener",
					listener));
		}

		if (listener instanceof MouseMotionListener) {
			m_listeners.add(new ListenerInfo(compName, MouseMotionListener.class, "addMouseMotionListener",
					"removeMouseMotionListener", listener));
		}

		if (listener instanceof MouseWheelListener) {
			m_listeners.add(new ListenerInfo(compName, MouseWheelListener.class, "addMouseWheelListener",
					"removeMouseWheelListener", listener));
		}

	}

	public void addListeners() {
		removeListeners();

		Object[] params = new Object[1];
		Class[] types = new Class[1];

		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			try {
				ListenerInfo linfo = (ListenerInfo) iter.next();
				types[0] = linfo.getListenerType();

				Component comp = m_finder.getComponentByName(linfo.getComponentName());
				assert (comp != null);

				Method method = comp.getClass().getMethod(linfo.getAddMethodName(), types);
				params[0] = linfo.getListener();
				method.invoke(comp, params);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void removeListeners() {
		Object[] params = new Object[1];
		Class[] types = new Class[1];

		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			try {
				ListenerInfo linfo = (ListenerInfo) iter.next();
				types[0] = linfo.getListenerType();

				Component comp = m_finder.getComponentByName(linfo.getComponentName());
				assert (comp != null);
				Method method = comp.getClass().getMethod(linfo.getRemoveMethodName(), types);
				params[0] = linfo.getListener();
				method.invoke(comp, params);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class ListenerInfo {
		private String m_compname;
		private Class m_listener_type;
		private String m_add_method;
		private String m_remove_method;
		private Object m_listener;

		public ListenerInfo(String cname, Class ltype, String addMethod, String removeMethod, Object listener) {
			m_compname = cname;
			m_listener_type = ltype;
			m_add_method = addMethod;
			m_remove_method = removeMethod;
			m_listener = listener;
		}

		public String getComponentName() {
			return m_compname;
		}

		public Class getListenerType() {
			return m_listener_type;
		}

		public String getAddMethodName() {
			return m_add_method;
		}

		public String getRemoveMethodName() {
			return m_remove_method;
		}

		public Object getListener() {
			return m_listener;
		}

	}

}
