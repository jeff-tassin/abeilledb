package com.jeta.plugins.abeille.mckoi;

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
 * This is the McKoi implementation for the database interface
 * 
 * @author Jeff Tassin
 */
public class McKoiDatabaseImplementation extends GenericDatabaseImplementation {

	/**
	 * Converts the case for the given object name to the case supported by the
	 * database. This is needed because some databases require exact case for
	 * table names (HSQLDB). So, if the user types in a table name, but the case
	 * does not match, we still want to be able to get the table from the db.
	 * This call converts to the case needed.
	 */
	public String convertCase(String objName) {
		return objName;
	}

	/**
	 * @return the type name used for auto increment types
	 */
	private String getAutoIncrementType() {
		return "INTEGER IDENTITY";
	}

	/**
	 * @return the current schema
	 */
	public Schema getCurrentSchema() throws SQLException {
		Schema schema = super.getCurrentSchema();
		if (schema == null) {
			TSConnection conn = getConnection();
			ConnectionInfo info = conn.getConnectionInfo();
			schema = conn.getSchema(Catalog.VIRTUAL_CATALOG, info.getName());
			if (schema == null)
				schema = conn.getSchema(Catalog.VIRTUAL_CATALOG, "APP");

			if (schema == null) {
				Collection schemas = conn.getModel(Catalog.VIRTUAL_CATALOG).getSchemas();
				Iterator iter = schemas.iterator();
				while (iter.hasNext()) {
					schema = (Schema) iter.next();
					break;
				}
			}
		}
		return schema;
	}

	/**
	 * @return true if the given database supports more than one catalog opened
	 *         for a connection as well as the ability to query between catalogs
	 *         for this connection. PostgreSQL <= 7.3 does not support this by
	 *         MySQL does.
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
