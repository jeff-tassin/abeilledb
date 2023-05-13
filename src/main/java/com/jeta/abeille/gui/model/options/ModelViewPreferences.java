package com.jeta.abeille.gui.model.options;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.gui.model.ModelViewFrame;

import com.jeta.foundation.componentmgr.ComponentMgr;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.preferences.Preferences;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

import com.jeta.open.rules.JETARule;

public class ModelViewPreferences implements Preferences {
	private ModelViewPreferencesView m_view;

	public void apply() {
		if (m_view != null) {
			m_view.saveToModel();
			m_view.getModel().save();

			TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
			Collection frames = wsframe.getAllFrames(null);
			Iterator iter = frames.iterator();
			while (iter.hasNext()) {
				Object frame = iter.next();
				if (frame instanceof ModelViewFrame) {
					ModelViewFrame modelframe = (ModelViewFrame) frame;
					modelframe.updateSettings();
				}
			}
		}
	}

	public JETARule getValidator() {
		return null;
	}

	public TSPanel getView() {
		if (m_view == null) {
			ModelViewPreferencesModel model = new ModelViewPreferencesModel();
			m_view = new ModelViewPreferencesView(model);
		}
		return m_view;
	}

	public String getTitle() {
		return I18N.getLocalizedMessage("Model View");
	}
}
