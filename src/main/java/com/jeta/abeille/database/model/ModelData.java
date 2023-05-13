package com.jeta.abeille.database.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.utils.JETATimer;

/**
 * 
 * @author Jeff Tassin
 */
class ModelData {
	/** the database connection */
	private TSConnection m_connection;

	/** This is the hash of TableIds (key) to TableMetaData (value) */
	private Hashtable m_tables = new Hashtable();

	/**
	 * Defines the links between tables (i.e. foreign keys) This allows us to
	 * quickly lookup relationships for any table
	 */
	private DefaultLinkModel m_linkmgr = new DefaultLinkModel();

	/**
	 * A sorted list of all tables (TableIds) in the system. Getting the sorted
	 * list of tables in the system is a very common operation for the
	 * application. So, we cache that list here. Each list contains all table
	 * ids (sorted) found in the schema
	 */
	private TreeSet m_sortedtables;

	/**
	 * The database model
	 */
	private DbModel m_dbmodel;

	/**
	 * The catalog that contains this meta data
	 */
	private Catalog m_catalog;

	/**
	 * The schema that contains this meta data
	 */
	private Schema m_schema;

	/**
	 * Flag that maintains the state of this model
	 */
	private int m_modelstate;

	static final int NOT_LOADED = 0;
	static final int CURRENTLY_LOADING = 1;
	static final int LOADED = 2;

	/**
	 * ctor
	 */
	ModelData(TSConnection connection, DbModel model, Catalog catalog, Schema schema) {
		m_connection = connection;
		m_dbmodel = model;
		m_catalog = catalog;
		m_schema = schema;
		m_sortedtables = new TreeSet(new TableIdComparator(m_connection));

	}

	/**
	 * Adds a foreign key to the given table. This includes setting up the links
	 * between the two tables
	 */
	private boolean addForeignKey(TableMetaData tmd, DbForeignKey fKey) {
		TableId tableid = tmd.getTableId();
		tmd.addForeignKey(fKey);
		Link link = new MultiColumnLink(fKey);
		synchronized (this) {
			m_linkmgr.addLink(link);
		}
		return true;
	}

	/**
	 * Adds a table to the list of tables for the given schema.
	 * 
	 * @param schema
	 *            the schema name
	 * @param tmd
	 *            the new table to add
	 */
	void addTable(Schema schema, TableMetaData tmd) {
		synchronized (this) {
			if (m_schema.equals(schema)) {
				if (m_tables.get(tmd.getTableId()) == null) {
					m_tables.put(tmd.getTableId(), tmd);
					m_sortedtables.add(tmd.getTableId());
				}
			}
		}
	}

	/**
	 * Called by the DbModel when the user closes a connection. Allows us to
	 * release any references/resources
	 */
	void close() {
		m_connection = null;
		m_tables.clear();
		m_linkmgr = null;
		m_sortedtables.clear();
		m_dbmodel = null;
	}

