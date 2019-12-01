/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JTable;

import com.jeta.foundation.gui.table.TableSelection;

/**
 * This class builds a collection of RowSelection objects that represent the
 * selected cells in the table view. Since the table can be split, we allow
 * multiple, discontiguous selections. We merge selections based on common rows.
 * Each RowSelection object represents the selected columns for that row in the
 * query. The columns can be different from row to row
 */
public class SelectionBuilder {

	/**
	 * Builds the set of RowSelection objects for the given table panel
	 */
	public static TableSelection build(TSTablePanel panel) {

		if (panel.getViewMode() == TSTablePanel.NORMAL || panel.getViewMode() == TSTablePanel.SPLIT_VERTICAL) {
			return buildNormal(panel);
		} else {
			return buildHorizontal(panel);
		}
	}

	/**
	 * Builds the set of RowSelection objects for the given table panel
	 */
	public static TableSelection buildNormal(AbstractTablePanel panel) {
		// @todo probably need to make this a little more efficient
		JTable table1 = panel.getTable1();

		int[] rows = table1.getSelectedRows();
		int[] cols = panel.getSelectedColumns(table1);

		TreeMap selections = new TreeMap();

		// TableSelection selections = new TableSelection( panel.getModel() );
		for (int index = 0; index < rows.length; index++) {
			int row = rows[index];
			Integer irow = new Integer(row);
			RowSelection rowselection = (RowSelection) selections.get(irow);
			if (rowselection == null) {
				rowselection = new MyRowSelection(table1, row);
				selections.put(irow, rowselection);
			}
			rowselection.addColumns(cols);
		}

		JTable table2 = panel.getTable2();
		if (table2 != null) {
			rows = table2.getSelectedRows();
			cols = panel.getSelectedColumns(table2);

			for (int index = 0; index < rows.length; index++) {
				int row = rows[index];
				Integer irow = new Integer(row);
				RowSelection rowselection = (RowSelection) selections.get(irow);
				if (rowselection == null) {
					rowselection = new MyRowSelection(table2, row);
					selections.put(irow, rowselection);
				}
				rowselection.addColumns(cols);
			}
		}

		TableSelection tableselection = new TableSelection(panel.getModel(), true);

		// now convert from table coordinates to model coordinates
		Iterator iter = selections.keySet().iterator();
		while (iter.hasNext()) {
			Integer row = (Integer) iter.next();
			MyRowSelection rowselection = (MyRowSelection) selections.get(row);
			rowselection.setRow(panel.convertTableToModelIndex(rowselection.table, rowselection.getRow()));

			tableselection.add(rowselection.getRow(), rowselection);
		}

		// now return the ordered set of RowSelection objects
		// tableselection.print();

		return tableselection;
	}

	/**
	 * Builds the set of RowSelection objects for the given table panel
	 */
	public static TableSelection buildHorizontal(TSTablePanel panel) {
		// @todo probably need to make this a little more efficient
		JTable table1 = panel.getTable1();

		int[] rows = panel.getSelectedRows(table1);
		int[] cols = panel.getSelectedColumns(table1);

		// this algorithm is a little different than the normal or vertical
		// case.
		// where we first select the rows from the top table. we then select
		// the rows from the bottom table

		TableSelection tableselection = new TableSelection(panel.getModel(), false);

		// TableSelection selections = new TableSelection( panel.getModel() );
		for (int index = 0; index < rows.length; index++) {
			int row = rows[index];
			Integer irow = new Integer(row);
			RowSelection rowselection = new RowSelection(row);
			rowselection.addColumns(cols);
			tableselection.add(row, rowselection);
		}

		JTable table2 = panel.getTable2();
		if (table2 != null) {
			rows = panel.getSelectedRows(table2);
			cols = panel.getSelectedColumns(table2);

			for (int index = 0; index < rows.length; index++) {
				int row = rows[index];
				Integer irow = new Integer(row);
				RowSelection rowselection = new RowSelection(row);
				rowselection.addColumns(cols);
				tableselection.add(row, rowselection);
			}
		}

		return tableselection;
	}

}
