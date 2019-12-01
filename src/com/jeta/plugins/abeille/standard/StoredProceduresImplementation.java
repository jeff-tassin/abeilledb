package com.jeta.plugins.abeille.standard;

import java.sql.SQLException;

import java.util.Collection;
import java.util.LinkedList;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.procedures.ParameterDirection;
import com.jeta.abeille.database.procedures.ProcedureLanguage;
import com.jeta.abeille.database.procedures.ProcedureParameter;
import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.database.procedures.StoredProcedureInterface;

/**
 * This service allows the user to enumerate all stored procedures in the
 * database as well as lookup individual stored procedures.
 * 
 * @author Jeff Tassin
 */
public class StoredProceduresImplementation implements StoredProcedureInterface {
	/** the database connection */
	private TSConnection m_connection;

	/**
	 * drops the given procedure from the database
	 */
	public void dropProcedure(StoredProcedure proc, boolean cascade) throws SQLException {

	}

	/**
	 * Gets all procedures in the given schema (both user and system )
	 */
	public Collection getProcedures(Schema schema) throws SQLException {
		LinkedList results = new LinkedList();
		return results;
	}

	/**
	 * @return a collection of procedures (Procedure objects)
	 */
	public Collection getSystemProcedures() throws SQLException {
		LinkedList results = new LinkedList();
		return results;
	}

	/**
	 * @return a collection of procedures (Procedure objects)
	 */
	public Collection getUserProcedures() throws SQLException {
		LinkedList results = new LinkedList();
		return results;
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
	 *            the getProcedures method. For Postgres, this is simply the
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

	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
