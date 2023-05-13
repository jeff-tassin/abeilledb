package com.jeta.plugins.abeille.factory.mysql;

import java.util.HashMap;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSServiceFactory;

import com.jeta.abeille.database.security.SecurityService;
import com.jeta.abeille.database.sequences.SequenceService;
import com.jeta.abeille.database.procedures.StoredProcedureService;
import com.jeta.abeille.database.triggers.TriggerService;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.model.TSForeignKeys;
import com.jeta.abeille.database.model.TSTable;
import com.jeta.abeille.database.model.ViewService;

import com.jeta.plugins.abeille.generic.GenericForeignKeysImplementation;

import com.jeta.plugins.abeille.standard.DefaultViewService;

import com.jeta.plugins.abeille.mysql.MySQLSecurityServiceImplementation;
import com.jeta.plugins.abeille.mysql.MySQLSequenceServiceImplementation;
import com.jeta.plugins.abeille.mysql.MySQLStoredProceduresImplementation;
import com.jeta.plugins.abeille.mysql.MySQLTriggerServiceImplementation;
import com.jeta.plugins.abeille.mysql.MySQLDatabaseImplementation;
import com.jeta.plugins.abeille.mysql.MySQLForeignKeysImplementation;
import com.jeta.plugins.abeille.mysql.MySQLTableImplementation;

/**
 * Factory that create the various services for PostgeSQL
 * 
 * @author Jeff Tassin
 */
public class MySQLServiceFactory implements TSServiceFactory {
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
			TSDatabase dbimpl = new MySQLDatabaseImplementation();
			dbimpl.setConnection(m_connection);
			m_servicecache.put(TSDatabase.COMPONENT_ID, dbimpl);

			m_servicecache.put(SecurityService.COMPONENT_ID, new MySQLSecurityServiceImplementation(m_connection));
			m_servicecache.put(SequenceService.COMPONENT_ID, new MySQLSequenceServiceImplementation(m_connection));
			m_servicecache.put(StoredProcedureService.COMPONENT_ID, new MySQLStoredProceduresImplementation(
					m_connection));
			m_servicecache.put(TriggerService.COMPONENT_ID, new MySQLTriggerServiceImplementation(m_connection));
			// m_servicecache.put( TSForeignKeys.COMPONENT_ID, new
			// MySQLForeignKeysImplementation( m_connection ) );
			m_servicecache.put(TSForeignKeys.COMPONENT_ID, new GenericForeignKeysImplementation(m_connection));
			m_servicecache.put(TSTable.COMPONENT_ID, new MySQLTableImplementation(m_connection));
			m_servicecache.put(ViewService.COMPONENT_ID, new DefaultViewService(m_connection));
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
