package com.jeta.plugins.abeille.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
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

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnId;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.DbVersion;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

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
public class MySQLSecurityServiceImplementation extends AbstractSecurityService {
	public static final String COMPONENT_ID = "mysqlsecuritysrv.id";

	static {

	}

	/**
	 * ctor
	 */
	public MySQLSecurityServiceImplementation() {

	}

	/**
	 * ctor
	 */
	public MySQLSecurityServiceImplementation(TSConnection conn) {
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
		modifyUserInternal((MySQLUser) user, null);
	}

	/**
	 * Changes the password for the given user
	 */
	public void changePassword(User user, String password) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("SET PASSWORD FOR ");
		sql.append(getUserNameSQL(user));
		sql.append(" = PASSWORD('");

		StringBuffer logsql = new StringBuffer(sql.toString());
		TSUtils.fillBuffer(logsql, '*', password.length());
		logsql.append("')");

		sql.append(password);
		sql.append("')");

		try {
			DbLogger.fine(logsql.toString());
			if (TSUtils.isDebug()) {
				TSUtils.printMessage(sql.toString());
			}
			DbUtils.executeMetaDataSQL(getConnection(), sql.toString());
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
			UserId userId = (UserId) user.getKey();
			String sql1 = getDropUserSQL(userId);
			String sql2 = "FLUSH PRIVILEGES";
			SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Drop User"), sql1, sql2);
		}
	}

	/**
	 * Generates a where clause for a SQL command when working with users
	 */
	private String generateWhereConstraint(UserId userId, Catalog catalog) {
		StringBuffer sqlbuff = new StringBuffer();
		if (userId != null) {
			boolean userok = false;
			String username = TSUtils.fastTrim(userId.getName());
			sqlbuff.append(" where User = '");
			sqlbuff.append(username);
			sqlbuff.append("'");

			String hostname = TSUtils.fastTrim(userId.getHost());
			sqlbuff.append(" and ");
			sqlbuff.append("Host = '");
			sqlbuff.append(hostname);
			sqlbuff.append("'");

			if (catalog != null) {
				sqlbuff.append(" and Db = '");
				sqlbuff.append(catalog.getName());
				sqlbuff.append("'");
			}
		}
		return sqlbuff.toString();
	}

	/**
	 * This method generates the SQL for a query against a MySQL system table
	 * for the specified user. Most MySQL system tables have a User and Host
	 * name fields.
	 */
	private String generateSystemTableQuery(UserId userId, Catalog catalog, String systemTable) {
		StringBuffer sqlbuff = new StringBuffer();
		sqlbuff.append("select * from ");
		sqlbuff.append(systemTable);
		sqlbuff.append(generateWhereConstraint(userId, catalog));
		return sqlbuff.toString();
	}

	/**
	 * @return the SQL used to drop the given user from the database
	 */
	public String getDropUserSQL(UserId userId) {
		if (userId != null) {
			StringBuffer sqlbuff = new StringBuffer();
			sqlbuff.append("DELETE FROM mysql.user");
			sqlbuff.append(generateWhereConstraint(userId, null));
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
		Collection results = EmptyCollection.getInstance();
		if (user == null) {
			return results;
		}

		if (objtype == MySQLObjectType.TABLE) {
			LinkedList grants = null;
			GrantDefinition gdef = getGlobalPrivileges(user);
			if (gdef != null) {
				LinkedList list = new LinkedList();
				list.add(gdef);
				results = list;
			}
		} else {
			assert (false);
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

		DbObjectType objtype = objid.getObjectType();
		assert (user != null);
		StringBuffer sqlbuff = new StringBuffer();
		if (bgrant)
			sqlbuff.append("GRANT ");
		else
			sqlbuff.append("REVOKE ");

		boolean with_grant = false;
		Iterator iter = privs.iterator();
		while (iter.hasNext()) {
			Privilege rp = (Privilege) iter.next();
			if (rp == Privilege.GRANT) {
				if (bgrant && objtype == MySQLObjectType.GLOBAL) {
					with_grant = true;
					if (privs.size() == 1) {
						sqlbuff.append("USAGE"); // this is required if the only
													// privilege is GRANT
					}
					continue;
				} else {
					sqlbuff.append("GRANT OPTION");
				}
			} else {
				sqlbuff.append(rp.getName());
			}
			if (iter.hasNext())
				sqlbuff.append(", ");
		}

		if (objtype == DbObjectType.COLUMN) {
			ColumnId cid = (ColumnId) objid;
			sqlbuff.append(' ');
			sqlbuff.append('(');
			sqlbuff.append(objid.getObjectName());
			sqlbuff.append(')');
			sqlbuff.append(" ON ");
			sqlbuff.append(cid.getTableId().getFullyQualifiedName());
		} else if (objtype == MySQLObjectType.GLOBAL) {
			sqlbuff.append(" ON ");
			sqlbuff.append("*.*");
		} else {
			sqlbuff.append(" ON ");
			sqlbuff.append(objid.getFullyQualifiedName());
		}

		if (bgrant)
			sqlbuff.append(" TO ");
		else
			sqlbuff.append(" FROM ");

		sqlbuff.append(getUserNameSQL((User) user));

		if (with_grant) {
			sqlbuff.append(" WITH GRANT OPTION");
		}

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
		Collection users = _getUsers((UserId) key);
		Iterator iter = users.iterator();
		if (iter.hasNext()) {
			return (User) iter.next();
		}
		return null;
	}

	/**
	 * @return the global privileges (from either the host or user tables) for
	 *         the given user. Null is returned if the user cannot be found.
	 */
	public GrantDefinition getGlobalPrivileges(AbstractUser user) throws SQLException {
		UserId userId = (UserId) user.getKey();
		if (userId == null)
			return null;

		GrantDefinition gdef = null;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			String sql = generateSystemTableQuery(userId, null, "mysql.user");
			rset = stmt.executeQuery(sql);
			if (rset.next()) {
				gdef = new GrantDefinition();
				gdef.setUser(user);
				Collection assoc = getPrivilegeAssociations(MySQLObjectType.GLOBAL);
				Iterator iter = assoc.iterator();
				while (iter.hasNext()) {
					PrivilegeInfo pi = (PrivilegeInfo) iter.next();
					setUserGrant(gdef, rset, (String) pi.getData1(), pi.getPrivilege());
				}

				assert (!iter.hasNext());
			}
		} finally {
			if (stmt != null)
				stmt.close();

			/** we do this to clear any locks */
			commit();
		}

		return gdef;
	}

	/**
	 * Gets the set of privileges for a user for all columns in a table.
	 * GrantDefinition objects are returned in a collection
	 * 
	 * @param db
	 *            the name of the database instance
	 * @return the set of privileges for a given user for a given table (In the
	 *         form of GrantDefintion objects)
	 */
	public Collection getColumnGrants(TableId tableId, Catalog catalog, UserId userId) throws SQLException {
		LinkedList results = new LinkedList();
		Statement stmt = null;
		ResultSet rset = null;

		User user = getUser(userId);

		TableMetaData tmd = getConnection().getModel(catalog).getTable(tableId);
		if (tmd == null)
			return results;

		try {
			// hash of ColumnMetaData objects (keys) to GrantDefinition
			HashMap found_cols = new HashMap();

			stmt = createStatement();
			String subsql = generateSystemTableQuery(userId, catalog, "mysql.columns_priv");
			StringBuffer sqlbuff = new StringBuffer();
			sqlbuff.append(subsql);
			sqlbuff.append(" and Table_name = '");
			sqlbuff.append(tableId.getTableName());
			sqlbuff.append("'");
			String sql = sqlbuff.toString();

			rset = stmt.executeQuery(sql);
			while (rset.next()) {
				try {
					if (TSUtils.isDebug()) {
						String tablename = new String(DbUtils.getBinaryData(rset, "Table_name"));
						assert (tablename.equals(tableId.getTableName()));
					}

					String columnname = new String(DbUtils.getBinaryData(rset, "Column_name"));
					ColumnMetaData cmd = tmd.getColumn(columnname);
					if (cmd != null) {
						ColumnId cid = cmd.getColumnId();
						assert (cid != null);
						GrantDefinition gdef = new GrantDefinition(cmd.getColumnId());
						assert (cid.getTableId().equals(tableId));
						gdef.setUser(user);

						String priv_tokens = rset.getString("Column_priv");
						if (priv_tokens != null) {
							StringTokenizer st = new StringTokenizer(priv_tokens, ",");
							while (st.hasMoreTokens()) {
								String privname = (String) st.nextToken();
								Privilege priv = Privilege.lookup(privname);
								gdef.addPrivilege(priv);
							}
						}
						found_cols.put(cmd, gdef);
					}
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}

			Collection cols = tmd.getColumns();
			Iterator iter = cols.iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				GrantDefinition gdef = (GrantDefinition) found_cols.get(cmd);
				if (gdef == null) {
					gdef = new GrantDefinition(cmd.getColumnId());
					gdef.setUser(user);
					results.add(gdef);
				} else {
					results.add(gdef);
				}
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
	 * Gets the set of privileges for a user for all tables. GrantDefinition
	 * objects are returned in a collection
	 * 
	 * @param db
	 *            the name of the database instance
	 * @return the set of privileges for a given user for a given table (In the
	 *         form of GrantDefintion objects)
	 */
	public Collection getTableGrants(Catalog catalog, UserId userId) throws SQLException {
		LinkedList results = new LinkedList();
		Statement stmt = null;
		ResultSet rset = null;
		User user = getUser(userId);
		try {

			/**
			 * get all tables in the system so we can set an empty privilege
			 * object for those tables that are not found in the
			 * mysql.tables_priv table
			 */
			TreeSet tables = getConnection().getModel(catalog).getTables(Schema.VIRTUAL_SCHEMA);

			/**
			 * the mysql.tables_priv does not store every table in the system.
			 * so, we keep track of the tables we found. We do this because we
			 * return the privileges for ALL tables in the system
			 */
			HashMap found_tables = new HashMap();

			stmt = createStatement();
			String sql = generateSystemTableQuery(userId, catalog, "mysql.tables_priv");
			rset = stmt.executeQuery(sql);
			while (rset.next()) {
				try {
					String tablename = new String(DbUtils.getBinaryData(rset, "Table_name"));
					TableId tableid = new TableId(catalog, Schema.VIRTUAL_SCHEMA, tablename);

					GrantDefinition gdef = new GrantDefinition(tableid);
					gdef.setUser(user);
					String priv_tokens = rset.getString("Table_priv");
					if (priv_tokens != null) {
						StringTokenizer st = new StringTokenizer(priv_tokens, ",");
						while (st.hasMoreTokens()) {
							String privname = (String) st.nextToken();
							Privilege priv = Privilege.lookup(privname);
							gdef.addPrivilege(priv);
						}
					}

					found_tables.put(tableid, gdef);
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}

			Iterator iter = tables.iterator();
			while (iter.hasNext()) {
				TableId tableid = (TableId) iter.next();
				GrantDefinition gdef = (GrantDefinition) found_tables.get(tableid);
				if (gdef == null) {
					gdef = new GrantDefinition(tableid);
					gdef.setUser(user);
					results.add(gdef);
				} else {
					results.add(gdef);
				}
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
	 * @param userId
	 *            the user id if we want a single user. If null, we get all
	 *            users
	 * @return a collection of Users for this database instance
	 * @param globalGrants
	 *            a flag that indicates if we should also retrieve the global
	 *            grants for this user
	 * @return a collection of user objects (MySQLUser objects)
	 */
	private Collection _getUsers(UserId userId) throws SQLException {
		LinkedList results = null;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			String sql = generateSystemTableQuery(userId, null, "mysql.user");
			rset = stmt.executeQuery(sql);
			while (rset.next()) {
				String hostname = new String(DbUtils.getBinaryData(rset, "Host"));
				String username = new String(DbUtils.getBinaryData(rset, "User"));
				String password = new String(DbUtils.getBinaryData(rset, "Password"));

				TSDatabase db = (TSDatabase) getConnection().getImplementation(TSDatabase.COMPONENT_ID);
				MySQLUser user = new MySQLUser(username, hostname);
				user.setKey(new UserId(username, hostname));
				user.setPassword(password.toCharArray());

				DbVersion version = db.getVersion();
				/** user limits in 4.0.2 */
				try {
					if (version.getMajor() > 4 || (version.getMajor() == 4 && version.getSub() >= 2)) {
						// long max_questions = rset.getLong( "max_questions" );
						// long max_updates = rset.getLong( "max_updates" );
						// long max_connections = rset.getLong(
						// "max_connections" );
						// user.setMaxQueries( max_questions );
						// user.setMaxUpdates( max_updates );
						// user.setMaxConnections( max_connections );
					}
				} catch (Exception e) {
					TSUtils.printException(e);
				}

				/** secure connections in 4.0.0 */
				try {
					if (version.getMajor() >= 4) {
						// String ssl_cipher = new String(
						// DbUtils.getBinaryData( rset, "ssl_cipher" ) );
						// String x509_issuer = new String(
						// DbUtils.getBinaryData( rset, "x509_issuer" ) );
						// String x509_subject = new String(
						// DbUtils.getBinaryData( rset, "x509_subject" ) );

						// user.setSSLCipher( ssl_cipher );
						// user.setX509Issuer( x509_issuer );
						// user.setX509Subject( x509_subject );

						// String ssl_type = rset.getString( "ssl_type" );

						// if ( "X509".equalsIgnoreCase( ssl_type ) )
						// user.setX509( true );
						// else if ( "ANY".equalsIgnoreCase( ssl_type ) )
						// user.setSSL( true );
					}
				} catch (Exception e) {
					TSUtils.printException(e);
				}

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

	public static String getUserNameSQL(User user) {
		MySQLUser myuser = (MySQLUser) user;
		StringBuffer sbuff = new StringBuffer();
		sbuff.append('\'');
		sbuff.append(myuser.getName());
		sbuff.append('\'');
		sbuff.append("@");
		sbuff.append('\'');
		sbuff.append(myuser.getHost());
		sbuff.append('\'');
		return sbuff.toString();
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

		if (objtype == DbObjectType.TABLE || objtype == DbObjectType.COLUMN || objtype == MySQLObjectType.GLOBAL) {
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

			/**
			 * we need to store the grants as separate commands because MySQL
			 * seems to have problems when granting multiple privileges for a
			 * table column with a single command. if you break it into
			 * individual commands, MySQL seems to work fine. So, that's what we
			 * do
			 */
			ArrayList colgrants = new ArrayList();

			if (objtype == DbObjectType.COLUMN && revokesql != null)
				colgrants.add(revokesql);

			String grantsql = null;
			if (grants.size() > 0) {
				if (objtype == DbObjectType.COLUMN) {

					// StringBuffer colgrantsql = new StringBuffer();
					// for columns, we need to set each privilege individually
					ArrayList grantsholder = new ArrayList();
					TSUtils.ensureSize(grantsholder, 1);
					Iterator giter = grants.iterator();
					while (giter.hasNext()) {
						grantsholder.set(0, giter.next());
						String colsql = getGrantSQL(grantsholder, objid, olddef.getUser(), true);
						colgrants.add(colsql);
						// colgrantsql.append( colsql );
						// if ( giter.hasNext() )
						// colgrantsql.append( "\n" );
					}
					// grantsql = colgrantsql.toString();
				} else {
					grantsql = getGrantSQL(grants, objid, olddef.getUser(), true);
				}
			}

			if (objtype == DbObjectType.COLUMN) {
				SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Modify Grant"), colgrants);
			} else {
				if (revokesql != null && grantsql == null) {
					SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Modify Grant"), revokesql);
				} else if (grantsql != null && revokesql == null) {
					SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Modify Grant"), grantsql);
				} else if (grantsql != null && revokesql != null) {
					SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Modify Grant"), revokesql,
							grantsql);
				}
			}
		} else {
			assert (false);
		}

	}

	/**
	 * Modifies the given group in the database
	 */
	public void modifyGroup(Group newGroup, Collection newUsers, Group oldGroup) throws SQLException {

	}

	/**
	 * Adds or modifies the given user
	 */
	public void modifyUserInternal(MySQLUser newUser, MySQLUser oldUser) throws SQLException {
		/*
		 * GRANT USAGE ON *.* TO user_name [REQUIRE NONE | [{SSL| X509}] [CIPHER
		 * cipher [AND]] [ISSUER issuer [AND]] [SUBJECT subject]] [WITH [GRANT
		 * OPTION | MAX_QUERIES_PER_HOUR # | MAX_UPDATES_PER_HOUR # |
		 * MAX_CONNECTIONS_PER_HOUR #]]
		 */

		StringBuffer sql1 = new StringBuffer();
		sql1.append("GRANT USAGE ON *.* TO ");
		sql1.append(getUserNameSQL(newUser));

		StringBuffer sql = null;
		StringBuffer sql2 = null;

		/*
		 * TSDatabase db = (TSDatabase)getConnection().getImplementation(
		 * TSDatabase.COMPONENT_ID ); DbVersion version = db.getVersion(); if (
		 * version.getMajor() >= 4 ) { sql1.append( " REQUIRE " ); if (
		 * newUser.isSSL() ) { sql2 = new StringBuffer( sql1.toString() );
		 * sql1.append( "SSL " ); sql = sql2; } else if ( newUser.isX509() ) {
		 * sql2 = new StringBuffer( sql1.toString() ); sql1.append( "X509 " );
		 * sql = sql2; } else { sql = sql1; }
		 * 
		 * String cipher = TSUtils.fastTrim( TSUtils.strip(
		 * newUser.getSSLCipher(), "\"" ) ); String issuer = TSUtils.fastTrim(
		 * TSUtils.strip( newUser.getX509Issuer(), "\"") ); String subject =
		 * TSUtils.fastTrim( TSUtils.strip( newUser.getX509Subject(), "\"" ) );
		 * 
		 * sql.append( "CIPHER " ); sql.append( DbUtils.toSQL( cipher, '\"' ) );
		 * sql.append( " " );
		 * 
		 * sql.append( "ISSUER " ); sql.append( DbUtils.toSQL( issuer, '\"' ) );
		 * sql.append( " " );
		 * 
		 * sql.append( "SUBJECT " ); sql.append( DbUtils.toSQL( subject, '\"' )
		 * ); sql.append( " " ); }
		 * 
		 * if ( version.getMajor() > 4 || ( version.getMajor() == 4 &&
		 * version.getSub() >= 2) ) { sql.append( "WITH " ); sql.append(
		 * "MAX_QUERIES_PER_HOUR " ); sql.append( newUser.getMaxQueries() );
		 * sql.append( " MAX_UPDATES_PER_HOUR " ); sql.append(
		 * newUser.getMaxUpdates() ); sql.append( " MAX_CONNECTIONS_PER_HOUR "
		 * ); sql.append( newUser.getMaxConnections() ); }
		 */

		String msg = null;
		SecurityManager smgr = (SecurityManager) getConnection().getImplementation(SecurityManager.COMPONENT_ID);
		if (oldUser == null) {
			msg = I18N.getLocalizedMessage("Create User");
		} else {
			smgr.removeFromCache(oldUser);
			msg = I18N.getLocalizedMessage("Modify User");
		}

		sql1.append(';');
		if (sql2 == null) {
			SQLCommand.runMetaDataCommand(getConnection(), msg, sql1.toString());
		} else {
			SQLCommand.runMetaDataCommand(getConnection(), msg, sql1.toString(), sql2.toString());
		}
	}

	/**
	 * Modifies the given user
	 */
	public void modifyUser(User newUser, User oldUser, Collection groups) throws SQLException {
		modifyUserInternal((MySQLUser) newUser, (MySQLUser) oldUser);
	}

	/**
	 * Sets the database connection
	 */
	public void setConnection(TSConnection conn) {
		super.setConnection(conn);
		conn.setImplementation(COMPONENT_ID, this);

		/** global privileges */
		associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.SELECT, "Select_priv"));
		associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.INSERT, "Insert_priv"));
		associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.UPDATE, "Update_priv"));
		associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.DELETE, "Delete_priv"));
		associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.CREATE, "Create_priv"));
		associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.DROP, "Drop_priv"));
		associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.GRANT, "Grant_priv"));
		associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.REFERENCES, "References_priv"));
		associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.INDEX, "Index_priv"));
		associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.ALTER, "Alter_priv"));

		TSDatabase db = (TSDatabase) getConnection().getImplementation(TSDatabase.COMPONENT_ID);
		DbVersion version = db.getVersion();
		if (version.getMajor() >= 4) {
			associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.RELOAD, "Reload_priv"));
			associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.SHUTDOWN, "Shutdown_priv"));
			associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.PROCESS, "Process_priv"));
			associatePrivilege(MySQLObjectType.GLOBAL, new PrivilegeInfo(MySQLPrivilege.FILE, "File_priv"));
			// associatePrivilege( MySQLObjectType.GLOBAL, new PrivilegeInfo(
			// MySQLPrivilege.SHOW_DATABASES, "Show_db_priv" ) );
			// associatePrivilege( MySQLObjectType.GLOBAL, new PrivilegeInfo(
			// MySQLPrivilege.SUPER, "Super_priv" ) );
			// associatePrivilege( MySQLObjectType.GLOBAL, new PrivilegeInfo(
			// MySQLPrivilege.CREATE_TEMPORARY_TABLES, "Create_tmp_table_priv" )
			// );
			// associatePrivilege( MySQLObjectType.GLOBAL, new PrivilegeInfo(
			// MySQLPrivilege.LOCK_TABLES, "Lock_tables_priv" ) );
			// associatePrivilege( MySQLObjectType.GLOBAL, new PrivilegeInfo(
			// MySQLPrivilege.EXECUTE, "Execute_priv" ) );
			// associatePrivilege( MySQLObjectType.GLOBAL, new PrivilegeInfo(
			// MySQLPrivilege.REPLICATION_SLAVE, "Repl_slave_priv" ) );
			// associatePrivilege( MySQLObjectType.GLOBAL, new PrivilegeInfo(
			// MySQLPrivilege.REPLICATION_CLIENT, "Repl_client_priv" ) );
		}

		/** table privileges */
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(MySQLPrivilege.SELECT));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(MySQLPrivilege.INSERT));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(MySQLPrivilege.UPDATE));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(MySQLPrivilege.DELETE));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(MySQLPrivilege.CREATE));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(MySQLPrivilege.DROP));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(MySQLPrivilege.GRANT));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(MySQLPrivilege.REFERENCES));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(MySQLPrivilege.INDEX));
		associatePrivilege(DbObjectType.TABLE, new PrivilegeInfo(MySQLPrivilege.ALTER));

		/** column privileges */
		associatePrivilege(DbObjectType.COLUMN, new PrivilegeInfo(MySQLPrivilege.SELECT));
		associatePrivilege(DbObjectType.COLUMN, new PrivilegeInfo(MySQLPrivilege.INSERT));
		associatePrivilege(DbObjectType.COLUMN, new PrivilegeInfo(MySQLPrivilege.UPDATE));

	}

	/**
	 * Sets the user privilege for a MySQL user
	 */
	private void setUserGrant(GrantDefinition gdef, ResultSet rset, String colName, Privilege priv) throws SQLException {
		String result = rset.getString(colName);
		if (result != null && result.length() == 1) {
			char c = result.charAt(0);
			if (c == 'Y' || c == 'y') {
				gdef.addGrant(priv);
			}
		}
	}

}
