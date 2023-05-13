package com.jeta.abeille.gui.sql;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.abeille.gui.queryresults.QueryResultsView;
import com.jeta.abeille.gui.queryresults.QueryResultsModel;
import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.gui.queryresults.ResultsManager;

import com.jeta.abeille.gui.update.InstanceFrame;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.gui.table.TableSettings;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.TSTablePanel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the Frame window for the sql results. It contains the JTables that
 * display the results of a query
 * 
 * @author Jeff Tassin
 */
public class SQLResultsFrame extends TSInternalFrame {

	/** the underlying connection manager */
	private TSConnection m_tsconnection;

	/**
	 * A weakreference to the window that launched this frame. If this results
	 * frame was launched from an instanceframe, then this member will be
	 * non-null
	 */
	private Object m_launcher;

	/** the frame icon for this frame */
	private static ImageIcon m_frameicon;

	/**
	 * The main container that holds the query views/tab pane
	 */
	private TSPanel m_view_container;

	static {
		m_frameicon = TSGuiToolbox.loadImage("incors/16x16/table_sql_view.png");
	}

	/** ctor */
	public SQLResultsFrame() {
		super(I18N.getLocalizedMessage("SQL Results"));
		setShortTitle(I18N.getLocalizedMessage("SQL Results"));

		initialize();
		setFrameIcon(m_frameicon);
	}

	/**
	 * Creates and initizes the menu for this frame
	 */
	protected void createMenu() {
		MenuTemplate template = this.getMenuTemplate();
		MenuDefinition menu = new MenuDefinition(I18N.getLocalizedMessage("Edit"));

		menu.add(i18n_createMenuItem("Copy", TSComponentNames.ID_COPY, null));
		menu.add(i18n_createMenuItem("Delete Row", SQLResultsNames.ID_DELETE_INSTANCE, null));
		menu.add(i18n_createMenuItem("Commit", SQLResultsNames.ID_COMMIT, null));
		menu.add(i18n_createMenuItem("Rollback", SQLResultsNames.ID_ROLLBACK, null));
		menu.addSeparator();
		menu.add(i18n_createMenuItem("Row View", SQLResultsNames.ID_SHOW_INSTANCE, null));
		menu.add(i18n_createMenuItem("First", SQLResultsNames.ID_FIRST, null));
		menu.add(i18n_createMenuItem("Last", SQLResultsNames.ID_LAST, null));
		menu.addSeparator();
		menu.add(i18n_createMenuItem("Preferences", SQLResultsNames.ID_PREFERENCES, null));

		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Table"));
		menu.add(i18n_createMenuItem("No Split", SQLResultsNames.ID_NO_SPLIT, null));
		menu.add(i18n_createMenuItem("Split Vertical", SQLResultsNames.ID_SPLIT_VERTICAL, null));
		menu.add(i18n_createMenuItem("Split Horizontal", SQLResultsNames.ID_SPLIT_HORIZONTAL, null));
		menu.add(i18n_createMenuItem("Table Options", SQLResultsNames.ID_TABLE_OPTIONS, null));

		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Query"));
		menu.add(i18n_createMenuItem("Export All", SQLResultsNames.ID_EXPORT_ALL, null));
		menu.add(i18n_createMenuItem("Export Selection", SQLResultsNames.ID_EXPORT_SELECTION, null));
		menu.add(i18n_createMenuItem("Query Information", SQLResultsNames.ID_QUERY_INFO, null));
		template.add(menu);
	}

