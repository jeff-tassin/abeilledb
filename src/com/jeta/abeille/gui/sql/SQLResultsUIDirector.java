package com.jeta.abeille.gui.sql;

import java.lang.ref.WeakReference;
import java.util.Collection;

import com.jeta.abeille.database.model.DbKey;

import com.jeta.abeille.gui.queryresults.QueryResultsModel;
import com.jeta.abeille.gui.queryresults.QueryResultsView;

import com.jeta.abeille.gui.update.InstanceFrame;

import com.jeta.foundation.gui.components.EnableEnum;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is used for updating the SQLResultsFrame toolbar/menus based on
 * the state of the model and view
 * 
 * @author Jeff Tassin
 */
public class SQLResultsUIDirector implements UIDirector {
	/** the frame we are updating */
	private WeakReference m_frameref;

	/**
	 * ctor
	 */
	public SQLResultsUIDirector(SQLResultsFrame frame) {
		m_frameref = new WeakReference(frame);
	}

	public SQLResultsFrame getFrame() {
		return (SQLResultsFrame) m_frameref.get();
	}

	/**
	 * @return the underlying view
	 */
	public ResultsView getCurrentView() {
		SQLResultsFrame frame = getFrame();
		if (frame != null)
			return frame.getCurrentView();
		else
			return null;
	}

	/**
	 * @return the underlying query results view
	 */
	public QueryResultsView getQueryResultsView() {
		ResultsView view = getCurrentView();
		if (view == null)
			return null;
		else
			return view.getView();
	}

	/**
	 * @return the underlying model
	 */
	public QueryResultsModel getModel() {
		SQLResultsFrame frame = getFrame();
		if (frame != null)
			return frame.getCurrentDataModel();
		else
			return null;
	}

	/**
	 * Updates the UI
	 */
	public void updateComponents(java.util.EventObject evt) {
		SQLResultsFrame frame = getFrame();

		if (frame == null)
			return;

		ResultsView rview = getCurrentView();
		if (rview != null) {
			QueryResultsView view = rview.getView();
			if (view.isSplitHorizontal()) {
				frame.enableComponent(SQLResultsNames.ID_NO_SPLIT, true);
				frame.enableComponent(SQLResultsNames.ID_SPLIT_VERTICAL, true);
				frame.enableComponent(SQLResultsNames.ID_SPLIT_HORIZONTAL, false);
			} else if (view.isSplitVertical()) {
				frame.enableComponent(SQLResultsNames.ID_NO_SPLIT, true);
				frame.enableComponent(SQLResultsNames.ID_SPLIT_VERTICAL, false);
				frame.enableComponent(SQLResultsNames.ID_SPLIT_HORIZONTAL, true);
			} else {
				frame.enableComponent(SQLResultsNames.ID_NO_SPLIT, false);
				frame.enableComponent(SQLResultsNames.ID_SPLIT_VERTICAL, true);
				frame.enableComponent(SQLResultsNames.ID_SPLIT_HORIZONTAL, true);
			}

			TSTablePanel tablepanel = view.getTablePanel();

			boolean bdelete = false;
			QueryResultsModel model = getModel();
			DbKey pk = model.getPrimaryKey();
			if (pk != null && pk.getColumnCount() > 0) {
				int[] rowheaders = tablepanel.getSelectedRowHeaders();
				if (rowheaders.length > 0) {
					bdelete = true;
				}
			}

			frame.enableComponent(SQLResultsNames.ID_DELETE_INSTANCE, bdelete);

			boolean btransact = false;
			Collection c = model.getInstancesMarkedForDeletion();
			if (c != null && c.size() > 0)
				btransact = true;

			frame.enableComponent(SQLResultsNames.ID_COMMIT, btransact);
			frame.enableComponent(SQLResultsNames.ID_ROLLBACK, btransact);

			if (model.getRowCount() == 0) {
				frame.enableComponent(SQLResultsNames.ID_COPY, false);
				frame.enableComponent(SQLResultsNames.ID_FIRST, false);
				frame.enableComponent(SQLResultsNames.ID_LAST, false);
			} else {
				frame.enableComponent(SQLResultsNames.ID_FIRST, true);
				if (model.isScrollable()) {
					frame.enableComponent(SQLResultsNames.ID_LAST, true);
				} else {
					frame.enableComponent(SQLResultsNames.ID_LAST, model.isRowCountKnown());
				}
			}

			if (model.isRowCountKnown()) {
				if (model.getRowCount() == 0) {
					rview.setStatus(I18N.getLocalizedMessage("No Results"));
				} else {
					rview.setStatus(I18N.format("Rows_1", new Integer(model.getRowCount())));
				}
			} else {
				rview.setStatus(I18N.format("Rows_plus_1", new Integer(model.getRowCount())));
			}

			boolean showinstance = true;
			Object launcher = frame.getLauncher();
			if (launcher instanceof InstanceFrame) {
				InstanceFrame iframe = (InstanceFrame) launcher;
				showinstance = iframe.isVisible();
				/**
				 * can't redo query because instance form uses prepared
				 * statements
				 */
				frame.enableComponent(SQLResultsNames.ID_REDO_QUERY, false);
			} else {
				frame.enableComponent(SQLResultsNames.ID_REDO_QUERY, !btransact);
			}

			frame.enableComponent(SQLResultsNames.ID_SHOW_INSTANCE, showinstance);

			if (frame.getDelegate() instanceof javax.swing.JFrame)
				frame.enableComponent(SQLResultsNames.ID_SHOW_IN_FRAME_WINDOW, false);

		} else {
			frame.enableAll(EnableEnum.ENABLE_ALL, false);
		}
	}
}
