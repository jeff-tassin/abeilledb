package com.jeta.abeille.gui.modeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

public class PrimaryKeyViewController extends TSController {
	private PrimaryKeyView m_view;

	public PrimaryKeyViewController(PrimaryKeyView view) {
		super(view);
		m_view = view;
		assignAction(PrimaryKeyView.ID_EDIT_PRIMARY_KEY, new EditPrimaryKeyAction());
		assignAction(PrimaryKeyView.ID_DELETE_PRIMARY_KEY, new DeletePrimaryKeyAction());
	}

	/**
	 * Action handler for deleting a primary key in a prototype table
	 */
	public class DeletePrimaryKeyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String msg = I18N.getLocalizedMessage("Delete Primary Key");
			int result = JOptionPane.showConfirmDialog(null, msg, I18N.getLocalizedMessage("Confirm"),
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				ColumnsGuiModel colmodel = m_view.getColumnsModel();
				Collection cols = colmodel.getColumns();
				Iterator iter = cols.iterator();
				while (iter.hasNext()) {
					ColumnInfo cinfo = (ColumnInfo) iter.next();
					cinfo.setPrimaryKey(false);
				}
				m_view.loadData(colmodel);
			}
		}
	}

	/**
	 * Action handler for editing a primary key in a prototype table
	 */
	public class EditPrimaryKeyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
			dlg.setTitle(I18N.getLocalizedMessage("Modify Primary Key"));
			PrimaryKeyAssignmentView assignview = new PrimaryKeyAssignmentView(m_view.getConnection(),
					m_view.getPrimaryKeyColumns(), m_view.getColumnsModel());

			dlg.setPrimaryPanel(assignview);
			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				HashMap assignments = DbUtils.createColumnMap(assignview.getAssignedColumns());
				ColumnsGuiModel colmodel = m_view.getColumnsModel();
				Collection cols = colmodel.getColumns();
				Iterator iter = cols.iterator();
				while (iter.hasNext()) {
					ColumnInfo cinfo = (ColumnInfo) iter.next();
					cinfo.setPrimaryKey(assignments.containsKey(cinfo.getColumnName()));
				}
				m_view.loadData(colmodel);
			}
		}
	}

}
