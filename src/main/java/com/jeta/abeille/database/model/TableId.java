package com.jeta.abeille.database.model;

import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class uniquely represents a table in a database. It includes schema and
 * table name.
 * 
 * @author Jeff Tassin
 */
public class TableId extends DbObjectId implements Cloneable, Comparable, JETAExternalizable, DatabaseObject {
	/** serialize id */
	static final long serialVersionUID = 6961783474489630933L;

	public static int VERSION = 1;

	public TableId() {

	}

	public TableId(Catalog catalog, Schema tSchema, String tableName) {
		super(DbObjectType.TABLE, catalog, tSchema, tableName);
	}

	public DbObjectId change(TSConnection conn, Catalog catalog, Schema schema) {
		TableId id = new TableId(catalog, schema, getTableName());
		return id;
	}

	public DbObjectId change(Catalog catalog, Schema schema) {
		TableId id = new TableId(catalog, schema, getTableName());
		return id;
	}

	public DbObjectId change(Catalog catalog) {
		TableId id = new TableId(catalog, getSchema(), getTableName());
		return id;
	}

	public DbObjectId change(Schema schema) {
		TableId id = new TableId(getCatalog(), schema, getTableName());
		return id;
	}

	public DbObjectId changeName(String name) {
		TableId id = new TableId(getCatalog(), getSchema(), name);
		return id;
	}

	/**
	 * Sets the connection for this table id
	 */
	public TableId change(TSConnection conn) {
		return this;
	}

	/**
	 * Comparable Implementation
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;
		else if (o instanceof TableId) {
			TableId id = (TableId) o;
			return getHashValue().compareToIgnoreCase(id.getHashValue());
		} else
			return -1;
	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		TableId id = new TableId(getCatalog(), getSchema(), getTableName());
		return id;
	}

	// @todo make more efficient
	public String getFullyQualifiedName() {
		StringBuffer buff = new StringBuffer();
		Catalog catalog = getCatalog();
		if (catalog.getName().length() > 0) {
			buff.append(catalog.getName());
			buff.append(".");
		}

		Schema schema = getSchema();
		if (schema != Schema.VIRTUAL_SCHEMA) {
			if (schema.getName().length() > 0) {
				buff.append(schema.getName());
				buff.append(".");
			}
		}
		buff.append(getObjectName());
		return buff.toString();
	}

	/**
	 * Database object implementation
	 */
	public DbObjectId getObjectId() {
		return this;
	}

	public String getTableName() {
		return getObjectName();
	}

	/**
	 * Generates the string value for our hashCode
	 */
	protected void generateHash() {
		StringBuffer dval = new StringBuffer();
		StringBuffer sval = new StringBuffer();

		Catalog catalog = getCatalog();
		assert (catalog != null);

		if (catalog.getName().length() > 0) {
			sval.append(catalog);
			sval.append(".");
		}

		Schema schema = getSchema();
		if (schema != null && schema != Schema.VIRTUAL_SCHEMA && schema.getName().length() > 0) {
			if (schema.isValid()) {
				sval.append(schema);
				sval.append(".");
				dval.append(schema);
				dval.append(".");
			}
		}

		String tablename = getTableName();
		sval.append(tablename);
		dval.append(tablename);

		setDisplayValue(dval.toString());

		sval.append(".");
		sval.append(getObjectType().toString());

		setHashValue(sval.toString().toLowerCase());
	}

	/**
	 * Debugging
	 */
	public void print() {
		System.out.println(toString());
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		super.readExternal(in);
		int version = in.readInt();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
	}

}
