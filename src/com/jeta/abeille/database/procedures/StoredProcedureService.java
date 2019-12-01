package com.jeta.abeille.database.procedures;

import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

/**
 * This service allows the user to enumerate all stored procedures in the
 * database as well as lookup individual stored procedures. We provide a wrapper
 * around the StoredProcedureInterface so we can implement caching
 * 
 * @author Jeff Tassin
 */
public class StoredProcedureService {
	public static final String COMPONENT_ID = "database.StoredProcedureService";

	/**
	 * This is the stored procedure implementation that we forward all of our
	 * calls to. This is database specific.
	 */
	private StoredProcedureInterface m_delegate;

	/**
	 * This is a collection of system procedures (StoredProcedure objects)
	 */
	private Collection m_systemprocedures;

	/**
	 * This is a collection of user procedures (StoredProcedure objects)
	 */
	private Collection m_userprocedures;

	/** a hash map of Schema(keys) to StoredProcedure objects (values) */
	private HashMap m_procedures;

	/**
	 * A set of procedures stored by name.
	 * HashMap<Schema,HashMap<String,LinkedList<StoredProcedure> > >
	 */
	private HashMap m_byname;

	/**
	 * ctor
	 */
	public StoredProcedureService(StoredProcedureInterface delegate) {
		m_delegate = delegate;
	}

	/**
	 * Modifies the procedure in the database
	 */
	public void modifyProcedure(StoredProcedure newProc, StoredProcedure oldProc) throws SQLException {
		m_delegate.modifyProcedure(newProc, oldProc);
		reload();
	}

	/**
	 * Drops the procedure from the database
	 */
	public void dropProcedure(StoredProcedure proc, boolean cascade) throws SQLException {
		m_delegate.dropProcedure(proc, cascade);
		removeFromCache(proc);
	}

	/**
	 * @return all procedures (StoredProcedure) objects that have the given name
	 *         in the given schema. This makes sense only for those database
	 *         systems that support method overloading.
	 */
	public synchronized Collection getProcedures(Schema schema, String procName) throws SQLException {
		LinkedList result = new LinkedList();
		Collection procs = getProcedures(schema);
		Iterator iter = procs.iterator();
		while (iter.hasNext()) {
			StoredProcedure proc = (StoredProcedure) iter.next();
			if (procName.equals(proc.getName())) {
				result.add(proc);
			}
		}
		return result;
	}

	/**
	 * Gets all procedures(StoredProcedure objects) in the given schema (both
	 * user and system )
	 */
	public synchronized Collection getProcedures(Schema schema) throws SQLException {
		if (m_procedures == null) {
			m_procedures = new HashMap();
			Collection procs = m_delegate.getProcedures(schema);
			m_procedures.put(schema, procs);
			return procs;
		} else {
			Collection procs = (Collection) m_procedures.get(schema);
			if (procs == null) {
				procs = m_delegate.getProcedures(schema);
				m_procedures.put(schema, procs);
			}
			return procs;
		}
	}

	/**
	 * @return a collection of procedures (StoredProcedure objects)
	 */
	public synchronized Collection getSystemProcedures() throws SQLException {
		if (m_systemprocedures == null) {
			m_systemprocedures = m_delegate.getSystemProcedures();
		}
		return m_systemprocedures;
	}

	/**
	 * @return a collection of procedures (StoredProcedure objects)
	 */
	public synchronized Collection getUserProcedures() throws SQLException {
		if (m_userprocedures == null) {
			m_userprocedures = m_delegate.getUserProcedures();
		}
		return m_userprocedures;
	}

	/**
	 * Loads the latest procedure information for the given stored procedure.
	 * For postgres, this simply queries the proc table for the given procedure
	 * name. Then, get procedure columns is called
	 */
	public synchronized StoredProcedure load(StoredProcedure proc) throws SQLException {
		return m_delegate.load(proc);
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
	public synchronized StoredProcedure lookupProcedure(Object procedureKey) throws SQLException {
		return m_delegate.lookupProcedure(procedureKey);
	}

	/**
	 * Reloads the stored procedures into the cache.
	 */
	public synchronized void reload() {
		m_systemprocedures = null;
		m_userprocedures = null;
		m_procedures = null;
		m_byname = null;
	}

	/**
	 * Removes the given procedure from the cache
	 */
	private synchronized void removeFromCache(StoredProcedure proc) {
		if (m_userprocedures != null)
			m_userprocedures.remove(proc);

		if (m_systemprocedures != null)
			m_systemprocedures.remove(proc);

		if (m_procedures != null && proc.getSchema() != null) {
			Collection procs = (Collection) m_procedures.get(proc.getSchema());
			if (procs != null)
				procs.remove(proc);
		}
	}

}
