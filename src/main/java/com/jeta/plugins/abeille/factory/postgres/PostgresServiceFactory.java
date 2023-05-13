package com.jeta.plugins.abeille.factory.postgres;

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

import com.jeta.abeille.gui.help.SQLReferenceService;

import com.jeta.plugins.abeille.postgres.PostgresSecurityServiceImplementation;
import com.jeta.plugins.abeille.postgres.PostgresSequenceServiceImplementation;
import com.jeta.plugins.abeille.postgres.PostgresStoredProceduresImplementation;
import com.jeta.plugins.abeille.postgres.TriggerServiceImplementation;
import com.jeta.plugins.abeille.postgres.PostgresDatabaseImplementation;
import com.jeta.plugins.abeille.postgres.ForeignKeysImplementation;
import com.jeta.plugins.abeille.postgres.PostgresTableImplementation;
import com.jeta.plugins.abeille.postgres.PostgresViewService;
import com.jeta.plugins.abeille.postgres.PostgresSQLReferenceService;

/**
 * Factory that create the various services for PostgeSQL
 * 
 * @author Jeff Tassin
 */
public class PostgresServiceFactory implements TSServiceFactory {
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

			m_servicecache.put(SecurityService.COMPONENT_ID, new PostgresSecurityServiceImplementation(m_connection));
			m_servicecache.put(SequenceService.COMPONENT_ID, new PostgresSequenceServiceImplementation(m_connection));
			m_servicecache.put(StoredProcedureService.COMPONENT_ID, new PostgresStoredProceduresImplementation(
					m_connection));
			m_servicecache.put(TriggerService.COMPONENT_ID, new TriggerServiceImplementation(m_connection));

			TSDatabase dbimpl = new PostgresDatabaseImplementation();
			dbimpl.setConnection(m_connection);
			m_servicecache.put(TSDatabase.COMPONENT_ID, dbimpl);

			m_servicecache.put(TSForeignKeys.COMPONENT_ID, new ForeignKeysImplementation(m_connection));
			m_servicecache.put(TSTable.COMPONENT_ID, new PostgresTableImplementation(m_connection));
			m_servicecache.put(ViewService.COMPONENT_ID, new PostgresViewService(m_connection));

			m_servicecache.put(SQLReferenceService.COMPONENT_ID, new PostgresSQLReferenceService());

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
