package com.jeta.abeille.gui.model;

import com.jeta.abeille.database.model.TableId;

/**
 * Listener for events from the ModelViewModel
 * 
 * @author Jeff Tassin
 */
public interface ModelViewModelListener {
	public void eventFired(ModelViewModelEvent evt);
}
