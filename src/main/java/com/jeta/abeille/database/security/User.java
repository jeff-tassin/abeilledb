package com.jeta.abeille.database.security;

import java.util.Calendar;

import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a user in the (PostgreSQL )database
 * 
 * @author Jeff Tassin
 */
public class User extends AbstractUser implements Comparable {
	/** true if this user can create a new database instance */
	private boolean m_createdb;

	/** true if this user can create other users */
	private boolean m_createuser;

	/** true if this user's password is encrypted */
	private boolean m_isencrypted;

	/** true if this user account never exipres */
	private boolean m_validalways = true;

	/** the date the password expires iff m_validalways = false */
	private Calendar m_expiredate;

	/**
	 * the user's password. If this is an existing user, then the password will
	 * be masked. If this is a new user, then the password will be clear-text
	 * here
	 */
	private char[] m_password;

	public static final User PUBLIC;
	static {
		PUBLIC = new User(I18N.getLocalizedMessage("Public"));
		PUBLIC.setKey(new Integer(0));
	}

	/**
	 * ctor
	 */
	public User() {
	}

	/**
	 * ctor
	 */
	public User(String username) {
		super(username);
	}

	/**
	 * Comparable interface
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;
		else if (o instanceof User) {
			User user = (User) o;
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
	 * @return true if this user can create a database
	 */
	public boolean canCreateDB() {
		return m_createdb;
	}

	/**
	 * Sets the flag that indicates if this user can create a database
	 */
	public void canCreateDB(boolean createDb) {
		m_createdb = createDb;
	}

	/**
	 * @return true if this user can create other users
	 */
	public boolean canCreateUser() {
		return m_createuser;
	}

	/**
	 * Sets the flag that indicates if this user can create other users
	 */
	public void canCreateUser(boolean createUser) {
		m_createuser = createUser;
	}

	/**
	 * @return the date at which the password expires. This is overriden if the
	 *         validAlways flag is set
	 */
	public Calendar getExpireDate() {
		return m_expiredate;
	}

	/**
	 * @return the user's password. If this is an existing user, then the
	 *         password will be masked. If this is a new user, then the password
	 *         will be clear-text here
	 */
	public char[] getPassword() {
		return m_password;
	}

	/**
	 * @return true if the password is encrypted in the database
	 */
	public boolean isPasswordEncrypted() {
		return m_isencrypted;
	}

	/**
	 * @return that flag that indicates if this account does not expire
	 */
	public boolean isValidAlways() {
		return m_validalways;
	}

	/**
	 * Sets the date at which the password expires. This is overriden if the
	 * validAlways flag is set
	 */
	public void setExpireDate(Calendar c) {
		m_expiredate = c;
	}

	/**
	 * Sets the password
	 */
	public void setPassword(char[] password) {
		m_password = password;
	}

	/**
	 * Sets the flag tht indicates if the password is encrypted in the database
	 */
	public void setPasswordEncrypted(boolean isEncrypted) {
		m_isencrypted = isEncrypted;
	}

	/**
	 * Sets that flag that indicates if this account does not expire
	 */
	public void setValidAlways(boolean validAlways) {
		m_validalways = validAlways;
	}
}
