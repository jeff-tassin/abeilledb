package com.jeta.abeille.gui.query;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.ConnectionReference;

import com.jeta.abeille.gui.command.AbstractCommand;

import com.jeta.abeille.gui.sql.SQLMediator;
import com.jeta.abeille.gui.sql.SQLMediatorEvent;
import com.jeta.abeille.gui.sql.SQLMediatorListener;

import com.jeta.foundation.utils.TSUtils;

/**
 * This command object works with a SQLMediator/SQLThread. It is different from
 * a standard QueryCommand in that SQL statements with '?' user inputs are
 * supported.
 * 
 * @author Jeff Tassin
 */
public class AdvancedQueryCommand extends AbstractCommand implements SQLMediatorListener {
	/** flag that indicates if the query is currently running */
	private boolean m_busy;

	/** the mediator that runs our query */
	private SQLMediator m_mediator;

	public AdvancedQueryCommand(String msg, ConnectionReference cref, String sql) throws SQLException {
		m_busy = true;
		Connection conn = cref.getConnection();
		TSConnection tsconn = cref.getTSConnection();
		int rtype = tsconn.getResultSetScrollType();
		int concurrency = tsconn.getResultSetConcurrency();
		Statement stmt = conn.createStatement(rtype, concurrency);

		m_mediator = new SQLMediator(cref, sql, this);
		m_mediator.start();
	}

	public void cancel() {
		// no op here
		if (m_mediator != null) {
			m_mediator.cancel();
		}
	}

	/**
	 * @return the mediator that runs our query
	 */
	public SQLMediator getMediator() {
		return m_mediator;
	}

	/**
	 * AbstractCommand implementation. Simply wait until the SQLMediator has
	 * finished the query. This allows the ModalCommandRunner to popup a dialog
	 * if the query takes a while. The SQLMediatorEvent status notifications are
	 * not used here because we don't have a status UI for the query builder.
	 */
	public void invoke() throws SQLException {
		if (isBusy()) {
			synchronized (this) {
				try {
					wait();
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}
		}
	}

	public synchronized boolean isBusy() {
		return m_busy;
	}

	/**
	 * SQLMediatorListener implementation. Shows the results of the given query
	 * by launching the sql results frame
	 * 
	 * @param mediator
	 *            the sql mediator that ran the command
	 */
	public void notifyEvent(SQLMediatorEvent evt) {
		if (evt.getID() == SQLMediatorEvent.ID_COMMAND_FINISHED) {
			setBusy(false);
			synchronized (this) {
				try {
					notify();
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}
		}
	}

	public synchronized void setBusy(boolean busy) {
		m_busy = busy;
	}
}
