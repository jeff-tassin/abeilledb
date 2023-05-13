package com.jeta.abeille.gui.checks.postgres;

import java.lang.ref.WeakReference;

import com.jeta.open.gui.framework.UIDirector;

/**
 * The UIDirector for the ChecksView. This class is responsible for
 * enabling/disabling the controls on the ChecksView
 * 
 * @author Jeff Tassin
 */
public class ChecksViewUIDirector implements UIDirector {
	/**
	 * the weak reference to the frame. We don't need a hard ref here If the
	 * frame wants to go away, then fine.
	 */
	private WeakReference m_viewref;

	/**
	 * ctor
	 */
	public ChecksViewUIDirector(ChecksView view) {
		m_viewref = new WeakReference(view);
	}

	/**
	 * @return the frame window we are updating components for
	 */
	private ChecksView getView() {
		return (ChecksView) m_viewref.get();
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		ChecksView view = getView();
		if (view != null) {
			if (view.getModel().getTableId() == null) {
				view.enableComponent(ChecksView.ID_CREATE_CHECK, false);
				view.enableComponent(ChecksView.ID_EDIT_CHECK, false);
				view.enableComponent(ChecksView.ID_DELETE_CHECK, false);
			} else {
				view.enableComponent(ChecksView.ID_CREATE_CHECK, true);

				CheckConstraint cc = view.getSelectedCheck();
				boolean enable = (cc != null);

				view.enableComponent(ChecksView.ID_EDIT_CHECK, enable);
				view.enableComponent(ChecksView.ID_DELETE_CHECK, enable);
			}
		}
	}
}
