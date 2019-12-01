package com.jeta.abeille.gui.query;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.IOException;
import java.io.File;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.TableSelectorDialog;
import com.jeta.abeille.gui.common.DefaultTableSelectorModel;
import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.model.AddTableAction;
import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewDelegateAction;
import com.jeta.abeille.gui.model.ModelViewNames;
import com.jeta.abeille.gui.model.ModelViewController;
import com.jeta.abeille.gui.model.QueryTableAction;
import com.jeta.abeille.gui.model.TableWidget;
import com.jeta.abeille.gui.sql.SQLFrame;
import com.jeta.abeille.gui.update.InstanceFrameCache;
import com.jeta.abeille.gui.update.TableInstanceViewBuilder;

import com.jeta.foundation.documents.DocumentFrameController;
import com.jeta.foundation.documents.DocumentFrame;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;

import com.jeta.foundation.gui.filechooser.FileChooserConfig;
import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;
import com.jeta.foundation.gui.filechooser.TSFileFilter;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.interfaces.app.ObjectStore;

import com.jeta.foundation.i18n.I18N;
import com.jeta.open.rules.RuleResult;

/**
 * This is the controller for the frame window that contains a graphical view of
 * tables and joins used in a query.
 * 
 * @author Jeff Tassin
 */
public class QueryBuilderController extends DocumentFrameController {
	/** the frame window that we are controlling */
	private QueryBuilderFrame m_frame;

	/** the underlying database connection */
	private TSConnection m_connection;

	/**
	 * the last active of either the reportables or constraints view (is null
	 * initially)
	 */
	private TSPanel m_activeview;

	private QueryValidatorRule m_queryvalidatorrule = new QueryValidatorRule();

