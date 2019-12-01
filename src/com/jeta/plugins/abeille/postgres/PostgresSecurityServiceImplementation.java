package com.jeta.plugins.abeille.postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.TreeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.database.procedures.StoredProcedureService;

import com.jeta.abeille.database.security.AbstractUser;
import com.jeta.abeille.database.security.GrantDefinition;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.Privilege;
import com.jeta.abeille.database.security.SecurityService;
import com.jeta.abeille.database.security.AbstractSecurityService;
import com.jeta.abeille.database.security.User;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.SQLFormatterFactory;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.abeille.logger.DbLogger;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This service implements the SecurityService for Postgres
 * 
 * @author Jeff Tassin
 */
public class PostgresSecurityServiceImplementation extends AbstractSecurityService {
	/** the database connection */
	private TSConnection m_connection;

	/** store regex Patterns here */
	private static HashMap m_regexes = new HashMap();

	/** regex to extract the users from a group list */
	private static final String GROUP_REGEX = "\\{(([^}]*))\\}";
	/** regex to extract each users from access control list */

	static {
		// cache regex patterns for efficiency
		try {
			m_regexes.put(GROUP_REGEX, createPattern(GROUP_REGEX));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ctor
	 */
	public PostgresSecurityServiceImplementation() {

	}

	/**
	 * ctor
	 */
	public PostgresSecurityServiceImplementation(TSConnection conn) {
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
		if (group == null)
			return;

		StringBuffer sqlbuff = new StringBuffer();
		sqlbuff.append("CREATE GROUP ");
		sqlbuff.append(group.getName());
		if (users != null) {
			if (users.size() > 0)
				sqlbuff.append(" WITH USER ");

			Iterator iter = users.iterator();
			while (iter.hasNext()) {
				User user = (User) iter.next();
				sqlbuff.append(user.getName());
				if (iter.hasNext())
					sqlbuff.append(", ");
			}
		}
		SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Create Group"), sqlbuff.toString());
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
		SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Create User"),
				getModifyUserSQL(user, groups, true));
	}

	/**
	 * Changes the password for the given user
	 */
	public void changePassword(User user, String password) throws SQLException {
		/** ALTER USER davide WITH PASSWORD 'hu8jmn3'; **/
		StringBuffer sql = new StringBuffer("ALTER USER ");
		sql.append(user.getName());
		sql.append(" WITH PASSWORD '");

		StringBuffer logsql = new StringBuffer(sql.toString());
		TSUtils.fillBuffer(logsql, '*', password.length());
		logsql.append("'");

		sql.append(password);
		sql.append("'");

		try {
			DbLogger.fine(logsql.toString());
			DbUtils.executeMetaDataSQL(m_connection, sql.toString());
		} catch (SQLException e) {
			DbLogger.log(e);
			throw e;
		}
	}

	/**
	 * Creates a statement object for executing SQL statements. We use the
	 * metadata connection since that's the data we are accessing here
	 */
	private Statement createStatement() throws SQLException {
		return m_connection.getMetaDataConnection().createStatement();
	}

	/**
	 * Commits any written data and releases any locks held by the connection.
	 */
	private void commit() {
		try {
			m_connection.getMetaDataConnection().commit();
		} catch (SQLException se) {
			TSUtils.printException(se);
		}
	}

	/**
	 * Drops the given group from the database
	 */
	public void dropGroup(Group group) throws SQLException {
		if (group != null) {
			StringBuffer sqlbuff = new StringBuffer();
			sqlbuff.append("DROP GROUP ");
			sqlbuff.append(group.getName());
			SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Drop Group"), sqlbuff.toString());
		}
	}

	/**
	 * Drops the given user from the database
	 */
	public void dropUser(User user) throws SQLException {
		if (user != null) {
			SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Drop User"), getDropUserSQL(user));
		}
	}

	/**
	 * Creates a pattern for the given regular expression
	 * 
	 * @param regEx
	 *            the regular expression
	 * @return the created pattern used to search strings based on the regex
	 */
	static Pattern createPattern(String regEx) {
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(regEx);
		} catch (PatternSyntaxException e) {
			TSUtils.printException(e);
		}
		return pattern;
	}

