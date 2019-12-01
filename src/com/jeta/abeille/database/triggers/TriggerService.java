package com.jeta.abeille.database.triggers;

import java.sql.SQLException;

import java.util.Collection;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

/**
 * This class defines a service for listing all triggers for a given table as
 * well as locating an individual trigger from a trigger key
 */
public interface TriggerService {
	public static final String COMPONENT_ID = "database.TriggerService";

	/**
	 * Creates the given trigger in the database
	 */
	public void createTrigger(Trigger newtrigger) throws SQLException;

	/**
	 * Drops the given trigger from its table
	 */
	public void dropTrigger(Trigger dropTrigger, boolean cascade) throws SQLException;

	/**
	 * @return all triggers (Trigger objects) for the given table id
	 */
	public Collection getTriggers(TableId tableId) throws SQLException;

	/**
	 * Modifies the given trigger in the database
	 */
	public void modifyTrigger(Trigger newTrigger, Trigger oldTrigger) throws SQLException;

	/**
	 * Sets the connection for the service
	 */
	public void setConnection(TSConnection connection);

}
