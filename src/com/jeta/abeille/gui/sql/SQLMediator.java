package com.jeta.abeille.gui.sql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.abeille.gui.queryresults.ResultsManager;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;

import org.netbeans.editor.BaseDocument;

/**
 * This class is a mediator between the SQL thread and the SQLController
 * 
 * @author Jeff Tassin
 */
public class SQLMediator {
	/** the underlying database connection */
	private ConnectionReference m_connectionref;

	/** the statement used for executing the queries */
	private Statement m_statement;

	/**
	 * The object that contains the sql we are going to execute. This can be one
	 * of two objects: a SQLBuffer or a String.
	 */
	private Object m_sqlsource;

	/** the controller object for the given buffer */
	private SQLMediatorListener m_listener;

	/** the timer updater timer */
	private Timer m_timer;

	/** the timer listener */
	private TimerListener m_timerlistener = new TimerListener();

	/** elapsed time of the SQL command (millisecs) */
	private long m_starttime;

	/** the amount of time to wait before showing the cancel popup */
	private static int POPUP_DELAY = 3000;

	/**
	 * the current start and end positions of the SQL in the document we are
	 * processing
	 */
	private int m_startpos;
	private int m_endpos;

	/** if an error occured, this is it */
	private Exception m_exception;

	/** the results of the query */
	private ResultsManager m_resultsmgr;

	/** the last sql command executed */
	private String m_lastsql;

	/** the thread that does the work */
	private SQLThread m_thread;

	/**
	 * set to true if the user issued an execute current command. False,
	 * otherwise
	 */
	private boolean m_step = false;

	/* mediator state definitions */
	public static final int RUNNING = 1;
	public static final int CANCELED = 2;
	public static final int SUCCESS = 3;
	public static final int ERROR = 4;
	public static final int COMMAND_NOT_FOUND = 5; // no SQL was found to
													// execute (this is not an
													// error condition)

	/* the mediator state */
	private int m_state = RUNNING;

	/**
	 * ctor
	 * 
	 * @param sqlSource
	 *            the object that contains the sql we are going to execute. This
	 *            can be one of two objects: a SQLBuffer or a String.
	 * @param listener
	 *            an object that gets event notifications (error, completed,
	 *            status) from this mediator.
	 */
	public SQLMediator(ConnectionReference cref, Object sqlSource, SQLMediatorListener listener) throws SQLException {
		assert (sqlSource instanceof SQLBuffer || sqlSource instanceof String);

		m_connectionref = cref;
		m_sqlsource = sqlSource;
		m_listener = listener;
		m_starttime = System.currentTimeMillis();

		if (sqlSource instanceof SQLBuffer) {
			SQLBuffer sqlbuff = (SQLBuffer) sqlSource;
			sqlbuff.setMediator(this);
		}

		// start the timer for updating the elapsed time of the query on the
		// status bar
		m_timerlistener = new TimerListener();
		m_timer = new Timer(500, m_timerlistener);
		m_timer.start();
	}

	/**
	 * Called from the SQLThread to indicate that it is beginning to execute a
	 * SQL statement at the given position in the document.
	 * 
	 * @param startpos
	 *            the start position of the sql statement in the document we are
	 *            processing
	 * @param endpos
	 *            the end position of the sql statement in the document we are
	 *            processing
	 */
	void beginExecuteStatement(int startpos, int endpos) {
		m_startpos = startpos;
		m_endpos = endpos;
		m_starttime = System.currentTimeMillis();
		try {
			SwingUtilities.invokeAndWait(new SwingRunner(this, startpos, endpos));
		} catch (Exception e) {

		}
	}

	/**
	 * Cancels the current mediator task
	 */
	public void cancel() {
		m_state = CANCELED;

		if (m_connectionref != null) {
			m_connectionref = null;
			if (m_sqlsource instanceof SQLBuffer) {
				SQLBuffer buffer = (SQLBuffer) m_sqlsource;
				// buffer.resetConnection();
			}
		}
	}

	/**
	 * Called when an exception occurs. We show a dialog showing the SQL error
	 * as well as the start/endpos. This is called by the SQLThread
	 */
	void commandCompleted(int startpos, int endpos, Exception e) {
		if (m_state == CANCELED) {
			commandCanceled();
		} else if (m_state == RUNNING) {
			m_exception = e;
			m_startpos = startpos;
			m_endpos = endpos;

			final SQLMediatorEvent evt = SQLMediatorEvent.createErrorEvent(SQLMediator.this, startpos, endpos);
			Runnable update = new Runnable() {
				public void run() {
					m_state = ERROR;
					finish();

					m_listener.notifyEvent(evt);
				}
			};
			SwingUtilities.invokeLater(update);
		}
	}

	/**
	 * Called when the sql command has been successfully completed. Notify the
	 * controller of the result using a Swing event. This is called by the
	 * SQLThread
	 */
	void commandCompleted(int startpos, int endpos, ResultsManager rmgr) {
		if (rmgr != null) {
			m_resultsmgr = rmgr;
		} else {
			try {
				m_statement.close();
			} catch (Exception e) {
				// just eat it here
			}
		}

		final SQLMediatorEvent evt = SQLMediatorEvent.createCompletedEvent(SQLMediator.this, startpos, endpos);
		Runnable update = new Runnable() {
			public void run() {
				m_state = SUCCESS;
				finish();
				m_listener.notifyEvent(evt);
			}
		};
		SwingUtilities.invokeLater(update);
	}

	/**
	 * Called if the sql command was canceled. This is called by the worker
	 * thread
	 */
	void commandCanceled() {
		Runnable update = new Runnable() {
			public void run() {
				m_state = CANCELED;
				finish();
				m_listener.notifyEvent(new SQLMediatorEvent(SQLMediator.this, SQLMediatorEvent.ID_COMMAND_FINISHED));
			}
		};
		SwingUtilities.invokeLater(update);
	}

