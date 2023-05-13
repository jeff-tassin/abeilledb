package com.jeta.abeille.gui.model;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.model.TSTable;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.abeille.gui.command.ModalCommandRunner;
import com.jeta.abeille.gui.command.QueryTableCommand;
import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.abeille.gui.common.TableSelectorDialog;
import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.model.export.SaveAsImageAction;
import com.jeta.abeille.gui.modeler.ModelerFactory;
import com.jeta.abeille.gui.modeler.TableEditorDialog;
import com.jeta.abeille.gui.model.options.ModelViewPreferences;

import com.jeta.abeille.gui.queryresults.QueryResultsModel;

import com.jeta.abeille.gui.sql.CompoundSQLCommand;

import com.jeta.abeille.gui.update.InstanceFrameCache;
import com.jeta.abeille.gui.update.TableInstanceViewBuilder;
import com.jeta.abeille.gui.update.ShowInstanceFrameAction;

import com.jeta.abeille.gui.utils.AdHocSQLDialog;
import com.jeta.abeille.gui.utils.ConfirmCommitPanel;
import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLCommandView;
import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.abeille.logger.DbLogger;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.documents.DocumentFrameController;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;
import com.jeta.open.rules.RuleUtils;

import com.jeta.foundation.utils.TSUtils;

/**
 * This is the controller for the frame window that contains the various model
 * views
 * 
 * @author Jeff Tassin
 */
public class ModelViewFrameController extends DocumentFrameController implements ViewGetter {
	/**
	 * The frame window we are controlling
	 */
	private ModelViewFrame m_frame;

	/**
	 * The underlying database connection
	 */
	private TSConnection m_connection;

	/**
	 * The modeler - keeps references to all unsaved tables and any other
	 * modeling information that is global for the connection
	 */
	private ModelerModel m_modeler;

	/**
	 * This listener gets events from the ModelerModel. We listen for table
	 * metadata change events so we can inform each view/model to update
	 * accordingly
	 */
	private ModelerEventHandler m_modelerlistener;

	/**
	 * ctor Note that this controller must be created after the views have been
	 * loaded in the frame
	 */
	public ModelViewFrameController(ModelViewFrame frame, TSConnection conn) {
		super(frame);
		assert (conn != null);

		m_frame = frame;
		m_connection = conn;

		assignListener(ModelViewFrame.ID_TAB_PANE, new ViewSelectedListener());
		assignAction(ModelViewNames.ID_PRINT, new com.jeta.abeille.gui.model.common.PrintAction(this));
		assignAction(ModelViewNames.ID_PRINT_PREVIEW, new com.jeta.abeille.gui.model.common.PrintPreviewAction(this));
		assignAction(ModelViewNames.ID_PAGE_SETUP, new com.jeta.abeille.gui.model.common.PageSetupAction());
		assignAction(ModelViewNames.ID_SAVE_AS_SVG, new SaveAsImageAction(this));

		assignAction(TSComponentNames.ID_CUT, new ModelViewDelegateAction(this, TSComponentNames.ID_CUT));
		assignAction(TSComponentNames.ID_COPY, new ModelViewDelegateAction(this, TSComponentNames.ID_COPY));
		assignAction(ModelViewNames.ID_COPY_JOINS, new com.jeta.abeille.gui.model.common.CopyJoinsAction(this, false));
		assignAction(ModelViewNames.ID_COPY_JOINS_QUALIFIED, new com.jeta.abeille.gui.model.common.CopyJoinsAction(
				this, true));
		assignAction(TSComponentNames.ID_PASTE, new ModelViewDelegateAction(this, TSComponentNames.ID_PASTE));
		assignAction(ModelViewNames.ID_SELECT_ALL, new ModelViewDelegateAction(this, ModelViewNames.ID_SELECT_ALL));
		assignAction(ModelViewNames.ID_EXPORT_TABLE_DATA, new ExportTableDataAction());
		assignAction(ModelViewNames.ID_PREFERENCES, new PreferencesAction());

		assignAction(ModelViewNames.ID_REMOVE_FROM_VIEW, new ModelViewController.RemoveFromViewAction(this));
		assignAction(ModelViewNames.ID_ADD_TO_VIEW, new AddToViewAction());

		assignAction(ModelViewNames.ID_MOUSE_TOOL, new SelectMouseToolAction());
		assignAction(ModelViewNames.ID_LINK_TOOL, new SelectLinkToolAction());

		assignAction(ModelViewNames.ID_NEW_VIEW, new CreateNewViewAction());
		assignAction(ModelViewNames.ID_CHANGE_VIEW_NAME, new ChangeViewNameAction());
		assignAction(ModelViewNames.ID_REMOVE_VIEW, new RemoveViewAction());

		assignAction(ModelViewNames.ID_CREATE_TABLE, new CreateTableAction());

		assignAction(ModelViewNames.ID_INCREASE_FONT, new IncreaseFontSizeAction());
		assignAction(ModelViewNames.ID_DECREASE_FONT, new DecreaseFontSizeAction());

		assignAction(ModelViewNames.ID_UPDATE_TABLE, new UpdateTableAction());
		assignAction(ModelViewNames.ID_QUERY_TABLE, new QueryTable());
		assignAction(ModelViewNames.ID_DROP_TABLE, new DropTableAction());
		assignAction(ModelViewNames.ID_COMMIT_TABLES, new CommitAction());

		assignAction(ModelViewNames.ID_IMPORT_DATA, new ImportAction());

		assignAction(ModelViewNames.ID_SHOW_PROTOTYPES, new ShowPrototypesAction(true));
		assignAction(ModelViewNames.ID_HIDE_PROTOTYPES, new ShowPrototypesAction(false));

		assignAction(ModelViewNames.ID_TABLE_PROPERTIES, new MyEditAction());

		m_frame.setUIDirector(new ModelViewFrameUIDirector(m_frame));
	}

