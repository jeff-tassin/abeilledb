package com.jeta.abeille.database.model;

import java.lang.ref.WeakReference;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.interfaces.resources.ResourceLoader;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.EmptyCollection;
import com.jeta.foundation.utils.TSUtils;

/**
 * 
 * @author Jeff Tassin
 */
public class DbModel implements DbTableModel {

	/** the catalog that contains this model */
	private Catalog m_catalog;

	/**
	 * Contains the metadata for a fully read model
	 */
	private HashMap m_tables = new HashMap();

	/** A cached list of schemas */
	private TreeSet m_schemas = new TreeSet();

	/** The database connection for this model */
	private TSConnection m_connection;

	/** a list of model listeners that want model change events */
	private LinkedList m_listeners = new LinkedList();

	/** a dummy model flag for invalid catalogs */
	private boolean m_emptymodel = false;

	/**
	 * ctor
	 */
	public DbModel(TSConnection conn, Catalog cat) {
		m_connection = conn;
		m_catalog = cat;
	}

	/**
	 * This model is an empty model used when a caller requests a model for a
	 * catalog that does not exist. This is so we don't have to check for nulls
	 * everywhere.
	 */
	DbModel(TSConnection conn) {
		this(conn, Catalog.createInstance(""));
		m_emptymodel = true;
	}

	/**
	 * Adds a listener that wants events from this model
	 */
	public void addListener(DbModelListener listener) {
		synchronized (m_listeners) {
			// make sure we don't already have the listener
			Iterator iter = m_listeners.iterator();
			while (iter.hasNext()) {
				WeakReference wref = (WeakReference) iter.next();
				DbModelListener l = (DbModelListener) wref.get();
				if (l == null) {
					iter.remove();
				} else {
					if (l == listener) {
						System.out.println("DbModel.addListener  listener already registered... not adding");
						return;
					}
				}
			}
			m_listeners.add(new WeakReference(listener));
		}
	}

	/**
	 * Called by the TSConnection when the user closes a connection. Allows us
	 * to release any references/resources
	 */
	void close() {
		m_connection = null;
		LinkedList tables = new LinkedList();
		synchronized (this) {
			tables.addAll(m_tables.values());
			m_tables.clear();
		}

		Iterator iter = tables.iterator();
		while (iter.hasNext()) {
			ModelData mdata = (ModelData) iter.next();
			mdata.close();
		}

		tables.clear();
	}

	/**
	 * @return true if the given schema is available for the current connection
	 */
	public boolean contains(Schema schema) {
		if (schema == null)
			return false;

		return m_schemas.contains(schema);
	}

