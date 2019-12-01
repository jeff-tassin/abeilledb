/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.i18n;

import java.util.ResourceBundle;
import java.util.Locale;
import java.text.MessageFormat;
import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.*;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.interfaces.resources.ResourceLoader;
import com.jeta.foundation.componentmgr.ComponentMgr;

/**
 * This is a utility class for dealing with internationalization issues
 * 
 * @author Jeff Tassin
 */
public class I18NHelper {
	private Locale m_locale;
	private static I18NHelper m_singleton;
	private LinkedList m_bundles = new LinkedList();

	private I18NHelper() {

	}

	public boolean equals(String arg1, String arg2) {
		if (arg1 == null || arg2 == null)
			return false;

		return arg1.equals(arg2);
	}

	public static I18NHelper getInstance() {
		if (m_singleton == null)
			m_singleton = new I18NHelper();

		return m_singleton;
	}

	public void loadBundle(String bundleName) {
		ResourceBundle currentbundle = null;
		ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
		currentbundle = ResourceBundle.getBundle(bundleName, m_locale, loader.getClassLoader());
		m_bundles.add(currentbundle);
	}

	public void setLocale(Locale locale) {
		Locale.setDefault(locale);
		m_locale = locale;
	}

	/**
	 * @return a locale specific message string for a message with a variable
	 *         number of arguments. Allows string messages to be composed
	 *         dynamically.
	 */
	public String format(String template, Object[] arguments) {
		MessageFormat formatter = new MessageFormat("");
		formatter.setLocale(m_locale);
		formatter.applyPattern(getLocalizedMessage(template));
		return formatter.format(arguments);
	}

	public String _getLocalizedMessage(ResourceBundle bundle, String messageId) {
		String result = null;
		try {
			result = bundle.getString(messageId);
		} catch (MissingResourceException mre) {
		}
		return result;
	}

	public String getLocalizedMessage(String messageId) {
		if (messageId == null)
			return null;

		String result = null;
		Iterator iter = m_bundles.iterator();
		while (iter.hasNext()) {
			ResourceBundle bundle = (ResourceBundle) iter.next();
			result = _getLocalizedMessage(bundle, messageId);
			if (result != null)
				break;

		}
		if (result == null) {
			result = messageId;
		}
		return result;
	}

	public boolean toBoolean(String strVal) {
		if (strVal == null)
			return false;
		return strVal.compareToIgnoreCase(getLocalizedMessage("true")) == 0;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// Helpers

	public char closeParenthesis() {
		return ')';
	}

	public char comma() {
		return ',';
	}

	public char newline() {
		return '\n';
	}

	public char openParenthesis() {
		return '(';
	}

	public char semicolon() {
		return ';';
	}

	public char space() {
		return ' ';
	}

	/**
	 * --- international JOption panes With 1.3, the labels are in properties
	 * files. You can add a property file with the labels for a JFileChooser,
	 * JColorChooser, and/or JOptionPane. All of these are set in the files
	 * basic.properties, or their localized version. The standard JDK from Sun
	 * includes ones for Japanese basic_ja.properties and Chinese
	 * basic_zh.properties. You would need to create one named appropriately,
	 * and located in a directory javax/swing/plaf/basic/resources in your
	 * CLASSPATH. With 1.2/1.1 (and also working in 1.3), you can just tell the
	 * UIManager to use different labels before creating the component. For
	 * French, you would do the following (assuming my French is correct):
	 * UIManager.put("OptionPane.yesButtonText", "Oui");
	 * UIManager.put("OptionPane.cancelButtonText", "Annulent");
	 * UIManager.put("OptionPane.noButotnText, "Non");
	 * UIManager.put("OptionPane.okButotnText, "D'accord");
	 */

}
