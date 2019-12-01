package com.jeta.plugins.abeille.mysql;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.i18n.I18N;

/**
 * @author Jeff Tassin
 */
public class MySQLTableType implements Comparable, JETAExternalizable {
	static final long serialVersionUID = -217169124712616742L;

	public static int VERSION = 1;

	/** the name for this table type */
	private String m_name;

	/**
	 * A description for this type
	 */
	private transient String m_description;

	/** the name for this table type */
	private transient String m_display;

	/** the list of supported types */
	private static LinkedList m_tabletypes = new LinkedList();

	public static MySQLTableType ISAM = new MySQLTableType("ISAM");
	public static MySQLTableType MyISAM = new MySQLTableType("MYISAM");
	public static MySQLTableType MERGE = new MySQLTableType("MRG_MyISAM");
	public static MySQLTableType HEAP = new MySQLTableType("HEAP");
	public static MySQLTableType BDB = new MySQLTableType("BDB");
	public static MySQLTableType InnoDB = new MySQLTableType("InnoDB");
	public static MySQLTableType Unknown = new MySQLTableType("Unknown", I18N.getLocalizedMessage("Unknown"));

	/**
	 * ctor for serialization
	 */
	public MySQLTableType() {

	}

	/**
	 * ctor
	 */
	private MySQLTableType(String name, String display) {
		m_name = name;
		m_display = display;
		m_description = display;
		m_tabletypes.add(this);
	}

	/**
	 * ctor
	 */
	private MySQLTableType(String name) {
		this(name, name);
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object obj) {
		if (obj == this)
			return 0;

		if (obj instanceof MySQLTableType) {
			MySQLTableType tt = (MySQLTableType) obj;
			return m_name.compareTo(tt.m_name);
		} else {
			return -1;
		}
	}

	/**
	 * Comparable implementation
	 */
	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	/**
	 * @return a predefined database object from a string. Note that the case
	 *         does not have to match.
	 */
	public static MySQLTableType fromString(String name) {
		if (name == null)
			return Unknown;

		if (name.equalsIgnoreCase(ISAM.m_name))
			return ISAM;
		else if (name.equalsIgnoreCase(MyISAM.m_name))
			return MyISAM;
		else if (name.equalsIgnoreCase(MERGE.m_name))
			return MERGE;
		else if (name.equalsIgnoreCase(HEAP.m_name))
			return HEAP;
		else if (name.equalsIgnoreCase(BDB.m_name))
			return BDB;
		else if (name.equalsIgnoreCase(InnoDB.m_name))
			return InnoDB;
		else
			return Unknown;
	}

	/**
	 * @return the database name
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return the set of supported table types
	 */
	public static Collection getTypes() {
		return m_tabletypes;
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
