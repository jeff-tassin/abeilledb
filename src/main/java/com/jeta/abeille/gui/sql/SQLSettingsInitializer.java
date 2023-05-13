package com.jeta.abeille.gui.sql;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.ExtSettingsNames;

/**
 * Extended settings for SQL
 * 
 * @author Jeff Tassin
 */

public class SQLSettingsInitializer extends Settings.AbstractInitializer {

	/** Name assigned to initializer */
	public static final String NAME = "sql-settings-initializer";


	/**
	 * Construct new sql-settings-initializer.
	 * 
	 * @param sqlKitClass
	 *            the real kit class for which the settings are created. It's
	 *            unknown here so it must be passed to this constructor.
	 */
	public SQLSettingsInitializer() {
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

		// Update sql colorings
		if (kitClass == BaseKit.class) {
			new SQLSettingsDefaults.SQLTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);
			new SQLSettingsDefaults.SQLLayerTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);
		}

		if (kitClass == SQLKit.class ) {
			SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST,	SQLSettingsDefaults.getSQLKeyBindings());

			SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST, new TokenContext[] {
					SQLTokenContext.context, SQLLayerTokenContext.context });


			settingsMap.put(SettingsNames.ABBREV_MAP, SQLSettingsDefaults.getSQLAbbrevMap());

			settingsMap.put(SettingsNames.MACRO_MAP, SQLSettingsDefaults.getSQLMacroMap());

			settingsMap.put(ExtSettingsNames.CARET_SIMPLE_MATCH_BRACE, SQLSettingsDefaults.defaultCaretSimpleMatchBrace);

			settingsMap.put(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE, SQLSettingsDefaults.defaultHighlightMatchBrace);

			settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR, SQLSettingsDefaults.defaultIdentifierAcceptor);

			settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR, SQLSettingsDefaults.defaultAbbrevResetAcceptor);

			settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE, SQLSettingsDefaults.defaultWordMatchMatchCase);

			settingsMap.put(SettingsNames.WORD_MATCH_STATIC_WORDS, SQLSettingsDefaults.defaultWordMatchStaticWords);

			settingsMap.put(SettingsNames.TEXT_LIMIT_LINE_COLOR, new java.awt.Color(211, 222, 255));
		}

	}

}
