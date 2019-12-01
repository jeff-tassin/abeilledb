package com.jeta.abeille.database.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import java.util.logging.Logger;

import java.sql.SQLException;
import java.sql.Connection;

import com.jeta.foundation.app.ApplicationStateStore;
import com.jeta.foundation.interfaces.resources.ResourceLoader;
import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.TSListener;
import com.jeta.foundation.componentmgr.TSEvent;
import com.jeta.foundation.componentmgr.TSNotifier;

import com.jeta.foundation.interfaces.app.ObjectStore;

import com.jeta.abeille.database.procedures.StoredProcedureInterface;
import com.jeta.abeille.database.procedures.StoredProcedureService;
import com.jeta.abeille.database.security.SecurityService;
import com.jeta.abeille.database.security.SecurityManager;
import com.jeta.abeille.database.sequences.SequenceService;
import com.jeta.abeille.database.triggers.TriggerService;
import com.jeta.abeille.logger.DbLogger;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is responsible for managing all database connections in the
 * application. Furthermore, this class is responsible for creating a database
 * connection was well as setting up the necessary plugins for that connection.
 * 
 * @author Jeff Tassin
 */
public class TSConnectionMgr {
	/**
	 * the map of all open connections in the application keyed on ConnectionId
	 * objects to TSConnections (values)
	 */
	private static HashMap m_connections = new HashMap();

	private static ConnectionListener m_listener = null; // used only to get
															// messages from
															// TSNotifier

	public static final String COMPONENT_ID = "jeta.abeille.database.model.tsconnmgr";
	public static final String MSG_GROUP = COMPONENT_ID;
	// messages
	public static final String MSG_CONNECTION_CREATED = "connection.created";

	/** a set of object stores */
	private static HashMap m_stores = new HashMap();

	static {
		// we do this solely to get disconnect events
		m_listener = new ConnectionListener();
		// TSNotifier.registerInterest( m_listener, TSConnection.COMPONENT_ID );
	}

	public static void closeConnection(TSConnection conn) {
		synchronized (TSConnectionMgr.class) {
			TSConnection myconn = (TSConnection) m_connections.remove(conn.getId());
			assert (myconn == conn);
			conn.close();
		}
	}

	public static TSConnection createConnection(ConnectionInfo info) throws SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		TSConnection connection = new TSConnection(info);
		m_connections.put(connection.getId(), connection);

		String pluginfile = "com.jeta.abeille.resources." + info.getDatabase().toString() + "_Plugins";
		ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
		ResourceBundle bundle = ResourceBundle.getBundle(pluginfile, Locale.getDefault(), loader.getClassLoader());

		// get database implementation
		String factoryplugin = bundle.getString(TSServiceFactory.COMPONENT_ID);
		Class factorypluginclass = Class.forName(factoryplugin);

		TSServiceFactory factory = (TSServiceFactory) factorypluginclass.newInstance();
		factory.setConnection(connection);
		connection.setServiceFactory(factory);

		try {
			ObjectStore os = getObjectStore(connection.getId());
			Integer iso = (Integer) os.load(TSConnection.TRANSACTION_ISOLATION);
			if (iso == null) {
				DbLogger.fine(I18N.format("Set_Transaction_Isolation_1",
						TSConnection.getIsolationString(connection.getTransactionIsolation())));
			} else {
				connection.setTransactionIsolation(iso.intValue());
			}
		} catch (java.io.IOException ioe) {
			TSUtils.printException(ioe);
		}

		TSNotifier n = TSNotifier.getInstance(connection.getId().getUID());
		n.registerInterest(m_listener, TSConnection.COMPONENT_ID);
		// fire message to any listeners of this component that a connection has
		// been created
		n = TSNotifier.getInstance(COMPONENT_ID);
		n.fireEvent(connection, MSG_GROUP, MSG_CONNECTION_CREATED, connection);

		Logger logger = Logger.getLogger(com.jeta.foundation.componentmgr.ComponentNames.APPLICATION_LOGGER);
		logger.fine(I18N.format("connected_to_1", connection.getUrl()));

		return connection;
	}

	/**
	 * Creates a dummy connection for development. Allows us to work on the
	 * application without a live connection
	 */
	public static TSConnection createDummyConnection(String databaseName) {
		// TSConnection connection = new TSConnection( databaseName );
		// m_connections.put( connection.getId(), connection );
		// return connection;
		return null;
	}

	/**
	 * Sends a status update to all opened connections
	 */
	public static synchronized void fireStatusUpdate() {
		Collection connections = m_connections.values();
		Iterator iter = connections.iterator();
		while (iter.hasNext()) {
			TSConnection tsconn = (TSConnection) iter.next();
			Collection cats = tsconn.getCatalogs();
			Iterator citer = cats.iterator();
			while (citer.hasNext()) {
				Catalog cat = (Catalog) citer.next();
				tsconn.fireStatusUpdate(cat);
			}
		}
	}

	/**
	 * @return the connection for the given id. If the connection is not found
	 *         in the cache, it is created from the list of defined connections
	 */
	public static synchronized TSConnection getConnection(ConnectionId id) throws SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		if (id == null)
			return null;
		else
			return (TSConnection) m_connections.get(id);
	}

	/**
	 * @return an object store for the given connection id
	 */
	public static synchronized ObjectStore getObjectStore(ConnectionId cid) {
		ObjectStore os = (ObjectStore) m_stores.get(cid);
		if (os == null) {
			ApplicationStateStore ass = new ApplicationStateStore(cid.getUID());
			os = ass;
			m_stores.put(cid, os);
		}
		return os;
	}

	/**
	 * Called when application is being shut down. Disconnects all open
	 * connections.
	 */
	public static void shutdown() {
		// TSNotifier.removeListener( m_listener );
		Iterator iter = m_connections.values().iterator();
		while (iter.hasNext()) {
			TSConnection connection = (TSConnection) iter.next();
			ObjectStore os = getObjectStore(connection.getId());
			try {
				os.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This class is used to get disconnect events from each connection
	 */
	static class ConnectionListener implements TSListener {
		/**
		 * Implementation of TSListener interface. We override here primarily to
		 * get disconnect events
		 */
		public void tsNotify(TSEvent evt) {
			// if this is a disconnect event, remove from connections hashmap
			if (evt.getGroup().equals(TSConnection.COMPONENT_ID)
					&& evt.getMessage().equals(TSConnection.MSG_DISCONNECT)) {
				TSConnection conn = (TSConnection) evt.getSender();
				m_connections.remove(conn.getId());
			}
		}
	}

}
