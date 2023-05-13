package com.jeta.abeille.gui.command;

import java.sql.SQLException;

/**
 * Invokes a database command in a background thread.
 * 
 * @author Jeff Tassin
 */
public class CommandRunner {

	/** the object that performs the command against the database */
	private Command m_command;

	/**
	 * flag that indicates the state of the command (i.e. waiting, completed,
	 * canceled, error )
	 */
	private int m_state;

	private static final int WAITING = 1;
	private static final int COMPLETED = 2;
	private static final int CANCELED = 3;
	private static final int ERROR = 4;

	/**
	 * ctor Runs the command without showing the cancel dialog. The caller is
	 * responsible for handling this
	 */
	public CommandRunner(Command command) {
		m_command = command;
	}

	/**
	 * Invokes the command with a worker thread.
	 * 
	 * @return always returns 0
	 */
	public int invoke() {
		Worker worker = new Worker();
		Thread t = new Thread(worker);
		t.start();
		return 0;
	}

	/**
	 * Worker thread that actually invokes the command
	 */
	public class Worker implements Runnable {
		/**
		 * Runnable implementation
		 */
		public void run() {
			try {
				m_command.invoke();
			} catch (SQLException e) {
				// should not be here for non-modal commands, they should handle
				// the errors
				// on their own
				e.printStackTrace();
			}
		}
	}

}
