/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.StringWriter;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumn;

import com.jeta.foundation.gui.components.TSComponentFinder;

import com.jeta.foundation.gui.table.export.DecoratorBuilder;
import com.jeta.foundation.gui.table.export.ExportModel;
import com.jeta.foundation.gui.table.export.ExportDecorator;
import com.jeta.foundation.gui.table.export.StandardExportBuilder;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class contains utility methods for doing common tasks with JTables
 * 
 * @author Jeff Tassin
 */
public class TableUtils {
	/**
	 * Clears the column header icons for a given table
	 */
	public static void clearColumnHeaders(JTable table) {
		if (table == null)
			return;

		TableColumnModel columnmodel = table.getColumnModel();
		// reset all other column sort modes to NONE at this point
		int count = columnmodel.getColumnCount();

		for (int index = 0; index < count; index++) {
			TableColumn tc = columnmodel.getColumn(index);
			Object robj = tc.getHeaderRenderer();
			if (robj instanceof SortedColumnHeaderRenderer)
				((SortedColumnHeaderRenderer) robj).setSortMode(SortMode.NONE);
		}
		JTableHeader th = table.getTableHeader();
		th.repaint();
	}

	/**
	 * This method searches the data in the given column of the given table to
	 * see if it contains the given object.
	 */
	public static boolean contains(JTable table, Object obj, int col) {
		if (obj == null)
			return false;

		for (int row = 0; row < table.getRowCount(); row++) {
			if (obj.equals(table.getValueAt(row, col)))
				return true;
		}
		return false;
	}

