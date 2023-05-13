package com.jeta.abeille.gui.formbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

import java.util.logging.Logger;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.PreparedStatementWriter;
import com.jeta.abeille.database.utils.SQLAction;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.command.AbstractCommand;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.gui.update.FieldElement;
import com.jeta.abeille.gui.update.InstanceComponent;
import com.jeta.abeille.gui.update.InstanceController;
import com.jeta.abeille.gui.update.InstanceFrame;
import com.jeta.abeille.gui.update.InstanceFrameCache;
import com.jeta.abeille.gui.update.InstanceFrameLauncher;
import com.jeta.abeille.gui.update.InstanceMetaData;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.update.QueryInstancesCommand;
import com.jeta.abeille.gui.update.ShowInstanceFrameAction;
import com.jeta.abeille.gui.update.SingletonInstanceProxy;
import com.jeta.abeille.gui.update.TableInstanceViewBuilder;

import com.jeta.abeille.logger.DbLogger;

import com.jeta.abeille.query.Expression;
import com.jeta.abeille.query.Operator;
import com.jeta.abeille.query.QueryConstraint;
import com.jeta.abeille.query.SQLBuilder;
import com.jeta.abeille.query.SQLBuilderFactory;

import com.jeta.foundation.componentmgr.ComponentNames;

import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.editor.TSTextDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETAController;

/**
 * This controller is used to handle events from the InstanceFrame/View
 * 
 * @author Jeff Tassin
 */
public class FormInstanceController extends InstanceController {
	/**
	 * this is the model for the description of the form including links,
	 * columns, and handlers
	 */
	private FormModel m_formmodel;

	/** this is the instance view */
	private FormInstanceView m_instanceview;

	/** this is the model for the instance view */
	private FormInstanceModel m_instancemodel;

	/** the icon for displaying query locked columns */
	private static ImageIcon m_querylockicon;

	static {
		m_querylockicon = TSGuiToolbox.loadImage("query16.gif");
	}

	/**
	 * ctor
	 */
	public FormInstanceController(FormModel formModel, InstanceFrame frame, FormInstanceView view,
			FormInstanceModel model) {
		super(frame, view, model);
		m_formmodel = formModel;
		m_instanceview = view;
		m_instancemodel = model;

		assignAction(InstanceFrame.ID_RUN_QUERY, new FormRunQueryAction(true));
		assignAction(InstanceFrame.ID_ADD_ROW, new FormAddAction(true));
		assignAction(InstanceFrame.ID_MODIFY_ROW, new FormModifyAction(true));
		assignAction(InstanceFrame.ID_DELETE_ROW, new FormDeleteAction(true));

		assignAction(FormNames.ID_QUERY_LOCK, new QueryLockAction());
		assignAction(FormNames.ID_QUERY_UNLOCK, new QueryUnlockAction());

		assignAction(FormNames.ID_SHOW_QUERY_PLAN, new FormRunQueryAction(false));
		assignAction(FormNames.ID_SHOW_INSERT_PLAN, new FormAddAction(false));
		assignAction(FormNames.ID_SHOW_UPDATE_PLAN, new FormModifyAction(false));
		assignAction(FormNames.ID_SHOW_DELETE_PLAN, new FormDeleteAction(false));

		FormInstanceFrameUIDirector uidirector = new FormInstanceFrameUIDirector(getFrame(), (FormInstanceView) view);
		getFrame().setUIDirector(uidirector);
	}

