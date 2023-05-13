package com.jeta.plugins.abeille.postgres;

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
public class PostgresSequenceServiceImplementation implements SequenceService {
	/** the database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public PostgresSequenceServiceImplementation() {

	}

	/**
	 * ctor
	 */
	public PostgresSequenceServiceImplementation(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * Creates the given sequence in the database
	 */
	public void createSequence(Sequence seq) throws SQLException {
		SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Create Sequence"), getCreateSQL(seq));
	}

	/**
	 * Drops the given sequence in the database
	 */
	public void dropSequence(Sequence seq, boolean cascade) throws SQLException {
		SQLCommand
				.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Drop Sequence"), getDropSQL(seq, cascade));
	}

	/**
	 * @return the SQL to create the given sequence in the database
	 */
	public String getCreateSQL(Sequence seq) {
		/*
		 * CREATE [ TEMPORARY | TEMP ] SEQUENCE seqname [ INCREMENT increment ]
		 * [ MINVALUE minvalue ] [ MAXVALUE maxvalue ] [ START start ] [ CACHE
		 * cache ] [ CYCLE ]
		 */

		StringBuffer sql = new StringBuffer();
		sql.append("CREATE ");

		if (seq.isTemporary() == Boolean.TRUE)
			sql.append("TEMPORARY ");

		sql.append("SEQUENCE ");
		sql.append(seq.getFullyQualifiedName());
		sql.append(" ");

		Long incr = seq.getIncrement();
		if (incr != null) {
			sql.append("INCREMENT ");
			sql.append(incr.toString());
			sql.append(" ");
		}

		Long minval = seq.getMin();
		if (minval != null) {
			sql.append("MINVALUE ");
			sql.append(minval.toString());
			sql.append(" ");
		}

		Long maxval = seq.getMax();
		if (maxval != null) {
			sql.append("MAXVALUE ");
			sql.append(maxval.toString());
			sql.append(" ");
		}

		Long start = seq.getStart();
		if (start != null) {
			sql.append("START ");
			sql.append(start.toString());
			sql.append(" ");
		}

		Long cache = seq.getCache();
		if (cache != null) {
			sql.append("CACHE ");
			sql.append(cache.toString());
			sql.append(" ");
		}

		if (seq.isCycle() == Boolean.TRUE) {
			sql.append("CYCLE");
		}

		return sql.toString();
	}

	/**
	 * @return the SQL to drop the given sequence in the database
	 */
	public String getDropSQL(Sequence seq, boolean cascade) {
		StringBuffer sql = new StringBuffer();
		sql.append("DROP SEQUENCE ");
		sql.append(seq.getFullyQualifiedName());

		if (cascade)
			sql.append(" CASCADE");

		return sql.toString();
	}

	/**
	 * (Re)loads all parameters for a given sequence
	 */
	public Sequence getSequence(Sequence seq) throws SQLException {
		if (seq == null)
			return null;

		Connection conn = m_connection.getMetaDataConnection();
		LinkedList results = new LinkedList();
		Statement stmt = null;
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * from ");
			sql.append(seq.getFullyQualifiedName());
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql.toString());
			if (rset.next()) {
				String seqname = rset.getString("sequence_name");
				Long last_value = new Long(rset.getLong("last_value"));
				Long increment = new Long(rset.getLong("increment_by"));
				Long max = new Long(rset.getLong("max_value"));
				Long min = new Long(rset.getLong("min_value"));
				Long cache = new Long(rset.getLong("cache_value"));
				Boolean cycle = Boolean.valueOf(rset.getBoolean("is_cycled"));

				seq.setLastValue(last_value);
				seq.setIncrement(increment);
				seq.setMax(max);
				seq.setMin(min);
				seq.setCache(cache);
				seq.setCycle(cycle);
			}
		} finally {
			if (stmt != null)
				stmt.close();

			conn.commit();
		}
		return seq;
	}

	/**
	 * Finds all sequences in the given schema
	 * 
	 * @return a collection of sequences (Sequence objects)
	 */
	public Collection getSequences(Schema schema) throws SQLException {
		Connection conn = m_connection.getMetaDataConnection();
		TreeSet results = new TreeSet();
		Statement stmt = null;
		try {
			String sql = PostgresUtils.pgClassSQL(m_connection, schema, DbObjectType.SEQUENCE, null);
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			while (rset.next()) {
				int oid = rset.getInt("oid");
				String seqname = rset.getString("relname");

				Sequence seq = new Sequence(new Integer(oid));
				if (m_connection.supportsSchemas()) {
					seq.setId(new DbObjectId(DbObjectType.SEQUENCE, m_connection.getDefaultCatalog(), schema, seqname));
				} else {
					seq.setId(new DbObjectId(DbObjectType.SEQUENCE, m_connection.getDefaultCatalog(),
							Schema.VIRTUAL_SCHEMA, seqname));
				}

				results.add(seq);
			}
		} finally {
			if (stmt != null)
				stmt.close();

			conn.commit();
		}
		return results;
	}

	/**
	 * Modifies an existing sequence in the database
	 */
	public void modifySequence(Sequence newSeq, Sequence oldSeq) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT setval('");
		sql.append(newSeq.getFullyQualifiedName());
		sql.append("', ");
		sql.append(newSeq.getStart());
		sql.append(")");
		SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Modify Sequence"), sql.toString());
	}

	/**
	 * Sets the database connection
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

}
