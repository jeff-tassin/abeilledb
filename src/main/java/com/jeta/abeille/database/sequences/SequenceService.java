package com.jeta.abeille.database.sequences;

import java.sql.SQLException;

import java.util.Collection;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

/**
 * This class defines a service for listing all sequences in a database
 */
public interface SequenceService {
	public static final String COMPONENT_ID = "database.SequenceService";

	/**
	 * Creates the given sequence in the database
	 */
	public void createSequence(Sequence seq) throws SQLException;

	/**
	 * Drops the given sequence in the database
	 */
	public void dropSequence(Sequence seq, boolean cascade) throws SQLException;

	/**
	 * (Re)loads all parameters for a given sequence
	 */
	public Sequence getSequence(Sequence seq) throws SQLException;

	/**
	 * @return all sequences (Sequence objects) in a given Schema
	 */
	public Collection getSequences(Schema schema) throws SQLException;

	/**
	 * Modifies an existing sequence in the database
	 */
	public void modifySequence(Sequence newSeq, Sequence oldSeq) throws SQLException;

	/**
	 * Sets the connection for the service
	 */
	public void setConnection(TSConnection connection);

}
