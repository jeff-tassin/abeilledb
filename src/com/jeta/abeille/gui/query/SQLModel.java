package com.jeta.abeille.gui.query;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * This is the model for a SQL string that the user stores in the QueryTree
 * 
 * @author Jeff Tassin
 */
public class SQLModel implements JETAExternalizable {
	static final long serialVersionUID = -3227089293947067308L;

	public static int VERSION = 1;

	/** The id for this query */
	private DbObjectId m_id;

	/** The display name */
	private String m_displayName;

	/**
	 * The SQL
	 */
	private String m_sql;

	/**
	 * Model key
	 */
	private static final String SQL_MODEL = "jeta.abeille.gui.query.sqlmodel";

	/**
	 * default ctor for serialization
	 */
	public SQLModel() {

	}

	/**
	 * The catalog that owns this query
	 */
	public Catalog getCatalog() {
		if (m_id == null) {
			assert (false);
			return null;
		} else {
			return m_id.getCatalog();
		}
	}

	/**
	 * @return the user defined name for this query
	 */
	public String getName() {
		return m_displayName;
	}

	/**
	 * The schema that owns this query
	 */
	public Schema getSchema() {
		if (m_id == null) {
			assert (false);
			return null;
		} else {
			return m_id.getSchema();
		}
	}

	/**
	 * @return the sql for this query
	 */
	public String getSQL() {
		return m_sql;
	}

	/**
	 * @return the key used to identify this model in the object store
	 */
	public String getStoreKey() {
		return SQLModel.getStoreKey(m_id.getObjectName());
	}

	/**
	 * @return the store key for a given query uid
	 */
	public static String getStoreKey(String uid) {
		String key = SQL_MODEL + "." + uid;
		return key;
	}

	/**
	 * Creates an instance of a query model. If the model is found in the data
	 * store it is loaded. If the model is not found, a new instance is created.
	 * The tag is used to uniquely indentify the model
	 * 
	 * @param connection
	 *            the database connection
	 * @param tag
	 *            the query identifier
	 */
	public static SQLModel loadInstance(TSConnection connection, DbObjectId id) {
		ObjectStore os = connection.getObjectStore();
		String storename = SQLModel.getStoreKey(id.getObjectName());
		SQLModel model = null;
		try {
			model = (SQLModel) os.load(storename);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}

		if (model == null) {
			// if the query is not found, let's create one
			model = new SQLModel();
		}
		model.setId(id);
		return model;
	}

	/**
	 * Saves this query
	 */
	public void save(TSConnection connection) {
		ObjectStore os = connection.getObjectStore();
		try {
			os.store(getStoreKey(), this);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Sets the catalog and schema for this proxy
	 */
	public void set(Catalog catalog, Schema schema) {
		m_id = m_id.change(catalog, schema);
	}

	/**
	 * Sets this form's id
	 * 
	 * @param objid
	 *            the id (schema.name) of the form to set
	 */
	public void setId(DbObjectId objid) {
		m_id = objid;
	}

	/**
	 * Sets this query's name
	 * 
	 * @param name
	 *            the name of the query to set
	 */
	public void setName(String name) {
		m_displayName = name;
	}

	/**
	 * Sets the sql
	 */
	public void setSQL(String sql) {
		m_sql = sql;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_id = (DbObjectId) in.readObject();
		m_displayName = (String) in.readObject();
		m_sql = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_id);
		out.writeObject(m_displayName);
		out.writeObject(m_sql);
	}

}
