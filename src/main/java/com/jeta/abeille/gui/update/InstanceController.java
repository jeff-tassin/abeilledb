package com.jeta.abeille.gui.update;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.model.TSResultSet;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.PreparedStatementWriter;
import com.jeta.abeille.database.utils.SQLFormatter;
import com.jeta.abeille.database.utils.SQLFormatterFactory;
import com.jeta.abeille.database.utils.SQLAction;
import com.jeta.abeille.gui.command.Command;
import com.jeta.abeille.gui.command.CommandRunner;
import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.gui.sql.SQLResultsFrame;
import com.jeta.abeille.gui.utils.ConfirmCommitPanel;
import com.jeta.abeille.gui.utils.SQLErrorDialog;
import com.jeta.abeille.logger.DbLogger;
import com.jeta.abeille.query.SQLBuilder;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;
import com.jeta.foundation.i18n.I18N;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;

import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.gui.framework.JETAController;
import com.jeta.open.gui.framework.UIDirector;

/**
 * This is the controller object for the InstanceFrame and InstanceView. It
 * handles all user input for menus and toolbars and other components.
 * 
 * @author Jeff Tassin
 */
public class InstanceController extends JETAController implements ActionListener {
	// this is the frame window for the view. It contains the toolbar, menus and
	// statusbar which
	// this controller is responsible for updating
	private InstanceFrame m_frame;

	// this is the view that contains the column fields
	private InstanceView m_view;

	/**
	 * this is the model that contains the metadata as well as the query results
	 * that we are showing
	 */
	private InstanceModel m_model;

	private SQLFormatter m_formatter;

	/** if we have a sql command that is running in the background, this is it */
	private Command m_command;

	/** this cache is used for launching foreign key frames */
	// private InstanceFrameCache m_framecache;

	/** flag that indicates if we are getting the table count */
	private volatile boolean m_gettingtablecount = false;

	public InstanceController(InstanceFrame frame, InstanceView view, InstanceModel model) {
		super(frame);
		m_frame = frame;
		m_view = view;
		m_view.addActionListener(this);
		m_model = model;
		view.getBrowserPopup().setController(this);

		assignAction(InstanceFrame.ID_ADD_ROW, new AddAction());
		assignAction(InstanceFrame.ID_DELETE_ROW, new DeleteAction());
		assignAction(InstanceFrame.ID_MODIFY_ROW, new ModifyAction());
		assignAction(InstanceFrame.ID_COMMIT, new CommitAction());
		assignAction(InstanceFrame.ID_ROLLBACK, new RollbackAction());
		assignAction(InstanceFrame.ID_RUN_QUERY, new RunQueryAction());
		assignAction(InstanceFrame.ID_SHOW_TABULAR_RESULTS, new ShowTabularResults());
		assignAction(InstanceFrame.ID_CLEAR_FORM, new ClearAction());

		assignAction(InstanceFrame.ID_FIRST_ROW, new FirstAction());
		assignAction(InstanceFrame.ID_LAST_ROW, new LastAction());
		assignAction(InstanceFrame.ID_NEXT_ROW, new NextAction());
		assignAction(InstanceFrame.ID_PREV_ROW, new PrevAction());
		assignAction(InstanceFrame.ID_CONFIGURE, new ConfigureAction());
		assignAction(InstanceFrame.ID_PREFERENCES, new PreferencesAction());
		assignAction(InstanceFrame.ID_PASTE_INSTANCE, new PasteInstanceAction());

		installLinkListeners();
		refreshTableCount();
	}

	/**
	 * This is the implementation of ActionListener. Here we get events from the
	 * InstanceView (specifically constraint button events.)
	 */
	public void actionPerformed(ActionEvent evt) {
		updateComponents(null);
	}

	/**
	 * Clears the form and the result set
	 */
	public void clearForm() {
		m_model.setInstanceProxy(null);
		m_view.clear();
		m_frame.setStatus(InstanceFrame.INSTANCE_POS, "");
	}

	/**
	 * Called when a command has been completed
	 * 
	 */
	public void commandCompleted() {
		m_model.setState(InstanceModel.READY);
		if (m_command instanceof QueryInstancesCommand)
			updateView();

		m_command = null;
		updateComponents(null);

		refreshTableCount();
	}

