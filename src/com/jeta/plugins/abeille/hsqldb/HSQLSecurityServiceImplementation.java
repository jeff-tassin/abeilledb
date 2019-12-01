package com.jeta.plugins.abeille.hsqldb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnId;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.security.AbstractSecurityService;
import com.jeta.abeille.database.security.AbstractUser;
import com.jeta.abeille.database.security.GrantDefinition;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.Privilege;
import com.jeta.abeille.database.security.PrivilegeInfo;
import com.jeta.abeille.database.security.SecurityManager;
import com.jeta.abeille.database.security.User;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.abeille.logger.DbLogger;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.EmptyCollection;
import com.jeta.foundation.utils.TSUtils;

/**
 * This service implements the SecurityService for MySQL
 * 
 * @author Jeff Tassin
 */
public class HSQLSecurityServiceImplementation extends AbstractSecurityService {
	public static final String COMPONENT_ID = "hsqlsecuritysrv.id";

	static {
	}

	/**
	 * ctor
	 */
	public HSQLSecurityServiceImplementation() {

	}

	/**
	 * ctor
	 */
	public HSQLSecurityServiceImplementation(TSConnection conn) {
		setConnection(conn);
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

	}

	/**
	 * Creates a new user in the database
	 * 
	 * @param user
	 *            the User to create
	 * @param groups
	 *            a collection of groups (Group objects) to add the user to
	 * @param bcreate
	 *            if true, then we are creating a new user. Otherwise, we are
	 *            editing an existing user.
	 */
	public void addUser(User user, Collection groups) throws SQLException {
		/** CREATE USER username PASSWORD password [ADMIN] */
		HSQLUser hsqluser = (HSQLUser) user;
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE USER ");
		sql.append(user.getName());
		sql.append(" PASSWORD ");

		char[] password = user.getPassword();
		if (password == null || password.length == 0) {
			sql.append("\"\"");
		} else {
			sql.append(password);
		}

		if (hsqluser.isAdministrator()) {
			sql.append(" ADMIN");
		}

		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Create User"), sql.toString());
	}

	/**
	 * Changes the password for the given user
	 */
	public void changePassword(User user, String password) throws SQLException {

	}

	/**
	 * Creates a statement object for executing SQL statements. We use the
	 * metadata connection since that's the data we are accessing here
	 */
	private Statement createStatement() throws SQLException {
		return getConnection().getMetaDataConnection().createStatement();
	}

	/**
	 * Commits any written data and releases any locks held by the connection.
	 */
	private void commit() {
		try {
			if (getConnection().supportsTransactions())
				getConnection().getMetaDataConnection().commit();
		} catch (SQLException se) {
			TSUtils.printException(se);
		}
	}

	/**
	 * Drops the given group from the database
	 */
	public void dropGroup(Group group) throws SQLException {
		if (group != null) {

		}
	}

	/**
	 * Drops the given user from the database
	 */
	public void dropUser(User user) throws SQLException {
		if (user != null) {

		}
	}

	/**
	 * Gets the set of privileges for a user for a table. GrantDefinition
	 * objects are returned in a collection
	 * 
	 * @return the set of privileges for a given user for a given table (In the
	 *         form of GrantDefintion objects)
	 */
	public Collection getGrants(DbObjectType objtype, Schema schema, AbstractUser user) throws SQLException {
		Collection results = EmptyCollection.getInstance();
		return results;
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
		Collection result = EmptyCollection.getInstance();
		return result;
	}

	/**
	 * Generates and returns the SQL needed to GRANT or REVOKE privileges for a
	 * user on a given database object.
	 * 
	 * @param privs
	 *            a Collection of Privilege objects to grant or revoke
	 * @param objid
	 *            the Id of the object we are modifying the privileges for
	 * @param user
	 *            the user we are granting/revoking to
	 * @param bgrant
	 *            if true, then we are granting. Otherwise, we are revoking
	 * @return the SQL command
	 */
	private String getGrantSQL(Collection privs, DbObjectId objid, AbstractUser user, boolean bgrant)
			throws SQLException {
		StringBuffer sqlbuff = new StringBuffer();
		return sqlbuff.toString();
	}

	/**
	 * Gets the group associated with the given key. The key is a unique integer
	 * that corresponds to the grosysid in the pg_group table.
	 */
	public User getGroup(Object key) throws SQLException {
		return null;
	}

	/**
	 * @return a collection of Users for this database instance
	 */
	public Collection getGroups() throws SQLException {
		TreeSet results = new TreeSet();
		return results;
	}

	/**
	 * @return the SQL used to create/alter a user in the database
	 * @param user
	 *            the User to create
	 * @param groups
	 *            a collection of groups (Group objects) to add the user to
	 */
	public String getModifyUserSQL(User user, Collection groups, boolean bcreate) {
		StringBuffer sqlbuff = new StringBuffer();
		return sqlbuff.toString();
	}

	/**
	 * Gets the user associated with the given key. The key is a unique integer
	 * object that corresponds to the usesysid in the pg_shadow table.
	 */
	public User getUser(Object key) throws SQLException {
		// Collection users = _getUsers( (UserId)key );
		// Iterator iter = users.iterator();
		// if ( iter.hasNext() )
		// {
		// return (User)iter.next();
		// }
		return null;
	}

	/**
	 * @return a collection of Users for this database instance
	 */
	public Collection getUsers() throws SQLException {
		LinkedList results = null;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			String sql = "select * from SYSTEM_USERS";
			rset = stmt.executeQuery(sql);
			while (rset.next()) {
				String username = rset.getString("USER");
				boolean admin = rset.getBoolean("ADMIN");

				HSQLUser user = new HSQLUser(username);
				user.setAdministrator(admin);

				if (results == null)
					results = new LinkedList();

				results.add(user);
			}
		} finally {
			if (stmt != null)
				stmt.close();

			/** we do this to clear any locks */
			commit();
		}
		return (results == null ? EmptyCollection.getInstance() : results);
	}

	/**
	 * Modifies the given grant in the database
	 */
	public void modifyGrant(GrantDefinition newdef, GrantDefinition olddef) throws SQLException {

	}

	/**
	 * Modifies the given group in the database
	 */
	public void modifyGroup(Group newGroup, Collection newUsers, Group oldGroup) throws SQLException {

	}

	/**
	 * Modifies the given user
	 */
	public void modifyUser(User newUser, User oldUser, Collection groups) throws SQLException {

	}

	/**
	 * Sets the database connection
	 */
	public void setConnection(TSConnection conn) {
		super.setConnection(conn);
		conn.setImplementation(COMPONENT_ID, this);

		/** table privileges */
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(Privilege.SELECT));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(Privilege.INSERT));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(Privilege.UPDATE));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(Privilege.DELETE));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(Privilege.CREATE));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(Privilege.DROP));
	}

}
