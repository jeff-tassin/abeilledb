package com.jeta.abeille.gui.table.postgres;

import java.lang.ref.WeakReference;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.open.gui.framework.UIDirector;

/**
 * The UIDirector for the table frame class. This class is responsible for
 * enabling/disabling the controls on the TableView
 * 
 * @author Jeff Tassin
 */
public class TableViewUIDirector implements UIDirector {
	/**
	 * the weak reference to the frame. We don't need a hard ref here If the
	 * frame wants to go away, then fine.
	 */
	private WeakReference m_viewref;

	/**
	 * ctor
	 */
	public TableViewUIDirector(PostgresTableView frame) {
		m_viewref = new WeakReference(frame);
	}

	/**
	 * @return the frame window we are updating components for
	 */
	private PostgresTableView getView() {
		return (PostgresTableView) m_viewref.get();
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		/**
		 * the view has multiple sub-views in a tabbed pane. Here, we simply
		 * forward the call to the currently visible view (tab)
		 */
		PostgresTableView view = getView();
		if (view != null) {
			TSPanel subview = view.getCurrentView();
			subview.updateComponents();
		}
	}
}
