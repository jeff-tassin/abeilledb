package com.jeta.abeille.gui.model.options;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import com.jeta.abeille.gui.model.ModelViewSettings;

import com.jeta.foundation.gui.components.JETAColorWell;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.layouts.ColumnLayout;
import com.jeta.foundation.gui.layouts.TableLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;

import com.jeta.forms.components.panel.FormPanel;

public class ModelViewPreferencesView extends TSPanel {
	private ModelViewPreferencesModel m_model;
	private FormPanel m_view;

	public ModelViewPreferencesView(ModelViewPreferencesModel model) {
		m_view = new FormPanel("com/jeta/abeille/gui/model/options/modelOptions.jfrm");
		setLayout(new BorderLayout());
		add(m_view, BorderLayout.CENTER);
		loadData(model);
	}

	public ModelViewPreferencesModel getModel() {
		return m_model;
	}

	private void loadColor(String prop_name) {
		JETAColorWell inkwell = (JETAColorWell) m_view.getComponentByName(prop_name);
		if (inkwell != null && m_model != null) {
			inkwell.setColor(m_model.getColor(prop_name));
		}
	}

	/**
	 * Loads the preferences from the model
	 */
	public void loadData(ModelViewPreferencesModel model) {
		m_model = model;
		if (m_model != null) {
			m_view.setSelected(ModelOptionsNames.ID_DISPLAY_ICON, m_model.isShowIcon());
			m_view.setSelected(ModelOptionsNames.ID_DISPLAY_DATA_TYPE, m_model.isShowType());
			m_view.setSelected(ModelOptionsNames.ID_USE_GRADIENT, m_model.isPaintGradient());
			m_view.setSelected(ModelOptionsNames.ID_AUTO_LOAD_MODEL, m_model.isAutoLoad());
			m_view.setSelected(ModelOptionsNames.ID_AUTO_SAVE_MODEL, m_model.isAutoSave());

			loadColor(ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND);
			loadColor(ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND);
			loadColor(ModelViewSettings.ID_TABLE_FOREGROUND);
			loadColor(ModelViewSettings.ID_TABLE_BACKGROUND);
			loadColor(ModelViewSettings.ID_PROTOTYPE_FOREGROUND);
			loadColor(ModelViewSettings.ID_PROTOTYPE_BACKGROUND);
		}
	}

	/**
	 * Saves the preferences in the view to the data model
	 */
	public void saveToModel() {
		m_model.setShowIcon(m_view.getBoolean(ModelOptionsNames.ID_DISPLAY_ICON));
		m_model.setShowType(m_view.getBoolean(ModelOptionsNames.ID_DISPLAY_DATA_TYPE));
		m_model.setPaintGradient(m_view.getBoolean(ModelOptionsNames.ID_USE_GRADIENT));
		m_model.setAutoLoad(m_view.getBoolean(ModelOptionsNames.ID_AUTO_LOAD_MODEL));
		m_model.setAutoSave(m_view.getBoolean(ModelOptionsNames.ID_AUTO_SAVE_MODEL));

		saveColor(ModelViewSettings.ID_TABLE_FOREGROUND);
		saveColor(ModelViewSettings.ID_TABLE_BACKGROUND);
		saveColor(ModelViewSettings.ID_PROTOTYPE_FOREGROUND);
		saveColor(ModelViewSettings.ID_PROTOTYPE_BACKGROUND);
		saveColor(ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND);
		saveColor(ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND);
	}

	private void saveColor(String prop_name) {
		JETAColorWell inkwell = (JETAColorWell) m_view.getComponentByName(prop_name);
		if (inkwell != null) {
			m_model.setColor(prop_name, inkwell.getColor());
		} else {
			assert (false);
		}
	}
}
