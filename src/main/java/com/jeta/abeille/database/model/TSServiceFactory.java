package com.jeta.abeille.database.model;

/**
 * This interface defines a set of methods that a given database would implement
 * to create the various services required by the application (e.g.
 * TSForeignKeys, TSTable, TriggerService, StoredProcedureService, etc.)
 * 
 * The implementation class is declared in the
 * com/jeta/abeille/resources/DATABASE_Plugins.properties file. It is
 * instantatiated at startup by TSConnectionMgr and set in the TSConnection
 * object.
 * 
 * @author Jeff Tassin
 */
public interface TSServiceFactory {
	public static final String COMPONENT_ID = "database.ServiceFactory";

	/**
	 * Creates an instance of the given component name
	 */
	public Object createService(String componentid);

	/**
	 * Sets the connection in the factory
	 */
	public void setConnection(TSConnection conn);
}
