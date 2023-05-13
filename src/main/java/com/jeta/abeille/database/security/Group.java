package com.jeta.abeille.database.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a group in the database
 * 
 * @author Jeff Tassin
 */
public class Group extends AbstractUser implements Comparable {

	/**
	 * An array of user keys assoicated with this group. The key is dependent on
	 * the database. You should call getUser(key) on the SecurityService using
	 * the values in this array
	 */
	private ArrayList m_users = new ArrayList();

	/**
	 * ctor
	 */
	public Group() {
	}

	/**
	 * ctor
	 */
	public Group(String name) {
		super(name);
	}

	/**
	 * Adds a user to this group
	 */
	public void addUser(Object userId) {
		if (userId != null) {
			m_users.add(userId);
		}
	}

	/**
	 * Adds a user to this group
	 */
	public void addUser(User user) {
		if (user != null) {
			addUser(user.getKey());
		}
	}

	/**
	 * Comparable interface
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;
		else if (o instanceof Group) {
			Group group = (Group) o;
			return I18N.compareToIgnoreCase(getName(), group.getName());
		} else
			return -1;

	}

	/**
	 * @return true if this group contains the given user
	 */
	public boolean containsUser(Object userId) {
		if (userId == null)
			return false;

		Iterator iter = m_users.iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			if (userId == key || userId.equals(key))
				return true;
		}
		return false;
	}

	/**
	 * Comparable
	 */
	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	/**
	 * @return a collection of identifiers (implementation dependent) that
	 *         identify the users in this group
	 */
	public Collection getUsers() {
		return m_users;
	}

	/**
	 * @return the number of users in this group.
	 */
	public int size() {
		return m_users.size();
	}

}
