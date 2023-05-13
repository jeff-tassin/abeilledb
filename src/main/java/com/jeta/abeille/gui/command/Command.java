package com.jeta.abeille.gui.command;

import java.sql.SQLException;

/**
 * This represents a database command. It is based on the Command pattern and
 * abstract a database command that might take a long time.
 * 
 * @author Jeff Tassin
 */
public interface Command {
	/** cancels this command */
	public void cancel() throws SQLException;

	/** invokes this command */
	public void invoke() throws SQLException;

	/**
	 * @return the SQL that is the basis for this command
	 */
	public String getSQL();

	/**
	 * @return a message that informs the user that this command has timed out.
	 */
	public String getTimeoutMessage();

}
