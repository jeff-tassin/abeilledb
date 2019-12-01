package com.jeta.abeille.database.security;

import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;

import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

/**
 * This class defines a service for managing users/groups in a database
 * 
 * @author Jeff Tassin
 */
public class SecurityManager implements SecurityService {
	private SecurityService m_delegate;

	private HashMap m_usercache = new HashMap();

	public SecurityManager(SecurityService delegate) {
		m_delegate = delegate;
	}

	/**
	 * Adds the given group to the database
	 * 
	 * @param group
	 *            the group object that defines the attributes for the group to
	 *            add Note. we ignore any user objects defined in the group
	 *            object. Instead, the users must be passed in as a separate
	 *            collection.
	 * @param users
	 *            a collection of User objects that make up the group.
	 */
	public void addGroup(Group group, Collection users) throws SQLException {
		m_delegate.addGroup(group, users);
	}

	/**
	 * Adds the given user to the database
	 */
	public void addUser(User user, Collection groups) throws SQLException {
		m_delegate.addUser(user, groups);
	}

	/**
	 * Changes the password for the given user
	 */
	public void changePassword(User user, String password) throws SQLException {
		m_delegate.changePassword(user, password);
	}

	/**
	 * Drops the given group from the database
	 */
	public void dropGroup(Group group) throws SQLException {
		m_delegate.dropGroup(group);
	}

	/**
	 * Drops the given user from the database
	 */
	public void dropUser(User user) throws SQLException {
		removeFromCache(user);
		m_delegate.dropUser(user);
	}

	/**
	 * Gets the set of privileges for a user for a database objec.
	 * GrantDefinition objects are returned in a collection
	 * 
	 * @param objtype
	 *            the type of object to get the grants for (e.g. table,
	 *            sequence, function)
	 * @param schema
	 *            the schema
	 * @param user
	 *            the suser or group
	 * @return the set of privileges for a given user for a given table (In the
	 *         form of GrantDefintion objects)
	 */
	public Collection getGrants(DbObjectType objtype, Schema schema, AbstractUser user) throws SQLException {
		return m_delegate.getGrants(objtype, schema, user);
	}

	/**
	 * Gets the set of privileges for a user for a database object.
	 * GrantDefinition objects are returned in a collection
	 * 
	 * @param objid
	 *            the id/type of object to get the grants for (e.g. table,
	 *            sequence, function)
	 * @param qualified
	 *            an optional qualifier that may be needed depending on the type
	 *            ( this is database specific)
	 * @param user
	 *            the suser or group
	 * @return the set of privileges for a given user for a given table (In the
	 *         form of GrantDefintion objects)
	 */
	public Collection getGrants(DbObjectId objId, String qualifier, AbstractUser user) throws SQLException {
		return m_delegate.getGrants(objId, qualifier, user);
	}

	/**
	 * Gets the group associated with the given key. The key is a unique object
	 * that is database dependent.
	 */
	public User getGroup(Object key) throws SQLException {
		return m_delegate.getGroup(key);
	}

	/**
	 * @return a collection of group objects (Group) for this database instance
	 */
	public Collection getGroups() throws SQLException {
		return m_delegate.getGroups();
	}

	/**
	 * @return a collection of supported privileges (Privilege objects) for the
	 *         given object type
	 */
	public Collection getSupportedPrivileges(DbObjectType objType) {
		return m_delegate.getSupportedPrivileges(objType);
	}

	/**
	 * @return a collection of user objects (User) for this database instance
	 */
	public Collection getUsers() throws SQLException {
		return m_delegate.getUsers();
	}

	/**
	 * Gets the user associated with the given key. The key is a unique object
	 * that is database dependent.
	 */
	public synchronized User getUser(Object key) throws SQLException {
		User user = (User) m_usercache.get(key);
		if (user == null) {
			user = m_delegate.getUser(key);
			if (user != null)
				m_usercache.put(key, user);
		}
		return user;
	}

	/**
	 * Modifies the given grant in the database
	 */
	public void modifyGrant(GrantDefinition newdef, GrantDefinition olddef) throws SQLException {
		m_delegate.modifyGrant(newdef, olddef);
	}

	/**
	 * Modifies the given group in the database
	 */
	public void modifyGroup(Group newGroup, Collection newUsers, Group oldGroup) throws SQLException {
		m_delegate.modifyGroup(newGroup, newUsers, oldGroup);
	}

	/**
	 * Modifies the given user
	 */
	public void modifyUser(User user, User newUser, Collection groups) throws SQLException {
		removeFromCache(user);
		m_delegate.modifyUser(user, newUser, groups);
	}

	/**
	 * Removes the given user from the cache
	 */
	public synchronized void removeFromCache(User user) {
		if (user != null) {
			m_usercache.remove(user.getKey());
		}
	}

	/**
	 * Sets the connection which this interface will operate on
	 * 
	 * @param conn
	 *            the connection to set
	 */
	public void setConnection(TSConnection conn) {
		m_delegate.setConnection(conn);
	}
}
