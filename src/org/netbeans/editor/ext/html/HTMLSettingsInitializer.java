/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.html;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenCategory;

/**
 * Extended settings provide the settings for the extended editor features
 * supported by the various classes of this package.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public class HTMLSettingsInitializer extends Settings.AbstractInitializer {

	private final Class htmlKitClass;

	/** Name assigned to initializer */
	public static final String NAME = "html-settings-initializer";

	/**
	 * Construct HTML Settings initializer
	 * 
	 * @param htmlKitClass
	 *            the real kit class for which the settings are created. It's
	 *            unknown here so it must be passed to this constructor.
	 */
	public HTMLSettingsInitializer(Class htmlKitClass) {
		super(NAME);
		this.htmlKitClass = htmlKitClass;
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

		if (kitClass == BaseKit.class) {

			new HTMLTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);

		}

		if (kitClass == htmlKitClass) {

			SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
					new TokenContext[] { HTMLTokenContext.context });
		}

	}

	static class HTMLTokenColoringInitializer extends SettingsUtil.TokenColoringInitializer {

		Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
		Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
		Settings.Evaluator boldSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.BOLD);
		Settings.Evaluator italicSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);
		Settings.Evaluator lightGraySubst = new SettingsUtil.ForeColorPrintColoringEvaluator(Color.lightGray);

		public HTMLTokenColoringInitializer() {
			super(HTMLTokenContext.context);
		}

		public Object getTokenColoring(TokenContextPath tokenContextPath, TokenCategory tokenIDOrCategory,
				boolean printingSet) {
			if (!printingSet) {
				switch (tokenIDOrCategory.getNumericID()) {
				case HTMLTokenContext.TEXT_ID:
				case HTMLTokenContext.WS_ID:
					return SettingsDefaults.emptyColoring;

				case HTMLTokenContext.ERROR_ID:
					return new Coloring(null, Color.white, Color.red);

				case HTMLTokenContext.TAG_ID:
					return new Coloring(null, Color.blue, null);

				case HTMLTokenContext.ARGUMENT_ID:
					return new Coloring(null, Color.green.darker().darker(), null);

				case HTMLTokenContext.OPERATOR_ID:
					return new Coloring(null, Color.green.darker().darker(), null);

				case HTMLTokenContext.VALUE_ID:
					return new Coloring(null, new Color(153, 0, 107), null);

				case HTMLTokenContext.BLOCK_COMMENT_ID:
					return new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE, Color.gray, null);

				case HTMLTokenContext.SGML_COMMENT_ID:
					return new Coloring(null, Color.gray, null);

				case HTMLTokenContext.DECLARATION_ID:
					return new Coloring(null, new Color(191, 146, 33), null);

				case HTMLTokenContext.CHARACTER_ID:
					return new Coloring(null, Color.red.darker(), null);
				}

			} else { // printing set
				switch (tokenIDOrCategory.getNumericID()) {
				case HTMLTokenContext.BLOCK_COMMENT_ID:
				case HTMLTokenContext.SGML_COMMENT_ID:
					return lightGraySubst;

				default:
					return SettingsUtil.defaultPrintColoringEvaluator;
				}

			}

			return null;

		}

	}

}
