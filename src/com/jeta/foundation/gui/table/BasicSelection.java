/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

/**
 * This class represents a single column of selected data. This is not for use
 * with tables. It is for general purpose copy/paste formatting.
 * 
 * @author Jeff Tassin
 */
public class BasicSelection extends JETATableModel {
	/**
	 * ctor
	 */
	public BasicSelection() {
		String[] names = new String[1];
		names[0] = "";

		setColumnNames(names);

		Class[] types = new Class[1];
		types[0] = Object.class;

		setColumnTypes(types);
	}

}
