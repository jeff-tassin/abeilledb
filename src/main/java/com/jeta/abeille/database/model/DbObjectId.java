package com.jeta.abeille.database.model;

import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class uniquely represents an object id. It includes schema and
 * objectname name.
 * 
 * @author Jeff Tassin
 */
public class DbObjectId implements Cloneable, Comparable, JETAExternalizable {

	/** serialize id */
	static final long serialVersionUID = 6563198322219095908L;

	/**
	 * The version of this class
	 */
	public static final int VERSION = 1;

	/** the type of object this id is for (TABLE, VIEW, etc.) */
	private DbObjectType m_objtype;

	/** the catalog this object belongs to */
	private Catalog m_catalog;

	/** the schema that owns the object */
	private Schema m_schema;

	/** the name for the object */
	private String m_objectName;

	/**
	 * this value is used to display the id as a string (i.e. toString() ) We
	 * keep it around so we don't need to instantiate a string every time
	 * toString is called
	 */
	private String m_displayValue;

	/**
	 * the hash code for this object. It is different from m_stringvalue because
	 * we ignore case here
	 */
	private volatile String m_hashvalue;

	/**
	 * This is a database specific object that uniquely identifies the object in
	 * the database. For example, in postgresql, the schema and object name are
	 * not enough to indentify a single function. This is because PostgreSQL
	 * support function overloading. So, we can have multiple functions with the
	 * same name but different arguments. In this case, the key is an Integer
	 * object that points to the function OID. Note, that most of the time this
	 * will be null. It depends on the context. Also, this object must always be
	 * serializable!
	 */
	private Object m_key;

	/**
	 * ctor: Creates a table id with a null catalog
	 */
	public DbObjectId() {
	}

	/**
	 * ctor: Creates a table id with a null catalog
	 */
	public DbObjectId(DbObjectType objtype, Catalog cat, Schema tSchema, String objectName) {
		assert (objtype != null);
		assert (cat != null);
		setObjectType(objtype);
		setCatalog(cat);
		setSchema(tSchema);
		setObjectName(objectName);
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;
		else if (o instanceof DbObjectId) {
			DbObjectId id = (DbObjectId) o;
			getHashValue();
			return toString().compareToIgnoreCase(id.toString());
		} else
			return -1;

	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		DbObjectId id = new DbObjectId(m_objtype, m_catalog, m_schema, m_objectName);
		return id;
	}

	public DbObjectId change(Catalog catalog) {
		DbObjectId id = new DbObjectId(m_objtype, catalog, m_schema, m_objectName);
		return id;
	}

	public DbObjectId change(Schema schema) {
		DbObjectId id = new DbObjectId(m_objtype, m_catalog, schema, m_objectName);
		return id;
	}

	public DbObjectId changeName(String name) {
		DbObjectId id = new DbObjectId(m_objtype, m_catalog, m_schema, name);
		return id;
	}

	public DbObjectId change(Catalog catalog, Schema schema) {
		DbObjectId id = new DbObjectId(m_objtype, catalog, schema, m_objectName);
		return id;
	}

	/**
	 * Comparable implementation
	 */
	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	/**
	 * Generates the string value for our hashCode
	 */
	protected void generateHash() {
		StringBuffer dval = new StringBuffer();
		StringBuffer sval = new StringBuffer();

		assert (m_catalog != null);

		if (m_catalog.getName().length() > 0) {
			sval.append(m_catalog);
			sval.append(".");
		}

		if (m_schema != null && m_schema != Schema.VIRTUAL_SCHEMA && m_schema.getName().length() > 0) {
			if (m_schema.isValid()) {
				sval.append(m_schema);
				sval.append(".");
				dval.append(m_schema);
				dval.append(".");
			}
		}
		sval.append(m_objectName);
		dval.append(m_objectName);

		setDisplayValue(dval.toString());

		sval.append(".");
		sval.append(m_objtype.toString());
		setHashValue(sval.toString().toLowerCase());
	}

	/**
	 * @return the catalog
	 */
	public Catalog getCatalog() {
		return m_catalog;
	}

	public String getHashValue() {
		if (m_hashvalue == null)
			generateHash();

		return m_hashvalue;
	}

	/**
	 * @return the database key for this object
	 */
	public Object getKey() {
		return m_key;
	}

	/**
	 * @return the type of object this is an ID for
	 */
	public DbObjectType getObjectType() {
		return m_objtype;
	}

	/**
	 * @return the schema for this object
	 */
	public Schema getSchema() {
		return m_schema;
	}

	/**
	 * @return the name of the schema that this object belongs to
	 */
	public String getSchemaName() {
		return m_schema.toString();
	}

	// @todo make more efficient
	public String getFullyQualifiedName() {
		StringBuffer buff = new StringBuffer();
		Catalog catalog = getCatalog();
		assert (catalog != null);

		if (catalog.getName().length() > 0) {
			buff.append(catalog.getName());
			buff.append(".");
		}

		if (m_schema != Schema.VIRTUAL_SCHEMA) {
			if (m_schema.getName().length() > 0) {
				buff.append(m_schema.getName());
				buff.append(".");
			}
		}
		buff.append(getObjectName());
		return buff.toString();
	}

	public String getObjectName() {
		return m_objectName;
	}

	public int hashCode() {
		return getHashValue().hashCode();
	}

	/**
	 * Debugging
	 */
	public void print() {
		System.out.println(toString());
	}

	void setCatalog(Catalog cat) {
		m_catalog = cat;
		m_hashvalue = null;
	}

	protected void setDisplayValue(String value) {
		m_displayValue = value;
	}

	/**
	 * Sets the database key for this object
	 */
	public void setKey(Object key) {
		m_key = key;
	}

	void setSchema(Schema tSchema) {
		m_schema = tSchema;
		m_hashvalue = null;
	}

	protected void setHashValue(String hash) {
		m_hashvalue = hash;
	}

	void setObjectName(String tableName) {
		if (tableName == null)
			m_objectName = "";
		else
			m_objectName = tableName;

		m_hashvalue = null;
	}

	void setObjectType(DbObjectType objtype) {
		m_objtype = objtype;
	}

	public String toString() {
		// getHashValue will generate the display value if it is null
		getHashValue();
		return m_displayValue;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_objtype = (DbObjectType) in.readObject();
		m_catalog = (Catalog) in.readObject();
		m_schema = (Schema) in.readObject();
		m_objectName = (String) in.readObject();
		m_displayValue = (String) in.readObject();
		m_key = in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_objtype);
		out.writeObject(m_catalog);
		out.writeObject(m_schema);
		out.writeObject(m_objectName);
		out.writeObject(m_displayValue);
		out.writeObject(m_key);
	}

}
