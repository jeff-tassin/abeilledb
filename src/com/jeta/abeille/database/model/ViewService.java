package com.jeta.abeille.database.model;

import java.sql.SQLException;

import java.util.Collection;

/**
 * This is the interface definition for handling views in the database
 * 
 * @author Jeff Tassin
 */
public interface ViewService {
	public static final String COMPONENT_ID = "database.ViewService";

	/**
	 * Creates the given view in the database
	 */
	public void createView(View view) throws SQLException;

	/**
	 * Drops the view from the database
	 */
	public void dropView(DbObjectId viewid, boolean cascade) throws SQLException;

	/**
	 * @return the view for the corresponding id. Null is returned if the view
	 *         cannot be found.
	 */
	public View getView(DbObjectId viewid) throws SQLException;

	/**
	 * @return all views defined in the given schema
	 */
	public Collection getViews(Catalog catalog, Schema schema) throws SQLException;

	/**
	 * Modifies the given view in the database.
	 */
	public void modifyView(View newView, View oldView) throws SQLException;

	/**
	 * Sets the connection which this interface will operate on
	 * 
	 * @param conn
	 *            the connection to set
	 */
	public void setConnection(TSConnection conn);

}