	private FormAddCommand buildAddCommand() {
		TSConnection connection = m_formmodel.getConnection();
		TableId anchorid = getAnchorTable();
		TableMetaData anchortmd = connection.getTable(anchorid);

		HashMap queries = buildQueries();

		try {
			// @todo remove loop
			Iterator qiter = queries.keySet().iterator();
			while (qiter.hasNext()) {
				TableId refid = (TableId) qiter.next();
				// System.out.println( "got sub query: " + refid );
				SubQuery sq = (SubQuery) queries.get(refid);
				// System.out.println( sq.getPlan( getFormatter() ) );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		LinkModel linkmodel = m_formmodel.getLinkModel();
		CommandBuilder cmdbuilder = new CommandBuilder(FormInstanceController.this);
		// @todo we probabaly need to analze the out links as well to handle the
		// custom out links case
		Collection c = linkmodel.getInLinks(anchorid);
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			Link l = (Link) iter.next();
			String columname = l.getDestinationColumn();
			ColumnMetaData cmd = anchortmd.getColumn(columname);
			TableId refid = l.getSourceTableId();
			TableMetaData reftmd = connection.getTable(refid);
			ColumnMetaData refcmd = reftmd.getColumn(l.getSourceColumn());

			// now, we have a start table for a sub query
			// we need to determine the contraints/reportables for that sub
			// query
			// first, build the sub link model
			SubQuery subquery = (SubQuery) queries.get(refid);
			assert (subquery != null);
			if (subquery.getConstraints().size() > 0)
				cmdbuilder.setProxy(cmd, new SubQueryProxy(subquery, cmd, refcmd));
			else {
				InstanceComponent comp = m_instanceview.getInstanceComponent(cmd);
				if (comp != null && !comp.isNull()) {
					cmdbuilder.setProxy(cmd, new ComponentProxy(cmd, comp, null));
				}
			}
		}

		FormInstanceMetaData fimd = m_formmodel.getInstanceMetaData();
		for (int index = 0; index < fimd.getColumnCount(); index++) {
			ColumnMetaData cmd = fimd.getColumnMetaData(index);
			InstanceComponent comp = m_instanceview.getInstanceComponent(cmd);
			if (anchorid.equals(cmd.getParentTableId())) {
				if (cmdbuilder.getProxy(cmd) == null && (comp != null && !comp.isNull()))
					cmdbuilder.setProxy(cmd, new ComponentProxy(cmd, comp, null));

			}
		}

		cmdbuilder.setQueries(queries.values());
		return (FormAddCommand) cmdbuilder.buildCommand(SQLAction.INSERT);
	}

	/**
	 * Builds a set of sub queries that are based on the first linked tables to
	 * the anchor table. The returned map is keyed on these linked tables with
	 * the values being the subqueries.
	 * 
	 * @param isadd
	 *            this flag tells the method if we are doing an add In this
	 *            case, all constraints command which ignores constraints
	 */
	private HashMap buildQueries() {

		TSConnection conn = m_formmodel.getConnection();
		// DbModel model = m_formmodel.getConnection().getModel();
		TableId anchorid = getAnchorTable();
		TableMetaData anchortmd = conn.getTable(anchorid);

		HashMap queries = new HashMap();
		LinkModel linkmodel = m_formmodel.getLinkModel();

		DefaultLinkModel defmodel = (DefaultLinkModel) linkmodel;
		defmodel.print();

		// @todo we probabaly need to analze the out links as well to handle the
		// custom out links case
		LinkedList c = new LinkedList();
		c.addAll(linkmodel.getInLinks(anchorid));
		c.addAll(linkmodel.getOutLinks(anchorid));
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			Link l = (Link) iter.next();

			String columname = null;
			/** this is the column in the anchor table that has a link */
			ColumnMetaData anchor_cmd = null;
			TableId refid = l.getSourceTableId();

			// for each link (in or out) find the anchor column
			if (refid.equals(anchorid)) {
				refid = l.getDestinationTableId();
				columname = l.getSourceColumn();
				anchor_cmd = anchortmd.getColumn(columname);
			} else {
				columname = l.getDestinationColumn();
				anchor_cmd = anchortmd.getColumn(columname);
				refid = l.getSourceTableId();
			}

			// now, we have a start table for a sub query
			// we need to determine the contraints/reportables for that sub
			// query
			// first, build the sub link model
			SubQuery subquery = (SubQuery) queries.get(refid);
			if (subquery == null) {
				subquery = SubQueryBuilder.build(refid, linkmodel, anchorid);
				// subquery.print();
				queries.put(refid, subquery);
			}

			TableMetaData reftmd = conn.getTable(refid);
			ColumnMetaData refcmd = reftmd.getColumn(l.getSourceColumn());
			InstanceComponent comp = m_instanceview.getInstanceComponent(anchor_cmd);
			// the component can be null if the given column is not part of the
			// form
			if (comp == null) {
				subquery.addReportable(refcmd);
			} else {
				// in all cases we only process columns (from other tables) that
				// are constrained
				Operator op = m_instanceview.getOperator(anchor_cmd);
				if (op != null) {
					// the value is constrained, so we can add it
					subquery.addConstraint(new LinkProxy(anchor_cmd, refcmd, comp, op));
				}
			}

		}

		/**
		 * Now iterate over all columns on the form. If a column is not located
		 * in the anchor table, we need to add that column as a constraint to
		 * the subquery that contains that column
		 */
		FormInstanceMetaData fimd = m_formmodel.getInstanceMetaData();
		for (int index = 0; index < fimd.getColumnCount(); index++) {
			ColumnMetaData cmd = fimd.getColumnMetaData(index);
			InstanceComponent comp = m_instanceview.getInstanceComponent(cmd);

			if (!anchorid.equals(cmd.getParentTableId())) {
				// column is not part of anchor table
				// now, locate the subquery that this component must be part of
				// and
				// add it as a constraint to the query
				if (!comp.isNull()) {
					boolean badded = false;
					iter = queries.values().iterator();
					while (iter.hasNext()) {
						SubQuery subquery = (SubQuery) iter.next();
						if (subquery.contains(cmd.getParentTableId())) {

							Operator op = m_instanceview.getOperator(cmd);
							if (op != null) {
								subquery.addConstraint(new ComponentProxy(cmd, comp, op));
								badded = true;
							}
						}
					}
				}
			}
		}
		return queries;
	}

