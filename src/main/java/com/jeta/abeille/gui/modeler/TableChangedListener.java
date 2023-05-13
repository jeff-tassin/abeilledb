package com.jeta.abeille.gui.modeler;

import com.jeta.abeille.database.model.TableId;

/**
 * This interface is used by the modeler classes to update different parts of
 * the system when a table has changed. This is similar to the ModelListener in
 * DbModel, but the event does not get sent throughout the application. This
 * interface is mainly used for singular events between two components rather
 * than a system wide event.
 * 
 * @author Jeff Tassin
 */
public interface TableChangedListener {
	public void tableChanged(TableId tableId);
}
