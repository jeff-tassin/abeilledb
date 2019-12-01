package com.jeta.abeille.database.model;

import java.sql.SQLException;

import com.jeta.abeille.gui.command.AbstractCommand;
import com.jeta.abeille.gui.command.ModalCommandRunner;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This command represents a simple command that might take an arbitrarily long
 * time to complete.
 * 
 * @author Jeff Tassin
 */
public class DbModelJoinCommand extends AbstractCommand {
	/** the database connection */
	private TSConnection m_connection;

	private ModelData m_modeldata;

	/**
	 * ctor
	 */
	public DbModelJoinCommand(TSConnection conn, ModelData modeldata) {
		super(I18N.format("model_loading_1", modeldata.getName()), null);
		m_modeldata = modeldata;
		m_connection = conn;
	}

	/** cancels this command */
	public void cancel() throws SQLException {
		// no op
	}

	/**
	 * Runs the rename command
	 */
	public void invoke() throws SQLException {
		try {
			synchronized (m_modeldata) {
				if (!m_modeldata.isLoaded()) {
					m_modeldata.wait();
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
    */
	public static int runJoinCommand(TSConnection connection, ModelData modeldata) {
		try {
			DbModelJoinCommand cmd = new DbModelJoinCommand(connection, modeldata);
			ModalCommandRunner crunner = new ModalCommandRunner(connection, cmd);
			return crunner.invoke();
		} catch (Exception e) {
			TSUtils.printException(e);
			assert (false); // we should never get here
			return -1;
		}
	}
}