	/**
	 * Creates the toolbar for this frame
	 */
	protected void createToolBar() {
		TSToolBarTemplate template = this.getToolBarTemplate();
		template.add(i18n_createToolBarButton(TSComponentNames.ID_COPY, "incors/16x16/copy.png", "Copy"));
		template.addSeparator();

		template.add(i18n_createToolBarButton(SQLResultsNames.ID_SHOW_INSTANCE, "incors/16x16/form_blue.png",
				"Row View"));

		template.addSeparator();
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_DELETE_INSTANCE, "incors/16x16/row_delete.png",
				"Delete Row"));
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_COMMIT, "incors/16x16/data_into.png", "Commit"));
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_ROLLBACK, "incors/16x16/undo.png", "Rollback"));

		template.addSeparator();
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_NO_SPLIT, "incors/16x16/table.png", "No Split"));
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_SPLIT_VERTICAL, "incors/16x16/column.png",
				"Split Vertical"));
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_SPLIT_HORIZONTAL, "incors/16x16/row.png",
				"Split Horizontal"));
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_TABLE_OPTIONS, "incors/16x16/preferences.png",
				"Table Options"));
		template.addSeparator();
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_FIRST, "incors/16x16/media_step_back.png", "First"));
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_LAST, "incors/16x16/media_step_forward.png", "Last"));
		template.addSeparator();
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_REDO_QUERY, "incors/16x16/redo.png", "Redo Query"));

		template.addSeparator();
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_QUERY_INFO, "incors/16x16/information.png",
				"Query Information"));

		template.add(javax.swing.Box.createHorizontalStrut(16));
		template.add(i18n_createToolBarButton(SQLResultsNames.ID_SHOW_IN_FRAME_WINDOW, "incors/16x16/windows.png",
				"Detach Window"));

		/*
		 * LicenseManager jlm = (LicenseManager)ComponentMgr.lookup(
		 * LicenseManager.COMPONENT_ID ); if ( jlm.isEvaluation() ) {
		 * template.add( Box.createHorizontalStrut(30) ); template.add( new
		 * JLabel( I18N.getLocalizedMessage( "Evaluation_query_results_limited"
		 * ) ) ); }
		 */

	}

	/**
	 * Override dispose so we can store the table settings for the given query.
	 * We do this so that the next time the user runs the same query, we can
	 * configure the view as it was the last time the user used it.
	 */
	public void dispose() {
		saveFrame();

		Collection views = getViews();
		Iterator iter = views.iterator();
		while (iter.hasNext()) {
			ResultsView view = (ResultsView) iter.next();
			view.dispose();
		}

		getContentPane().removeAll();
		super.dispose();

		m_tsconnection = null;
		m_launcher = null;
		setController(null);
	}

	/**
	 * @return the connection manager
	 */
	public TSConnection getConnection() {
		return m_tsconnection;
	}

	/**
	 * @return the underlying data model that transforms the result set to a
	 *         table model
	 * @deprecated
	 */
	public SQLResultsModel getCurrentDataModel() {
		ResultsView view = getCurrentView();
		if (view != null) {
			return view.getModel();
		} else {
			return null;
		}
	}

	/**
	 * @return the object that launched this frame ( can be null).
	 */
	public Object getLauncher() {
		return m_launcher;
	}

	/**
	 * @return the preferred size for this frame
	 */
	public Dimension getPreferredSize() {
		return new Dimension(750, 500);
	}

	/**
	 * @return the underlying view
	 */
	public ResultsView getCurrentView() {
		Component comp = m_view_container.getComponent(0);
		if (comp == null) {
			return null;
		} else {
			if (comp instanceof JTabbedPane) {
				JTabbedPane tabpane = (JTabbedPane) comp;
				ResultsView view = (ResultsView) tabpane.getSelectedComponent();
				return view;
			} else if (comp instanceof ResultsView) {
				ResultsView view = (ResultsView) comp;
				return view;
			}
		}
		return null;
	}

	/**
	 * @return a collection of all ResultsView objects in the frame.
	 */
	public Collection getViews() {
		LinkedList results = new LinkedList();
		Component comp = m_view_container.getComponent(0);
		if (comp == null) {
			return results;
		} else {
			if (comp instanceof JTabbedPane) {
				JTabbedPane tabpane = (JTabbedPane) comp;
				for (int index = 0; index < tabpane.getTabCount(); index++) {
					ResultsView view = (ResultsView) tabpane.getComponentAt(index);
					results.add(view);
				}
			} else if (comp instanceof ResultsView) {
				ResultsView view = (ResultsView) comp;
				results.add(view);
			}
		}
		return results;
	}

	/**
	 * Creates the menu, toolbar, and child components for this frame window
	 */
	void initialize() {
		createMenu();
		createToolBar();
		m_view_container = new TSPanel(new BorderLayout());
		getContentPane().add(m_view_container, BorderLayout.CENTER);
	}

	/**
	 * Creates the menu, toolbar, and content window for this frame
	 * 
	 * @param params
	 *            a 2 length array that contains a TSConnection and
	 *            ResultSetReference object
	 */
	public void initializeModel(Object[] params) {
		try {

			m_tsconnection = (TSConnection) params[0];
			Object results = params[1];
			m_launcher = params[2];
			TableId tableid = null;
			if (params.length == 4)
				tableid = (TableId) params[3];

			if (results instanceof ResultSetReference) {
				ResultSetReference ref = (ResultSetReference) results;
				SQLResultsModel model = new SQLResultsModel(m_tsconnection, ref, tableid);
				ResultsView view = new ResultsView(m_tsconnection, model);
				setContent(view);
			} else if (results instanceof QueryResultSet) {
				/**
				 * we allow passing QueryResultSet for cases where the resultset
				 * if forward only
				 */
				SQLResultsModel model = new SQLResultsModel(m_tsconnection, (QueryResultSet) results, tableid);
				ResultsView view = new ResultsView(m_tsconnection, model);
				setContent(view);
			} else if (results instanceof ResultsManager) {
				/**
				 * this supports the case where we have multiple results from a
				 * query/stored procedure
				 */
				ResultsManager rmgr = (ResultsManager) results;
				if (rmgr.size() <= 1) {
					Collection query_results = rmgr.getResults();
					Iterator iter = query_results.iterator();
					if (iter.hasNext()) {
						QueryResultSet qset = (QueryResultSet) iter.next();
						SQLResultsModel model = new SQLResultsModel(m_tsconnection, qset, null);
						ResultsView view = new ResultsView(m_tsconnection, model);
						setContent(view);
					}
				} else {
					Collection query_results = rmgr.getResults();
					JTabbedPane tab = new JTabbedPane(JTabbedPane.BOTTOM);
					Iterator iter = query_results.iterator();
					int count = 1;
					while (iter.hasNext()) {
						QueryResultSet qset = (QueryResultSet) iter.next();
						SQLResultsModel model = new SQLResultsModel(m_tsconnection, qset, null);
						ResultsView view = new ResultsView(m_tsconnection, model);
						tab.addTab(I18N.format("ResultSet_1", TSUtils.getInteger(count)), m_frameicon, view);
						count++;
					}
					setContent(tab);
				}
			}

			SQLResultsController controller = new SQLResultsController(this);
			setController(controller);
			SQLResultsUIDirector uidirector = new SQLResultsUIDirector(this);
			setUIDirector(uidirector);
			uidirector.updateComponents(null);
		} catch (Exception e) {
			SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, this, true);
			dlg.initialize(e);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
		}
	}

	/**
	 * Sets the main content pane
	 */
	private void setContent(Component comp) {
		m_view_container.removeAll();
		// getContentPane().add( comp, BorderLayout.CENTER );
		m_view_container.add(comp, BorderLayout.CENTER);
	}

	/**
	 * This is only needed to support docking frames
	 */
	public void setDelegate(com.jeta.foundation.gui.components.WindowDelegate delegate) {
		java.awt.Container cc = getContentPane();
		if (cc != null)
			cc.remove(m_view_container);

		super.setDelegate(delegate);
		cc = getContentPane();
		if (cc != null && m_view_container != null) {
			cc.add(m_view_container, java.awt.BorderLayout.CENTER);
		}
	}

	/**
	 * Saves any frame settings such as column widths for a given SQL
	 */
	void saveFrame() {
		Collection views = getViews();
		Iterator iter = views.iterator();
		while (iter.hasNext()) {
			ResultsView resultsview = (ResultsView) iter.next();
			SQLResultsModel model = resultsview.getModel();
			QueryResultsView view = resultsview.getView();

			// now, let's store the table settings (e.g. the table column
			// widths) so that the next
			// time the user runs this query, we can restore the table to the
			// way the user prefers it
			String sql = model.getUnprocessedSQL();
			if (sql == null)
				sql = model.getSQL();

			if (sql != null && sql.length() > 0) {
				TSTablePanel tspanel = view.getTablePanel();
				TableSettings tablesettings = TableUtils.getTableSettings(tspanel);
				SQLSettingsMgr smgr = SQLSettingsMgr.getInstance(getConnection());
				String trimsql = smgr.trim(sql);

				SQLSettings sqlsettings = smgr.get(trimsql);
				if (sqlsettings == null) {
					sqlsettings = new SQLSettings(trimsql);
				} else {
					// @todo trim spaces and semicolon from sql as well as
					// convert to lowercase since
					// we want to handle from sql command window as well
					smgr.remove(trimsql);
				}

				sqlsettings.setTableSettings(tablesettings);
				smgr.add(sqlsettings);

				// @todo we probably could optimize this and not save the entire
				// SQLSettingsMgr every time
				smgr.save();
			}
		}
	}

}