	/**
	 * Called if no SQL was found to execute
	 */
	void commandNotFound() {
		Runnable update = new Runnable() {
			public void run() {
				m_state = COMMAND_NOT_FOUND;
				finish();
				m_listener.notifyEvent(new SQLMediatorEvent(SQLMediator.this, SQLMediatorEvent.ID_COMMAND_FINISHED));
			}
		};
		SwingUtilities.invokeLater(update);
	}

	/**
	 * Sets the GUI back to an editable state.
	 */
	private void finish() {
		if (m_state == ERROR) {
			try {
				if (m_statement != null)
					m_statement.close();
			} catch (Exception e) {
				// okay to eat here
			}
		}

		if (m_sqlsource instanceof SQLBuffer) {
			SQLBuffer sqlbuff = (SQLBuffer) m_sqlsource;
			sqlbuff.setMediator(null);
		}

		m_timer.stop();
		m_timer.removeActionListener(m_timerlistener);
		m_timer = null;
	}

	/** for testing */
	public void finalize() throws Throwable {
		try {
			super.finalize();
		} catch (Exception e) {

		}
	}

	/**
	 * @return the elapsed time (in milliseconds) since this query was started
	 */
	public long getElapsedTime() {
		long diff = System.currentTimeMillis() - m_starttime;
		return diff;
	}

	/**
	 * @return the exception that was thrown if an error occured during
	 *         execution the sql
	 */
	public Exception getException() {
		return m_exception;
	}

	/**
	 * @return the last SQL command that this mediator just completed
	 */
	public String getLastSQL() {
		return m_lastsql;
	}

	/**
	 * @return the line number of the last processed SQL statement
	 */
	int getStartPos() {
		return m_startpos;
	}

	int getEndPos() {
		return m_endpos;
	}

	/**
	 * @return the result of the sql operation. This can be the following:
	 *         SUCCESS, CANCELED, or ERROR
	 */
	public int getResult() {
		return m_state;
	}

	/**
	 * @return return the underlying resultsets
	 */
	public ResultsManager getResultsManager() {
		return m_resultsmgr;
	}

	/**
	 * @return The object that is the source for the sql we are going to
	 *         execute. This can be one of two objects: a SQLBuffer or a String.
	 */
	public Object getSQLSource() {
		return m_sqlsource;
	}

	/**
	 * @return the statement used to execute the sql
	 */
	Statement getStatement() {
		return m_statement;
	}

	/**
	 * @return true if there are results from the SQL statement
	 */
	public boolean hasResults() {
		return (m_resultsmgr != null && m_resultsmgr.size() > 0);
	}

	/**
	 * @return true if the query was canceled
	 */
	public boolean isCanceled() {
		return (m_state == CANCELED);
	}

	/**
	 * @return true if the user issued an execute current command. False,
	 *         otherwise
	 */
	public boolean isStep() {
		return m_step;
	}

	/**
	 * @return true if the query was successful
	 */
	public boolean isSuccess() {
		return (m_state == SUCCESS || m_state == COMMAND_NOT_FOUND);
	}

	/**
	 * Starts the sql command
	 */
	public void start() throws SQLException {
		start(-1, false);
	}

	/**
	 * Starts the sql command at the given document position. If the start
	 * position is in the middle of a SQL statement, then the beginning of the
	 * statement is located and executed from the new start position.
	 * 
	 * @param startpos
	 *            the start position in the document.
	 */
	public void start(int startpos, boolean step) throws SQLException {
		// kicks off the sql thread
		try {
			Connection conn = m_connectionref.getConnection();
			TSConnection tsconn = m_connectionref.getTSConnection();
			int rtype = tsconn.getResultSetScrollType();
			int concurrency = tsconn.getResultSetConcurrency();
			m_statement = conn.createStatement(rtype, concurrency);
			m_statement.setMaxRows(TSUserPropertiesUtils.getInteger(TSConnection.ID_MAX_QUERY_ROWS, 500));

			if (m_sqlsource instanceof SQLBuffer) {
				SQLBuffer buffer = (SQLBuffer) m_sqlsource;
				JEditorPane editor = buffer.getEditor();
				if (editor != null) {
					BaseDocument doc = (BaseDocument) editor.getDocument();
					m_thread = new SQLThread(m_connectionref, m_statement, doc, this, startpos);
				}
			} else if (m_sqlsource instanceof String) {
				m_thread = new SQLThread(m_connectionref, m_statement, (String) m_sqlsource, this);
			} else {
				assert (false);
			}
		} catch (Exception e) {
			commandCompleted(0, 0, e);
			return;
		}
		m_step = step;
		m_thread.start(step);
	}

	/**
	 * Defines a runnable to handle updating SwingComponents. Invoked from
	 * SwingUtilities
	 */
	static class SwingRunner implements Runnable {
		private int m_startpos;
		private int m_endpos;
		private SQLMediator m_mediator;

		public SwingRunner(SQLMediator mediator, int startpos, int endpos) {
			m_startpos = startpos;
			m_endpos = endpos;
			m_mediator = mediator;
		}

		public void run() {
			m_mediator.m_listener.notifyEvent(SQLMediatorEvent.createStatementEvent(m_mediator, m_startpos, m_endpos));
		}
	}

	/**
	 * Defines an action listener that we use with a timer to update the time
	 * cell on the status popup
	 */
	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_listener.notifyEvent(SQLMediatorEvent.createTimeEvent(SQLMediator.this, getElapsedTime()));
		}
	}

}
