package com.jeta.plugins.abeille.postgres;

import java.lang.reflect.Method;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;

import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.database.procedures.ParameterDirection;
import com.jeta.abeille.database.procedures.ProcedureLanguage;
import com.jeta.abeille.database.procedures.ProcedureParameter;
import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.database.procedures.StoredProcedureInterface;

import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This service allows the user to enumerate all stored procedures in the
 * database as well as lookup individual stored procedures.
 * 
 * @todo we should probabaly cache procedures here
 * @author Jeff Tassin
 */
public class PostgresStoredProceduresImplementation implements StoredProcedureInterface {
	/** the database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public PostgresStoredProceduresImplementation() {

	}

	/**
	 * ctor
	 */
	public PostgresStoredProceduresImplementation(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * drops the given procedure from the database
	 */
	public void dropProcedure(StoredProcedure proc, boolean cascade) throws SQLException {
		SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Drop Function"),
				getDropSQL(proc, cascade));
	}

	/**
	 * @return the procedure arguments as a comma-separated string within
	 *         parenthesis. This is used to create and drop procedures
	 */
	private static void getArgsString(StringBuffer sql, StoredProcedure proc) {
		sql.append('(');
		for (int index = 0; index < proc.getParameterCount(); index++) {
			ProcedureParameter param = proc.getParameter(index);
			String vtype = param.getVendorType();
			if (vtype == null || vtype.length() == 0) {
				vtype = DbUtils.getJDBCTypeName(param.getType());
			}

			if (index > 0)
				sql.append(", ");
			sql.append(vtype);
		}
		sql.append(')');
	}

	/**
	 * @return the SQL to create the given stored procedure in the database
	 */
	private String getCreateSQL(StoredProcedure proc, boolean breplace) throws SQLException {
		/**
		 * CREATE FUNCTION trigger_test4(text,text) RETURNS opaque AS ' BEGIN
		 * NEW.sval2 = TG_ARGV[0]; NEW.sval3 = $1; RETURN NEW; END; ' LANGUAGE
		 * 'plpgsql'
		 */

		StringBuffer sql = new StringBuffer();
		if (breplace)
			sql.append("CREATE OR REPLACE FUNCTION ");
		else
			sql.append("CREATE FUNCTION ");

		sql.append(proc.getFullyQualifiedName());
		getArgsString(sql, proc);
		sql.append(" RETURNS ");

		ProcedureParameter param = proc.getReturnParameter();
		String vtype = param.getVendorType();
		if (vtype == null || vtype.length() == 0) {
			vtype = DbUtils.getJDBCTypeName(param.getType());
		}

		sql.append(vtype);
		sql.append(" AS ");
		// the procedure source may have string 'literals' declared. So, we need
		// to covert any quote' characters to SQL ''
		sql.append(DbUtils.toSQL(proc.getSource(), '\''));
		sql.append(" LANGUAGE '");
		sql.append(proc.getLanguage().getLanguage());
		sql.append("'");
		return sql.toString();
	}

