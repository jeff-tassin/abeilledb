package com.jeta.abeille.database.model;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.utils.TSUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class represents the information needed to establish a connection to a
 * database.
 * 
 * @author Jeff Tassin
 */
public class ConnectionInfo implements JETAExternalizable, Cloneable {
	static final long serialVersionUID = -8773885002678833882L;

	public static int VERSION = 1;

	/** the class name of the JDBC driver */
	private String m_driver;

	/** the unique id that identifies the connection */
	private String m_uid;

	/** the url of the database that we want to connect to */
	private String m_url;

	/** the user supplied description for this connection */
	private String m_description;

	/** the vendor name - must be one of the pre-defined vendors */
	private Database m_database;

	/** the server name */
	private String m_server;

	/** the port number */
	private int m_port;

	/**
	 * the name of the user
	 */
	private String m_username;

	/**
	 * the password
	 */
	private String m_password;

	/**
	 * the name for this connection. For databases that support catalogs, this
	 * is the catalog name. For databases that don't support catalogs (such as
	 * McKoi), this can be the schema name
	 */
	private String m_name;

	/** the path/name of the JDBC jar file(s) for this connection */
	private ArrayList m_jars = new ArrayList();

	/* flag to indicate if this info set to advanced by the user */
	private boolean m_advanced = false;

	/** flag that indicats if this database is embedded */
	private boolean m_embedded = false;

	/** database specific parameter */
	private String m_parameter1;

	/**
	 * The class loader for the jar file
	 */
	private transient ClassLoader m_classloader;

	/** the connection id object */
	private transient ConnectionId m_connectionid;

	/**
	 * ctor
	 */
	public ConnectionInfo() {

	}

	/**
	 * ctor
	 */
	public ConnectionInfo(Database database, String uid, String driver, String name, String server, int port) {
		m_database = database;
		m_uid = uid;
		m_driver = driver;
		m_server = server;
		m_port = port;
		m_name = name;
	}

	public ConnectionInfo(Database database, String uid, String name, String server, int port) {
		m_database = database;
		m_uid = uid;
		m_driver = null;
		m_server = server;
		m_port = port;
		m_name = name;
	}

	
	public Object clone() {
		ConnectionInfo ci = new ConnectionInfo();
		ci.m_driver = m_driver;
		ci.m_uid = m_uid;
		ci.m_url = m_url;
		ci.m_description = m_description;
		ci.m_database = m_database;
		ci.m_server = m_server;
		ci.m_port = m_port;
		ci.m_username = m_username;
		ci.m_password = m_password;
		ci.m_name = m_name;
		ci.m_jars = (ArrayList) m_jars.clone();
		ci.m_advanced = m_advanced;
		ci.m_classloader = m_classloader;
		ci.m_connectionid = m_connectionid;
		ci.m_embedded = m_embedded;
		ci.m_parameter1 = m_parameter1;

		return ci;
	}

	/**
	 * Equals test
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof ConnectionInfo) {
			ConnectionInfo info = (ConnectionInfo) obj;
			if (m_uid != null)
				return m_uid.equals(info.m_uid);
			else
				return false;
		} else
			return false;
	}

	/**
	 * @return the class loader for the jdbc jar file
	 */
	public ClassLoader getClassLoader() {
		return m_classloader;
	}

	/**
	 * @return the connection id
	 */
	public ConnectionId getConnectionId() {
		if (m_connectionid == null)
			m_connectionid = new ConnectionId(getUID());

		return m_connectionid;
	}

	/**
	 * @return the underlying database name (e.g. DB2, ORACLE )
	 */
	public Database getDatabase() {
		return m_database;
	}

	/**
	 * @return the description for this connection. This is the value displayed
	 *         in the combo box when the user selects a connection to login with
	 */
	public String getDescription() {
		return m_description;
	}

	public static String getDefaultDriver(Database db) {
		String driver = "";
		if (db == Database.POSTGRESQL) {
			driver = "org.postgresql.Driver";
		} else if (db == Database.MYSQL) {
			driver = "com.mysql.jdbc.Driver";
		} else if (db == Database.HSQLDB) {
			driver = "org.hsqldb.jdbcDriver";
		} else if (db == Database.MCKOI) {
			driver = "com.mckoi.JDBCDriver";
		} else if (db == Database.ORACLE) {
			driver = "oracle.jdbc.driver.OracleDriver";
		} else if (db == Database.DB2) {
			driver = "COM.ibm.db2.jdbc.net.DB2Driver";
		} else if (Database.DAFFODIL.equals(db)) {
			driver = "in.co.daffodil.db.rmi.RmiDaffodilDBDriver";
		} else if (db == Database.POINTBASE) {
			driver = "com.pointbase.jdbc.jdbcUniversalDriver";
		} else if (db == Database.SYBASE) {
			driver = "com.sybase.jdbc2.jdbc.SybDriver";
		}
		return driver;
	}

