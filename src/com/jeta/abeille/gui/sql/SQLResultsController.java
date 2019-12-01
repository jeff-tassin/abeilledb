package com.jeta.abeille.gui.sql;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.ref.WeakReference;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.PreparedStatementWriter;
import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.abeille.gui.command.ModalCommandRunner;
import com.jeta.abeille.gui.command.QueryCommand;

import com.jeta.abeille.gui.main.MainFrame;

import com.jeta.abeille.gui.queryresults.QueryResultsModel;
import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.gui.queryresults.QueryResultsView;

import com.jeta.abeille.gui.update.InstanceFrame;
import com.jeta.abeille.gui.update.InstanceFrameLauncher;
import com.jeta.abeille.gui.update.ShowInstanceFrameAction;
import com.jeta.abeille.gui.update.TableInstanceViewBuilder;
import com.jeta.abeille.gui.utils.ConfirmCommitPanel;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;
import com.jeta.foundation.gui.components.JPanelFrame;
import com.jeta.foundation.gui.components.JFrameEx;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.print.PrintPreviewDialog;

import com.jeta.foundation.gui.table.TablePrintable;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.gui.table.TableSelection;
import com.jeta.foundation.gui.table.TableSorter;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

/**
 * This is the controller for the SQLResultsFrame window
 * 
 * @author Jeff Tassin
 */
public class SQLResultsController extends TSController {
	/** the frame window we are controlling */
	private SQLResultsFrame m_frame;

	public SQLResultsController(SQLResultsFrame frame) {
		super(frame);
		m_frame = frame;

		assignAction(TSComponentNames.ID_COPY, new CopyAction());
		assignAction(SQLResultsNames.ID_EXPORT_ALL, new ExportAllAction());
		assignAction(SQLResultsNames.ID_EXPORT_SELECTION, new ExportSelectionAction());
		assignAction(TSComponentNames.ID_PRINT_PREVIEW, new PrintPreviewAction());

		assignAction(SQLResultsNames.ID_SHOW_INSTANCE, new ShowInstanceAction());
		assignAction(SQLResultsNames.ID_NO_SPLIT, new NoSplitAction());
		assignAction(SQLResultsNames.ID_SPLIT_VERTICAL, new SplitVerticalAction());
		assignAction(SQLResultsNames.ID_SPLIT_HORIZONTAL, new SplitHorizontalAction());

		assignAction(SQLResultsNames.ID_DELETE_INSTANCE, new DeleteInstanceAction());
		assignAction(SQLResultsNames.ID_COMMIT, new CommitAction());
		assignAction(SQLResultsNames.ID_ROLLBACK, new RollbackAction());

		assignAction(SQLResultsNames.ID_QUERY_INFO, new QueryInfoAction());

		assignAction(SQLResultsNames.ID_TABLE_OPTIONS, new TableOptionsAction());

		assignAction(SQLResultsNames.ID_FIRST, new FirstInstanceAction());
		assignAction(SQLResultsNames.ID_LAST, new LastInstanceAction());

		assignAction(SQLResultsNames.ID_PREFERENCES, new SQLResultsPreferencesAction());
		assignAction(SQLResultsNames.ID_REDO_QUERY, new RedoQueryAction());

		assignAction(SQLResultsNames.ID_SHOW_IN_FRAME_WINDOW, new DetachFrameAction());
	}

	private boolean confirmCommit() {
		boolean bcommit = true;
		if (isShowCommitDialog()) {
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
			ConfirmCommitPanel commitpanel = new ConfirmCommitPanel();
			dlg.setPrimaryPanel(commitpanel);
			dlg.setSize(dlg.getPreferredSize());
			dlg.setTitle(I18N.getLocalizedMessage("Confirm"));
			dlg.setInitialFocusComponent(dlg.getCloseButton());
			dlg.showCenter();
			bcommit = dlg.isOk();
			TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
			userprops.setProperty(SQLResultsPreferencesView.ID_SQL_RESULTS_CONFIRM_COMMIT,
					String.valueOf(commitpanel.isShowCommitDialog()));
		}
		return bcommit;
	}

	/**
	 * @return the underlying frame window
	 */
	public SQLResultsFrame getFrame() {
		return m_frame;
	}

