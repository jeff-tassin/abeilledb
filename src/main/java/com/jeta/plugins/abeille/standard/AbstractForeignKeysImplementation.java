package com.jeta.plugins.abeille.standard;

import java.sql.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.ForeignKeyConstraints;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSForeignKeys;

import com.jeta.abeille.gui.modeler.ForeignKeyView;

import com.jeta.foundation.utils.EmptyCollection;

/**
 * This is the implementation for foreign key retrieval for Postgres.
 * 
 * @author Jeff Tassin
 */
public abstract class AbstractForeignKeysImplementation implements TSForeignKeys {
	private TSConnection m_connection; // the connection to the database

	public static final Object[] STANDARD_ON_UPDATE = { "NO ACTION", "CASCADE", "SET NULL", "SET DEFAULT" };
	public static final Object[] STANDARD_ON_DELETE = { "NO ACTION", "CASCADE", "SET NULL", "SET DEFAULT" };

	public AbstractForeignKeysImplementation() {

	}

	public AbstractForeignKeysImplementation(TSConnection conn) {
		m_connection = conn;
	}

	/**
	 * Creates a new foreign key in the database
	 */
	public void createForeignKey(DbForeignKey newkey) throws SQLException {

	}

	/**
	 * Drops a foreign key in the database
	 */
	public void dropForeignKey(DbForeignKey newkey, boolean cascade) throws SQLException {

	}

	/**
	 * Setting the cache will cause the next call to getForeignKeys to cache all
	 * the keys for all tables in a given schema. This improves performance
	 * significantly and is used when loading the model. Once the model is
	 * loaded, turn the cache off. This cause all metadat requests to be
	 * forwarded directly to the database.
	 */
	public synchronized void enableCache(boolean bcache) throws SQLException {
		// ignore
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @param tableId
	 *            the id of the table whose foreign keys we wish to retrieve
	 * @return a collection of DbForeignKey objects for the given table
	 */
	public Collection getForeignKeys(TableId tableid) throws SQLException {
		return EmptyCollection.getInstance();
	}

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
	public Object getSupportedFeature(String featuresName) {
		return getSupportedFeature(featuresName, null);
	}

	/**
	 * Convenience method that allows user to send a default value if the
	 * feature is not supported.
	 * 
	 * @return an object that is specific for the requested feature. Some
	 *         features require a collection, and others might require a
	 *         Boolean. It depends.
	 */
	public Object getSupportedFeature(String featureName, Object defaultValue) {
		assert (featureName != null);
		assert (featureName.indexOf("checked.") == 0);
		TSConnection tsconn = getConnection();
		Database db = tsconn.getDatabase();
		if (ForeignKeyView.FEATURE_FOREIGN_KEY_ON_UPDATE.equals(featureName)) {
			return STANDARD_ON_UPDATE;
		} else if (ForeignKeyView.FEATURE_FOREIGN_KEY_ON_DELETE.equals(featureName)) {
			return STANDARD_ON_DELETE;
		} else if (ForeignKeyView.FEATURE_FOREIGN_KEY_DEFERRABLE.equals(featureName)) {
			return Boolean.TRUE;
		} else {
			return null;
		}
	}

	/**
	 * Modifies an existing foreign key
	 */
	public void modifyForeignKey(DbForeignKey newkey, DbForeignKey oldKey) throws SQLException {

	}

	/**
	 * Sets the connection which this interface will operate on
	 * 
	 * @param conn
	 *            the connection to set
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
