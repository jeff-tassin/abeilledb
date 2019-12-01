package com.jeta.abeille.gui.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.abeille.database.model.ColumnMetaData;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.open.gui.framework.UIDirector;

/**
 * Controller for the ColumnAssignmentPanel
 * 
 * @author Jeff Tassin
 */
public class ColumnAssignmentPanelController extends TSController {
	/**
	 * The view we are controlling
	 */
	private ColumnAssignmentPanel m_view;

	/**
	 * ctor
	 */
	public ColumnAssignmentPanelController(ColumnAssignmentPanel panel) {
		super(panel);
		m_view = panel;

		assignAction(ColumnAssignmentPanel.ID_ASSIGN_COLUMN, new AssignColumnAction());
		assignAction(ColumnAssignmentPanel.ID_REMOVE_COLUMN, new RemoveAction());
	}

	/**
	 * Adds a column to the assigned list
	 */
	public class AssignColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ColumnMetaData cmd = (ColumnMetaData) m_view.getSelectedSourceObject();
			if (cmd != null) {
				m_view.assignColumn(cmd);
			}
		}
	}

	/**
	 * Removes the selected column from the assigned list
	 */
	public class RemoveAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ColumnMetaData cmd = (ColumnMetaData) m_view.getSelectedAssignedObject();
			if (cmd != null) {
				m_view.clearAssignment(cmd);
			}
		}
	}

}
