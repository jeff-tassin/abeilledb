package com.jeta.abeille.gui.update;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.gui.command.AbstractCommand;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.foundation.i18n.I18N;

/**
 * @author Jeff Tassin
 */
public abstract class InstanceBaseCommand extends AbstractCommand {
	private Statement m_stmt;
	private InstanceController m_controller;
	private InstanceModel m_instancemodel;
	private InstanceFrame m_frame;

	/**
	 * ctor
	 */
	public InstanceBaseCommand(InstanceController controller, Statement stmt, String sql) {
		m_controller = controller;
		m_stmt = stmt;
		m_instancemodel = m_controller.getModel();
		m_frame = m_controller.getFrame();
		setSQL(sql);
	}

	/**
	 * Cancels the operation
	 */
	public void cancel() throws SQLException {
		// @todo cancel.statement
		try {
			setCanceled(true);
			m_stmt.cancel();
			m_instancemodel.resetConnection();
		} catch (Exception e) {
			// just trap here
		}

		try {
			m_stmt.close();
		} catch (Exception e) {
			// just trap here
		}
	}

	/**
	 * @return the instance controller
	 */
	public InstanceController getController() {
		return m_controller;
	}

	/**
	 * Resets the connection for all errors. Derived classes should call on
	 * error
	 */
	protected void handleError() {
		assert (isError());
		TSConnection conn = m_instancemodel.getConnection();
		TSDatabase db = (TSDatabase) conn.getImplementation(TSDatabase.COMPONENT_ID);
		boolean rollback = db.rollbackOnException();
		if (rollback)
			m_instancemodel.resetConnection();

		m_controller.showSQLError(getException(), null, rollback);
	}

	/**
	 * Updates the frame status
	 */
	protected void handleCancel() {
		assert (isCanceled());
		m_frame.setStatus(InstanceFrame.INSTANCE_POS, I18N.getLocalizedMessage("Command Canceled"));
	}

	/**
	 * Sets the statement for this command
	 */
	protected void setStatement(Statement stmt) {
		m_stmt = stmt;
	}
}
