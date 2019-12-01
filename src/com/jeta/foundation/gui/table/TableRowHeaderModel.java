/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import javax.swing.AbstractListModel;
import javax.swing.JTable;

public class TableRowHeaderModel extends AbstractListModel {
	private JTable table;

	/**
	 * ctor
	 */
	public TableRowHeaderModel(JTable table) {
		this.table = table;
	}

	public int getSize() {
		return table.getRowCount();
	}

	public Object getElementAt(int index) {
		return null;
	}

	public void tableChanged() {
		fireContentsChanged(this, 0, table.getRowCount());
	}
}
