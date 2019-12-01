package com.jeta.abeille.gui.update;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is repsonsible for updating the GUI component states
 * (enabled/disabled) for the InstanceOptionsView
 * 
 * @author Jeff Tassin
 */
public class InstanceOptionsUIDirector implements UIDirector {
	/** the view we are resposible for updating */
	private InstanceOptionsView m_view;

	/**
	 * ctor
	 */
	public InstanceOptionsUIDirector(InstanceOptionsView view) {
		m_view = view;
	}

	/**
	 * @return the view this object is responsible for updating
	 */
	protected InstanceOptionsView getView() {
		return m_view;
	}

	/**
	 * UIDirector implementation Updates the state of toolbar buttons
	 */
	public void updateComponents(java.util.EventObject evt) {
		try {
			int row = m_view.getSelectedRow();
			if (row < 0) {
				// nothing selected
				m_view.enableComponent(InstanceOptionsView.ID_EDIT_COLUMN, false);
				m_view.enableComponent(InstanceOptionsView.ID_MOVE_UP, false);
				m_view.enableComponent(InstanceOptionsView.ID_MOVE_DOWN, false);
			} else {
				m_view.enableComponent(InstanceOptionsView.ID_EDIT_COLUMN, true);
				m_view.enableComponent(InstanceOptionsView.ID_MOVE_UP, true);
				m_view.enableComponent(InstanceOptionsView.ID_MOVE_DOWN, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
