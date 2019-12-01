package com.jeta.abeille.gui.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import java.io.File;
import java.io.Serializable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.model.LinkUI;
import com.jeta.abeille.gui.model.ModelerLinkUI;
import com.jeta.abeille.gui.model.ModelerEventHandler;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewModelEvent;
import com.jeta.abeille.gui.model.ModelViewModelListener;
import com.jeta.abeille.gui.model.ModelViewNames;
import com.jeta.abeille.gui.model.TableViewController;
import com.jeta.abeille.gui.model.TableWidget;
import com.jeta.abeille.gui.model.ViewGetter;
import com.jeta.abeille.gui.update.InstanceNames;

import com.jeta.foundation.documents.DocumentFrame;
import com.jeta.foundation.documents.DocumentManager;
import com.jeta.foundation.documents.DocumentNames;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.gui.split.CustomSplitPane;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the frame window that contains a graphical view of tables and joins
 * used for a query
 * 
 * @author Jeff Tassin
 */
public class QueryBuilderFrame extends DocumentFrame implements ViewGetter, ModelViewModelListener {
	/** the database connection */
	private TSConnection m_connection;

	/** the view (ModelView) of the tables and links in the query */
	private QueryBuilderView m_queryview;

	/** the constraint viwe in the bottom left pane */
	private ConstraintView m_constraintview;

	/** the model for the constraint view */
	private ConstraintModel m_constraintmodel;

	/**
	 * the view of reportables (columns to display in the results), in bottom
	 * right pane
	 */
	private ReportablesView m_reportablesview;

	/** the model for the reportables view */
	private ReportablesModel m_reportablesmodel;

	/**
	 * the model for the query as a whole - includes everything (constraints,
	 * reportables, relationships, etc. )
	 */
	private QueryModel m_querymodel = null;

	/**
	 * Time stamp for keeping track of modifications
	 */
	private long m_time_stamp = 0;

	/**
	 * The main split pane
	 */
	private JSplitPane m_main_split;

	private ModelerEventHandler m_modelerlistener = new BuilderModelerEventHandler(this);

	public static final String ID_MAIN_SPLIT = "main.splitter";
	public static final String ID_BOTTOM_SPLIT = "bottom.splitter";
	public static final String ID_VARIABLES_TOOLBAR = "variables.toolbar";
	public static final String ID_DISTINCT_CHECK = "distinct.check";

	/** the frame icon for this frame */
	static ImageIcon m_frameicon;

	public static final String ID_ANCHOR_CELL = "status.anchor.cell";

	static {
		m_frameicon = TSGuiToolbox.loadImage("incors/16x16/query_builder.png");
	}

	/**
	 * ctor
	 */
	public QueryBuilderFrame() {
		super("", "dbquery");
		setFrameIcon(m_frameicon);
		setTitle(I18N.getLocalizedMessage("Query Builder"));
		setShortTitle(I18N.getLocalizedMessage("Query Builder"));
	}

	protected void clearModel() {
		getContentPane().remove(m_main_split);
		if (m_querymodel != null)
			m_querymodel.getModeler().removeListener(m_modelerlistener);

		m_main_split.removeAll();
		m_querymodel = null;
		m_queryview = null;
		getContentPane().repaint();
	}

	/**
	 * Creates the toolbar, contraint view, and reportables view at the bottom
	 * pane in this frame
	 */
	JPanel createBottomPanel() {
		CustomSplitPane split = new CustomSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setName(ID_BOTTOM_SPLIT);

		m_constraintmodel = new ConstraintModel(m_querymodel);
		m_constraintview = new ConstraintView(m_constraintmodel, m_connection);
		m_constraintview.setController(new ConstraintViewController(this, m_constraintview));
		split.add(m_constraintview);

		m_reportablesmodel = new ReportablesModel(m_querymodel);
		m_reportablesview = new ReportablesView(m_reportablesmodel);
		m_reportablesview.setController(new ReportablesViewController(getConnection(), m_reportablesview));

		split.add(m_reportablesview);
		JPanel panel = new JPanel(new BorderLayout());

		panel.add(split, BorderLayout.CENTER);
		split.setDividerLocation(0.5f);

		panel.setMinimumSize(new Dimension(100, 32));
		return panel;
	}

