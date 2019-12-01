/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

public class EnableEnum {
	private int m_mask;

	public static final int ENABLE_MENUS_MASK = 1;
	public static final int ENABLE_TOOLBARS_MASK = 2;
	public static final int ENABLE_CONTENT_MASK = 4;

	public static final EnableEnum ENABLE_MENUS = new EnableEnum(ENABLE_MENUS_MASK);
	public static final EnableEnum ENABLE_TOOLBARS = new EnableEnum(ENABLE_TOOLBARS_MASK);
	public static final EnableEnum ENABLE_MENUS_AND_TOOLBARS = new EnableEnum(ENABLE_MENUS_MASK | ENABLE_TOOLBARS_MASK);
	public static final EnableEnum ENABLE_ALL = new EnableEnum(ENABLE_MENUS_MASK | ENABLE_TOOLBARS_MASK
			| ENABLE_CONTENT_MASK);

	public EnableEnum(int mask) {
		m_mask = mask;
	}

	public boolean isEnableMenus() {
		return ((m_mask & ENABLE_MENUS_MASK) != 0);
	}

	public boolean isEnableToolBars() {
		return ((m_mask & ENABLE_TOOLBARS_MASK) != 0);
	}

	public boolean isEnableContent() {
		return ((m_mask & ENABLE_CONTENT_MASK) != 0);
	}

}
