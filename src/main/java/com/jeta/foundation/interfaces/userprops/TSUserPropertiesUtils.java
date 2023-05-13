/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.interfaces.userprops;

import java.awt.Color;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.utils.TSUtils;

/**
 * Utils for store/retrive user properties
 * 
 * @author Jeff Tassin
 */
public class TSUserPropertiesUtils {

	public static boolean getBoolean(String propName, boolean defValue) {
		boolean result = defValue;
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		String value = userprops.getProperty(propName);
		if (value != null) {
			try {
				result = Boolean.valueOf(value).booleanValue();
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}

		return result;
	}

	public static Color getColor(String propName, Color defaultColor) {
		assert (defaultColor != null);
		if (defaultColor != null) {
			int rgb = getInteger(propName, defaultColor.getRGB());
			return new Color(rgb);
		} else {
			return Color.black;
		}
	}

	public static int getInteger(String propName, int defValue) {
		int result = defValue;
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		String value = userprops.getProperty(propName);
		if (value != null) {
			try {
				result = Integer.parseInt(value);
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}
		return result;
	}

	public static String getString(String propName, String defValue) {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		return userprops.getProperty(propName, defValue);
	}

	public static void setBoolean(String propName, boolean value) {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		userprops.setProperty(propName, String.valueOf(value));
	}

	public static void setColor(String propName, Color color) {
		assert (color != null);
		if (color != null) {
			setInteger(propName, color.getRGB());
		}
	}

	public static void setInteger(String propName, int value) {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		userprops.setProperty(propName, String.valueOf(value));
	}

	public static void setString(String propName, String value) {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		userprops.setProperty(propName, value);
	}

}