	/**
	 * Notifies all listeners of a model event
	 */
	public void fireEvent(DbModelEvent event) {
		// System.out.println( "DbModel.fireEvent entered" );
		synchronized (m_listeners) {
			final DbModelEvent evt = event;
			final Collection listeners = (java.util.Collection) m_listeners.clone();
			Runnable gui_update = new Runnable() {
				public void run() {
					int count = 1;
					Iterator iter = listeners.iterator();
					while (iter.hasNext()) {
						WeakReference wref = (WeakReference) iter.next();
						DbModelListener listener = (DbModelListener) wref.get();
						if (listener == null) {
							synchronized (m_listeners) {
								m_listeners.remove(wref);
							}
						} else {
							try {
								listener.eventFired(evt);
								count++;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			};

			javax.swing.SwingUtilities.invokeLater(gui_update);
		}
	}

	/**
	 * This method will send a DbModelEvent.STATUS_UPDATE message to any
	 * listeners. This allows those listeners to make status request queries
	 * against the database. This is typically done after a commit/rollback
	 * command somewhere in the system.
	 * 
	 */
	public void fireStatusUpdate() {
		fireEvent(new DbModelEvent(this, DbModelEvent.STATUS_UPDATE));
	}

	public Catalog getCatalog() {
		return m_catalog;
	}

	/**
	 * @return the connection manager associated with the model
	 */
	public TSConnection getConnection() {
		assert (m_connection != null);
		return m_connection;
	}

	/**
	 * @return the model data object for the given schema. If the object is not
	 *         found in the cache, it is created
	 */
	private ModelData getModelData(Schema schema) {
		synchronized (this) {
			assert (schema != null);
			ModelData mdata = (ModelData) m_tables.get(schema);
			if (mdata == null) {
				mdata = new ModelData(m_connection, this, m_catalog, schema);
				m_tables.put(schema, mdata);
			}
			return mdata;
		}
	}

	public String getName() {
		return m_catalog.getName();
	}

	/**
	 * @param schemaName
	 *            the name of the schema to return
	 * @return a schema object for a given name. We have this method mainly to
	 *         support databases that don't have schemas. If the name is null or
	 *         empty, we return the VIRTUAL_SCHEMA in this case.
	 */
	public Schema getSchemaInstance(String schemaName) {
		if (schemaName == null || schemaName.trim().length() == 0)
			return Schema.VIRTUAL_SCHEMA;
		else
			return new Schema(schemaName);
	}

	/**
	 * @return a set of Schema objects
	 */
	public Collection getSchemas() {
		if (m_emptymodel) {
			return EmptyCollection.getInstance();
		}

		synchronized (m_schemas) {
			if (m_schemas.size() == 0) {
				try {
					TSDatabase dbase = (TSDatabase) m_connection.getImplementation(TSDatabase.COMPONENT_ID);
					if (dbase.supportsSchemas()) {
						DatabaseMetaData metadata = m_connection.getMetaDataConnection().getMetaData();
						ResultSet sset = metadata.getSchemas();
						while (sset.next()) {
							String sname = sset.getString("TABLE_SCHEM");
							if (m_connection.supportsCatalogs() && !Database.SYBASE.equals(m_connection.getDatabase())) {
								try {
									String catalog = sset.getString("TABLE_CATALOG");
									if (I18N.equals(catalog, m_catalog.getName())) {
										m_schemas.add(new Schema(sname));
									}
								} catch (SQLException se) {
									m_schemas.add(new Schema(sname));
									TSUtils.printException(se);
								}
							} else {
								m_schemas.add(new Schema(sname));
							}
						}
					} else {
						m_schemas.add(Schema.VIRTUAL_SCHEMA);
					}
				} catch (SQLException se) {
					TSUtils.printException(se);
				}
			}
		}
		return m_schemas;
	}

	/**
	 * @return a list of TableId objects for the given Schema
	 */
	public TreeSet getTables(Schema schema, String tableType) {
		join(schema);
		ModelData modeldata = getModelData(schema);
		return modeldata.getTables(tableType);
	}

	/**
	 * @return a list of TableId objects for the given Schema
	 */
	public TreeSet getTables(Schema schema) {
		join(schema);
		ModelData modeldata = getModelData(schema);
		return modeldata.getTables();
	}

	/**
	 * @return the table metadata for the given table id. If the table is not
	 *         found in the model cache, it is loaded from the database. If this
	 *         fails, null is returned
	 */
	public TableMetaData getTable(TableId tableId) {
		if (m_emptymodel)
			return null;

		if (TSUtils.isDebug()) {
			if (!m_catalog.equals(tableId.getCatalog())) {
				TSUtils.printMessage("******* ERROR:  DbModel.getTable failed. TableId catalog: "
						+ tableId.getCatalog() + " does not equal model catalog: " + m_catalog);
				return null;
			}
		}

		if (tableId == null)
			return null;

		// join( tableId.getSchema() );
		ModelData modeldata = getModelData(tableId.getSchema());
		// assert( modeldata.getState() == ModelData.LOADED );

		String tablename = tableId.getTableName();
		if (tablename == null || tablename.trim().length() == 0)
			return null;

		TableMetaData tmd = modeldata.getTable(tableId);
		if (tmd != null) {
			if (TSUtils.isDebug()) {
				tmd.validate();
			}
		}

		return tmd;
	}

	/**
	 * @return the table metadata for the given table id. If the table is not
	 *         found in the model cache, it is loaded from the database. If this
	 *         fails, null is returned
	 */
	public TableMetaData getTableEx(TableId tableId, int flag) {
		assert (tableId.getCatalog() != null);
		TableMetaData tmd = getTable(tableId);
		if (tmd != null) {
			try {
				ModelData modeldata = getModelData(tableId.getSchema());

				if ((flag & TableMetaData.LOAD_FOREIGN_KEYS) > 0) {
					int currentmask = tmd.getLoadMask();
					if ((currentmask & TableMetaData.LOAD_FOREIGN_KEYS) == 0) {
						modeldata.loadForeignKeys(tmd);
						tmd.ORLoadMask(TableMetaData.LOAD_FOREIGN_KEYS);
					}
				}

				if ((flag & TableMetaData.LOAD_COLUMNS_EX) > 0) {
					int currentmask = tmd.getLoadMask();
					if ((currentmask & TableMetaData.LOAD_COLUMNS_EX) == 0) {
						modeldata.loadColumnsEx(tmd);
						tmd.ORLoadMask(TableMetaData.LOAD_COLUMNS_EX);
					}
				}

				if ((flag & TableMetaData.LOAD_EXPORTED_KEYS) > 0) {
					int currentmask = tmd.getLoadMask();

					if ((currentmask & TableMetaData.LOAD_EXPORTED_KEYS) == 0) {
						modeldata.loadExportedKeys(tmd);
						tmd.ORLoadMask(TableMetaData.LOAD_EXPORTED_KEYS);
					}
				}

			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}
		return tmd;
	}

	/**
	 * Trys to get the table metadata from the cache *EVEN* if the metadata is
	 * not fully loaded. We use this primarily to detect if a table is a view or
	 * not. We know a table is a view or not even if we haven't loaded any
	 * columns. If the table is not in the cache, it is loaded.
	 * 
	 * @return the table metadata for the given table id. If the table is not
	 *         found in the model cache, it is loaded from the database. If this
	 *         fails, null is returned
	 */
	public TableMetaData getTableFast(TableId tableId) {
		if (m_emptymodel)
			return null;

		ModelData modeldata = getModelData(tableId.getSchema());
		return modeldata.getTableFast(tableId);
	}

	/**
	 * This method blocks in a modal loop until the model is loaded. If the
	 * model is not in the process of being loaded, then the process is stared.
	 * 
	 */
	public void join(Schema schema) {
		ModelData modeldata = getModelData(schema);
		modeldata.join();
	}

	/**
	 * @return a flag indicating if this model is currently being loaded in a
	 *         background thread. We do this because some data models might have
	 *         a large number of tables
	 */
	public synchronized boolean isLoading(Schema schema) {
		assert (schema != null);
		ModelData modeldata = getModelData(schema);
		return modeldata.isLoading();
	}

	/**
	 * @return a flag indicating if this model has been loaded (i.e. all tables
	 *         read )
	 */
	public synchronized boolean isLoaded(Schema schema) {
		assert (schema != null);
		ModelData modeldata = getModelData(schema);
		return modeldata.isLoaded();
	}

	/**
	 * Sets the loading flag and notifies any blocked threads that the model is
	 * read
	 */
	void loadingCompleted(ModelData modeldata) {
		ModelData old_model_data = null;
		synchronized (this) {
			try {
				old_model_data = (ModelData) m_tables.get(modeldata.getSchema());
				m_tables.put(modeldata.getSchema(), modeldata);
				m_connection.setAutoCommit(m_connection.getMetaDataConnection(), m_connection.getDefaultAutoCommit());
			} catch (Exception e) {
				TSUtils.printStackTrace(e);
			}
		}
		try {
			synchronized (modeldata) {
				modeldata.notifyAll();
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		/**
		 * this handles the case when the user selects a reload. Instead of
		 * trying to handle synchronization for any clients that want metadata
		 * during the reload, we simply replace the old model data object with
		 * the new one. If any clients are waiting on a join, then let's notify
		 * them. They may get old meta data in this case (this really should not
		 * happen though)
		 */
		try {
			if (old_model_data != null) {
				synchronized (old_model_data) {
					old_model_data.notifyAll();
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
		Schema schema = modeldata.getSchema();
		assert (schema != null);
		if (schema != null) {
			DbModelEvent evt = new DbModelEvent(this, DbModelEvent.SCHEMA_LOADED);
			evt.setSchema(schema);
			fireEvent(evt);
		}

	}

	/**
	 * Called when the user wants to reload a model. This might happen if a
	 * table was added or deleted using sql or by another client and the user
	 * wants this application to see the latest table list.
	 */
	public void reload(Schema schema) {
		if (m_emptymodel)
			return;

		resetReadOnlyConnection();

		ModelData modeldata = getModelData(schema);
		synchronized (this) {
			int model_state = modeldata.getState();
			if (model_state != ModelData.CURRENTLY_LOADING) {
				TSUtils.printMessage("DbModel.constructing new model data for schema: " + schema);
				/**
				 * when the model data is finished loading, it will post a
				 * message back to this object
				 */
				modeldata = new ModelData(m_connection, this, m_catalog, schema);
			}
		}
		TSUtils.printMessage("DbModel.reload : " + schema + "  modeldata: " + modeldata.hashCode());
		modeldata.join();
	}

	/**
	 * Call when a table has been changed and we need to reload any cached
	 * values. This method also sends a message to the notifier server
	 */
	public void reloadTable(TableId id) {

		if (TSUtils.isDebug()) {
			if (id == null) {
				TSUtils.printMessage("Error DbModel.reloadTable  id = 0");
				Exception e = new Exception();
				e.printStackTrace();
			}

			TSUtils.printMessage("DbModel.reloadTable   id: " + id);
		}

		assert (id != null);
		if (id != null) {
			ModelData modeldata = getModelData(id.getSchema());
			assert (modeldata.isLoaded());
			modeldata.removeTable(id);
			fireEvent(new DbModelEvent(this, DbModelEvent.TABLE_CHANGED, id));
		}
	}

	/**
	 * Removes a listener that wants events from this model
	 */
	public void removeListener(DbModelListener listener) {
		synchronized (m_listeners) {

			Iterator iter = m_listeners.iterator();
			while (iter.hasNext()) {
				WeakReference wref = (WeakReference) iter.next();
				DbModelListener ml = (DbModelListener) wref.get();
				if (ml == null || ml == listener) {
					iter.remove();
				}
			}
		}
	}

	/**
	 * Call when a table name has changed. Removes the table from the cache and
	 * fires an event
	 */
	public void renameTable(TableId newid, TableId oldid) {
		Schema schema = newid.getSchema();
		ModelData modeldata = getModelData(schema);
		assert (modeldata.isLoaded());
		modeldata.join();
		modeldata.renameTable(newid, oldid);
		fireEvent(new DbModelEvent(this, newid, oldid));
	}

	/**
	 * Resets the read only connection so the model can see any changes to the
	 * database made by other connections
	 */
	public void resetReadOnlyConnection() {
		try {
			m_connection.resetReadOnlyConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the underlying connection for this model. Currently called by
	 * TSConnection.setModel
	 */
	void setConnection(TSConnection conn) {
		m_connection = conn;
	}

	/**
	 * Call when a table has been created (e.g. with the commit table in the
	 * ModelView). The table should already be added to the database. This
	 * method simply fires a CREATED EVENT, which allows other components to
	 * update themselves accordingly
	 */
	public void tableCreated(TableMetaData tmd) {
		Schema schema = tmd.getTableId().getSchema();
		ModelData modeldata = getModelData(schema);
		assert (modeldata.isLoaded());
		modeldata.join();

		modeldata.addTable(schema, tmd);
		fireEvent(new DbModelEvent(this, DbModelEvent.TABLE_CREATED, tmd.getTableId()));
	}

	/**
	 * Call when a table has been dropped. The table should already be removed
	 * from the database. This method simply removes the table from the cache
	 * and fires an event to any model listeners Removes the table from the
	 * cache and fires an event
	 */
	public void tableDropped(TableId tableId) {
		Schema schema = tableId.getSchema();
		ModelData modeldata = getModelData(schema);

		modeldata.join();

		modeldata.removeTable(tableId);
		fireEvent(new DbModelEvent(this, DbModelEvent.TABLE_DELETED, tableId));
	}

	/**
	 * Call when a table has been dropped. The table should already be removed
	 * from the database. This method simply removes the table from the cache
	 * and DOES NOT fire an event to any model listeners
	 */
	public void tableDroppedSilent(TableId tableId) {
		Schema schema = tableId.getSchema();
		ModelData modeldata = getModelData(schema);
		assert (modeldata.isLoaded());
		modeldata.join();

		modeldata.removeTable(tableId);
	}

}
