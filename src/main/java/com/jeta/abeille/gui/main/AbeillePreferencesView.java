package com.jeta.abeille.gui.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JTabbedPane;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.keyboard.KeyBindingsPreferences;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.preferences.Preferences;
import com.jeta.foundation.gui.preferences.PreferencesValidator;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.open.rules.JETARule;

public class AbeillePreferencesView extends TSPanel {
	private JTabbedPane m_tabpane;

	private TSConnection m_connection;

	/**
	 * A linked list of Preferences objects
	 */
	private LinkedList m_prefs = new LinkedList();

	/**
	 * This object validates all of the preferences as a group
	 */
	private PreferencesValidator m_validator = new PreferencesValidator();

	public AbeillePreferencesView(TSConnection conn) {

		m_connection = conn;
		setLayout(new BorderLayout());
		m_tabpane = new JTabbedPane();
		add(m_tabpane, BorderLayout.CENTER);
		addView(new GeneralPreferences());
		addView(new KeyBindingsPreferences());
		if (conn != null) {
			addView(new DatabasePreferences(conn));
			addView(new com.jeta.abeille.gui.sql.SQLPreferences(conn));
		}
		addView(new com.jeta.abeille.gui.model.options.ModelViewPreferences());
	}

	public void addView(Preferences prefs) {
		m_prefs.add(prefs);
		m_validator.addPreferences(prefs);
		m_tabpane.addTab(prefs.getTitle(), prefs.getView());
	}

	public JETARule getValidator() {
		return m_validator;
	}

	/**
	 * @return the preferred size for this view. This method iterates over all
	 *         views in the tab window and calculates the width/height based on
	 *         the views preferred widths/heights
	 */
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		Dimension d2 = TSGuiToolbox.getWindowDimension(7, 12);
		d2.height = d.height;
		return d2;
	}

	public void save() {
		Iterator iter = m_prefs.iterator();
		while (iter.hasNext()) {
			Preferences prefs = (Preferences) iter.next();
			prefs.apply();
		}
	}
}
