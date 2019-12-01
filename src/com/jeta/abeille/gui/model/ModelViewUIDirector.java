package com.jeta.abeille.gui.model;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.foundation.gui.components.TSComponentNames;

import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.UIDirector;

/**
 * Responsible for updating the menu items related to the ModelView (e.g. cut,
 * copy, paste )
 * 
 * @author Jeff Tassin
 */
public class ModelViewUIDirector implements UIDirector {
	private JETAContainer m_frame;

	private ViewGetter m_getter;

	/**
	 * ctor
	 * 
	 * @param frame
	 *            the window container for the model view (e.g. frame, dialog,
	 *            etc) The frame also has components that need updating based on
	 *            the view logic
	 * @param getter
	 *            the view getter
	 */
	public ModelViewUIDirector(JETAContainer frame, ViewGetter getter) {
		m_frame = frame;
		m_getter = getter;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		ModelView view = m_getter.getModelView();
		if (view != null) {
			Collection items = view.getSelectedItems();
			Iterator iter = items.iterator();
			boolean bcut = (items.size() > 0);
			boolean bcopy = (items.size() > 0);

			m_frame.enableComponent(TSComponentNames.ID_CUT, bcut);
			m_frame.enableComponent(TSComponentNames.ID_COPY, bcopy);
			view.enableComponent(TSComponentNames.ID_CUT, bcut);
			view.enableComponent(TSComponentNames.ID_COPY, bcopy);
		}
	}
}
