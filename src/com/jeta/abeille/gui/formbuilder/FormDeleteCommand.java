package com.jeta.abeille.gui.formbuilder;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.SwingUtilities;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.PreparedStatementWriter;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.command.AbstractCommand;
import com.jeta.abeille.gui.command.CompositeCommand;
import com.jeta.abeille.gui.update.InstanceComponent;
import com.jeta.abeille.gui.update.InstanceModel;

import com.jeta.abeille.query.Operator;
import com.jeta.abeille.query.Reportable;
import com.jeta.abeille.query.SQLBuilder;
import com.jeta.abeille.query.SQLBuilderFactory;

/**
 * This command runs the delete command for the FormInstanceView.
 * 
 * @author Jeff Tassin
 */
public class FormDeleteCommand extends CompositeCommand {
	private ConnectionReference m_connectionref;
	private FormInstanceController m_controller;

	private FormModel m_formmodel;
	private FormInstanceView m_instanceview;

	/**
	 * ctor
	 */
	public FormDeleteCommand(ConnectionReference connectionref, FormInstanceController controller) {
		m_connectionref = connectionref;
		m_controller = controller;
		m_formmodel = m_controller.getFormModel();
		m_instanceview = (FormInstanceView) m_controller.getView();
	}

	/**
	 * Cancels the operation
	 */
	public void cancel() throws SQLException {
		// @todo cancel.statement
		try {
			setCanceled(true);
			// m_stmt.cancel();
			// m_model.resetConnection();
		} catch (Exception e) {
			// just trap here
		}
	}

	/**
	 * Updates the user interface when the command has been completed
	 */
	protected void commandCompleted() {

		Runnable gui_update = new Runnable() {
			public void run() {
				m_controller.commandCompleted();
				if (isSuccess()) {

				} else if (isError()) {
					try {
						m_connectionref.rollback();
					} catch (Exception e) {
						e.printStackTrace();
					}

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
	 * @param formatter
	 *            the SQL formatter
	 * @return a formatted string message that indicates the sequence of SQL
	 *         statements that will be invoked when then command is run. This is
	 *         mainly for user feedback and diagnostics.
	 */
	public String getPlan(SQLFormatter formatter) throws SQLException {
		return getSQL();
	}

	/**
	 * @return the reportables for our update command. In this case it is all
	 *         constrained columns as well as all anchor table columsn in the
	 *         form
	 */
	Collection getReportables() {
		LinkedList result = new LinkedList();
		TableId anchorid = m_controller.getAnchorTable();
		if (anchorid == null)
			return result;

		FormInstanceMetaData fimd = m_formmodel.getInstanceMetaData();
		for (int index = 0; index < fimd.getColumnCount(); index++) {
			ColumnMetaData cmd = fimd.getColumnMetaData(index);
			if (anchorid.equals(cmd.getParentTableId()))
				result.add(new Reportable(cmd));
			else if (m_instanceview.isConstrained(cmd))
				result.add(new Reportable(cmd));
		}
		return result;
	}

	/**
	 * @return the SQL (for a prepared statement) that will delete the selected
	 *         data from the anchor table
	 */
	public String getSQL() {
		TableId anchorid = m_controller.getAnchorTable();
		StringWriter writer = new StringWriter();
		writer.write("DELETE FROM " + anchorid.getFullyQualifiedName() + " ");
		LinkModel linkmodel = m_formmodel.getLinkModel();
		Collection tables = m_formmodel.getTables();
		Collection constraints = m_instanceview.getConstraints(m_controller.getFormatter());
		Collection reportables = getReportables();

		SQLBuilder builder = SQLBuilderFactory.createBuilder(m_controller.getConnection());
		String wheresql = builder.buildWhere(tables, linkmodel, constraints, reportables, m_formmodel.getCatalog(),
				m_formmodel.getSchema());
		writer.write(wheresql);
		writer.write("\n");
		return writer.toString();
	}

	/**
	 * Command implementation. Invoked by the CommandRunner
	 */
	public void invoke() throws SQLException {
		try {
			SQLFormatter formatter = m_controller.getFormatter();
			String sql = getSQL();
			PreparedStatement pstmt = m_connectionref.getConnection().prepareStatement(sql);
			pstmt.executeUpdate();
			if (!isCanceled()) {
				m_controller.getModel().setModified();
			}
			// @todo close statement
		} catch (SQLException e) {
			setException(e);
		} catch (Exception e) {
			// just trap here
			e.printStackTrace();
		}
		commandCompleted();
	}

}
