/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.utils;

import java.util.*;

public class IntegerHashtable {

	private Hashtable m_hashTable;

	public IntegerHashtable() {
		m_hashTable = new Hashtable();

	}

	public boolean containsKey(int key) {
		return m_hashTable.containsKey(TSUtils.getInteger(key));
	}

	/*
	 * Later I will change the implementation to handle integers natively. Not
	 * just wrapper Hashtable
	 */
	public void put(int key, Object value) {
		m_hashTable.put(TSUtils.getInteger(key), value);
	}

	public Object get(int key) {
		return m_hashTable.get(TSUtils.getInteger(key));
	}

	public Set getKeySet() {
		return m_hashTable.keySet();
	}

}
