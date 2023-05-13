package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * Class represents a supported database
 * 
 * @author Jeff Tassin
 */
public class Database implements JETAExternalizable {
	static final long serialVersionUID = -8898028937428279202L;

	public static int VERSION = 1;

	/** the name for this database */
	private String m_name;

	/** the list of supported databases */
	private static LinkedList m_databases = new LinkedList();

	public static Database DB2 = new Database("DB2");
	public static Database POSTGRESQL = new Database("POSTGRESQL");
	public static Database MYSQL = new Database("MYSQL");
	public static Database ORACLE = new Database("ORACLE");
	public static Database HSQLDB = new Database("HSQLDB");
	public static Database MCKOI = new Database("MCKOI");
	public static Database SYBASE = new Database("SYBASE");
	public static Database GENERIC = new Database("GENERIC");
	public static Database DAFFODIL = new Database("DAFFODIL");
	public static Database POINTBASE = new Database("POINTBASE");
	public static Database SQLSERVER = new Database("SQLSERVER");

	static {
		m_databases.add(DB2);
		m_databases.add(DAFFODIL);
		m_databases.add(GENERIC);
		m_databases.add(HSQLDB);
		m_databases.add(MCKOI);
		m_databases.add(MYSQL);
		m_databases.add(ORACLE);
		m_databases.add(POINTBASE);
		m_databases.add(POSTGRESQL);
		m_databases.add(SYBASE);
		m_databases.add(SQLSERVER);
	}

	/**
	 * ctor only for serialization
	 */
	public Database() {

	}

	/**
	 * ctor
	 */
	private Database(String name) {
		m_name = name;
	}

	/**
	 * @return a predefined database object from a string. Note that the case
	 *         does not have to match.
	 */
	public static Database fromString(String name) {
		if (name == null)
			return null;

		name = name.toUpperCase();
		if (name.equals(DB2.m_name))
			return DB2;
		else if (name.equals(POSTGRESQL.m_name))
			return POSTGRESQL;
		else if (name.equals(MYSQL.m_name))
			return MYSQL;
		else if (name.equals(ORACLE.m_name))
			return ORACLE;
		else if (name.equals(HSQLDB.m_name))
			return HSQLDB;
		else if (name.equals(MCKOI.m_name))
			return MCKOI;
		else if (name.equals(SYBASE.m_name))
			return SYBASE;
		else if (name.equals(GENERIC.m_name))
			return GENERIC;
		else if (name.equals(DAFFODIL.m_name))
			return DAFFODIL;
		else if (name.equals(POINTBASE.m_name))
			return POINTBASE;
		else if ( name.equals(SQLSERVER.m_name))
			return SQLSERVER;
		else
			return GENERIC;
	}

	/**
	 * @return the set of supported databases
	 */
	public static Collection getDatabases() {
		return m_databases;
	}

	/**
	 * @return the database name
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * For comparable
	 */
	public int hashCode() {
		return m_name.hashCode();
	}

	/**
	 * You absolutely need this when deserializing this class classes
	 */
	private Object readResolve() throws ObjectStreamException {
		return fromString(m_name);
	}

	/**
	 * toString implementation
	 */
	public String toString() {
		return m_name;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_name = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_name);
	}

}
