package com.jeta.abeille.gui.common;

import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdComparator;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.SortedListModel;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is a model used by the table selector dialog. This model includes all
 * schemas and tables visible to the application.
 * 
 * @author Jeff Tassin
 */
public class DefaultTableSelectorModel implements TableSelectorModel {
	private TSConnection m_connection;

	/**
	 * this is a cache of database tables for a given catalog/schema. We use the
	 * cache so we don't need re-create the table list every time the user needs
	 * to access the table selector panel. Our cache is keyed on the hashCode of
	 * the tables object in the model. If the model is reloaded, the hash code
	 * changes, so we can reload as well
	 */
	private static HashMap m_cache = new HashMap();

	/**
	 * ctor: Loads all schemas and tables
	 */
	public DefaultTableSelectorModel(TSConnection connection) {
		assert (connection != null);
		m_connection = connection;
	}

	/**
	 * @return the connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the default schema
	 */
	public Schema getCurrentSchema(Catalog catalog) {
		Schema result = m_connection.getCurrentSchema(catalog);
		assert (result != null);
		return result;
	}

	/**
	 * @return the default schema
	 */
	public Catalog getCurrentCatalog() {
		Catalog result = m_connection.getCurrentCatalog();
		assert (result != null);
		return result;
	}

	/**
	 * @return the opened databases in the connection
	 */
	public Collection getCatalogs() {
		return m_connection.getCatalogs();
	}

	/**
	 * @return the collection of available schemas in this model.
	 */
	public Collection getSchemas(Catalog cat) {
		DbModel model = m_connection.getModel(cat);
		return model.getSchemas();
	}

	/**
	 * @return a table metadata object for the given table name
	 */
	public TableMetaData getTable(TableId tableid) {
		DbModel model = m_connection.getModel(tableid.getCatalog());
		return model.getTable(tableid);

	}

	/**
	 * @return a table metadata object for the given table name
	 */
	public TableMetaData getTableEx(TableId tableid, int flag) {
		DbModel model = m_connection.getModel(tableid.getCatalog());
		return model.getTableEx(tableid, flag);
	}

	/**
	 * @return the selected tables (TableIds) for the given schema
	 */
	public SortedListModel getTables(Catalog cat, Schema schema) {
		synchronized (m_cache) {
			DbModel model = m_connection.getModel(cat);
			TreeSet treeset = model.getTables(schema);
			int hashcode = treeset.hashCode();

			StringBuffer keybuff = new StringBuffer();
			keybuff.append(m_connection.getId().getUID());
			keybuff.append('.');
			keybuff.append(cat.getName());
			keybuff.append('.');
			keybuff.append(schema.getName());

			String key = keybuff.toString();
			TableSet tableset = (TableSet) m_cache.get(key);
			if (tableset != null) {
				// check if hash code has changed for DbModel tables
				if (hashcode != tableset.getHashCode()) {
					tableset = null;
				}
			}

			if (tableset == null) {
				// TSUtils.printMessage("DefaultTableSelectorModel.getting new tables..."
				// );
				// always use case-insensitive comparator here
				TreeSet tset = new TreeSet(new TableIdComparator(m_connection, false));
				tset.addAll(treeset);
				tableset = new TableSet(hashcode, new SortedListModel(tset));
				m_cache.put(key, tableset);
			} else {
				// TSUtils.printMessage("DefaultTableSelectorModel.got cached tables..."
				// );
			}
			return tableset.getModel();
		}
	}

	/**
	 * We cache the set of tables for a given database model instead of cloning
	 * the set everytime the user wants to use the table selector panel.
	 * However, we need to know if the table set has changed. Instead of
	 * registering an event listener with the DbModel, we get the hash code for
	 * the table set returned from the model. If the hash code has changed from
	 * the last time, we generate a new tree set. Otherwise, we just return the
	 * cached value. We don't return the set directly from the DbModel because
	 * we want to use a case-insensitive tableid comparator here. Whereas in the
	 * model, a case-sensitive comparator is used of the database supports
	 * mixed-case table names.
	 */
	public static class TableSet {
		public int m_hashCode;
		public SortedListModel m_model;

		public TableSet(int hashCode, SortedListModel model) {
			m_hashCode = hashCode;
			m_model = model;
		}

		public int getHashCode() {
			return m_hashCode;
		}

		public SortedListModel getModel() {
			return m_model;
		}
	}
}
