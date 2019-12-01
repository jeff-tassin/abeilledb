package com.jeta.abeille.gui.formbuilder;

import com.jeta.abeille.gui.update.InstanceOptionsView;

import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is repsonsible for updating the GUI component states
 * (enabled/disabled) for the ColumnsView
 * 
 * @author Jeff Tassin
 */
public class ColumnsViewUIDirector implements UIDirector {
	private JETAContainer m_frame;
	private ColumnsView m_view;

	/**
	 * ctor
	 */
	public ColumnsViewUIDirector(ColumnsView view, JETAContainer frame) {
		m_frame = frame;
		m_view = view;
	}

	/**
	 * UIDirector implementation Updates the state of toolbar buttons
	 */
	public void updateComponents(java.util.EventObject evt) {
		try {
			int row = m_view.getSelectedRow();
			if (row < 0) {
				if (m_frame != null) {
					m_frame.enableComponent(FormNames.ID_REMOVE_COLUMN, false);
					m_frame.enableComponent(InstanceOptionsView.ID_EDIT_COLUMN, false);
					m_frame.enableComponent(InstanceOptionsView.ID_MOVE_UP, false);
					m_frame.enableComponent(InstanceOptionsView.ID_MOVE_DOWN, false);
				}
			} else {
				if (m_frame != null) {
					m_frame.enableComponent(FormNames.ID_REMOVE_COLUMN, true);
					m_frame.enableComponent(InstanceOptionsView.ID_EDIT_COLUMN, true);
					m_frame.enableComponent(InstanceOptionsView.ID_MOVE_UP, true);
					m_frame.enableComponent(InstanceOptionsView.ID_MOVE_DOWN, true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
