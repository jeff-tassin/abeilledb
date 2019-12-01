package com.jeta.abeille.gui.common;

import java.sql.SQLException;

import java.util.Collection;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbTableModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.SortedListModel;

/**
 * This is a model used by various components to for selecting a table
 * 
 * @author Jeff Tassin
 */
public interface TableSelectorModel extends DbTableModel {

	/**
	 * @return the connection
	 */
	public TSConnection getConnection();

	/**
	 * @return the default schema
	 */
	public Schema getCurrentSchema(Catalog cat);

	/**
	 * @return the default catalog
	 */
	public Catalog getCurrentCatalog();

	/**
	 * @return the set of opened catalogs in the connection
	 */
	public Collection getCatalogs();

	/**
	 * @return the collection of available schemas in this model.
	 */
	public Collection getSchemas(Catalog catalog);

	/**
	 * @return the selected tables (TableIds) for the given schema
	 */
	public SortedListModel getTables(Catalog catalog, Schema schema);

	/**
	 * @return a table metadata object for the given table name
	 */
	public TableMetaData getTable(TableId tableid);

	/**
	 * @return a table metadata object for the given table name
	 */
	public TableMetaData getTableEx(TableId tableid, int flag);

}
