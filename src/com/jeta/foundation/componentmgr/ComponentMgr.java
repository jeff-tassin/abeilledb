/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.componentmgr;

import java.util.*;
import java.lang.reflect.*;

/**
 * ComponentMgr is a top level class that is used to manager components for the
 * application. Client classes can obtain an instance to a well know service by
 * using the component mgr.
 * 
 * @author Jeff Tassin
 */
public class ComponentMgr {
	private static Hashtable m_components = new Hashtable();
	private static TSComponent m_appShutdown = null;
	private static LinkedList m_shutdownOrder = new LinkedList(); // shutdown in
																	// order
																	// they were
																	// registered

	public static boolean isDebug() {
		return true;
	}

	synchronized public static Object lookup(String componentName) {
		return m_components.get(componentName);
	}

	synchronized public static void registerComponent(String componentName, Object componentImpl) {
		m_components.put(componentName, componentImpl);
		m_shutdownOrder.addFirst(componentImpl);
	}

	public static void shutdown() {

		Iterator iter = m_shutdownOrder.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof TSComponent) {
				TSComponent tscomp = (TSComponent) obj;
				tscomp.shutdown();
			}
		}
		shutdown(m_appShutdown);
	}

	/**
	 * sets the object that is responsible for shutting down the program and
	 * exiting. The ComponentMgr introspects the object and looks for a public
	 * shutdown() method. If it finds one, the method is called.
	 */
	public static void setAppShutdown(TSComponent shutdown) {
		m_appShutdown = shutdown;
	}

	public static void shutdown(TSComponent obj) {
		if (obj == null)
			return;

		obj.shutdown();

	}

}
