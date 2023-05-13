package com.jeta.abeille.gui.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.logger.DbLogger;

/**
 * This command represents a simple SQL command that might take an arbitrarily
 * long time to complete. This command does not support a cancel. It is simply
 * used to show a message box on the screen indicating the command is in
 * progress
 * 
 * @author Jeff Tassin
 */
public class SQLCommand extends AbstractCommand {
	/** the database connection */
	private TSConnection m_tsconnection;

	/** the sql connection */
	private Connection m_connection;

	/** the sql statement */
	private Statement m_statement;

	private LinkedList m_sqlcommands = new LinkedList();

	/**
	 * set to true if this command should be run against the metadata connection
	 */
	private boolean m_metadata = false;

	/** the sql filter */
	private static SQLFilter m_filter = null;

	/**
	 * ctor
	 */
	public SQLCommand(TSConnection conn, String timeoutMsg, String sql) {
		super(timeoutMsg, sql);
		m_tsconnection = conn;
		m_sqlcommands.add(sql);
	}

	/**
	 * ctor
	 * 
	 */
	public SQLCommand(TSConnection conn, String timeoutMsg, Collection sqllist) {
		super(timeoutMsg, "");
		m_tsconnection = conn;
		m_sqlcommands.addAll(sqllist);
	}

	/**
	 * Adds a filter to this command
	 */
	public static void addFilter(SQLFilter filter) {
		assert (m_filter == null);
		m_filter = filter;
	}

	/** cancels this command */
	public void cancel() throws SQLException {
		// no op
	}

	/**
	 * Adds a filter to this command
	 */
	public static SQLFilter getFilter() {
		return m_filter;
	}

	/**
	 * Runs the rename command
	 */
	public void invoke() throws SQLException {
		SQLFilter filter = getFilter();
		if (filter == null) {
			assert (m_tsconnection != null);
			TSDatabase db = (TSDatabase) m_tsconnection.getImplementation(TSDatabase.COMPONENT_ID);
			try {
				if (m_metadata) {
					m_connection = m_tsconnection.getMetaDataConnection();
				} else
					m_connection = m_tsconnection.getWriteConnection();

				m_statement = m_connection.createStatement();
				Iterator iter = m_sqlcommands.iterator();
				while (iter.hasNext()) {
					String sql = (String) iter.next();
					setSQL(sql);
					DbLogger.fine(sql);
					m_statement.execute(sql);
				}
				m_statement.close();
				m_tsconnection.jetaCommit(m_connection);

				m_tsconnection.fireStatusUpdate(m_tsconnection.getDefaultCatalog());
			} catch (SQLException e) {
				DbLogger.log(e);
				if (db.supportsTransactions()) {
					m_tsconnection.jetaRollback(m_connection);
				}
				throw e;
			} finally {
				m_tsconnection.release(m_connection);
			}
		} else {
			// if we have a sql filter, then don't invoke in the database.
			// rather, we forward the call to the SQLFilter instead.
			// This is used when the user wants to preview a SQL command.
			Iterator iter = m_sqlcommands.iterator();
			while (iter.hasNext()) {
				String sql = (String) iter.next();
				filter.sqlCommand(sql);
			}
		}
	}

	/**
	 * Removes the filter for the sql commands
	 */
	public static void removeFilter(SQLFilter filter) {
		assert (filter == m_filter);
		m_filter = null;
	}

	/**
	 * Runs a command modally against the metadata connection. If the command
	 * takes a long time, a modal dialog is displayed with a message. The user
	 * cannot close the dialog until the command has completed unless the
	 * database supports cancelling a statement.
	 */
	public static int runMetaDataCommand(TSConnection connection, String msg, String sql) throws SQLException {
		try {
			SQLCommand cmd = new SQLCommand(connection, msg, sql);
			cmd.setMetaData(true);
			ModalCommandRunner crunner = new ModalCommandRunner(connection, cmd);
			return crunner.invoke();
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	/**
	 * Runs a command modally. If the command takes a long time, a modal dialog
	 * is displayed with a message. The user cannot close the dialog until the
	 * command has completed unless the database supports cancelling a
	 * statement.
	 */
	public static int runModalCommand(TSConnection connection, String msg, String sql) throws SQLException {
		try {
			SQLCommand cmd = new SQLCommand(connection, msg, sql);
			ModalCommandRunner crunner = new ModalCommandRunner(connection, cmd);
			return crunner.invoke();
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	/**
	 * Runs a command modally. If the command takes a long time, a modal dialog
	 * is displayed with a message. The user cannot close the dialog until the
	 * command has completed unless the database supports cancelling a
	 * statement.
	 * 
	 * @param list
	 *            a list of sql commands (String objects) to run
	 */
	public static int runModalCommand(TSConnection connection, String msg, Collection list) throws SQLException {
		try {
			SQLCommand cmd = new SQLCommand(connection, msg, list);
			ModalCommandRunner crunner = new ModalCommandRunner(connection, cmd);
			return crunner.invoke();
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	/**
	 * Runs a command modally. If the command takes a long time, a modal dialog
	 * is displayed with a message. The user cannot close the dialog until the
	 * command has completed unless the database supports cancelling a
	 * statement.
	 * 
	 * @param list
	 *            a list of sql commands (String objects) to run
	 */
	public static int runMetaDataCommand(TSConnection connection, String msg, Collection list) throws SQLException {
		try {
			SQLCommand cmd = new SQLCommand(connection, msg, list);
			cmd.setMetaData(true);
			ModalCommandRunner crunner = new ModalCommandRunner(connection, cmd);
			return crunner.invoke();
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	/**
	 * Runs two SQL commands modally. If the command takes a long time, a modal
	 * dialog is displayed with a message. The user cannot close the dialog
	 * until the command has completed unless the database supports cancelling a
	 * statement. This command is similar to the runMetaDataCommand(..., list).
	 * It is here because often our list of sql commands has only two elements.
	 * 
	 * @param sql1
	 *            the first sql command to run
	 * @param sql2
	 *            the second sql command to run
	 */
	public static int runMetaDataCommand(TSConnection connection, String msg, String sql1, String sql2)
			throws SQLException {
		LinkedList list = new LinkedList();
		list.add(sql1);
		list.add(sql2);
		return runMetaDataCommand(connection, msg, list);
	}

	/**
	 * Sets the flag that indicates if this command should be run using the
	 * metadata connection
	 */
	private void setMetaData(boolean md) {
		m_metadata = md;
	}

}