	/**
	 * @return the name of the current database
	 */
	private String getCurrentDatabase() {
		try {
			String database = m_connection.getMetaDataConnection().getCatalog();
			if (database == null || database.length() == 0) {
				Driver driver = m_connection.getJDBCDriver();
				Class[] paramtypes = new Class[0];
				Method m = driver.getClass().getDeclaredMethod("database", paramtypes);
				Object[] params = new Object[0];
				database = (String) m.invoke(driver, params);
			}
			return database;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * @return the SQL that drops the given stored procedure
	 */
	private String getDropSQL(StoredProcedure proc, boolean cascade) {
		StringBuffer sql = new StringBuffer();
		sql.append("DROP FUNCTION ");
		sql.append(proc.getSignature());
		// getArgsString( sql, proc );
		if (cascade)
			sql.append(" CASCADE");

		return sql.toString();
	}

	/**
	 * @returns the maximum system oid
	 */
	private int getMaxSystemOID() throws SQLException {
		Statement stmt = null;
		Connection conn = m_connection.getMetaDataConnection();
		try {
			String database = getCurrentDatabase();
			String sql = null;
			if (m_connection.supportsSchemas()) {
				sql = "select * from pg_catalog.pg_database";
			} else {
				sql = "select * from pg_database";
			}
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			while (rset.next()) {
				String datname = rset.getString("datname");
				if (database.equalsIgnoreCase(datname)) {
					return rset.getInt("datlastsysoid");
				}
			}
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				// eat it here
			}
		}
		return 0;
	}

	/**
	 * Finds all procedures
	 * 
	 * @param oidOp
	 *            the operator to constrain how procedures are searched against
	 *            the max system oid (e.g. >, <, >=, etc.) (can be null)
	 * @param schema
	 *            the schema to search for (can be null).
	 * @return a collection of procedures (Procedure objects)
	 */
	public LinkedList getProcedures(String oidOp, Schema schema) throws SQLException {
		LinkedList results = new LinkedList();
		Statement stmt = null;
		Connection conn = m_connection.getMetaDataConnection();
		try {

			StringBuffer sql = new StringBuffer();

			int maxoid = getMaxSystemOID();
			// prolang = 14 selects only SQL procedures
			if (m_connection.supportsSchemas()) {
				sql.append("SELECT pg_proc.oid,* FROM pg_catalog.pg_proc, pg_catalog.pg_namespace, pg_catalog.pg_language WHERE pronamespace = pg_catalog.pg_namespace.oid AND pg_proc.prolang = pg_language.oid");
				if (schema != null) {
					sql.append(" AND pg_namespace.nspname = '");
					sql.append(schema.getName());
					sql.append("'");
				}
			} else {
				sql.append("SELECT pg_proc.oid,* FROM pg_proc, pg_language WHERE pg_proc.prolang = pg_language.oid");
			}

			if (oidOp != null) {
				sql.append(" AND pg_proc.oid");
				sql.append(oidOp);
				sql.append(maxoid);
			}

			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql.toString());
			while (rset.next()) {
				int oid = rset.getInt("oid");
				String proname = rset.getString("proname");
				String prosrc = rset.getString("prosrc");
				String lang = rset.getString("lanname");

				StoredProcedure proc = new StoredProcedure();
				// we use the oid as the key for the procedure because postgres
				// allows procedures
				// to have non unique names
				proc.setKey(new Integer(oid));
				proc.setSource(prosrc);
				if (m_connection.supportsSchemas()) {
					String sname = rset.getString("nspname");
					Schema pschema = new Schema(sname);
					proc.setId(new DbObjectId(DbObjectType.FUNCTION, m_connection.getDefaultCatalog(), pschema, proname));
				} else {
					proc.setId(new DbObjectId(DbObjectType.FUNCTION, m_connection.getDefaultCatalog(),
							Schema.VIRTUAL_SCHEMA, proname));
				}
				proc.setLanguage(new ProcedureLanguage(lang));
				results.add(proc);
			}
		} catch (SQLException se) {
			conn.rollback();
			throw se;
		} finally {
			if (stmt != null)
				stmt.close();
		}

		conn.commit();
		return results;
	}

	/**
	 * Gets all procedures in the given schema (both user and system )
	 */
	public Collection getProcedures(Schema schema) throws SQLException {
		return getProcedures(null, schema);
	}

	/**
	 * @return a collection of procedures (Procedure objects)
	 */
	public Collection getSystemProcedures() throws SQLException {
		return getProcedures(" <= ", null);
	}

	/**
	 * @return a collection of user-defined procedures (Procedure objects)
	 */
	public Collection getUserProcedures() throws SQLException {
		return getProcedures(" > ", null);
	}

	/**
	 * Loads the latest procedure information for the given stored procedure.
	 * For postgres, this simply queries the proc table for the given procedure
	 * name. Then, get procedure columns is called
	 */
	public StoredProcedure load(StoredProcedure proc) throws SQLException {
		assert (proc != null);
		Statement stmt = null;
		Connection conn = m_connection.getMetaDataConnection();
		try {
			StringBuffer sqlbuff = new StringBuffer();

			// we store the oid in the procedure key
			// if the oid is null, then we try using the procedure name/schema
			if (m_connection.supportsSchemas()) {
				sqlbuff.append("SELECT *, pg_catalog.pg_language.lanname FROM pg_catalog.pg_namespace, pg_catalog.pg_proc, pg_catalog.pg_language WHERE pg_proc.pronamespace = pg_namespace.oid ");
				if (proc.getKey() == null) {
					Schema schema = proc.getSchema();
					if (schema == null || proc.getName() == null)
						return proc;

					sqlbuff.append(" AND pg_proc.proname = '");
					sqlbuff.append(proc.getName());
					sqlbuff.append("'");
					sqlbuff.append(" AND pg_namespace.nspname = '");
					sqlbuff.append(proc.getSchema().getName());
					sqlbuff.append("'");
				} else {
					sqlbuff.append(" AND pg_proc.oid = ");
					sqlbuff.append(proc.getKey());
				}
				sqlbuff.append(" AND pg_language.oid = pg_proc.prolang");
			} else {
				sqlbuff.append("SELECT *, pg_language.lanname FROM pg_proc, pg_language ");
				if (proc.getKey() == null) {
					if (proc.getName() == null)
						return proc;

					sqlbuff.append("WHERE pg_proc.proname = '");
					sqlbuff.append(proc.getName());
					sqlbuff.append("'");
				} else {
					sqlbuff.append("WHERE pg_proc.oid = ");
					sqlbuff.append(proc.getKey());
				}
				sqlbuff.append(" AND pg_language.oid = pg_proc.prolang");
			}

			String sql = sqlbuff.toString();

			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			if (rset.next()) {
				String proname = rset.getString("proname");
				String prosrc = rset.getString("prosrc");
				String lang = rset.getString("lanname");

				proc.setSource(prosrc);
				proc.setLanguage(new ProcedureLanguage(lang));
				Integer oid = (Integer) proc.getKey();
				if (m_connection.supportsSchemas()) {
					String schemaname = rset.getString("nspname");
					proc.setId(new DbObjectId(DbObjectType.FUNCTION, m_connection.getDefaultCatalog(), new Schema(
							schemaname), proname));
				} else {
					proc.setId(new DbObjectId(DbObjectType.FUNCTION, m_connection.getDefaultCatalog(),
							Schema.VIRTUAL_SCHEMA, proname));
				}
				proc.setDescription(PostgresUtils.getDescription(conn, m_connection.supportsSchemas(), oid.intValue()));
			}

		} catch (SQLException se) {
			conn.rollback();
			throw se;
		} finally {
			if (stmt != null)
				stmt.close();
		}

		conn.commit();

		loadProcedureMetaData(proc);
		return proc;
	}

