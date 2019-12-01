package com.jeta.abeille.database.security;

import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.utils.EmptyCollection;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class provides for some common security service operations.
 * 
 * @author Jeff Tassin
 */
public abstract class AbstractSecurityService implements SecurityService {
	/** the database connection */
	private TSConnection m_connection;

	/**
	 * A map of DbObjectTypes(keys) to a LinkedList(values) of PrivilegeInfo
	 * objects. This associates a list Privileges supported by a given object
	 * type.
	 */
	private HashMap m_associations = new HashMap();

	/**
	 * This is simply a cache of just DbObjecTypes(kesy) to a LinkedList of
	 * Privileges
	 */
	private HashMap m_privscache = new HashMap();

	/**
	 * Associates a privilege with a given data type. This allows the security
	 * system to query which privileges are available for which database objects
	 * for a given database vendor. For example, in MySQL the table privileges
	 * are different that those found in PostgreSQL.
	 * 
	 * @param objType
	 *            the type of database object we are associating a privilege
	 *            with.
	 * @param data1
	 *            some arbitrary data the caller can associate. Database
	 *            dependent.
	 */
	public void associatePrivilege(DbObjectType objType, PrivilegeInfo privInfo) {
		LinkedList list = (LinkedList) m_associations.get(objType);
		if (list == null) {
			list = new LinkedList();
			m_associations.put(objType, list);
		}
		list.add(privInfo);
		m_privscache.remove(objType);
	}

	/**
	 * Sets the database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return a collection of PrivilegeInfo objects that are associated with
	 *         the given object type.
	 */
	public Collection getPrivilegeAssociations(DbObjectType objType) {
		LinkedList list = (LinkedList) m_associations.get(objType);
		if (list == null)
			return EmptyCollection.getInstance();
		else
			return list;
	}

	/**
	 * @return a collection of supported privileges (Privilege Objects) for the
	 *         given object type
	 */
	public Collection getSupportedPrivileges(DbObjectType objType) {
		LinkedList privs = (LinkedList) m_privscache.get(objType);
		if (privs == null) {
			LinkedList list = (LinkedList) m_associations.get(objType);
			if (list == null) {
				return EmptyCollection.getInstance();
			} else {
				privs = new LinkedList();
				Iterator iter = list.iterator();
				while (iter.hasNext()) {
					PrivilegeInfo pinfo = (PrivilegeInfo) iter.next();
					privs.add(pinfo.getPrivilege());
				}
				m_privscache.put(objType, privs);
			}
		}
		return privs;
	}

	/**
	 * Sets the database connection
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
