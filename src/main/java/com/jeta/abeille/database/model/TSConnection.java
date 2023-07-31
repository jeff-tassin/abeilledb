package com.jeta.abeille.database.model;

import com.jeta.abeille.database.procedures.StoredProcedureInterface;
import com.jeta.abeille.database.procedures.StoredProcedureService;
import com.jeta.abeille.database.security.SecurityManager;
import com.jeta.abeille.database.security.SecurityService;
import com.jeta.abeille.logger.DbLogger;
import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.componentmgr.TSNotifier;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;
import com.jeta.foundation.utils.TSUtils;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.sql.*;
import java.util.*;

/**
 * This class represents a connection to a single database instance
 * 
 * @author Jeff Tassin
 */
public class TSConnection implements JETAExternalizable {
	static final long serialVersionUID = 6476913931394467693L;

	public static int VERSION = 1;

	public static final String TRANSACTION_ISOLATION = "transaction.isolation";
	public static final String USE_MULTIPLE_CONNECTIONS = "use.multiple.connections";
	public static final String AUTO_LOAD_CATALOGS = "auto.load.catalogs";

	public static final String READ_UNCOMMITTED = "READ_UNCOMMITTED";
	public static final String READ_COMMITTED = "READ_COMMITTED";
	public static final String REPEATABLE_READ = "REPEATABLE_READ";
	public static final String SERIALIZABLE = "SERIALIZABLE";

	public static final String TYPE_FORWARD_ONLY = "TYPE_FORWARD_ONLY";
	public static final String TYPE_SCROLL_INSENSITIVE = "TYPE_SCROLL_INSENSITIVE";
	public static final String TYPE_SCROLL_SENSITIVE = "TYPE_SCROLL_SENSITIVE";

	/** id for current schema for the connection */
	public static final String ID_CURRENT_SCHEMAS = "current.schemas";
	public static final String ID_CURRENT_CATALOG = "current.catalog";

	/** the default auto commit flag for this connection */
	public static final String ID_AUTO_COMMIT = "auto.commit";

	/** the maxiumum number for rows to return from a query */
	public static final String ID_MAX_QUERY_ROWS = "max.query.rows";

	/**
	 * Used for metadata commands. This is only used if the
	 * 'use.separate.metadata.connection' flag is true. Otherwise, the metadata
	 * commands share the JDBC connection.
	 */
	private transient Connection m_metadata_connection;

	/**
	 * The actual JDBC connection
	 */
	private transient Connection m_connection;

	/**
	 * This is a list of opened write connections
	 */
	private transient LinkedList m_writeconnections = new LinkedList();

	/**
	 * The database driver
	 */
	private transient Driver m_driver;

	/**
	 * A map of Catalog Objects (key) to CatalogInfo objects (values)
	 */
	private transient TreeMap m_catalogs = new TreeMap();

	/**
	 * An empty model we return when the caller requests a model for a catalog
	 * that is invalid. Instead of returning null, we return an empty model.
	 */
	private transient DbModel m_emptymodel = new DbModel(this);

	/**
	 * This object contains the information about the connection: driver, url,
	 * username, etc.
	 */
	private ConnectionInfo m_connectioninfo;

	/**
	 * Instances of vendor specific implementation if interfaces such as
	 * TSDatabaseUsers, TSForeignKeys, TSStoredProcedures, etc.
	 */
	private transient HashMap m_implementations = new HashMap();

	/** flag that indicates if the given database supports schemas */
	private transient Boolean m_supportsschemas = null;

	/**
	 * flag that indicates if the given database supports multiple catalogs
	 * opened in a single connection
	 */
	private transient Boolean m_supportscatalogs = null;

	/** flag that indicates if the given database supports transactions */
	private transient Boolean m_supportstransactions = null;

	/** flag that indicates if the given database table names are case sensitive */
	private transient Boolean m_casesensitive = null;

	/**
	 * Flag that indicates whether it is possible to have multiple ResultSet
	 * objects returned from a CallableStatement object simultaneously.
	 */
	private transient Boolean m_supportsmultiresults = null;

	/**
	 * Object that creates the various service implementations (see.
	 * getImplementation)
	 */
	private transient TSServiceFactory m_servicefactory;

	// ////////// non transient members

	public static final String COMPONENT_ID = "jeta.abeille.database.model.tsconn";
	// TSNotifier messages
	public static final String MSG_CONNECT = "jeta.abeille.database.model.tsconn.connect";
	public static final String MSG_DISCONNECT = "jeta.abeille.database.model.tsconn.disconnect";
	public static final String MSG_CONNECTION_CREATED = "tsconn.connection.created";
	public static final String MSG_CONNECTION_RELEASED = "tsconn.connection.released";
	public static final String MSG_STATUS = "jeta.abeille.database.model.tsconn.status";

