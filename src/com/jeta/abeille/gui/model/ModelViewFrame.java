package com.jeta.abeille.gui.model;

import java.awt.Component;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import java.io.File;
import java.io.Serializable;

import java.util.Calendar;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.abeille.gui.keyboard.KeyboardManager;
import com.jeta.abeille.gui.keyboard.KeyBindingsNames;

import com.jeta.abeille.gui.model.options.ModelViewPreferencesModel;
import com.jeta.abeille.gui.model.overview.CanvasOverview;
import com.jeta.abeille.gui.update.InstanceNames;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;

import com.jeta.foundation.documents.DocumentFrame;
import com.jeta.foundation.documents.DocumentManager;
import com.jeta.foundation.documents.DocumentNames;

import com.jeta.foundation.gui.canvas.CanvasListener;
import com.jeta.foundation.gui.canvas.CanvasEvent;

import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.gui.split.CustomSplitPane;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;

import com.jeta.foundation.utils.JETATimer;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.gui.framework.JETAController;

/**
 * This is the frame window that contains a graphical view of tables in the
 * database and their relationships to one another. Allows the user to create
 * new tables, add tables to views, create new views, etc.
 * 
 * @author Jeff Tassin
 */
public class ModelViewFrame extends DocumentFrame implements ViewGetter, CanvasListener, ModelerGetter {
	/** the database connection */
	private TSConnection m_connection;

	/** the tab pane for this frame */
	private JTabbedPane m_tabPane;

	/**
	 * The split pane that holds the modelerview and the tabpane
	 */
	private CustomSplitPane m_splitpane;

	public static final String MODEL_VIEWS_NAME = "model.view.frame.views";
	public static final String ID_MAIN_CELL = "model.view.frame.maincell";
	public static final String ID_MAIN_SPLIT = "model.view.frame.splitpane";
	public static final String ID_TAB_PANE = "model.view.frame.tabpane";
	public static final String ID_LAST_MODEL = "last.model";

	/** view of the modeler tree */
	private ModelerView m_modelerview;

	/**
	 * we need this to keep track of the list of tables that are currently being
	 * modeled but are not yet saved in the system. This allows us to see those
	 * tables in any drop downs lists when we are modeling. Each ModelViewModel
	 * gets a reference to this object
	 */
	private ModelerModel m_modeler = null;

	/**
	 * The view of the modelermodel
	 */
	private ModelSideBar m_modelviewcontainer = null;

	/**
	 * The frame state
	 */
	private ModelViewFrameState m_framestate = null;

	/**
	 * Time stamp for keeping track of modifications
	 */
	private long m_time_stamp = 0;

	/** the frame icon for this frame */
	public static ImageIcon FRAME_ICON;

	static {
		FRAME_ICON = TSGuiToolbox.loadImage("incors/16x16/branch.png");
	}

	/**
	 * ctor
	 */
	public ModelViewFrame() {
		super("", "dbmodel");
		setFrameIcon(FRAME_ICON);
	}

	/**
	 * CanvasListener implementation. We need canvas events to update the
	 * overview window.
	 */
	public void canvasEvent(CanvasEvent evt) {
		ModelSideBar sidebar = (ModelSideBar) getModelerViewContainer();
		if (sidebar.isVisible()) {
			CanvasOverview overview = sidebar.getOverview();
			if (overview != null) {
				overview.repaint();
			}
		}
	}

