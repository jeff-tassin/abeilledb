package com.jeta.abeille.database.security;

/**
 * This class allows a database plugin to associate some data with a given
 * privilege. MySQL uses this to associate a column name in a security table
 * with its corresponding privilege.
 * 
 * @author Jeff Tassin
 */
public class PrivilegeInfo {
	private Privilege m_priv;
	private Object m_data;

	public PrivilegeInfo(Privilege priv) {
		m_priv = priv;
	}

	public PrivilegeInfo(Privilege priv, Object data) {
		m_priv = priv;
		m_data = data;
	}

	public Privilege getPrivilege() {
		return m_priv;
	}

	public Object getData1() {
		return m_data;
	}
}
