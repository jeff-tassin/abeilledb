package com.jeta.abeille.gui.model;

import java.util.Collection;

import com.jeta.abeille.database.model.TableMetaData;

import com.jeta.foundation.documents.DocumentNames;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.i18n.I18N;

/**
 * This class is responsible for updating the menu/toolbars on the
 * ModelViewFrame window.
 * 
 * @author Jeff Tassin
 */
public class ModelViewFrameUIDirector extends ModelViewUIDirector {
	/**
	 * The frame window we are updating
	 */
	private ModelViewFrame m_frame;

	/**
	 * ctor
	 */
	public ModelViewFrameUIDirector(ModelViewFrame frame) {
		super(frame, frame);
		m_frame = frame;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		super.updateComponents(evt);
		if (!m_frame.hasModel()) {
			m_frame.enableComponent(DocumentNames.ID_OPEN, true);
			m_frame.enableComponent(DocumentNames.ID_CLOSE, false);
			m_frame.enableComponent(DocumentNames.ID_NEW, true);
			m_frame.enableComponent(DocumentNames.ID_SAVE, false);
			m_frame.enableComponent(DocumentNames.ID_SAVE_AS, false);
			m_frame.enableComponent(ModelViewNames.ID_REMOVE_FROM_VIEW, false);
			m_frame.enableComponent(ModelViewNames.ID_UPDATE_TABLE, false);
			m_frame.enableComponent(ModelViewNames.ID_QUERY_TABLE, false);
			m_frame.enableComponent(ModelViewNames.ID_COMMIT_TABLES, false);
			m_frame.enableComponent(ModelViewNames.ID_DROP_TABLE, false);
			m_frame.enableComponent(ModelViewNames.ID_TABLE_PROPERTIES, false);
			m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS, false);
			m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS_QUALIFIED, false);
			m_frame.enableComponent(ModelViewNames.ID_TABLE_PROPERTIES, false);
			m_frame.enableComponent(ModelViewNames.ID_REMOVE_FROM_VIEW, false);
			m_frame.enableComponent(ModelViewNames.ID_REMOVE_VIEW, false);
			m_frame.enableComponent(ModelViewNames.ID_UPDATE_TABLE, false);
			m_frame.enableComponent(ModelViewNames.ID_QUERY_TABLE, false);
			m_frame.enableComponent(ModelViewNames.ID_DROP_TABLE, false);
			m_frame.enableComponent(ModelViewNames.ID_EXPORT_TABLE_DATA, false);
			m_frame.enableComponent(ModelViewNames.ID_SHOW_PROTOTYPES, false);
			m_frame.enableComponent(ModelViewNames.ID_HIDE_PROTOTYPES, false);

			m_frame.enableComponent(ModelViewNames.ID_PRINT, false);
			m_frame.enableComponent(ModelViewNames.ID_PRINT_PREVIEW, false);
			m_frame.enableComponent(ModelViewNames.ID_PAGE_SETUP, false);
			m_frame.enableComponent(ModelViewNames.ID_SAVE_AS_SVG, false);

			m_frame.enableComponent(TSComponentNames.ID_CUT, false);
			m_frame.enableComponent(TSComponentNames.ID_COPY, false);
			m_frame.enableComponent(TSComponentNames.ID_PASTE, false);
			m_frame.enableComponent(ModelViewNames.ID_SELECT_ALL, false);

			m_frame.enableComponent(ModelViewNames.ID_ADD_TO_VIEW, false);

			m_frame.enableComponent(ModelViewNames.ID_MOUSE_TOOL, false);
			m_frame.enableComponent(ModelViewNames.ID_LINK_TOOL, false);
			m_frame.enableComponent(ModelViewNames.ID_NEW_VIEW, false);
			m_frame.enableComponent(ModelViewNames.ID_CHANGE_VIEW_NAME, false);
			m_frame.enableComponent(ModelViewNames.ID_REMOVE_VIEW, false);

			m_frame.enableComponent(ModelViewNames.ID_CREATE_TABLE, false);