	ModelerModel getModeler() {
		return m_frame.getModeler();
	}

	/**
	 * Invokes the table editor dialog
	 */
	void editTable(ModelView view, TableWidget oldwidget) {
		if (oldwidget != null) {
			TableMetaData tmd = oldwidget.getTableMetaData();
			TableSelectorModel tm = m_frame.getTableSelector();

			TableEditorDialog dlg = new TableEditorDialog(m_frame.getParentFrame(), (TableMetaData) tmd.clone(),
					m_connection, getModeler());

			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				// to effect new changes, let's just remove the old widget and
				// create a new one
				// the view will automatically create the links (also, we don't
				// allow user defined links
				// on non-saved tables )
				tmd = dlg.createTableMetaData();

				TableId newid = tmd.getTableId();
				TableId oldid = oldwidget.getTableId();

				// modeler will fire table_changed event
				tmd.setTableId(oldid);
				getModeler().tableChanged(tmd);

				if (!newid.equals(oldid)) {
					tmd.setTableId(newid);
					// modeler will fire table_renamed event
					getModeler().tableRenamed(newid, oldid);
				}
			}
		}
	}

	/**
	 * @return the underlying database connection for this controller
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the underlying database model for this controller
	 */
	public DbModel getModel(Catalog cat) {
		return m_connection.getModel(cat);
	}

	/**
	 * ViewGetter implementation
	 */
	public ModelView getModelView() {
		return m_frame.getCurrentView();
	}

	/**
	 * ViewGetter implementation
	 */
	public Collection getViews() {
		return m_frame.getViews();
	}

	/**
	 * @return the main workspace frame for the application
	 */
	private TSWorkspaceFrame getWorkspaceFrame() {
		return (TSWorkspaceFrame) m_frame.getParentFrame();
	}

	/**
	 * Helper method that shows a sql exception
	 * 
	 */
	public void showSQLError(Object comp, Exception e, String sql) {
		SQLErrorDialog dlg = null;

		if (comp instanceof TSInternalFrame)
			dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, (TSInternalFrame) comp, true);
		else if (comp instanceof java.awt.Component)
			dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, (java.awt.Component) comp, true);
		else
			dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, m_frame, true);

		dlg.initialize(e, sql);
		dlg.setSize(dlg.getPreferredSize());
		dlg.showCenter();
	}

	/**
	 * Adds a table to the given view
	 */
	public class AddToViewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {

				TableSelectorDialog tsdlg = new TableSelectorDialog((java.awt.Frame) null, true);
				tsdlg.setTitle(I18N.getLocalizedMessage("Add Table To View"));
				tsdlg.setModel(m_connection, m_frame.getTableSelector());

				tsdlg.setSize(tsdlg.getPreferredSize());
				tsdlg.showCenter();
				if (tsdlg.isOk()) {
					TableId id = tsdlg.createTableId(getConnection());

					if (getModeler().contains(id)) {
						ModelView view = getModelView();
						if (id != null && view != null) {
							AddTableAction.addTable(view, id);
						}
					} else {
						String title = I18N.getLocalizedMessage("Error");
						String msg = I18N.format("table_not_found_1", id);
						javax.swing.JOptionPane.showMessageDialog(null, msg, title,
								javax.swing.JOptionPane.ERROR_MESSAGE);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Command handler to change the current view name
	 */
	public class ChangeViewNameAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView view = getModelView();
			if (view != null) {
				String viewname = JOptionPane.showInputDialog(I18N.getLocalizedMessage("New view name"));
				if (viewname != null)
					m_frame.changeCurrentViewName(viewname);
			}
		}
	}

	/**
	 * Command handler to commit the selected tables to the database if they are
	 * currently not saved Note: This only works on one table for now
	 */
	public class CommitAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String msg = I18N.getLocalizedMessage("CommitTablesQuestion");
			String commitmsg = I18N.getLocalizedMessage("Commit");

			TSConnection tsconn = getConnection();

			String sql = null;
			TableMetaData tmd = null;
			TableWidget tw = null;

			ModelView view = getModelView();
			Iterator iter = view.getSelectedItems().iterator();
			try {
				if (iter.hasNext()) {
					Object obj = iter.next();
					if (obj instanceof TableWidget) {
						tw = (TableWidget) obj;
						tmd = tw.getTableMetaData();

						// convert the table name to the case supported by the
						// database
						TSDatabase db = (TSDatabase) tsconn.getImplementation(TSDatabase.COMPONENT_ID);
						tmd.setTableName(db.convertCase(tmd.getTableName()));

						ModelerFactory factory = ModelerFactory.getFactory(tsconn);
						JETARule tablerule = (JETARule) factory.getTableValidationRule();
						if (RuleUtils.checkNotify(tablerule, tsconn, tmd, getModeler()).equals(RuleResult.SUCCESS)) {
							TSTable tablesrv = (TSTable) tsconn.getImplementation(TSTable.COMPONENT_ID);
							sql = tablesrv.createTableSQL(tmd);
							sql = sql.trim();
						}
					}
				}
			} catch (SQLException e) {
				showSQLError(m_frame, e, null);
				return;
			}

			if (sql == null || tmd == null)
				return;

			// final SQLCommandView sqlview = new SQLCommandView( m_connection,
			// sql, true );
			// SQLCommandDialog dlg = SQLCommandDialog.createDialog(
			// m_connection, m_frame, true );
			// SQLCommandDialog dlg = new SQLCommandDialog( m_connection,
			// (java.awt.Frame)null, true )
			// {
			// protected boolean validateLicense()
			// {
			// return true;
			// }
			// };

			/*
			 * LicenseManager jlm = (LicenseManager)ComponentMgr.lookup(
			 * LicenseManager.COMPONENT_ID ); if ( jlm.isEvaluation() ) {
			 * dlg.getOkButton().setEnabled( false );
			 * sqlview.getEditor().setEnabled( false ); JPanel btnpanel =
			 * dlg.getBottomPanel(); JPanel panel = new JPanel( new
			 * java.awt.FlowLayout( java.awt.FlowLayout.LEFT ) ); panel.add(
			 * javax.swing.Box.createHorizontalStrut(10) ); panel.add( new
			 * JLabel(I18N.getLocalizedMessage("Editor_disabled_eval") ) );
			 * btnpanel.add( panel, BorderLayout.WEST ); }
			 */

			final String sql_cmd = sql;
			AdHocSQLDialog dlg = new AdHocSQLDialog(m_connection, TSWorkspaceFrame.getInstance(), true) {
				/**
				 * allow user to save/commit prototypes in evaluation and
				 * standard versions
				 */
				protected boolean checkLicense() {
					return false;
				}

				protected void handleInvocationException(SQLException se) {
					/**
					 * MySQL allows foreign keys under very limited conditions.
					 * If we detect this situation, then post a MySQL message
					 * describing the foreign key requirements when creating a
					 * table
					 */
					if (m_connection.getDatabase().equals(Database.MYSQL)) {
						String errmsg = se.getMessage();
						if (errmsg != null && (errmsg.indexOf("errno: 150") >= 0)
								&& sql_cmd.indexOf("CREATE TABLE") >= 0 && sql_cmd.indexOf("FOREIGN KEY") >= 0) {
							StringBuffer msg = new StringBuffer();
							msg.append(errmsg);
							msg.append('\n');
							msg.append(I18N.getLocalizedMessage("mysql_create_table_error"));
							se = new SQLException(msg.toString());
						}
					}

					try {
						m_connection.getMetaDataConnection().rollback();
					} catch (Exception e) {
						TSUtils.printException(e);
					}

					showSQLError(this, se, null);
				}
			};

			dlg.initialize(sql);
			dlg.setTitle(msg);

			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				getModeler().removePrototypeSilent(tmd.getTableId());

				tsconn.getModel(tmd.getCatalog()).resetReadOnlyConnection();

				// okay, now we get a little ugly here. We don't want to send a
				// notify to
				// all ModelerListeners to remove the prototype, because we
				// still want them
				// on the ModelViews, just change the color to Saved color.
				// However, we
				// need the prototype to be remvoed from the ModelerTree, so we
				// have to
				// couple our design slightly here

				ModelerView mv = m_frame.getModelerView();
				if (mv != null) {
					mv.tableDeleted(m_connection, tmd.getTableId());
				} else {
					System.out.println("ERROR. Unable to get modeler view");
				}

				// reload the table here because it might have foreign keys that
				// were assigned
				// by the user but the database does not support. MySQL will
				// ignore any foreign key
				// assignements, so we need to display this in the view.
				m_connection.getModel(tmd.getCatalog()).reloadTable(tmd.getTableId());
				tmd = m_connection.getTable(tmd.getTableId());
				if (tmd != null) {
					tsconn.getModel(tmd.getCatalog()).tableCreated(tmd);
				}
				tw.repaint();
			}
		}
	}

	/**
	 * Creates a new tab(view) in the frame window.
	 */
	public class CreateNewViewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String viewname = JOptionPane.showInputDialog(I18N.getLocalizedDialogLabel("Enter View Name"));
			if (viewname != null && (m_frame.getView(viewname) == null)) {
				ModelViewModel model = new ModelViewModel(viewname, getModeler(), m_connection);
				ModelView view = m_frame.createView(model);
				m_frame.selectView(m_frame.getViewCount() - 1);
			}
		}
	}

	/**
	 * Called when the user selectes create table from the menu
	 */
	public class CreateTableAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final TableEditorDialog dlg = new TableEditorDialog(m_frame.getParentFrame(), null, m_connection,
					getModeler());
			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {

				TableMetaData tmd = dlg.createTableMetaData();
				ModelView view = getModelView();
				ModelViewModel viewmodel = view.getModel();

				getModeler().addTablePrototype(tmd);

				javax.swing.JViewport viewport = ModelViewFrame.getViewport(view);
				if (viewport != null) {
					Point pt = viewport.getViewPosition();
					viewmodel.addTable(tmd.getTableId(), pt.x + 10, pt.y + 10);
				}
			}
		}
	}

	public class DecreaseFontSizeAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.changeFontSize(false);
		}
	}

	/**
	 * Called when the user selectes drop table from the menu. This removes the
	 * table from the database
	 */
	public class DropTableAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				ModelView view = getModelView();
				Iterator iter = view.getSelectedItems().iterator();
				if (iter.hasNext()) {
					Object obj = iter.next();
					if (obj instanceof TableWidget) {
						TableWidget widget = (TableWidget) obj;
						TableMetaData tmd = widget.getTableMetaData();
						if (tmd == null)
							return;

						String title = I18N.format("Drop_Table_1", tmd.getTableName());
						String msg = I18N.getLocalizedMessage("Drop Table Warn");
						String cboxtxt = I18N.getLocalizedMessage("Confirm");
						final TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
						final ConfirmCommitPanel commitpanel = new ConfirmCommitPanel(msg, cboxtxt);
						dlg.setPrimaryPanel(commitpanel);
						dlg.setSize(dlg.getPreferredSize());
						dlg.setTitle(title);
						dlg.setInitialFocusComponent(dlg.getCloseButton());
						dlg.getOkButton().setEnabled(false);

						ActionListener listener = new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								javax.swing.JCheckBox cbox = commitpanel.getCheckBox();
								if (cbox.isSelected()) {
									dlg.getOkButton().setEnabled(true);
								} else {
									dlg.getOkButton().setEnabled(false);
								}
							}
						};

						commitpanel.getCheckBox().addActionListener(listener);

						dlg.showCenter();
						if (dlg.isOk()) {
							commitpanel.getCheckBox().removeActionListener(listener);
							// the DbUtils will send the notification message
							TableId tableid = tmd.getTableId();
							DbUtils.dropTable(m_connection, tableid);
							view.getModel().removeWidget(widget);
							// tell the DbModel of the drop
							m_connection.getModel(tableid.getCatalog()).tableDropped(tableid);
							// the DbModel will fire an event which the
							// ModelerModel will pick up
						}
					}
				}
			} catch (SQLException e) {
				TSUtils.printException(e);
				showSQLError(m_frame, e, null);
			}
		}
	}

	/**
	 * Allows the user to export the data for a given table
	 */
	public class ExportTableDataAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TableMetaData tmd = m_frame.getSelectedTable();
			if (tmd != null) {
				ModalCommandRunner crunner = null;
				try {
					int count = (int) DbUtils.getTableCount(m_connection, tmd.getTableId());
					if (count == 0) {
						String title = I18N.getLocalizedMessage("Error");
						String msg = I18N.getLocalizedMessage("table_has_data_to_export");
						javax.swing.JOptionPane.showMessageDialog(null, msg, title,
								javax.swing.JOptionPane.ERROR_MESSAGE);
					} else {
						QueryTableCommand cmd = new QueryTableCommand(m_connection, tmd.getTableId());
						crunner = new ModalCommandRunner(m_connection, cmd);
						if (crunner.invoke() == ModalCommandRunner.COMPLETED) {
							ResultSetReference rref = cmd.getResultSetReference();
							TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
							TSInternalFrame exportframe = wsframe.createInternalFrame(
									com.jeta.abeille.gui.export.ExportFrame.class, false, m_connection.getId());
							wsframe.addWindow(exportframe, false);

							Object[] params = new Object[1];
							params[0] = new QueryResultsModel(m_connection, rref);
							exportframe.initializeModel(params);
							exportframe.setSize(exportframe.getPreferredSize());
							exportframe.setTitle(I18N.format("Export_1", tmd.getTableId().getTableName()));
							wsframe.centerWindow(exportframe);
							wsframe.show(exportframe);
						}
					}
				} catch (SQLException e) {
					DbLogger.log(e);
					SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox
							.createDialog(SQLErrorDialog.class, m_frame, true);
					if (crunner == null) {
						dlg.initialize(e, null, true);
					} else {
						dlg.initialize(crunner.getError(), null, true);
					}
					dlg.setSize(dlg.getPreferredSize());
					dlg.showCenter();
				}
			}
		}
	}

	/**
	 * Command handler to import data to the selected table Note: This only
	 * works on one table for now
	 */
	public class ImportAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSInternalFrame frame = (TSInternalFrame) getWorkspaceFrame().createInternalFrame(
					com.jeta.abeille.gui.importer.ImportBuilderFrame.class, false, m_connection.getId());

			Object[] params = new Object[1];
			params[0] = m_connection;
			frame.initializeModel(params);

			java.awt.Dimension d = new java.awt.Dimension(800, 600);
			frame.setSize(d);
			getWorkspaceFrame().addWindow(frame);
			getWorkspaceFrame().centerWindow(frame);
		}
	}

	public class IncreaseFontSizeAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.changeFontSize(true);
		}
	}

	public class PreferencesAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelViewPreferences prefs = new ModelViewPreferences();
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
			dlg.setTitle(I18N.getLocalizedMessage("Preferences"));
			dlg.setPrimaryPanel(prefs.getView());
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				prefs.apply();
			}
		}
	}

	/**
	 * Queries all the rows in the table and opens the SQL results frame.
	 */
	public class QueryTable implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView view = getModelView();
			Iterator iter = view.getSelectedItems().iterator();
			if (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof TableWidget) {
					TableMetaData tmd = ((TableWidget) obj).getTableMetaData();
					if (tmd != null)
						QueryTableAction.invoke(m_connection, tmd.getTableId());
				}
			}
		}
	}

	/**
	 * Removes the current tab(view) in the frame window.
	 */
	public class RemoveViewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			int tabindex = m_frame.getCurrentTabIndex();
			String msg = I18N.format("Remove_view_1", m_frame.getViewName(tabindex));
			int result = JOptionPane.showConfirmDialog(null, msg, I18N.getLocalizedMessage("Remove View"),
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				m_frame.removeView(tabindex);
			}
		}
	}

	/**
	 * Command handler to rename a selected table
	 */
	public class RenameTableAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TableMetaData tmd = m_frame.getSelectedTable();
			if (tmd != null) {
				String input = JOptionPane.showInputDialog((java.awt.Component) null,
						I18N.getLocalizedMessage("New table name"));
				if (input != null) {
					TableId oldid = tmd.getTableId();

					TSDatabase db = (TSDatabase) m_connection.getImplementation(TSDatabase.COMPONENT_ID);
					input = db.convertCase(input);

					TableId newid = (TableId) oldid.changeName(input);
					if (getModeler().isPrototype(oldid)) {
						// now, inform the modeler
						if (getModeler().contains(newid)) {
							// table id already exists
							String title = I18N.getLocalizedMessage("Error");
							String msg = I18N.format("Invalid_table_name_already_exists_1", newid.getTableName());
							javax.swing.JOptionPane.showMessageDialog(null, msg, title,
									javax.swing.JOptionPane.ERROR_MESSAGE);
						} else {
							getModeler().tableRenamed(newid, oldid);
						}
					} else {
						try {
							TSTable tst = (TSTable) m_connection.getImplementation(TSTable.COMPONENT_ID);
							tst.renameTable(newid, oldid);
							// tell the DbModel of the rename
							m_connection.getModel(oldid.getCatalog()).renameTable(newid, oldid);
							// the DbModel will fire an event which the Modeler
							// will pick up

						} catch (SQLException e) {
							showSQLError(m_frame, e, null);
						}
					}
				}
			}
		}
	}

	/**
	 * Selects the mouse tool
	 */
	public class SelectMouseToolAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Collection views = m_frame.getViews();
			Iterator iter = views.iterator();
			while (iter.hasNext()) {
				ModelView view = (ModelView) iter.next();
				view.setCursor(Cursor.getDefaultCursor());
				view.enableLinkTool(false);
			}
		}
	}

	/**
	 * Selects the link tool which allows the user to draw/delete links
	 */
	public class SelectLinkToolAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Collection views = m_frame.getViews();
			Iterator iter = views.iterator();
			while (iter.hasNext()) {
				ModelView view = (ModelView) iter.next();
				view.enableLinkTool(true);
			}
		}
	}

	/**
	 * Shows/Hides the prototoype (ModelerView) in the ModelViewFrame
	 */
	public class ShowPrototypesAction implements ActionListener {
		private boolean m_show;

		public ShowPrototypesAction(boolean bshow) {
			m_show = bshow;
		}

		public void actionPerformed(ActionEvent evt) {
			m_frame.showPrototypes(m_show);
		}
	}

	/**
	 * Runs the update frame for the selected table.
	 */
	public class UpdateTableAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView view = getModelView();
			Iterator iter = view.getSelectedItems().iterator();
			if (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof TableWidget) {
					TableWidget widget = (TableWidget) obj;
					if (widget.isSaved()) {
						TableMetaData tmd = widget.getTableMetaData();
						if (tmd != null) {
							ShowInstanceFrameAction.showFrame(m_connection, tmd.getTableId());
						}
					}
				}
			}
		}
	}

	public class MyEditAction extends ShowInstanceFrameAction {
		public void actionPerformed(ActionEvent evt) {
			TableMetaData tmd = m_frame.getSelectedTable();
			if (tmd != null) {
				EditTableAction.editTable(getModeler(), tmd.getTableId());
			}
		}
	}

	public class ViewSelectedListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			updateComponents(null);
			ModelView view = m_frame.getCurrentView();
			if (view != null) {
				ModelSideBar sidebar = m_frame.getModelerViewContainer();
				if (sidebar != null) {
					sidebar.getOverview().repaint();
				}
			}
		}
	}

}