	/**
	 * @return the SQL used to drop the given user from the database
	 */
	public String getDropUserSQL(User user) {
		if (user != null) {
			StringBuffer sqlbuff = new StringBuffer();
			sqlbuff.append("DROP USER ");
			sqlbuff.append(user.getName());
			sqlbuff.append(';');
			return sqlbuff.toString();
		} else {
			return "";
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

		TreeSet results = new TreeSet();
		StringBuffer sql = new StringBuffer();
		// relkind 'r' TABLE, 'S' sequence, 'v' view
		if (m_connection.supportsSchemas()) {
			if (objtype == DbObjectType.FUNCTION) {
				sql.append("select oid, proname, proacl, proowner from pg_catalog.pg_proc where pg_catalog.pg_proc.pronamespace = pg_catalog.pg_namespace.oid ");
				sql.append(" AND pg_catalog.pg_namespace.nspname = '");
				sql.append(schema.getName());
				sql.append("'");
			} else {
				// tables, views, and sequences
				sql.append("select oid, relname, relacl, relowner from pg_catalog.pg_class where pg_class.relnamespace = pg_catalog.pg_namespace.oid ");
				sql.append(" AND pg_namespace.nspname = '");
				sql.append(schema.getName());
				sql.append("' and");
				sql.append(" relkind = '");
			}
		} else {
			if (objtype == DbObjectType.FUNCTION) {
				sql.append("select oid, proname, proacl, proowner from pg_proc");
			} else {
				sql.append("select oid, relname, relacl, relowner from pg_class where");
				sql.append(" relkind = '");
			}
		}

		if (objtype == DbObjectType.TABLE) {
			sql.append("r'");
		} else if (objtype == DbObjectType.SEQUENCE) {
			sql.append("S'");
		} else if (objtype == DbObjectType.VIEW) {
			sql.append("v'");
		}

		Statement stmt = null;
		ResultSet rset = null;

		try {
			stmt = createStatement();

			rset = stmt.executeQuery(sql.toString());
			while (rset.next()) {
				long oid = rset.getLong("oid");
				String relacl = null;
				String objname = null;
				int relowner = 0;

				if (objtype == DbObjectType.FUNCTION) {
					relacl = rset.getString("proacl");
					objname = rset.getString("proname");
					relowner = rset.getInt("proowner");
				} else {
					relacl = rset.getString("relacl");
					objname = rset.getString("relname");
					relowner = rset.getInt("relowner");
				}

				// don't show system tables for versions of Postgres that don't
				// support schemas
				if (schema == Schema.VIRTUAL_SCHEMA && (objtype == DbObjectType.TABLE) && objname.indexOf("pg_") == 0)
					continue;

				boolean isowner = false;
				Integer userid = (Integer) user.getKey();
				if (userid.intValue() == relowner)
					isowner = true;

				DbObjectId objid = new DbObjectId(objtype, m_connection.getDefaultCatalog(), schema, objname);
				objid.setKey(new Integer((int) oid));

				// we have an access control list, so lets parse it
				// Note: if relacl is null, then assume all privileges (this
				// appears to be what Postgres assumes)
				if (relacl == null) {
					// all privileges for all users
					GrantDefinition gdef = new GrantDefinition(objid);
					gdef.setOwner(isowner);
					gdef.setUser(user);
					results.add(gdef);
				} else {
					// strip off {}
					relacl = TSUtils.strip(relacl, "{}");
					// now tokenize for each comman seperated value
					boolean badded = false;
					StringTokenizer st = new StringTokenizer(relacl, ",");
					while (st.hasMoreTokens()) {
						String usertoken = st.nextToken();
						GrantDefinition grantdef = parseACL(usertoken);

						grantdef.setOwner(isowner);

						// set the object name
						grantdef.setObjectId(objid);
						if (user.equals(grantdef.getUser())) {
							results.add(grantdef);
							badded = true;
						}
					}

					if (!badded) {
						// then add an empty grant
						GrantDefinition gdef = new GrantDefinition(objid);
						gdef.setOwner(isowner);
						gdef.setUser(user);
						results.add(gdef);
					}
				}
			}
		} finally {
			if (stmt != rset)
				stmt.close();

			/** we do this to clear any locks */
			commit();
		}
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
		// not used for PostgreSQL
		assert (false);
		return null;
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
		if (bgrant)
			sqlbuff.append("GRANT ");
		else
			sqlbuff.append("REVOKE ");

		Iterator iter = privs.iterator();
		while (iter.hasNext()) {
			Privilege rp = (Privilege) iter.next();
			sqlbuff.append(rp.getName());
			if (iter.hasNext())
				sqlbuff.append(", ");
		}
		sqlbuff.append(" ON ");
		if (objid.getObjectType() == DbObjectType.FUNCTION) {
			sqlbuff.append("FUNCTION ");
			StoredProcedureService ps = (StoredProcedureService) m_connection
					.getImplementation(StoredProcedureService.COMPONENT_ID);
			assert (objid.getKey() != null);
			StoredProcedure proc = ps.lookupProcedure(objid.getKey());
			sqlbuff.append(proc.getSignature());
		} else {
			sqlbuff.append(objid.getFullyQualifiedName());
		}

		if (bgrant)
			sqlbuff.append(" TO ");
		else
			sqlbuff.append(" FROM ");

		if (user instanceof Group) {
			sqlbuff.append("GROUP ");
		}
		sqlbuff.append(user.getName());
		sqlbuff.append(';');
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
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			// grolist is a postres array of integers like: {26,27}
			// we will treat this array as a string object an parse it
			String sql = null;
			if (m_connection.supportsSchemas()) {
				sql = "select * from pg_catalog.pg_group";
			} else {
				sql = "select * from pg_group";
			}

			rset = stmt.executeQuery(sql);
			while (rset.next()) {
				String groupname = rset.getString("groname");
				String grolist = rset.getString("grolist");
				int groupid = rset.getInt("grosysid");

				Group group = new Group(groupname);
				group.setKey(new Integer(groupid));
				Collection c = parseUsers(grolist);
				Iterator useriter = c.iterator();
				while (useriter.hasNext()) {
					Integer uid = (Integer) useriter.next();
					group.addUser(uid);
				}
				results.add(group);
			}
		} finally {
			if (stmt != null)
				stmt.close();

			/** we do this to clear any locks */
			commit();
		}
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

		if (bcreate) {
			sqlbuff.append("CREATE USER ");
		} else {
			sqlbuff.append("ALTER USER ");
		}

		sqlbuff.append(user.getName());
		sqlbuff.append(" WITH ");

		if (bcreate) {

			char[] password = user.getPassword();
			if (password != null && password.length > 0) {
				if (user.isPasswordEncrypted()) {
					sqlbuff.append("ENCRYPTED ");
				} else {
					sqlbuff.append("UNENCRYPTED ");
				}

				sqlbuff.append("PASSWORD '");
				sqlbuff.append(password);
				sqlbuff.append("' ");
			}
		}

		if (user.canCreateDB()) {
			sqlbuff.append("CREATEDB ");
		} else {
			sqlbuff.append("NOCREATEDB ");
		}

		if (user.canCreateUser()) {
			sqlbuff.append("CREATEUSER ");
		} else {
			sqlbuff.append("NOCREATEUSER ");
		}

		if (bcreate) {
			if (groups != null && groups.size() > 0) {
				sqlbuff.append("IN GROUP ");
				Iterator iter = groups.iterator();
				while (iter.hasNext()) {
					Group group = (Group) iter.next();
					sqlbuff.append(group.getName());
					if (iter.hasNext())
						sqlbuff.append(", ");
				}
				sqlbuff.append(" ");
			}
		}

		if (!user.isValidAlways()) {
			sqlbuff.append("VALID UNTIL ");
			SQLFormatterFactory ffactory = SQLFormatterFactory.getInstance(m_connection);
			SQLFormatter formatter = ffactory.createFormatter();
			sqlbuff.append(formatter.formatTimestamp(user.getExpireDate()));
		}

		return sqlbuff.toString();
	}

