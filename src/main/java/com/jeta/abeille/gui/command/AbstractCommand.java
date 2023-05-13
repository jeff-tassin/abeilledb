package com.jeta.abeille.gui.command;

import java.sql.SQLException;

/**
 * This represents a database command. It is based on the Command pattern and
 * abstract a database command that might take a long time.
 * 
 * @author Jeff Tassin
 */
public abstract class AbstractCommand implements Command {
	/** if there is sql associated with this command, this is it (can be null) */
	private String m_sql;

	/** message that informs the user this command has timed out. */
	private String m_timeoutMsg;

	private boolean m_canceled = false;

	/** if an error occurs we store it here */
	private Exception m_exception;

	public AbstractCommand() {

	}

	/**
	 * ctor
	 */
	public AbstractCommand(String timeoutMsg, String sql) {
		m_timeoutMsg = timeoutMsg;
		m_sql = sql;
	}

	/** cancels this command */
	public abstract void cancel() throws SQLException;

	/**
	 * @return the exception object if an error occurred
	 */
	public Exception getException() {
		return m_exception;
	}

	/**
	 * @return true if an error occurred
	 */
	public boolean isError() {
		return (getException() != null);
	}

	/**
	 * @return true if the command succesfully executed and if the command was
	 *         not canceled
	 */
	public boolean isSuccess() {
		return (getException() == null && !isCanceled());
	}

	/** invokes this command */
	public abstract void invoke() throws SQLException;

	/**
	 * @return the canceled flag
	 */
	public boolean isCanceled() {
		return m_canceled;
	}

	/**
	 * @return the SQL that is the basis for this command
	 */
	public String getSQL() {
		return m_sql;
	}

	/**
	 * @return a message that informs the user that this command has timed out.
	 */
	public String getTimeoutMessage() {
		return m_timeoutMsg;
	}

	/**
	 * Sets the canceled flag
	 */
	protected void setCanceled(boolean bcancel) {
		m_canceled = bcancel;
	}

	/**
	 * Sets the error flag if an exception occurs
	 */
	protected void setException(Exception e) {
		m_exception = e;
	}

	/**
	 * Sets the SQL that is the basis for this command
	 */
	public void setSQL(String sql) {
		m_sql = sql;
	}

	/**
	 * Sets the timeout message that informs the user this command has timed out
	 */
	public void setTimeoutMessage(String msg) {
		m_timeoutMsg = msg;
	}
}
