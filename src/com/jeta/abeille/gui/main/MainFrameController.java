package com.jeta.abeille.gui.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;
import java.text.DecimalFormat;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.database.model.DbModelEvent;
import com.jeta.abeille.database.model.DbModelListener;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSConnectionMgr;

import com.jeta.abeille.gui.login.ConnectionMgr;
import com.jeta.abeille.gui.login.ConnectionMgrDialog;
import com.jeta.abeille.gui.login.LoginDialog;
import com.jeta.abeille.gui.login.LoginView;

import com.jeta.abeille.gui.main.options.SetPathView;
import com.jeta.abeille.gui.main.options.SetPathViewValidator;

import com.jeta.abeille.gui.model.ModelViewFrame;
import com.jeta.abeille.gui.query.QueryBuilderFrame;

import com.jeta.abeille.gui.store.ConnectionContext;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;
import com.jeta.foundation.componentmgr.TSNotifier;
import com.jeta.foundation.componentmgr.TSListener;
import com.jeta.foundation.componentmgr.TSEvent;
import com.jeta.foundation.gui.components.JPanelFrame;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.components.WindowDelegate;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.interfaces.resources.ResourceLoader;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.gui.utils.JETAToolbox;

/**
 * This is the controller object for the main frame window of the application.
 * It handles all user input for menus and toolbars and other components.
 * 
 * @author Jeff Tassin
 */
public class MainFrameController extends TSController implements TSListener, DbModelListener {
	private MainFrame m_frame;

