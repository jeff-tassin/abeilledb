package com.jeta.plugins.abeille.postgres;

import java.sql.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.StringTokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.abeille.database.model.*;

/**
 * This is the Postgres implementation for the users interface
 * 
 * Postgres supports 4 types of privileges. The privileges are stored in the
 * pg_class table (relacl column). Each privilege is stored as a single
 * character in an access control list (array). The characters are defined as
 * follows: select (r) insert (a) update (w) rule (R) (permission to define
 * rules on a table)
 * 
 * access control lists are in the form: {"=","postgres=arwR","jeff=rR" }
 * 
 * This class currently uses a regular expression library to extract the
 * privileges from the array.
 * 
 * @author Jeff Tassin
 */
public class DatabaseUsersImplementation implements TSDatabaseUsers {
	private TSConnection m_connection; // the database connection

	// regex to extract each users from access control list
	private static final String ALL_USERS_REGEX = "\"((\\\\\"|[^\"\\\\])*)\"";
	// regex to extract user=privileges e.g. jeff=arR
	// group 1 = user group 2 = privileges
	private static final String USER_REGEX = "([a-zA-Z]+)=([arwR]+)";
	// regex to extract the users from a group list
	private static final String GROUP_REGEX = "{([^}]*)}";

	private static HashMap m_regexes = new HashMap(); // store regex Pattern
														// objects here

	public static final String SELECT_PRIVILEGE = "Select";
	public static final String INSERT_PRIVILEGE = "Insert";
	public static final String UPDATE_PRIVILEGE = "Update";
	public static final String RULE_PRIVILEGE = "Role";

	public static final char POSTGRES_SELECT_PRIVILEGE = 'a';
	public static final char POSTGRES_INSERT_PRIVILEGE = 'w';
	public static final char POSTGRES_UPDATE_PRIVILEGE = 'r';
	public static final char POSTGRES_RULE_PRIVILEGE = 'R';

	static {
		// cache regex patterns for efficiency
		m_regexes.put(USER_REGEX, createPattern(USER_REGEX));
		m_regexes.put(ALL_USERS_REGEX, createPattern(ALL_USERS_REGEX));
		m_regexes.put(GROUP_REGEX, createPattern(GROUP_REGEX));
	}

