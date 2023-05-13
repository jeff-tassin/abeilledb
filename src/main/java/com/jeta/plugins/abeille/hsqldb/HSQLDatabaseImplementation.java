package com.jeta.plugins.abeille.hsqldb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.plugins.abeille.generic.GenericDatabaseImplementation;

/**
 * This is the HSQLDB implementation for the database interface
 * 
 * @author Jeff Tassin
 */
public class HSQLDatabaseImplementation extends GenericDatabaseImplementation {

	/**
	 * Converts the case for the given object name to the case supported by the
	 * database. This is needed because some databases require exact case for
	 * table names (HSQLDB). So, if the user types in a table name, but the case
	 * does not match, we still want to be able to get the table from the db.
	 * This call converts to the case needed.
	 */
	public String convertCase(String objName) {
		if (objName == null)
			return null;
		else
			return objName.toUpperCase();
	}

	/**
	 * @return the type name used for auto increment types
	 */
	private String getAutoIncrementType() {
		return "INTEGER IDENTITY";
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
		return false;
	}

	/**
	 * @return true if this database supports transactions
	 */
	public boolean supportsTransactions() {
		return true;
	}

}
