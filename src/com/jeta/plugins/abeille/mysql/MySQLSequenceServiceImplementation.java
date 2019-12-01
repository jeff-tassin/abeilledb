package com.jeta.plugins.abeille.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.sequences.Sequence;
import com.jeta.abeille.database.sequences.SequenceService;

import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This service allows the user to enumerate all sequences
 * 
 * @author Jeff Tassin
 */
public class MySQLSequenceServiceImplementation implements SequenceService {
	/** the database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public MySQLSequenceServiceImplementation() {

	}

	/**
	 * ctor
	 */
	public MySQLSequenceServiceImplementation(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * Creates the given sequence in the database
	 */
	public void createSequence(Sequence seq) throws SQLException {

	}

	/**
	 * Drops the given sequence in the database
	 */
	public void dropSequence(Sequence seq, boolean cascade) throws SQLException {

	}

	/**
	 * @return the SQL to create the given sequence in the database
	 */
	public String getCreateSQL(Sequence seq) {
		return "";
	}

	/**
	 * @return the SQL to drop the given sequence in the database
	 */
	public String getDropSQL(Sequence seq, boolean cascade) {
		return "";
	}

	/**
	 * (Re)loads all parameters for a given sequence
	 */
	public Sequence getSequence(Sequence seq) throws SQLException {
		return seq;
	}

	/**
	 * Finds all sequences in the given schema
	 * 
	 * @return a collection of sequences (Sequence objects)
	 */
	public Collection getSequences(Schema schema) throws SQLException {
		TreeSet results = new TreeSet();
		return results;
	}

	/**
	 * Modifies an existing sequence in the database
	 */
	public void modifySequence(Sequence newSeq, Sequence oldSeq) throws SQLException {
	}

	/**
	 * Sets the database connection
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
