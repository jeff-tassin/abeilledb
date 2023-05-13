package com.jeta.abeille.gui.query;

import java.awt.Component;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewNames;
import com.jeta.abeille.gui.model.ModelViewUIDirector;
import com.jeta.abeille.gui.model.TableWidget;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.open.gui.framework.UIDirector;

import com.jeta.foundation.documents.DocumentNames;

/**
 * Responsible for enabling/disabling UI components on the QueryBuilderFrame
 * 
 * @author Jeff Tassin
 */
public class QueryBuilderUIDirector extends ModelViewUIDirector {
	/** the frame window we are enabling/disabling components for */
	private QueryBuilderFrame m_frame;

	/** the controller for the frame */
	private QueryBuilderController m_controller;

	/**
	 * ctor
	 */
	public QueryBuilderUIDirector(QueryBuilderController controller) {
		super(controller.getFrame(), controller.getFrame());
		m_controller = controller;
		m_frame = controller.getFrame();
	}

	/**
	 * Updates the UI
	 */
	public void updateComponents(java.util.EventObject evt) {
		super.updateComponents(evt);
		if (!m_frame.hasModel()) {
			m_frame.enableComponent(DocumentNames.ID_OPEN, true);
			m_frame.enableComponent(DocumentNames.ID_CLOSE, false);
			m_frame.enableComponent(DocumentNames.ID_NEW, true);
			m_frame.enableComponent(DocumentNames.ID_SAVE, false);
			m_frame.enableComponent(DocumentNames.ID_SAVE_AS, false);
			m_frame.enableComponent(ModelViewNames.ID_PRINT, false);
			m_frame.enableComponent(ModelViewNames.ID_PRINT_PREVIEW, false);
			m_frame.enableComponent(ModelViewNames.ID_PAGE_SETUP, false);
			m_frame.enableComponent(ModelViewNames.ID_SAVE_AS_SVG, false);

			m_frame.enableComponent(QueryNames.ID_REMOVE_FROM_VIEW, false);
			m_frame.enableComponent(QueryNames.ID_UPDATE_TABLE, false);
			m_frame.enableComponent(QueryNames.ID_QUERY_TABLE, false);
			m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS, false);
			m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS_QUALIFIED, false);
			m_frame.enableComponent(QueryNames.ID_MOVE_UP, false);
			m_frame.enableComponent(QueryNames.ID_MOVE_DOWN, false);
			m_frame.enableComponent(QueryNames.ID_ADD_CONSTRAINT, false);
			m_frame.enableComponent(QueryNames.ID_REMOVE_CONSTRAINT, false);
			m_frame.enableComponent(QueryNames.ID_REMOVE_REPORTABLE, false);
			m_frame.enableComponent(QueryNames.ID_ADD_REPORTABLE, false);

			m_frame.enableComponent(QueryNames.ID_ADD_TO_VIEW, false);
			m_frame.enableComponent(QueryNames.ID_UPDATE_TABLE, false);
			m_frame.enableComponent(QueryNames.ID_QUERY_TABLE, false);
			m_frame.enableComponent(QueryNames.ID_SHOW_SQL, false);
			m_frame.enableComponent(QueryNames.ID_RUN_QUERY, false);
			m_frame.enableComponent(QueryNames.ID_RUN_QUERY_NEW_WINDOW, false);

			m_frame.enableComponent(TSComponentNames.ID_COPY, false);
			m_frame.enableComponent(TSComponentNames.ID_CUT, false);
			m_frame.enableComponent(TSComponentNames.ID_PASTE, false);
			m_frame.enableComponent(ModelViewNames.ID_SELECT_ALL, false);
			m_frame.enableComponent(ModelViewNames.ID_MOUSE_TOOL, false);
			m_frame.enableComponent(ModelViewNames.ID_LINK_TOOL, false);

			m_frame.enableComponent(QueryNames.ID_INCLUDE_TABLE, false);
			m_frame.enableComponent(QueryNames.ID_EXCLUDE_TABLE, false);

