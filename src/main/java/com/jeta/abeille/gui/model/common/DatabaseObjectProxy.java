package com.jeta.abeille.gui.model.common;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;

public interface DatabaseObjectProxy {
	/**
	 * @return the catalog that owns the object associated with this proxy
	 */
	public Catalog getCatalog();

	/**
	 * @return the schema that owns the object associated with this proxy
	 */
	public Schema getSchema();

	/**
	 * @return the underlying data model
	 */
	public Object getModel();

	/**
	 * @return the name for this object
	 */
	public String getName();

	/**
	 * Sets the catalog and schema for this proxy
	 */
	public void set(Catalog catalog, Schema schema);

}
