/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;

import java.lang.ref.WeakReference;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.JTableHeader;

/**
 * This class listens for mouse events on the column headers of a sorted table.
 * When the user clicks a column, this class invokes the appropriate sort
 * routine for that column.
 * 
 * @author Jeff Tassin
 */
class TableSorterHeaderMouseAdapter extends MouseAdapter {
	private WeakReference m_sorterref;
	private WeakReference m_tablepanelref;
	private WeakReference m_tableref;

	/**
	 * ctor
	 */
	public TableSorterHeaderMouseAdapter(TableSorter sorter, JTable table, TSTablePanel tablePanel) {
		m_sorterref = new WeakReference(sorter);
		m_tableref = new WeakReference(table);
		m_tablepanelref = new WeakReference(tablePanel);
	}

	/**
	 * @return the underlying table panel ( can be null even if not gc'ed)
	 */
	TSTablePanel getTablePanel() {
		return (TSTablePanel) m_tablepanelref.get();
	}

	/**
	 * @return the underlying table sorter. This will be null if the table has
	 *         been gc'ed
	 */
	TableSorter getSorter() {
		return (TableSorter) m_sorterref.get();
	}

	/**
	 * @return the underlying table. This will be null if the table has been
	 *         gc'ed
	 */
	JTable getTable() {
		return (JTable) m_tableref.get();
	}

	/**
	 * MouseAdapter event
	 */
	public void mouseClicked(MouseEvent e) {
		TSTablePanel tablepanel = getTablePanel();
		JTable table = getTable();
		TableSorter sorter = getSorter();

		// sometimes the controller can be null here even if not garbage
		// collected
		if (sorter != null && table != null) {
			TableColumnModel columnmodel = table.getColumnModel();
			int viewcolumn = columnmodel.getColumnIndexAtX(e.getX());
			if (viewcolumn < 0)
				return;

			/**
			 * allow the controller to check if a column can be sorted this is
			 * important when we have a large query result that has not been
			 * fully downloaded to the table
			 */
			if (tablepanel != null) {
				if (!tablepanel.canSortColumn(table, viewcolumn)) {
					return;
				}
			}

			int modelcolumn = columnmodel.getColumn(viewcolumn).getModelIndex();

			TableColumn tablecol = columnmodel.getColumn(viewcolumn);
			SortedColumnHeaderRenderer renderer = null;
			Object obj = tablecol.getHeaderRenderer();
			if (obj instanceof SortedColumnHeaderRenderer)
				renderer = (SortedColumnHeaderRenderer) obj;

			if (e.getClickCount() == 1 && modelcolumn != -1) {
				SortMode mode = SortMode.NONE;
				int shiftpressed = e.getModifiers() & InputEvent.SHIFT_MASK;
				int ctrlpressed = e.getModifiers() & InputEvent.CTRL_MASK;

				if (shiftpressed != 0 && ctrlpressed != 0)
					mode = SortMode.NONE; // user explicitly chose natural sort
				else if (shiftpressed != 0)
					mode = SortMode.DESCENDING; // user explicitly chose
												// descending sort
				else if (ctrlpressed != 0)
					mode = SortMode.ASCENDING; // user explicitly chose
												// ascending sort
				else {
					// then toggle through the sort states
					if (renderer != null) {
						mode = renderer.getSortMode();
						if (mode == SortMode.NONE)
							mode = SortMode.ASCENDING;
						else if (mode == SortMode.ASCENDING)
							mode = SortMode.DESCENDING;
						else if (mode == SortMode.DESCENDING)
							mode = SortMode.NONE;
						else
							mode = SortMode.ASCENDING;
					} else
						mode = SortMode.ASCENDING;
				}
				sorter.sortByColumn(modelcolumn, mode);

				if (renderer != null) {
					TableUtils.clearColumnHeaders(table);
					renderer.setSortMode(mode);
				}
			}
		}
	}
}
