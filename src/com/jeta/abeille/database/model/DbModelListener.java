package com.jeta.abeille.database.model;

/**
 * Defines the listener methods for the DbModel events. The DbModel fires events
 * when the database metadata changes in some way. e.g. table dropped or changed
 * 
 * @author Jeff Tassin
 */
public interface DbModelListener {
	public void eventFired(DbModelEvent evt);
}