			m_frame.enableComponent(ModelViewNames.ID_INCREASE_FONT, false);
			m_frame.enableComponent(ModelViewNames.ID_DECREASE_FONT, false);
		} else {
			m_frame.enableComponent(DocumentNames.ID_OPEN, true);
			m_frame.enableComponent(DocumentNames.ID_NEW, (m_frame.getCurrentFile() != null));
			m_frame.enableComponent(DocumentNames.ID_SAVE, true);
			m_frame.enableComponent(DocumentNames.ID_SAVE_AS, true);
			m_frame.enableComponent(DocumentNames.ID_CLOSE, true);

			m_frame.enableComponent(ModelViewNames.ID_PRINT, true);
			m_frame.enableComponent(ModelViewNames.ID_PRINT_PREVIEW, true);
			m_frame.enableComponent(ModelViewNames.ID_PAGE_SETUP, true);
			m_frame.enableComponent(ModelViewNames.ID_SAVE_AS_SVG, true);
			m_frame.enableComponent(ModelViewNames.ID_ADD_TO_VIEW, true);

			m_frame.enableComponent(ModelViewNames.ID_MOUSE_TOOL, true);
			m_frame.enableComponent(ModelViewNames.ID_LINK_TOOL, true);
			m_frame.enableComponent(ModelViewNames.ID_NEW_VIEW, true);
			m_frame.enableComponent(ModelViewNames.ID_CHANGE_VIEW_NAME, true);
			m_frame.enableComponent(ModelViewNames.ID_REMOVE_VIEW, true);

			m_frame.enableComponent(ModelViewNames.ID_CREATE_TABLE, true);

			m_frame.enableComponent(ModelViewNames.ID_INCREASE_FONT, true);
			m_frame.enableComponent(ModelViewNames.ID_DECREASE_FONT, true);

			ModelView view = m_frame.getCurrentView();
			ModelViewModel model = view.getModel();

			TableWidget widget = view.getSelectedTableWidget();
			if (widget == null) {
				m_frame.enableComponent(ModelViewNames.ID_REMOVE_FROM_VIEW, false);
				m_frame.enableComponent(ModelViewNames.ID_UPDATE_TABLE, false);
				m_frame.enableComponent(ModelViewNames.ID_QUERY_TABLE, false);
				m_frame.enableComponent(ModelViewNames.ID_COMMIT_TABLES, false);
				m_frame.enableComponent(ModelViewNames.ID_DROP_TABLE, false);
				m_frame.enableComponent(ModelViewNames.ID_TABLE_PROPERTIES, false);
				m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS, false);
				m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS_QUALIFIED, false);
			} else {
				m_frame.enableComponent(ModelViewNames.ID_TABLE_PROPERTIES, true);

				m_frame.enableComponent(ModelViewNames.ID_REMOVE_FROM_VIEW, true);
				boolean removeview = (m_frame.getViewCount() > 1);
				m_frame.enableComponent(ModelViewNames.ID_REMOVE_VIEW, removeview);

				boolean commit = false;
				Collection c = view.getSelectedItems();
				if (c.size() == 1) {
					Object obj = c.iterator().next();
					if (obj instanceof TableWidget) {
						TableWidget selwidget = (TableWidget) obj;
						if (!selwidget.isSaved())
							commit = true;
					}
				}

				if (c.size() == 1) {
					m_frame.enableComponent(ModelViewNames.ID_UPDATE_TABLE, !commit);
					m_frame.enableComponent(ModelViewNames.ID_QUERY_TABLE, !commit);
					m_frame.enableComponent(ModelViewNames.ID_DROP_TABLE, !commit);
					m_frame.enableComponent(ModelViewNames.ID_EXPORT_TABLE_DATA, !commit);
				} else {
					m_frame.enableComponent(ModelViewNames.ID_DROP_TABLE, false);
					m_frame.enableComponent(ModelViewNames.ID_UPDATE_TABLE, false);
					m_frame.enableComponent(ModelViewNames.ID_QUERY_TABLE, false);
					m_frame.enableComponent(ModelViewNames.ID_EXPORT_TABLE_DATA, false);
				}

				m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS, c.size() >= 1);
				m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS_QUALIFIED, c.size() >= 1);
				m_frame.enableComponent(ModelViewNames.ID_COMMIT_TABLES, commit);

			}

			Integer count = new Integer(model.getTableWidgetCount());
			m_frame.setStatus(I18N.format("Tables_1", count));

			m_frame.enableComponent(ModelViewNames.ID_SHOW_PROTOTYPES, !m_frame.isPrototypesVisible());
			m_frame.enableComponent(ModelViewNames.ID_HIDE_PROTOTYPES, m_frame.isPrototypesVisible());
		}
	}

}
