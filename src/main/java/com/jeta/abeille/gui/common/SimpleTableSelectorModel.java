package com.jeta.abeille.gui.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.SortedListModel;

/**
 * This is a model used by the table selector dialog. This model allows the user
 * to pass in a set of tables that will make up the selection.
 * 
 * @author Jeff Tassin
 */
public class SimpleTableSelectorModel implements TableSelectorModel {
	private TSConnection m_connection;

	/** a set of tableids */
	private TreeSet m_tables = new TreeSet();

	private Catalog m_current_catalog;

	private Schema m_current_schema;

	/**
	 * ctor: Loads all schemas and tables
	 */
	public SimpleTableSelectorModel(TSConnection connection, Collection tableIds) {
		m_connection = connection;
		m_tables.addAll(tableIds);
	}

	/**
	 * @return the default schema
	 */
	public Schema getCurrentSchema(Catalog cat) {
		if (m_current_schema == null)
			return m_connection.getCurrentSchema(cat);
		else
			return m_current_schema;
	}

	/**
	 * @return the opened databases in the connection
	 */
	public Collection getCatalogs() {
		TreeSet cats = new TreeSet();
		Iterator iter = m_tables.iterator();
		while (iter.hasNext()) {
			TableId tableid = (TableId) iter.next();
			cats.add(tableid.getCatalog());
		}
		return cats;
	}

	/**
	 * @return the connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	public Catalog getCurrentCatalog() {
		if (m_current_catalog == null)
			return m_connection.getCurrentCatalog();
		else
			return m_current_catalog;
	}

	/**
	 * @return the collection of available schemas in this model.
	 */
	public Collection getSchemas(Catalog cat) {
		TreeSet schemas = new TreeSet();
		Iterator iter = m_tables.iterator();
		while (iter.hasNext()) {
			TableId tableid = (TableId) iter.next();
			if (cat.equals(tableid.getCatalog())) {
				schemas.add(tableid.getSchema());
			}
		}
		return schemas;
	}

	/**
	 * @return a table metadata object for the given table name
	 */
	public TableMetaData getTable(TableId tableid) {
		if (m_tables.contains(tableid))
			return m_connection.getTable(tableid);
		else
			return null;
	}

	/**
	 * @return a table metadata object for the given table name
	 */
	public TableMetaData getTableEx(TableId tableid, int flag) {
		if (m_tables.contains(tableid)) {
			DbModel model = m_connection.getModel(tableid.getCatalog());
			if (model == null) {
				return null;
			} else {
				return model.getTableEx(tableid, flag);
			}
		} else
			return null;
	}

	/**
	 * @return the selected tables ( TableId objects) for the given schema
	 */
	public SortedListModel getTables(Catalog catalog, Schema schema) {
		TreeSet tables = new TreeSet();
		Iterator iter = m_tables.iterator();
		while (iter.hasNext()) {
			TableId tableid = (TableId) iter.next();
			if (catalog.equals(tableid.getCatalog()) && schema.equals(tableid.getSchema())) {
				tables.add(tableid);
			}
		}
		return new SortedListModel(tables);
	}

	public void setCurrentCatalog(Catalog catalog) {
		m_current_catalog = catalog;
	}

	public void setCurrentSchema(Schema schema) {
		m_current_schema = schema;
	}
}
