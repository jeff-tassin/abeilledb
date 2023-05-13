package com.jeta.abeille.gui.main;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.preferences.Preferences;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

import com.jeta.open.rules.JETARule;

public class GeneralPreferences implements Preferences {
	private GeneralPreferencesView m_view;

	public void apply() {
		if (m_view != null) {
			m_view.save();
		}
	}

	public JETARule getValidator() {
		return (JETARule) getView().getController();
	}

	public TSPanel getView() {
		if (m_view == null) {
			TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
			m_view = new GeneralPreferencesView(userprops);
		}
		return m_view;
	}

	public String getTitle() {
		return I18N.getLocalizedMessage("General");
	}

}
