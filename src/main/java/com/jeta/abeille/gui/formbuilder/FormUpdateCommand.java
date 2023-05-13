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
 * This command runs the update, insert, or delete command for the
 * FormInstanceView. This command is run after we run all relevlent queries to
 * get the linked column data
 * 
 * @author Jeff Tassin
 */
public class FormUpdateCommand extends CompositeCommand {
	private ConnectionReference m_connectionref;
	private FormInstanceController m_controller;

	private FormModel m_formmodel;
	private FormInstanceView m_instanceview;

	/**
	 * ctor
	 */
	public FormUpdateCommand(ConnectionReference connectionref, FormInstanceController controller) {
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
	 * @return a collection of ColumnMetaData objects that correspond to
	 *         modified values in the anchor table
	 */
	public Collection getModifiedColumns() {
		ArrayList setcols = new ArrayList();
		TableId anchorid = m_controller.getAnchorTable();

		FormInstanceMetaData fimd = m_formmodel.getInstanceMetaData();
		for (int index = 0; index < fimd.getColumnCount(); index++) {
			ColumnMetaData cmd = fimd.getColumnMetaData(index);
			InstanceComponent comp = m_instanceview.getInstanceComponent(cmd);
			if (comp != null && anchorid.equals(cmd.getParentTableId())) {
				// column is part of anchor table
				if (comp.isModified()) {
					Operator op = m_instanceview.getOperator(cmd);
					if (op == null) {
						setcols.add(cmd);
					}
				}
			}
		}// end for(...)

		return setcols;
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
	 * Generates the SQL for this update command. The update command updates
	 * data in the anchor table based on the given constraints
	 * 
	 * @param setcols
	 *            a collection of ColumnMetaData objects that are on the form
	 *            and have been modified by the user. We only use modified
	 *            columns in the update.
	 * @return the SQL statement that forms the basis for the update.
	 */
	public String getSQL(Collection setcols) {
		SQLFormatter formatter = m_controller.getFormatter();
		TableId anchorid = m_controller.getAnchorTable();
		if (setcols.size() > 0) {
			// PrintWriter writer = new PrintWriter( System.out );
			StringWriter writer = new StringWriter();
			writer.write("UPDATE " + anchorid.getFullyQualifiedName() + " SET ");
			Iterator iter = setcols.iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				writer.write(cmd.getColumnName());
				writer.write(" = ?");
				if (iter.hasNext()) {
					writer.write(", ");
				}
			}

			TSConnection conn = m_controller.getConnection();

			LinkModel linkmodel = m_formmodel.getLinkModel();
			Collection tables = m_formmodel.getTables();
			Collection constraints = m_instanceview.getConstraints(formatter);
			Collection reportables = getReportables();
			SQLBuilder builder = SQLBuilderFactory.createBuilder(conn);
			String wheresql = builder.buildWhere(tables, linkmodel, constraints, reportables, m_formmodel.getCatalog(),
					m_formmodel.getSchema());
			writer.write(wheresql);
			writer.write("\n");

			return writer.toString();
		} else
			return "";
	}

	/**
	 * @param formatter
	 *            the SQL formatter
	 * @return a formatted string message that indicates the sequence of SQL
	 *         statements that will be invoked when then command is run. This is
	 *         mainly for user feedback and diagnostics.
	 */
	public String getPlan(SQLFormatter formatter) throws SQLException {
		Collection modcols = getModifiedColumns();
		if (modcols.size() > 0) {
			PreparedStatementWriter pwriter = new PreparedStatementWriter(getSQL(modcols));
			prepareStatement(pwriter, modcols, formatter);
			return pwriter.getPreparedSQL();
		} else
			return "";

	}

	/**
	 * Command implementation. Invoked by the CommandRunner
	 */
	public void invoke() throws SQLException {
		try {
			SQLFormatter formatter = m_controller.getFormatter();
			Collection setcols = getModifiedColumns();
			PreparedStatement pstmt = m_connectionref.getConnection().prepareStatement(getSQL());
			prepareStatement(pstmt, setcols, formatter);
			pstmt.executeUpdate();
			if (!isCanceled()) {
				m_controller.getModel().setModified();
			}
		} catch (SQLException e) {
			setException(e);
		} catch (Exception e) {
			// just trap here
			e.printStackTrace();
		}
		commandCompleted();
	}

	/**
	 * Prepares the given prepared statement. It is assumed that the sql that
	 * was used to create the prepared statement matches the columns given in
	 * the setcols parameter.
	 */
	void prepareStatement(PreparedStatement pstmt, Collection setcols, SQLFormatter formatter) throws SQLException {
		int index = 0;
		Iterator iter = setcols.iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			InstanceComponent comp = m_instanceview.getInstanceComponent(cmd);
			comp.prepareStatement(index + 1, pstmt, formatter);
			index++;
		}
	}

}
