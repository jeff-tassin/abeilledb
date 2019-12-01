package com.jeta.abeille.gui.sql;

import java.io.File;

import java.sql.Connection;
import java.sql.SQLException;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.ConnectionReference;

import com.jeta.foundation.gui.editor.Buffer;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class specializes a standard buffer for the SQL frame. Basically we add
 * behaviors that tell the clients if a query is currently being processed
 * against the SQL in the buffer.
 * 
 * @author Jeff Tassin
 */
public class SQLBuffer extends Buffer {
	/**
	 * flag that indicates if a query is currently active against the sql in
	 * this buffer
	 */
	private boolean m_busy;

	/** the connection mgr */
	private TSConnection m_tsconnection;

	/** the database connect */
	private ConnectionReference m_connectionref;

	/**
	 * The SQLMediator object if this buffer is currently executing a query
	 */
	private SQLMediator m_mediator;

	/**
	 * ctor
	 */
	public SQLBuffer(TSConnection conn) {
		m_tsconnection = conn;
		m_busy = false;
		setName("SQL");
	}

	/**
	 * Notifies the buffer that it is being closed
	 */
	protected void notifyClose() {
		m_connectionref = null;
	}

	/**
	 * @return the database connection for this buffer. This could throw an
	 *         exception because we need to create a new connection if the
	 *         connection is not already created. Each buffer has its own
	 *         connection
	 */
	public ConnectionReference getConnectionReference() throws SQLException {
		if (m_connectionref == null) {
			// @todo change getWriteConnection to take an objec instance so we
			// can do
			// some error checking
			m_connectionref = new ConnectionReference(m_tsconnection, m_tsconnection.getWriteConnection());
		}
		return m_connectionref;
	}

	/**
	 * @return the mediator for this buffer. This is only valid when a SQL
	 *         command(s) is being executed for this buffer. When the command
	 *         has completed, the mediator is set to null.
	 */
	public SQLMediator getMediator() {
		return m_mediator;
	}

	/**
	 * @return the name SQL
	 */
	public String getName() {
		return "SQL";
	}

	/**
	 * @return true if a query is currently running against this buffer
	 */
	public boolean isBusy() {
		return m_busy;
	}

	/**
	 * Resets the connection in the buffer. Releases the connection back to the
	 * pool which closes the connection
	 */
	void resetConnection() {
		try {
			try {
				m_connectionref.rollback();
			} catch (Exception e) {

			}
		} catch (Exception e) {
			TSUtils.printException(e);
		} finally {
			// the connectionref finalize will do an explicit close
			m_connectionref = null;
		}
	}

	/**
	 * Sets the busy flag for this buffer
	 */
	public void setBusy(boolean busy) {
		m_busy = busy;
	}

	/**
	 * Sets the editor that is associated with this buffer
	 */
	public void setEditor(javax.swing.JEditorPane editor) {
		super.setEditor(editor);

		java.util.TreeSet keyset = new java.util.TreeSet();
		editor.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keyset);
	}

	/**
	 * Override because a SQL buffer cannot have a file
	 */
	public void setFile(File f) {
		// override - SQL buffer cannot have a file
	}

	/**
	 * Sets the mediator for this buffer. This is only set when a SQL command(s)
	 * is being executed for this buffer. When the command has completed, the
	 * mediator is set to null.
	 */
	public void setMediator(SQLMediator mediator) {
		if (TSUtils.isDebug()) {
			if (mediator == null) {
				assert (m_mediator != null);
			} else {
				assert (m_mediator == null);
			}
		}
		m_mediator = mediator;
	}

	/**
	 * Updates the status bar. Currently, we only set the auto commit flag
	 */
	public void updateStatus() {
		String auto_commit = "";
		String max_rows = "";

		try {
			max_rows = String.valueOf(TSConnection.getMaxQueryRows());
			max_rows = I18N.format("Max_query_rows_1", max_rows);

		} catch (Exception e) {
			TSUtils.printException(e);
		}

		try {
			if (m_tsconnection.isAutoCommit())
				auto_commit = I18N.getLocalizedMessage("On");
			else
				auto_commit = I18N.getLocalizedMessage("Off");

			auto_commit = I18N.format("Autocommit_1", auto_commit);
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		org.netbeans.editor.StatusBar sb = org.netbeans.editor.Utilities.getEditorUI(getEditor()).getStatusBar();
		sb.setText("auto.commit", auto_commit);
		sb.setText("max.rows", max_rows);
	}
}