			m_frame.enableComponent(QueryNames.ID_OPTIONS, false);

		} else {
			m_frame.enableComponent(DocumentNames.ID_OPEN, true);
			m_frame.enableComponent(DocumentNames.ID_CLOSE, true);
			m_frame.enableComponent(DocumentNames.ID_NEW, true);
			m_frame.enableComponent(DocumentNames.ID_SAVE, true);
			m_frame.enableComponent(DocumentNames.ID_SAVE_AS, true);
			m_frame.enableComponent(ModelViewNames.ID_PRINT, true);
			m_frame.enableComponent(ModelViewNames.ID_PRINT_PREVIEW, true);
			m_frame.enableComponent(ModelViewNames.ID_PAGE_SETUP, true);
			m_frame.enableComponent(ModelViewNames.ID_SAVE_AS_SVG, true);
			m_frame.enableComponent(ModelViewNames.ID_SELECT_ALL, true);
			m_frame.enableComponent(ModelViewNames.ID_MOUSE_TOOL, true);
			m_frame.enableComponent(ModelViewNames.ID_LINK_TOOL, true);

			m_frame.enableComponent(QueryNames.ID_ADD_CONSTRAINT, true);
			m_frame.enableComponent(QueryNames.ID_ADD_REPORTABLE, true);
			m_frame.enableComponent(QueryNames.ID_OPTIONS, true);

			m_frame.enableComponent(QueryNames.ID_ADD_TO_VIEW, true);
			m_frame.enableComponent(QueryNames.ID_SHOW_SQL, true);
			m_frame.enableComponent(QueryNames.ID_RUN_QUERY, true);
			m_frame.enableComponent(QueryNames.ID_RUN_QUERY_NEW_WINDOW, true);
			m_frame.enableComponent(QueryNames.ID_INCLUDE_TABLE, true);
			m_frame.enableComponent(QueryNames.ID_EXCLUDE_TABLE, true);
			m_frame.enableComponent(TSComponentNames.ID_PASTE, true);

			ModelView modelview = m_frame.getModelView();
			Collection items = modelview.getSelectedItems();
			TableWidget widget = null;
			Iterator iter = items.iterator();
			if (iter.hasNext()) {
				Component comp = (Component) iter.next();
				if (comp instanceof TableWidget)
					widget = (TableWidget) comp;
			}

			if (items.size() == 0) {
				m_frame.enableComponent(QueryNames.ID_REMOVE_FROM_VIEW, false);
			} else {
				m_frame.enableComponent(QueryNames.ID_REMOVE_FROM_VIEW, true);
			}

			if (widget == null) {
				m_frame.enableComponent(QueryNames.ID_UPDATE_TABLE, false);
				m_frame.enableComponent(QueryNames.ID_QUERY_TABLE, false);
				m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS, false);
				m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS_QUALIFIED, false);
			} else {
				m_frame.enableComponent(QueryNames.ID_UPDATE_TABLE, true);
				m_frame.enableComponent(QueryNames.ID_QUERY_TABLE, true);
				m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS, true);
				m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS_QUALIFIED, true);
			}

			TSPanel activeview = m_controller.getActiveView();
			if (activeview == null) {
				m_frame.enableComponent(QueryNames.ID_MOVE_UP, false);
				m_frame.enableComponent(QueryNames.ID_MOVE_DOWN, false);
				m_frame.enableComponent(QueryNames.ID_REMOVE_CONSTRAINT, false);
				m_frame.enableComponent(QueryNames.ID_REMOVE_REPORTABLE, false);
			} else {
				int[] rows = null;
				if (activeview instanceof ConstraintView) {
					ConstraintView view = (ConstraintView) activeview;
					rows = view.getSelectedRows();
				} else if (activeview instanceof ReportablesView) {
					ReportablesView view = (ReportablesView) activeview;
					rows = view.getSelectedRows();
				}

				if (rows == null || rows.length == 0) {
					m_frame.enableComponent(QueryNames.ID_MOVE_UP, false);
					m_frame.enableComponent(QueryNames.ID_MOVE_DOWN, false);
					m_frame.enableComponent(QueryNames.ID_REMOVE_CONSTRAINT, false);
					m_frame.enableComponent(QueryNames.ID_REMOVE_REPORTABLE, false);

					activeview.enableComponent(TSComponentNames.ID_COPY, false);
					activeview.enableComponent(TSComponentNames.ID_CUT, false);
				} else if (rows.length == 1) {
					m_frame.enableComponent(QueryNames.ID_MOVE_UP, true);
					m_frame.enableComponent(QueryNames.ID_MOVE_DOWN, true);

					if (activeview instanceof ConstraintView) {
						m_frame.enableComponent(QueryNames.ID_REMOVE_CONSTRAINT, true);
						m_frame.enableComponent(QueryNames.ID_REMOVE_REPORTABLE, false);
					} else {
						m_frame.enableComponent(QueryNames.ID_REMOVE_CONSTRAINT, false);
						m_frame.enableComponent(QueryNames.ID_REMOVE_REPORTABLE, true);
					}

					activeview.enableComponent(TSComponentNames.ID_COPY, true);
					activeview.enableComponent(TSComponentNames.ID_CUT, true);

				} else {
					m_frame.enableComponent(QueryNames.ID_MOVE_UP, false);
					m_frame.enableComponent(QueryNames.ID_MOVE_DOWN, false);

					if (activeview instanceof ConstraintView) {
						m_frame.enableComponent(QueryNames.ID_REMOVE_CONSTRAINT, true);
						m_frame.enableComponent(QueryNames.ID_REMOVE_REPORTABLE, false);
					} else {
						m_frame.enableComponent(QueryNames.ID_REMOVE_CONSTRAINT, false);
						m_frame.enableComponent(QueryNames.ID_REMOVE_REPORTABLE, true);
					}

					activeview.enableComponent(TSComponentNames.ID_COPY, true);
					activeview.enableComponent(TSComponentNames.ID_CUT, true);
				}
			}
		}
	}

}
