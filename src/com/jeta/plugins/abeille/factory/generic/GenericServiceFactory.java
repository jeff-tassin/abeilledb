package com.jeta.plugins.abeille.factory.generic;

import java.util.HashMap;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSServiceFactory;

import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.model.TSForeignKeys;
import com.jeta.abeille.database.model.TSTable;
import com.jeta.abeille.database.model.ViewService;
import com.jeta.abeille.database.security.SecurityService;

import com.jeta.plugins.abeille.standard.DefaultViewService;

import com.jeta.plugins.abeille.generic.GenericDatabaseImplementation;
import com.jeta.plugins.abeille.generic.GenericForeignKeysImplementation;
import com.jeta.plugins.abeille.generic.GenericTableImplementation;
import com.jeta.plugins.abeille.generic.GenericViewService;

import com.jeta.plugins.abeille.hsqldb.HSQLDatabaseImplementation;
import com.jeta.plugins.abeille.oracle.OracleDatabaseImplementation;

/**
 * Factory that create the various services for PostgeSQL
 * 
 * @author Jeff Tassin
 */
public class GenericServiceFactory implements TSServiceFactory {
	/**
	 * The database connection
	 */
	private TSConnection m_connection;

	/**
	 * Cache the service based on the component id of the service
	 */
	private HashMap m_servicecache;

	/**
	 * Creates an instance of the given component name
	 */
	public synchronized Object createService(String componentid) {
		if (m_servicecache == null) {
			m_servicecache = new HashMap();
			TSDatabase dbimpl = null;
			if (m_connection.getDatabase() == Database.ORACLE) {
				dbimpl = new OracleDatabaseImplementation();
			} else if (m_connection.getDatabase() == Database.HSQLDB) {
				dbimpl = new HSQLDatabaseImplementation();
			} else if (Database.POINTBASE.equals(m_connection.getDatabase())) {
				dbimpl = new com.jeta.plugins.abeille.pointbase.PointBaseDatabaseImplementation();
			} else if ( Database.SQLSERVER.equals( m_connection.getDatabase() )) {
				dbimpl = new com.jeta.plugins.abeille.sqlserver.SQLServerDatabaseImplementation();
			} else {
				dbimpl = new GenericDatabaseImplementation();
			}
			dbimpl.setConnection(m_connection);

			m_servicecache.put(TSDatabase.COMPONENT_ID, dbimpl);
			m_servicecache.put(TSTable.COMPONENT_ID, new GenericTableImplementation(m_connection));
			m_servicecache.put(TSForeignKeys.COMPONENT_ID, new GenericForeignKeysImplementation(m_connection));
			m_servicecache.put(ViewService.COMPONENT_ID, new GenericViewService(m_connection));

		}
		return m_servicecache.get(componentid);
	}

	/**
	 * Sets the connection in the factory
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
