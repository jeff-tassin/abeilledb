package com.jeta.abeille.gui.modeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableMetaData;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.common.TableSelectorPanel;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.open.gui.framework.UIDirector;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

public class ForeignKeyViewController extends TSController implements UIDirector {
	/** the view we are controlling */
	private ForeignKeyView m_view;

	/**
	 * ctor
	 */
	public ForeignKeyViewController(ForeignKeyView view) {
		super(view);
		m_view = view;

		final TSComboBox cbox = (TSComboBox) m_view.getComponentByName(TableSelectorPanel.ID_TABLES_COMBO);
		cbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_view.updateReferenceTable();
			}
		});

		assignAction(ForeignKeyView.ID_EDIT_COLUMN_ASSIGNMENTS, new EditColumnAssignmentsAction());
		m_view.setUIDirector(this);
		updateComponents(null);
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		JCheckBox cbox = (JCheckBox) m_view.getComponentByName(ForeignKeyView.ID_DEFERRABLE_CHECK);
		JComponent ir = (JComponent) m_view.getComponentByName(ForeignKeyView.ID_INITIALLY_IMMEDIATE);
		JComponent id = (JComponent) m_view.getComponentByName(ForeignKeyView.ID_INITIALLY_DEFERRED);
		if (ir != null)
			ir.setEnabled(cbox.isSelected());

		if (id != null)
			id.setEnabled(cbox.isSelected());
	}

	/**
	 * Edits the column assignments for this foreign key
	 * 
	 */
	public class EditColumnAssignmentsAction implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
			dlg.setTitle(I18N.getLocalizedMessage("Edit Column Assignments"));

			TableSelectorModel selectormodel = m_view.getTableSelectorModel();
			// TableMetaData localtmd = selectormodel.getTable(
			// m_view.getLocalTableId() );
			TableMetaData reftmd = selectormodel.getTable(m_view.getReferenceTable());
			if (reftmd == null) {
				JOptionPane.showMessageDialog(null, I18N.getLocalizedMessage("Invalid Reference Table Name"),
						I18N.getLocalizedMessage("Error"), JOptionPane.ERROR_MESSAGE);
			} else if (!reftmd.hasPrimaryKey()) {
				JOptionPane.showMessageDialog(null,
						I18N.getLocalizedMessage("Reference table does not have a primary key"),
						I18N.getLocalizedMessage("Error"), JOptionPane.ERROR_MESSAGE);
			} else {
				ForeignKeyColumnAssignmentView view = new ForeignKeyColumnAssignmentView(m_view.getConnection(),
						m_view.getAssignments(), m_view.getLocalTableId(), m_view.getLocalColumns(), reftmd);
				dlg.setPrimaryPanel(view);
				TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
				dlg.showCenter();
				if (dlg.isOk()) {
					AssignedForeignKeyColumnsView assignview = m_view.getAssignmentView();
					assignview.setForeignKey(view.getAssignments(), m_view.getLocalColumns(), reftmd);
				}
			}
		}
	}
}
