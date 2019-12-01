package com.jeta.abeille.database.model;

import java.util.Collection;
import java.sql.SQLException;

/**
 * This is the interface definition for foreign key retrieval within the
 * application. Most database vendors will use the JDBC driver implementation;
 * however, some databases such as Postgres don't have this suport yet.
 * 
 * @author Jeff Tassin
 */
public interface TSForeignKeys {
	public static final String COMPONENT_ID = "database.TSForeignKeys";

	/**
	 * Creates a new foreign key in the database
	 */
	public void createForeignKey(DbForeignKey newkey) throws SQLException;

	/**
	 * Drops a foreign key in the database
	 */
	public void dropForeignKey(DbForeignKey newkey, boolean cascade) throws SQLException;

	/**
	 * Setting the cache will cause the next call to getForeignKeys to cache all
	 * the keys for all tables in a given schema. This improves performance
	 * significantly and is used when loading the model. Once the model is
	 * loaded, turn the cache off. This cause all metadat requests to be
	 * forwarded directly to the database.
	 */
	public void enableCache(boolean bcache) throws SQLException;

	/**
	 * @param tableId
	 *            the id of the table whose foreign keys we wish to retrieve
	 */
	public Collection getForeignKeys(TableId tableId) throws SQLException;

	/**
	 * This method returns an object for a given supported feature relating to
	 * foreign keys. For example, databases support different foreign key
	 * constraints for ON_UPDATE and ON_DELETE. So, the client application can
	 * query this service to see what features are supported and update the GUI
	 * accordingly IMPORTANT: By convention, you should begin any feature names
	 * with the string: checked.feature. FEATURE_FOREIGN_KEY_ON_UPDATE =
	 * "checked.feature.foreign.key.on.update"; FEATURE_FOREIGN_KEY_ON_DELETE =
	 * "checked.feature.foreign.key.on.delete"; etc.
	 * 
	 * @return an object that is specific for the requested feature. Some
	 *         features require a collection, and others might require a
	 *         Boolean. It depends.
	 */
	public Object getSupportedFeature(String featuresName);

	/**
	 * Convenience method that allows user to send a default value if the
	 * feature is not supported.
	 * 
	 * @return an object that is specific for the requested feature. Some
	 *         features require a collection, and others might require a
	 *         Boolean. It depends.
	 */
	public Object getSupportedFeature(String featuresName, Object defaultValue);

	/**
	 * Modifies an existing foreign key
	 */
	public void modifyForeignKey(DbForeignKey newkey, DbForeignKey oldKey) throws SQLException;

	/**
	 * Sets the connection which this interface will operate on
	 * 
	 * @param conn
	 *            the connection to set
	 */
	public void setConnection(TSConnection conn);

}
