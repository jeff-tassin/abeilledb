package com.jeta.plugins.abeille.sqlserver;

import java.sql.SQLException;

import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.plugins.abeille.generic.GenericDatabaseImplementation;

public class SQLServerDatabaseImplementation extends GenericDatabaseImplementation {


	/**
	 * @return true if the given database supports more than one catalog opened
	 *         for a connection as well as the ability to query between catalogs
	 *         for this connection.
	 */
	public boolean supportsCatalogs() throws SQLException {
		return true;
	}

	/**
	 * @return false.
	 */
	public boolean supportsSchemas() throws SQLException {
		return false;
	}
	public boolean rollbackOnException() {
		return false;
	}

	public String getFullyQualifiedName( DbObjectId objId ) {
		return objId.getObjectName();
	}

}