package com.jeta.abeille.gui.sql;

import java.awt.Color;
import java.util.Map;

import org.netbeans.editor.Coloring;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtEditorUI;

public class SQLEditorUI extends ExtEditorUI {

	private boolean m_PROD = false;
	
	public SQLEditorUI(boolean isPROD) {
		m_PROD = isPROD;
		/*
		Map map = this.getColoringMap();
		if ( m_PROD ) {
			Coloring sqlColoring = new Coloring(SettingsDefaults.defaultFont, SettingsDefaults.defaultForeColor, new Color(255,173,173));
			map.put(SettingsNames.DEFAULT_COLORING, sqlColoring );
		}
		*/
	}
	
	public Coloring getDefaultColoring() {
		if ( m_PROD ) {
			return new Coloring(SettingsDefaults.defaultFont, SettingsDefaults.defaultForeColor, new Color(255,200,200));
		} else {
			return super.getDefaultColoring();
		}
	}
}
