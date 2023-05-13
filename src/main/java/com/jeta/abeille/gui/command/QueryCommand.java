package com.jeta.abeille.gui.command;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.foundation.i18n.I18N;
import com.jeta.abeille.logger.DbLogger;

/**
 * This command runs a sql prepared statement in the background. This is for
 * invoking a query. The command is run in a background thread so it can be
 * canceled
 * 
 * @author Jeff Tassin
 */
public class QueryCommand extends AbstractCommand {
	private Statement m_stmt;
	private ResultSetReference m_rsetref;

	/** the connection reference */
	private ConnectionReference m_connectionref;

	private String m_sql;

	/**
	 * ctor
	 */
	public QueryCommand(ConnectionReference connref, PreparedStatement pstmt) {
		m_connectionref = connref;
		m_stmt = pstmt;
		setTimeoutMessage(I18N.getLocalizedMessage("Running Query"));
	}

	/**
	 * ctor
	 */
	public QueryCommand(ConnectionReference connref, Statement stmt, String sql) {
		m_connectionref = connref;
		m_stmt = stmt;
		m_sql = sql;
		setTimeoutMessage(I18N.getLocalizedMessage("Running Query"));
	}

	/**
	 * Cancels the operation
	 */
	public void cancel() throws SQLException {
		// @todo cancel.statement
		try {
			setCanceled(true);
			// m_stmt.cancel();
			m_connectionref.closeConnection();

		} catch (Exception e) {
			// just trap here
		}

	}

	/**
	 * @return the resulting query results
	 */
	public ResultSetReference getResultSetReference() {
		return m_rsetref;
	}

	/**
	 * Command implementation. Invoked by the CommandRunner
	 */
	public void invoke() throws SQLException {
		try {
			if (m_stmt instanceof PreparedStatement) {
				PreparedStatement pstmt = (PreparedStatement) m_stmt;
				m_rsetref = new ResultSetReference(m_connectionref, pstmt, pstmt.executeQuery(), null);
			} else {
				DbLogger.fine(m_sql);
				assert (m_sql != null);
				m_rsetref = new ResultSetReference(m_connectionref, m_stmt, m_stmt.executeQuery(m_sql), m_sql);
			}

		} catch (SQLException e) {
			// just trap here
			if (!isCanceled()) {
				m_connectionref.rollback();
				throw e;
			}
		}
	}

}
