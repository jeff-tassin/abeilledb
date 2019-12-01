package com.jeta.abeille.database.procedures;

import java.sql.SQLException;

import java.util.Collection;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

/**
 * This service allows the user to enumerate all stored procedures in the
 * database as well as lookup individual stored procedures.
 * 
 * @todo we should probabaly cache procedures here
 * @author Jeff Tassin
 */
public interface StoredProcedureInterface {

	/** drops the given procedure from the database */
	public void dropProcedure(StoredProcedure proc, boolean cascade) throws SQLException;

	/**
	 * Gets all procedures in the given schema (both user and system )
	 */
	public Collection getProcedures(Schema schema) throws SQLException;

	/**
	 * @return a collection of procedures (StoredProcedure objects)
	 */
	public Collection getSystemProcedures() throws SQLException;

	/**
	 * @return a collection of procedures (StoredProcedure objects)
	 */
	public Collection getUserProcedures() throws SQLException;

	/**
	 * Loads the latest procedure information for the given stored procedure.
	 * For postgres, this simply queries the proc table for the given procedure
	 * name. Then, get procedure columns is called
	 */
	public StoredProcedure load(StoredProcedure proc) throws SQLException;

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
	public StoredProcedure lookupProcedure(Object procedureKey) throws SQLException;

	/**
	 * Modifies the procedure in the database
	 */
	public void modifyProcedure(StoredProcedure newProc, StoredProcedure oldProc) throws SQLException;

	/**
	 * Sets the database connection for this service
	 */
	public void setConnection(TSConnection connection);

}
