/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;

/**
 *
 */
public class DualTableSorter extends TableSorter {
	TableModel m_primarymodel;
	TableSorter m_syncmodel;
	private boolean m_bignoreTableChanges = false;
	private TSTablePanel m_tablepanel;

	public DualTableSorter(TSTablePanel panel, TableModel model, TableSorter syncmodel) {
		super(model);
		m_tablepanel = panel;
		m_primarymodel = model;
		m_syncmodel = syncmodel;
	}

	public void ignoreTableChanges(boolean bignore) {
		m_bignoreTableChanges = bignore;
	}

	public void sortByColumn(int column, SortMode sortMode) {
		super.sortByColumn(column, sortMode);
		if (m_syncmodel != null && !m_tablepanel.isSplitHorizontal())
			m_syncmodel.setIndexes(m_indexes);
	}

	public void setSyncTableSorter(TableSorter sorter) {
		m_syncmodel = sorter;
	}

	public void tableChanged(TableModelEvent e) {
		if (!m_bignoreTableChanges) {
			super.tableChanged(e);
		}
	}
}
