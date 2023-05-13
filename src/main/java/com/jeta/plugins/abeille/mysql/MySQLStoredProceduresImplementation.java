package com.jeta.plugins.abeille.mysql;

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
public class MySQLStoredProceduresImplementation implements StoredProcedureInterface {
	/** the database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public MySQLStoredProceduresImplementation() {

	}

	/**
	 * ctor
	 */
	public MySQLStoredProceduresImplementation(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * drops the given procedure from the database
	 */
	public void dropProcedure(StoredProcedure proc, boolean cascade) throws SQLException {

	}

	/**
	 * @return the SQL to create the given stored procedure in the database
	 */
	private String getCreateSQL(StoredProcedure proc, boolean breplace) throws SQLException {
		return "";
	}

	/**
	 * @return the SQL that drops the given stored procedure
	 */
	private String getDropSQL(StoredProcedure proc, boolean cascade) {
		return "";
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
		return proc;
	}

	/**
	 * Looks up the stored procedure from the database or cache.
	 * 
	 * @param procedureKey
	 *            this is the unique key to locate the procedure. It was set in
	 *            the getProcedures method. For MySQL, this is simply the
	 *            oid(Integer) of the procedure in the pg_proc table.
	 * @return the procedure. This is always a valid object.
	 * @throws SQLException
	 *             if an error occurs or the procedure cannot be found
	 * 
	 */
	public StoredProcedure lookupProcedure(Object procedureKey) throws SQLException {
		return null;
	}

	/**
	 * Modifies the procedure in the database
	 */
	public void modifyProcedure(StoredProcedure newProc, StoredProcedure oldProc) throws SQLException {
	}

	/**
	 * Sets the database connection for this service.
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
