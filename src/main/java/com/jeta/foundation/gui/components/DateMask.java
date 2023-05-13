/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.io.StringReader;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.foundation.componentmgr.ComponentMgr;

import com.jeta.foundation.gui.components.datetime.DateMaskParser;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

/**
 * This class defines a date mask used by the date field. It checks if the mask
 * is valid after a parse operation valid definitions are any ordering of the
 * given mask values. However, a mask value can appear only once in the
 * definitions. If an element is added to the definition, but is not a
 * pre-defined mask value, then we assume that element is static text that will
 * be displayed as is in the date field.
 * 
 * @author Jeff Tassin
 */
public class DateMask {
	public static final String yyyy = "yyyy";
	public static final String MM = "MM";
	public static final String MMM = "MMM";
	public static final String dd = "dd";

	private LinkedList m_definition = new LinkedList();
	private boolean m_valid = true;

	public static final String COMPONENT_ID = "jeta.DateMask";

	/**
	 * Adds an element to this mask
	 */
	public void addElement(String el) {
		m_definition.add(el);
	}

	/**
	 * The default mask is the mask entered by the user in the preferences
	 * dialog. If this value has not been entered, then the default mask depends
	 * on the locale
	 */
	public static String getDefaultMask() {
		String mask = "MMM dd, yyyy";

		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		if (userprops != null) {
			mask = userprops.getProperty(DateMask.COMPONENT_ID, mask);
		}
		return mask;
	}

	/**
	 * @return the collection of elements that make up the mask
	 */
	public Collection getElements() {
		return m_definition;
	}

	/**
	 * Prints this mask contents to the console
	 */
	public void print() {
		Iterator iter = m_definition.iterator();
		while (iter.hasNext()) {
			String def = (String) iter.next();
			System.out.println("def: " + def);
		}
	}

	/**
	 * @return true if this mask is valid. A valid mask contains at least one of
	 *         the following: YYYY dd and (MM or MMM)
	 */
	public boolean isValid() {
		int mcount = 0;
		int ycount = 0;
		int dcount = 0;

		Iterator iter = m_definition.iterator();
		while (iter.hasNext()) {
			String def = (String) iter.next();
			if (def.equals(yyyy))
				ycount++;
			else if (def.equals(dd))
				dcount++;
			else if (def.equals(MM))
				mcount++;
			else if (def.equals(MMM))
				mcount++;
		}
		return (ycount == 1 && mcount == 1 && dcount == 1);
	}

	/**
	 * Parses the given mask and determines if it is valid
	 */
	public static boolean isValid(String mask) {
		DateMask dmask = DateMask.parse(mask);
		return (dmask != null && dmask.isValid());
	}

	/**
	 * Parses the string and returns a DateMask object. This method may return
	 * null if the parse failed. If it returns an object, you still need to Call
	 * DateMask.isValid to determine if the mask was valid or not
	 */
	public static DateMask parse(String mask) {
		DateMask dmask = null;
		try {
			StringReader reader = new StringReader(mask);
			DateMaskParser parser = new DateMaskParser(reader);
			dmask = parser.parse();
		} catch (Exception e) {

		} catch (Error e) {

		}
		return dmask;
	}

	/**
	 * The default mask is the mask entered by the user in the preferences
	 * dialog. If this value has not been entered, then the default mask depends
	 * on the locale
	 */
	public static void setDefaultMask(String mask) {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		if (userprops != null) {
			if (DateMask.isValid(mask)) {
				userprops.setProperty(DateMask.COMPONENT_ID, mask);
			}
		}
	}

	/**
	 * @return this mask as a string
	 */
	public String toString() {
		StringBuffer sbuff = new StringBuffer();
		Iterator iter = m_definition.iterator();
		while (iter.hasNext()) {
			String def = (String) iter.next();
			sbuff.append(def);
		}
		return sbuff.toString();
	}
}
