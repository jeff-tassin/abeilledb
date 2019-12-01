package com.jeta.abeille.gui.formbuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.utils.ConnectionReference;

import com.jeta.abeille.gui.command.AbstractCommand;
import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.gui.update.InstanceController;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.SubInstanceProxy;

import com.jeta.foundation.i18n.I18N;

/**
 * This command runs a subquery in the background.
 * 
 * @author Jeff Tassin
 */
public class SubQueryCommand extends AbstractCommand {
	private SubQuery m_subquery;

	/** the reference to the connection */
	private ConnectionReference m_connectionref;

	private FormInstanceController m_controller;

	/**
	 * ctor
	 */
	public SubQueryCommand(ConnectionReference connref, FormInstanceController controller, SubQuery query)
			throws SQLException {
		m_controller = controller;
		m_subquery = query;
		m_connectionref = connref;
	}

	public void cancel() {

	}

	/**
	 * Updates the user interface
	 */
	protected void commandCompleted() {
		Runnable gui_update = new Runnable() {
			public void run() {
				// @todo handle error here
				if (isSuccess()) {
					QueryResultSet qset = m_subquery.getQueryResults();
					SubInstanceProxy instanceproxy = new SubInstanceProxy(m_subquery.getReportables(), qset);

					InstanceModel instancemodel = m_controller.getModel();
					instancemodel.setInstanceProxy(instanceproxy);
					m_controller.updateView();
					m_controller.commandCompleted();
				} else {
					InstanceModel imodel = m_controller.getModel();
					TSConnection conn = imodel.getConnection();
					TSDatabase db = (TSDatabase) conn.getImplementation(TSDatabase.COMPONENT_ID);
					boolean rollback = db.rollbackOnException();
					if (rollback)
						imodel.resetConnection();

					m_controller.showSQLError(getException(), null, rollback);
				}
			}
		};
		SwingUtilities.invokeLater(gui_update);
	}

	/**
	 * Invokes this command
	 */
	public void invoke() {
		try {
			m_subquery.executeQuery(m_connectionref, m_controller.getFormatter());
		} catch (SQLException se) {
			se.printStackTrace();
			setException(se);
		}
		commandCompleted();
	}

}
