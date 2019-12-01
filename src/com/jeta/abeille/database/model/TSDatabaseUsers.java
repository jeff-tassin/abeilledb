package com.jeta.abeille.database.model;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Collection;

/**
 * This is the interface definition for user management within the application.
 * Each database vendor will have an implementation of this interface
 * 
 * @author Jeff Tassin
 */
public interface TSDatabaseUsers {
	public static final String COMPONENT_ID = "database.TSDatabaseUsers";

	/**
	 * Get the password for a given user. This will probably be hidden.
	 * 
	 * @param username
	 *            the name of the user whose password we want
	 * @return the password for the given username
	 * @throws SQLException
	 */
	public String getPassword(String username) throws SQLException;

	/**
	 * @return a collection of user privileges (String objects ) that are
	 *         support by this vendor
	 */
	public Collection getSupportedPrivileges() throws SQLException;

	/**
	 * Gets the set of privileges for a user for a table. TSPrivileges objects
	 * are returned in a collection
	 * 
	 * @param tableId
	 *            the id of the table
	 * @param name
	 *            the name of the user/group
	 * @return the set of privileges for a given user for a given table (In the
	 *         form of TSUserPrivileges objects)
	 * @see com.jeta.abeille.database.user.UserPrivileges
	 */
	public Collection getTablePrivileges(TableId tableId, String userName) throws SQLException;

	/**
	 * @return a collection of user/group objects (either TSUser or TSGroup) for
	 *         this database instance
	 */
	public Collection getUsers() throws SQLException;

	/**
	 * Sets the connection which this interface will operate on
	 * 
	 * @param conn
	 *            the connection to set
	 */
	public void setConnection(TSConnection conn);

}
