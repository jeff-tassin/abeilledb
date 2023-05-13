package com.jeta.plugins.abeille.hsqldb;

import java.sql.SQLException;

import java.util.Collection;
import java.util.LinkedList;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSDatabaseUsers;
import com.jeta.abeille.database.model.TSConnection;

/**
 * This is the MySQL implementation for the users interface
 * 
 * @author Jeff Tassin
 */
public class HSQLDatabaseUsersImplementation implements TSDatabaseUsers {
	private TSConnection m_connection; // the database connection

	/**
	 * ctor
	 */
	public HSQLDatabaseUsersImplementation() {
	}

	/**
	 * Get the password for a given user. This will probably be hidden.
	 * 
	 * @param username
	 *            the name of the user whose password we want
	 * @return the password for the given username
	 */
	public String getPassword(String username) throws SQLException {
		return "";

	}

	/**
	 * @return a collection of user privileges (String objects ) that are
	 *         support by this vendor For postgres: Select, Insert, Update, and
	 *         Role
	 */
	public Collection getSupportedPrivileges() throws SQLException {
		LinkedList results = new LinkedList();
		return results;
	}

	/**
	 * Gets the set of privileges for a user for a table. TSUserPrivilege
	 * objects are returned in a collection
	 * 
	 * @return the set of privileges for a given user for a given table (In the
	 *         form of TSUserPrivilege objects)
	 */
	public Collection getTablePrivileges(TableId tableId, String userName) throws SQLException {
		LinkedList results = new LinkedList();
		return results;
	}

	/**
	 * @return a collection of TSUser or TSGroup objects for this database
	 *         instance
	 */
	public Collection getUsers() throws SQLException {
		LinkedList results = new LinkedList();
		return results;
	}

	/**
	 * Sets the connection which this interface will operate on
	 * 
	 * @param conn
	 *            the connection to set
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
