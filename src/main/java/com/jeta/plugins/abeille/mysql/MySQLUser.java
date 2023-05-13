package com.jeta.plugins.abeille.mysql;

import com.jeta.abeille.database.security.GrantDefinition;
import com.jeta.abeille.database.security.User;

public class MySQLUser extends User {
	/** the global grants for the user */
	private GrantDefinition m_usergrant;

	/**
	 * the host this user is allowed to connect from. This is only valid for
	 * MySQL
	 */
	private String m_host;

	/**
	 * This is the name/host combination
	 */
	private String m_qualifiedname;

	/** the max queries/hour for this user */
	private long m_maxqueries;

	/** the max updates/hour for this user */
	private long m_maxupdates;

	/** the max connections/hour for this user */
	private long m_maxconnections;

	private boolean m_ssl;
	private boolean m_x509;

	private String m_ssl_cipher;
	private String m_x509issuer;
	private String m_x509subject;

	/**
	 * ctor
	 */
	public MySQLUser() {

	}

	public MySQLUser(String userName, String hostName) {
		super(userName);
		setHost(hostName);
	}

	public GrantDefinition getGrantDefinition() {
		return m_usergrant;
	}

	/**
	 * @return the host for this user (only for MySQL )
	 */
	public String getHost() {
		return m_host;
	}

	/**
	 * @return the maximum queries per hour allowed to this user
	 */
	public long getMaxQueries() {
		return m_maxqueries;
	}

	/**
	 * Sets the maximum updates per hour allowed to this user
	 */
	public long getMaxUpdates() {
		return m_maxupdates;
	}

	/**
	 * Sets the maximum connections per hour allowed to this user
	 */
	public long getMaxConnections() {
		return m_maxconnections;
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
		if (m_qualifiedname == null) {
			StringBuffer sbuff = new StringBuffer();
			sbuff.append(getName());
			sbuff.append("@");
			sbuff.append(getHost());
			m_qualifiedname = sbuff.toString();
		}

		return m_qualifiedname;
	}

	public String getSSLCipher() {
		return m_ssl_cipher;
	}

	public String getX509Issuer() {
		return m_x509issuer;
	}

	public String getX509Subject() {
		return m_x509subject;
	}

	public boolean isSSL() {
		return m_ssl;
	}

	public boolean isX509() {
		return m_x509;
	}

	public void setGrantDefinition(GrantDefinition gdef) {
		m_usergrant = gdef;
	}

	/**
	 * Sets the host for this user (only for MySQL )
	 */
	public void setHost(String host) {
		m_host = host;
		m_qualifiedname = null;
	}

	/**
	 * Sets the name for this user
	 */
	public void setName(String name) {
		super.setName(name);
		m_qualifiedname = null;
	}

	/**
	 * Sets the maximum queries per hour allowed to this user
	 */
	public void setMaxQueries(long maxQueries) {
		m_maxqueries = maxQueries;
	}

	/**
	 * Sets the maximum updates per hour allowed to this user
	 */
	public void setMaxUpdates(long maxUpdates) {
		m_maxupdates = maxUpdates;
	}

	/**
	 * Sets the maximum connections per hour allowed to this user
	 */
	public void setMaxConnections(long maxConnections) {
		m_maxconnections = maxConnections;
	}

	public void setSSL(boolean bssl) {
		m_ssl = bssl;
	}

	public void setSSLCipher(String ssl_cipher) {
		m_ssl_cipher = ssl_cipher;
	}

	public void setX509(boolean bx509) {
		m_x509 = bx509;
	}

	public void setX509Issuer(String x509_issuer) {
		m_x509issuer = x509_issuer;
	}

	public void setX509Subject(String x509_subject) {
		m_x509subject = x509_subject;
	}

}
