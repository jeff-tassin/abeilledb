package com.jeta.abeille.gui.update;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.command.AbstractCommand;
import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.query.Reportable;

import com.jeta.foundation.i18n.I18N;

/**
 * This command runs a sql prepared statement in the background. This is for
 * invoking a query. The command is run in a background thread so it can be
 * canceled
 * 
 * @author Jeff Tassin
 */
public class QueryInstancesCommand extends AbstractCommand {
	private PreparedStatement m_pstmt;

	/** the underlying sql that is the basis for the prepared statement */
	private String m_sql;

	private InstanceController m_controller;
	private InstanceModel m_instancemodel;

	private InstanceFrame m_frame;

	private QueryResultSet m_queryset;

	/** this is the instance proxy that results from the query */
	private DefaultInstanceProxy m_instanceproxy;

	/**
	 * if this query is against a single table, then this tableid should be set.
	 * This is really for a limitation in Postgres because it does not return
	 * the table in the result set
	 */
	private TableId m_tableid;

	/**
	 * A collection of Reportable objects. If this query is against multiple
	 * tables (i.e. join), then the reportables should be set. This is really
	 * for a limitation in Postgres because it does not return the tables in the
	 * result set
	 */
	private Collection m_reportables;

	/**
	 * ctor
	 */
	public QueryInstancesCommand(InstanceController controller, PreparedStatement pstmt, TableId tableId, String sql) {
		m_controller = controller;
		m_pstmt = pstmt;
		m_sql = sql;
		m_instancemodel = m_controller.getModel();
		m_frame = m_controller.getFrame();
		m_tableid = tableId;
		if (m_tableid != null) {
			assert (m_tableid.getCatalog() != null);
		}
	}

	/**
	 * ctor
	 * 
	 * @param tables
	 *            a collection of table ids that make up the query
	 * @param constraints
	 *            a collection of QueryConstraint objects ( as returned by
	 *            InstanceView) There is one object per filtered column. The AND
	 *            logical connective should be set.
	 * @param reportables
	 *            a collection or Reportable objects
	 */
	public QueryInstancesCommand(InstanceController controller, PreparedStatement pstmt, Collection reportables) {
		this(controller, pstmt, (TableId) null, null);
		m_reportables = reportables;
	}

	/**
	 * Cancels the operation
	 */
	public void cancel() throws SQLException {
		// @todo cancel.statement
		try {
			setCanceled(true);
			m_instancemodel.resetConnection();
		} catch (Exception e) {
			// just trap here
		}

	}

	/**
	 * Command implementation. Invoked by the CommandRunner
	 */
	public void invoke() throws SQLException {
		try {
			ResultSetReference ref = new ResultSetReference(m_instancemodel.getWriteConnection(), m_pstmt,
					m_pstmt.executeQuery(), null);

			ref.setSQL(m_sql);
			ref.setUnprocessedSQL(m_sql);

			Catalog catalog = null;
			if (m_tableid != null)
				catalog = m_tableid.getCatalog();

			m_queryset = new QueryResultSet(catalog, ref);
			m_queryset.first();

			/**
			 * this is because Postgres does not set the tableid in the result
			 * set, so we need to explictily do it here for the single table
			 * case
			 */
			if (m_tableid != null) {
				ColumnMetaData[] cmd = m_queryset.getColumnMetaData();
				for (int index = 0; index < cmd.length; index++) {
					cmd[index].setParentTableId(m_tableid);
				}
			} else if (m_reportables != null) {
				ColumnMetaData[] cmd = m_queryset.getColumnMetaData();

				int index = 0;
				Iterator iter = m_reportables.iterator();
				while (iter.hasNext()) {
					Reportable reportable = (Reportable) iter.next();
					ColumnMetaData rcmd = reportable.getColumn();
					assert (rcmd.getColumnName().equals(cmd[index].getColumnName()));
					cmd[index].setParentTableId(rcmd.getParentTableId());
					index++;
				}
			}

			m_instanceproxy = new DefaultInstanceProxy(m_instancemodel, m_queryset);
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
				} else if (isSuccess()) {
					m_instancemodel.setInstanceProxy(m_instanceproxy);
					m_controller.commandCompleted();
				} else {

					TSConnection conn = m_instancemodel.getConnection();
					TSDatabase db = (TSDatabase) conn.getImplementation(TSDatabase.COMPONENT_ID);
					boolean rollback = db.rollbackOnException();
					if (rollback)
						m_instancemodel.resetConnection();

					m_controller.showSQLError(getException(), null, rollback);
					m_controller.commandCompleted();
				}
			}
		};
		SwingUtilities.invokeLater(gui_update);
	}

}
