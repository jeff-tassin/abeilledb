package com.jeta.plugins.abeille.hsqldb;

import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.procedures.ParameterDirection;
import com.jeta.abeille.database.procedures.ProcedureParameter;
import com.jeta.abeille.database.triggers.Trigger;
import com.jeta.abeille.database.triggers.TriggerService;

/**
 * This class defines a service for MySQL listing all triggers for a given table
 * as well as locating an individual trigger from a trigger key
 */
public class HSQLTriggerServiceImplementation implements TriggerService {
	/** the underlying database connection */
	private TSConnection m_connection;

	/**
	 * Creates the given trigger in the database
	 */
	public void createTrigger(Trigger newtrigger) throws SQLException {

	}

	/**
	 * Drops the given trigger from its table
	 */
	public void dropTrigger(Trigger dropTrigger, boolean cascade) throws SQLException {

	}

	/**
	 * @return all triggers (Trigger objects) for the given table id
	 */
	public Collection getTriggers(TableId tableId) throws SQLException {
		LinkedList results = new LinkedList();
		return results;
	}

	/**
	 * Modifies the given trigger in the database
	 */
	public void modifyTrigger(Trigger newTrigger, Trigger oldTrigger) throws SQLException {

	}

	/**
	 * ctor
	 */
	public void setConnection(TSConnection connection) {
		m_connection = connection;
	}

}
