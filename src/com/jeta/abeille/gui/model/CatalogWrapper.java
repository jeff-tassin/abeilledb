package com.jeta.abeille.gui.model;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

/**
 * Acts as a wrapper around a catalog for the Object Tree model
 * 
 * @author Jeff Tassin
 */
public class CatalogWrapper implements Comparable {

	/**
	 * The database connection
	 */
	private TSConnection m_connection;

	/**
	 * the catalog
	 */
	private Catalog m_catalog;

	/**
	 * The node in the tree that is associated with this wrapper object
	 */
	private ObjectTreeNode m_node;

	/**
	 * ctor
	 */
	public CatalogWrapper(TSConnection conn, Catalog catalog, ObjectTreeNode node) {
		m_connection = conn;
		m_catalog = catalog;
		m_node = node;
	}

	/**
	 * Comparable interface. Tree folder always come before other objects
	 */
	public int compareTo(Object obj) {
		if (obj instanceof CatalogWrapper) {
			CatalogWrapper folder = (CatalogWrapper) obj;
			if (m_catalog == null)
				return -1;
			return m_catalog.compareTo(obj);
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
	 * @returns the name of this catalog
	 */
	public String getName() {
		return m_catalog.getName();
	}

	/**
	 * @returns the name of this catalog
	 */
	public String getDisplayName() {
		return m_catalog.getDisplayName();
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
			return model.isLoading(Schema.VIRTUAL_SCHEMA);
	}

	/**
	 * toString
	 */
	public String toString() {
		return getName();
	}

}