	public static int getDefaultPort(Database db) {

		if (db == null)
			return 0;

		int port = 0;
		if (db == Database.POSTGRESQL) {
			port = 5432;
		} else if (db == Database.MYSQL) {
			port = 3306;
		} else if (db == Database.HSQLDB) {
			port = 9001;
		} else if (db == Database.MCKOI) {
			port = 9157;
		} else if (db == Database.ORACLE) {
			port = 1521;
		} else if (db == Database.DB2) {
			port = 6789;
		} else if (Database.DAFFODIL.equals(db)) {
			port = 3456;
		} else if (db == Database.POINTBASE) {
			port = 9092;
		} else if (db == Database.SYBASE) {
			port = 0;
		}
		return port;
	}

	/**
	 * @return the class name of the JDBC driver used for a given database
	 *         connection
	 */
	public String getDriver() {
		if (m_driver == null || !isAdvanced()) {
			String driver = "";
			Database db = getDatabase();
			driver = getDefaultDriver(db);
			return driver;
		} else {
			return m_driver;
		}
	}

	/**
	 * @return the path/name to the JDBC jar file for this connection
	 */
	public String getJDBCJar() {
		if (m_jars.size() > 0)
			return (String) m_jars.get(0);
		else
			return null;
	}

	/**
	 * @return the instance name for the database. This is the name that is
	 *         passed in the URL when connection
	 */
	public String getName() {
		if (m_name == null)
			return "";
		else
			return m_name;
	}

	/**
	 * @return a database specific parameter. For example, in daffodil, this
	 *         sets the embedded database location
	 */
	public String getParameter1() {
		return m_parameter1;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return m_password;
	}

	/**
	 * @return the server port for this connection
	 */
	public int getPort() {
		if (m_port == 0)
			return getDefaultPort(getDatabase());
		else
			return m_port;
	}

	/**
	 * @return the server name for this connection
	 */
	public String getServer() {
		return m_server;
	}

	/**
	 * @return a unique id that identifies this connection
	 */
	public String getUID() {
		return m_uid;
	}

	/**
	 * @return the URL for a given database connection
	 */
	public String getUrl() {
		if (isAdvanced())
			return m_url;
		else
			return getUrl(getDatabase(), getServer(), getPort(), getName());
	}

	/**
	 * @return the URL for a given database connection
	 */
	public static String getUrl(Database db, String server, int port, String instanceName) {
		StringBuffer urlbuff = new StringBuffer();
		if (db == Database.POSTGRESQL) {
			urlbuff.append("jdbc:postgresql://");
			urlbuff.append(server);
			urlbuff.append(":");
			urlbuff.append(String.valueOf(port));
			urlbuff.append("/");
			urlbuff.append(instanceName);
		} else if (db == Database.MYSQL) {
			urlbuff.append("jdbc:mysql://");
			urlbuff.append(server);
			urlbuff.append(":");
			urlbuff.append(String.valueOf(port));
			urlbuff.append("/");
			urlbuff.append(instanceName);
		} else if (db == Database.HSQLDB) {
			urlbuff.append("jdbc:hsqldb:hsql://");
			urlbuff.append(server);
			urlbuff.append(":");
			urlbuff.append(String.valueOf(port));
		} else if (db == Database.MCKOI) {
			urlbuff.append("jdbc:mckoi://");
			urlbuff.append(server);
			urlbuff.append(":");
			urlbuff.append(String.valueOf(port));
			if (instanceName != null && instanceName.length() > 0) {
				urlbuff.append("/");
				urlbuff.append(instanceName);
			}
		} else if (db == Database.ORACLE) {
			urlbuff.append("jdbc:oracle:thin:@");
			urlbuff.append(server);
			urlbuff.append(":");
			urlbuff.append(String.valueOf(port));
			urlbuff.append(":");
			urlbuff.append(instanceName);
		} else if (db == Database.DB2) {
			urlbuff.append("jdbc:db2://");
			urlbuff.append(server);
			urlbuff.append(":");
			urlbuff.append(String.valueOf(port));
			urlbuff.append("/");
			urlbuff.append(instanceName);
		} else if (Database.DAFFODIL.equals(db)) {
			urlbuff.append("jdbc:daffodilDB://");
			urlbuff.append(server);
			urlbuff.append(":");
			urlbuff.append(String.valueOf(port));
			urlbuff.append("/");
			urlbuff.append(instanceName);
		} else if (Database.POINTBASE.equals(db)) {
			urlbuff.append("jdbc:pointbase:server://");
			urlbuff.append(server);
			urlbuff.append(":");
			urlbuff.append(String.valueOf(port));
			urlbuff.append("/");
			urlbuff.append(instanceName);
		} else if (db == Database.SYBASE) {
			// jdbc:sybase:Tds:localhost:7500
			urlbuff.append("jdbc:sybase:Tds:");
			urlbuff.append(server);
			urlbuff.append(":");
			urlbuff.append(String.valueOf(port));
			urlbuff.append("/");
			urlbuff.append(instanceName);
		}
		return urlbuff.toString();
	}