	/**
	 * @param regEx
	 *            the regular expression whose pattern we wish to retrieve
	 * @return a pattern used for searching the given regular expression
	 */
	Pattern getPattern(String regEx) {
		return (Pattern) m_regexes.get(regEx);
	}

	/**
	 * Gets the user associated with the given key. The key is a unique integer
	 * object that corresponds to the usesysid in the pg_shadow table.
	 */
	public User getUser(Object key) throws SQLException {
		Collection users = _getUsers((Integer) key);
		Iterator iter = users.iterator();
		if (iter.hasNext()) {
			return (User) iter.next();
		}
		return null;
	}

	/**
	 * @param userId
	 *            the user id if we want a single user. If null, we get all
	 *            users
	 * @return a collection of Users for this database instance
	 */
	public Collection _getUsers(Integer userId) throws SQLException {
		TreeSet results = new TreeSet();
		Statement stmt = null;
		ResultSet rset = null;

		try {
			stmt = createStatement();
			StringBuffer sqlbuff = new StringBuffer();
			if (m_connection.supportsSchemas()) {
				sqlbuff.append("select * from pg_catalog.pg_user");
			} else {
				sqlbuff.append("select * from pg_user");
			}

			if (userId != null) {
				sqlbuff.append(" where usesysid = ");
				sqlbuff.append(userId);
			}

			String sql = sqlbuff.toString();
			rset = stmt.executeQuery(sql);
			while (rset.next()) {
				int uid = rset.getInt("usesysid");
				String usename = rset.getString("usename");
				boolean createdb = rset.getBoolean("usecreatedb");
				boolean usesuper = rset.getBoolean("usesuper");
				String password = rset.getString("passwd");
				java.sql.Timestamp ts = rset.getTimestamp("valuntil");

				User user = new User(usename);
				user.setKey(new Integer(uid));
				user.canCreateDB(createdb);
				user.canCreateUser(usesuper);
				user.setPassword(password.toCharArray());
				if (ts != null) {
					user.setValidAlways(false);
					Calendar c = Calendar.getInstance();
					c.setTime(ts);
					user.setExpireDate(c);
				}
				results.add(user);
			}
		} finally {
			if (stmt != null)
				stmt.close();

			/** we do this to clear any locks */
			commit();
		}

		return results;
	}