	/**
	 * Initializes the components on this frame
	 */
	protected void createComponents() {
		createMenu();
		createToolBar();
		setController(new QueryBuilderController(this, m_connection));
	}

	/**
	 * Creates and initizes the menu for this frame
	 */
	protected void createMenu() {
		MenuTemplate template = this.getMenuTemplate();

		MenuDefinition menu = new MenuDefinition(I18N.getLocalizedMessage("File"));
		menu.setName("file");

		menu.add(i18n_createMenuItem("Open", DocumentNames.ID_OPEN,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("New", DocumentNames.ID_NEW,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK, false)));

		menu.add(i18n_createMenuItem("Save", DocumentNames.ID_SAVE,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Save As", DocumentNames.ID_SAVE_AS, null));
		menu.add(i18n_createMenuItem("Close", DocumentNames.ID_CLOSE, null));
		menu.addSeparator();

		menu.add(i18n_createMenuItem("Show SQL", QueryNames.ID_SHOW_SQL, null));

		menu.addSeparator();
		menu.add(i18n_createMenuItem("Page Setup", ModelViewNames.ID_PAGE_SETUP, null));
		menu.add(i18n_createMenuItem("Print Preview", ModelViewNames.ID_PRINT_PREVIEW, null));
		menu.add(i18n_createMenuItem("Print", ModelViewNames.ID_PRINT, null));
		menu.addSeparator();
		menu.add(i18n_createMenuItem("Save As Image", ModelViewNames.ID_SAVE_AS_SVG, null));
		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Edit"));
		menu.add(i18n_createMenuItem("Cut", TSComponentNames.ID_CUT,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Copy", TSComponentNames.ID_COPY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Copy Joins", ModelViewNames.ID_COPY_JOINS,
				KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Copy Joins Qualified", ModelViewNames.ID_COPY_JOINS_QUALIFIED, null));
		menu.add(i18n_createMenuItem("Paste", TSComponentNames.ID_PASTE,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Select All", ModelViewNames.ID_SELECT_ALL,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK, false)));
		menu.addSeparator();
		menu.add(i18n_createMenuItem("Options", QueryNames.ID_OPTIONS, null));
		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Table"));
		menu.add(i18n_createMenuItem("Add Table", QueryNames.ID_ADD_TO_VIEW, null));
		menu.add(i18n_createMenuItem("Remove Table", QueryNames.ID_REMOVE_FROM_VIEW, null));
		menu.add(i18n_createMenuItem("Include", QueryNames.ID_INCLUDE_TABLE,
				KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Exclude", QueryNames.ID_EXCLUDE_TABLE,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK, false)));

		menu.addSeparator();
		menu.add(createMenuItem(InstanceNames.ID_NAME, QueryNames.ID_UPDATE_TABLE,
				KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("select_star", QueryNames.ID_QUERY_TABLE,
				KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, false)));

		template.add(menu);

		// constraints menu
		menu = new MenuDefinition(I18N.getLocalizedMessage("Constraints"));
		menu.add(i18n_createMenuItem("Add Constraint", QueryNames.ID_ADD_CONSTRAINT, null));
		menu.add(i18n_createMenuItem("Remove Constraint", QueryNames.ID_REMOVE_CONSTRAINT, null));
		template.add(menu);

		// reportables menu
		menu = new MenuDefinition(I18N.getLocalizedMessage("Results"));
		menu.add(i18n_createMenuItem("Add Column", QueryNames.ID_ADD_REPORTABLE, null));
		menu.add(i18n_createMenuItem("Remove Column", QueryNames.ID_REMOVE_REPORTABLE, null));

		template.add(menu);

	}

	public TSInternalFrame createNewFrame() {
		TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
		TSInternalFrame iframe = wsframe.createInternalFrame(QueryBuilderFrame.class, false, m_connection.getId());
		Object[] params = new Object[1];
		params[0] = m_connection;
		iframe.initializeModel(params);
		wsframe.addWindow(iframe);
		return iframe;
	}

	public Serializable createNewModel() {
		QueryModel qmodel = new QueryModel(getConnection());
		QueryBuilderFile qfile = new QueryBuilderFile(qmodel);
		setModel(qfile);
		setTitle(I18N.getLocalizedMessage("New Query"));
		return qfile;
	}

	/**
	 * creates the toolbar at the top of the frame
	 */
	protected void createToolBar() {
		TSToolBarTemplate template = this.getToolBarTemplate();

		template.add(i18n_createToolBarButton(DocumentNames.ID_OPEN, "incors/16x16/folder.png", "Open Query"));
		template.add(i18n_createToolBarButton(DocumentNames.ID_NEW, "incors/16x16/document_plain_new.png", "New Query"));
		template.add(i18n_createToolBarButton(DocumentNames.ID_SAVE, "incors/16x16/disk_blue.png", "Save"));

		template.addSeparator();

		template.add(i18n_createToolBarButton(QueryNames.ID_ADD_TO_VIEW, "incors/16x16/table_sql_add.png", "Add Table"));

		template.add(createToolBarButton(QueryNames.ID_UPDATE_TABLE, "incors/16x16/form_blue.png",
				InstanceNames.ID_SHOW_INSTANCE_VIEW));
		template.add(i18n_createToolBarButton(QueryNames.ID_QUERY_TABLE, "incors/16x16/table_sql_view.png",
				"select_star"));

		template.addSeparator();

		template.add(i18n_createToolBarButton(QueryNames.ID_SHOW_SQL, "incors/16x16/view.png", "Show SQL"));
		template.add(i18n_createToolBarButton(QueryNames.ID_RUN_QUERY, "run_current16.gif", "Run Query"));
		template.add(i18n_createToolBarButton(QueryNames.ID_RUN_QUERY_NEW_WINDOW, "run_new_window16.gif",
				"Run Query With New Results Window"));

		template.add(javax.swing.Box.createHorizontalStrut(16));

		ButtonGroup group = new ButtonGroup();
		JRadioButton btn = new JRadioButton(TSGuiToolbox.loadImage("mouse16.gif"));
		btn.setSelectedIcon(TSGuiToolbox.loadImage("mouse_sel16.gif"));
		btn.setToolTipText(I18N.getLocalizedMessage("Mouse Tool"));
		btn.setSelected(true);
		btn.setContentAreaFilled(false);
		setCommandHandler(btn, QueryNames.ID_MOUSE_TOOL);
		template.add(btn);
		group.add(btn);

		btn = new JRadioButton(TSGuiToolbox.loadImage("link16.gif"));
		btn.setSelectedIcon(TSGuiToolbox.loadImage("link_sel16.gif"));
		btn.setToolTipText(I18N.getLocalizedMessage("Link_Tool_Tip"));
		btn.setContentAreaFilled(false);

		setCommandHandler(btn, QueryNames.ID_LINK_TOOL);
		group.add(btn);
		template.add(btn);

		template.add(Box.createHorizontalStrut(10));

		template.add(i18n_createToolBarButton(QueryNames.ID_ADD_CONSTRAINT, "constraint16.gif", "Add Constraint"));
		template.add(i18n_createToolBarButton(QueryNames.ID_ADD_REPORTABLE, "reportable16.gif", "Add Result"));
		template.add(i18n_createToolBarButton(QueryNames.ID_MOVE_UP, "incors/16x16/navigate_up.png", "Move Up"));
		template.add(i18n_createToolBarButton(QueryNames.ID_MOVE_DOWN, "incors/16x16/navigate_down.png", "Move Down"));
		template.addSeparator();
		template.add(i18n_createToolBarButton(QueryNames.ID_OPTIONS, "incors/16x16/preferences.png", "Options"));
	}

	/**
	 * ModelViewModelListener implementation. Used when the model name changes.
	 */
	public void eventFired(ModelViewModelEvent evt) {
		if (evt.getID() == QueryModel.MODEL_NAME_CHANGED) {
			setQueryName((String) evt.getParameter(0));
		}
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	public Serializable getCurrentDocument() {
		saveViewToModel();
		return new QueryBuilderFile(m_querymodel);
	}

	/**
	 * DocumentOwner Implementation
	 */
	public long getLastModifiedTime() {
		long tm = 0;
		if (m_queryview != null) {
			tm = m_queryview.getLastModifiedTime();
			if (tm < m_constraintmodel.getLastModifiedTime())
				tm = m_constraintmodel.getLastModifiedTime();
			if (tm < m_reportablesmodel.getLastModifiedTime())
				tm = m_reportablesmodel.getLastModifiedTime();
		}
		return tm;
	}

	/**
	 * @return the query model for the view
	 */
	public QueryModel getModel() {
		return m_querymodel;
	}

	/**
	 * @return the view responsible for managing the constraints
	 */
	public ConstraintView getConstraintView() {
		return m_constraintview;
	}

	/**
	 * @return the view repsonsible for displaying the reportables
	 */
	public ReportablesView getReportablesView() {
		return m_reportablesview;
	}

	/**
	 * @return the currently selected table in the current view. If more than
	 *         one table is selected, the table with the current focus is
	 *         selected from the view. If no tables are selected, null is
	 *         returned.
	 */
	public TableMetaData getSelectedTable() {
		QueryBuilderView view = m_queryview;
		Iterator iter = view.getSelectedItems().iterator();
		if (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof TableWidget) {
				TableMetaData tmd = ((TableWidget) obj).getTableMetaData();
				return tmd;
			}
		}
		return null;
	}

	/**
	 * @return the one and only query view component for this frame
	 */
	public ModelView getModelView() {
		return m_queryview;
	}

	/**
	 * @return the one and only query view component for this frame
	 */
	public Collection getViews() {
		LinkedList list = new LinkedList();
		list.add(m_queryview);
		return list;
	}

	public boolean hasModel() {
		return (m_querymodel != null);
	}

	/**
	 * Sets the connection needed by this frame.
	 * 
	 * @param params
	 *            a 2 length array. The first element must contain the
	 *            TSConnection object. The second element must contain the
	 *            QueryModel object that we wish to edit.
	 */
	public void initializeModel(Object[] params) {
		m_connection = (TSConnection) params[0];
		createComponents();
		createNewModel();
	}

	public boolean isDocumentModified() {
		long last_mod_time = getLastModifiedTime();
		return (m_time_stamp < last_mod_time);
	}

	public void setDocumentModified(boolean bModified) {
		if (bModified)
			m_time_stamp = 0;
		else
			m_time_stamp = System.currentTimeMillis();
	}

	/**
	 * Reloads the constriants in the constraintview.
	 */
	void set(Catalog catalog, Schema schema) {
		m_constraintmodel.set(m_connection, catalog, schema);
		m_constraintview.repaint();
	}

	protected void setModel(Object obj) {
		try {
			getController().enableEvents(false);

			if (m_main_split != null)
				getContentPane().remove(m_main_split);

			QueryBuilderFile qfile = (QueryBuilderFile) obj;
			qfile.initialize(getConnection());
			qfile.getModeler().removeListener(m_modelerlistener);
			qfile.getModeler().addListener(m_modelerlistener);

			m_querymodel = qfile.getQueryModel();
			LinkUI linkui = new ModelerLinkUI(m_querymodel.getModeler());
			m_queryview = new QueryBuilderView(qfile.getQueryModel(), linkui);
			linkui.setView(m_queryview);

			m_queryview.setController(new TableViewController(m_queryview, qfile.getModeler()));
			linkui.setEnabled(false);

			JScrollPane scroller = new JScrollPane(m_queryview);

			m_main_split = new CustomSplitPane(JSplitPane.VERTICAL_SPLIT);
			m_main_split.add(scroller);
			m_main_split.add(createBottomPanel());
			m_main_split.setDividerLocation(0.7f);
			getContentPane().add(m_main_split, BorderLayout.CENTER);

			getContentPane().repaint();

			getController().enableEvents(true);
			((QueryBuilderController) getController()).initializeController();

			m_time_stamp = System.currentTimeMillis();

			revalidate();
			repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void saveViewToModel() {
		m_constraintmodel.saveState(m_querymodel);
		m_reportablesmodel.saveState(m_querymodel);
	}

	/**
	 * Sets the name of the query
	 */
	public void setQueryName(String queryName) {
		setTitle(I18N.getLocalizedMessage("Query Builder"));
		setShortTitle(I18N.getLocalizedMessage("Query Builder"));
	}

	public void updateUI() {
		super.updateUI();
		revalidate();
	}

}
