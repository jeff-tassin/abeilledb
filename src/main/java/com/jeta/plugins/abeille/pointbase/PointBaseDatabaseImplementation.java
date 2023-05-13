package com.jeta.plugins.abeille.pointbase;

import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.plugins.abeille.generic.GenericDatabaseImplementation;

/**
 * This is the PointBase implementation for the database interface
 * 
 * @author Jeff Tassin
 */
public class PointBaseDatabaseImplementation extends GenericDatabaseImplementation {

	/**
	 * Converts the case for the given object name to the case supported by the
	 * database. This is needed because some databases require exact case for
	 * table names (HSQLDB). So, if the user types in a table name, but the case
	 * does not match, we still want to be able to get the table from the db.
	 * This call converts to the case needed.
	 */
	public String convertCase(String objName) {
		return GenericDatabaseImplementation.toUpper(objName);
	}

	/**
	 * @return true if the given database supports more than one catalog opened
	 *         for a connection as well as the ability to query between catalogs
	 *         for this connection.
	 */
	public boolean supportsCatalogs() throws SQLException {
		return false;
	}

	/**
	 * @return false. Postgres does not support schemas
	 */
	public boolean supportsSchemas() throws SQLException {
		return true;
	}
}
