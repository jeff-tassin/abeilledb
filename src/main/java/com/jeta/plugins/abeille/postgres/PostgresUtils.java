package com.jeta.plugins.abeille.postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.utils.TSUtils;

/**
 * Some common utility methods
 * 
 * @author Jeff Tassin
 */
public class PostgresUtils {
	/**
	 * @return the description for the given object id
	 */
	public static String getDescription(Connection conn, boolean supportsSchemas, int oid) throws SQLException {
		String description = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			StringBuffer sqlbuff = new StringBuffer();
			if (supportsSchemas) {
				sqlbuff.append("select * from pg_catalog.pg_description where objoid = ");
				sqlbuff.append(oid);
			} else {
				sqlbuff.append("select * from pg_description where objoid = ");
				sqlbuff.append(oid);
			}

			ResultSet rset = stmt.executeQuery(sqlbuff.toString());
			if (rset.next()) {
				description = rset.getString("description");

				if (rset.next()) {
					// we have multiple results, this is an error
					if (TSUtils.isDebug()) {
						System.out.println("Multiple results for getDescription oid = " + oid);
					}
					description = null;
				}
			}
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}

		return description;
	}

	/**
	 * Gets the SQL needed to query the pg_class table for a given type
	 * 
	 * @param relname
	 *            the name to get (can be null)
	 */
	public static String pgClassSQL(TSConnection connection, Schema schema, DbObjectType objtype, String relname) {
		StringBuffer sqlbuff = new StringBuffer();
		if (connection.supportsSchemas()) {
			sqlbuff.append("SELECT oid, * from pg_catalog.pg_class WHERE pg_catalog.pg_class.relnamespace = pg_catalog.pg_namespace.oid AND pg_class.relkind = '");
			sqlbuff.append(relKind(objtype));
			sqlbuff.append("'");
			sqlbuff.append(" AND pg_namespace.nspname = '");
			sqlbuff.append(schema.getName());
			sqlbuff.append("'");

		} else {
			sqlbuff.append("SELECT oid, * from pg_class WHERE relkind = '");
			sqlbuff.append(relKind(objtype));
			sqlbuff.append("'");
		}

		if (relname != null) {
			sqlbuff.append(" AND pg_class.relname = '");
			sqlbuff.append(relname);
			sqlbuff.append("'");
		}
		return sqlbuff.toString();
	}

	/**
	 * @return the relkind flag used to indicate a given database type in the
	 *         pg_class table
	 */
	public static char relKind(DbObjectType objtype) {
		if (objtype == DbObjectType.TABLE) {
			return 'r';
		} else if (objtype == DbObjectType.SEQUENCE) {
			return 'S';
		} else if (objtype == DbObjectType.VIEW) {
			return 'v';
		} else {
			assert (false);
			return '\0';
		}
	}
}