	/**
	 * Actually does the delete in the database
	 */
	private void deleteInstance(int row) {
		// if we are here, then the query is against a single table.
		// so, we need to get the primary key values for each row
		// and use as a constraint. Only tables with primary keys
		// support deletes in the query results view.
		SQLResultsModel model = m_frame.getCurrentDataModel();
		QueryResultsView view = m_frame.getCurrentView().getView();

		TableId tableid = model.getTableId();
		StringBuffer sqlbuff = new StringBuffer();
		sqlbuff.append("delete from ");
		sqlbuff.append(tableid.getFullyQualifiedName());
		sqlbuff.append(" where ");

		ArrayList constraints = new ArrayList();
		ArrayList values = new ArrayList();

		boolean band = false;
		DbKey pk = model.getPrimaryKey();
		if (pk != null && pk.getColumnCount() > 0) {
			for (int col = 0; col < model.getColumnCount(); col++) {
				ColumnMetaData cmd = model.getColumnMetaData(col);
				if (pk.containsField(cmd.getColumnName())) {
					if (band)
						sqlbuff.append(" and ");

					band = true;
					sqlbuff.append(cmd.getColumnName());

					Object value = model.getValueAt(row, col);
					if (value == null) {
						sqlbuff.append(" is NULL");
					} else {
						sqlbuff.append(" = ?");
						constraints.add(cmd);
						values.add(value);
					}
				}
			}

		}

		Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
		ConnectionReference cref = model.getConnectionReference();
		try {
			String sql = sqlbuff.toString();

			PreparedStatement pstmt = cref.prepareStatement(sql);
			PreparedStatementWriter pwriter = new PreparedStatementWriter(sql);

			for (int count = 0; count < constraints.size(); count++) {
				ColumnMetaData cmd = (ColumnMetaData) constraints.get(count);
				pstmt.setObject(count + 1, values.get(count));
				pwriter.setObject(count + 1, values.get(count));
			}

			pstmt.executeUpdate();
			logger.fine(pwriter.getPreparedSQL());
		} catch (SQLException e) {
			logger.fine(e.getLocalizedMessage());
			invokeAction(SQLResultsNames.ID_ROLLBACK);
			showError(e);
		}
	}

	/**
	 * @return true if the commit dialog should be displayed before a commit.
	 */
	public boolean isShowCommitDialog() {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		return Boolean.valueOf(userprops.getProperty(SQLResultsPreferencesView.ID_SQL_RESULTS_CONFIRM_COMMIT, "true"))
				.booleanValue();
	}

