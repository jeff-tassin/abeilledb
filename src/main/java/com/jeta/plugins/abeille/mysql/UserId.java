package com.jeta.plugins.abeille.mysql;

/**
 * An object that uniquely identifies a user in the database
 */
public class UserId {
	private String m_host;
	private String m_name;

	public UserId(String name, String host) {
		m_name = name;
		m_host = host;
	}

	public String getHost() {
		return m_host;
	}

	public String getName() {
		return m_name;
	}
}
