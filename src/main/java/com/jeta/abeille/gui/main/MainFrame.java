package com.jeta.abeille.gui.main;

import com.jeta.abeille.database.model.*;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.keyboard.KeyBindingsNames;
import com.jeta.abeille.gui.keyboard.KeyboardManager;
import com.jeta.abeille.gui.logger.LoggerFrame;
import com.jeta.abeille.gui.login.ConnectionMgrFrame;
import com.jeta.abeille.gui.model.ModelViewFrame;
import com.jeta.abeille.gui.model.ObjectTreeFrame;
import com.jeta.abeille.gui.store.ConnectionContext;
import com.jeta.abeille.gui.store.FrameState;
import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;
import com.jeta.foundation.documents.DocumentFrame;
import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.open.gui.framework.UIDirector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * This is the main frame window for the application.
 * 
 * @author Jeff Tassin
 */
public class MainFrame extends TSWorkspaceFrame {
	/**
	 * a set of ConnectionContext objects that correspond to the currently
	 * opened connections
	 */
	private LinkedList m_connections = new LinkedList();

	/** this combo box displays the list of opened connections */
	private JComboBox m_connectionbox;

	/** the status bar */
	private TSStatusBar m_statusbar;
	private TSCell m_urlcell;
	private TSCell m_usercell;
	private TSCell m_memorycell;
	private TSCell m_autocommitcell;

	/** displays the log view */
	private TSInternalFrame m_logframe;

	/** displays the connection mgr */
	private TSInternalFrame m_connection_mgr_frame;

	/**
	 * the database object tree frame
	 */
	private ObjectTreeFrame m_object_tree_frame;

	/** displays the current schema for ths given connection */
	private JTextField m_pathbox;

	/**
	 * The main split pane
	 */
	private JSplitPane m_main_split;

	/**
	 * The main tabbed pane
	 */
	private JTabbedPane m_main_tab;

	/**
	 * The connection that was last selected
	 */
	private ConnectionId m_last_cid;

	private AbeilleFrameManager m_frame_mgr = new AbeilleFrameManager(this);

	// status bar cell ids
	public static final String ID_DATABASE_CELL = "database.cell";
	public static final String ID_MEMORY_CELL = "memory.cell";
	public static final String ID_URL_CELL = "url.cell";
	public static final String ID_USER_CELL = "user.cell";
	public static final String ID_AUTO_COMMIT_CELL = "auto.commit.cell";

	public static final String MAIN_FRAME_STATE = "abeille.frame.state";
	public static final String OBJECT_TREE_FRAME_STATE = "object.tree.frame.state";

