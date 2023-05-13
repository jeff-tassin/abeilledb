package com.jeta.abeille.database.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.LinkedList;

import com.jeta.abeille.database.model.TSConnection;

/**
 * This class represents a reference counted sql Connection. In the application,
 * we allow multiple views for a given result set. So, we need a way to cleanly
 * close the connection/result set when the last view is closed. We use this
 * reference counted class to handle that. When the ref count goes to zero, we
 * close the connection. This puts the responsibility on the user of this class
 * to call addRef, releaseRef at the appropriate times. Furthermore, some views
 * may make modifications and commits/rollbacks with the given connection. This
 * class allows other users to get notified of these events and react
 * accordingly.
 * 
 * @author Jeff Tassin
 */
public class ConnectionReference {
	private TSConnection m_tsconn;
	private Connection m_connection;
	private LinkedList m_listeners;

	/**
	 * this is an identifier that identifies this connection reference. We only
	 * use this for debugging. There can be serveral connections opened and
	 * often we need to know if a series of commands are working against the same
	 * connection. We use this uid for that purpose
	 */
	private String m_uid;

	/** a counter to compute the next UID for this reference */
	private static int m_count = 0;

	/**
	 * ctor
	 */
	public ConnectionReference(TSConnection tsconn, Connection conn) {
		m_tsconn = tsconn;
		m_connection = conn;

		synchronized (ConnectionReference.class) {
			m_count++;
		}

		m_uid = "ConnectionReference." + String.valueOf(m_count);
	}

	/**
	 * Adds a listener to this reference. Allows the caller to get an event when
	 * the connection is closed, committed, or rolled back
	 */
	public synchronized void addListener(ConnectionReference.ConnectionListener listener) {
		if (m_listeners == null)
			m_listeners = new LinkedList();
	}

	/**
	 * Closes the connection. Notifies any listener that the connection was
	 * closed. We allow any caller to close the connection even if the ref count
	 * is not zero. This allows other users of the same connection reference to
	 * handle the closure accordingly.
	 */
	public void closeConnection() throws SQLException {
		if (m_connection != null) {
			m_tsconn.release(m_connection);
		}
	}

	/**
	 * Commits any transactions on the connection. Notifies any listener of the
	 * event.
	 */
	public void commit() throws SQLException {
		if (m_connection != null) {
			// m_connection.commit();
			m_tsconn.jetaCommit(m_connection);
		}
	}

	/**
	 * @return a new statement object
	 */
	public Statement createStatement() throws SQLException {
		return m_connection.createStatement();
	}

	/**
	 * Close the connection here
	 */
	public void finalize() {
		try {

			m_tsconn.release(m_connection);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @return the underlying connection manager
	 */
	public TSConnection getTSConnection() {
		return m_tsconn;
	}

	/**
	 * @return the underlying connection object. Caution, do not cache this
	 *         value. The connection can be closed underneath you if you do.
	 */
	public Connection getConnection() {
		return m_connection;
	}

	/**
	 * @return the uid for this connection
	 */
	public String getUID() {
		return m_uid;
	}

	/**
	 * Removes a listener for this reference.
	 */
	public void removeListener(ConnectionReference.ConnectionListener listener) {
		assert (m_listeners != null);
		m_listeners.remove(listener);
	}

	/**
	 * Rolls back any transactions on the connection. Notifies any listener of
	 * the event.
	 */
	public void rollback() throws SQLException {
		if (m_connection != null) {
			// m_connection.rollback();
			m_tsconn.jetaRollback(m_connection);
		}
	}

	/**
	 * @return a new prepared statement object
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return m_connection.prepareStatement(sql);
	}

	/**
	 * Interface definition for listener
	 */
	public interface ConnectionListener {
		public void close();
	}

}
