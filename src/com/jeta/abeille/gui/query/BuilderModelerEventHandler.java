package com.jeta.abeille.gui.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TableId;

import com.jeta.abeille.gui.model.LinkWidget;
import com.jeta.abeille.gui.model.ModelerEvent;
import com.jeta.abeille.gui.model.ModelerEventHandler;
import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewModel;
import com.jeta.abeille.gui.model.ViewGetter;

import com.jeta.foundation.utils.TSUtils;

/**
 * @author Jeff Tassin
 */
public class BuilderModelerEventHandler extends ModelerEventHandler {

	/**
	 * ctor
	 * 
	 * @param viewgetter
	 *            the object that gets the views so they can be notified when
	 *            ModelerEvent occur
	 * 
	 */
	public BuilderModelerEventHandler(ViewGetter viewgetter) {
		super(viewgetter);
	}

	/**
	 * ModelerModel Event handler
	 */
	public void eventFired(ModelerEvent evt) {

		TableId tableid = evt.getTableId();
		if (evt.getID() == ModelerEvent.TABLE_CHANGED || evt.getID() == ModelerEvent.TABLE_RENAMED
				|| evt.getID() == ModelerEvent.CATALOG_CHANGED) {

			LinkedList user_links = new LinkedList();
			ModelView view = getViewGetter().getModelView();
			if (view != null) {
				ModelViewModel model = view.getModel();
				LinkedList links = model.getLinkWidgets();
				Iterator iter = links.iterator();
				while (iter.hasNext()) {
					LinkWidget lw = (LinkWidget) iter.next();
					if (lw.isUserDefined()) {
						if (evt.getID() == ModelerEvent.CATALOG_CHANGED) {
							user_links.add(lw.getLink());
						} else {
							if (tableid.equals(lw.getSourceTableId()) || tableid.equals(lw.getDestinationTableId())) {
								user_links.add(lw.getLink());
							}
						}
					}
				}
			}
			super.eventFired(evt);
			Iterator iter = user_links.iterator();
			while (iter.hasNext()) {
				view.addLinkWidget((Link) iter.next());
			}
		} else {
			super.eventFired(evt);
		}
	}
}