	/**
	 * ctor
	 */
	public MainFrame() {
		super("Abeille Database Client");

		setJMenuBar(new DynamicMenuBar());

		createMenu();
		createToolBar();
		createStatusBar();

		Rectangle frame_bounds = null;
		FrameState otree_fstate = null;
		try {
			ObjectStore os = (ObjectStore) ComponentMgr.lookup(ComponentNames.APPLICATION_STATE_STORE);
			FrameState mstate = (FrameState) os.load(MAIN_FRAME_STATE);
			if (mstate != null) {
				frame_bounds = mstate.getBounds();
			}

			otree_fstate = (FrameState) os.load(OBJECT_TREE_FRAME_STATE);
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean otree_docked = true;
		if (otree_fstate != null)
			otree_docked = otree_fstate.isInternal();

		/** major hack for now */
		m_frame_mgr.setObjectTreeFrameDock(otree_docked);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int inset = 50;
		Rectangle def_frame_bounds = new Rectangle(inset, inset, screenSize.width - inset * 2, screenSize.height
				- inset * 2);
		frame_bounds = calculateFrameBounds(screenSize, frame_bounds, def_frame_bounds, true);
		setBounds(frame_bounds);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				shutDown();
			}
		});

		m_main_split = new JSplitPane();
		m_main_tab = new JTabbedPane() {
			public Dimension getMinimumSize() {
				Dimension d = super.getMinimumSize();
				d.width = 100;
				return d;
			}
		};
		m_main_tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		// now create the one and only connection mgr frame
		Class fclass = com.jeta.abeille.gui.login.ConnectionMgrFrame.class;
		m_connection_mgr_frame = createInternalFrame(fclass, true, "connectionmgr");
		addWindow(m_connection_mgr_frame, true);

		// now create the one and only LoggerView
		fclass = com.jeta.abeille.gui.logger.LoggerFrame.class;
		m_logframe = createInternalFrame(fclass, true, "loggerview");
		addWindow(m_logframe, false);
		closeWindow(m_logframe);

		javax.swing.ImageIcon icon = TSGuiToolbox.loadImage("abeille16.gif");
		if (icon != null)
			setIconImage(icon.getImage());

		if (otree_docked) {
			getContentPane().add(m_main_split, BorderLayout.CENTER);

			fclass = com.jeta.abeille.gui.model.ObjectTreeFrame.class;
			TSInternalFrame iframe = createInternalFrame(fclass, true, null);
			iframe.initializeModel(null);
			m_main_split.setLeftComponent(iframe.getDelegate());
			m_object_tree_frame = (ObjectTreeFrame) iframe;
			addWindow(m_object_tree_frame, true);

			m_main_split.setRightComponent(m_main_tab);

			if (otree_fstate != null && !otree_fstate.isVisible()) {
				showObjectTree(false);
			}
		} else {
			getContentPane().add(m_main_tab, BorderLayout.CENTER);
			fclass = com.jeta.abeille.gui.model.ObjectTreeFrame.class;
			TSInternalFrame iframe = createInternalFrame(fclass, true, null);
			iframe.initializeModel(null);
			m_object_tree_frame = (ObjectTreeFrame) iframe;
			setInternalFrameState(m_object_tree_frame, otree_fstate, new Rectangle(50, 50, 400, 600));
			addWindow(m_object_tree_frame, otree_fstate.isVisible());
		}
		setController(new MainFrameController(this));

		/*
		Timer timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
				boolean isPathBox = focusOwner == m_pathbox;
				System.out.println("----------------------\n isPathBox: " + isPathBox + "  Current focus: " + (focusOwner == null ? "NULL" : focusOwner.getClass()) );
			}
		});
		timer.start();
		 */
	}

	void activateInternalMenu(MenuTemplate template) {
		JMenuBar menubar = getJMenuBar();
		if (menubar instanceof DynamicMenuBar) {
			DynamicMenuBar dmb = (DynamicMenuBar) menubar;
			dmb.setMenus(template);
		}
	}

	/**
	 * Updates the workspace UI when a frame is selected
	 */
	protected void activateInternalFrame(TSInternalFrame frame) {
		super.activateInternalFrame(frame);
	}

	/**
	 * Calculated the bounds on the given frame window.
	 * 
	 * @param containerBounds
	 *            the bounds of the container for the frame
	 * @param requestedBounds
	 *            the requested bounds
	 * @param defaultBounds
	 *            the default bounds if the request cannot be fulfilled.
	 * @param contained
	 *            flag that indicates if the result bounds should be fully
	 *            within the containerBounds
	 */
	Rectangle calculateFrameBounds(Dimension containerBounds, Rectangle requestedBounds, Rectangle defaultBounds,
			boolean contained) {
		if (requestedBounds == null) {
			return defaultBounds;
		}

		Rectangle result = new Rectangle(requestedBounds);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (result.width > screenSize.width)
			result.width = screenSize.width;

		if (result.height > screenSize.height)
			result.height = screenSize.height;

		if (result.x < 0)
			result.x = defaultBounds.x;

		if (result.y < 0)
			result.y = defaultBounds.y;

		if (result.width < 50)
			result.width = defaultBounds.width;

		if (result.height < 20)
			result.height = defaultBounds.height;

		if (result.x > containerBounds.width)
			result.x = defaultBounds.x;

		if (result.y > containerBounds.height)
			result.y = defaultBounds.y;

		if (contained) {
			if ((result.x + result.width) > containerBounds.width)
				result = defaultBounds;
			else if ((result.y + result.height) > containerBounds.height)
				result = defaultBounds;
		}
		return result;
	}

	/**
	 * Closes the given connection and all assoicated windows
	 * 
	 * @return false if the user canceled the close operation in some window
	 */
	boolean closeConnection(ConnectionContext ctx) {
		try {
			ConnectionId cid = ctx.getConnection().getId();
			Collection frames = getAllFrames(cid);
			Iterator iter = frames.iterator();
			while (iter.hasNext()) {
				TSInternalFrame iframe = (TSInternalFrame) iter.next();
				Object winid = iframe.getWindowId();
				if (winid != null && !(iframe instanceof LoggerFrame) && !(iframe instanceof ConnectionMgrFrame)
						&& !(iframe instanceof ObjectTreeFrame)) {
					assert (winid instanceof ConnectionId);
					if (cid.equals(winid)) {
						if (!disposeFrame(iframe))
							return false;
					}
				} else {
					if (TSUtils.isDebug()) {
						System.out.println("MainFrame.closeEnv  winid = null for " + iframe.getClass());
					}
				}
			}

			Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
			logger.fine(I18N.format("disconnected_from_1", ctx.getConnection().getUrl()));

			m_object_tree_frame.closeConnection(ctx.getConnection());

			m_connections.remove(ctx);
			m_connectionbox.removeItem(ctx.getConnection());
			m_connectionbox.revalidate();

			updateConnectionStatus();
			ObjectStore os = ctx.getConnection().getObjectStore();
			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Creates the windows/environment for a given connection
	 */
	void createEnvironment(TSConnection connection) {
		ConnectionContext ctx = new ConnectionContext(connection);

		m_object_tree_frame.addConnection(connection);
		m_connectionbox.addItem(connection);
		m_connectionbox.setSelectedItem(connection);
		m_connections.add(ctx);

		Object[] params = new Object[2];
		params[0] = connection;

		try {
			/** try to load the last model in the model view */
			ObjectStore os = connection.getObjectStore();
			params[1] = (String) os.load(ModelViewFrame.ID_LAST_MODEL);
		} catch (Exception e) {
		}

		Class fclass = ModelViewFrame.class;
		TSInternalFrame iframe = createInternalFrame(fclass, false, connection.getId());
		iframe.initializeModel(params);
		ctx.setFrame(fclass, iframe);
		addWindow(iframe, true);

		params = new Object[1];
		params[0] = connection;
		fclass = com.jeta.abeille.gui.sql.SQLFrame.class;
		iframe = createInternalFrame(fclass, true, connection.getId());
		iframe.initializeModel(params);
		ctx.setFrame(fclass, iframe);
		addWindow(iframe, true);

		MainFrameController controller = (MainFrameController) getController();
		UIDirector uidirector = getUIDirector();
		if (uidirector != null)
			uidirector.updateComponents(null);

	}

	protected JETAFrameManager createFrameManager() {
		return m_frame_mgr;
	}

	/**
	 * Creates the main menu for this application. These menu items will be
	 * shared across all internal windows.
	 */
	protected void createMenu() {
		DynamicMenuBar menubar = (DynamicMenuBar) getJMenuBar();

		MenuTemplate template = this.getMenuTemplate();
		MenuDefinition menu = new MenuDefinition(I18N.getLocalizedMessage("File"));
		menu.setName("file");
		menu.add(i18n_createMenuItem("Garbage Collect", MainFrameNames.ID_GC, null));
		menu.add(i18n_createMenuItem("Exit", MainFrameNames.ID_EXIT, null));
		template.add(menu);

		menubar.addApplicationMenu(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Options"));
		menu.setName("options");
		menu.add(createMenuItem("User Preferences", MainFrameNames.ID_USER_PREFERENCES, null));
		menubar.addApplicationMenu(menu);

		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Windows"));
		menu.setName("windows");
		menu.add(i18n_createMenuItem("Close", MainFrameNames.ID_CLOSE_WINDOW,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK, false)));

		menu.addSeparator();
		KeyboardManager kmgr = KeyboardManager.getInstance();
		menu.add(i18n_createMenuItem("Object Tree", MainFrameNames.ID_OBJECT_TREE,
				kmgr.getKeyStroke(KeyBindingsNames.ID_OBJECT_TREE)));
		menu.add(i18n_createMenuItem("Model View", MainFrameNames.ID_MODEL_VIEW,
				kmgr.getKeyStroke(KeyBindingsNames.ID_MODEL_VIEW)));
		menu.add(i18n_createMenuItem("SQL Editor", MainFrameNames.ID_SQL,
				kmgr.getKeyStroke(KeyBindingsNames.ID_SQL_EDITOR)));
		menu.add(i18n_createMenuItem("Table Properties", MainFrameNames.ID_TABLE_PROPERTIES,
				kmgr.getKeyStroke(KeyBindingsNames.ID_TABLE_PROPERTIES)));
		menu.add(i18n_createMenuItem("System Information", MainFrameNames.ID_DRIVER_INFO,
				kmgr.getKeyStroke(KeyBindingsNames.ID_SYSTEM_INFO)));
		menu.add(i18n_createMenuItem("Switch Connections", MainFrameNames.ID_TOGGLE_CONNECTION,
				kmgr.getKeyStroke(KeyBindingsNames.ID_SWITCH_CONNECTIONS)));

        menu.addSeparator();
        menu.add(i18n_createMenuItem("About", MainFrameNames.ID_ABOUT, null));

		template.add(menu);
		menubar.addApplicationMenu(menu);

		/*
		menu = new MenuDefinition(I18N.getLocalizedMessage("Help"));
		menu.setName("help");
		javax.swing.JMenuItem hitem = i18n_createMenuItem("Help Topics", MainFrameNames.ID_HELP, null);
		com.jeta.foundation.help.HelpUtils.enableHelpOnButton(hitem, MainFrameNames.ID_HELP);
		menu.add(hitem);
		 */

	}

	/**
	 * Creates the status bar for the application
	 */
	private void createStatusBar() {
		m_statusbar = new TSStatusBar();

		m_usercell = new TSCell(ID_USER_CELL, "User: #################");
		m_usercell.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		m_usercell.setText(I18N.format("User_1", ""));
		m_usercell.setIcon(TSGuiToolbox.loadImage("incors/16x16/businessman.png"));
		m_usercell.setBorder(BorderFactory.createCompoundBorder(m_usercell.getBorder(),
				BorderFactory.createEmptyBorder(2, 2, 0, 0)));

		m_statusbar.addCell(m_usercell);

		m_autocommitcell = new TSCell(ID_AUTO_COMMIT_CELL, "Auto Commit: ###########");
		m_autocommitcell.setIcon(TSGuiToolbox.loadImage("incors/16x16/data_into.png"));
		m_autocommitcell.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		m_autocommitcell.setText(I18N.format("Autocommit_1", ""));
		m_autocommitcell.setBorder(BorderFactory.createCompoundBorder(m_autocommitcell.getBorder(),
				BorderFactory.createEmptyBorder(2, 2, 0, 0)));
		m_statusbar.addCell(m_autocommitcell);

		m_urlcell = new TSCell(ID_URL_CELL, "URL: #################################");
		m_urlcell.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		m_urlcell.setText(I18N.format("URL_1", ""));
		m_statusbar.addCell(m_urlcell);

		m_memorycell = new TSCell(ID_MEMORY_CELL, "Memory #######K/###########K");
		m_memorycell.setText("Memory:");
		m_memorycell.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		if (TSUtils.isDebug()) {
			m_memorycell.setMain(true);
			m_statusbar.addCell(m_memorycell);
		} else {
			m_urlcell.setMain(true);
		}

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(m_statusbar, BorderLayout.CENTER);
		if (isInternalFrames()) {
			panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		} else {
			// panel.setBorder( BorderFactory.createLineBorder(
			// java.awt.Color.blue ) );
		}

		getContentPane().add(panel, BorderLayout.SOUTH);
	}

	/**
	 * Creates the main application toolbar
	 */
	protected void createToolBar() {
		TSToolBarTemplate template = this.getToolBarTemplate();
		template.add(i18n_createToolBarButton(MainFrameNames.ID_OBJECT_TREE, "incors/16x16/text_tree.png",
				"Show Object Tree"));
		template.add(i18n_createToolBarButton(MainFrameNames.ID_MODEL_VIEW, "incors/16x16/branch.png",
				"Show Model View"));
		template.add(i18n_createToolBarButton(MainFrameNames.ID_SQL, "incors/16x16/view.png", "Show SQL Editor"));
		template.add(i18n_createToolBarButton(MainFrameNames.ID_QUERY_BUILDER, "incors/16x16/query_builder.png",
				"Query Builder"));
		template.add(i18n_createToolBarButton(MainFrameNames.ID_SECURITY_MGR, "incors/16x16/businessman.png",
				"Users and Groups"));

		template.add(i18n_createToolBarButton(MainFrameNames.ID_TABLE_PROPERTIES, "incors/16x16/table_sql_check.png",
				"Table Properties"));
		template.add(i18n_createToolBarButton(MainFrameNames.ID_LOG, "incors/16x16/scroll.png", "Show Log"));
		template.add(i18n_createToolBarButton(MainFrameNames.ID_DRIVER_INFO, "incors/16x16/information.png",
				"JDBC Driver Info"));

		// create the connection combo box on the toolbar
		m_connectionbox = new JComboBox();
		m_connectionbox.setFocusable(false);
		Dimension d = m_connectionbox.getPreferredSize();
		d.width = TSGuiToolbox.calculateAverageTextWidth(m_connectionbox, 30);
		m_connectionbox.setPreferredSize(d);
		m_connectionbox.setMaximumSize(d);
		m_connectionbox.setMinimumSize(d);

		JLabel clabel = new JLabel(I18N.getLocalizedDialogLabel("Connection") + " ");
		// template.addSeparator();
		template.add(Box.createHorizontalStrut(10));

		template.add(i18n_createToolBarButton(MainFrameNames.ID_CONNECTION_MGR, "incors/16x16/server_client.png",
				"Manage Connections"));
		template.add(i18n_createToolBarButton(MainFrameNames.ID_CONNECT, "incors/16x16/server_connection.png",
				"Connect"));
		template.add(i18n_createToolBarButton(MainFrameNames.ID_DISCONNECT, "incors/16x16/server_forbidden.png",
				"Disconnect"));
		template.add(Box.createHorizontalStrut(10));

		template.add(clabel);
		template.add(m_connectionbox);

		m_connectionbox.setRenderer(new ConnectionCellRenderer());
		m_connectionbox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				MainFrameController controller = (MainFrameController) getController();
				controller.updateComponents(null);
				_setCurrentConnection((TSConnection) m_connectionbox.getSelectedItem());
			}
		});
		template.add(Box.createHorizontalStrut(5));
		template.add(i18n_createToolBarButton(MainFrameNames.ID_TOGGLE_CONNECTION, "incors/16x16/server_into.png",
				"Switch Connection"));

		template.add(Box.createHorizontalStrut(5));

		template.add(new JLabel(I18N.getLocalizedDialogLabel("Path")));
		template.add(Box.createHorizontalStrut(5));

		m_pathbox = new JTextField(15);
		m_pathbox.setEditable(false);
		m_pathbox.setFocusable(false);
		d = m_pathbox.getPreferredSize();
		m_pathbox.setPreferredSize(d);
		m_pathbox.setMaximumSize(d);
		m_pathbox.setMinimumSize(d);
		template.add(m_pathbox);
		template.add(Box.createHorizontalStrut(5));

		template.add(i18n_createToolBarButton(MainFrameNames.ID_SET_CURRENT_SCHEMA, "incors/16x16/folder_cubes.png",
				"Set Current Path"));

		LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
		if (jlm.isEvaluation()) {
			setTitle(I18N.getLocalizedMessage("Abeille Evaluation"));
		}

		template.add(Box.createHorizontalStrut(5));

		/*
		javax.swing.JButton btn = i18n_createToolBarButton(MainFrameNames.ID_HELP, "incors/16x16/help2.png", "Help");
		com.jeta.foundation.help.HelpUtils.enableHelpOnButton(btn, MainFrameNames.ID_HELP);
		template.add(btn);
		 */

	}

	public void detachFrame(TSInternalFrame iframe) {
		// undock. first get location of screen of object tree
		java.awt.Point pt = new java.awt.Point();
		JPanelFrame pframe = (JPanelFrame) iframe.getDelegate();
		javax.swing.SwingUtilities.convertPointToScreen(pt, pframe);
		int x = pt.x - 10;
		int y = pt.y - 10;

		pt.x = pframe.getWidth();
		pt.y = pframe.getHeight();
		javax.swing.SwingUtilities.convertPointToScreen(pt, pframe);
		int width = pt.x - x + 20;
		int height = pt.y - y + 20;

		javax.swing.JTabbedPane tab = getTabbedPane();
		tab.remove(pframe);
		JFrameEx jframe = new JFrameEx(iframe, iframe.getTitle());
		jframe.setFrameIcon(iframe.getFrameIcon());

		UIDirector uidirector = iframe.getUIDirector();
		iframe.setDelegate(jframe);
		iframe.setUIDirector(uidirector);
		iframe.setBounds(x, y, width, height);
		iframe.setVisible(true);

		javax.swing.JMenuBar menubar = jframe.getJMenuBar();
		MenuTemplate menu_template = pframe.getMenuTemplate();
		for (int index = 0; index < menu_template.getMenuCount(); index++) {
			MenuDefinition mdef = menu_template.getMenuAt(index);
			menubar.add(mdef.createMenu());
		}

		JToolBar toolbar = pframe.getToolBar();
		pframe.remove(toolbar);
		jframe.removeToolBar();
		jframe.setToolBar(toolbar);
		jframe.getContentPane().repaint();
		jframe.revalidate();
		jframe.doLayout();

		getContentPane().repaint();
		revalidate();
	}

	/**
	 * Docks or undocks the object tree from this frame.
	 */
	public void dockObjectTree() {
		if (m_object_tree_frame.getDelegate() instanceof JPanelFrame) {
			m_frame_mgr.setObjectTreeFrameDock(false);

			// undock. first get location of screen of object tree
			java.awt.Point pt = new java.awt.Point();
			JPanelFrame pframe = (JPanelFrame) m_object_tree_frame.getDelegate();
			javax.swing.SwingUtilities.convertPointToScreen(pt, pframe);
			int x = pt.x - 10;
			int y = pt.y - 10;

			pt.x = pframe.getWidth();
			pt.y = pframe.getHeight();
			javax.swing.SwingUtilities.convertPointToScreen(pt, pframe);
			int width = pt.x - x + 20;
			int height = pt.y - y + 20;

			m_main_split.remove(pframe);
			m_main_split.remove(m_main_tab);
			getContentPane().remove(m_main_split);
			getContentPane().add(m_main_tab, BorderLayout.CENTER);
			JControlledFrame jframe = new JControlledFrame(m_object_tree_frame, m_object_tree_frame.getTitle());
			jframe.setFrameIcon(m_object_tree_frame.getFrameIcon());
			jframe.setJMenuBar(null);
			jframe.removeToolBar();

			m_object_tree_frame.setDelegate(jframe);

			m_object_tree_frame.setBounds(x, y, width, height);
			m_object_tree_frame.setVisible(true);

			getContentPane().repaint();
			revalidate();
		} else if (m_object_tree_frame.getDelegate() instanceof JFrameEx) {
			m_frame_mgr.setObjectTreeFrameDock(true);

			// make sure to remove the window listener in JFrameEx
			getContentPane().remove(m_main_tab);
			JFrameEx jframe = (JFrameEx) m_object_tree_frame.getDelegate();

			JPanelFrame pframe = new JPanelFrame(m_object_tree_frame, m_object_tree_frame.getTitle());
			m_object_tree_frame.setDelegate(pframe);

			m_main_split.setLeftComponent(null);
			m_main_split.setRightComponent(null);

			m_main_split.setLeftComponent(pframe);
			m_main_split.setRightComponent(m_main_tab);
			Dimension d = pframe.getPreferredSize();
			m_main_split.setDividerLocation(d.width);
			jframe.dispose();
			getContentPane().add(m_main_split, BorderLayout.CENTER);
			m_main_split.revalidate();
			m_main_split.repaint();
			getContentPane().repaint();
			revalidate();
		}
	}

	/**
	 * @return the database connection that has 'focus'.
	 */
	public TSConnection getConnection() {
		return (TSConnection) m_connectionbox.getSelectedItem();
	}

	/**
	 * @return a collection of all frames (TSInternalFrames) in the workspace.
	 *         This includes hidden singleton frames.
	 */
	public Collection getAllFrames(Object windowId) {
		HashSet results = new HashSet();
		Collection singletons = getSingletonFrames();
		Iterator iter = singletons.iterator();
		while (iter.hasNext()) {
			TSInternalFrame iframe = (TSInternalFrame) iter.next();
			if (windowId == null) {
				results.add(iframe);
			} else {
				if (windowId.equals(iframe.getWindowId()))
					results.add(iframe);
			}
		}

		Collection conns = getConnections();
		iter = conns.iterator();
		while (iter.hasNext()) {
			ConnectionContext cc = (ConnectionContext) iter.next();
			Collection frames = cc.getFrames();
			Iterator fiter = frames.iterator();
			while (fiter.hasNext()) {
				TSInternalFrame iframe = (TSInternalFrame) fiter.next();
				if (windowId == null) {
					results.add(iframe);
				} else {
					if (windowId.equals(iframe.getWindowId()))
						results.add(iframe);
				}
			}
		}

		TSInternalFrame[] visframes = getFrameManager().getAllFrames();
		for (int index = 0; index < visframes.length; index++) {
			TSInternalFrame iframe = visframes[index];
			if (windowId == null) {
				results.add(iframe);
			} else {
				if (windowId.equals(iframe.getWindowId()))
					results.add(iframe);
			}
		}
		return results;
	}

	public int getConnectionCount() {
		return m_connections.size();
	}

	/**
	 * Return a collection of ConnectionContext objects that represent the
	 * opened connections
	 */
	public Collection getConnections() {
		return m_connections;
	}

	/**
	 * @return the connection context that is associated with the given
	 *         connection
	 */
	public ConnectionContext getContext(ConnectionId cid) {
		Iterator iter = m_connections.iterator();
		while (iter.hasNext()) {
			ConnectionContext ctx = (ConnectionContext) iter.next();
			if (cid.equals(ctx.getConnection().getId()))
				return ctx;
		}
		return null;
	}

	/**
	 * @return the connection context that has the current 'focus'
	 */
	public ConnectionContext getCurrentContext() {
		TSConnection current = (TSConnection) m_connectionbox.getSelectedItem();
		if (current == null)
			return null;

		Iterator iter = m_connections.iterator();
		while (iter.hasNext()) {
			ConnectionContext ctx = (ConnectionContext) iter.next();
			if (ctx.getConnection() == current)
				return ctx;
		}

		assert (false);
		return null;
	}

	public TSInternalFrame getCurrentFrame() {
		Component comp = m_main_tab.getSelectedComponent();
		if (comp instanceof WindowDelegate) {
			WindowDelegate delegate = (WindowDelegate) comp;
			return delegate.getTSInternalFrame();
		}
		return null;
	}

	/**
	 * @return the one and only LoggerFrame
	 */
	TSInternalFrame getLogFrame() {
		return m_logframe;
	}

	TSInternalFrame getConnectionMgrFrame() {
		return m_connection_mgr_frame;
	}

	/**
	 * @return the one and only object tree frame
	 */
	TSInternalFrame getObjectTreeFrame() {
		return m_object_tree_frame;
	}

	/**
	 * @return the set of currently opened connections (ConnectionContext
	 *         objects)
	 */
	public Collection getOpenedConnections() {
		return m_connections;
	}

	/**
	 * The main split pane
	 */
	JSplitPane getSplitPane() {
		return m_main_split;
	}

	/**
	 * The main tabbed pane
	 */
	JTabbedPane getTabbedPane() {
		return m_main_tab;
	}

	/**
	 * @return an initial X position for child windows
	 */
	private int getXOffset() {
		if (isInternalFrames())
			return 0;
		else
			return getX();
	}

	/**
	 * @return an initial Y position for child windows
	 */
	private int getYOffset() {
		if (isInternalFrames())
			return 0;
		else
			return getY() + getHeight();
	}

	boolean isConnectionOriented(TSInternalFrame iframe) {
		return (!(iframe instanceof LoggerFrame) && !(iframe instanceof ConnectionMgrFrame) && !(iframe instanceof ObjectTreeFrame));
	}

	/**
	 * Return true if the file is opened in the workspace.
	 */
	public boolean isDocumentOpened(java.io.File f) {
		Collection frames = getAllFrames(null);
		Iterator fiter = frames.iterator();
		while (fiter.hasNext()) {
			Object frame = fiter.next();
			if (frame instanceof DocumentFrame) {
				if (f.equals(((DocumentFrame) frame).getCurrentDocument()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the object tree frame is visible
	 */
	public boolean isObjectTreeVisible() {
		TSInternalFrame ot_frame = getObjectTreeFrame();
		if (ot_frame.getDelegate() instanceof JFrame) {
			return ot_frame.isVisible();
		} else {
			return (getContentPane() == m_main_split.getParent());
		}
	}

	public void showObjectTree(boolean bShow) {
		if (m_object_tree_frame.getDelegate() instanceof JFrameEx) {
			if (bShow)
				show(m_object_tree_frame);
			else {
				m_object_tree_frame.setVisible(true);
				closeWindow(m_object_tree_frame);
			}
		} else if (m_object_tree_frame.getDelegate() instanceof JPanelFrame) {
			JPanelFrame pframe = (JPanelFrame) m_object_tree_frame.getDelegate();
			if (bShow) {
				if (!isObjectTreeVisible()) {
					getContentPane().remove(m_main_tab);

					m_main_split.remove(m_main_tab);
					m_main_split.remove(pframe);

					m_main_split.setLeftComponent(null);
					m_main_split.setRightComponent(null);

					m_main_split.setLeftComponent(pframe);
					m_main_split.setRightComponent(m_main_tab);
					Dimension d = pframe.getPreferredSize();
					m_main_split.setDividerLocation(d.width);
					getContentPane().add(m_main_split, BorderLayout.CENTER);
					m_main_split.revalidate();
					m_main_split.repaint();
					getContentPane().repaint();
					revalidate();
				}
			} else {
				if (isObjectTreeVisible()) {
					m_main_split.remove(pframe);
					m_main_split.remove(m_main_tab);
					m_main_split.setLeftComponent(null);
					m_main_split.setRightComponent(null);

					m_main_split.revalidate();
					getContentPane().remove(m_main_split);
					getContentPane().add(m_main_tab, BorderLayout.CENTER);
					getContentPane().repaint();
					revalidate();
				}
			}
		}
	}

	private void _setCurrentConnection(TSConnection tsconn) {
		if (tsconn == null)
			return;

		ConnectionContext ctx = getContext(m_last_cid);
		if (ctx != null)
			ctx.clearFrames();

		if (ctx != null) {
			Object current_tab = m_main_tab.getSelectedComponent();
			if (current_tab != null)
				ctx.setLastFrame(((WindowDelegate) current_tab).getTSInternalFrame());
			else
				ctx.setLastFrame(null);
		}

		int total = m_main_tab.getTabCount();
		for (int index = 0; index < total; index++) {
			Object tabobj = m_main_tab.getComponentAt(index);
			TSInternalFrame iframe = ((WindowDelegate) tabobj).getTSInternalFrame();
			if (isConnectionOriented(iframe)) {
				m_main_tab.remove(index);
				total--;
				index--;
				ConnectionId cid = (ConnectionId) iframe.getWindowId();
				if (cid.equals(m_last_cid) && ctx != null) {
					ctx.addFrame(iframe);
				}
			}
		}

		ctx = getContext(tsconn.getId());
		if (ctx != null) {
			Collection frames = ctx.getFrames();
			Iterator iter = frames.iterator();
			while (iter.hasNext()) {
				TSInternalFrame iframe = (TSInternalFrame) iter.next();
				m_main_tab.addTab(iframe.getShortTitle(), iframe.getFrameIcon(), (JPanelFrame) iframe.getDelegate());
			}

			TSInternalFrame sel_frame = ctx.getLastFrame();
			if (sel_frame != null) {
				for (int index = 0; index < m_main_tab.getTabCount(); index++) {
					Object tabobj = m_main_tab.getComponentAt(index);
					TSInternalFrame iframe = ((WindowDelegate) tabobj).getTSInternalFrame();
					if (sel_frame.getClass() == iframe.getClass()) {
						m_main_tab.setSelectedIndex(index);
						break;
					}
				}

			}
		}

		m_last_cid = tsconn.getId();

		updateConnectionStatus();
		
		if ( tsconn.isPROD() )  {
			m_main_tab.setForeground(Color.red);
		} else {
			m_main_tab.setForeground(Color.black);
		}	
	}

	public void setCurrentConnection(TSConnection tsconn) {
		if (!tsconn.equals(getConnection())) {
			m_connectionbox.setSelectedItem(tsconn);
		}
	}

	/**
	 * Sets the frame state for a given internal frame
	 */
	void setInternalFrameState(TSInternalFrame iframe, FrameState fstate, Rectangle defaultBounds) {
		Dimension desktopdims = getWorkspaceSize();

		if (fstate != null) {
			Rectangle rect = fstate.getBounds();
			if (rect.width < 50 || rect.height < 50 || rect.x < 0 || rect.y < 0) {
				TSUtils.printDebugMessage("frame state invalid for " + iframe.getClass());
				TSUtils.printDebugMessage(rect.toString());
				fstate = null;
			}

		}

		if (fstate == null) {
			Rectangle frame_bounds = calculateFrameBounds(desktopdims, null, defaultBounds, false);
			TSUtils.printMessage("frame bounds: " + frame_bounds);
			iframe.setBounds(frame_bounds);
		} else {
			try {
				Rectangle frame_bounds = calculateFrameBounds(desktopdims, fstate.getBounds(), defaultBounds, false);
				iframe.setBounds(frame_bounds);

				// if ( fstate.isIconified() )
				// iframe.setIcon( true );
				// else if ( fstate.isMaximum() )
				// iframe.setMaximum( true );
			} catch (Exception e) {
				Rectangle frame_bounds = calculateFrameBounds(desktopdims, fstate.getBounds(), defaultBounds, false);
				iframe.setBounds(frame_bounds);
			}
		}
	}

	/**
	 * Sets the memory status cell in the status bar
	 */
	void setMemoryStatus(String str) {
		m_memorycell.setText(str);
	}

	/**
	 * This method is used to show a singleton frame. Specialized workspaces
	 * should override this method to provide custom behavior. This is mainly
	 * used to provide deferred creation/loading for frames until they are
	 * explicitly requested by the user.
	 * 
	 * @param params
	 *            this must be a TSConnection object that will be associated
	 *            with the frame if it must be created.
	 */
	public TSInternalFrame show(Class frameClass, Object param) {
		TSConnection conn = (TSConnection) param;
		ConnectionContext ctx = getContext(conn.getId());
		if (ctx != null) {
			TSInternalFrame iframe = ctx.getFrame(frameClass);
			if (iframe == null) {
				if (frameClass == com.jeta.abeille.gui.table.TableFrame.class
						|| frameClass == com.jeta.abeille.gui.procedures.ProcedureFrame.class
						|| frameClass == com.jeta.abeille.gui.security.SecurityMgrFrame.class
						|| frameClass == com.jeta.abeille.gui.help.SQLReferenceFrame.class
						|| frameClass == com.jeta.abeille.gui.main.SystemInfoFrame.class) {
					ConnectionId cid = conn.getId();
					assert (cid != null);
					iframe = createInternalFrame(frameClass, true, conn.getId());
					Object[] params = new Object[1];
					params[0] = conn;

					iframe.initializeModel(params);
					ctx.setFrame(frameClass, iframe);

					centerWindow(iframe);
					addWindow(iframe);
				}
			}

			if (iframe != null) {
				show(iframe);
			}
			return iframe;
		}
		return null;
	}

	/**
	 * Shuts down this frame. Saves the frame state to the object store and
	 * Closes all open windows. Tells ComponentMgr to initiate application
	 * shutdown.
	 */
	void shutDown() {
		try {
			setExiting(true);
			Collection connections = (Collection) m_connections.clone();
			Iterator iter = connections.iterator();
			while (iter.hasNext()) {
				ConnectionContext ctx = (ConnectionContext) iter.next();
				if (!closeConnection(ctx)) {
					setExiting(false);
					return;
				}
			}

			FrameState mstate = new FrameState(this);
			ObjectStore os = (ObjectStore) ComponentMgr.lookup(ComponentNames.APPLICATION_STATE_STORE);
			os.store(MAIN_FRAME_STATE, mstate);

			FrameState lstate = new FrameState(m_object_tree_frame);
			lstate.setVisible(isObjectTreeVisible());
			os.store(OBJECT_TREE_FRAME_STATE, lstate);

		} catch (Exception e) {
			e.printStackTrace();
		}

		disposeAll(); // close all open frame windows. this allows them to save
						// their state
		ComponentMgr.shutdown();
	}

	/**
	 * Updates the current schema in the schema box
	 */
	void updateCurrentPath() {
		TSConnection connection = getConnection();
		if (connection == null) {
			m_pathbox.setText("");
		} else {
			Schema schema = connection.getCurrentSchema();
			Catalog catalog = connection.getCurrentCatalog();
			m_pathbox.setText(DbUtils.getQualifiedName(catalog, schema));
		}
	}

	/**
	 * Sets the connection status cell in the status bar
	 */
	void updateConnectionStatus() {
		updateCurrentPath();

		Catalog cat = null;
		TSConnection connection = getConnection();

		if (connection != null) {
			cat = connection.getCurrentCatalog();
		}

		if (cat == null) {
			m_urlcell.setText(I18N.format("URL_1", ""));
			m_usercell.setText(I18N.format("User_1", ""));
			m_autocommitcell.setText(I18N.format("Autocommit_1", ""));
		} else {
			ConnectionInfo cinfo = connection.getConnectionInfo();
			m_urlcell.setText(I18N.format("URL_1", cinfo.getUrl()));
			m_usercell.setText(I18N.format("User_1", cinfo.getUserName()));
		}

		try {
			String result = null;
			if (connection != null) {
				if (connection.isAutoCommit())
					result = I18N.getLocalizedMessage("On");
				else
					result = I18N.getLocalizedMessage("Off");

				m_autocommitcell.setText(I18N.format("Autocommit_1", result));
			}
		} catch (java.sql.SQLException se) {
			TSUtils.printException(se);
		}
	}

	/**
	 * Connection box renderer
	 */
	class ConnectionCellRenderer extends JLabel implements ListCellRenderer {
		public ConnectionCellRenderer() {
			// must set or the background color won't show
			setOpaque(true);
			setFont(UIManager.getFont("List.font"));
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			TSConnection connection = (TSConnection) value;
			if (connection == null) {
				setText("");
				setIcon(null);
			} else {
				setText(connection.getDescription() + ":" + connection.getUser());
				setIcon(com.jeta.abeille.gui.model.ObjectTreeRenderer.m_servericon);
			}

			if (isSelected) {
				setBackground(UIManager.getColor("List.selectionBackground"));
				setForeground(UIManager.getColor("List.selectionForeground"));
			} else {
				setBackground(UIManager.getColor("List.background"));
				setForeground(UIManager.getColor("List.foreground"));
			}

			return this;
		}
	}

	/**
	 * LayoutManager for our toolbar. This is mainly to resize the status bar on
	 * the toolbar
	 */
	public class ToolBarLayout extends BoxLayout {
		ToolBarLayout(JToolBar toolBar) {
			super(toolBar, BoxLayout.X_AXIS);
		}

		public void layoutContainer(java.awt.Container target) {
			super.layoutContainer(target);

			Dimension d = m_statusbar.getSize();
			Dimension pd = m_statusbar.getPreferredSize();
			d.height = pd.height;

			Dimension toolbard = target.getSize();
			d.width = toolbard.width - m_statusbar.getX() - 10;
			m_statusbar.setSize(d);

			java.awt.Point pt = m_statusbar.getLocation();
			pt.y = m_connectionbox.getY() + (m_connectionbox.getHeight() - d.height) / 2;
			m_statusbar.setLocation(pt);
		}
	}
}
