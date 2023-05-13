package com.jeta.abeille.gui.update;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.utils.SQLAction;

import com.jeta.abeille.gui.command.AbstractCommand;

import com.jeta.foundation.i18n.I18N;

/**
 * This command runs a sql prepared statement in the background. This is for
 * invoking an insert, update, or delete command. The command is run in a
 * background thread so it can be canceled
 * 
 * @author Jeff Tassin
 */
public class UpdateCommand extends AbstractCommand {
	private PreparedStatement m_pstmt;
	private InstanceController m_controller;
	private InstanceModel m_model;
	private InstanceFrame m_frame;

	private SQLAction m_action;

	/**
	 * ctor
	 */
	public UpdateCommand(InstanceController controller, PreparedStatement pstmt, SQLAction action) {
		m_controller = controller;
		m_pstmt = pstmt;
		m_action = action;
		m_model = m_controller.getModel();
		m_frame = m_controller.getFrame();
	}

	/**
	 * Cancels the operation
	 */
	public void cancel() throws SQLException {
		// @todo cancel.statement
		try {
			setCanceled(true);
			m_pstmt.cancel();
			m_model.resetConnection();
		} catch (Exception e) {
			// just trap here
		}

	}

	/**
	 * Command implementation. Invoked by the CommandRunner
	 */
	public void invoke() throws SQLException {
		try {
			if (m_action == SQLAction.INSERT)
				m_pstmt.executeUpdate();
			else if (m_action == SQLAction.UPDATE)
				m_pstmt.executeUpdate();
			else if (m_action == SQLAction.DELETE)
				m_pstmt.executeUpdate();

			m_pstmt.close();

			if (!isCanceled()) {
				m_model.setModified();
			}

		} catch (SQLException e) {
			// just trap here
			if (!isCanceled())
				setException(e);
		}

		commandCompleted();
	}

	/**
	 * Updates the user interface
	 */
	protected void commandCompleted() {
		Runnable gui_update = new Runnable() {
			public void run() {
				if (isCanceled()) {
					m_frame.setStatus(InstanceFrame.INSTANCE_POS, I18N.getLocalizedMessage("Command Canceled"));
					m_controller.commandCompleted();
				} else if (isSuccess()) {
					if (m_action == SQLAction.INSERT)
						m_frame.setStatus(InstanceFrame.INSTANCE_POS, I18N.getLocalizedMessage("Added"));
					else if (m_action == SQLAction.UPDATE)
						m_frame.setStatus(InstanceFrame.INSTANCE_POS, I18N.getLocalizedMessage("Modified"));
					else if (m_action == SQLAction.DELETE)
						m_frame.setStatus(InstanceFrame.INSTANCE_POS, I18N.getLocalizedMessage("Deleted"));

					m_controller.commandCompleted();
				} else {
					TSConnection conn = m_model.getConnection();
					TSDatabase db = (TSDatabase) conn.getImplementation(TSDatabase.COMPONENT_ID);
					boolean rollback = db.rollbackOnException();
					if (rollback) {
						m_model.resetConnection();
					}

					m_controller.showSQLError(getException(), null, rollback);
					m_controller.commandCompleted();
				}
			}
		};

		SwingUtilities.invokeLater(gui_update);
	}

}
