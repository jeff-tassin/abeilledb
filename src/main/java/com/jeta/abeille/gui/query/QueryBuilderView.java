package com.jeta.abeille.gui.query;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.gui.model.LinkUI;
import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewModel;
import com.jeta.abeille.gui.model.ModelViewNames;

import com.jeta.foundation.gui.components.BasicPopupMenu;

/**
 * This is the 'canvas' that graphically shows table properties and their
 * relationships to other tables in a given query
 * 
 * @author Jeff Tassin
 */
public class QueryBuilderView extends ModelView {

	/**
	 * ctor
	 */
	public QueryBuilderView(ModelViewModel model, LinkUI linkui) {
		super(model, linkui);

		BasicPopupMenu popup = getPopup();
		popup.removeItem(ModelViewNames.ID_COPY_AS_NEW);
	}

}
