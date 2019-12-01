/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.TransferHandler;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;

import com.jeta.foundation.gui.table.export.ExportModel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the basic controller for a table in our application. It provides
 * command handling for the popup menu and some Swing component events (e.g.
 * cut, copy, paste, column select )
 * 
 * @author Jeff Tassin
 */
public class BasicTableController extends TSController {
	/**
	 * An adapter for managing a table that we are performing operations against
	 */
	private AbstractTablePanel m_tablepanel;

	/**
	 * ctor
	 */
	public BasicTableController(AbstractTablePanel tablePanel) {
		super(tablePanel);
		m_tablepanel = tablePanel;

		assignAction(TSTableNames.ID_DESELECT, new DeselectAction());
		assignAction(TSTableNames.ID_SELECT_COLUMN, new SelectColumnAction());
		assignAction(TSComponentNames.ID_COPY, new CopyAction());
		assignAction(TSComponentNames.ID_COPY_SPECIAL, new CopySpecialAction());
	}

	/**
	 * Helper method that copies the selected items in the table to the
	 * clipboard
	 * 
	 * @param bSpecial
	 *            if set to true, then this method acts as a Copy-Special
	 *            command and invokes a copy options dialog. If this parameter
	 *            is false, no dialog is launched, and the default Export
	 *            options are used.
	 * @param tableSelection
	 *            a table model to copy
	 */
	public static void copy(boolean bSpecial, TableSelection selection, Component sourceComp) {
		int rows = selection.getRowCount();
		int[] cols = selection.getColumnSpan();

		if (rows >= 1 && cols.length >= 1) {
			ExportModel exportmodel = null;

			if (bSpecial) {
				if (sourceComp instanceof TSTablePanel)
					sourceComp = ((TSTablePanel) sourceComp).getTable();

				// if bSpecial is set, then this is a copy-special command. So,
				// launch
				// the copy options dialog
				TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, sourceComp, true);
				CopyListOptionsPanel panel = new CopyListOptionsPanel();
				dlg.setTitle(I18N.getLocalizedMessage("Copy Special"));
				dlg.setPrimaryPanel(panel);
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
				if (dlg.isOk()) {
					exportmodel = panel.getModel(selection);
				}
			} else {
				CopyListOptionsPanel panel = new CopyListOptionsPanel();
				exportmodel = panel.getModel(selection);
			}

			TableUtils.copyToClipboard(exportmodel, selection);
		}
	}

	/**
	 * Helper method that copies the selected items in the table to the
	 * clipboard
	 * 
	 * @param bSpecial
	 *            if set to true, then this method acts as a Copy-Special
	 *            command and invokes a copy options dialog. If this parameter
	 *            is false, no dialog is launched, and the default Export
	 *            options are used.
	 */
	void copy(boolean bSpecial) {
		assert (m_tablepanel != null);
		copy(bSpecial, m_tablepanel.getSelection(), m_tablepanel);
	}

	/**
	 * Copies the selected cells to the clip board object. The copy operation
	 * dictated by the default settings in the export model
	 */
	public class CopyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			copy(false);
		}
	}

	/**
	 * Invokes the copy special dialog that allows the user to control how table
	 * values are copied to clipboard
	 */
	public class CopySpecialAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			copy(true);
		}
	}

	/**
	 * Deselects any selected cells in the focused table.
	 */
	public class DeselectAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_tablepanel.deselect();
		}
	}

	/**
	 * Selects the column in the focus table. If the panel is split
	 * horizontally, then both tables are selected.
	 */
	public class SelectColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TablePopupMenu popup = m_tablepanel.getPopupMenu();
			m_tablepanel.selectColumn(popup.getColumn());
		}
	}

}