	/**
	 * Invokes a dialog showing the exception. Also, send the exception message
	 * to the log
	 */
	void showError(SQLException e) {
		Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
		logger.fine(e.getLocalizedMessage());

		SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, m_frame, true);
		dlg.initialize(e, null, false);
		dlg.setSize(dlg.getPreferredSize());
		dlg.showCenter();
	}

	/**
	 * Performs a delete/commit on any rows marked for deletion
	 */
	public class CommitAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				if (confirmCommit()) {
					SQLResultsModel model = m_frame.getCurrentDataModel();
					ConnectionReference cref = model.getConnectionReference();
					cref.commit();

					Collection rows = model.getInstancesMarkedForDeletion();
					Iterator iter = rows.iterator();
					while (iter.hasNext()) {
						Integer row = (Integer) iter.next();
						model.markDeleted(row.intValue());
					}
					model.unmarkForDeletion();
					ResultsView rview = m_frame.getCurrentView();
					QueryResultsView view = rview.getView();
					view.getTablePanel().repaint();

					Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
					logger.fine(I18N.getLocalizedMessage("Commit"));
				}
			} catch (SQLException e) {
				showError(e);
			}
		}
	}

	/**
    */
	public class DetachFrameAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (m_frame.getDelegate() instanceof JPanelFrame) {
				MainFrame mainframe = (MainFrame) TSWorkspaceFrame.getInstance();
				mainframe.detachFrame(m_frame);
			}
		}
	}

	/**
	 * Action listener that copies the selected table rows/columns to the
	 * clipboard using the default copy settings.
	 */
	public class CopyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.getCurrentView().getView().copy();
		}
	}

	/**
	 * Action that marks all selected rows in the table for deletion. The query
	 * results must be from a single table and that table must have a primary
	 * key
	 */
	public class DeleteInstanceAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			SQLResultsModel model = m_frame.getCurrentDataModel();
			QueryResultsView view = m_frame.getCurrentView().getView();
			DbKey pk = model.getPrimaryKey();
			if (pk != null && pk.getColumnCount() > 0) {
				boolean bdelete = true;
				if (!model.supportsTransactions()) {
					bdelete = confirmCommit();
				}

				if (bdelete) {
					TSTablePanel tp = view.getTablePanel();
					int[] rowheaders = tp.getSelectedRowHeaders();
					if (rowheaders.length > 0) {
						for (int index = 0; index < rowheaders.length; index++) {
							int delete_row = rowheaders[index];
							if (!model.isDeleted(delete_row)) {
								deleteInstance(delete_row);
								model.markForDeletion(delete_row);
								if (!model.supportsTransactions()) {
									model.markDeleted(delete_row);

								}

							}
						}
						model.truncateResults();
					}

				}

				if (!model.supportsTransactions()) {
					model.unmarkForDeletion();
				}
				view.getTablePanel().repaint();
			}
		}
	}

	/**
	 * Exports the entire query results
	 */
	public class ExportAllAction implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
			TSInternalFrame exportframe = wsframe.createInternalFrame(com.jeta.abeille.gui.export.ExportFrame.class,
					false, m_frame.getConnection().getId());
			wsframe.addWindow(exportframe, false);

			Object[] params = new Object[1];
			params[0] = m_frame.getCurrentDataModel();

			exportframe.initializeModel(params);
			exportframe.setSize(exportframe.getPreferredSize());
			wsframe.centerWindow(exportframe);
			wsframe.show(exportframe);
		}
	}

	/**
	 * Exports the selection results
	 */
	public class ExportSelectionAction implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
			TSInternalFrame exportframe = wsframe.createInternalFrame(com.jeta.abeille.gui.export.ExportFrame.class,
					false, m_frame.getConnection().getId());
			wsframe.addWindow(exportframe, false);

			Object[] params = new Object[2];
			params[0] = m_frame.getCurrentDataModel();
			params[1] = m_frame.getCurrentView().getView().getSelection();

			exportframe.initializeModel(params);
			exportframe.setSize(exportframe.getPreferredSize());
			wsframe.centerWindow(exportframe);
			wsframe.show(exportframe);
		}
	}

	/**
	 * Moves to the top of the result set and scrolls the active table to make
	 * the instance visible
	 */
	public class FirstInstanceAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			SQLResultsModel model = m_frame.getCurrentDataModel();
			if (model.getRowCount() > 0) {
				TSTablePanel tablepanel = m_frame.getCurrentView().getView().getTablePanel();
				JTable table = tablepanel.getFocusTable();
				Rectangle rect = table.getCellRect(0, 0, true);
				table.scrollRectToVisible(rect);
			}
		}
	}

	/**
	 * Moves to the end of the result set and scrolls the active table to make
	 * the instance visible
	 */
	public class LastInstanceAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				SQLResultsModel model = m_frame.getCurrentDataModel();
				model.last();
				int rowcount = model.getRowCount();
				TSTablePanel tablepanel = m_frame.getCurrentView().getView().getTablePanel();
				JTable table = tablepanel.getFocusTable();
				if (rowcount > 0 && table != null) {
					Rectangle rect = table.getCellRect(rowcount - 1, 0, true);
					table.scrollRectToVisible(rect);

				}
			} catch (SQLException sqe) {
				showError(sqe);
			}
		}
	}

	/**
	 * Action that removes all splitters from the view and shows the results in
	 * a single table
	 */
	public class NoSplitAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.getCurrentView().getView().showNormal();
		}
	}

	/**
	 * Invokes the print preview for the current view
	 */
	public class PrintPreviewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			PrintPreviewDialog dlg = (PrintPreviewDialog) TSGuiToolbox.createDialog(PrintPreviewDialog.class, m_frame,
					true);
			TablePrintable tp = new TablePrintable(m_frame.getCurrentView().getView().getTablePanel());
			dlg.setPrintable(tp);
			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.showCenter();
		}
	}

	/**
	 * Pops up the query info dialog box which shows the actual SQL for the
	 * query as well as the ResultSetMetaData for the results
	 */
	public class QueryInfoAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				QueryInfoDialog dlg = (QueryInfoDialog) TSGuiToolbox.createDialog(QueryInfoDialog.class, m_frame, true);
				QueryResultsModel model = m_frame.getCurrentDataModel();
				ResultSetReference ref = model.getResultSetReference();
				dlg.initialize(m_frame.getConnection(), ref, ref.getSQL());
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCloseLink();
				dlg.setTitle(I18N.getLocalizedMessage("Metadata"));
				dlg.showCenter();
			} catch (SQLException e) {
				showError(e);
			}
		}
	}

	/**
	 * Re-runs the query
	 */
	public class RedoQueryAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				m_frame.saveFrame();
				SQLResultsModel model = m_frame.getCurrentDataModel();
				ResultsView view = m_frame.getCurrentView();
				ConnectionReference cref = model.getConnectionReference();
				ResultSetReference ref = model.getResultSetReference();
				String sql = ref.getSQL();

				TSConnection tsconn = cref.getTSConnection();
				int rtype = tsconn.getResultSetScrollType();
				int concurrency = tsconn.getResultSetConcurrency();
				Statement stmt = cref.getConnection().createStatement(rtype, concurrency);

				QueryCommand cmd = new QueryCommand(cref, stmt, sql);
				ModalCommandRunner crunner = new ModalCommandRunner(cref.getTSConnection(), m_frame, cmd);
				if (crunner.invoke() == ModalCommandRunner.COMPLETED) {
					ref = cmd.getResultSetReference();
					ref.setSQL(sql);
					ref.setUnprocessedSQL(model.getUnprocessedSQL());
					SQLResultsModel smodel = new SQLResultsModel(tsconn, ref, model.getTableId());
					view.setResults(smodel);
				}
			} catch (SQLException e) {
				showError(e);
			}
		}
	}

	/**
	 * Unmarks any rows marked for deletion
	 */
	public class RollbackAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				SQLResultsModel model = m_frame.getCurrentDataModel();
				ConnectionReference cref = model.getConnectionReference();

				cref.rollback();
				model.unmarkForDeletion();
				QueryResultsView view = m_frame.getCurrentView().getView();
				view.getTablePanel().repaint();
				Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
				logger.fine(I18N.getLocalizedMessage("Rollback"));
			} catch (SQLException e) {
				showError(e);
			}
		}
	}

	/**
	 * Action that shows the instance view for the given result set
	 */
	public class ShowInstanceAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				ResultsView view = m_frame.getCurrentView();

				Object launcher = m_frame.getLauncher();
				if (launcher instanceof InstanceFrame) {
					InstanceFrame iframe = (InstanceFrame) launcher;
					if (iframe.isVisible())
						TSWorkspaceFrame.getInstance().show(iframe);
					else
						m_frame.enableComponent(SQLResultsNames.ID_SHOW_INSTANCE, false);
				} else {

					TSInternalFrame instance_frame = null;
					WeakReference iframe_ref = view.getInstanceFrameReference();
					if (iframe_ref != null) {
						TSInternalFrame iframe = (TSInternalFrame) iframe_ref.get();
						if (iframe != null && iframe.isVisible())
							instance_frame = iframe;
					}

					if (instance_frame == null) {
						SQLResultsModel model = m_frame.getCurrentDataModel();
						TableId tableid = model.getTableId();
						QueryResultsView qrview = m_frame.getCurrentView().getView();
						TSTablePanel tablepanel = qrview.getTablePanel();
						JTable table = tablepanel.getFocusTable();
						int[] rows = table.getSelectedRows();
						int row = 0;
						if (rows != null && rows.length > 0) {
							row = rows[0];
						}

						if (tableid == null) {
							try {
								QueryInstanceViewBuilder builder = new QueryInstanceViewBuilder(model, table,
										m_frame.getConnection(), model.getResultSetReference(), row);
								TSInternalFrame iframe = ShowInstanceFrameAction.launchFrame(m_frame.getConnection(),
										builder, new InstanceFrameLauncher(m_frame));
								view.setInstanceFrameReference(new WeakReference(iframe));
							} catch (SQLException e) {
								showError(e);
							}
						} else {
							TableQueryInstanceViewBuilder builder = new TableQueryInstanceViewBuilder(model,
									m_frame.getConnection(), tableid, table, row);
							TSInternalFrame iframe = ShowInstanceFrameAction.launchFrame(m_frame.getConnection(),
									builder, new InstanceFrameLauncher(m_frame));
							view.setInstanceFrameReference(new WeakReference(iframe));
						}
					} else {
						TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
						wsframe.show(instance_frame);
					}
				}
			} catch (SQLException e) {
				showError(e);
			}
		}
	}

	/**
	 * Action that puts a horizontal split bar and divides the query table into
	 * two views (upper and lower)
	 */
	public class SplitHorizontalAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.getCurrentView().getView().splitHorizontal();
		}
	}

	/**
	 * Action that puts a vertical split bar and divides the query table into
	 * two views ( left and right )
	 */
	public class SplitVerticalAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.getCurrentView().getView().splitVertical();
		}
	}

	/**
	 * Action that invokes the SQL results preferences dailog
	 */
	public class SQLResultsPreferencesAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
			SQLResultsPreferencesView view = new SQLResultsPreferencesView(
					(TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID));
			dlg.setPrimaryPanel(view);
			dlg.setSize(dlg.getPreferredSize());
			dlg.setTitle(I18N.getLocalizedMessage("Preferences"));
			dlg.setInitialFocusComponent(dlg.getCloseButton());
			dlg.showCenter();
			if (dlg.isOk()) {
				view.save();
			}
		}
	}

	/**
	 * Configures table options for the selected table
	 */
	public class TableOptionsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.getCurrentView().getView().configureTableOptions();
		}
	}
}
