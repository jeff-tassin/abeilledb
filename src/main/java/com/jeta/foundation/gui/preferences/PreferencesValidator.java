/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.preferences;

import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

public class PreferencesValidator implements JETARule {
	private LinkedList m_preferences = new LinkedList();

	public void addPreferences(Preferences prefs) {
		m_preferences.add(prefs);
	}

	/**
	 * JETARule implementation
	 * 
	 * @params an array of objects needed for this validator.
	 */
	public RuleResult check(Object[] params) {
		Object[] sub_params = new Object[params.length + 1];
		for (int index = 0; index < params.length; index++)
			sub_params[index] = params[index];

		RuleResult result = RuleResult.SUCCESS;
		Iterator iter = m_preferences.iterator();
		while (iter.hasNext()) {
			Preferences prefs = (Preferences) iter.next();
			sub_params[params.length] = prefs.getView();
			JETARule rule = prefs.getValidator();
			if (rule != null) {
				result = rule.check(sub_params);
				if (result != RuleResult.SUCCESS)
					return result;
			}
		}

		return result;
	}
}
