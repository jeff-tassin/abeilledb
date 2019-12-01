package com.jeta.plugins.abeille.standard;

import java.util.Collection;
import java.util.LinkedList;
import java.sql.*;

import com.jeta.foundation.i18n.I18N;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSDatabaseUsers;

/**
 * This is the Default implementation for the users interface
 * 
 * @author Jeff Tassin
 */
public class DefaultDatabaseUsersImplementation implements TSDatabaseUsers {
	private TSConnection m_connection; // the database connection

	public DefaultDatabaseUsersImplementation() {

	}

	/**
	 * Gets the set of privileges for a user for a table. UserPrivilge objects
	 * are returned in a collection
	 * 
	 * @param tableId
	 *            the id of the table
	 * @param userName
	 *            the name of the user
	 * @return the set of privileges for a given user for a given table (In the
	 *         form of UserPrivilege objects)
	 * @see com.jeta.abeille.database.user.UserPrivileges
	 */
	public Collection getTablePrivileges(TableId tableId, String userName) throws SQLException {
		LinkedList results = new LinkedList();
		return results;
	}

	/**
	 * @return a collection of user objects for this database instance
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

	/**
	 * Get the password for a given user. This will probably be hidden.
	 * 
	 * @param username
	 *            the name of the user whose password we want
	 * @return the password for the given username
	 * @throws SQLException
	 */
	public String getPassword(String username) throws SQLException {
		return null;
	}

	/**
	 * @return a collection of user privileges (String objects ) that are
	 *         support by this vendor
	 */
	public Collection getSupportedPrivileges() throws SQLException {
		return new LinkedList();
	}

}
