package com.jeta.abeille.gui.command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.logger.DbLogger;

import com.jeta.foundation.i18n.I18N;

/**
 * A command that queries a table for all rows. We typically run as a command in
 * case there are locks held on the table by other connections. This allows the
 * user to cancel the call if the query is taking too long.
 * 
 * @author Jeff Tassin
 */
public class QueryTableCommand extends AbstractCommand {
	/** the database connection */
	private TSConnection m_tsconnection;

	/** the sql statement */
	private Statement m_statement;

	/** the id of the table */
	private TableId m_tableid;

	private ResultSetReference m_resultref;

	private ConnectionReference m_connectionref;

	/** the max number of rows to query */
	private int m_max_rows;

	/**
	 * ctor
	 */
	public QueryTableCommand(TSConnection c, TableId tableId) {
		this(c, tableId, 0);
	}

	/**
	 * ctor
	 */
	public QueryTableCommand(TSConnection c, TableId tableId, int max_rows) {
		m_tsconnection = c;
		m_tableid = tableId;
		m_max_rows = max_rows;
		setTimeoutMessage(I18N.getLocalizedMessage("Attempting_query_table"));
	}

	/**
	 * Cancels the operation. This closes the connection for this command.
	 */
	public void cancel() throws SQLException {
		if (m_statement != null)
			m_statement.cancel();

		if (m_connectionref != null)
			m_connectionref.closeConnection();
	}

	/**
	 * Runs the rename command
	 */
	public void invoke() throws SQLException {
		try {

			Connection connection = m_tsconnection.getWriteConnection();
			m_connectionref = new ConnectionReference(m_tsconnection, connection);

			int rtype = m_tsconnection.getResultSetScrollType();
			int concurrency = m_tsconnection.getResultSetConcurrency();
			m_statement = connection.createStatement(rtype, concurrency);
			m_statement.setMaxRows(m_max_rows);

			TSDatabase dbase = (TSDatabase) m_tsconnection.getImplementation(TSDatabase.COMPONENT_ID);

			String sql = "select * from " + dbase.getFullyQualifiedName(m_tableid);
			try {
				if (m_tsconnection.getDatabase() == Database.POSTGRESQL) {
					com.jeta.abeille.database.postgres.PostgresObjectStore postgresos = com.jeta.abeille.database.postgres.PostgresObjectStore
							.getInstance(m_tsconnection);
					if (postgresos != null) {
						if (postgresos.isShowOID(m_tableid)) {
							sql = "select oid,* from " + dbase.getFullyQualifiedName(m_tableid);
						}
					}
				}
			} catch (Exception e) {
				// just eat it here
			}

			DbLogger.fine(sql);
			ResultSet rset = m_statement.executeQuery(sql);
			m_resultref = new ResultSetReference(m_connectionref, m_statement, rset, sql);

			// release the reference
			m_connectionref = null;
		} catch (SQLException e) {
			m_connectionref.rollback();
			throw e;
		}
	}

	/**
	 * @return a reference to the result set
	 */
	public ResultSetReference getResultSetReference() {
		return m_resultref;
	}
}