	/**
	 * Changes the name of the current view to the new given name
	 * 
	 * @param newViewName
	 *            the new name to set
	 */
	public void changeCurrentViewName(String newViewName) {
		ModelView view = getCurrentView();
		if (view != null) {
			int tabindex = m_tabPane.getSelectedIndex();
			m_tabPane.setTitleAt(tabindex, newViewName);
			view.setViewName(newViewName);
		}
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

		menu.add(i18n_createMenuItem("Page Setup", ModelViewNames.ID_PAGE_SETUP, null));
		menu.add(i18n_createMenuItem("Print Preview", ModelViewNames.ID_PRINT_PREVIEW, null));
		menu.add(i18n_createMenuItem("Print", ModelViewNames.ID_PRINT, null));
		menu.addSeparator();
		menu.add(i18n_createMenuItem("Save As Image", ModelViewNames.ID_SAVE_AS_SVG, null));

		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Edit"));
		menu.setName("edit");

		menu.add(i18n_createMenuItem("Cut", TSComponentNames.ID_CUT,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Copy", TSComponentNames.ID_COPY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Copy Joins", ModelViewNames.ID_COPY_JOINS,
				KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Copy Joins Qualified", ModelViewNames.ID_COPY_JOINS_QUALIFIED, null));
		menu.add(i18n_createMenuItem("Paste", TSComponentNames.ID_PASTE,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Select All", ModelViewNames.ID_SELECT_ALL,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK, false)));
		menu.addSeparator();
		menu.add(i18n_createMenuItem("Preferences", ModelViewNames.ID_PREFERENCES, null));
		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Table"));
		menu.setName("table");

		KeyboardManager kmgr = KeyboardManager.getInstance();

		menu.add(i18n_createMenuItem("Table Properties", ModelViewNames.ID_TABLE_PROPERTIES,
				kmgr.getKeyStroke(KeyBindingsNames.ID_TABLE_PROPERTIES)));
		menu.add(createMenuItem(InstanceNames.ID_NAME, ModelViewNames.ID_UPDATE_TABLE,
				KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("select_star", ModelViewNames.ID_QUERY_TABLE,
				KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, false)));
		menu.add(i18n_createMenuItem("Export Data", ModelViewNames.ID_EXPORT_TABLE_DATA, null));
		menu.addSeparator();
		menu.add(i18n_createMenuItem("Add Table To View", ModelViewNames.ID_ADD_TO_VIEW, null));
		menu.add(i18n_createMenuItem("Create New Table", ModelViewNames.ID_CREATE_TABLE, null));
		menu.add(i18n_createMenuItem("Delete From View", ModelViewNames.ID_REMOVE_FROM_VIEW, null));

		menu.add(i18n_createMenuItem("Commit Prototype", ModelViewNames.ID_COMMIT_TABLES, null));
		menu.add(i18n_createMenuItem("Drop Table", ModelViewNames.ID_DROP_TABLE, null));

		if (TSUtils.isDebug()) {
			menu.add(i18n_createMenuItem("Import", ModelViewNames.ID_IMPORT_DATA, null));
		}

		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("View"));
		menu.setName("view");

		menu.add(i18n_createMenuItem(I18N.getLocalizedMessage("New View"), ModelViewNames.ID_NEW_VIEW, null));
		menu.add(i18n_createMenuItem("Change view name", ModelViewNames.ID_CHANGE_VIEW_NAME, null));
		menu.add(i18n_createMenuItem(I18N.getLocalizedMessage("Remove View"), ModelViewNames.ID_REMOVE_VIEW, null));
		menu.add(i18n_createMenuItem("Show Prototypes", ModelViewNames.ID_SHOW_PROTOTYPES,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK, false)));
		menu.add(i18n_createMenuItem("Hide Prototypes", ModelViewNames.ID_HIDE_PROTOTYPES,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK, false)));
		template.add(menu);
	}

	public TSInternalFrame createNewFrame() {
		TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
		TSInternalFrame iframe = wsframe.createInternalFrame(ModelViewFrame.class, false, m_connection.getId());
		Object[] params = new Object[1];
		params[0] = m_connection;
		iframe.initializeModel(params);
		wsframe.addWindow(iframe);
		return iframe;
	}

	public Serializable createNewModel() {
		ModelerModel modeler = new ModelerModel(getConnection());
		ModelViewModel model = new ModelViewModel(I18N.getLocalizedMessage("Main"), m_modeler, getConnection());

		ModelWorkBook workbook = new ModelWorkBook(modeler, model);
		setModel(workbook);
		setTitle(I18N.getLocalizedMessage("New Model"));
		return workbook;
	}

	protected void createToolBar() {
		TSToolBarTemplate template = this.getToolBarTemplate();
		Dimension d = new Dimension(16, 16);

		template.add(i18n_createToolBarButton(DocumentNames.ID_OPEN, "incors/16x16/folder.png", "Open Model"));
		template.add(i18n_createToolBarButton(DocumentNames.ID_NEW, "incors/16x16/document_plain_new.png", "New Model"));
		template.add(i18n_createToolBarButton(DocumentNames.ID_SAVE, "incors/16x16/disk_blue.png", "Save"));

		template.add(i18n_createToolBarButton(ModelViewNames.ID_PRINT_PREVIEW, "incors/16x16/printer_view.png",
				"Print Preview"));
		template.add(i18n_createToolBarButton(ModelViewNames.ID_PRINT, "incors/16x16/printer.png", "Print"));

		template.addSeparator();
		template.add(i18n_createToolBarButton(ModelViewNames.ID_ADD_TO_VIEW, "incors/16x16/table_sql_add.png",
				"Add Table To View"));

		template.add(i18n_createToolBarButton(ModelViewNames.ID_CREATE_TABLE, "incors/16x16/table_sql_create.png",
				"Create New Table"));

		template.addSeparator();
		template.add(Box.createHorizontalStrut(5));

		ButtonGroup group = new ButtonGroup();
		JRadioButton btn = new JRadioButton(TSGuiToolbox.loadImage("mouse16.gif"));
		btn.setToolTipText(I18N.getLocalizedMessage("Mouse Tool"));
		btn.setSelectedIcon(TSGuiToolbox.loadImage("mouse_sel16.gif"));
		btn.setSelected(true);
		btn.setPreferredSize(new Dimension(24, 24));
		btn.setMaximumSize(new Dimension(24, 24));
		btn.setContentAreaFilled(false);

		setCommandHandler(btn, ModelViewNames.ID_MOUSE_TOOL);
		template.add(btn);
		template.add(Box.createHorizontalStrut(5));

		group.add(btn);

		btn = new JRadioButton(TSGuiToolbox.loadImage("link16.gif"));
		btn.setMaximumSize(new Dimension(24, 24));
		btn.setPreferredSize(new Dimension(24, 24));
		btn.setToolTipText(I18N.getLocalizedMessage("Link_Tool_Tip"));
		btn.setSelectedIcon(TSGuiToolbox.loadImage("link_sel16.gif"));
		btn.setContentAreaFilled(false);

		setCommandHandler(btn, ModelViewNames.ID_LINK_TOOL);
		group.add(btn);
		template.add(btn);
		template.add(Box.createHorizontalStrut(5));

		template.addSeparator();

		template.add(i18n_createToolBarButton(ModelViewNames.ID_PREFERENCES, "incors/16x16/preferences.png",
				"Preferences"));
		template.add(i18n_createToolBarButton(ModelViewNames.ID_INCREASE_FONT, "incors/16x16/arrow_up_blue.png",
				"Increase Font Size"));
		template.add(i18n_createToolBarButton(ModelViewNames.ID_DECREASE_FONT, "incors/16x16/arrow_down_blue.png",
				"Decrease Font Size"));
		template.addSeparator();

		template.add(i18n_createToolBarButton(ModelViewNames.ID_TABLE_PROPERTIES, "incors/16x16/table_sql_check.png",
				"Table Properties"));
		template.add(createToolBarButton(ModelViewNames.ID_UPDATE_TABLE, "incors/16x16/form_blue.png",
				InstanceNames.ID_NAME));
		template.add(i18n_createToolBarButton(ModelViewNames.ID_QUERY_TABLE, "incors/16x16/table_sql_view.png",
				"select_star"));

		template.addSeparator();
		template.add(i18n_createToolBarButton(ModelViewNames.ID_COMMIT_TABLES, "incors/16x16/data_into.png",
				"Commit Prototype"));
	}

	public Dimension getCanvasSize() {
		return new Dimension(2200, 1600);
	}

	/** return the named component */
	public Component getComponentByName(String componentName) {
		if (ID_TAB_PANE.equals(componentName))
			return m_tabPane;
		else
			return super.getComponentByName(componentName);
	}

	/**
	 * Creates the modeler view that we display in the left split pane
	 */
	ModelSideBar getModelerViewContainer() {
		if (m_modelviewcontainer == null && m_modeler != null) {
			m_modelviewcontainer = new ModelSideBar(this, m_modeler);
		}
		return m_modelviewcontainer;
	}

	public void centerView(ModelView view, int center_x, int center_y) {
		Container parent = view.getParent();
		if (parent instanceof JViewport) {
			JViewport viewport = (JViewport) parent;
			Dimension view_sz = viewport.getExtentSize();

			int org_x = center_x - (view_sz.width / 2);
			int org_y = center_y - (view_sz.height / 2);
			if (org_x < 0) {
				org_x = 0;
			} else if ((org_x + view_sz.width) > view.getWidth()) {
				org_x = view.getWidth() - view_sz.width;
			}

			if (org_y < 0) {
				org_y = 0;
			} else if ((org_y + view_sz.height) > view.getHeight()) {
				org_y = view.getHeight() - view_sz.height;
			}

			Point pt = viewport.getViewPosition();
			pt.x = org_x;
			pt.y = org_y;
			viewport.setViewPosition(pt);
		} else {
			assert (false);
		}

	}

	/**
	 * Instanstiates and initializes the view class for the given model. and any
	 * necessary event handling mechanisms are initialized. The view is NOT
	 * added to the tab pane.
	 * 
	 * @param viewName
	 *            the name for the view
	 * @param model
	 *            the model. This can be null. If so, a new emtpy model is
	 *            created
	 */
	private ModelView createViewInternal(ModelViewModel model) {
		assert (model != null);

		model.rememberDeletedLinks(false);
		LinkUI linkui = new ModelerLinkUI(m_modeler);
		ModelView modelview = new ModelView(model, linkui);
		modelview.addListener(this);

		linkui.setView(modelview);
		modelview.setViewName(model.getViewName());
		modelview.setController(new ModelViewController(modelview, m_modeler));
		// don't allow dragging links in this view
		modelview.getLinkUI().setEnabled(false);

		modelview.setSize(getCanvasSize());
		modelview.setPreferredSize(getCanvasSize());
		modelview.validateComponents();
		return modelview;
	}

	/**
	 * Instanstiates the view class for the given model. The view is added to
	 * the tab pane and any necessary event handling mechanisms are initialized.
	 * 
	 * @param viewName
	 *            the name for the view
	 * @param model
	 *            the model. This can be null. If so, a new emtpy model is
	 *            created
	 */
	protected ModelView createView(ModelViewModel model) {
		ModelView modelview = createViewInternal(model);
		JScrollPane scroller = new JScrollPane(modelview);
		scroller.getViewport().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				/**
				 * so the canvas overview can be repainted to show the viewport
				 * rect
				 */
				canvasEvent(null);
			}
		});
		m_tabPane.addTab(model.getViewName(), scroller);
		return modelview;
	}

	/**
	 * Decreases the font size on the view by 1 point
	 */
	public void changeFontSize(boolean increase) {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		Font f = javax.swing.UIManager.getFont("List.font");
		int fontsize = f.getSize();
		String fsize = userprops.getProperty(ModelViewNames.ID_TABLE_WIDGET_FONT_SIZE);
		if (fsize != null) {
			try {
				fontsize = Integer.parseInt(fsize);
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}

		if (increase) {
			fontsize++;
		} else {
			fontsize--;
			if (fontsize < 1)
				fontsize = 1;
		}

		Collection views = getViews();
		Iterator iter = views.iterator();
		while (iter.hasNext()) {
			ModelView view = (ModelView) iter.next();
			view.setFontSize(fontsize);
		}
		userprops.setProperty(ModelViewNames.ID_TABLE_WIDGET_FONT_SIZE, String.valueOf(fontsize));
	}

	protected void clearModel() {
		ModelViewFrameState framestate = getFrameState();
		if (framestate != null) {
			try {
				ObjectStore os = m_connection.getObjectStore();
				os.store(ModelViewFrameState.COMPONENT_ID, framestate);
			} catch (Exception e) {
			}
		}

		getController().enableEvents(false);
		m_modeler = null;
		Container contentpane = getContentPane();
		contentpane.remove(m_splitpane);
		contentpane.remove(m_tabPane);

		if (m_modelviewcontainer != null) {
			m_splitpane.remove(m_modelviewcontainer);
			m_modelviewcontainer.dispose();
		}

		m_tabPane.removeAll();
		m_modelviewcontainer = null;
		revalidate();
		contentpane.repaint();
		setTitle(I18N.getLocalizedMessage("Model View"));
		getController().enableEvents(true);

	}

	/**
	 * DocumentOwner Implementation
	 */
	public long getLastModifiedTime() {
		long tm = 0;
		for (int index = 0; index < m_tabPane.getTabCount(); index++) {
			ModelView view = getView(index);
			if (tm < view.getLastModifiedTime())
				tm = view.getLastModifiedTime();
		}
		return tm;
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	int getCurrentTabIndex() {
		return m_tabPane.getSelectedIndex();
	}

	/**
	 * @return the currently visible view
	 */
	protected ModelView getCurrentView() {
		if (m_modeler != null) {
			if (m_tabPane.getTabCount() > 0) {
				return getView(m_tabPane.getSelectedIndex());
			}
		}
		return null;
	}

	/**
	 * @return the frame state
	 */
	ModelViewFrameState getFrameState() {
		ModelViewFrameState result = null;

		if (m_framestate != null) {
			result = m_framestate;
		} else {
			try {
				ObjectStore os = m_connection.getObjectStore();
				result = (ModelViewFrameState) os.load(ModelViewFrameState.COMPONENT_ID);
			} catch (Exception e) {

			}
		}

		if (result == null) {
			if (m_splitpane != null && m_splitpane.isVisible()) {
				result = new ModelViewFrameState(true, m_splitpane.getDividerLocation());
			} else {
				result = new ModelViewFrameState(false, -1);
			}
		}

		m_framestate = result;
		return result;

	}

	/**
	 * @return the modeler tree view
	 */
	public ModelerView getModelerView() {
		if (m_modelviewcontainer != null)
			return m_modelviewcontainer.getModelerView();
		else
			return null;
	}

	/**
	 * @return the currently selected table in the current view. If more than
	 *         one table is selected, the table with the current focus is
	 *         selected from the view. If no tables are selected, null is
	 *         returned.
	 */
	public TableMetaData getSelectedTable() {
		ModelView view = getCurrentView();
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
	 * @return the table selector
	 */
	public TableSelectorModel getTableSelector() {
		return m_modeler;
	}

	/**
	 * ViewGetter implementation
	 */
	public ModelView getModelView() {
		return getCurrentView();
	}

	protected ModelView getView(int tabIndex) {
		JScrollPane scroller = (JScrollPane) m_tabPane.getComponentAt(tabIndex);
		return (ModelView) scroller.getViewport().getView();
	}

	protected ModelView getView(String viewName) {
		if (viewName == null)
			return null;

		int tabcount = m_tabPane.getTabCount();
		for (int index = 0; index < tabcount; index++) {
			if (viewName.equalsIgnoreCase(getViewName(index)))
				return getView(index);
		}
		return null;
	}

	/**
	 * @return the number of views in the frame
	 */
	public int getViewCount() {
		return m_tabPane.getTabCount();
	}

	/**
	 * @return the viewport from the view.
	 */
	public static javax.swing.JViewport getViewport(ModelView view) {
		java.awt.Component comp = view.getParent();
		while ((comp != null) && !(comp instanceof java.awt.Frame) && !(comp instanceof java.awt.Dialog)) {
			if (comp instanceof javax.swing.JViewport) {
				return (javax.swing.JViewport) comp;
			}
		}
		return null;
	}

	/**
	 * @return all views in the frame
	 */
	public Collection getViews() {
		LinkedList views = new LinkedList();
		int tabcount = m_tabPane.getTabCount();
		for (int index = 0; index < tabcount; index++) {
			JComponent comp = (JComponent) m_tabPane.getComponentAt(index);
			if (comp instanceof JScrollPane) {
				views.add(getView(index));
			}
		}
		return views;
	}

	/**
	 * @return the name of the view at the given tab index
	 */
	protected String getViewName(int tabIndex) {
		return m_tabPane.getTitleAt(tabIndex);
	}

	public ModelerModel getModeler() {
		return m_modeler;
	}

	public boolean hasModel() {
		return (m_modeler != null);
	}

	protected void initializeFrame() {
		if (!isInitialized()) {
			createMenu();
			createToolBar();

			m_tabPane = new JTabbedPane();
			m_tabPane.setName(ID_TAB_PANE);
			m_tabPane.setTabPlacement(JTabbedPane.BOTTOM);
			getContentPane().add(m_tabPane, BorderLayout.CENTER);

			try {
				m_splitpane = new CustomSplitPane(JSplitPane.HORIZONTAL_SPLIT);
				m_splitpane.setName(ID_MAIN_SPLIT);
				m_splitpane.setDividerLocation(0.7f);
				m_splitpane.setResizeWeight(1.0f);
			} catch (Exception e) {
				TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, this, true);
				dlg.initialize(null, e);
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
			}
		}
	}

	/**
	 * Sets the connection needed by this frame.
	 * 
	 * @param params
	 *            a 1 length array. The zero element must contain the
	 *            TSConnection object
	 */
	public void initializeModel(Object[] params) {
		m_connection = (TSConnection) params[0];

		m_connection.setImplementation(ModelerGetter.COMPONENT_ID, this);

		String mvtxt = I18N.getLocalizedMessage("Model View");
		setTitle(mvtxt);

		initializeFrame();

		setController(new ModelViewFrameController(this, m_connection));

		// try to automatically load the last model
		try {
			ModelViewPreferencesModel prefs = new ModelViewPreferencesModel();
			if (params.length > 1 && prefs.isAutoLoad()) {
				ObjectStore os = m_connection.getObjectStore();
				String lastpath = (String) params[1];
				if (lastpath != null) {
					File f = new File(lastpath);
					if (f.isFile()) {
						openModel(f);
						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		createNewModel();
	}

	protected boolean isAutoSave() {
		return TSUserPropertiesUtils.getBoolean(ModelViewSettings.ID_AUTO_SAVE_MODEL, false);
	}

	public boolean isDocumentModified() {
		long last_mod_time = getLastModifiedTime();
		return (m_time_stamp < last_mod_time);
	}

	public Serializable loadModel(File f) throws Exception {
		ModelWorkBook workbook = (ModelWorkBook) super.loadModel(f);
		LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
		System.out.println("ModelViewFrame.loadModel licensemgr: " + jlm);
		if (jlm.isEvaluation()) {
			if (!validateTimeOut(workbook)) {
				System.out.println("ModelViewFrame license manager is evaluation..");
				jlm.postMessage(I18N.getLocalizedMessage("Model_File_Expired"));
				return null;
			}
		}
		return workbook;
	}

	public void setDocumentModified(boolean bModified) {
		if (bModified)
			m_time_stamp = 0;
		else
			m_time_stamp = System.currentTimeMillis();
	}

	/**
	 * @return true if the views have been loaded in the frame. The frame might
	 *         not be initialized if the default database model has not finished
	 *         loading yet
	 */
	private boolean isInitialized() {
		return (m_modeler != null);
	}

	public boolean isPrototypesVisible() {
		return getFrameState().isModelerVisible();
	}

	/**
	 * Opens the model. Override so we can store the ID_LAST_MODEL value
	 */
	public boolean openModel(File f) {
		boolean result = super.openModel(f);
		if (result) {
			try {
				ObjectStore os = m_connection.getObjectStore();
				os.store(ID_LAST_MODEL, f.getPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Removes the view at the given tab index
	 */
	void removeView(int tabIndex) {
		if (m_tabPane.getTabCount() > 1 && tabIndex >= 0 && tabIndex < m_tabPane.getTabCount())
			m_tabPane.remove(tabIndex);
	}

	protected void setModel(Object obj) {
		getController().enableEvents(false);

		ModelWorkBook workbook = (ModelWorkBook) obj;
		workbook.initialize(getConnection());
		if (isPrototypesVisible() && m_tabPane.getTabCount() > 0) {
			ModelViewFrameState framestate = getFrameState();
			framestate.setDividerLocation(m_splitpane.getDividerLocation());
		}

		m_tabPane.removeAll();
		m_modeler = workbook.getModeler();
		m_modeler.addListener(new ModelerEventHandler(this));

		Collection mvms = workbook.getViewModels();
		Iterator iter = mvms.iterator();
		while (iter.hasNext()) {
			ModelViewModel mvm = (ModelViewModel) iter.next();
			createView(mvm);
		}
		getController().enableEvents(true);
		updateContentPane();
		m_time_stamp = System.currentTimeMillis();
	}

	public Serializable getCurrentDocument() {
		if (m_modeler != null) {
			LinkedList viewmodels = new LinkedList();
			for (int index = 0; index < m_tabPane.getTabCount(); index++) {
				ModelView view = getView(index);
				ModelViewModel model = view.getModel();
				viewmodels.add(model);
			}
			return new ModelWorkBook(m_modeler, viewmodels);
		} else {
			return null;
		}
	}

	/**
	 * Sets the message in the status bar
	 */
	public void setStatus(String msg) {

	}

	/**
	 * Select the view at the given tab index (zero based)
	 * 
	 * @param index
	 *            the index of the tab which to make active
	 */
	public void selectView(int index) {
		if (index >= 0 && index < m_tabPane.getTabCount()) {
			m_tabPane.setSelectedIndex(index);
		}
	}

	/**
	 * Shows/Hides the prototypes view (ModelerTreeView) in this frame.
	 */
	void showPrototypes(boolean bshow) {
		ModelViewFrameState framestate = getFrameState();
		framestate.setModelerVisible(bshow);
		if (!bshow)
			framestate.setDividerLocation(m_splitpane.getDividerLocation());

		updateContentPane();
	}

	/**
	 * Rebuilds the content pane based on the frame state
	 */
	void updateContentPane() {
		// not initalized yet
		assert (m_tabPane != null);

		Container contentpane = getContentPane();
		contentpane.remove(m_splitpane);
		contentpane.remove(m_tabPane);

		TSPanel mview = getModelerViewContainer();
		assert (mview != null);
		m_splitpane.remove(mview);
		m_splitpane.remove(m_tabPane);

		ModelViewFrameState framestate = getFrameState();
		if (framestate.isModelerVisible()) {
			m_splitpane.add(m_tabPane);
			m_splitpane.add(mview);

			contentpane.add(m_splitpane, BorderLayout.CENTER);

			if (framestate.getDividerLocation() < 16)
				m_splitpane.setDividerLocation(0.7);
			else
				m_splitpane.setDividerLocation(framestate.getDividerLocation());

			m_splitpane.revalidate();
		} else {
			contentpane.add(m_tabPane, BorderLayout.CENTER);
		}

		getController().updateComponents(null);
		revalidate();
	}

	/**
	 * Called when the user has changed some preferences and we need to update
	 * the views to refresh based on the the new preferences.
	 */
	public void updateSettings() {
		Collection views = getViews();
		Iterator iter = views.iterator();
		while (iter.hasNext()) {
			ModelView view = (ModelView) iter.next();
			view.updateSettings();
		}
	}

	/**
	 * Checks the time stamp on the model file to determin if it is older than
	 * 10 days. This is used in the evaluation version.
	 */
	private boolean validateTimeOut(ModelWorkBook workbook) {
		System.out.println("ModelViewFrame.validateTimeOut... ");
		Calendar created = workbook.getCreatedDate();
		if (created != null) {
			created = (Calendar) created.clone();
			created.add(Calendar.DATE, 10);
			Calendar current = Calendar.getInstance();
			if (current.after(created))
				return false;
		}
		return true;
	}

	/**
	 * This class is used as a place holder in the JTabbedPane for ModelViews.
	 * We don't want to load every view on startup because this can take time.
	 * So, instead of a ModelView, we put a ModelViewLabel in the tab pane. When
	 * the user clicks on the tab, we check the component is this type of class.
	 * If so, we load the view at that time.
	 */
	public static class ModelViewLabel extends JLabel {
		private ModelViewInfo m_info;

		public ModelViewLabel(ModelViewInfo info) {
			super(info.getViewName());
			m_info = info;
		}

		public String getModelId() {
			return m_info.getModelId();
		}
	}

}
