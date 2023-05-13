package com.jeta.abeille.gui.command;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This represents a set of database commands that are to be executed as one
 * command. It is based on the Command pattern and abstract a database command
 * that might take a long time.
 * 
 * @author Jeff Tassin
 */
public abstract class CompositeCommand extends AbstractCommand {
	/** the list of commands */
	private ArrayList m_commands;

	/** the currently running command */
	private int m_activeindex = -1;

	/**
	 * ctor
	 */
	public CompositeCommand() {

	}

	/**
	 * Adds a command to this composite
	 */
	public void addCommand(AbstractCommand cmd) {
		if (m_commands == null)
			m_commands = new ArrayList();

		m_commands.add(cmd);
	}

	/** cancels the active command */
	public synchronized void cancel() throws SQLException {
		if (m_commands != null) {
			if (m_activeindex >= 0 && m_activeindex < m_commands.size()) {
				setCanceled(true);
				AbstractCommand cmd = (AbstractCommand) m_commands.get(m_activeindex);
				cmd.cancel();
			}
		}
	}

	/**
	 * @return the next command to be invoked. Null is returned if there is no
	 *         command to execute
	 */
	private synchronized AbstractCommand getNextCommand() {
		AbstractCommand result = null;
		if (m_commands != null && !isCanceled()) {
			m_activeindex++;
			if (m_activeindex < m_commands.size()) {
				result = (AbstractCommand) m_commands.get(m_activeindex);
			}
		}
		return result;
	}

	/** invokes this command */
	public void invoke() throws SQLException {
		AbstractCommand cmd = getNextCommand();
		while (cmd != null && !isCanceled()) {
			cmd.invoke();
			cmd = getNextCommand();
		}
	}

}
