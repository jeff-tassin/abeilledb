/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.UIDirector;
import com.jeta.open.support.ComponentFinder;
import com.jeta.open.support.CompositeComponentFinder;
import com.jeta.open.support.DefaultComponentFinder;

/**
 * This class defines a container for a JTable(s) used in the application.
 * 
 * @author Jeff Tassin
 */
public abstract class AbstractTablePanel extends TSPanel {
	/** context menu invoked when user right clicks on table with mouse */
	private TablePopupMenu m_tablepopup;

	/**
	 * An array of table column options that determine the column settings for
	 * the horizontal split and the normal view
	 */
	private ArrayList m_tablecolumns = new ArrayList();

	/**
	 * ctor
	 */
	public AbstractTablePanel() {
		m_tablepopup = new TablePopupMenu();
		m_tablepopup.add(createMenuItem(I18N.getLocalizedMessage("Deselect"), TSTableNames.ID_DESELECT, null));
		m_tablepopup
				.add(createMenuItem(I18N.getLocalizedMessage("Select Column"), TSTableNames.ID_SELECT_COLUMN, null));
		m_tablepopup.add(createMenuItem(I18N.getLocalizedMessage("Copy"), TSComponentNames.ID_COPY, null));
		m_tablepopup.add(createMenuItem(I18N.getLocalizedMessage("Copy Special"), TSComponentNames.ID_COPY_SPECIAL,
				null));

		// the listener for the popup should be set in the specialized class of
		// AbstractTablePanel
	}

	/**
	 * Converts a table row the the corresponding model index. This is needed
	 * when the table is sorted. If the table is not sorted, the table index
	 * will equal the model index
	 * 
	 * @param index
	 *            the table index to convert
	 * @return the corresponding model index
	 */
	public int convertTableToModelIndex(JTable table, int index) {
		int result = index;
		if (index >= 0) {
			TableModel model = table.getModel();
			if (model instanceof TableSorter) {
				TableSorter sorter = (TableSorter) model;
				result = sorter.getModelRow(index);
			}
		}
		return result;
	}

	/**
	 * Creates a component finder that is used to locate child components in
	 * this panel by name.
	 */
	protected ComponentFinder createComponentFinder() {
		CompositeComponentFinder finder = new CompositeComponentFinder();
		finder.add(new DefaultComponentFinder(this));
		finder.add(new DefaultComponentFinder(m_tablepopup));
		return finder;
	}

	/**
	 * Deselects any selected cells in the table
	 */
	public abstract void deselect();

	/**
	 * @return the underlying data model. This is the original model, not the
	 *         TableSorter (if there is one )
	 */
	public abstract TableModel getModel();

	/**
	 * @return the popup menu used for the table.
	 */
	public TablePopupMenu getPopupMenu() {
		return m_tablepopup;
	}

	/**
	 * @return the selected rows (in *MODEL* coordinates not Table) for the
	 *         given table. The table must be a table managed by this panel
	 *         (either table1 or table2)
	 */
	public int[] getSelectedColumns(JTable table) {
		int[] cols = table.getSelectedColumns();
		TableColumnModel colmodel = table.getColumnModel();
		// here we need to convert from table coordinates to model coordinates
		for (int index = 0; index < cols.length; index++) {
			TableColumn column = colmodel.getColumn(cols[index]);
			cols[index] = column.getModelIndex();
		}
		return cols;
	}

	/**
	 * @return the selected rows (in model coordinates) for the given table. The
	 *         table must be a table managed by this panel (either table1 or
	 *         table2)
	 */
	public int[] getSelectedRows(JTable table) {
		int[] rows = table.getSelectedRows();
		for (int index = 0; index < rows.length; index++) {
			rows[index] = convertTableToModelIndex(table, rows[index]);
		}
		return rows;
	}

	/**
	 * @return a selection object that represents the selected cells in the
	 *         table *model*
	 */
	public abstract TableSelection getSelection();

	/**
	 * @return the scroll pane for the given table in the panel
	 */
	public abstract JScrollPane getScrollPane(JTable table);

	/**
	 * @return the current table
	 */
	public abstract JTable getTable();

	/**
	 * @return the table 1
	 */
	abstract JTable getTable1();

	/**
	 * @return the table 2 (can be null)
	 */
	abstract JTable getTable2();

	/**
	 * @return the table column info object at the given model index
	 */
	public TableColumnInfo getColumnInfo(int modelindex) {
		if (modelindex >= 0 && modelindex < m_tablecolumns.size()) {
			return (TableColumnInfo) m_tablecolumns.get(modelindex);
		} else {
			assert (false);
			return null;
		}
	}

	/**
	 * @return the table column info object associated with the given column
	 */
	public TableColumnInfo getColumnInfo(TableColumn col) {
		return getColumnInfo(col.getModelIndex());
	}

	/**
	 * @return a collection of TableColumnInfo objects for the table
	 */
	Collection getTableColumns() {
		return m_tablecolumns;
	}

	/**
	 * @return the UIDirector for the panel
	 */
	public abstract UIDirector getUIDirector();

	/**
	 * Resets the column sizes based on the TableColumnInfo settings
	 */
	public abstract void resizeColumns();

	/**
	 * This reads the column widths, settings, and column orders for table 1 and
	 * stores them in the table columns array
	 */
	public abstract void saveTableSettings();

	/**
	 * Select all cells in the given column.
	 */
	public abstract void selectColumn(int colIndex);

	/**
	 * Selects an entire column.
	 * 
	 * @param table
	 *            the table that is to be selected. The column number is
	 *            relative to this table. This value will be either the free or
	 *            fixed table.
	 * @param column
	 *            the column in the given table to select
	 */
	public void selectColumn(JTable table, int column) {
		if (table != null) {
			int rowcount = table.getRowCount();
			table.setRowSelectionInterval(0, rowcount - 1);
			table.setColumnSelectionInterval(column, column);
		}
	}

	/**
	 * Sets the table cell rendere for the given table column. You need to call
	 * this if you want your renderer to be present when the user splits the
	 * view. New tables are created when the view is split/unsplit, so we need
	 * to cache the renderes here
	 */
	public void setCellRenderer(int colindex, TableCellRenderer renderer) {
		TableColumnInfo info = (TableColumnInfo) m_tablecolumns.get(colindex);
		assert (info != null);
		if (info != null) {
			TableColumn column = info.getColumn();
			column.setCellRenderer(renderer);
		}
	}

	/**
	 * Sets the table that currently has focus
	 */
	abstract void setFocusTable(JTable table);

	/**
    *
    */
	public void syncTableColumnCache() {
		m_tablecolumns.clear();
		TableColumnModel defaultcolmodel = getTable1().getColumnModel();
		for (int index = 0; index < defaultcolmodel.getColumnCount(); index++) {
			TableColumn column = defaultcolmodel.getColumn(index);
			TableColumnInfo info = new TableColumnInfo(column, getModel().getColumnName(index));
			m_tablecolumns.add(info);
		}
	}

}
