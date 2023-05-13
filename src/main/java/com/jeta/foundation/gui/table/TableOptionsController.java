/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * Controller for TableOptionsView
 * 
 * @author Jeff Tassin
 */
public class TableOptionsController extends TSController implements JETARule {
	/** the view */
	private TableOptionsView m_view;

	/**
	 * ctor
	 */
	public TableOptionsController(TableOptionsView view) {
		super(view);
		m_view = view;
		assignAction(TableOptionsView.ID_MOVE_DOWN, new MoveDownAction());
		assignAction(TableOptionsView.ID_MOVE_UP, new MoveUpAction());
	}

	/**
	 * Override TSController.check if you want to provide a validation in your
	 * controller. Here we check if the user has set all columns to invisible
	 * 
	 * @return an error message if the validation failed. Return null if the
	 *         validation succeeded
	 */
	public RuleResult check(Object[] params) {
		RuleResult result = RuleResult.SUCCESS;
		String msg = null;
		TSTablePanel tpanel = m_view.getTablePanel();
		int mode = tpanel.getViewMode();
		if (mode == TSTablePanel.SPLIT_HORIZONTAL || mode == TSTablePanel.NORMAL) {
			boolean bvalid = false;
			TableOptionsModel model = m_view.getModel();
			for (int index = 0; index < model.getRowCount(); index++) {
				TableColumnInfo info = model.getColumnInfo(index);
				if (info.isVisible()) {
					bvalid = true;
					break;
				}

			}

			if (!bvalid) {
				result = new RuleResult(I18N.getLocalizedMessage("Error_Table_must_have_a_visible_col"));
			}
		}

		return result;
	}

	/**
	 * Moves the column down in the order
	 */
	public class MoveDownAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TableOptionsModel model = m_view.getModel();
			int index = m_view.getSelectedRow();
			if (index >= 0 && index < (model.getRowCount() - 1)) {
				TableColumnInfo info = model.getRow(index);
				model.reorder(index + 1, index);
				index++;
				JTable table = m_view.getTable();
				table.setRowSelectionInterval(index, index);
				table.setColumnSelectionInterval(0, 0);
				table.repaint();
			}
		}
	}

	/**
	 * Moves the column up in the order
	 */
	public class MoveUpAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TableOptionsModel model = m_view.getModel();
			int index = m_view.getSelectedRow();
			if (index > 0) {
				TableColumnInfo info = model.getRow(index);
				model.reorder(index - 1, index);
				index--;
				JTable table = m_view.getTable();
				table.setRowSelectionInterval(index, index);
				table.setColumnSelectionInterval(0, 0);
				table.repaint();
			}
		}
	}

}
