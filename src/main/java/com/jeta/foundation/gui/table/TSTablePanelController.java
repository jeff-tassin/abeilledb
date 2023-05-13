/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.StringWriter;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;

import com.jeta.foundation.gui.table.export.DecoratorBuilder;
import com.jeta.foundation.gui.table.export.ExportModel;
import com.jeta.foundation.gui.table.export.ExportDecorator;
import com.jeta.foundation.gui.table.export.StandardExportBuilder;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the controller for the TSTablePanel component. It provides command
 * handling for the popup menu and some Swing component events
 * 
 * @author Jeff Tassin
 */
public class TSTablePanelController extends BasicTableController {
	/** The table panel we are controlling */
	private TSTablePanel m_tablepanel;

	/**
	 * ctor
	 */
	public TSTablePanelController(TSTablePanel panel) {
		super(panel);
		m_tablepanel = panel;

		assignAction(TSTableNames.ID_SPLIT_COLUMN, new SplitColumnAction());
		assignAction(TSTableNames.ID_HIDE_COLUMN, new HideColumnAction());
		assignAction(TSTableNames.ID_TABLE_OPTIONS, new ConfigureTableAction());
	}

	/**
	 * Invokes the configure table dialog that allows the user to configure that
	 * table columns
	 */
	public class ConfigureTableAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTable table = m_tablepanel.getFocusTable();
			if (table != null) {
				LinkedList cols = new LinkedList();
				TreeSet visible_cols = new TreeSet();
				TableColumnModel colmodel = table.getColumnModel();
				for (int index = 0; index < colmodel.getColumnCount(); index++) {
					TableColumn column = colmodel.getColumn(index);

					TableColumnInfo info = m_tablepanel.getColumnInfo(column);
					if (info != null) {
						cols.add(info);
						visible_cols.add(info);
						info.setVisible(true);
					}
				}

				Collection def_cols = m_tablepanel.getTableColumns();
				Iterator iter = def_cols.iterator();
				while (iter.hasNext()) {
					TableColumnInfo info = (TableColumnInfo) iter.next();
					if (!visible_cols.contains(info)) {
						cols.add(info);
						info.setVisible(false);
					}
				}

				TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_tablepanel, true);
				TableOptionsModel model = new TableOptionsModel(cols, m_tablepanel.getModel());
				TableOptionsView view = new TableOptionsView(model, m_tablepanel);

				TableOptionsController controller = new TableOptionsController(view);
				view.setController(controller);
				dlg.addValidator(controller);

				dlg.setTitle(I18N.getLocalizedMessage("Table Options"));
				dlg.setPrimaryPanel(view);
				dlg.setSize(dlg.getPreferredSize());
				TSGuiToolbox.calculateReasonableComponentSize(dlg.getSize());
				dlg.showCenter();
				if (dlg.isOk()) {
					JTable othertable = null;
					TableColumnModel othercolmodel = null;
					if (m_tablepanel.getViewMode() == TSTablePanel.SPLIT_HORIZONTAL) {
						// then we need to make changes to both top and bottom
						// tables
						othertable = m_tablepanel.getOtherTable(table);
						if (othertable != null) {
							othercolmodel = othertable.getColumnModel();
						}
					}

					// remove all columns from the table
					int total = colmodel.getColumnCount();
					for (int index = 0; index < total; index++) {
						TableColumn column = colmodel.getColumn(0);
						colmodel.removeColumn(column);
					}

					if (othercolmodel != null) {
						// remove all columns from the table
						total = othercolmodel.getColumnCount();
						for (int index = 0; index < total; index++) {
							TableColumn column = othercolmodel.getColumn(0);
							othercolmodel.removeColumn(column);
						}
					}

					// now add columns back from the options model
					for (int index = 0; index < model.getRowCount(); index++) {
						TableColumnInfo info = model.getColumnInfo(index);
						if (info != null && info.isVisible()) {
							colmodel.addColumn(info.getColumn());
							if (othercolmodel != null) {
								othercolmodel.addColumn(TSTablePanel.cloneColumn(info.getColumn()));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Splits the selected column. This will create a vertical split view. The
	 * selected column will be moved to the left split.
	 */
	public class SplitColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTable table = m_tablepanel.getFocusTable();
			if (m_tablepanel.getViewMode() == TSTablePanel.NORMAL
					|| m_tablepanel.getViewMode() == TSTablePanel.SPLIT_VERTICAL) {
				int[] cols = table.getSelectedColumns();
				m_tablepanel.splitColumns(cols, table);
			}
		}
	}

	/**
	 * Hides the selected column
	 */
	public class HideColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TablePopupMenu popup = m_tablepanel.getPopupMenu();
			int col = popup.getColumn();
			if (col >= 0) {
				m_tablepanel.hideColumn(col, m_tablepanel.getFocusTable());
			}
		}
	}

}
