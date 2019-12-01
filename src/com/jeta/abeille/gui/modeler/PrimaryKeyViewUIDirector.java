package com.jeta.abeille.gui.modeler;

import com.jeta.open.gui.framework.UIDirector;

/**
 * UIDirector for the PrimaryKeyView
 */
public class PrimaryKeyViewUIDirector implements UIDirector {
	/** the view we are updating */
	private PrimaryKeyView m_view;

	/**
	 * ctor
	 */
	public PrimaryKeyViewUIDirector(PrimaryKeyView view) {
		m_view = view;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		m_view.enableComponent(PrimaryKeyView.ID_DELETE_PRIMARY_KEY, (m_view.getPrimaryKeyColumnsCount() > 0));
	}
}
