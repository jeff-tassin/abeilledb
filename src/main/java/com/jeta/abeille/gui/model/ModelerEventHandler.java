package com.jeta.abeille.gui.model;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TableId;

import com.jeta.foundation.utils.TSUtils;

/**
 * This event handler for the ModelerModel is used with the
 * ModelView/Model/Controllers It gets model changes from the ModelerModel and
 * updates the ModelView/ModelViewModel objects accordingly. (Note that the
 * ModelerModel typcially gets database events from the DbModel as well as
 * explicit messages from clients).
 * 
 * @author Jeff Tassin
 */
public class ModelerEventHandler implements ModelerListener {

	/** the object that contains the ModelViews that we update when events occur */
	private ViewGetter m_viewgetter;

	/**
	 * ctor
	 * 
	 * @param viewgetter
	 *            the object that gets the views so they can be notified when
	 *            ModelerEvent occur
	 * 
	 */
	public ModelerEventHandler(ViewGetter viewgetter) {
		m_viewgetter = viewgetter;
	}

	/**
	 * ModelerModel Event handler
	 */
	public void eventFired(ModelerEvent evt) {
		TableId tableid = evt.getTableId();
		if (evt.getID() == ModelerEvent.TABLE_CHANGED) {
			// notify all views that a table's metadata has changed
			Collection views = m_viewgetter.getViews();
			Iterator iter = views.iterator();
			while (iter.hasNext()) {
				ModelView view = (ModelView) iter.next();
				ModelViewModel model = view.getModel();
				view.tableChanged(tableid);
			}
		} else if (evt.getID() == ModelerEvent.TABLE_RENAMED) {
			// notify all views that a table's metadata has changed and table
			// name changed
			Collection views = m_viewgetter.getViews();
			Iterator iter = views.iterator();
			while (iter.hasNext()) {
				ModelView view = (ModelView) iter.next();
				ModelViewModel model = view.getModel();
				view.tableRenamed((TableId) evt.getParameter(1), tableid);
			}
		} else if (evt.getID() == ModelerEvent.TABLE_DELETED) {
			// notify all views that the table is deleted
			Collection views = m_viewgetter.getViews();
			Iterator iter = views.iterator();
			while (iter.hasNext()) {
				ModelView view = (ModelView) iter.next();
				ModelViewModel model = view.getModel();
				model.removeWidget(model.getTableWidget(evt.getTableId()));
			}
		} else if (evt.getID() == ModelerEvent.LINK_CREATED) {
			// notify all views that the link was created
			Link link = (Link) evt.getParameter(0);
			Collection views = m_viewgetter.getViews();
			Iterator iter = views.iterator();
			while (iter.hasNext()) {
				ModelView view = (ModelView) iter.next();
				LinkWidget lw = view.addLinkWidget(link);
				if (lw != null)
					lw.recalc();

				view.repaint();
			}

		} else if (evt.getID() == ModelerEvent.LINK_DELETED) {
			// notify all views that the link was created
			Link link = (Link) evt.getParameter(0);
			Collection views = m_viewgetter.getViews();
			Iterator iter = views.iterator();
			while (iter.hasNext()) {
				ModelView view = (ModelView) iter.next();
				view.removeLinkWidget(link);
				view.repaint();
			}
		} else if (evt.getID() == ModelerEvent.CATALOG_CHANGED) {
			Catalog catalog = (Catalog) evt.getParameter(0);
			Collection views = m_viewgetter.getViews();
			Iterator iter = views.iterator();
			while (iter.hasNext()) {
				ModelView view = (ModelView) iter.next();
				view.catalogChanged(catalog);
				/*
				 * we need this because the link layouts need to be refreshed
				 * under certain conditions
				 */
				view.resetLinkTerminals();
			}
		}
	}

	/** the object that contains the ModelViews that we update when events occur */
	public ViewGetter getViewGetter() {
		return m_viewgetter;
	}

}
