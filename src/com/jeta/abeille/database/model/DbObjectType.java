package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.HashMap;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.i18n.I18N;

/**
 * Class defines common database objects
 * 
 * @author Jeff Tassin
 */
public class DbObjectType implements JETAExternalizable {
	/** serialize id */
	static final long serialVersionUID = -8458636015789410341L;

	public static int VERSION = 1;

	/** the name for this object */
	private String m_name;
	/** the name to display for this object */
	private String m_display;

	private static HashMap m_types = new HashMap();

	public static DbObjectType TABLE = new DbObjectType("TABLE", I18N.getLocalizedMessage("Table"));
	public static DbObjectType COLUMN = new DbObjectType("COLUMN", I18N.getLocalizedMessage("Column"));
	public static DbObjectType VIEW = new DbObjectType("VIEW", I18N.getLocalizedMessage("View"));
	public static DbObjectType SEQUENCE = new DbObjectType("SEQUENCE", I18N.getLocalizedMessage("Sequence"));
	public static DbObjectType FUNCTION = new DbObjectType("FUNCTION", I18N.getLocalizedMessage("Function"));
	public static DbObjectType INDEX = new DbObjectType("INDEX", I18N.getLocalizedMessage("Index"));
	public static DbObjectType TRIGGER = new DbObjectType("TRIGGER", I18N.getLocalizedMessage("Trigger"));
	public static DbObjectType RULE = new DbObjectType("RULE", I18N.getLocalizedMessage("Rule"));
	public static DbObjectType PROCEDURE = new DbObjectType("PROCEDURE", I18N.getLocalizedMessage("Procedure"));
	public static DbObjectType SCHEMA = new DbObjectType("SCHEMA", I18N.getLocalizedMessage("Schema"));
	public static DbObjectType JETA_FORM = new DbObjectType("JETA_FORM", I18N.getLocalizedMessage("Form"));
	public static DbObjectType JETA_QUERY = new DbObjectType("JETA_QUERY", I18N.getLocalizedMessage("Query"));

	/**
	 * ctor only for serialization
	 */
	public DbObjectType() {

	}

	/**
	 * ctor
	 */
	protected DbObjectType(String name, String display) {
		m_name = name;
		m_display = display;
		m_types.put(name, this);
	}

	/**
	 * @return a predefined direction object from a string. Note that the case
	 *         must match.
	 */
	public static DbObjectType fromString(String name) {
		if (name == null)
			return null;

		return (DbObjectType) m_types.get(name);
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
		return m_display;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_name = (String) in.readObject();
		m_display = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_name);
		out.writeObject(m_display);
	}

}
