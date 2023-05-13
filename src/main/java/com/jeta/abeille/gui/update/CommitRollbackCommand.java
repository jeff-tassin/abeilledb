package com.jeta.abeille.gui.update;

import java.sql.SQLException;

import javax.swing.SwingUtilities;

import com.jeta.abeille.gui.command.AbstractCommand;

import com.jeta.foundation.i18n.I18N;

/**
 * This command runs a sql prepared statement in the background. This is for
 * invoking an insert, update, or delete command. The command is run in a
 * background thread so it can be canceled
 * 
 * @author Jeff Tassin
 */
public class CommitRollbackCommand extends AbstractCommand {
	private InstanceController m_controller;
	private InstanceModel m_model;
	private InstanceFrame m_frame;

	public static final int COMMIT = 1;
	public static final int ROLLBACK = 2;
	public static final int CANCELED = 4;

	private int m_action;

	/**
	 * ctor
	 */
	public CommitRollbackCommand(InstanceController controller, int action) {
		m_controller = controller;
		m_action = action;
		m_model = m_controller.getModel();
		m_frame = m_controller.getFrame();
	}

	/**
	 * Cancels the operation
	 */
	public void cancel() throws SQLException {
		m_model.resetConnection();
		m_action = CANCELED;
	}

	/**
	 * Command implementation. Invoked by the CommandRunner
	 */
	public void invoke() throws SQLException {
		try {
			if (m_action == COMMIT) {
				m_model.commit();

			} else if (m_action == ROLLBACK) {
				m_model.rollback();
			}
		} catch (SQLException e) {
			// just trap here
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
				if (isSuccess()) {
					if (m_action == COMMIT)
						m_frame.setStatus(InstanceFrame.INSTANCE_POS, I18N.getLocalizedMessage("Committed"));
					else if (m_action == ROLLBACK)
						m_frame.setStatus(InstanceFrame.INSTANCE_POS, I18N.getLocalizedMessage("Rolled back"));
					m_controller.commandCompleted();
				} else {
					m_controller.showSQLError(getException(), null, false);
					m_controller.commandCompleted();
				}
			}
		};

		SwingUtilities.invokeLater(gui_update);
	}

}
