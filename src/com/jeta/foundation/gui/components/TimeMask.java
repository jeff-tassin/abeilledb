/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.io.StringReader;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.datetime.TimeMaskParser;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

/**
 * This class defines a date mask used by the time field. It checks if the mask
 * is valid after a parse operation valid definitions are any ordering of the
 * given mask values. However, a mask value can appear only once in the
 * definitions. If an element is added to the definition, but is not a
 * pre-defined mask value, then we assume that element is static text that will
 * be displayed as is in the date field.
 * 
 * @author Jeff Tassin
 */
public class TimeMask {
	public static final String hh = "hh";
	public static final String HH = "HH";
	public static final String mm = "mm";
	public static final String ss = "ss";
	public static final String a = "a";

	private LinkedList m_definition = new LinkedList();
	private boolean m_valid = true;

	public static final String COMPONENT_ID = "jeta.TimeMask";

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
		String mask = "hh:mm:ss a";

		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		if (userprops != null) {
			mask = userprops.getProperty(TimeMask.COMPONENT_ID, mask);
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
	 *         the following: (hh or HH) mm and ss and optionally 'a' only if hh
	 *         is selected
	 */
	public boolean isValid() {
		int hcount = 0;
		int Hcount = 0;
		int mcount = 0;
		int scount = 0;
		int acount = 0;

		Iterator iter = m_definition.iterator();
		while (iter.hasNext()) {
			String def = (String) iter.next();
			if (def.equals(hh))
				hcount++;
			if (def.equals(HH))
				Hcount++;
			else if (def.equals(ss))
				scount++;
			else if (def.equals(mm))
				mcount++;
			else if (def.equals(a))
				acount++;
		}

		if (hcount == 1) {
			if (Hcount != 0)
				return false;

			return (mcount == 1 && scount == 1 && acount == 1);
		} else if (Hcount == 1) {
			if (hcount != 0)
				return false;

			return (mcount == 1 && scount == 1 && acount == 0);
		} else {
			return false;
		}
	}

	/**
	 * Parses the given mask and determines if it is valid
	 */
	public static boolean isValid(String mask) {
		TimeMask tmask = TimeMask.parse(mask);
		return (tmask != null && tmask.isValid());
	}

	/**
	 * Parses the string and returns a TimeMask object. This method may return
	 * null if the parse failed. If it returns an object, you still need to Call
	 * TimeMask.isValid to determine if the mask was valid or not
	 */
	public static TimeMask parse(String mask) {
		TimeMask dmask = null;
		try {
			StringReader reader = new StringReader(mask);
			TimeMaskParser parser = new TimeMaskParser(reader);
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
			if (TimeMask.isValid(mask)) {
				userprops.setProperty(TimeMask.COMPONENT_ID, mask);
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