	/**
	 * This method reads a ResulSet that is returned from a DatabaseMetaData
	 * call to getColumns for a given table. It creates a ColumnMetaData object
	 * for that table based on the information in the result set.
	 * 
	 * @param index
	 *            this is a fail safe value for the column ordinal value. If the
	 *            JDBC driver does not return a valid value for the
	 *            ORDINAL_POSITION, then we use the index
	 */
	private ColumnMetaData createColumn(ResultSet colset, TableId tableId, int index) throws SQLException {
		String colname = colset.getString("COLUMN_NAME");
		short coltype = colset.getShort("DATA_TYPE");
		String typename = colset.getString("TYPE_NAME");
		int colsize = colset.getInt("COLUMN_SIZE");
		boolean nullable = colset.getBoolean("NULLABLE");
		int decimal_digits = colset.getInt("DECIMAL_DIGITS");
		int ordinal_position = colset.getInt("ORDINAL_POSITION");
		ColumnMetaData cmd = new ColumnMetaData(colname, coltype, typename, colsize, tableId, nullable);
		cmd.setScale(decimal_digits);

		if (ordinal_position < 1)
			ordinal_position = index;
		cmd.setOrdinalPosition(ordinal_position);

		if (m_connection.getDatabase() == Database.MYSQL) {
			String def_value = colset.getString("COLUMN_DEF");
			cmd.setDefaultValue(def_value);

			try {
				// for mysql, the auto_increment flag is stored in the REMAKRS
				// column
				String extra = colset.getString("REMARKS");
				if (extra != null && (extra.indexOf("auto_increment") >= 0)) {
					cmd.setAutoIncrement(true);
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}
		return cmd;
	}

	/**
	 * If the database support schemas, then return the schema name. If the
	 * database does NOT support schemas, return the catalog name;
	 */
	public String getName() {
		if (m_schema == Schema.VIRTUAL_SCHEMA)
			return m_catalog.getName();
		else
			return m_schema.getName();
	}

	public LinkModel getLinkModel() {
		return m_linkmgr;
	}

	/**
	 * @return the collection of incoming links (Link objects) for the given
	 *         table. A link is defined by a foreign key for the given table
	 */
	Collection getInLinks(TableId tableId) {
		synchronized (this) {
			return m_linkmgr.getInLinks(tableId);
		}
	}

	/**
	 * @return the collection of outgoing links (Link objects) for the given
	 *         table. A link is defined by a table who has a foreign key
	 *         referenced to the given table's primary key
	 */
	Collection getOutLinks(TableId tableId) {
		synchronized (this) {
			return m_linkmgr.getOutLinks(tableId);
		}
	}

	/**
	 * @return the schema associated with this meta data
	 */
	Schema getSchema() {
		return m_schema;
	}

	/**
	 * @return the flag that maintains the state of this model
	 */
	int getState() {
		return m_modelstate;
	}

	/**
	 * @return the table metadata for the given table id from the tiven model
	 *         data. The model data object will be either the loaded or loading
	 *         object. If the table is not found in the model cache, it is
	 *         loaded from the database. If this fails, null is returned
	 */
	TableMetaData getTable(TableId tableId) {
		if (tableId == null)
			return null;

		TableMetaData tmd = null;
		synchronized (this) {
			tmd = (TableMetaData) m_tables.get(tableId);
		}

		try {
			if (tmd == null) {
				tmd = loadTable(tableId, null);
				if (tmd != null) {
					tmd.ORLoadMask(TableMetaData.LOAD_COLUMNS);
					tmd.validate();
				}
			} else {
				int currentmask = tmd.getLoadMask();
				if ((currentmask & TableMetaData.LOAD_COLUMNS) == 0) {
					loadTable(tableId, tmd);
					tmd.ORLoadMask(TableMetaData.LOAD_COLUMNS);
					tmd.validate();
				}
			}
		} catch (SQLException e) {
			TSUtils.printException(e);
		}
		return tmd;
	}

	/**
	 * @return the table metadata for the given table id from the tiven model
	 *         data. If the table is not found in the model cache, it is NOT
	 *         loaded from the database.
	 */
	TableMetaData getTableFast(TableId tableId) {
		if (tableId == null)
			return null;

		TableMetaData tmd = null;
		synchronized (this) {
			tmd = (TableMetaData) m_tables.get(tableId);
		}
		return tmd;
	}

	/**
	 * @return a list of TableId objects for the given Schema
	 */
	TreeSet getTables() {
		return m_sortedtables;
	}

	TreeSet getTables(String tableType) {
		TreeSet tset = new TreeSet();
		if (tableType == null)
			return tset;

		Collection tables = getTables();
		Iterator iter = tables.iterator();
		while (iter.hasNext()) {
			TableId tableid = (TableId) iter.next();
			TableMetaData tmd = getTableFast(tableid);
			if (tmd != null && tableType.equals(tmd.getTableType()))
				tset.add(tableid);
		}
		return tset;
	}

	/**
	 * @return a flag indicating if this model is currently being loaded in a
	 *         background thread. We do this because some data models might have
	 *         a large number of tables
	 */
	public synchronized boolean isLoading() {
		return (m_modelstate == CURRENTLY_LOADING);
	}

	/**
	 * @return a flag indicating if this model has been loaded (i.e. all tables
	 *         read )
	 */
	public synchronized boolean isLoaded() {
		return (m_modelstate == LOADED);
	}

	/**
	 * This method blocks in a modal loop until the model is loaded. If the
	 * model is not in the process of being loaded, then the process is stared.
	 * 
	 */
	public void join() {
		if (isLoading()) {
			DbModelJoinCommand.runJoinCommand(m_connection, this);
		} else if (isLoaded()) {
			return;
		} else {
			synchronized (this) {
				if (m_modelstate != CURRENTLY_LOADING) {
					m_modelstate = CURRENTLY_LOADING;
					startLoad();
				}
			}
			DbModelJoinCommand.runJoinCommand(m_connection, this);
		}
	}

	/**
	 * Loads columns information such as constraints and default values.
	 */
	void loadColumnsEx(TableMetaData tmd) throws SQLException {
		TSTable db = (TSTable) m_connection.getImplementation(TSTable.COMPONENT_ID);
		db.loadColumnsEx(tmd);
	}

	/**
	 * Find the foreign keys in other tables that reference the primary key in
	 * this table.
	 */
	void loadExportedKeys(TableMetaData tmd) throws SQLException {
		TSTable db = (TSTable) m_connection.getImplementation(TSTable.COMPONENT_ID);
		db.loadExportedKeys(tmd);
	}

	/**
	 * Loads the foreign key definitions for all loaded tables
	 * 
	 */
	void loadForeignKeys(TableMetaData tmd) throws SQLException {
		TSForeignKeys fkeys = (TSForeignKeys) m_connection.getImplementation(TSForeignKeys.COMPONENT_ID);
		TableId tableid = tmd.getTableId();
		Collection c = fkeys.getForeignKeys(tableid);
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			addForeignKey(tmd, (DbForeignKey) iter.next());
		}
	}

	/**
	 * Loads the primary key for the given table
	 */
	private DbKey loadPrimaryKey(Catalog catalog, Schema schema, String tableName) {
		try {
			DatabaseMetaData metadata = m_connection.getMetaDataConnection().getMetaData();
			ResultSet rs = null;
			try {
				rs = metadata.getPrimaryKeys(catalog.getMetaDataSearchParam(), schema.getMetaDataSearchParam(),
						tableName);
				DbKey primarykey = null;
				while (rs.next()) {
					String tablename = rs.getString("TABLE_NAME");
					String colname = rs.getString("COLUMN_NAME");

					String key_seq = rs.getString("KEY_SEQ");
					String pk_name = rs.getString("PK_NAME");

					if (primarykey == null) {
						primarykey = new DbKey();
						primarykey.setKeyName(pk_name);
					}
					primarykey.addField(colname);
				}

				return primarykey;

				// if ( !bfirst )
				// {
				// tmd.setPrimaryKey( primarykey );
				// }
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			} finally {
				try {
					if (rs != null)
						rs.close();
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Loads the tables in the given schema
	 */
	private void load() throws SQLException {

		DatabaseMetaData metadata = m_connection.getMetaDataConnection().getMetaData();

		String[] ttypes = new String[7];
		ttypes[0] = "TABLE";
		ttypes[1] = "VIEW";
		ttypes[2] = "SYSTEM TABLE";
		ttypes[3] = "GLOBAL TEMPORARY";
		ttypes[4] = "LOCAL TEMPORARY";
		ttypes[5] = "ALIAS";
		ttypes[6] = "SYNONYM";

		ResultSet rset = null;
		try {
			// first load all tables in the system
			//rset = metadata.getTables(m_catalog.getMetaDataSearchParam(), m_schema.getMetaDataSearchParam(), null,	ttypes);
			rset = metadata.getTables(null, null, null, ttypes);
			TSUtils.printDebugMessage( "ModelData load table: " + m_catalog.getMetaDataSearchParam() + "  "  + m_schema.getMetaDataSearchParam() );;

			while (rset.next()) {
				String tablename = rset.getString("TABLE_NAME");
				TSUtils.printDebugMessage( "ModelData load table: " + tablename);
				String ttype = rset.getString("TABLE_TYPE");

				if (m_catalog != Catalog.VIRTUAL_CATALOG) {
					String cat = rset.getString("TABLE_CAT");
					assert (m_catalog.getName().equals(cat));
				}

				if (m_schema != Schema.VIRTUAL_SCHEMA) {
					String schema = rset.getString("TABLE_SCHEM");
					assert (m_schema.getName().equals(schema));
					if ( !m_schema.getName().equals(schema)) {
						continue;
					}
				}

				if ("synonym_test119_".equalsIgnoreCase(tablename))
					ttype = "SYNONYM";

				// be careful here because the table may have been implicitly
				// loaded by a foreign key relationship
				TableId tableid = new TableId(m_catalog, m_schema, tablename);
				TableMetaData tmd = null;
				synchronized (this) {
					tmd = (TableMetaData) m_tables.get(tableid);
				}
				if (tmd == null)
					tmd = new TableMetaData(tableid);

				tmd.setTableType(ttype);
				/*
				TSUtils.printMessage( "ModelData.loadSchema: " +
				m_schema.getName() + "  table: " + tablename + "   type: " +
				ttype + "  isView: " + tmd.isView() + "   hash: " +
				 tmd.hashCode() );
				 */

				addTable(m_schema, tmd);
			}

		} finally {
			rset.close();
			m_modelstate = LOADED;
		}
	}

	/**
	 * Loads the given table if it is not already loaded
	 */
	private TableMetaData loadTable(TableId tableId, DatabaseMetaData metadata, TableMetaData tmd) throws SQLException {
		// convert the table name to the case supported by the database
		TSDatabase db = (TSDatabase) m_connection.getImplementation(TSDatabase.COMPONENT_ID);
		String tablename = db.convertCase(tableId.getTableName());

		int index = 1;
		ResultSet colset = metadata.getColumns(tableId.getCatalog().getMetaDataSearchParam(), tableId.getSchema()
				.getMetaDataSearchParam(), tablename, null);
		while (colset.next()) {
			if (tmd == null)
				tmd = new TableMetaData(tableId);

			ColumnMetaData cmd = createColumn(colset, tableId, index);
			tmd.setColumn(cmd, cmd.getOrdinalPosition());
			index++;
		}
		colset.close();

		if (tmd != null) {
			addTable(tableId.getSchema(), tmd);
			try {
				// as opposed to not being a view
				if (!tmd.isView()) {
					DbKey pk = loadPrimaryKey(tableId.getCatalog(), tableId.getSchema(), tablename);
					tmd.setPrimaryKey(pk);
				}
			} catch (Exception e) {
				// just in case
				System.out.println("Error loading keys for table: " + tmd.getTableName());
				e.printStackTrace();
			}
		}
		return tmd;
	}

	/**
	 * This method loads all tables in a given schema. This method is used
	 * during startup to improve performance. It is a separate method from the
	 * one above because we are loading everything and can't be assured of the
	 * order of tables.
	 */
	private void loadTables(Catalog catalog, Schema schema, DatabaseMetaData metadata) throws SQLException {
		ResultSet colset = metadata.getColumns(catalog.getMetaDataSearchParam(), schema.getMetaDataSearchParam(), null,
				null);
		TableMetaData tmd = null;
		int index = 1;
		while (colset.next()) {
			String tablename = colset.getString("TABLE_NAME");
			String colname = colset.getString("COLUMN_NAME");
			TableId tableid = new TableId(catalog, schema, tablename);
			tmd = (TableMetaData) m_tables.get(tableid);
			if (tmd != null) {
				ColumnMetaData cmd = createColumn(colset, tableid, index);
				tmd.setColumn(cmd, cmd.getOrdinalPosition());
				index++;
			}
		}
		colset.close();
	}

	/**
	 * Loads the given table if it is not already loaded
	 */
	private TableMetaData loadTable(TableId tableId, TableMetaData tmd) throws SQLException {
		Connection conn = m_connection.getMetaDataConnection();
		if (conn != null) {
			DatabaseMetaData metadata = conn.getMetaData();
			tmd = loadTable(tableId, metadata, tmd);
			if (tmd == null) {
				if (tableId == null || tableId.getTableName() == null || tableId.getTableName().length() == 0) {
					assert (false);
				} else {
					// System.out.println( "ModelData.loadTable failed for: " +
					// tableId );
				}

			}
			return tmd;
		} else {
			TSUtils.printMessage(">>>>ModelData.ERROR>>>");
			assert (false);
			return null;
		}
	}

	/**
	 * Call when a table name has changed. Removes the table from the cache and
	 * fires an event
	 */
	void renameTable(TableId newid, TableId oldid) {
		LinkedList removes = new LinkedList();
		removes.add(oldid);

		TableMetaData tmd = getTable(oldid);
		if (tmd != null) {
			Collection fkeys = tmd.getForeignKeys();
			Iterator fiter = fkeys.iterator();
			while (fiter.hasNext()) {
				DbForeignKey fkey = (DbForeignKey) fiter.next();
				if (oldid.equals(fkey.getReferenceTableId())) {
					removes.add(fkey.getLocalTableId());
				}
			}

			// you also need to remove all tables that reference this table so
			// that their metadata can be reloaded from the database
			Iterator iter = removes.iterator();
			while (iter.hasNext()) {
				TableId tableid = (TableId) iter.next();
				removeTable(tableid);
			}
		}
	}

	/**
	 * Removes the table from the cache
	 */
	void removeTable(TableId id) {
		synchronized (this) {
			if (id != null) {
				m_tables.remove(id);
				m_sortedtables.remove(id);
				m_linkmgr.removeTable(id);
			}
		}
	}

	/**
	 * Creates a background thread that loads the model
	 */
	private void startLoad() {

		Runnable loader = new Runnable() {
			public void run() {
				try {
					TSUtils.printMessage("--------------- ModelData starting to load model " + m_catalog.getName()
							+ "." + m_schema.getName() + "  ---------------");

					m_connection.setAutoCommit(m_connection.getMetaDataConnection(), true);
					load();
				} catch (SQLException e) {
					// we need to notify the user here
					TSUtils.printException(e);
				} finally {
					TSUtils.printMessage("--------------- ModelData finished load model " + m_catalog.getName() + "."
							+ m_schema.getName() + "  ---------------");
					m_dbmodel.loadingCompleted(ModelData.this);
				}
			}
		};

		Thread t = new Thread(loader, "ModelData.loader.thread");
		t.start();
	}

}
