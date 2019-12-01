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

package org.netbeans.editor.ext.java;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
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
 * Extended settings for Java.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavaSettingsInitializer extends Settings.AbstractInitializer {

	/** Name assigned to initializer */
	public static final String NAME = "java-settings-initializer";

	private Class javaKitClass;

	/**
	 * Construct new java-settings-initializer.
	 * 
	 * @param javaKitClass
	 *            the real kit class for which the settings are created. It's
	 *            unknown here so it must be passed to this constructor.
	 */
	public JavaSettingsInitializer(Class javaKitClass) {
		super(NAME);
		this.javaKitClass = javaKitClass;
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

		// Update java colorings
		if (kitClass == BaseKit.class) {

			new JavaSettingsDefaults.JavaTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);
			new JavaSettingsDefaults.JavaLayerTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);

		}

		if (kitClass == javaKitClass) {

			SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST,
					JavaSettingsDefaults.getJavaKeyBindings());

			SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST, new TokenContext[] {
					JavaTokenContext.context, JavaLayerTokenContext.context });

			settingsMap.put(SettingsNames.ABBREV_MAP, JavaSettingsDefaults.getJavaAbbrevMap());

			settingsMap.put(SettingsNames.MACRO_MAP, JavaSettingsDefaults.getJavaMacroMap());

			settingsMap.put(ExtSettingsNames.CARET_SIMPLE_MATCH_BRACE,
					JavaSettingsDefaults.defaultCaretSimpleMatchBrace);

			settingsMap.put(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE, JavaSettingsDefaults.defaultHighlightMatchBrace);

			settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR, JavaSettingsDefaults.defaultIdentifierAcceptor);

			settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR, JavaSettingsDefaults.defaultAbbrevResetAcceptor);

			settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE, JavaSettingsDefaults.defaultWordMatchMatchCase);

			settingsMap.put(SettingsNames.WORD_MATCH_STATIC_WORDS, JavaSettingsDefaults.defaultWordMatchStaticWords);

			// Formatting settings
			settingsMap.put(JavaSettingsNames.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS,
					JavaSettingsDefaults.defaultJavaFormatSpaceBeforeParenthesis);

			settingsMap.put(JavaSettingsNames.JAVA_FORMAT_SPACE_AFTER_COMMA,
					JavaSettingsDefaults.defaultJavaFormatSpaceAfterComma);

			settingsMap.put(JavaSettingsNames.JAVA_FORMAT_NEWLINE_BEFORE_BRACE,
					JavaSettingsDefaults.defaultJavaFormatNewlineBeforeBrace);

			settingsMap.put(JavaSettingsNames.JAVA_FORMAT_LEADING_SPACE_IN_COMMENT,
					JavaSettingsDefaults.defaultJavaFormatLeadingSpaceInComment);

			settingsMap.put(JavaSettingsNames.JAVA_FORMAT_LEADING_STAR_IN_COMMENT,
					JavaSettingsDefaults.defaultJavaFormatLeadingStarInComment);

			settingsMap.put(JavaSettingsNames.INDENT_HOT_CHARS_ACCEPTOR,
					JavaSettingsDefaults.defaultIndentHotCharsAcceptor);

			settingsMap.put(ExtSettingsNames.REINDENT_WITH_TEXT_BEFORE, Boolean.FALSE);
		}

	}

}
