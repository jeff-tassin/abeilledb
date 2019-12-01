package com.jeta.plugins.abeille.hsqldb;

import com.jeta.abeille.database.security.User;

public class HSQLUser extends User {
	/** flag that indicates if this user is an administartor */
	private boolean m_admin = false;

	/**
	 * ctor
	 */
	public HSQLUser() {

	}

	public HSQLUser(String userName) {
		super(userName);
	}

	/** flag that indicates if this user is an administartor */
	public boolean isAdministrator() {
		return m_admin;
	}

	/** sets the flag that indicates if this user is an administartor */
	public void setAdministrator(boolean badmin) {
		m_admin = badmin;
	}

}
