package com.jeta.abeille.gui.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JScrollPane;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.ParameterDirection;
import com.jeta.abeille.database.procedures.ProcedureParameter;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.database.utils.SQLFormatter;
import com.jeta.abeille.database.utils.SQLFormatterFactory;

import com.jeta.abeille.gui.command.ModalCommandRunner;
import com.jeta.abeille.gui.command.QueryCommand;
import com.jeta.abeille.gui.update.InstanceComponent;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This class provides support for invoking a stored procedure from the
 * application. It reads the procedure parameters from a data model and displays
 * in input dialog if the procedure requires inputs. The procedure is then
 * invoked. If there are any outputs, the result is displayed to the user.
 * 
 * @author Jeff Tassin
 */
public class ProcedureRunner {
	/** the underlying database connection */
	private TSConnection m_connection;

	/** the model that describes the procedure parameters */
	private ProcedureModel m_model;

	/**
	 * ctor
	 */
	public ProcedureRunner(TSConnection connection, ProcedureModel model) {
		m_connection = connection;
		m_model = model;
	}

	/**
	 * Executes the query for the given prepared statement
	 */
	private boolean executeQuery(ConnectionReference connref, PreparedStatement pstmt) {
		boolean bresult = false;
		QueryCommand cmd = new QueryCommand(connref, pstmt);
		ModalCommandRunner crunner = new ModalCommandRunner(m_connection, cmd);
		try {
			int result = crunner.invoke();
			if (result == ModalCommandRunner.COMPLETED) {
				ResultSetReference ref = cmd.getResultSetReference();
				showResults(ref);
				bresult = true;
			}
		} catch (SQLException e) {
			SQLErrorDialog.showErrorDialog(TSWorkspaceFrame.getInstance(), e, null);
			bresult = false;
		}

		return bresult;
	}

	/**
	 * Invokes the procedure and returns.
	 * 
	 * @return true if the procedure was successful
	 */
	public boolean invokeProcedure() {
		boolean bresult = false;
		if (m_model.getParameterCount() > 0) {
			ProcedureInputViewBuilder viewbuilder = new ProcedureInputViewBuilder(m_connection, m_model);
			final InstanceView view = viewbuilder.getView();
			JScrollPane scroller = new JScrollPane(view);
			scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, TSWorkspaceFrame.getInstance(), true);
			dlg.setPrimaryPanel(scroller);
			dlg.setTitle(I18N.format("Invoke_procedure_1", m_model.getName()));
			dlg.setSize(new java.awt.Dimension(500, 300));
			dlg.addComponentListener(new java.awt.event.ComponentAdapter() {
				public void componentResized(java.awt.event.ComponentEvent e) {
					view.doLayout();
				}
			});

			dlg.showCenter();
			if (dlg.isOk()) {
				try {
					Connection conn = m_connection.getWriteConnection();
					ConnectionReference connref = new ConnectionReference(m_connection, conn);

					String procname = m_model.getName();
					StringBuffer callsql = new StringBuffer();
					callsql.append("select ");
					callsql.append(procname);
					callsql.append("(");
					boolean bfirst;
					for (int index = 0; index < m_model.getParameterCount(); index++) {
						if (index > 0)
							callsql.append(", ");

						callsql.append("?");
					}
					callsql.append(")");

					SQLFormatterFactory factory = SQLFormatterFactory.getInstance(m_connection);
					SQLFormatter formatter = factory.createFormatter();

					PreparedStatement pstmt = conn.prepareStatement(callsql.toString());

					ProcedureParametersMetaData metadata = (ProcedureParametersMetaData) viewbuilder.getMetaData();
					for (int index = 0; index < metadata.getColumnCount(); index++) {
						ColumnMetaData cmd = metadata.getColumnMetaData(index);
						InstanceComponent comp = view.getInstanceComponent(cmd);
						comp.prepareStatement(index + 1, pstmt, formatter);
					}

					bresult = executeQuery(connref, pstmt);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				Connection conn = m_connection.getWriteConnection();
				ConnectionReference connref = new ConnectionReference(m_connection, conn);

				// form for no return value { call procname }
				// form for return value { ? = call procname }
				String procname = m_model.getName();
				StringBuffer callsql = new StringBuffer();
				callsql.append("select ");
				callsql.append(procname);
				callsql.append("()");

				PreparedStatement pstmt = conn.prepareStatement(callsql.toString());
				bresult = executeQuery(connref, pstmt);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return bresult;
	}

	/**
	 * Launches the SQLResultsFrame and displays the results from the stored
	 * procedure
	 * 
	 * @param ref
	 *            the result set reference to display
	 */
	public void showResults(ResultSetReference ref) {
		TSInternalFrame iframe = TSWorkspaceFrame.getInstance().createInternalFrame(
				com.jeta.abeille.gui.sql.SQLResultsFrame.class, false, m_connection.getId());
		iframe.setSize(400, 300);
		TSWorkspaceFrame.getInstance().addWindow(iframe);

		Object[] params = new Object[3];
		params[0] = m_connection;
		params[1] = ref;
		params[2] = null;
		iframe.initializeModel(params);
		// iframe.show();
	}

}