	/**
	 * @return the jar files (String objects) defined by this connection
	 */
	public Collection getJars() {
		return m_jars;
	}

	/**
	 * @return the name of the logged in user
	 */
	public String getUserName() {
		return m_username;
	}

	/**
	 * hashCode
	 */
	public int hashCode() {
		assert (m_uid != null);
		return m_uid.hashCode();
	}

	/**
	 * @return true if this connection is advanced
	 */
	public boolean isAdvanced() {
		return m_advanced;
	}

	/**
	 * @return true if this connection is basic
	 */
	public boolean isBasic() {
		return !isAdvanced();
	}

	/**
	 * @return the flag that indicates we are connecting to an embedded database
	 */
	public boolean isEmbedded() {
		return m_embedded;
	}

	public void print() {
		System.out.println("Printing connection info@ " + super.hashCode());
		System.out.println(" url = " + getUrl());
		System.out.println(" driver = " + getDriver());
		System.out.println(" jar = " + getJDBCJar());
	}

	public void setAdvanced(boolean advanced) {
		m_advanced = advanced;
	}

	/**
	 * Sets the database for a given database connection
	 */
	public void setDatabase(Database db) {
		m_database = db;
	}

	/**
	 * Sets the class name of the JDBC driver used for a given database
	 * connection
	 */
	public void setDriver(String driver) {
		m_driver = driver;
	}

	/**
	 * Sets the class loader for the jdbc jar file
	 */
	public void setClassLoader(ClassLoader cloader) {
		m_classloader = cloader;
	}

	/**
	 * Sets the flag that indicates we are connecting to an embedded database
	 */
	public void setEmbedded(boolean embed) {
		m_embedded = embed;
	}

	/**
	 * Sets the path/name to the JDBC jar file for this connection
	 */
	public void setJar(String jar) {
		TSUtils.ensureSize(m_jars, 1);
		m_jars.set(0, jar);
	}

	/**
	 * Sets the jars collection in this object to the collection given
	 * 
	 * @param jars
	 *            a collection of jar files/paths (String objects)
	 */
	public void setJars(Collection jars) {
		m_jars.clear();
		Iterator iter = jars.iterator();
		while (iter.hasNext()) {
			m_jars.add((String) iter.next());
		}
	}

	/**
	 * Sets the description for this connection. This is the value displayed in
	 * the combo box when the user selects a connection to login with
	 */
	public void setDescription(String desc) {
		if (desc != null)
			desc = desc.trim();

		m_description = desc;
	}

	/**
	 * Sets the name for this connection. This is instance name that will passed
	 * to the connection URL
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * Sets a database specific parameter. For example, in daffodil, this sets
	 * the embedded database location
	 */
	public void setParameter1(String param1) {
		m_parameter1 = param1;
	}

	/**
	 * Sets the password
	 */
	public void setPassword(String password) {
		m_password = password;
	}

	/**
	 * Sets the server port for this connection
	 */
	public void setPort(int port) {
		m_port = port;
	}

	/**
	 * Sets the server name for this connection
	 */
	public void setServer(String serverName) {
		m_server = serverName;
	}

	/**
	 * Sets the UID for this connection
	 */
	public void setUID(String uid) {
		m_uid = uid;
	}

	/**
	 * Sets the URL for a given database connection
	 */
	public void setUrl(String url) {
		m_url = url;
	}

	/**
	 * Sets the name of the user for the database connection
	 */
	public void setUserName(String userName) {
		m_username = userName;
	}

	/**
	 * @return the name of the connection
	 */
	public String toString() {
		if (m_description == null || m_description.length() == 0)
			return getName();
		else
			return m_description;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_driver = (String) in.readObject();
		m_uid = (String) in.readObject();
		m_url = (String) in.readObject();
		m_description = (String) in.readObject();
		m_database = (Database) in.readObject();
		m_server = (String) in.readObject();
		m_port = in.readInt();
		m_username = (String) in.readObject();
		m_name = (String) in.readObject();
		m_jars = (ArrayList) in.readObject();
		m_advanced = in.readBoolean();
		m_embedded = in.readBoolean();
		m_parameter1 = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_driver);
		out.writeObject(m_uid);
		out.writeObject(m_url);
		out.writeObject(m_description);
		out.writeObject(m_database);
		out.writeObject(m_server);
		out.writeInt(m_port);
		out.writeObject(m_username);
		out.writeObject(m_name);
		out.writeObject(m_jars);
		out.writeBoolean(m_advanced);
		out.writeBoolean(m_embedded);
		out.writeObject(m_parameter1);
	}

}
