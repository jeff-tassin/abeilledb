package com.jeta.abeille.gui.modeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.gui.common.TableSelectorPanel;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.open.gui.framework.UIDirector;

/**
 * The controller class for the ForeignKeyView
 * 
 * @author Jeff Tassin
 */
public class ForeignKeyColumnAssignmentController extends TSController {
	/** the view we are controlling */
	private ForeignKeyColumnAssignmentView m_view;

	/**
	 * ctor
	 */
	public ForeignKeyColumnAssignmentController(ForeignKeyColumnAssignmentView view) {
		super(view);
		m_view = view;

		assignAction(ForeignKeyColumnAssignmentView.ID_ASSIGN_COLUMN, new AssignColumnAction());
		assignAction(ForeignKeyColumnAssignmentView.ID_REMOVE_COLUMN, new ClearColumnAction());

		UIDirector uidirector = new ForeignKeyColumnAssignmentViewUIDirector(view);
		view.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Assigns a column from the local table to the selected column in the
	 * reference table's primary key
	 */
	public class AssignColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ColumnMetaData cmd = m_view.getSelectedLocalColumn();
			ColumnMetaData pkcol = m_view.getSelectedPrimaryKeyColumn();
			if (cmd != null && pkcol != null) {
				m_view.assignColumn(cmd, pkcol);
			}
		}
	}

	/**
	 * Clears the selected assigned column from the reference table's primary
	 * key
	 */
	public class ClearColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ColumnMetaData pkcol = m_view.getSelectedPrimaryKeyColumn();
			if (pkcol != null) {
				m_view.clearAssignment(pkcol);
			}
		}
	}

	/**
	 * UIDirector for the ForeignKeyColumnAssignmentView. Updates/enables
	 * controls based on view/model state.
	 */
	public static class ForeignKeyColumnAssignmentViewUIDirector implements UIDirector {
		java.lang.ref.WeakReference m_viewref;

		/**
		 * ctor
		 */
		public ForeignKeyColumnAssignmentViewUIDirector(ForeignKeyColumnAssignmentView view) {
			m_viewref = new java.lang.ref.WeakReference(view);
		}

		/**
		 * UIDirector implementation
		 */
		public void updateComponents(java.util.EventObject evt) {
			ForeignKeyColumnAssignmentView view = (ForeignKeyColumnAssignmentView) m_viewref.get();
			if (view != null) {
				ColumnMetaData cmd = view.getSelectedLocalColumn();
				ColumnMetaData pkcol = view.getSelectedPrimaryKeyColumn();

				if (pkcol == null)
					view.enableComponent(ForeignKeyColumnAssignmentView.ID_REMOVE_COLUMN, false);
				else
					view.enableComponent(ForeignKeyColumnAssignmentView.ID_REMOVE_COLUMN, true);

				if (cmd != null && pkcol != null)
					view.enableComponent(ForeignKeyColumnAssignmentView.ID_ASSIGN_COLUMN, true);
				else
					view.enableComponent(ForeignKeyColumnAssignmentView.ID_ASSIGN_COLUMN, false);

			}
		}
	}
}
