package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.i18n.I18N;

/**
 * Class represents a schema in a database
 * 
 * @author Jeff Tassin
 */
public class Schema implements Comparable, JETAExternalizable {
	/** serialize id */
	static final long serialVersionUID = 3018729626463201234L;

	public static int VERSION = 1;

	/** the name for this schema */
	private String m_name;

	private String m_hash;

	/** for those databases that don't support schemas */
	private boolean m_bvirtual = false;

	public static final Schema VIRTUAL_SCHEMA;
	public static final Schema EMPTY_SCHEMA;

	static {
		VIRTUAL_SCHEMA = new Schema(I18N.getLocalizedMessage("Default"));
		VIRTUAL_SCHEMA.m_bvirtual = true;
		EMPTY_SCHEMA = new Schema("");
	}

	/**
	 * ctor only for serializatoin
	 */
	public Schema() {

	}

	/**
	 * Only allow package access. Use DbModel.getSchemaInstance if you want to
	 * create a schema object from a name.
	 */
	public Schema(String name) {
		m_name = name;
		if (m_name != null)
			m_hash = m_name.toLowerCase();
	}

	public int compareTo(Object obj) {
		if (obj == this)
			return 0;
		else if (obj instanceof Schema) {
			Schema sch = (Schema) obj;
			int result = m_name.compareToIgnoreCase(sch.m_name);
			return result;
		} else
			return -1;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (obj instanceof Schema)
			return m_name.equalsIgnoreCase(((Schema) obj).m_name);
		else
			return false;
	}

	public String getName() {
		return m_name;
	}

	public String getMetaDataSearchParam() {
		if (this == VIRTUAL_SCHEMA)
			return null;
		else
			return getName();
	}

	public int hashCode() {
		if (isValid())
			return m_hash.hashCode();
		else
			return super.hashCode();
	}

	/**
	 * @return true if this schema has a valid name or not
	 */
	public boolean isValid() {
		if (m_hash != null)
			return true;
		else
			return false;
	}

	/**
	 * You absolutely need this when deserializing this class classes
	 */
	private Object readResolve() throws ObjectStreamException {
		if (m_bvirtual)
			return VIRTUAL_SCHEMA;
		else
			return this;
	}

	public String toString() {
		return m_name;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_name = (String) in.readObject();
		m_hash = (String) in.readObject();
		m_bvirtual = in.readBoolean();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_name);
		out.writeObject(m_hash);
		out.writeBoolean(m_bvirtual);
	}

}
