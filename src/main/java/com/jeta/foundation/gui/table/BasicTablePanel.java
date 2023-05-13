/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This class defines a container for a single JTable used in the application.
 * 
 * @author Jeff Tassin
 */
public class BasicTablePanel extends AbstractTablePanel {
	/** the table we are containing */
	private JTable m_table;

	/** the data model */
	private TableModel m_model;

	/** the scroll pane */
	private JScrollPane m_scroll;

	/**
	 * ctor
	 * 
	 * @param table
	 *            the table we are containing
	 * @param the
	 *            orginal data model (not the TableSorter if one is used )
	 */
	public BasicTablePanel(JScrollPane scroll, JTable table, TableModel dataModel) {
		m_scroll = scroll;
		m_table = table;
		m_model = dataModel;

		m_table.addMouseListener(new TablePopupMouseListener(m_table, this));
		setController(new BasicTableController(this));
		syncTableColumnCache();
	}

	/**
	 * Deselects any selected cells in the table
	 */
	public void deselect() {
		m_table.clearSelection();
	}

	/**
	 * @return the underlying data model. This is the original model, not the
	 *         TableSorter (if there is one )
	 */
	public TableModel getModel() {
		return m_model;
	}

	/**
	 * @return the scroll pane for the given table in the panel.
	 */
	public JScrollPane getScrollPane(JTable table) {
		return m_scroll;
	}

	/**
	 * @return a selection object that represents the selected cells in the
	 *         table *model*
	 */
	public TableSelection getSelection() {
		return SelectionBuilder.buildNormal(this);
	}

	/**
	 * @return the current table
	 */
	public JTable getTable() {
		return m_table;
	}

	/**
	 * @return the table 1
	 */
	JTable getTable1() {
		return m_table;
	}

	/**
	 * @return the table 2. This is null in our case because there is only one
	 *         table
	 */
	JTable getTable2() {
		return null;
	}

	/**
	 * @return the UIDirector for the panel
	 */
	public UIDirector getUIDirector() {
		return null;
	}

	/**
	 * Resets the column sizes based on the TableColumnInfo settings
	 */
	public void resizeColumns() {
		TableColumnModel colmodel = getTable1().getColumnModel();
		for (int index = 0; index < colmodel.getColumnCount(); index++) {
			TableColumn column = colmodel.getColumn(index);
			TableColumnInfo info = getColumnInfo(column.getModelIndex());
			if (info != null)
				column.setPreferredWidth(info.getWidth());
		}
	}

	/**
	 * This reads the column widths, settings, and column orders for table 1 and
	 * stores them in the table columns array
	 */
	public void saveTableSettings() {
		TableColumnModel colmodel = getTable1().getColumnModel();
		for (int index = 0; index < colmodel.getColumnCount(); index++) {
			TableColumn column = colmodel.getColumn(index);
			TableColumnInfo info = getColumnInfo(column.getModelIndex());
			assert (info != null);
			info.setWidth(column.getPreferredWidth());
		}
	}

	/**
	 * Select all cells in the given column.
	 */
	public void selectColumn(int colIndex) {
		selectColumn(m_table, colIndex);
	}

	/**
	 * Sets the table that currently has focus
	 */
	void setFocusTable(JTable table) {
		// no op
	}

}