	/**
	 * Reads the column information for the given stored procedure. Postgres
	 * does not support the JDBC interface for this so we need to read the
	 * metadata directly from the database
	 */
	private void loadProcedureMetaData(StoredProcedure proc) throws SQLException {
		Connection pgconn = m_connection.getMetaDataConnection();
		Statement stmt = null;
		try {
			proc.clearParameters();
			StringBuffer sql = new StringBuffer();

			sql.append("SELECT format_type(prorettype,NULL),oidvectortypes(proargtypes),* FROM pg_proc WHERE oid = ");
			sql.append(proc.getKey());
			stmt = pgconn.createStatement();
			ResultSet rset = stmt.executeQuery(sql.toString());
			if (rset.next()) {
				String vectypes = rset.getString("oidvectortypes");
				if (vectypes != null && vectypes.length() > 0) {
					// parse the procedure parameter types
					StringTokenizer st = new StringTokenizer(vectypes, ",");
					while (st.hasMoreTokens()) {
						String postgres_stype = st.nextToken().trim();
						int jdbc_type = 0;
						try {
							jdbc_type = getJDBCType(pgconn, postgres_stype);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						ProcedureParameter param = new ProcedureParameter(null, ParameterDirection.IN, jdbc_type,
								postgres_stype);
						proc.addParameter(param);
					}
				}
				// the format_type function in postgres sometimes puts quote
				// characters around the result
				String rettype = rset.getString("format_type");
				rettype = TSUtils.strip(rettype, '\"');
				if (rettype.length() == 0 || rettype.equals("-"))
					rettype = "opaque";
				int jdbc_type = getJDBCType(pgconn, rettype);
				ProcedureParameter returns = new ProcedureParameter(null, ParameterDirection.RETURN, jdbc_type, rettype);
				proc.setReturnParameter(returns);
			}
		} finally {
			if (stmt != null)
				stmt.close();
		}

	}

	/**
	 * Converts the Postgres type to the corresponding JDBC type.
	 */
	private static int getJDBCType(Connection pgconn, String postgresType) {
		try {
			Class[] paramtypes = new Class[1];
			paramtypes[0] = String.class;
			Method m = pgconn.getClass().getMethod("getSQLType", paramtypes);
			Object[] params = new Object[1];
			params[0] = postgresType;
			Integer result = (Integer) m.invoke(pgconn, params);
			if (result == null)
				return 0;
			else
				return result.intValue();
		} catch (Exception e) {
			TSUtils.printException(e);
			return 0;
		}
	}

	/**
	 * Looks up the stored procedure from the database or cache.
	 * 
	 * @param procedureKey
	 *            this is the unique key to locate the procedure. It was set in
	 *            the getProcedures method. For Postgres, this is simply the
	 *            oid(Integer) of the procedure in the pg_proc table.
	 * @return the procedure. This is always a valid object.
	 * @throws SQLException
	 *             if an error occurs or the procedure cannot be found
	 * 
	 */
	public StoredProcedure lookupProcedure(Object procedureKey) throws SQLException {
		StoredProcedure proc = new StoredProcedure();
		proc.setKey(procedureKey);
		return load(proc);
	}

	/**
	 * Modifies the procedure in the database
	 */
	public void modifyProcedure(StoredProcedure newProc, StoredProcedure oldProc) throws SQLException {
		String sql = getCreateSQL(newProc, true);
		SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Drop Function"), sql);
	}

	/**
	 * Sets the database connection for this service.
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