	/**
	 * Override InstanceController
	 */
	protected MouseListener createLinkListener() {
		return new LinkListener();
	}

	/**
	 * @return the table that forms the basis for the FormInstance view
	 */
	public TableId getAnchorTable() {
		return m_formmodel.getAnchorTable();
	}

	public TSConnection getConnection() {
		return m_formmodel.getConnection();
	}

	/**
	 * @return the underlying form model
	 */
	public FormModel getFormModel() {
		return m_formmodel;
	}

	/**
	 * Shows the instance frame for a referenced table. Any data that is from
	 * that table is also passed to the frame
	 */
	private void launchLinkedInstanceFrame(TableId reftable, boolean showNew) {

		TableInstanceViewBuilder builder = new TableInstanceViewBuilder(m_formmodel.getConnection(), reftable);

		TSConnection conn = m_formmodel.getConnection();
		// DbModel dbmodel = m_formmodel.getConnection().getModel();
		TableMetaData reftmd = conn.getTable(reftable);
		if (reftmd != null) {
			// since we are launching a linked foriegn key table, lets get the
			// values from this table
			// that are related to the other table
			ArrayList refcols = new ArrayList();
			ArrayList localcols = new ArrayList();
			ArrayList values = new ArrayList();

			InstanceMetaData metadata = m_instancemodel.getMetaData();
			Iterator iter = m_instancemodel.getColumns().iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				TableId parentid = cmd.getParentTableId();
				if (reftable.equals(parentid)) {
					Object value = m_instanceview.getValue(cmd);
					refcols.add(cmd);
					values.add(value);
					localcols.add(cmd);
				}
			}

			SingletonInstanceProxy iproxy = null;
			if (refcols.size() > 0) {
				iproxy = new SingletonInstanceProxy((ColumnMetaData[]) refcols.toArray(new ColumnMetaData[0]),
						values.toArray());

				builder.setInstanceProxy(iproxy);
			}

			InstanceFrameCache icache = getInstanceFrameCache();
			InstanceFrame iframe = icache.getFrame(builder.getID());
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
				InstanceFrameLauncher launcher = new InstanceFrameLauncher(getFrame(), localcols, refcols);
				iframe = icache.createFrame(m_formmodel.getConnection(), builder, launcher);
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
	 * Shows the given update/query/insert/delete plan in a text dialog
	 * 
	 * @param plan
	 *            the plan to show
	 */
	private void showPlan(String plan) {
		TSTextDialog dlg = (TSTextDialog) TSGuiToolbox.createDialog(TSTextDialog.class, m_instanceview, true);
		TSGuiToolbox.centerFrame(dlg, 0.75f, 0.5f);
		dlg.initialize(com.jeta.abeille.gui.sql.SQLKit.class);
		dlg.setText(plan);
		dlg.showCenter();
	}

	/**
	 * Adds an instance to the anchor table. This action is much more
	 * complicated than the single table case because we need to traverse paths
	 * to reference tables to get values for foreign keys.
	 */
	public class FormAddAction implements ActionListener {
		/**
		 * If true, the add command is run against the database. If false, then
		 * we simply show the add plan to the user
		 */
		private boolean m_invoke;

		public FormAddAction(boolean binvoke) {
			m_invoke = binvoke;
		}

		public void actionPerformed(ActionEvent evt) {
			FormAddCommand cmd = buildAddCommand();
			if (m_invoke) {
				try {
					String plan = cmd.getPlan(getFormatter());
					Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
					logger.fine(plan);
				} catch (Exception e) {

				}
				invokeCommand(cmd);
			} else {
				try {
					String plan = cmd.getPlan(getFormatter());
					showPlan(plan);
				} catch (SQLException se) {
					showSQLError(se, null, false);
				}
			}
		}
	}

	/**
	 * Deletes an instance in the anchor table based on the given constraints.
	 */
	public class FormDeleteAction implements ActionListener {
		/**
		 * If true, the modify command is run against the database. If false,
		 * then we simply show the modify plan to the user
		 */
		private boolean m_invoke;

		/**
		 * ctor
		 */
		public FormDeleteAction(boolean binvoke) {
			m_invoke = binvoke;
		}

		public void actionPerformed(ActionEvent evt) {
			CommandBuilder cmdbuilder = new CommandBuilder(FormInstanceController.this);
			FormDeleteCommand cmd = (FormDeleteCommand) cmdbuilder.buildCommand(SQLAction.DELETE);
			if (m_invoke) {
				try {
					String plan = cmd.getPlan(getFormatter());
					Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
					logger.fine(plan);
				} catch (Exception e) {

				}

				invokeCommand(cmd);
			} else {
				try {
					// System.out.println(
					// "----------- show delete plan -----------" );
					String plan = cmd.getPlan(getFormatter());
					showPlan(plan);
				} catch (SQLException se) {
					showSQLError(se, null, false);
				}
			}
		}
	}

	/**
	 * Listener on the foreign key label. This launches the InstanceView that
	 * corresponds to the referenced foreign key table
	 */
	public class LinkListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				javax.swing.JLabel label = (javax.swing.JLabel) e.getSource();
				FieldElement fe = m_instanceview.getFieldElement(label);
				if (fe != null) {
					m_instanceview.setQueryLock(fe.getColumnMetaData());
					m_instanceview.getPopupMenu().show(label, e.getX(), e.getY());
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				javax.swing.JLabel label = (javax.swing.JLabel) e.getSource();
				FieldElement fe = m_instanceview.getFieldElement(label);
				if (fe != null) {
					m_instanceview.setQueryLock(fe.getColumnMetaData());
					m_instanceview.getPopupMenu().show(label, e.getX(), e.getY());
				}
			}
		}

