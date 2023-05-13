package com.jeta.abeille.gui.sql;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.command.AbstractCommand;

import com.jeta.foundation.i18n.I18N;

/**
 * A command that parses and runs a sequence of SQL statements in the main gui
 * thread. This class only executes SQL statements and does not return results
 * 
 * @author Jeff Tassin
 */
public class CompoundSQLCommand extends AbstractCommand {
	/** the database connection */
	private TSConnection m_tsconnection;

	/** the sql connection */
	private Connection m_connection;

	/** the sql statement */
	private Statement m_statement;

	/** the sql to run */
	private String m_sql;

	/**
	 * ctor
	 * 
	 * @param sql
	 *            the sequence of SQL statements to run separated by a semicolon
	 */
	public CompoundSQLCommand(TSConnection c, String sql) {
		m_tsconnection = c;
		m_sql = sql;
	}

	/**
	 * Runs the rename command
	 */
	public void invoke() throws SQLException {
		try {
			m_connection = m_tsconnection.getWriteConnection();
			m_statement = m_connection.createStatement();

			// System.out.println( "CompoundSQL starting: " + m_sql );

			SQLDocumentParser parser = new SQLDocumentParser(m_tsconnection, m_sql, SQLPreferences.getDelimiter());
			while (parser.hasMoreTokens()) {
				String sqlcmd = parser.getSQL();
				if (sqlcmd == null)
					sqlcmd = "";
				else
					sqlcmd = sqlcmd.trim();

				if (sqlcmd.length() > 0) {
					// System.out.println( "CompoundSQLCommand:executesql: " +
					// sqlcmd );
					m_statement.execute(sqlcmd);
					// System.out.println( "Statement Executed" );
				}
			}

			// System.out.println( "CompoundSQL command finished" );
			m_statement.close();
			// m_connection.commit()
			m_tsconnection.jetaCommit(m_connection);
		} catch (javax.swing.text.BadLocationException ble) {
			m_connection.rollback();
			throw new SQLException(ble.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			m_connection.rollback();
			throw e;
		} finally {
			m_tsconnection.release(m_connection);
		}
	}

	/**
	 * Cancels the operation
	 */
	public void cancel() throws SQLException {
		m_tsconnection.release(m_connection);
	}

}