	/**
	 * ctor
	 */
	public DatabaseUsersImplementation() {
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
			System.out.println("Parse Privileges Bad pattern: " + regEx);
			System.out.println(e.getMessage());
			return null;
		}
		return pattern;
	}

	/**
	 * Get the password for a given user. This will probably be hidden.
	 * 
	 * @param username
	 *            the name of the user whose password we want
	 * @return the password for the given username
	 */
	public String getPassword(String username) throws SQLException {
		Statement stmt = null;
		String passwd = "";
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT passwd FROM pg_user WHERE usename = '");
			sql.append(username);
			sql.append("'");
			stmt = m_connection.createStatement();
			ResultSet rset = stmt.executeQuery(sql.toString());
			if (rset.next())
				passwd = rset.getString("passwd");

			return passwd;
		} finally {
			if (stmt != null)
				stmt.close();
		}

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
	 * @return a collection of user privileges (String objects ) that are
	 *         support by this vendor For postgres: Select, Insert, Update, and
	 *         Role
	 */
	public Collection getSupportedPrivileges() throws SQLException {
		LinkedList results = new LinkedList();
		results.add(SELECT_PRIVILEGE);
		results.add(INSERT_PRIVILEGE);
		results.add(UPDATE_PRIVILEGE);
		results.add(RULE_PRIVILEGE);
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

		StringBuffer sql = new StringBuffer();
		sql.append("select oid, relacl from pg_class where relkind='r' and relname = '");
		sql.append(tableId.getTableName());
		sql.append("'");

		Statement stmt = null;
		ResultSet rset = null;

		try {
			stmt = m_connection.createStatement();
			rset = stmt.executeQuery(sql.toString());
			if (rset.next()) {
				long oid = rset.getLong("oid");
				String relacl = rset.getString("relacl");

				// we have an access control list, so lets parse it
				// Note: if relacl is null, then assume all privileges (this
				// appears to be what Postgres assumes)
				if (relacl == null) {
					TSPrivileges priv = new TSPrivileges();
					priv.m_type = TSDatabaseObject.TABLE;
					priv.m_username = userName;
					priv.m_privileges.add(SELECT_PRIVILEGE);
					priv.m_privileges.add(INSERT_PRIVILEGE);
					priv.m_privileges.add(UPDATE_PRIVILEGE);
					priv.m_privileges.add(RULE_PRIVILEGE);
					results.add(priv);
				} else {
					Collection c = parseAccessControlList(relacl);
					Iterator iter = c.iterator();
					while (iter.hasNext()) {
						String userprivpair = (String) iter.next();
						TSPrivileges priv = parsePrivileges(userprivpair);
						if (priv != null && I18N.equals(userName, priv.m_username)) {
							priv.m_objectname = tableId.getTableName();
							results.add(priv);
						}
					}
					// this should be the only instance returned
				}
				TSUtils._assert(!rset.next());
			}
		} finally {
			if (stmt != rset)
				stmt.close();
		}
		return results;
	}

	/**
	 * @return a collection of TSUser or TSGroup objects for this database
	 *         instance
	 */
	public Collection getUsers() throws SQLException {
		LinkedList results = new LinkedList();
		Statement stmt = null;
		ResultSet rset = null;

		try {
			HashMap userids = new HashMap();
			stmt = m_connection.createStatement();
			rset = stmt.executeQuery("select usename, usesysid from pg_user");
			while (rset.next()) {
				String uid = rset.getString("usesysid");
				String usename = rset.getString("usename");
				// store ids in hash map so we can easily get them when reading
				// groups
				userids.put(uid, usename);
				results.add(new TSUser(usename));
			}

			stmt.close();
			stmt = m_connection.createStatement();
			// grolist is a postres array of integers like: {26,27}
			// we will treat this array as a string object an parse it
			rset = stmt.executeQuery("select groname, grolist from pg_group");
			while (rset.next()) {
				String groupname = rset.getString("groname");
				String grolist = rset.getString("grolist");

				TSGroup group = new TSGroup(groupname);
				Collection c = parseUsers(grolist);
				Iterator useriter = c.iterator();
				while (useriter.hasNext()) {
					String uid = (String) useriter.next();
					String user = (String) userids.get(uid);
					group.addUser((String) userids.get(uid));
				}
				results.add(group);
			}
		} finally {
			if (stmt != null)
				stmt.close();
		}

		return results;
	}

	/**
	 * @param aclStr
	 *            the access control list to parse.
	 * @return a collection of String objects that contain the
	 *         username/privilege pairs. this information is extracted directly
	 *         from the access control list using a regular expression.
	 */
	Collection parseAccessControlList(String aclStr) {
		LinkedList results = new LinkedList();

		Pattern pattern = getPattern(ALL_USERS_REGEX);
		Matcher matcher = pattern.matcher(aclStr);
		while (matcher.find()) {
			/**
			 * MatchResult result = matcher.getMatch(); int groups =
			 * result.groups(); if ( groups >= 1 ) { String acl =
			 * result.group(1); if ( acl != null ) results.add( acl ); }
			 */
		}
		return results;
	}

	/**
	 * @param userprivPair
	 *            the user privilege pair (e.g. jeff = awR )
	 * @return a TSPrivileges object that defines the rights for a give user on
	 *         a table. Null is returned if the no privileges are found or an
	 *         error occurs
	 */
	TSPrivileges parsePrivileges(String userprivPair) {
		TSPrivileges priv = null;
		/*
		 * Pattern pattern = getPattern( USER_REGEX ); PatternMatcherInput input
		 * = new PatternMatcherInput(userprivPair); PatternMatcher matcher = new
		 * Perl5Matcher(); while(matcher.contains(input, pattern)) { MatchResult
		 * result = matcher.getMatch(); int groups = result.groups(); if (
		 * groups >= 2 ) { String username = result.group(1); String privs =
		 * result.group(2); if ( username != null && privs != null ) { priv =
		 * new TSPrivileges(); priv.m_type = TSDatabaseObject.TABLE;
		 * priv.m_username = username;
		 * 
		 * int plength = privs.length(); for( int index=0; index < plength;
		 * index++ ) { priv.m_privileges.add( toSQLString(privs.charAt(index) )
		 * ); } } } }
		 */
		return priv;
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

		/*
		 * Pattern pattern = getPattern( GROUP_REGEX ); PatternMatcherInput
		 * input = new PatternMatcherInput( groList ); PatternMatcher matcher =
		 * new Perl5Matcher(); while(matcher.contains(input, pattern)) {
		 * MatchResult result = matcher.getMatch(); int regexgroups =
		 * result.groups(); if ( regexgroups >= 1 ) { String uids =
		 * result.group(1); // we stripped off the braces, so now we have a
		 * comma separated list of items // so, lets tokenize if ( uids != null
		 * ) { StringTokenizer tz = new StringTokenizer( uids, "," ); while(
		 * tz.hasMoreElements() ) { results.add( (String)tz.nextElement() ); } }
		 * } }
		 */
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
	 * @param privAttrib
	 *            the privilege attribute that defines the right: r, a, w, or R
	 * @return the human readable SQL string that corresponds to a single digit
	 *         postgres privilege attribute as found in an access control list
	 */
	public String toSQLString(char privAttrib) {
		if (privAttrib == POSTGRES_SELECT_PRIVILEGE)
			return SELECT_PRIVILEGE;
		else if (privAttrib == POSTGRES_INSERT_PRIVILEGE)
			return INSERT_PRIVILEGE;
		else if (privAttrib == POSTGRES_UPDATE_PRIVILEGE)
			return UPDATE_PRIVILEGE;
		else if (privAttrib == POSTGRES_RULE_PRIVILEGE)
			return RULE_PRIVILEGE;
		else
			return null;
	}
}
