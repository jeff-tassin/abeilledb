/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.preferences;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.open.rules.JETARule;

public interface Preferences {
	public void apply();

	public JETARule getValidator();

	public TSPanel getView();

	public String getTitle();
}