	public QueryBuilderController(QueryBuilderFrame frame, TSConnection conn) {
		super(frame);
		assert (conn != null);
		m_frame = frame;
		m_connection = conn;

		assignAction(QueryNames.ID_ADD_TO_VIEW, new AddTableAction(m_frame.getParentFrame(), m_connection) {
			protected ModelView getView() {
				return m_frame.getModelView();
			}

			protected TableSelectorModel getTableSelectorModel() {
				return new DefaultTableSelectorModel(m_connection);
			}
		});

		// actions for the main model view
		assignAction(ModelViewNames.ID_PRINT, new com.jeta.abeille.gui.model.common.PrintAction(m_frame));
		assignAction(ModelViewNames.ID_PRINT_PREVIEW, new com.jeta.abeille.gui.model.common.PrintPreviewAction(m_frame));
		assignAction(ModelViewNames.ID_PAGE_SETUP, new com.jeta.abeille.gui.model.common.PageSetupAction());
		assignAction(ModelViewNames.ID_SAVE_AS_SVG, new com.jeta.abeille.gui.model.export.SaveAsImageAction(m_frame));
		assignAction(ModelViewNames.ID_COPY_JOINS,
				new com.jeta.abeille.gui.model.common.CopyJoinsAction(m_frame, false));
		assignAction(ModelViewNames.ID_COPY_JOINS_QUALIFIED, new com.jeta.abeille.gui.model.common.CopyJoinsAction(
				m_frame, true));

		assignAction(QueryNames.ID_REMOVE_FROM_VIEW, new ModelViewController.RemoveFromViewAction(m_frame));
		assignAction(QueryNames.ID_SHOW_SQL, new ShowSQLAction());
		assignAction(QueryNames.ID_MOUSE_TOOL, new MouseToolAction());
		assignAction(QueryNames.ID_LINK_TOOL, new LinkToolAction());
		assignAction(QueryNames.ID_UPDATE_TABLE, new ShowInstanceFrameAction());
		assignAction(QueryNames.ID_QUERY_TABLE, new QueryTable());
		assignAction(QueryNames.ID_INCLUDE_TABLE, new IncludeTableAction());
		assignAction(QueryNames.ID_EXCLUDE_TABLE, new ExcludeTableAction());

		RunQueryAction rqa = new RunQueryAction(this);
		assignAction(QueryNames.ID_RUN_QUERY, rqa);
		assignAction(QueryNames.ID_RUN_QUERY_NEW_WINDOW, rqa);

		assignAction(TSComponentNames.ID_CUT, new ModelViewDelegateAction(m_frame, TSComponentNames.ID_CUT));
		assignAction(TSComponentNames.ID_COPY, new ModelViewDelegateAction(m_frame, TSComponentNames.ID_COPY));
		assignAction(TSComponentNames.ID_PASTE, new ModelViewDelegateAction(m_frame, TSComponentNames.ID_PASTE));
		assignAction(ModelViewNames.ID_SELECT_ALL, new ModelViewDelegateAction(m_frame, ModelViewNames.ID_SELECT_ALL));

		// actions delegated to the constraint and reportable view
		assignAction(QueryNames.ID_DELETE_ITEM, new DelegateAction(QueryNames.ID_DELETE_ITEM));
		assignAction(QueryNames.ID_REMOVE_CONSTRAINT, new RemoveConstraintAction());
		assignAction(QueryNames.ID_REMOVE_REPORTABLE, new RemoveReportableAction());
		assignAction(QueryNames.ID_ADD_CONSTRAINT, new AddConstraintAction());
		assignAction(QueryNames.ID_ADD_REPORTABLE, new AddReportableAction());
		assignAction(QueryNames.ID_MOVE_UP, new DelegateAction(QueryNames.ID_MOVE_UP));
		assignAction(QueryNames.ID_MOVE_DOWN, new DelegateAction(QueryNames.ID_MOVE_DOWN));
		assignAction(QueryNames.ID_OPTIONS, new QueryOptionsAction());

		QueryBuilderUIDirector uidirector = new QueryBuilderUIDirector(this);
		frame.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Helper method that checks the query model state using the query validator
	 * rule.
	 */
	RuleResult check(QueryModel queryModel) {
		return m_queryvalidatorrule.check(queryModel);
	}

	/**
	 * Excludes the widget (table) from the query generation
	 */
	private void excludeWidget(TableWidget tw) {
		QueryModel model = m_frame.getModel();
		model.excludeWidget(tw);
	}

	/**
	 * Includes the widget (table) in the query generation
	 */
	private void includeWidget(TableWidget tw) {
		QueryModel model = m_frame.getModel();
		model.includeWidget(tw);
	}

	/**
	 * @return the last active view of either the ReportablesView or the
	 *         ConstraintsView This can be null
	 */
	TSPanel getActiveView() {
		return m_activeview;
	}

	/** the underlying database connection */
	public TSConnection getConnection() {
		return m_connection;
	}

	TSController getConstraintController() {
		ConstraintView cview = m_frame.getConstraintView();
		if (cview != null)
			return (TSController) cview.getController();
		else
			return null;
	}

	TSController getReportablesController() {
		ReportablesView rview = m_frame.getReportablesView();
		if (rview != null)
			return (TSController) rview.getController();
		else
			return null;
	}

	/**
	 * @return the frame window that we are controlling
	 */
	QueryBuilderFrame getFrame() {
		return m_frame;
	}

	void initializeController() {
		/**
		 * we use the bottom toolbar in the frame for both the constraint view
		 * and reportables view. So, we need to know which view was last active
		 * when the user hits a button like delete. We delete the selected item
		 * from the last active table
		 */
		ConstraintView cview = m_frame.getConstraintView();
		JTable table = cview.getTable();
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				m_activeview = m_frame.getConstraintView();
				ReportablesView rview = m_frame.getReportablesView();
				rview.getTable().clearSelection();
			}
		});

		ReportablesView rview = m_frame.getReportablesView();
		table = rview.getTable();
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				m_activeview = m_frame.getReportablesView();
				ConstraintView cview = m_frame.getConstraintView();
				cview.getTable().clearSelection();
			}
		});
	}

	/**
	 * -------------------------- actions --------------------------------------
	 */

	/**
    */
	public class AddConstraintAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (getConstraintController() != null)
				getConstraintController().invokeAction(QueryNames.ID_ADD_CONSTRAINT);
		}
	}

	public class AddReportableAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (getReportablesController() != null)
				getReportablesController().invokeAction(QueryNames.ID_ADD_REPORTABLE);
		}
	}

	/**
	 * This action delegates an action event to either a given controller or the
	 * active view's controller ( if the given controller is null ). This is
	 * mainly to share events between the constraintview and reportables view
	 */
	public class DelegateAction implements ActionListener {
		/** the command id */
		private String m_actionname;

		/**
		 * Use this ctor to forward the event to a given controller. If this
		 * controller param is null, QueryBuilderController.activeview is used
		 * as the target
		 */
		public DelegateAction(String actionName) {
			m_actionname = actionName;
		}

		public void actionPerformed(ActionEvent evt) {
			if (m_activeview != null) {
				TSController controller = (TSController) m_activeview.getController();
				if (controller != null) {
					controller.invokeAction(m_actionname);
				}
			}
		}
	}

	/**
	 * Delete the selected item from the active view (either the ConstraintView
	 * or ReportablesView )
	 */
	public class DeleteItemAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSPanel panel = getActiveView();
			if (panel != null) {
				TSController controller = (TSController) panel.getController();
				if (controller != null)
					controller.invokeAction(QueryNames.ID_DELETE_ITEM);
			}
		}
	}

	/**
	 * Excludes any tables in the given selection that are marked as included.
	 * Excluded tables are not included in the query path generation.
	 */
	public class ExcludeTableAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView view = m_frame.getModelView();
			Iterator iter = view.getSelectedItems().iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof TableWidget) {
					TableWidget tw = (TableWidget) obj;
					excludeWidget(tw);
				}
			}
		}
	}

	/**
	 * Includes any tables in the given selection that are marked as excluded
	 */
	public class IncludeTableAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView view = m_frame.getModelView();
			Iterator iter = view.getSelectedItems().iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof TableWidget) {
					TableWidget tw = (TableWidget) obj;
					includeWidget(tw);
				}
			}
		}
	}

	/**
	 * Sets the canvas tool to the link tool. This allows the user to drag links
	 * from one table to another
	 */
	public class LinkToolAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView view = m_frame.getModelView();
			view.enableLinkTool(true);
		}
	}

	/**
	 * Sets the canvas tool to the standard mouse pointer. This allows the user
	 * to select, move, delete items from the canvas
	 */
	public class MouseToolAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView view = m_frame.getModelView();
			view.setCursor(Cursor.getDefaultCursor());
			view.enableLinkTool(false);
		}
	}

	/**
	 * Invokes Query Options dialog
	 */
	public class QueryOptionsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			QueryModel model = m_frame.getModel();
			QueryPropertiesView view = new QueryPropertiesView(model);
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
			dlg.setTitle(I18N.getLocalizedMessage("Options"));
			dlg.setPrimaryPanel(view);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				model.setQualified(view.isQualified());
				model.setDistinct(view.isDistinct());
				model.set(view.getCatalog(), view.getSchema());
				m_frame.set(view.getCatalog(), view.getSchema());
			}
		}
	}

	/**
	 * Queries all the rows in the table and opens the SQL results frame.
	 */
	public class QueryTable implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView view = m_frame.getModelView();
			TableMetaData tmd = view.getSelectedTable();
			if (tmd != null) {
				QueryTableAction.invoke(m_connection, tmd.getTableId());
			}
		}
	}

	/**
	 * Removes the selected contraint
	 */
	public class RemoveConstraintAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (getConstraintController() != null)
				getConstraintController().invokeAction(QueryNames.ID_DELETE_ITEM);
		}
	}

	/**
	 * Removes the selected reportable
	 */
	public class RemoveReportableAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (getReportablesController() != null)
				getReportablesController().invokeAction(QueryNames.ID_DELETE_ITEM);
		}
	}

	/**
	 * Action handler that shows the instance frame for the selected table
	 */
	public class ShowInstanceFrameAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView view = m_frame.getModelView();
			TableMetaData tmd = view.getSelectedTable();
			if (tmd != null) {
				TableInstanceViewBuilder builder = new TableInstanceViewBuilder(m_connection, tmd.getTableId());
				InstanceFrameCache icache = InstanceFrameCache.getInstance(m_connection);
				icache.launchFrame(m_connection, builder);
			}

		}
	}

	/**
	 * Action handler that shows the JOINS in the console
	 */
	public class ShowJoinsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.saveViewToModel();
			// create the sql and paste it into the sql window
			ModelView view = m_frame.getModelView();
			QueryModel querymodel = (QueryModel) view.getModel();
			querymodel.printJoins();
		}
	}

	/**
	 * Action handler that shows the SQL text in the SQL editor
	 */
	public class ShowSQLAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
			SQLFrame sqlframe = (SQLFrame) wsframe.getSingletonFrame(SQLFrame.class, m_connection.getId());
			if (sqlframe != null) {
				m_frame.saveViewToModel();
				// create the sql and paste it into the sql window
				ModelView view = m_frame.getModelView();
				QueryModel querymodel = (QueryModel) view.getModel();

				RuleResult result = check(querymodel);
				if (result == RuleResult.SUCCESS) {
					String sql = querymodel.getSQL();
					sqlframe.setText(sql);
					wsframe.show(sqlframe);
				} else {
					if (result.getCode() == RuleResult.FAIL_MESSAGE_ID) {
						String title = I18N.getLocalizedMessage("Error");
						JOptionPane.showMessageDialog(null, result.getMessage(), title, JOptionPane.ERROR_MESSAGE);
					} else {
						assert (false);
					}
				}
			} else {
				assert (false);
			}
		}
	}
}
