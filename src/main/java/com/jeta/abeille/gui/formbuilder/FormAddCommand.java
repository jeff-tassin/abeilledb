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

import javax.swing.SwingUtilities;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.PreparedStatementWriter;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.command.AbstractCommand;
import com.jeta.abeille.gui.command.CompositeCommand;

import com.jeta.abeille.gui.update.InstanceModel;

/**
 * This command runs the update, insert, or delete command for the
 * FormInstanceView. This command is run after we run all relevlent queries to
 * get the linked column data
 * 
 * @author Jeff Tassin
 */
public class FormAddCommand extends CompositeCommand {
	private ConnectionReference m_connectionref;
	private FormInstanceController m_controller;

	private String PADDING = "                                                                                                 ";

	/**
	 * This is a hash map of value proxies (values) keyed on column metadata in
	 * the form
	 */
	private HashMap m_proxies;

	/**
	 * ctor
	 */
	public FormAddCommand(ConnectionReference connectionref, FormInstanceController controller, HashMap proxies) {
		m_connectionref = connectionref;
		m_controller = controller;
		m_proxies = proxies;
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

	public String getSQL() {
		TableId tableid = m_controller.getAnchorTable();
		ColumnMetaData[] cols = getColumns();

		StringBuffer values = new StringBuffer();
		StringWriter writer = new StringWriter();
		writer.write("INSERT INTO " + tableid.getFullyQualifiedName() + " (");
		for (int index = 0; index < cols.length; index++) {
			writer.write(cols[index].getColumnName());
			values.append(" ?");
			if ((index + 1) < cols.length) {
				writer.write(", ");
				values.append(", ");
			}
		}

		writer.write(") VALUES (");
		writer.write(values.toString());
		writer.write(")\n");
		writer.flush();
		return writer.toString();
	}

	ColumnMetaData[] getColumns() {
		Collection c = m_proxies.values();
		ArrayList colsarray = new ArrayList(m_proxies.keySet());
		ColumnMetaData[] cols = (ColumnMetaData[]) colsarray.toArray(new ColumnMetaData[0]);
		return cols;
	}

	private ValueProxy getProxy(ColumnMetaData cmd) {
		return (ValueProxy) m_proxies.get(cmd);
	}

	/**
	 * Prepare the given prepared statement
	 */
	public void prepareStatement(PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {

		ColumnMetaData[] cols = getColumns();
		String sql = getSQL();
		for (int index = 0; index < cols.length; index++) {
			ValueProxy proxy = getProxy(cols[index]);
			proxy.prepareStatement(index + 1, pstmt, formatter);
		}
	}

	/**
	 * Command implementation. Invoked by the CommandRunner
	 */
	public void invoke() throws SQLException {
		try {
			super.invoke();
			// check if any of the queries were canceled or had a problem
			if (isSuccess()) {
				String sql = getSQL();
				PreparedStatement pstmt = m_connectionref.getConnection().prepareStatement(sql);
				PreparedStatementWriter pwriter = new PreparedStatementWriter(sql);

				SQLFormatter formatter = m_controller.getFormatter();
				prepareStatement(pstmt, formatter);
				prepareStatement(pwriter, formatter);

				java.util.logging.Logger logger = java.util.logging.Logger
						.getLogger(com.jeta.foundation.componentmgr.ComponentNames.APPLICATION_LOGGER);
				logger.fine(pwriter.getPreparedSQL());

				pstmt.executeUpdate();
				if (!isCanceled()) {
					m_controller.getModel().setModified();
				}
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
	 * Updates the user interface
	 */
	protected void commandCompleted() {

		Runnable gui_update = new Runnable() {
			public void run() {
				m_controller.commandCompleted();
				if (isCanceled()) {

				} else if (isSuccess()) {

				} else {
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
		// for formatting
		int offset = 0;
		StringBuffer buffer = new StringBuffer();
		ColumnMetaData[] cols = getColumns();
		for (int index = 0; index < cols.length; index++) {
			ColumnMetaData cmd = cols[index];
			String name = cmd.getColumnName();
			if (offset < name.length())
				offset = name.length();
		}

		// to handle the colon and space after the column name
		for (int index = 0; index < cols.length; index++) {
			ColumnMetaData cmd = cols[index];
			ValueProxy proxy = getProxy(cmd);
			String proxyplan = proxy.getPlan(m_controller.getConnection(), formatter);

			// if ( index > 0 )
			// buffer.append( "\n" );

			String colname = cmd.getColumnName();
			buffer.append(colname);
			buffer.append(": ");

			int pad_len = 0;
			if (colname.length() < offset) {
				pad_len = offset - colname.length();
				if (pad_len > PADDING.length())
					pad_len = PADDING.length();
			}

			boolean bfirst = true;
			java.util.StringTokenizer st = new java.util.StringTokenizer(proxyplan, "\n");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (bfirst) {
					bfirst = false;
					if (pad_len > 0)
						buffer.append(PADDING.substring(0, pad_len));
				} else {
					buffer.append(PADDING.substring(0, colname.length() + 2 + pad_len));
				}

				buffer.append(token);
				buffer.append("\n");
			}
		}

		String sql = getSQL();
		buffer.append("\n");
		buffer.append(sql);
		return buffer.toString();
	}

}
