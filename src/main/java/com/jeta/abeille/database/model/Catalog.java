package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.HashMap;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * Class represents a Catalog in a database
 * 
 * @author Jeff Tassin
 */
public class Catalog implements Comparable, JETAExternalizable {
	/** serialize id */
	static final long serialVersionUID = -3911536336350256900L;

	public static int VERSION = 1;

	/** the name for this schema */
	private String m_name;

	private String m_hash;

	/** catalog is an immutable object, so let's cache them */
	private static HashMap m_catalogs = new HashMap();

	/** for those databases that don't support catalogs */
	private boolean m_bvirtual = false;

	/**
	 * catalog for those databases that don't support catalogs (e.g. HSQLDB)
	 */
	public static final Catalog VIRTUAL_CATALOG;
	public static final Catalog EMPTY_CATALOG = new Catalog("");

	static {
		VIRTUAL_CATALOG = new Catalog("");
		VIRTUAL_CATALOG.m_bvirtual = true;
	}

	public Catalog() {

	}

	/**
    */
	private Catalog(String name) {
		m_name = name;
		assert (m_name != null);
		if (m_name != null)
			m_hash = m_name.toLowerCase();
	}

	/**
	 * Factory
	 */
	public synchronized static Catalog createInstance(String name) {
		if (name == null)
			name = "";

		Catalog cat = (Catalog) m_catalogs.get(name);
		if (cat == null) {
			cat = new Catalog(name);
			m_catalogs.put(name, cat);
		}
		return cat;
	}

	public int compareTo(Object obj) {
		if (obj == this)
			return 0;
		else if (obj instanceof Catalog) {
			Catalog cat = (Catalog) obj;
			int result = m_name.compareToIgnoreCase(cat.m_name);
			return result;
		} else
			return -1;
	}

	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	public String getName() {
		return m_name;
	}

	public String getDisplayName() {
		if (this == VIRTUAL_CATALOG) {
			return I18N.getLocalizedMessage("Default");
		} else {
			return m_name;
		}
	}

	public String getMetaDataSearchParam() {
		if (this == VIRTUAL_CATALOG)
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
			return VIRTUAL_CATALOG;
		else
			return this;
	}

	public String toString() {
		if (m_name == null) {
			TSUtils.printMessage("Catalog.toString()  invalid name");
		}
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