	/**
	 * ctor only for serialization
	 */
	public TSConnection() {

	}

	/** for testing only */
	public TSConnection(boolean caseSensitive) {
		m_casesensitive = Boolean.valueOf(caseSensitive);
	}

	/**
	 * Constructor. Logs into the database at the given url.
	 */
	public TSConnection(ConnectionInfo info) throws SQLException, ClassNotFoundException {
		m_connectioninfo = info;
		resetReadOnlyConnection();
	}

	/**
	 * Adds the given listener to all available models in the connection
	 */
	public synchronized void addModelListener(DbModelListener listener) {
		Collection catalogs = m_catalogs.values();
		assert (catalogs.size() > 0);
		Iterator iter = catalogs.iterator();
		while (iter.hasNext()) {
			CatalogInfo cinfo = (CatalogInfo) iter.next();
			cinfo.getModel().addListener(listener);
		}
	}

	/**
	 * Closes a write connection The connection should have been acquired by the
	 * getWriteConnection call
	 * 
	 * @param conn
	 *            the connection to close
	 * 
	 */
	public void closeConnection(Connection conn) {
		try {
			release(conn);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Called by the TSConnectionMgr when the user closes a connection. Allows
	 * us to release any references
	 */
	void close() {
		Collection write_cons = (Collection) m_writeconnections.clone();
		Iterator iter = write_cons.iterator();
		while (iter.hasNext()) {
			Connection c = (Connection) iter.next();
			try {
				c.close();
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}

		m_writeconnections.clear();

		try {
			m_connection.close();
			m_connection = null;
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		try {
			if (m_metadata_connection != null) {
				m_metadata_connection.close();
				m_metadata_connection = null;
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		m_implementations.clear();

		iter = m_catalogs.values().iterator();
		while (iter.hasNext()) {
			CatalogInfo cinfo = (CatalogInfo) iter.next();
			DbModel model = cinfo.getModel();
			try {
				if (model != null) {
					model.close();
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}
		m_catalogs.clear();
		m_connectioninfo = null;
	}

	/**
	 * @return true if this connection contains the given catalog
	 */
	public boolean contains(Catalog cat) {
		if (cat == null)
			return false;
		return m_catalogs.containsKey(cat);
	}

	/**
	 * @return true if this connection contains the given catalog/schema pair
	 */
	public boolean contains(Catalog cat, Schema schema) {
		if (cat == null || schema == null)
			return false;

		DbModel model = getModel(cat);
		return model.contains(schema);
	}

	/**
	 * Logs into the database
	 */
	public Connection createConnection(ConnectionInfo cinfo) throws SQLException, ClassNotFoundException {
		Connection connection = null;
		try {
			java.util.Properties info = new java.util.Properties();
			info.put("user", cinfo.getUserName());
			info.put("password", cinfo.getPassword());

			if (m_driver == null) {
				ClassLoader cloader = cinfo.getClassLoader();
				if(cloader != null) {
					Class dc = cloader.loadClass(cinfo.getDriver());
					m_driver = (Driver) dc.newInstance();
				}
			}
			if ( m_driver == null ) {
				// cinfo.getDatabase() == Database.ORACLE
				// driver must be in classpath
				connection = DriverManager.getConnection(cinfo.getUrl(), cinfo.getUserName(), cinfo.getPassword());
			} else {
				connection = m_driver.connect(cinfo.getUrl(), info);
			}
			connection.setAutoCommit(true);

			if (TSUtils.isDebug()) {
				System.out.println("TSConnection.connected...");
			}

			if (connection.getMetaData().supportsCatalogsInTableDefinitions() || Database.MYSQL.equals(cinfo.getDatabase())) {
				java.sql.ResultSet rset = connection.getMetaData().getCatalogs();
				while (rset.next()) {
					String catname = rset.getString("TABLE_CAT");
					Catalog c = Catalog.createInstance(catname);

					DbModel model = new DbModel(this, c);
					CatalogInfo catinfo = new CatalogInfo(c, model);
					m_catalogs.put(c, catinfo);
				}
			} else {
				Catalog c = Catalog.VIRTUAL_CATALOG;

				DbModel model = new DbModel(this, c);
				CatalogInfo catinfo = new CatalogInfo(c, model);
				m_catalogs.put(c, catinfo);
			}

			return connection;
		} catch (InstantiationException e) {
			throw new SQLException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new SQLException(e.getMessage());
		}
	}

	/**
	 * Creates a statement object used to execute SQL statements.
	 * 
	 * @return the statement object
	 */
	public Statement createStatement() throws SQLException {
		return m_connection.createStatement();
	}

	/**
	 * Creates a scrollable statement for querying metadata
	 */
	public Statement createScrollableMetaDataStatement() throws SQLException {
		Connection conn = getMetaDataConnection();
		int rtype = getResultSetScrollType();
		int concurrency = getResultSetConcurrency();
		return conn.createStatement(rtype, concurrency);
	}

	public void fireStatusUpdate(Catalog catalog) {
		DbModel model = getModel(catalog);
		if (model != null)
			model.fireStatusUpdate();
	}

	/**
	 * Equals test
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof TSConnection) {
			TSConnection conn = (TSConnection) obj;
			return m_connectioninfo.equals(conn.m_connectioninfo);
		} else
			return false;
	}

	/**
	 * @return the catalog object that matches the given name
	 */
	public synchronized Catalog getCatalogInstance(String name) {
		Iterator iter = m_catalogs.keySet().iterator();
		while (iter.hasNext()) {
			Catalog cat = (Catalog) iter.next();
			if (I18N.equalsIgnoreCase(cat.getName(), name))
				return cat;
		}
		return null;
	}

	/**
	 * @return the set of catalogs (Catalog objects) in the connection
	 */
	public synchronized Collection getCatalogs() {
		return m_catalogs.keySet();
	}

	/**
	 * @return the database this connection is bound to
	 */
	public Database getDatabase() {
		return m_connectioninfo.getDatabase();
	}

	/**
	 * @return the JDBC driver for this connection
	 */
	public Driver getJDBCDriver() {
		return m_driver;
	}

	/**
	 * @return the max rows to return in a query
	 */
	public static int getMaxQueryRows() {
		return TSUserPropertiesUtils.getInteger(TSConnection.ID_MAX_QUERY_ROWS, 500);
	}

	/**
	 * @return the metadata object for the connection
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return getMetaDataConnection().getMetaData();
	}

	/**
	 * We store some user preferences according to the current database
	 * connection. For example, we store the history of SQL commands (say the
	 * last 100 commands) in this store. These commands are specific to a
	 * database connection, so that is why we have the association here.
	 */
	public ObjectStore getObjectStore() {
		return TSConnectionMgr.getObjectStore(getId());
	}

	/**
	 * @return the number of opened connections
	 */
	public int getConnectionCount() {
		// the number of write connections plus the read only connection
		return m_writeconnections.size() + 1;
	}

	/**
	 * @return the underlying connection info object
	 */
	public ConnectionInfo getConnectionInfo() {
		return m_connectioninfo;
	}

	/**
	 * @return the current catalog for the user
	 */
	public Catalog getCurrentCatalog() {
		ObjectStore os = getObjectStore();
		Catalog catalog = null;

		try {
			catalog = (Catalog) os.load(ID_CURRENT_CATALOG);
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		if (!contains(catalog)) {
			catalog = getDefaultCatalog();
		}

		return catalog;
	}

	/**
	 * @return the default schema for the currrent user
	 */
	public Schema getCurrentSchema() {
		return getCurrentSchema(getCurrentCatalog());
	}

	/**
	 * @return the default schema for the currrent user
	 */
	public Schema getCurrentSchema(Catalog cat) {
		try {
			// @todo **** cache the current schema ***/
			validate(cat, null);

			/**
			 * first try to locate the schema that was manually set by the user
			 * using the SetPath dialog. If that schema is found, we need to
			 * make sure it still exists in the database
			 */
			Schema schema = null;
			ObjectStore os = getObjectStore();
			HashMap hmap = (HashMap) os.load(ID_CURRENT_SCHEMAS);
			if (hmap != null) {
				schema = (Schema) hmap.get(cat);
				if (!contains(cat, schema)) {
					schema = null;
				}
			}

			/**
			 * If the schema could not be found in the persistent store, then
			 * get the current schema from the database.
			 */
			if (schema == null) {
				TSDatabase db = (TSDatabase) getImplementation(TSDatabase.COMPONENT_ID);
				schema = db.getCurrentSchema();
			}

			/**
			 * If all else fails, get the first schema in the model
			 */
			if (schema == null || !contains(cat, schema)) {
				Collection schemas = getModel(cat).getSchemas();
				Iterator iter = schemas.iterator();
				while (iter.hasNext()) {
					schema = (Schema) iter.next();
					break;
				}
			}
			return schema;
		} catch (Exception e) {
			TSUtils.printException(e);
			return null;
		}
	}

	/**
	 * @return the classname of the JDBC driver used for this connection
	 */
	public String getDriver() {
		if (m_connectioninfo != null)
			return m_connectioninfo.getDriver();
		else
			return null;
	}

	/**
	 * @return a unique id that identifies this connection
	 */
	public ConnectionId getId() {
		assert (m_connectioninfo != null);
		if (m_connectioninfo != null)
			return m_connectioninfo.getConnectionId();
		else
			return null;
	}

	/**
	 * Returns the implementation object instance for a given interface name
	 * 
	 * @return the object that is registered for the given interface name. Null
	 *         is returned if no object is found.
	 */
	public synchronized Object getImplementation(String componentId) {
		Object impl = m_implementations.get(componentId);
		if (impl == null) {
			impl = m_servicefactory.createService(componentId);

			// StoredProcedureService and SecurityService have special wrappers
			// around the actual
			// implementation
			if (StoredProcedureService.COMPONENT_ID.equals(componentId)) {
				impl = new StoredProcedureService((StoredProcedureInterface) impl);
			} else if (SecurityService.COMPONENT_ID.equals(componentId)) {
				impl = new SecurityManager((SecurityService) impl);
			}
			m_implementations.put(componentId, impl);
		}
		return impl;
	}

	/**
	 * @return a connection for reading/writing metadata information. We provide
	 *         a separate method in case we really want to do this at a future
	 *         date. This is mainly required because we call commit during many
	 *         metadata operations (e.g. create trigger ... commit), and we
	 *         might not want to effect transactions on other connections such
	 *         as the instance view. Currently, we just return a read connection
	 *         even though we might do meta data writes on it. The write
	 *         connections are used to maintain transactions for an
	 *         interdeterminate length of time, whereas any transactions on this
	 *         connection are immediate.
	 */
	public Connection getMetaDataConnection() {
		if (m_metadata_connection == null)
			return m_connection;
		else
			return m_metadata_connection;
	}

	/**
	 * @return all models in this connection
	 */
	public synchronized Collection getModels() {
		LinkedList results = new LinkedList();
		Iterator iter = m_catalogs.values().iterator();
		while (iter.hasNext()) {
			CatalogInfo cinfo = (CatalogInfo) iter.next();
			results.add(cinfo.getModel());
		}
		return results;
	}

	/**
	 * @return the underlying database model
	 */
	public synchronized DbModel getModel(Catalog cat) {
		assert (cat != null);

		CatalogInfo cinfo = (CatalogInfo) m_catalogs.get(cat);
		if (cinfo == null) {
			TSUtils.printMessage("TSConnection.getModel   bad catalog: " + cat);
			// TSUtils.printStackTrace();
			return m_emptymodel;
		} else {
			DbModel model = cinfo.getModel();
			assert (model != null);
			return model;
		}
	}

	/**
	 * @return the description of the database. This is the description of the
	 *         database as supplied by the user in the connection management
	 *         window.
	 */
	public String getDescription() {
		return m_connectioninfo.toString();
	}

	public Catalog getDefaultCatalog() {
		if (supportsCatalogs()) {
			String def_name = null;
			if (m_connectioninfo.isBasic()) {
				def_name = m_connectioninfo.getName();
			}

			Catalog def_catalog = null;
			Iterator iter = m_catalogs.keySet().iterator();
			while (iter.hasNext()) {
				Catalog catalog = (Catalog) iter.next();
				if (def_name == null) {
					return catalog;
				} else {
					if (def_name.equalsIgnoreCase(catalog.getName()))
						return catalog;
				}

				if (def_catalog == null)
					def_catalog = catalog;
			}

			return def_catalog;
		} else {
			return Catalog.VIRTUAL_CATALOG;
		}
	}

	/**
	 * @return the default auto commit flag for this connection
	 */
	public boolean getDefaultAutoCommit() {
		try {
			ObjectStore os = getObjectStore();
			Boolean def_commit = Boolean.TRUE;
			/** pointbase has a problem with autocommit = on */
			if (Database.POINTBASE.equals(getDatabase())) {
				def_commit = Boolean.FALSE;
			}
			Boolean auto_commit = (Boolean) os.load(ID_AUTO_COMMIT, def_commit);
			return (Boolean.TRUE.equals(auto_commit));
		} catch (Exception e) {
			TSUtils.printException(e);
			return true;
		}
	}

	/**
	 * @return the name of the database. This is the name of the database as
	 *         supplied by the user in the connection management window.
	 */
	public String getName() {
		return m_connectioninfo.getName();
	}

	public boolean isPROD() {
		String connName = getDescription();
		return connName == null ? false : connName.contains("PROD");
	}
	
	/**
	 * @return the ResultSet concurrency flag used for queries.
	 */
	public int getResultSetConcurrency() {
		return ResultSet.CONCUR_READ_ONLY;
	}

	/**
	 * @return the ResultSet scroll type flag used for queries.
	 */
	public int getResultSetScrollType() {
		int result = ResultSet.TYPE_SCROLL_INSENSITIVE;
		try {
			ObjectStore os = getObjectStore();
			Integer scroll_type = (Integer) os.load(TSDatabase.RESULT_SET_TYPE);
			if (scroll_type != null) {
				result = scroll_type.intValue();
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		if (TSUtils.isDebug()) {
			if (result == ResultSet.TYPE_SCROLL_INSENSITIVE) {
				TSUtils.printMessage("TSConnection.getResultSetScrollType:  TYPE_SCROLL_INSENSITIVE");
			} else if (result == ResultSet.TYPE_SCROLL_SENSITIVE) {
				TSUtils.printMessage("TSConnection.getResultSetScrollType:  TYPE_SCROLL_SENSITIVE");
			} else {
				assert (false);
			}
		}

		return result;
	}

	/**
	 * Searches the available shemas in the connection and compares them to the
	 * given schema name (ignoring case). If the schema is found, it is
	 * returned. Otherwise, null is returned.
	 */
	public Schema getSchema(Catalog cat, String schemaName) {
		if (schemaName == null)
			return null;

		Collection schemas = getModel(cat).getSchemas();
		Iterator iter = schemas.iterator();
		while (iter.hasNext()) {
			Schema schema = (Schema) iter.next();
			if (I18N.equalsIgnoreCase(schemaName, schema.getName()))
				return schema;
		}
		return null;
	}

	/**
	 * @return the tables that belong to the given catalog and schema
	 */
	public Collection getSchemas(Catalog cat) {
		return getModel(cat).getSchemas();
	}

	/**
	 * @return the server for this connection
	 */
	public String getServer() {
		return m_connectioninfo.getServer();
	}

	/**
	 * @return a short version of the id. This id is made up of only the
	 *         database name and user name. Therefore, it might not be unique
	 *         because the database name is arbitrary.
	 */
	public String getShortId() {
		return m_connectioninfo.getDescription() + ":" + m_connectioninfo.getUserName();
	}

	public static String getIsolationString(int isolation) {
		if (isolation == Connection.TRANSACTION_READ_UNCOMMITTED)
			return TSConnection.READ_UNCOMMITTED;
		else if (isolation == Connection.TRANSACTION_READ_COMMITTED)
			return TSConnection.READ_COMMITTED;
		else if (isolation == Connection.TRANSACTION_REPEATABLE_READ)
			return TSConnection.REPEATABLE_READ;
		else if (isolation == Connection.TRANSACTION_SERIALIZABLE)
			return TSConnection.SERIALIZABLE;
		else
			return "Unknown";
	}

	/**
	 * Helper method to get the table metadata for the given table
	 */
	public TableMetaData getTable(TableId tableId) {
		if (tableId == null)
			return null;

		DbModel model = getModel(tableId.getCatalog());
		assert (model != null);
		return model.getTable(tableId);
	}

	/**
	 * @return the table metadata for the given table id. If the table is not
	 *         found in the model cache, it is loaded from the database. If this
	 *         fails, null is returned
	 */
	public TableMetaData getTableEx(TableId tableId, int flag) {
		if (tableId == null)
			return null;

		DbModel model = getModel(tableId.getCatalog());
		assert (model != null);
		return model.getTableEx(tableId, flag);
	}

	/**
	 * @return the tables that belong to the given catalog and schema
	 */
	public java.util.TreeSet getTables(Catalog cat, Schema schema) {
		return getModel(cat).getTables(schema);
	}

	/**
	 * @return the transaction isolation for the application
	 */
	public int getTransactionIsolation() {
		try {
			Connection c = getMetaDataConnection();
			return c.getTransactionIsolation();
		} catch (SQLException se) {
			DbLogger.log(se);
			DbLogger.fine(I18N.getLocalizedMessage("Get Transaction Isolation Failed"));
			return Connection.TRANSACTION_SERIALIZABLE;
		}
	}

	private Connection createWriteConnection() throws SQLException {
		try {
			Connection c = createConnection(m_connectioninfo);

			TSNotifier n = TSNotifier.getInstance(getId().getUID());
			n.fireEvent(this, COMPONENT_ID, MSG_CONNECTION_CREATED, c);
			m_writeconnections.add(c);
			return c;
		} catch (ClassNotFoundException cnfe) {
			throw new SQLException(I18N.format("Unable_to_load_JDBC_driver_1", m_connectioninfo.getDriver()));
		}
	}

	/**
	 * @return a connection that we can commit/rollback. This is a connection
	 *         for dealing with user data. If you are dealing with metadata,
	 *         call getMetaDataConnection
	 */
	public Connection getWriteConnection() throws SQLException {
		if (isMultipleConnections()) {
			return createWriteConnection();
		} else {
			return m_connection;
		}
	}

	/**
	 * @return the url of the database that this connection is connected to
	 */
	public String getUrl() {
		if (m_connectioninfo != null)
			return m_connectioninfo.getUrl();
		else
			return null;
	}

	/**
	 * @return the name of the user that this connection is logged in as
	 */
	public String getUser() {
		if (m_connectioninfo != null)
			return m_connectioninfo.getUserName();
		else
			return null;
	}

	/**
	 * hashCode
	 */
	public int hashCode() {
		if (m_connectioninfo != null)
			return m_connectioninfo.hashCode();
		else
			return 0;
	}

	/**
    */
	public boolean isQueryTableCounts() {
		try {
			ObjectStore os = getObjectStore();
			Boolean bquery = (Boolean) os.load(TSDatabase.QUERY_TABLE_COUNTS);
			if (bquery != null)
				return bquery.booleanValue();
		} catch (Exception se) {
			TSUtils.printException(se);
		}
		return true;
	}

	/**
	 * Use this method to commit a connection owned by this TSConnection. We do
	 * this to provide logging and debugging.
	 */
	public void jetaCommit(Connection conn) throws SQLException {
		try {
			if (conn.getAutoCommit())
				return;
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		if (conn == m_metadata_connection) {
			TSUtils.printMessage("TSConenction.commit metadata connection");
		} else {
			TSUtils.printMessage("TSConenction.commit data connection");
		}

		conn.commit();
	}

	/**
	 * Use this method to rollback a connection owned by this TSConnection. We
	 * do this to provide logging and debugging.
	 */
	public void jetaRollback(Connection conn) throws SQLException {
		if ( isAutoCommit() ) {
			return;
		}
		if (conn == m_metadata_connection) {
			TSUtils.printMessage("TSConenction.rollback metadata connection");
		} else {
			TSUtils.printMessage("TSConenction.rollback data connection");
		}
		
		conn.rollback();
	}

	/**
	 * @return true if the current connection has auto commit to true
	 */
	public boolean isAutoCommit() throws SQLException {
		return m_connection.getAutoCommit();
	}

	/**
	 * @return true if table names are case-sensitive for this connection.
	 */
	public boolean isCaseSensitive() {
		if (m_casesensitive == null) {
			TSDatabase db = (TSDatabase) getImplementation(TSDatabase.COMPONENT_ID);
			m_casesensitive = Boolean.valueOf(db.isCaseSensitive());
		}
		return m_casesensitive.booleanValue();
	}

	/**
	 * Reads to user properties value for USE_MULTIPLE_CONNECTIONS and returns
	 * the value for this property if found. If the prop is not found, then
	 * return false by default
	 */
	public boolean isMultipleConnections() {
		// TSUserProperties userprops = (TSUserProperties)ComponentMgr.lookup(
		// TSUserProperties.COMPONENT_ID );
		// return Boolean.valueOf( userprops.getProperty(
		// TSConnection.USE_MULTIPLE_CONNECTIONS, "false" ) ).booleanValue();
		return false;
	}

	/**
	 * You absolutely need this when deserializing this class. The
	 * TSConnectionMgr is the only class that can create connections. This class
	 * only serializes the connection id. We pass this to the connection mgr to
	 * get a valid object.
	 */
	private Object readResolve() throws ObjectStreamException {
		try {
			Object result = TSConnectionMgr.getConnection(getId());
			return result;
		} catch (Exception e) {
			if (e instanceof ObjectStreamException)
				throw (ObjectStreamException) e;

			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Releases a write connection and allows other components to request it.
	 * The connection should have been acquired by the getWriteConnection call
	 * 
	 * @param conn
	 *            the connection to release.
	 * 
	 */
	public void release(Connection conn) {
		try {
			if (conn != null) {

				if (conn != m_connection && m_writeconnections.remove(conn)) {
					// for now, just close it
					conn.close();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (conn != m_connection) {
			TSNotifier n = TSNotifier.getInstance(getId().getUID());
			n.fireEvent(this, COMPONENT_ID, MSG_CONNECTION_RELEASED, conn);
		}
	}

	/**
	 * Removes the given listener to all available models in the connection
	 */
	public synchronized void removeModelListener(DbModelListener listener) {
		Iterator iter = m_catalogs.values().iterator();
		while (iter.hasNext()) {
			CatalogInfo cinfo = (CatalogInfo) iter.next();
			cinfo.getModel().removeListener(listener);
		}
	}

	/**
	 * Resets the read only connection. Sometimes we need this when the database
	 * changes (such as a table was added) and we want the read connection to
	 * see the new changes
	 */
	public void resetReadOnlyConnection() throws SQLException, ClassNotFoundException {
		if (isMultipleConnections() || m_connection == null) {
			m_connection = createConnection(m_connectioninfo);
			try {
				m_connection.setAutoCommit(getDefaultAutoCommit());
			} catch (Exception e) {
				TSUtils.printStackTrace();
			}

			boolean share_metadata_connection = false;
			try {
				share_metadata_connection = TSUserPropertiesUtils.getBoolean("share.metadata.connection", false);
			} catch (Exception e) {
				// eat it
				TSUtils.printException(e);
			}

			if (share_metadata_connection) {
				DbLogger.fine("Sharing metadata connection");
				m_metadata_connection = m_connection;
			} else {
				if (m_metadata_connection == null) {
					m_metadata_connection = createConnection(m_connectioninfo);
				}
			}
		}
	}

	/**
	 * Sets the current schema for the currrent user
	 */
	public void setCurrentCatalog(Catalog catalog) throws SQLException {
		try {
			ObjectStore os = getObjectStore();
			os.store(ID_CURRENT_CATALOG, catalog);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Sets the current schema for the currrent user
	 */
	public void setCurrentSchema(Catalog catalog, Schema schema) throws SQLException {
		try {
			if (contains(catalog, schema)) {
				ObjectStore os = getObjectStore();
				HashMap hmap = (HashMap) os.load(ID_CURRENT_SCHEMAS);
				if (hmap == null) {
					hmap = new HashMap();
				}
				hmap.put(catalog, schema);
				os.store(ID_CURRENT_SCHEMAS, hmap);
			} else {
				TSUtils.printMessage("TSConnection.setCurrentSchema failed: catalog: " + catalog + "  schema: "
						+ schema);
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Sets the auto commit flag for this connection. Also, stores the value in
	 * a properties file which is read when the connection is created.
	 */
	public void setAutoCommit(boolean auto_commit) throws SQLException {
		m_connection.setAutoCommit(auto_commit);
		DbLogger.fine(I18N.format("Set_Auto_commit_1", String.valueOf(auto_commit)));
		fireStatusUpdate(getCurrentCatalog());
	}

	/**
	 * Sets the auto commit flag for the sql connection owned by this connection
	 */
	void setAutoCommit(Connection conn, boolean auto_commit) throws SQLException {
		assert (conn == m_metadata_connection);
		conn.setAutoCommit(auto_commit);
	}

	/**
	 * Sets the default auto commit flag for all connections
	 */
	public void setDefaultAutoCommit(boolean auto_commit) {
		try {
			ObjectStore os = getObjectStore();
			os.store(ID_AUTO_COMMIT, Boolean.valueOf(auto_commit));
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Sets the implementation object instance for a given interface name
	 * 
	 * @param componentId
	 *            the name of the interface
	 * @param impl
	 *            the object to set
	 */
	public void setImplementation(String componentId, Object impl) {
		m_implementations.put(componentId, impl);
	}

	/**
	 * Sets the ResultSet scroll type flag used for queries.
	 */
	public void setQueryTableCounts(boolean bQuery) {
		try {
			ObjectStore os = getObjectStore();
			os.store(TSDatabase.QUERY_TABLE_COUNTS, Boolean.valueOf(bQuery));
		} catch (Exception se) {
			TSUtils.printException(se);
		}
	}

	/**
	 * Sets the max rows to return in a query
	 */
	public static void setMaxQueryRows(int max_rows) {
		TSUserPropertiesUtils.setInteger(TSConnection.ID_MAX_QUERY_ROWS, max_rows);
		TSConnectionMgr.fireStatusUpdate();
	}

	/**
	 * Sets the ResultSet scroll type flag used for queries.
	 */
	public void setResultSetScrollType(int scroll_type) {
		try {
			DatabaseMetaData metadata = getMetaData();
			if (metadata.supportsResultSetType(scroll_type)) {
				ObjectStore os = getObjectStore();
				os.store(TSDatabase.RESULT_SET_TYPE, TSUtils.getInteger(scroll_type));
			} else {
				assert (false);
			}
		} catch (Exception se) {
			TSUtils.printException(se);
		}
	}

	/**
	 * Sets the object that creates the various service implementations (see.
	 * getImplementation)
	 */
	void setServiceFactory(TSServiceFactory factory) {
		m_servicefactory = factory;
	}

	/**
	 * Sets the transaction isolation for the application
	 */
	public void setTransactionIsolation(int isolation) {

		Connection c = getMetaDataConnection();
		int oldisolation = 0;

		try {
			oldisolation = c.getTransactionIsolation();
		} catch (SQLException se) {
			DbLogger.log(se);
			DbLogger.fine(I18N.getLocalizedMessage("Get Transaction Isolation Failed"));
			return;
		}

		try {
			DatabaseMetaData metadata = c.getMetaData();
			if (metadata.supportsTransactionIsolationLevel(isolation)) {
				c.setTransactionIsolation(isolation);
				DbLogger.fine(I18N.format("Set_Transaction_Isolation_1", TSConnection.getIsolationString(isolation)));
			} else {
				DbLogger.fine(I18N.format("Transaction_Isolation_Not_Supported_1",
						TSConnection.getIsolationString(isolation)));
				isolation = oldisolation;
				c.setTransactionIsolation(isolation);
				DbLogger.fine(I18N.format("Set_Transaction_Isolation_1", TSConnection.getIsolationString(isolation)));
			}

			ObjectStore os = getObjectStore();
			try {
				os.store(TRANSACTION_ISOLATION, new Integer(isolation));
			} catch (java.io.IOException ioe) {
				TSUtils.printException(ioe);
			}

			try {
				Iterator iter = m_writeconnections.iterator();
				while (iter.hasNext()) {
					Connection wc = (Connection) iter.next();
					wc.setTransactionIsolation(isolation);
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}

		} catch (SQLException e) {

			try {
				DbLogger.log(e);
				DbLogger.fine(I18N.format("Set_Transaction_Isolation_1", TSConnection.getIsolationString(oldisolation)));
				c.setTransactionIsolation(oldisolation);

				Iterator iter = m_writeconnections.iterator();
				while (iter.hasNext()) {
					Connection wc = (Connection) iter.next();
					wc.setTransactionIsolation(oldisolation);
				}

				ObjectStore os = getObjectStore();
				try {
					os.store(TRANSACTION_ISOLATION, new Integer(oldisolation));
				} catch (java.io.IOException ioe) {
					TSUtils.printException(e);
				}
			} catch (Exception wse) {
				TSUtils.printException(e);
			}

		}
	}

	/**
	 * @return true if this database supports canceling a SQL statement
	 */
	public boolean supportsCancelStatement() {
		TSDatabase db = (TSDatabase) getImplementation(TSDatabase.COMPONENT_ID);
		return db.supportsCancelStatement();
	}

	/**
	 * @return true if the given database supports more than one catalog opened
	 *         for a connection as well as the ability to query between catalogs
	 *         for this connection. PostgreSQL <= 7.3 does not support this by
	 *         MySQL does.
	 */
	public boolean supportsCatalogs() {
		if (m_supportscatalogs == null) {
			TSDatabase db = (TSDatabase) getImplementation(TSDatabase.COMPONENT_ID);
			try {
				m_supportscatalogs = Boolean.valueOf(db.supportsCatalogs());
			} catch (Exception e) {
				TSUtils.printException(e);
				return false;
			}
		}
		return m_supportscatalogs.booleanValue();
	}

	/**
	 * Retrieves whether it is possible to have multiple ResultSet objects
	 * returned from a CallableStatement object simultaneously.
	 */
	public boolean supportsMultipleOpenResults() {
		if (m_supportsmultiresults == null) {
			try {
				DatabaseMetaData metadata = getMetaData();
				m_supportsmultiresults = Boolean.valueOf(metadata.supportsMultipleOpenResults());
			} catch (Exception e) {
				TSUtils.printDebugException(e);
				m_supportsmultiresults = Boolean.FALSE;
			} catch (Error e) {
				TSUtils.printDebugException(e);
				m_supportsmultiresults = Boolean.FALSE;
			}
		}
		return m_supportsmultiresults.booleanValue();
	}

	/**
	 * @return true if the given database instance supports schemas
	 */
	public boolean supportsSchemas() {
		if (m_supportsschemas == null) {
			TSDatabase db = (TSDatabase) getImplementation(TSDatabase.COMPONENT_ID);
			try {
				m_supportsschemas = Boolean.valueOf(db.supportsSchemas());
			} catch (Exception e) {
				TSUtils.printException(e);
				return false;
			}
		}
		return m_supportsschemas.booleanValue();
	}

	/**
	 * @return true if the given database instance supports transactions
	 */
	public boolean supportsTransactions() throws SQLException {
		if (m_supportstransactions == null) {
			TSDatabase db = (TSDatabase) getImplementation(TSDatabase.COMPONENT_ID);
			m_supportstransactions = Boolean.valueOf(db.supportsTransactions());
		}
		return m_supportstransactions.booleanValue();
	}

	/**
	 * This method is used for testing. If the current connection supports
	 * schemas, this method verifies that the given schema is not the virtual
	 * schema. If the current connection supports catalogs, this method verifies
	 * that the given schema is not the virtual catalog.
	 */
	public void validate(Catalog catalog, Schema schema) {
		if (TSUtils.isDebug()) {
			if (schema != null) {
				if (supportsSchemas()) {
					assert (schema != Schema.VIRTUAL_SCHEMA);
				} else {
					assert (schema == Schema.VIRTUAL_SCHEMA);
				}
			}

			if (catalog != null) {
				if (supportsCatalogs()) {
					assert (catalog != Catalog.VIRTUAL_CATALOG);
				} else {
					assert (catalog == Catalog.VIRTUAL_CATALOG);
				}
			}
		}

	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_connectioninfo = (ConnectionInfo) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_connectioninfo);
	}

}
