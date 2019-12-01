/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

public class SortMode {
	private final String m_element;

	SortMode(String element) {
		this.m_element = element;
	}

	public String toString() {
		return m_element;
	}

	public static final SortMode ASCENDING = new SortMode("ascending");
	public static final SortMode DESCENDING = new SortMode("descending");
	public static final SortMode NONE = new SortMode("none");

}
