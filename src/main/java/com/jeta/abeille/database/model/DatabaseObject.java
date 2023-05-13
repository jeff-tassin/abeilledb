package com.jeta.abeille.database.model;

/**
 * Interface for common database objects
 * 
 * @author Jeff Tassin
 */
public interface DatabaseObject {
	/** @return the object id/type for this object */
	public DbObjectId getObjectId();
}
