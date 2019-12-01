package com.jeta.abeille.database.security;

import java.util.Calendar;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the base class for a user and group in the (PostgreSQL )database
 * 
 * @author Jeff Tassin
 */
public abstract class AbstractUser implements Comparable {
	/** object that uniquely identifies this use (database dependent) */
	private Object m_key;

	/** The user name */
	private String m_name;

	/**
	 * ctor
	 */
	public AbstractUser() {
	}

	/**
	 * ctor
	 */
	public AbstractUser(String username) {
		m_name = username;
	}

	/**
	 * Comparable interface
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;
		else if (o instanceof AbstractUser) {
			AbstractUser user = (AbstractUser) o;
			return I18N.compareToIgnoreCase(getName(), user.getName());
		} else
			return -1;

	}

	/**
	 * Comparable
	 */
	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	/**
	 * @return the database dependent object that uniquely identifies the user
	 *         in the system.
	 */
	public Object getKey() {
		return m_key;
	}

	/**
	 * @return this user's name
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Some databases such as MySQL determine a user by not only the name but
	 * also the host they are connecting from. This method will return that
	 * combination for MySQL users. For other databases, it just returns the
	 * name.
	 * 
	 * @return the qualified name for this user.
	 */
	public String getQualifiedName() {
		return getName();
	}

	/**
	 * @return the hashCode for this user object
	 */
	public int hashCode() {
		return m_key.hashCode();
	}

	/**
	 * Sets the database dependent object that uniquely identifies the user in
	 * the system.
	 */
	public void setKey(Object key) {
		m_key = key;
	}

	/**
	 * Sets the name for this user
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * toString implementation. Simply returns the user name
	 */
	public String toString() {
		return getName();
	}

}
