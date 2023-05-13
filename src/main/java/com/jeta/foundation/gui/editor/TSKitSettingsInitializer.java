/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.BaseKit;

/**
 * Extended settings for application
 * 
 * @author Jeff Tassin
 */

public class TSKitSettingsInitializer extends Settings.AbstractInitializer {
	/** Name assigned to initializer */
	public static final String NAME = "ts-settings-initializer";

	public static final Color defaultStatusBarBoldForeColor = Color.white;
	public static final Color defaultStatusBarBoldBackColor = new Color(115, 152, 198);
	public static final Coloring defaultStatusBarBoldColoring = new Coloring(null, defaultStatusBarBoldForeColor,
			defaultStatusBarBoldBackColor);

	/**
	 * Construct new ts-settings-initializer.
	 */
	public TSKitSettingsInitializer() {
		super(NAME);
	}

	/**
	 * Update map filled with the settings.
	 * 
	 * @param kitClass
	 *            kit class for which the settings are being updated. It is
	 *            always non-null value.
	 * @param settingsMap
	 *            map holding [setting-name, setting-value] pairs. The map can
	 *            be empty if this is the first initializer that updates it or
	 *            if no previous initializers updated it.
	 */
	public void updateSettingsMap(Class kitClass, Map settingsMap) {
		if (kitClass == TSKit.class) {
			Color defaultLineNumberForeColor = new Color(121, 135, 180);
			Color defaultLineNumberBackColor = new Color(224, 224, 224);
			Coloring defaultLineNumberColoring = new Coloring(null, defaultLineNumberForeColor,
					defaultLineNumberBackColor);
			SettingsUtil.setColoring(settingsMap, SettingsNames.LINE_NUMBER_COLORING, defaultLineNumberColoring);

			SettingsUtil.setColoring(settingsMap, SettingsNames.STATUS_BAR_BOLD_COLORING, defaultStatusBarBoldColoring);

			// EditorOptionsModel editormodel =
			// EditorOptionsModel.getInstance();

		}

	}

}
