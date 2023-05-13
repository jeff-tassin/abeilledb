/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import javax.swing.JTable;

class MyRowSelection extends RowSelection {
	JTable table;

	public MyRowSelection(JTable table, int row) {
		super(row);
		this.table = table;
	}
}
