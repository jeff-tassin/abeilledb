/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.utils;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComboBox;

/**
 * Very often we have JComboBoxes that are loaded with String values but which
 * map to integers. So, we use this helper class to make using combo boxes in
 * these situations a little easier.
 * 
 * @author Jeff Tassin
 */
public class IntegerComboMap {
	/**
	 * The actual map
	 */
	private LinkedList m_map = new LinkedList();

	/**
	 * Assigns a combo box item to an integer value
	 */
	public void map(int key, String comboItem) {
		Iterator iter = m_map.iterator();
		while (iter.hasNext()) {
			KeyValuePair kv = (KeyValuePair) iter.next();
			if (kv.key == key)
				iter.remove();
		}
		m_map.add(new KeyValuePair(key, comboItem));
	}

	/**
	 * @return the selected map value. -1 is returned if no item is selected in
	 *         the box or no match is found in this map for the selected item in
	 *         the combo.
	 */
	public int getSelectedValue(JComboBox cbox) {
		if (cbox == null)
			return -1;

		Object value = cbox.getSelectedItem();
		if (value == null)
			return -1;

		Iterator iter = m_map.iterator();
		while (iter.hasNext()) {
			KeyValuePair kv = (KeyValuePair) iter.next();
			if (value.equals(kv.value))
				return kv.key;
		}
		return -1;
	}

	/**
	 * Sets the selected item in the combo box using the key.
	 */
	public void setSelectedItem(JComboBox cbox, int key) {
		if (cbox == null)
			return;

		Iterator iter = m_map.iterator();
		while (iter.hasNext()) {
			KeyValuePair kv = (KeyValuePair) iter.next();
			if (key == kv.key) {
				cbox.setSelectedItem(kv.value);
				return;
			}
		}
	}

	private static class KeyValuePair {
		Object value;
		int key;

		KeyValuePair(int key, Object value) {
			this.key = key;
			this.value = value;
		}
	}
}
