package com.jeta.abeille.gui.model;

import com.jeta.abeille.database.model.TableId;

/**
 * Defines the listener methods for the ModelerModel events. The ModelerModel
 * fires events when the TableMetaData has changed in some way. e.g. table
 * changed or table renamed
 * 
 * @author Jeff Tassin
 */
public interface ModelerListener {
	public void eventFired(ModelerEvent evt);
}
