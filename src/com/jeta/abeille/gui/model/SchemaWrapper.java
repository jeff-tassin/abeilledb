package com.jeta.abeille.gui.model;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

/**
 * Acts as a wrapper around a schema for the Object Tree model
 * 
 * @author Jeff Tassin
 */
public class SchemaWrapper implements Comparable {

	/**
	 * The database connection
	 */
	private TSConnection m_connection;

	/**
	 * the catalog this schema belongs to
	 */
	private Catalog m_catalog;

	/**
	 * the schema
	 */
	private Schema m_schema;

	/**
	 * The node in the tree that is associated with this wrapper object
	 */
	private ObjectTreeNode m_node;

	/**
	 * ctor
	 */
	public SchemaWrapper(TSConnection conn, Catalog catalog, Schema schema, ObjectTreeNode node) {
		m_connection = conn;
		m_catalog = catalog;
		m_schema = schema;
		m_node = node;
	}

	/**
	 * Comparable interface. Tree folder always come before other objects
	 */
	public int compareTo(Object obj) {
		if (obj instanceof SchemaWrapper) {
			SchemaWrapper wrapper = (SchemaWrapper) obj;
			if (m_catalog == null || m_schema == null) {
				return -1;
			} else {

				int result = m_catalog.compareTo(wrapper.getCatalog());
				if (result == 0)
					result = m_schema.compareTo(wrapper.getSchema());

				return result;
			}
		} else
			return -1;
	}

	public Catalog getCatalog() {
		return m_catalog;
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	public DbModel getModel() {
		return m_connection.getModel(m_catalog);
	}

	/**
	 * @returns the name of this schema
	 */
	public String getName() {
		return m_schema.getName();
	}

	/**
	 * @returns the name of this catalog
	 */
	public String getDisplayName() {
		return m_schema.getName();
	}

	public Schema getSchema() {
		return m_schema;
	}

	public boolean isLoaded() {
		// if ( m_node.getChildCount() == 1 )
		// {
		// ObjectTreeNode node = (ObjectTreeNode)m_node.getChildAt(0);
		// if ( node instanceof EmptyTreeNode )
		// return false;
		// }

		return true;
	}

	public boolean isLoading() {
		DbModel model = m_connection.getModel(m_catalog);
		if (model == null)
			return false;
		else
			return model.isLoading(m_schema);
	}

	/**
	 * toString
	 */
	public String toString() {
		return getName();
	}

}
