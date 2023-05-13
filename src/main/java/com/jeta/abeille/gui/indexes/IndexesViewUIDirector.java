package com.jeta.abeille.gui.indexes;

import java.lang.ref.WeakReference;

import com.jeta.open.gui.framework.UIDirector;

/**
 * UIDirector for IndexesView
 * 
 * @author Jeff Tassin
 */
public class IndexesViewUIDirector implements UIDirector {
	private WeakReference m_viewref;

	public IndexesViewUIDirector(IndexesView view) {
		m_viewref = new WeakReference(view);
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		IndexesView view = (IndexesView) m_viewref.get();
		if (view != null) {
			TableIndex index = view.getSelectedIndex();
			if (index == null) {
				if (view.getModel().getTableId() == null) {
					view.enableComponent(IndexesView.ID_CREATE_INDEX, false);
					view.enableComponent(IndexesView.ID_REINDEX_TABLE, false);
				} else {
					view.enableComponent(IndexesView.ID_CREATE_INDEX, true);
					view.enableComponent(IndexesView.ID_REINDEX_TABLE, true);
				}

				view.enableComponent(IndexesView.ID_EDIT_INDEX, false);
				view.enableComponent(IndexesView.ID_DELETE_INDEX, false);
				view.enableComponent(IndexesView.ID_REINDEX_INDEX, false);
			} else {

				if (index.isPrimaryKey()) {
					view.enableComponent(IndexesView.ID_DELETE_INDEX, false);
				} else {
					view.enableComponent(IndexesView.ID_DELETE_INDEX, true);
				}

				view.enableComponent(IndexesView.ID_CREATE_INDEX, true);
				view.enableComponent(IndexesView.ID_REINDEX_TABLE, true);
				view.enableComponent(IndexesView.ID_EDIT_INDEX, true);
				view.enableComponent(IndexesView.ID_REINDEX_INDEX, true);
			}
		}
	}
}