	public MainFrameController(MainFrame frame) {
		super(frame);
		m_frame = frame;
		assignAction(MainFrameNames.ID_GC, new GarbageCollectAction());
		assignAction(MainFrameNames.ID_EXIT, new ExitAction());
		assignAction(MainFrameNames.ID_OBJECT_TREE, new ShowObjectTreeAction());
		assignAction(MainFrameNames.ID_MODEL_VIEW, new ShowModelViewAction());
		assignAction(MainFrameNames.ID_SQL, new ShowSQLEditorAction());
		assignAction(MainFrameNames.ID_QUERY_BUILDER, new NewQueryAction());
		assignAction(MainFrameNames.ID_SECURITY_MGR, new ShowSecurityMgrAction());
		assignAction(MainFrameNames.ID_TABLE_PROPERTIES, new ShowTableProperties());
		assignAction(MainFrameNames.ID_LOG, new ShowLogViewAction());
		assignAction(MainFrameNames.ID_DRIVER_INFO, new SystemInfoAction());
		assignAction(MainFrameNames.ID_CONNECT, new ConnectAction());
		assignAction(MainFrameNames.ID_DISCONNECT, new DisconnectAction());
		assignAction(MainFrameNames.ID_CONNECTION_MGR, new ConnectionMgrAction());
		assignAction(MainFrameNames.ID_SET_CURRENT_SCHEMA, new SetPathAction());
		assignAction(MainFrameNames.ID_TOGGLE_CONNECTION, new NextConnectionAction());

		assignAction(MainFrameNames.ID_USER_PREFERENCES, new UserPreferencesAction());

		assignAction(MainFrameNames.ID_CLOSE_WINDOW, new CloseWindowAction());
		assignAction(MainFrameNames.ID_TILE_WINDOWS, new TileWindowsAction());
		assignAction(MainFrameNames.ID_ABOUT, new AboutAction());

		TSNotifier n = TSNotifier.getInstance(TSConnectionMgr.COMPONENT_ID);
		n.registerInterest(this, TSConnectionMgr.MSG_GROUP);

		ActionListener memory_updater = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Runtime rt = Runtime.getRuntime();
				long total = rt.totalMemory();
				long allocated = total - rt.freeMemory();

				total = total / 1000;
				allocated = allocated / 1000;

				DecimalFormat format = new DecimalFormat();
				String memstr = I18N.format("memory_status_2", new Long(allocated), new Long(total));

				if (TSUtils.isDebug()) {
					Object e = ComponentMgr.lookup(ComponentNames.ID_DEBUG_EXCEPTION_FLAG);
					if (e != null) {
						memstr = memstr + " (Exception)";
					}
				}
				m_frame.setMemoryStatus(memstr);
			}
		};

		m_frame.getTabbedPane().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				WindowDelegate window = (WindowDelegate) m_frame.getTabbedPane().getSelectedComponent();
				if (window != null) {
					m_frame.activateInternalFrame(window.getTSInternalFrame());
					m_frame.activateInternalMenu(window.getMenuTemplate());
					window.getTSInternalFrame().requestFocus();
				}
			}
		});

		int delay = 5000; // milliseconds
		new javax.swing.Timer(delay, memory_updater).start();

		MainFrameUIDirector uidirector = new MainFrameUIDirector(m_frame);
		m_frame.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Called when we get an event from the DbModel
	 */
	public void eventFired(DbModelEvent evt) {
		if (evt.getID() == DbModelEvent.STATUS_UPDATE) {
			m_frame.updateConnectionStatus();
		}
	}

	/**
	 * @return the database connection that has 'focus'.
	 */
	public TSConnection getConnection() {
		return m_frame.getConnection();
	}

	/**
	 * @return the default look and feel for the application. The following OSes
	 *         will have the given default look and feel: Linux Java Metal
	 *         Windows Windows Apple Apple Other Java Metal
	 */
	public static String getDefaultLookAndFeel() {
		return MainFrameNames.ID_JAVA_LF;
	}

	/**
	 * Renders a connection in the frame window
	 */
	public void renderConnection(TSConnection connection) {
		m_frame.createEnvironment(connection);
		TSNotifier n = TSNotifier.getInstance(connection.getId().getUID());
		n.registerInterest(this, TSConnection.COMPONENT_ID);
		updateConnectionCount();
		connection.addModelListener(this);
		connection.fireStatusUpdate(connection.getDefaultCatalog());
	}

	/** Sets the look and feel for the application based on user settings */
	public static void setLookAndFeel() {
		// get the application state store
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		try {
			assert (userprops != null);
			String lf = userprops.getProperty(MainFrameNames.ID_LOOK_AND_FEEL, getDefaultLookAndFeel());
			MainFrameController.setLookAndFeel(null, lf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** sets the look and feel for the given frame and look an feel class name */
	public static void setLookAndFeel(MainFrame frame, String lookandfeel) {
		try {
			TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
			userprops.setProperty(MainFrameNames.ID_LOOK_AND_FEEL, lookandfeel);

			if (lookandfeel == null) {
				lookandfeel = getDefaultLookAndFeel();
			}
			// standard look and feel
			UIManager.setLookAndFeel(lookandfeel);

			if (frame != null) {
				frame.updateUI();
			}

			MainFrameController.updateLookAndFeel(frame);
			if (frame != null) {
				TSInternalFrame iframe = frame.getLogFrame();
				if (iframe != null)
					iframe.updateUI();

				Collection frames = frame.getAllFrames(null);
				Iterator iter = frames.iterator();
				while (iter.hasNext()) {
					iframe = (TSInternalFrame) iter.next();
					if (iframe.getDelegate() instanceof javax.swing.JFrame)
						MainFrameController.updateLookAndFeel(iframe.getDelegate());
					else if (iframe.getDelegate() instanceof JPanelFrame) {
						MenuTemplate template = iframe.getMenuTemplate();
						for (int index = 0; index < template.getMenuCount(); index++) {
							MenuDefinition mdef = template.getMenuAt(index);
							for (int ii = 0; ii < mdef.getItemCount(); ii++) {
								Object obj = mdef.getItem(ii);
								if (obj instanceof Component) {
									updateLookAndFeel((Component) obj);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * TSListener implementation. Called by TSNotifier for connect/disconnect
	 * events.
	 */
	public void tsNotify(TSEvent evt) {
		if (evt.getGroup().equals(TSConnectionMgr.MSG_GROUP)) {
			if (evt.getMessage().equals(TSConnectionMgr.MSG_CONNECTION_CREATED)) {
				TSConnection connection = (TSConnection) evt.getSender();
				renderConnection(connection);
			}
		} else if (evt.getGroup().equals(TSConnection.COMPONENT_ID)) {
			// just update the connection count
			updateConnectionCount();
		}

	}

	/**
	 * Updates the number of opened connections in the statusbar on the main
	 * frame
	 */
	void updateConnectionCount() {
		Runnable gui_updater = new Runnable() {
			public void run() {
				ConnectionContext ctx = m_frame.getCurrentContext();
				m_frame.updateConnectionStatus();
			}
		};

		SwingUtilities.invokeLater(gui_updater);
	}

	/**
	 * Updates the look and feel for all components in the application
	 * 
	 */
	protected static void updateLookAndFeel(Component c) {
		if (c == null)
			return;

		try {
			if (c instanceof JComponent) {
				if (!(c instanceof com.jeta.foundation.gui.editor.TSEditorPane))
					((JComponent) c).updateUI();
			}
			Component[] children = null;
			if (c instanceof JMenu) {
				children = ((JMenu) c).getMenuComponents();
			} else if (c instanceof Container) {
				children = ((Container) c).getComponents();
			}
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					updateLookAndFeel(children[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** ------------------ actions --------------------- */
	public class AboutAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
			JPanel panel = new JPanel(new BorderLayout());
			AboutView2 view = new AboutView2();
			dlg.showCloseLink();
			panel.add(view, BorderLayout.NORTH);

			try {
				// javax.swing.JEditorPane editor = new
				// javax.swing.JEditorPane();
				// editor.setEditorKit( new
				// javax.swing.text.html.HTMLEditorKit() );

				// java.net.URL url = ClassLoader.getSystemResource(
				// "com/jeta/abeille/resources/help/credits.htm" );
				ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
				ClassLoader classloader = loader.getClassLoader();
				java.net.URL url = classloader.getResource("com/jeta/abeille/resources/help/credits.htm");

				javax.swing.JEditorPane editor = (javax.swing.JEditorPane) view.getComponentByName("credits");
				editor.setPage(url);
				editor.setEditable(false);
				// panel.add( new javax.swing.JScrollPane(editor),
				// BorderLayout.CENTER );
			} catch (Exception e) {
				TSUtils.printException(e);
			}
			// panel.add( javax.swing.Box.createVerticalStrut(20),
			// BorderLayout.SOUTH );
			// Dimension d2 = TSGuiToolbox.getWindowDimension(1,8);
			// d.height += d2.height;

			dlg.setPrimaryPanel(panel);
			// TSGuiToolbox.setReasonableWindowSize( dlg, d );
			dlg.setSize(dlg.getPreferredSize());
			dlg.setTitle(I18N.getLocalizedMessage("About"));
			dlg.setResizable(false);
			dlg.showCenter();
		}
	}

	/**
	 * Launches the login dialog which allows the user to connect to a database
	 */
	public class ConnectAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ConnectionMgr cmgr = new ConnectionMgr();
			LoginDialog dlg = new LoginDialog(m_frame, true);
			dlg.initialize(cmgr);
			dlg.setSize(dlg.getPreferredSize());
			TSGuiToolbox.centerWindow(dlg);
			LoginView view = dlg.getView();
			view.setController(new MainFrameLoginController(m_frame, view));
			dlg.showCenter();
			// the LoginController does the rest
		}
	}

	/**
	 * Creates/Defines a new database connection
	 */
	public class ConnectionMgrAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.show(m_frame.getConnectionMgrFrame());
		}
	}

	/**
	 * Cascades the windows in the workspace
	 */
	public class CloseWindowAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSInternalFrame frame = m_frame.getCurrentFrame();
			if (frame != null) {
				m_frame.closeWindow(frame);
			}
		}
	}

	/**
	 * Closes the current connection and all associated windows with that
	 * connection
	 */
	public class DisconnectAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ConnectionContext ctx = m_frame.getCurrentContext();
			if (ctx != null) {
				String title = I18N.getLocalizedMessage("Confirm");
				String msg = I18N.format("Close_the_connection_1", ctx.getConnection().getDescription());
				int nresult = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
				if (nresult == JOptionPane.YES_OPTION) {
					TSConnection conn = ctx.getConnection();
					if (m_frame.closeConnection(ctx)) {
						TSConnectionMgr.closeConnection(ctx.getConnection());
					}
				}
			}
			updateConnectionCount();
		}
	}

	/**
	 * Invokes a dialog that shows the JDBC information
	 */
	public class SystemInfoAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ConnectionContext ctx = m_frame.getCurrentContext();
			if (ctx != null) {

				m_frame.show(SystemInfoFrame.class, ctx.getConnection());
			}
		}
	}

	/**
	 * Exits the application
	 */
	public class ExitAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.shutDown();
		}
	}

	/**
	 * Forces the garbage collector to run
	 */
	public class GarbageCollectAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			System.gc();
		}
	}

	/**
	 * Invoked when the user clicks the new query menu item.
	 */
	public class NewQueryAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSConnection conn = m_frame.getConnection();
			if (conn != null) {
				Collection frames = m_frame.getAllFrames(conn.getId());
				Iterator iter = frames.iterator();
				while (iter.hasNext()) {
					TSInternalFrame iframe = (TSInternalFrame) iter.next();
					if (iframe instanceof QueryBuilderFrame) {
						m_frame.show(iframe);
						return;
					}
				}
			}
			TSInternalFrame iframe = m_frame.createInternalFrame(QueryBuilderFrame.class, false, conn.getId());
			Object[] params = new Object[1];
			params[0] = conn;
			iframe.initializeModel(params);
			m_frame.addWindow(iframe);
		}
	}

	/**
	 * Selects the next conneciton
	 */
	public class NextConnectionAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSConnection conn = m_frame.getConnection();
			if (conn != null) {
				ConnectionContext first_conn = null;
				Collection conns = m_frame.getConnections();
				Iterator iter = conns.iterator();
				while (iter.hasNext()) {
					ConnectionContext nextconn = (ConnectionContext) iter.next();
					if (first_conn == null)
						first_conn = nextconn;

					if (conn.equals(nextconn.getConnection())) {
						if (iter.hasNext())
							nextconn = (ConnectionContext) iter.next();
						else
							nextconn = first_conn;

						m_frame.setCurrentConnection(nextconn.getConnection());
						break;
					} else if (!iter.hasNext()) {
						m_frame.setCurrentConnection(first_conn.getConnection());
						break;
					}
				}
			}
		}
	}

	/**
	 * Invokes a dialog that allows the user to set the current path for the
	 * current connection.
	 */
	public class SetPathAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSConnection conn = m_frame.getConnection();
			if (conn != null) {
				SetPathView view = new SetPathView(conn);
				TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
				dlg.addValidator(view, new SetPathViewValidator());
				dlg.setPrimaryPanel(view);
				dlg.setSize(dlg.getPreferredSize());
				dlg.setTitle(I18N.getLocalizedMessage("Set Current Path"));
				dlg.showCenter();
				if (dlg.isOk()) {
					try {
						Schema schema = view.getSchema();
						Catalog catalog = view.getCatalog();

						conn.setCurrentCatalog(catalog);
						conn.setCurrentSchema(catalog, schema);

						assert (catalog.equals(conn.getCurrentCatalog()));
						assert (schema.equals(conn.getCurrentSchema(catalog)));
						m_frame.updateCurrentPath();
					} catch (SQLException e) {
						TSUtils.printException(e);
					}
				}
			}
		}
	}

	/**
	 * Shows/brings to front the Log View
	 */
	public class ShowLogViewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSInternalFrame iframe = m_frame.getLogFrame();
			if (iframe != null)
				m_frame.show(iframe);
		}
	}

	/**
	 * Shows/brings to front the Model View window
	 */
	public class ShowModelViewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSConnection conn = m_frame.getConnection();
			if (conn != null) {
				Collection frames = m_frame.getAllFrames(conn.getId());
				Iterator iter = frames.iterator();
				while (iter.hasNext()) {
					TSInternalFrame iframe = (TSInternalFrame) iter.next();
					if (iframe instanceof ModelViewFrame) {
						m_frame.show(iframe);
						return;
					}
				}
			}
			TSInternalFrame iframe = m_frame.createInternalFrame(ModelViewFrame.class, false, conn.getId());
			Object[] params = new Object[1];
			params[0] = conn;
			iframe.initializeModel(params);
			m_frame.addWindow(iframe);
		}
	}

	/**
	 * Shows/brings to front the Object Tree window
	 */
	public class ShowObjectTreeAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.showObjectTree(true);
		}
	}

	/**
	 * Shows/brings to front the SQL Editor window
	 */
	public class ShowSQLEditorAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ConnectionContext ctx = m_frame.getCurrentContext();
			if (ctx != null)
				m_frame.show(ctx.getFrame(com.jeta.abeille.gui.sql.SQLFrame.class));
		}
	}

	/**
	 * Invokes the security mgr for the current connnection
	 */
	public class ShowSecurityMgrAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.show(com.jeta.abeille.gui.security.SecurityMgrFrame.class, m_frame.getConnection());
		}
	}

	/**
	 * Invokes the table properties dialog for the current connnection
	 */
	public class ShowTableProperties implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.show(com.jeta.abeille.gui.table.TableFrame.class, m_frame.getConnection());
		}
	}

	/**
	 * Tiles the windows in the workspace
	 */
	public class TileWindowsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			// TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
			// wsframe.tileWindows();
		}
	}

	/**
	 * Invokes the user preferences dialog
	 */
	public class UserPreferencesAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ConnectionContext ctx = m_frame.getCurrentContext();
			TSConnection conn = null;
			if (ctx != null) {
				conn = ctx.getConnection();
			}

			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
			AbeillePreferencesView view = new AbeillePreferencesView(conn);

			/**
			 * each preferences validator for Abeille must take 2 parameters.
			 * The first parameter is the current connection. The second
			 * parameter is the view for those Preferences
			 */
			dlg.addValidator(conn, view.getValidator());
			dlg.setPrimaryPanel(view);
			dlg.setSize(dlg.getPreferredSize());
			dlg.setTitle(I18N.getLocalizedMessage("Preferences"));
			dlg.showCenter();
			if (dlg.isOk())
				view.save();
		}

	}

}
