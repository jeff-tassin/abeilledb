package com.jeta.abeille.gui.main;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.preferences.Preferences;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

import com.jeta.open.rules.JETARule;

public class DatabasePreferences implements Preferences {
	private TSConnection m_connection;

	private DatabasePreferencesView m_view;

	public DatabasePreferences(TSConnection conn) {
		m_connection = conn;
	}

	public void apply() {
		if (m_view != null) {
			m_view.save();
		}
	}

	public JETARule getValidator() {
		return (JETARule) getView().getController();
	}

	/**
	 * Expecting 1 parameter in the params array (a TSConnection object)
	 */
	public TSPanel getView() {
		if (m_view == null) {
			TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
			m_view = new DatabasePreferencesView(m_connection, userprops);
		}
		return m_view;
	}

	public String getTitle() {
		return I18N.getLocalizedMessage("Database");
	}

}
