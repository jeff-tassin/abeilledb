/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.jeta.foundation.i18n.I18N;

/**
 * PluginMgr is used to manage plugin classes for the TS foundation framework.
 * 
 * @author Jeff Tassin
 */
public class PluginMgr {
	/**
	 * @return an Instance of a given plugin.
	 */
	public static Object getPlugin(String pluginName) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		String objectname = I18N.getResource(pluginName);
		Class pluginclass = Class.forName(objectname);
		Object pluginobj = pluginclass.newInstance();
		return pluginobj;
	}

	/**
	 * Runs the specified method on the specified plugin object.
	 * 
	 * @param pluginName
	 *            the name of the plugin object to run
	 */
	public static void runPlugin(String pluginName, String method, Class[] paramtypes, Object[] params) {
		try {
			String objectname = I18N.getResource(pluginName);
			Class pluginclass = Class.forName(objectname);
			Object pluginobj = pluginclass.newInstance();
			Method m = pluginclass.getDeclaredMethod(method, paramtypes);
			m.invoke(pluginobj, params);
			return;
		} catch (ClassNotFoundException e) {
			// catch for now
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		System.out.println("runPlugin failed for " + pluginName + "  " + method);
	}

}
