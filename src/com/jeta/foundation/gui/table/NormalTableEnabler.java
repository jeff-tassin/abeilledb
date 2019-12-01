/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This class enables the menus for the TSTablePanel when in normal mode
 * 
 * @author Jeff Tassin
 */
public class NormalTableEnabler implements UIDirector {
	private TSTablePanel m_tablepanel;

	/**
	 * ctor
	 */
	public NormalTableEnabler(TSTablePanel tspanel) {
		m_tablepanel = tspanel;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		m_tablepanel.enableComponent(TSTableNames.ID_SPLIT_COLUMN, true);
		m_tablepanel.enableComponent(TSTableNames.ID_HIDE_COLUMN, true);
	}
}