		public void mouseClicked(MouseEvent evt) {
			if (evt.getButton() == MouseEvent.BUTTON1) {
				// System.out.println( "FormInstanceController.mouseClicked" );
				// label has been clicked, now get column metadata object
				InstanceMetaData metadata = m_instancemodel.getMetaData();
				Iterator iter = m_instancemodel.getColumns().iterator();
				while (iter.hasNext()) {
					ColumnMetaData cmd = (ColumnMetaData) iter.next();
					FieldElement fe = m_instanceview.getFieldElement(cmd);
					if (fe.getLabel() == evt.getSource()) {
						TableId reftable = cmd.getParentTableId();
						launchLinkedInstanceFrame(reftable, evt.isControlDown());
					}
				}
			}
		}
	}

	/**
	 * Modifies an instance in the anchor table. This action is much more
	 * complicated than the single table case because we need to traverse paths
	 * to reference tables to get values for foreign keys.
	 */
	public class FormModifyAction implements ActionListener {
		/**
		 * If true, the modify command is run against the database. If false,
		 * then we simply show the modify plan to the user
		 */
		private boolean m_invoke;

		public FormModifyAction(boolean binvoke) {
			m_invoke = binvoke;
		}

		public void actionPerformed(ActionEvent evt) {
			CommandBuilder cmdbuilder = new CommandBuilder(FormInstanceController.this);
			FormUpdateCommand cmd = (FormUpdateCommand) cmdbuilder.buildCommand(SQLAction.UPDATE);
			Collection c = cmd.getModifiedColumns();
			if (c.size() == 0) {
				String msg = I18N.getLocalizedMessage("No anchor columns have been modified");
				String error = I18N.getLocalizedMessage("Error");
				JOptionPane.showMessageDialog(null, msg, error, JOptionPane.ERROR_MESSAGE);
			} else {
				if (m_invoke) {
					try {
						String plan = cmd.getPlan(getFormatter());
						Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
						logger.fine(plan);
					} catch (Exception e) {

					}
					invokeCommand(cmd);
				} else {
					try {
						String plan = cmd.getPlan(getFormatter());
						showPlan(plan);
					} catch (SQLException se) {
						showSQLError(se, null, false);
					}
				}
			}
		}
	}

	/**
	 * Event handler when user invokes Query Lock command. The query lock allows
	 * a user to select a sub query that will be the only query called when the
	 * user hits the query button. This allows the user to scroll/select a
	 * subset of data independently of other links
	 */
	public class QueryLockAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Iterator iter = m_instancemodel.getColumns().iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				FieldElement fe = m_instanceview.getFieldElement(cmd);
				m_instanceview.decorate(fe);
			}

			ColumnMetaData cmd = m_instanceview.getQueryLock();
			if (cmd != null) {
				// System.out.println( "Querylock.1" );
				TableId refid = cmd.getParentTableId();
				if (refid.equals(getAnchorTable())) {
					InstanceMetaData metadata = m_instancemodel.getMetaData();
					iter = m_instancemodel.getColumns().iterator();
					while (iter.hasNext()) {
						cmd = (ColumnMetaData) iter.next();
						if (refid.equals(cmd.getParentTableId())) {
							FieldElement fe = m_instanceview.getFieldElement(cmd);
							fe.setIcon(m_querylockicon);
						}
					}
				} else {
					// System.out.println( "Querylock.2  refid = " + refid );

					SubQuery selectedquery = null;
					// handle query for anchor table here
					HashMap queries = buildQueries();
					iter = queries.values().iterator();
					while (iter.hasNext()) {
						SubQuery subquery = (SubQuery) iter.next();
						// subquery.print();
						if (subquery.contains(refid)) {
							assert (selectedquery == null);
							selectedquery = subquery;
						}
					}

					if (selectedquery == null) {
						m_instanceview.setQueryLock(null);
					} else {
						// System.out.println( "Querylock.3" );

						InstanceMetaData metadata = m_instancemodel.getMetaData();
						iter = m_instancemodel.getColumns().iterator();
						while (iter.hasNext()) {
							cmd = (ColumnMetaData) iter.next();
							if (selectedquery.contains(cmd.getParentTableId())) {
								selectedquery.addReportable(cmd);
								FieldElement fe = m_instanceview.getFieldElement(cmd);
								fe.setIcon(m_querylockicon);
							}
						}
					}
				}
			}

		}
	}

	public class QueryUnlockAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			// System.out.println( "query unlock" );
			Iterator iter = m_instancemodel.getColumns().iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				FieldElement fe = m_instanceview.getFieldElement(cmd);
				m_instanceview.decorate(fe);
			}
			m_instanceview.setQueryLock(null);
		}
	}

	/**
	 * Runs a query specified by the constraints in the update form
	 */
	public class FormRunQueryAction implements ActionListener {
		private boolean m_invoke;

		public FormRunQueryAction(boolean invoke) {
			m_invoke = invoke;
		}

		public void actionPerformed(ActionEvent evt) {
			try {
				ColumnMetaData cmd = m_instanceview.getQueryLock();
				if (!m_invoke || cmd == null) {
					// then this is a query all
					// create the sql

					LinkModel linkmodel = m_formmodel.getLinkModel();
					Collection tables = m_formmodel.getTables();
					Collection constraints = m_instanceview.getConstraints(getFormatter());
					Collection reportables = m_instanceview.getReportables();

					// change all the expression values in the constraints
					// collection to ?
					Iterator citer = constraints.iterator();
					while (citer.hasNext()) {
						QueryConstraint qc = (QueryConstraint) citer.next();
						QueryConstraint.prepareConstraint(qc);
					}

					SQLBuilder builder = SQLBuilderFactory.createBuilder(m_formmodel.getConnection());
					String sql = builder.build(false, tables, linkmodel, constraints, reportables, null, null);

					ConnectionReference cref = null;
					PreparedStatement pstmt = null;

					try {
						if (m_invoke) {
							cref = m_instancemodel.getWriteConnection();
							TSConnection tsconn = cref.getTSConnection();
							int rtype = tsconn.getResultSetScrollType();
							int concurrency = tsconn.getResultSetConcurrency();
							pstmt = cref.getConnection().prepareStatement(sql, rtype, concurrency);
						}

						PreparedStatementWriter pwriter = new PreparedStatementWriter(sql);
						Collection cols = builder.getConstraintExpressions();
						SQLFormatter formatter = getFormatter();

						int count = 0;
						Iterator iter = cols.iterator();
						while (iter.hasNext()) {
							Expression expr = (Expression) iter.next();
							InstanceComponent comp = m_instanceview.getInstanceComponent(expr.getColumnMetaData());
							if (pstmt != null)
								comp.prepareStatement(count + 1, pstmt, formatter);

							comp.prepareStatement(count + 1, pwriter, formatter);
							count++;
						}

						if (m_invoke) {
							Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
							logger.fine(pwriter.getPreparedSQL());

							QueryInstancesCommand qcmd = new QueryInstancesCommand(FormInstanceController.this, pstmt,
									reportables);
							invokeCommand(qcmd);
							updateView();
							updateComponents(null);
						} else {
							showPlan(pwriter.getPreparedSQL());
						}
					} catch (SQLException sqe) {
						if (pstmt != null) {
							try {
								cref.rollback();
							} catch (Exception e) {
							}
						}
						throw sqe;
					}
				} else // then this is a subquery on the specified lock
				{

					TableId refid = cmd.getParentTableId();
					if (refid.equals(getAnchorTable())) {
						SubQuery subquery = new SubQuery();
						subquery.addTable(refid);
						InstanceMetaData metadata = m_instancemodel.getMetaData();
						Iterator citer = m_instancemodel.getColumns().iterator();
						while (citer.hasNext()) {
							ColumnMetaData ccmd = (ColumnMetaData) citer.next();
							if (refid.equals(ccmd.getParentTableId())) {
								subquery.addReportable(ccmd);
								Operator op = m_instanceview.getOperator(cmd);
								if (op != null) {
									InstanceComponent comp = m_instanceview.getInstanceComponent(ccmd);
									subquery.addConstraint(new ComponentProxy(ccmd, comp, op));
								}
							}
						}

						try {

							Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
							logger.fine(subquery.getPlan(m_formmodel.getConnection(), getFormatter()));
						} catch (Exception e) {

						}

						ConnectionReference connref = m_instancemodel.getWriteConnection();
						invokeCommand(new SubQueryCommand(connref, FormInstanceController.this, subquery));
						updateView();
						updateComponents(null);
					} else {
						// this is a query on a table other than the anchor
						// table
						HashMap queries = buildQueries();
						Iterator iter = queries.values().iterator();
						while (iter.hasNext()) {
							SubQuery subquery = (SubQuery) iter.next();
							if (subquery.contains(refid)) {
								InstanceMetaData metadata = m_instancemodel.getMetaData();
								Iterator citer = m_instancemodel.getColumns().iterator();
								while (citer.hasNext()) {
									ColumnMetaData ccmd = (ColumnMetaData) citer.next();
									if (subquery.contains(ccmd.getParentTableId())) {
										subquery.addReportable(ccmd);
									}
								}

								try {

									Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
									logger.fine(subquery.getPlan(m_formmodel.getConnection(), getFormatter()));
								} catch (Exception e) {

								}

								ConnectionReference connref = m_instancemodel.getWriteConnection();
								invokeCommand(new SubQueryCommand(connref, FormInstanceController.this, subquery));
								updateView();
								updateComponents(null);
								break;
							}
						}
					}
				}
			} catch (SQLException se) {
				showSQLError(se, null, true);
			}
		}
	}

}
