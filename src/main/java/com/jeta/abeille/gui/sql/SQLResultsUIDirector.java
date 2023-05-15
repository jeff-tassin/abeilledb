package com.jeta.abeille.gui.sql;

import com.jeta.abeille.gui.queryresults.QueryResultsModel;
import com.jeta.foundation.i18n.I18N;
import com.jeta.open.gui.framework.UIDirector;

import java.lang.ref.WeakReference;


/**
 * This class is used for updating the SQLResultsview toolbar/menus based on
 * the state of the model and view
 * 
 * @author Jeff Tassin
 */
public class SQLResultsUIDirector implements UIDirector {
	/** the view we are updating */
	private WeakReference<ResultsView> m_viewref;

	/**
	 * ctor
	 */
	public SQLResultsUIDirector(ResultsView view) {
		m_viewref = new WeakReference<ResultsView>(view);
	}

	/**
	 * Updates the UI
	 */
	public void updateComponents(java.util.EventObject evt) {
		ResultsView view = m_viewref.get();

		if (view == null)
			return;

		QueryResultsModel model = view.getModel();

		if (view.isSplitHorizontal()) {
			view.enableComponent(SQLResultsNames.ID_NO_SPLIT, true);
			view.enableComponent(SQLResultsNames.ID_SPLIT_VERTICAL, true);
			view.enableComponent(SQLResultsNames.ID_SPLIT_HORIZONTAL, false);
		} else if (view.isSplitVertical()) {
			view.enableComponent(SQLResultsNames.ID_NO_SPLIT, true);
			view.enableComponent(SQLResultsNames.ID_SPLIT_VERTICAL, false);
			view.enableComponent(SQLResultsNames.ID_SPLIT_HORIZONTAL, true);
		} else {
			view.enableComponent(SQLResultsNames.ID_NO_SPLIT, false);
			view.enableComponent(SQLResultsNames.ID_SPLIT_VERTICAL, true);
			view.enableComponent(SQLResultsNames.ID_SPLIT_HORIZONTAL, true);
		}

		if (model.getRowCount() == 0) {
			view.enableComponent(SQLResultsNames.ID_COPY, false);
			view.enableComponent(SQLResultsNames.ID_FIRST, false);
			view.enableComponent(SQLResultsNames.ID_LAST, false);
		} else {
			view.enableComponent(SQLResultsNames.ID_FIRST, true);
			if (model.isScrollable()) {
				view.enableComponent(SQLResultsNames.ID_LAST, true);
			} else {
				view.enableComponent(SQLResultsNames.ID_LAST, model.isRowCountKnown());
			}
		}

		if (model.isRowCountKnown()) {
			if (model.getRowCount() == 0) {
				view.setStatus(I18N.getLocalizedMessage("No Results"));
			} else {
				view.setStatus(I18N.format("Rows_1",model.getRowCount()));
			}
		} else {
			view.setStatus(I18N.format("Rows_plus_1", model.getRowCount()));
		}

		view.enableComponent(SQLResultsNames.ID_REDO_QUERY, true);
		view.enableComponent(SQLResultsNames.ID_SHOW_INSTANCE, true);
	}

}