	/**
	 * Displays a confirmation dialog that prompts the user if they wish to
	 * commit. The use may choose not to display the dialog in the future. In
	 * these cases, this method will return true.
	 */
	public boolean confirmCommit() {
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
			userprops.setProperty(InstancePreferencesView.ID_CONFIRM_COMMIT,
					String.valueOf(commitpanel.isShowCommitDialog()));
		}
		return bcommit;
	}

	/**
	 * @return the SQL formatter object
	 */
	public SQLFormatter getFormatter() {
		if (m_formatter == null) {
			SQLFormatterFactory ff = SQLFormatterFactory.getInstance(m_model.getConnection());
			m_formatter = ff.createFormatter();
		}
		return m_formatter;
	}

	/**
	 * Override InstanceController
	 */
	protected MouseListener createLinkListener() {
		return new ForeignKeyLinkListener();
	}

	/**
	 * @return the frame associated with this controller
	 */
	public InstanceFrame getFrame() {
		return m_frame;
	}

	/** this cache is used for launching foreign key frames */
	protected InstanceFrameCache getInstanceFrameCache() {
		// if ( m_framecache == null )
		// m_framecache = new InstanceFrameCache();

		// return m_framecache;
		return InstanceFrameCache.getInstance(m_model.getConnection());
	}

	/**
	 * @return the underlyin instance data object
	 */
	public InstanceProxy getInstanceProxy() {
		return m_model.getInstanceProxy();
	}

	/**
	 * @return the underlying data model
	 */
	public InstanceModel getModel() {
		return m_model;
	}

	/**
	 * @return a collection of ColumnMetaData objects that are modified in the
	 *         form
	 */
	Collection getModifiedColumns() {
		ArrayList cols = new ArrayList();
		Iterator iter = m_model.getColumns().iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			InstanceComponent comp = m_view.getInstanceComponent(cmd);
			if (comp.isModified() && !m_view.isConstrained(cmd)) {
				cols.add(cmd);
			}
		}
		return cols;
	}

	/**
	 * Installs the mouse listener used when the user clicks on a table link
	 * label
	 */
	protected void installLinkListeners() {
		InstanceMetaData imd = m_model.getMetaData();

		MouseListener listener = createLinkListener();
		Collection c = m_model.getColumns();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			FieldElement fe = m_view.getFieldElement(cmd);
			if (imd.isLink(cmd)) {
				fe.label.setCursor(new Cursor(Cursor.HAND_CURSOR));
				fe.label.addMouseListener(createLinkListener());
			}
		}
	}

	/**
	 * Runs the given command with a command runner. Also sets the model state
	 * to busy and updates the UI
	 */
	protected void invokeCommand(Command cmd) {
		m_command = cmd;
		m_model.setState(InstanceModel.BUSY);
		updateComponents(null);
		CommandRunner runner = new CommandRunner(cmd);
		runner.invoke();
	}

	/**
	 * @return true if the commit dialog should be displayed before a commit.
	 */
	public boolean isShowCommitDialog() {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		return Boolean.valueOf(userprops.getProperty(InstancePreferencesView.ID_CONFIRM_COMMIT, "true")).booleanValue();
	}

	/**
	 * Launches an InstanceFrame that is linked to this frame
	 */
	void launchLinkedInstanceFrame(BrowserLink target) {
		launchLinkedInstanceFrame(target.getSourceTableMetaData(), target.getSourceColumn(), target.getTargetTableId(),
				target.isLoadAllColumns(), target.isNewFrame());
	}

	/**
	 * Launches an InstanceFrame that is linked to this frame
	 * 
	 * @param localtmd
	 *            the table metadata of the current or local table that is the
	 *            source of the launch
	 * @param srcCol
	 *            the name of the source column
	 * @param reftable
	 *            the table metadata of the table that is the target to launch
	 * @param loadAllLinkedColumns
	 *            this flag controls whether all columns for all links between
	 *            the source and destination table are initialzed/loaded when
	 *            the destination form is launched. If set to false, only the
	 *            linked column that is clicked is loaded in the new form. If
	 *            set to true, all linked columns are loaded.
	 * @param showNew
	 *            if true, always launch a new instance frame. If false, check
	 *            if an existing frame is visible for the reftable and show that
	 *            instead of launching a new frame.
	 */
	protected void launchLinkedInstanceFrame(TableMetaData localtmd, String srcCol, TableId refid,
			boolean loadAllLinkedColumns, boolean showNew) {
		if (localtmd == null || refid == null) {
			assert (false);
			return;
		}

		InstanceMetaData imd = (InstanceMetaData) m_model.getMetaData();
		LinkModel linkmodel = imd.getLinkModel();
		TableId localid = localtmd.getTableId();

		TableInstanceViewBuilder builder = new TableInstanceViewBuilder(m_model.getConnection(), refid);
		DbModel dbmodel = m_model.getConnection().getModel(refid.getCatalog());
		TableMetaData reftmd = dbmodel.getTable(refid);

		if (reftmd != null) {
			// since we are launching a linked foriegn key table, lets get the
			// values from this table
			// that are related to the other table
			ArrayList refcols = new ArrayList();
			ArrayList localcols = new ArrayList();
			ArrayList values = new ArrayList();

			Collection links = linkmodel.getLinks(localid);
			Iterator iter = links.iterator();
			while (iter.hasNext()) {
				Link link = (Link) iter.next();
				/**
				 * we currently don't handle loading all columns for self
				 * referencing tables. Only the clicked column
				 */
				if (loadAllLinkedColumns && !refid.equals(localid)) {
					ColumnMetaData refcmd = null;
					String valueCol = srcCol;
					if (link.contains(localid, srcCol)) {
						refcmd = reftmd.getColumn(link.getLinkedColumn(localid, srcCol));
					} else if (link.contains(refid)) {
						refcmd = reftmd.getColumn(link.getColumn(refid));
						valueCol = link.getColumn(localid);
					}

					ColumnMetaData localcmd = localtmd.getColumn(valueCol);
					Object value = m_view.getValue(localcmd);
					refcols.add(refcmd);
					values.add(value);
					localcols.add(localcmd);
				} else {
					if (link.contains(localid, srcCol)) {
						ColumnMetaData refcmd = reftmd.getColumn(link.getLinkedColumn(localid, srcCol));
						if (refcmd != null) {
							ColumnMetaData localcmd = localtmd.getColumn(srcCol);
							Object value = m_view.getValue(localcmd);
							refcols.add(refcmd);
							values.add(value);
							localcols.add(localcmd);
						}
					}
				}
			}

			SingletonInstanceProxy iproxy = null;
			if (refcols.size() > 0) {
				iproxy = new SingletonInstanceProxy((ColumnMetaData[]) refcols.toArray(new ColumnMetaData[0]),
						values.toArray());

				builder.setInstanceProxy(iproxy);
			}

			/** self-referencing tables are a special case */
			boolean is_self_ref = localid.equals(reftmd.getTableId());
			String builder_id = builder.getID();
			if (is_self_ref) {
				builder_id += ".self_ref";
				if (!showNew) {
					/**
					 * check if this frame was launched by another frame. If so,
					 * show the launcher frame instead of showing a new one
					 */
					InstanceFrameLauncher launcher = m_frame.getLauncher();
					if (launcher != null) {
						Object lsrc = launcher.getSource();
						if (lsrc instanceof InstanceFrame) {
							InstanceFrame lframe = (InstanceFrame) lsrc;
							InstanceViewBuilder lbuilder = lframe.getViewBuilder();

							if (lbuilder instanceof TableInstanceViewBuilder) {
								TableInstanceViewBuilder tbuilder = (TableInstanceViewBuilder) lbuilder;
								TableId lid = tbuilder.getTableId();
								if (localid.equals(lid)) {
									builder_id = builder.getID();
								}
							}
						}
					}
				}
			}
			InstanceFrameCache icache = getInstanceFrameCache();
			InstanceFrame iframe = icache.getFrame(builder_id);
			if (!showNew && iframe != null && iframe.isVisible()) {
				InstanceView iview = iframe.getView();
				if (iproxy != null) {
					iview.getModel().setInstanceProxy(iproxy);
					try {
						iview.updateView();
					} catch (Exception e) {
						// ignore here
					}
				}
				TSWorkspaceFrame.getInstance().show(iframe);
			} else {
				InstanceFrameLauncher launcher = new InstanceFrameLauncher(m_frame, localcols, refcols);
				iframe = icache.createFrame(m_model.getConnection(), builder, builder_id, launcher);
			}

			if (iframe != null) {
				InstanceView iview = iframe.getView();
				iview.clearConstraints();
				iter = refcols.iterator();
				while (iter.hasNext()) {
					iview.setConstraint((ColumnMetaData) iter.next(), InstanceView.EQUAL);
				}

				JETAController controller = iframe.getController();
				if (controller != null)
					controller.invokeAction(InstanceFrame.ID_RUN_QUERY);
			}
		}

	}

	/**
	 * Method checks if the current database is PostgreSQL, If so, then
	 * determines if the OID should be displayed for the given table.
	 * 
	 * @param tableid
	 *            the table id to check for OID
	 * @return true of the OID is visible for the given table
	 */
	private boolean isPostgresOID(TableId tableid) {
		boolean bresult = false;
		TSConnection connection = m_model.getConnection();
		if (connection.getDatabase() == Database.POSTGRESQL) {
			com.jeta.abeille.database.postgres.PostgresObjectStore postgresos = com.jeta.abeille.database.postgres.PostgresObjectStore
					.getInstance(connection);

			if (postgresos != null && postgresos.isShowOID(tableid))
				bresult = true;
		}
		return bresult;
	}

	/**
	 * Prepares a sql statement for running an update, select, insert or delete.
	 * 
	 * @param ue
	 *            the action to peform ( select, update, insert, delete )
	 * @param resultsql
	 *            a string buffer that this method places the sql command into.
	 *            This param can be null
	 */
	private PreparedStatement prepareStatement(SQLAction ue, StringBuffer resultsql) throws SQLException {
		// select * from tablename where x = ? and y = ? ...
		// update tablename SET col1 = ?, col2 = ? where col3 = ? and col4 = ?
		// insert into tablename (?,?,?) values( ?,?,?)
		// delete from tablename where col1 = ? and col2 = ?

		// if we are here, then we are using a TableInstanceModel
		TableInstanceMetaData timd = (TableInstanceMetaData) m_model.getMetaData();
		TSDatabase database = (TSDatabase) m_model.getConnection().getImplementation(TSDatabase.COMPONENT_ID);

		TableId tableid = timd.getTableId();

		ArrayList updateditems = new ArrayList();
		StringBuffer sqlbuff = new StringBuffer();

		if (ue == SQLAction.SELECT) {
			StringBuffer values = new StringBuffer();
			if (isPostgresOID(tableid)) {
				sqlbuff.append("select oid, * from ");
			} else {
				sqlbuff.append("select * from ");
			}
			sqlbuff.append( database.getFullyQualifiedName(tableid) );
			prepareWhereClause(sqlbuff, updateditems);
		} else if (ue == SQLAction.UPDATE) {
			sqlbuff.append("update ");
			sqlbuff.append( database.getFullyQualifiedName(tableid) );
			sqlbuff.append(" set ");
			Collection cols = getModifiedColumns();
			Iterator iter = cols.iterator();
			boolean bfirst = true;
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				if (bfirst)
					bfirst = false;
				else
					sqlbuff.append(", ");

				sqlbuff.append(cmd.getColumnName());
				sqlbuff.append(" = ?"); // we are using prepared statements
				InstanceComponent comp = m_view.getInstanceComponent(cmd);

				updateditems.add(comp);
			}
			prepareWhereClause(sqlbuff, updateditems);
		} else if (ue == SQLAction.INSERT) {
			StringBuffer values = new StringBuffer();
			sqlbuff.append("insert into ");
			sqlbuff.append( database.getFullyQualifiedName(tableid) );
			sqlbuff.append(" (");
			values.append(" values (");
			Iterator iter = m_model.getColumns().iterator();
			boolean bfirst = true;
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				InstanceComponent comp = m_view.getInstanceComponent(cmd);
				comp.syncValue();

				if (!comp.isNull() && !(comp instanceof InstanceUnknownComponent)) {
					if (bfirst)
						bfirst = false;
					else {
						sqlbuff.append(", ");
						values.append(", ");
					}

					sqlbuff.append(cmd.getColumnName());
					values.append(" ?"); // we are using prepared statements
					updateditems.add(comp);
				}
			}

			sqlbuff.append(" )");
			values.append(")");
			sqlbuff.append(values);
		} else if (ue == SQLAction.DELETE) {
			sqlbuff.append("delete from ");
			sqlbuff.append(tableid.getFullyQualifiedName());
			prepareWhereClause(sqlbuff, updateditems);
		} else {
			assert (false);
		}

		String sql = sqlbuff.toString();

		SQLFormatter formatter = getFormatter();
		ConnectionReference cref = m_model.getWriteConnection();

		TSConnection tsconn = cref.getTSConnection();
		int rtype = tsconn.getResultSetScrollType();
		int concurrency = tsconn.getResultSetConcurrency();
		PreparedStatement pstmt = null;

		if (ue == SQLAction.SELECT) {
			pstmt = cref.getConnection().prepareStatement(sql, rtype, concurrency);
			try {
				pstmt.setMaxRows(TSUserPropertiesUtils.getInteger(TSConnection.ID_MAX_QUERY_ROWS, 500));
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		} else {
			pstmt = cref.getConnection().prepareStatement(sql);
		}

		PreparedStatementWriter pwriter = new PreparedStatementWriter(sql);
		for (int count = 0; count < updateditems.size(); count++) {
			InstanceComponent comp = (InstanceComponent) updateditems.get(count);
			comp.prepareStatement(count + 1, pstmt, formatter);
			try {
				comp.prepareStatement(count + 1, pwriter, formatter);
			} catch (Exception e) {

			}
		}

		Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
		logger.fine(pwriter.getPreparedSQL());

		if (resultsql != null)
			resultsql.append(sqlbuff);

		return pstmt;
	}

	/**
	 * Creates the where clause of a prepared sql statement.
	 * 
	 * @param sqlbuff
	 *            a string buffer that this method appends the sql to
	 * @param updateditems
	 *            a collection of ColumnMetaData objects that this method will
	 *            append any columns that are part of the where clause
	 * 
	 */
	private void prepareWhereClause(StringBuffer sqlbuff, ArrayList updateditems) {
		Collection cc = m_view.getConstraintColumns();
		if (cc.size() > 0) {
			sqlbuff.append(" where ");
			Iterator iter = cc.iterator();
			boolean bfirst = true;
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				InstanceComponent comp = m_view.getInstanceComponent(cmd);

				if (bfirst)
					bfirst = false;
				else {
					sqlbuff.append(" and ");
				}
				sqlbuff.append(cmd.getColumnName());
				sqlbuff.append(" ");

				String constraint = m_view.getConstraint(cmd);

				if (comp.isNull()) {
					if (InstanceView.EQUAL.equals(constraint))
						sqlbuff.append("is NULL");
					else if (InstanceView.NOTEQUAL.equals(constraint))
						sqlbuff.append("is not NULL");

					// else
					// sqlbuff.append( constraint );
				} else {
					sqlbuff.append(m_view.getConstraint(cmd));
					sqlbuff.append(" ?"); // we are using prepared statements
					updateditems.add(comp);
				}
			}
		}
	}

	/**
	 * This method updates the table count cell in the InstanceFrame status bar.
	 * It works in a background thread
	 */
	public void refreshTableCount() {
		if (!m_gettingtablecount) {
			m_gettingtablecount = true;
			InstanceMetaData md = m_model.getMetaData();
			if (md instanceof TableInstanceMetaData) {
				TableInstanceMetaData timd = (TableInstanceMetaData) md;
				new BackgroundThread(m_model, timd.getTableId()).start();
			}
		}
	}

	/**
	 * Runs a query on the current table using the selected constraints
	 */
	private void runQuery() {
		try {
			// if we are here, then we are using a TableInstanceModel
			TableInstanceMetaData timd = (TableInstanceMetaData) m_model.getMetaData();

			StringBuffer sqlbuff = new StringBuffer();
			PreparedStatement pstmt = prepareStatement(SQLAction.SELECT, sqlbuff);
			invokeCommand(new QueryInstancesCommand(this, pstmt, timd.getTableId(), sqlbuff.toString()));
		} catch (SQLException se) {
			showSQLError(se, null, true);
		}
	}

	/**
	 * Helper method that shows a sql exception
	 * 
	 */
	public void showSQLError(Exception e, String sql, boolean rollback) {
		Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
		logger.fine(SQLErrorDialog.format(e, sql));

		SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, m_frame, true);
		dlg.initialize(e, sql, rollback);
		dlg.setSize(dlg.getPreferredSize());
		dlg.showCenter();
	}

	/**
	 * Stops a running command
	 */
	void stopCommand() {
		if (m_command != null) {
			try {
				m_command.cancel();
				m_command = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Update all buttons and menus for the InstanceFrame. This method looks at
	 * the current window state and determines which buttons/menus to
	 * enable/disable.
	 */
	public void updateComponents(Object src) {
		super.updateComponents(src);
		try {
			InstanceProxy iproxy = getInstanceProxy();
			if (iproxy == null)
				m_frame.updateRowStatus(-1, -1, -1);
			else {
				// System.out.println(
				// "InstanceController.updateComponents    rowCount = " +
				// iproxy.getRowCount() );
				m_frame.updateRowStatus(iproxy.getRow() + 1, iproxy.getRowCount(), iproxy.getMaxRow() + 1);
			}

		} catch (Exception e) {
			e.printStackTrace();
			m_frame.updateRowStatus(-1, -1, -1);
		}
		UIDirector director = getView().getUIDirector();
		director.updateComponents(null);
	}

	/**
	 * Gets the current instance from the result set and loads it into the
	 * model. Then inform the view to update. Update the user interface as well
	 * (toolbar and menus )
	 */
	public void updateView() {
		try {
			updateComponents(null);
			m_view.updateView();
		} catch (SQLException sqle) {
			showSQLError(sqle, null, false);
		}
	}

	/**
	 * This thread gets the table count in the background. We periodically do
	 * this to update the status bar in the frame window
	 */
	class BackgroundThread extends Thread {
		private TableId m_tableid;
		private InstanceModel m_model;
		private ConnectionReference m_cref;

		BackgroundThread(InstanceModel model, TableId tableId) {
			m_model = model;
			m_tableid = tableId;
		}

		public void run() {
			try {
				m_cref = m_model.getWriteConnection();
				long count = DbUtils.getTableCount(m_cref.getTSConnection(), m_tableid);
				m_frame.updateTableCount(count);
				m_gettingtablecount = false;
			} catch (Exception e) {
				try {
					m_cref.rollback();
				} catch (Exception rbe) {
					// eat this one
				}

				StringBuffer msg = new StringBuffer();
				msg.append(I18N.getLocalizedMessage("Get table count"));
				msg.append("\n");
				msg.append(e.getMessage());
				DbLogger.fine(msg.toString());
			}
		}
	}

	/**
	 * Adds the values in the view to the database insert into TEST (ENTRY_ID,
	 * TIMESTAMP, ENTRY_TEXT ) values (7, '2001-10-11-10.01.00.123456', 'test'
	 * );
	 */
	public class AddAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			boolean all_nulls = true;
			Iterator iter = m_model.getColumns().iterator();
			boolean bfirst = true;
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				InstanceComponent comp = m_view.getInstanceComponent(cmd);
				comp.syncValue();
				if (!comp.isNull()) {
					all_nulls = false;
					break;
				}
			}

			if (all_nulls) {
				String msg = I18N.getLocalizedMessage("Nothing_to_add_All_columns_are_null");
				String error = I18N.getLocalizedMessage("Error");
				JOptionPane.showMessageDialog(null, msg, error, JOptionPane.ERROR_MESSAGE);
				return;
			}

			/** only show internal frame if we pass license check */
			LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
			if (jlm.isEvaluation()) {
				jlm.postMessage(m_frame, I18N.getLocalizedMessage("Feature disabled in evaluation version"));
				return;
			}

			try {
				boolean update = true;
				if (!m_model.supportsTransactions()) {
					update = confirmCommit();
				}

				if (update) {
					// if we are here, then we are using a TableInstanceModel
					TableInstanceMetaData timd = (TableInstanceMetaData) m_model.getMetaData();
					PreparedStatement pstmt = prepareStatement(SQLAction.INSERT, null);
					invokeCommand(new UpdateCommand(InstanceController.this, pstmt, SQLAction.INSERT));
				}
			} catch (SQLException se) {
				showSQLError(se, null, true);
			}
		}
	}

	/**
	 * Clears the view of all values in the controls
	 */
	public class ClearAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			clearForm();
		}
	}

	/**
	 * Commits any changes to the database
	 */
	public class CommitAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (confirmCommit()) {
				Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
				logger.fine("commit;");
				invokeCommand(new CommitRollbackCommand(InstanceController.this, CommitRollbackCommand.COMMIT));
				m_model.setInstanceProxy(null);
			}
		}
	}

	/**
	 * Invokes the configure dialog which allows the user to configure options
	 * for displaying the InstanceView for this particular table.
	 */
	public class ConfigureAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.getViewBuilder().configure();
			installLinkListeners();
			updateView();
		}
	}

	/**
	 * Deletes the specified instances from the table Constraints are included.
	 * At least one constraint must be selected, or we should not be in this
	 * method (the UI should disable the modify button if no constraints are
	 * selected ).
	 */
	public class DeleteAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
			if (jlm.isEvaluation()) {
				jlm.postMessage(m_frame, I18N.getLocalizedMessage("Feature disabled in free edition"));
				return;
			}

			try {
				boolean update = true;
				if (!m_model.supportsTransactions()) {
					update = confirmCommit();
				}

				if (update) {
					// if we are here, then we are using a TableInstanceModel
					TableInstanceMetaData timd = (TableInstanceMetaData) m_model.getMetaData();
					PreparedStatement pstmt = prepareStatement(SQLAction.DELETE, null);
					invokeCommand(new UpdateCommand(InstanceController.this, pstmt, SQLAction.DELETE));
				}
			} catch (SQLException se) {
				showSQLError(se, null, true);
			}
		}
	}

	/**
	 * Moves the cursor for the result set to the first row
	 */
	public class FirstAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				getInstanceProxy().first();
				updateView();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Listener on the foreign key label. This launches the InstanceView that
	 * corresponds to the referenced foreign key table
	 */
	public class ForeignKeyLinkListener extends MouseAdapter {
		/**
		 * If the user left clicks on the label that is a foreign key, then we
		 * launch the view for the table refererenced by that table
		 */
		public void mouseClicked(MouseEvent evt) {
			if (evt.getButton() == MouseEvent.BUTTON1) {
				// label has been clicked, now get column metadata object
				InstanceMetaData imd = (InstanceMetaData) m_model.getMetaData();
				LinkModel linkmodel = imd.getLinkModel();
				if (linkmodel == null) {
					assert (false);
					return;
				}
				// TableMetaData tmd = timd.getTableMetaData();

				TreeSet launch_targets = new TreeSet();
				Iterator iter = m_model.getColumns().iterator();
				while (iter.hasNext()) {
					ColumnMetaData cmd = (ColumnMetaData) iter.next();
					String srccol = cmd.getColumnName();

					FieldElement fe = m_view.getFieldElement(cmd);
					if (fe.label == evt.getSource()) {
						// System.out.println(
						// "link for clicked table.... cmd: " + cmd );
						// ((com.jeta.abeille.database.model.DefaultLinkModel)linkmodel).print();
						// System.out.println( "-------------" );

						TableId tableid = cmd.getParentTableId();
						Collection links = linkmodel.getLinks(tableid);
						Iterator liter = links.iterator();
						// System.out.println( "link mode links.size: " +
						// links.size() );
						while (liter.hasNext()) {
							Link link = (Link) liter.next();
							// link.print();
							// System.out.println();

							// if ( cmd.getColumnName().equals(
							// link.getDestinationColumn() ) )
							if (link.contains(tableid, cmd.getColumnName())) {
								TableId linktid = link.getLinkedTable(tableid);
								TableMetaData localtmd = m_model.getConnection().getTable(tableid);
								launch_targets.add(new BrowserLink(localtmd, srccol, linktid, link.getLinkedColumn(
										tableid, srccol), evt.isShiftDown(), evt.isControlDown()));
							}
						}
					}
				}

				if (launch_targets.size() == 1) {
					launchLinkedInstanceFrame((BrowserLink) launch_targets.first());
				} else if (launch_targets.size() > 1) {
					java.awt.Point pt = javax.swing.SwingUtilities.convertPoint((java.awt.Component) evt.getSource(),
							evt.getPoint(), m_view);
					m_view.showBrowserLinkPopup(launch_targets, (int) pt.getX(), (int) pt.getY());
				}
			}
		}
	}

	/**
	 * Moves the cursor for the result set to the last row
	 */
	public class LastAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				getInstanceProxy().last();
				updateView();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Modifies the specified instances. This method queries each component for
	 * its modified flag. If the component has been modified by the user, it is
	 * included in the UPDATE sql statement. If no component has been modified,
	 * this command does nothing. Constraints are included, therefore, more than
	 * one instance can be potentially modified. At least one constraint must be
	 * selected by the user, or we should not be in this method (the UI should
	 * disable the modify button if no constraints are selected ).
	 */
	public class ModifyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
				if (jlm.isEvaluation()) {
					jlm.postMessage(m_frame, I18N.getLocalizedMessage("Feature disabled in free edition"));
					return;
				}

				boolean update = true;
				// if we are here, then we are using a TableInstanceModel
				Collection c = getModifiedColumns();
				if (c.size() == 0) {
					String msg = I18N.getLocalizedMessage("No unconstrained columns have been modified");
					String error = I18N.getLocalizedMessage("Error");
					JOptionPane.showMessageDialog(null, msg, error, JOptionPane.ERROR_MESSAGE);
					update = false;
				} else {
					if (!m_model.supportsTransactions()) {
						update = confirmCommit();
					}
				}

				if (update) {
					TableInstanceMetaData timd = (TableInstanceMetaData) m_model.getMetaData();
					PreparedStatement pstmt = prepareStatement(SQLAction.UPDATE, null);
					invokeCommand(new UpdateCommand(InstanceController.this, pstmt, SQLAction.UPDATE));
				}
			} catch (SQLException se) {
				showSQLError(se, null, true);
			}
		}
	}

	/**
	 * Move the current row to the next row in the result set
	 */
	public class NextAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				if (getInstanceProxy() != null) {
					getInstanceProxy().next();
					updateView();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * If this instance frame was launched by another frame via a foreign key,
	 * then we allow the user to paste the primary key contents to launched
	 * frame.
	 */
	public class PasteInstanceAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			InstanceFrameLauncher launcher = m_frame.getLauncher();
			if (launcher != null) {
				Object src = launcher.getSource();
				if (src instanceof InstanceFrame) {
					InstanceFrame iframe = (InstanceFrame) src;
					InstanceView pasteview = iframe.getView();
					Collection srccols = launcher.getSourceColumns();
					Iterator iter = srccols.iterator();
					while (iter.hasNext()) {
						ColumnMetaData srccmd = (ColumnMetaData) iter.next();
						ColumnMetaData relcmd = launcher.getMappedColumn(srccmd);
						if (relcmd != null) {
							pasteview.setValue(srccmd, m_view.getValue(relcmd));
						}
					}

					TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
					wsframe.show(iframe);
				}
			}
		}
	}

	/**
	 * Move the current row to the previous row in the result set
	 */
	public class PrevAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				if (getInstanceProxy() != null)
					getInstanceProxy().previous();
				updateView();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Commits any changes to the database
	 */
	public class RollbackAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
			logger.fine("rollback;");
			invokeCommand(new CommitRollbackCommand(InstanceController.this, CommitRollbackCommand.ROLLBACK));
			m_model.setInstanceProxy(null);
		}
	}

	/**
	 * Runs a query specified by the constraints in the update form
	 */
	public class RunQueryAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_view.clearUnconstrained();
			runQuery();
		}
	}

	/**
	 * Action that invokes the preferences dailog
	 */
	public class PreferencesAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
			InstancePreferencesView view = new InstancePreferencesView(
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
	 * Shows the query results in a tabular form (using SQLResults frame)
	 */
	public class ShowTabularResults implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			InstanceFrameLauncher launcher = m_frame.getLauncher();
			if (launcher != null) {
				Class lclass = launcher.getSourceClass();

				if (SQLResultsFrame.class.isAssignableFrom(lclass)) {
					SQLResultsFrame sqlframe = (SQLResultsFrame) launcher.getSource();
					if (sqlframe != null && sqlframe.isVisible()) {
						TSWorkspaceFrame.getInstance().show(sqlframe);
						return;
					} else
						m_frame.enableComponent(InstanceFrame.ID_SHOW_TABULAR_RESULTS, false);
				}
			}

			InstanceProxy proxy = m_model.getInstanceProxy();
			if (proxy instanceof AbstractInstanceProxy) {
				AbstractInstanceProxy iproxy = (AbstractInstanceProxy) proxy;
				TSInternalFrame frame = TSWorkspaceFrame.getInstance().createInternalFrame(
						com.jeta.abeille.gui.sql.SQLResultsFrame.class, false, m_model.getConnection().getId());
				frame.setSize(frame.getPreferredSize());
				TSWorkspaceFrame.getInstance().addWindow(frame, false);

				Object[] params = new Object[3];
				params[0] = m_model.getConnection();
				boolean scrollable = true;
				try {
					scrollable = iproxy.isScrollable();
				} catch (Exception e) {
					TSUtils.printException(e);
				}

				if (scrollable) {
					params[1] = iproxy.getQueryResults().getResultSetReference();
				} else {
					params[1] = iproxy.getQueryResults();
				}
				params[2] = m_frame;

				frame.initializeModel(params);
				TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
				wsframe.centerWindow(frame);
				wsframe.show(frame);
			}
		}
	}

	/**
	 * Stops a running command and resets the connection
	 */
	public class StopAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			stopCommand();
		}
	}

}