	/**
	 * @return a collection of Users for this database instance
	 */
	public Collection getUsers() throws SQLException {
		return _getUsers(null);
	}

	/**
	 * Modifies the given grant in the database
	 */
	public void modifyGrant(GrantDefinition newdef, GrantDefinition olddef) throws SQLException {

		DbObjectId objid = olddef.getObjectId();
		DbObjectType objtype = objid.getObjectType();

		if (objtype == DbObjectType.TABLE || objtype == DbObjectType.VIEW || objtype == DbObjectType.SEQUENCE
				|| objtype == DbObjectType.FUNCTION) {
			LinkedList revokes = new LinkedList();
			LinkedList grants = new LinkedList();

			// first we need to find out what grants were dropped and what
			// grants where added
			Collection privs = Privilege.getDefinitions();
			Iterator iter = privs.iterator();
			while (iter.hasNext()) {
				Privilege priv = (Privilege) iter.next();
				if (newdef.isGranted(priv)) {
					if (!olddef.isGranted(priv)) {
						grants.add(priv);
					}
				} else {
					if (olddef.isGranted(priv)) {
						revokes.add(priv);
					}
				}
			}

			String revokesql = null;
			if (revokes.size() > 0) {
				revokesql = getGrantSQL(revokes, objid, olddef.getUser(), false);
			}

			String grantsql = null;
			if (grants.size() > 0) {
				grantsql = getGrantSQL(grants, objid, olddef.getUser(), true);
			}

			if (revokesql != null && grantsql == null) {
				SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Modify Grant"), revokesql);
			} else if (grantsql != null && revokesql == null) {
				SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Modify Grant"), grantsql);
			} else if (grantsql != null && revokesql != null) {
				SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Modify Grant"), revokesql,
						grantsql);
			}
		} else {
			assert (false);
		}
	}

	/**
	 * Modifies the given group in the database
	 */
	public void modifyGroup(Group newGroup, Collection newUsers, Group oldGroup) throws SQLException {
		StringBuffer dropsql = null;
		StringBuffer addsql = null;

		SecurityService srv = (SecurityService) m_connection.getImplementation(SecurityService.COMPONENT_ID);

		// we support adding and dropping users
		// first let's get list of users to drop
		Collection oldusers = oldGroup.getUsers();
		Iterator iter = oldusers.iterator();
		while (iter.hasNext()) {
			Object userkey = (Object) iter.next();
			if (!newGroup.containsUser(userkey)) {
				User user = srv.getUser(userkey);
				if (user != null) {
					if (dropsql == null) {
						dropsql = new StringBuffer();
						dropsql.append("ALTER GROUP ");
						dropsql.append(oldGroup.getName());
						dropsql.append(" DROP USER ");
						dropsql.append(user.getName());
					} else {
						dropsql.append(", ");
						dropsql.append(user.getName());
					}
				}
			}
		}

		if (dropsql != null)
			dropsql.append(';');

		// now, let's get the list of users to add
		iter = newUsers.iterator();
		while (iter.hasNext()) {
			User user = (User) iter.next();
			if (!oldGroup.containsUser(user.getKey())) {
				if (addsql == null) {
					addsql = new StringBuffer();
					addsql.append("ALTER GROUP ");
					addsql.append(oldGroup.getName());
					addsql.append(" ADD USER ");
					addsql.append(user.getName());
				} else {
					addsql.append(", ");
					addsql.append(user.getName());
				}
			}
		}

		if (dropsql != null && addsql == null)
			SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Modify Group"), dropsql.toString());
		else if (addsql != null && dropsql == null)
			SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Modify Group"), addsql.toString());
		else if (dropsql != null && addsql != null)
			SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Modify Group"), dropsql.toString(),
					addsql.toString());
	}

	/**
	 * Modifies the given user
	 */
	public void modifyUser(User user, User oldUser, Collection groups) throws SQLException {
		SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Modify User"),
				getModifyUserSQL(user, groups, false));
	}

	/**
	 * @param aclStr
	 *            the access control tokens for a single user (e.g.
	 *            postgres=arwdRxt or "group foo=r" )
	 * @return a GrantDefinition object that defines the privileges for the
	 *         given user {=,postgres=arwdRxt,jeff=r,"group foo=r"}
	 */
	private GrantDefinition parseACL(String usertoken) {

		GrantDefinition result = null;
		int pos = usertoken.indexOf("\"group");
		if (pos >= 0) {
			// then its a group
			usertoken = TSUtils.strip(usertoken, "\"");
			usertoken = usertoken.substring(pos + 6, usertoken.length());

			result = parserUserPermissions(usertoken, true);
		} else {
			result = parserUserPermissions(usertoken, false);
		}
		return result;
	}

	/**
	 * Parses the permissions from a single user format: user=arwdRxt
	 * 
	 * @param bGroup
	 *            set to true if the permissions are being parsed for a group
	 *            rather than a user. We need this so we can construct either a
	 *            group or user object for the GrantDefinition
	 */
	private GrantDefinition parserUserPermissions(String usertoken, boolean bGroup) {
		GrantDefinition gdef = new GrantDefinition();
		int pos = usertoken.indexOf("=");
		if (pos >= 0) {
			if (pos == 0) {
				gdef.setUser(User.PUBLIC);
			} else {
				String username = usertoken.substring(0, pos).trim();
				if (bGroup) {
					gdef.setUser(new Group(username));
				} else {
					gdef.setUser(new User(username));
				}
			}

			String tokens = usertoken.substring(pos + 1, usertoken.length());
			for (int index = 0; index < tokens.length(); index++) {
				char c = tokens.charAt(index);
				switch (c) {
				case 'r': // SELECT ("read")
					gdef.addGrant(Privilege.SELECT);
					break;

				case 'w': // -- UPDATE ("write")
					gdef.addGrant(Privilege.UPDATE);
					break;

				case 'a': // -- INSERT ("append")
					gdef.addGrant(Privilege.INSERT);
					break;

				case 'd': // -- DELETE
					gdef.addGrant(Privilege.DELETE);
					break;

				case 'R': // -- RULE
					gdef.addGrant(Privilege.RULE);
					break;

				case 'x': // -- REFERENCES
					gdef.addGrant(Privilege.REFERENCES);
					break;

				case 't': // -- TRIGGER
					gdef.addGrant(Privilege.TRIGGER);
					break;

				case 'X': // -- EXECUTE
					gdef.addGrant(Privilege.EXECUTE);
					break;

				case 'U': // -- USAGE
					gdef.addGrant(Privilege.USAGE);
					break;

				case 'C': // -- CREATE
					gdef.addGrant(Privilege.CREATE);
					break;

				case 'T': // -- TEMPORARY
					gdef.addGrant(Privilege.TEMPORARY);
					break;
				}
			}
		}
		return gdef;
	}

	/**
	 * @param groList
	 *            a group list obtained from the pg_group table, grolist column.
	 *            This is in the form { uid1, uid2, ..., uidN } where uid is an
	 *            integer
	 * @return a collection of user ids (String objects) from a postgres array
	 *         object
	 */
	Collection parseUsers(String groList) {
		LinkedList results = new LinkedList();
		if (groList == null || groList.trim().length() == 0) {
			return results;
		}

		Pattern pattern = getPattern(GROUP_REGEX);
		Matcher matcher = pattern.matcher(groList);
		if (matcher.find()) {
			if (matcher.groupCount() > 1) {
				String userids = matcher.group(1);
				// we stripped off the braces, so now we have a comma separated
				// list of items
				// so, lets tokenize
				StringTokenizer tz = new StringTokenizer(userids, ",");
				while (tz.hasMoreElements()) {
					Integer userid = new Integer((String) tz.nextElement());
					results.add(userid);
				}
			}
		}
		return results;
	}

	/**
	 * Helper method that sets all privileges on the given grant object
	 */
	private static void setAllPrivileges(GrantDefinition gdef) {
		Collection privs = Privilege.getDefinitions();
		Iterator iter = privs.iterator();
		while (iter.hasNext()) {
			gdef.addGrant((Privilege) iter.next());
		}
	}

	/**
	 * Sets the database connection
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