	/**
	 * Copies the export model and selection parameters to the clipboard for the
	 * given table.
	 */
	public static void copyToClipboard(ExportModel exportmodel, TableSelection selection) {
		if (exportmodel != null) {
			StringWriter writer = new StringWriter();
			DecoratorBuilder decbuilder = new StandardExportBuilder(-1);
			ExportDecorator decorator = decbuilder.build(exportmodel);
			try {
				decorator.write(writer, selection, 0, 0);
				TSGuiToolbox.copyToClipboard(writer.toString());
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}
	}

	/**
	 * Converts a table row the the corresponding model index. This is needed
	 * when the table is sorted. If the table is not sorted, the table index
	 * will equal the model index
	 * 
	 * @param table
	 *            the table whose index to convert
	 * @param index
	 *            the table index to convert
	 * @return the corresponding model index
	 */
	public static int convertTableToModelIndex(JTable table, int index) {
		if (index >= 0 && (table.getModel() instanceof TableSorter)) {
			TableSorter sorter = (TableSorter) table.getModel();
			return sorter.getModelRow(index);
		} else
			return index;
	}

	/**
	 * Creates a non-sortable table with the given model. Adds the necessary
	 * event handlers and controller to the table.
	 * 
	 * @param model
	 *            the table model for the resulting table
	 * @param sortable
	 *            flag that indicates if the table columns should be sortable
	 *            (by clicking the column headers).
	 * @deprecated use createBasicTablePanel instead
	 */
	public static TSTablePanel createSimpleTable(TableModel model, boolean sortable) {
		TSTablePanel result = new TSTablePanel(model, sortable);
		JTable table = result.getTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		try {
			TablePopupMenu popup = result.getPopupMenu();

			for (int index = 0; index < popup.getComponentCount(); index++) {
				Component comp = popup.getComponent(index);
				String name = comp.getName();
				if (comp instanceof JPopupMenu.Separator) {
					popup.remove(index);
					index--;
				} else if (TSTableNames.ID_SPLIT_COLUMN.equals(name)) {
					popup.remove(index);
					index--;
				} else if (TSTableNames.ID_HIDE_COLUMN.equals(name)) {
					popup.remove(index);
					index--;
				} else if (TSTableNames.ID_TABLE_OPTIONS.equals(name)) {
					popup.remove(index);
					index--;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Creates a sortable table with the given column model. The column model
	 * must correspond to the given table model
	 * 
	 * @deprecated use createBasicTablePanel instead
	 */
	public static JTable createSortableTable(TableModel model, TableColumnModel colmodel) {
		TableSorter sorter = new TableSorter(model);
		JTable table = null;
		if (colmodel == null)
			table = new JTable(sorter);
		else
			table = new JTable(sorter, colmodel);

		initializeTableSorter(sorter, table, null);
		return table;

	}

	/**
	 * Creates a table that can be sorted.
	 * 
	 * @deprecated use createBasicTablePanel instead
	 */
	public static JTable createSortableTable(TableModel model) {
		return createSortableTable(model, null);
	}

	/**
	 * Creates a panel that contains a table that can be sorted can be sorted.
	 * The table is scrollable and has automatically resized subsequent columns
	 * 
	 * @param model
	 *            the data model for the table. Don't send in a TableSorter
	 *            here.
	 * @param sortable
	 *            if true, then we will return a table that has sortable
	 *            columns. This will wrap the tablemodel with a TableSorter.
	 * @return a container that contains the table.
	 */
	public static AbstractTablePanel createBasicTablePanel(TableModel model, boolean sortable) {
		JTable table = null;
		if (sortable) {
			TableSorter sorter = new TableSorter(model);
			table = new JTable(sorter);
			initializeTableSorter(sorter, table, null);
		} else {
			table = new JTable(model);
		}

		table.setCellSelectionEnabled(true);
		JScrollPane scroll = new JScrollPane(table);

		BasicTablePanel panel = new BasicTablePanel(scroll, table, model);
		panel.setLayout(new BorderLayout());
		panel.add(scroll, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * If the parent of the given table is a JViewport, then we make sure the
	 * viewport is over the given row.
	 */
	public static void ensureRowIsVisible(JTable table, int row) {
		Container p = table.getParent();
		if (p instanceof JViewport) {
			Rectangle cellrect = table.getCellRect(row, 0, true);

			JViewport vp = (JViewport) p;
			Rectangle viewrect = vp.getViewRect();

			int view_y = viewrect.y;
			if (cellrect.y < viewrect.y) {
				view_y = cellrect.y;
			} else if (cellrect.y > (viewrect.y + viewrect.height - cellrect.height)) {
				view_y = cellrect.y + cellrect.height - viewrect.height;
			} else {
				return; // the cell is already visible
			}

			if (view_y < 0)
				view_y = 0;

			vp.setViewPosition(new Point(viewrect.x, view_y));
		}
	}

	/**
	 * @return a object that stores the table settings for a TSTablePanel. This
	 *         is used mainly to persist table settings between program
	 *         invocations.
	 */
	public static TableSettings getTableSettings(AbstractTablePanel tablepanel) {
		tablepanel.saveTableSettings();
		Collection cols = tablepanel.getTableColumns();
		return new TableSettings(cols);
	}

	/**
	 * Sets the column width of a table to a given number of characters using
	 * the current table font.
	 * 
	 * @param table
	 *            the table whose column we wish to set
	 * @param colIndex
	 *            the index of the column to set
	 * @param widthChars
	 *            the width in characters to set the column
	 */
	public static void setColumnWidth(JTable table, int colIndex, int widthChars) {
		TableColumn column = table.getColumnModel().getColumn(colIndex);
		assert (column != null);
		if (column != null) {
			int width = TSGuiToolbox.calculateAverageTextWidth(table, widthChars);
			column.setPreferredWidth(width);
		}
	}

	/**
	 * Sets the column widths of a JTable based on the column heading width. The
	 * table must have been initialized and column headings set.
	 * 
	 * @param table
	 *            the table whose column widths we wish to set
	 */
	public static void setColumnWidths(JTable table) {
		TableModel model = table.getModel();
		int cols = model.getColumnCount();
		for (int index = 0; index < cols; index++) {
			Font f = table.getFont();
			FontMetrics fm = table.getFontMetrics(f);

			String coltitle = model.getColumnName(index);
			coltitle += "###";
			int colwidth = fm.getStringBounds(coltitle, table.getGraphics()).getBounds().width;
			TableColumnModel cmodel = table.getColumnModel();
			cmodel.getColumn(index).setPreferredWidth(colwidth);
			cmodel.getColumn(index).setMinWidth(colwidth);
		}
	}

	/**
	 * Method to determine if a given table has a visible row header
	 */
	public static boolean isRowHeaderVisible(JTable table) {
		Container p = table.getParent();
		if (p instanceof JViewport) {
			Container gp = p.getParent();
			if (gp instanceof JScrollPane) {
				JScrollPane scrollPane = (JScrollPane) gp;
				JViewport rowHeaderViewPort = scrollPane.getRowHeader();
				if (rowHeaderViewPort != null)
					return rowHeaderViewPort.getView() != null;
			}
		}
		return false;
	}

	/**
	 * Intializes a table and a table sorter
	 */
	public static void initializeTableSorter(TableSorter sorter, JTable table, TSTablePanel tablePanel) {
		sorter.addMouseListenerToHeaderInTable(table, tablePanel);

		// set the renderer for each column
		TableColumnModel columnmodel = table.getColumnModel();
		int count = columnmodel.getColumnCount();
		for (int index = 0; index < count; index++) {
			TableColumn column = columnmodel.getColumn(index);
			column.setHeaderRenderer(new SortedColumnHeaderRenderer());
		}
	}

	/**
	 * * Creates row header for table with row number (starting with 1)
	 * displayed
	 */
	public static void removeRowHeader(JTable table) {
		Container p = table.getParent();
		if (p instanceof JViewport) {
			Container gp = p.getParent();
			if (gp instanceof JScrollPane) {
				JScrollPane scrollPane = (JScrollPane) gp;
				scrollPane.setRowHeader(null);
			}
		}
	}

	/**
	 * Restores the table settings for a given table panel and a given table
	 * settings object. The table panel must already be initialized.
	 * Furthermore, the columns in the table panel must match those in the
	 * settings (i.e. column names and indices are the same between the two). If
	 * both are consistent, then this method will iterate over the columns and
	 * set the column widths in the tabelpanel to those found in the settings.
	 * 
	 * @return true if the table settings were successfully restored. false is
	 *         returned if the settings and table panel are inconsistent.
	 */
	public static boolean restoreTableSettings(AbstractTablePanel tablepanel, TableSettings settings) {
		if (tablepanel == null || settings == null)
			return false;

		boolean bresult = true;
		Collection cols = tablepanel.getTableColumns();
		Collection stored = settings.getTableColumns();
		if (cols.size() == stored.size()) {
			Iterator citer = cols.iterator();
			Iterator siter = stored.iterator();
			while (citer.hasNext()) {
				TableColumnInfo cinfo = (TableColumnInfo) citer.next();
				TableColumnInfo sinfo = (TableColumnInfo) siter.next();
				String colname = cinfo.getName();
				if (colname == null || !colname.equals(sinfo.getName())) {
					bresult = false;
					break;
				}
			}

			if (bresult) {
				// okay, the settings are consistent with the panel, so let's go
				// back
				// and set the column widths
				citer = cols.iterator();
				siter = stored.iterator();
				while (citer.hasNext()) {
					TableColumnInfo cinfo = (TableColumnInfo) citer.next();
					TableColumnInfo sinfo = (TableColumnInfo) siter.next();

					int width = sinfo.getWidth();
					if (width < 10)
						width = 10;
					else if (width > 1024)
						width = 1024;

					cinfo.setWidth(width);
				}

				tablepanel.resizeColumns();
			}
		}
		return bresult;
	}

	/**
	 * This is a helper method that allows you to easily display an icon in a
	 * table cell. It assumes that the table cell has a String.class as the
	 * value.
	 */
	public static void setIconDecorator(JTable table, int colIndex, ImageIcon image) {
		TableColumn tcol = table.getColumnModel().getColumn(colIndex);
		tcol.setCellRenderer(new IconDecoratorRenderer(image));
	}

	/** Creates row header for table with row number (starting with 1) displayed */
	public static TableRowHeader setRowHeader(JTable table) {
		return setRowHeader(table, -1);
	}

	/**
	 * Creats a row header for the given table. The row number is displayed to
	 * the left of the table ( starting with row 1).
	 * 
	 * @param table
	 *            the table to create the row header for
	 * @param headerWidth
	 *            the number of characters to size the header
	 */
	public static TableRowHeader setRowHeader(JTable table, int headerWidth) {
		boolean isok = false;

		TableRowHeader result = null;
		Container p = table.getParent();
		if (p instanceof JViewport) {
			Container gp = p.getParent();
			if (gp instanceof JScrollPane) {
				JScrollPane scrollPane = (JScrollPane) gp;
				result = new TableRowHeader(table);
				scrollPane.setRowHeaderView(result);
				isok = true;
			}
		}
		assert (isok);
		return result;
	}

	/**
	 * Stops any editing for a given cell on a table.
	 */
	public static void stopEditing(JTable table) {
		if (table == null) {
			assert (false);
			return;
		}

		if (table.isEditing()) {
			int row = table.getEditingColumn();
			int col = table.getEditingRow();
			if (row >= 0 && col >= 0) {
				javax.swing.CellEditor editor = table.getCellEditor(row, col);
				editor.stopCellEditing();
			}
		}
	}
}
