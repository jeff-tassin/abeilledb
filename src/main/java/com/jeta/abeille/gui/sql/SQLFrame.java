package com.jeta.abeille.gui.sql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.InternalFrameFocusTraversalPolicy;

import com.jeta.abeille.database.model.DbModelEvent;
import com.jeta.abeille.database.model.DbModelListener;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.gui.editor.Buffer;
import com.jeta.foundation.gui.editor.BufferEvent;
import com.jeta.foundation.gui.editor.BufferMgr;
import com.jeta.foundation.gui.editor.EditorController;
import com.jeta.foundation.gui.editor.EditorFrame;
import com.jeta.foundation.gui.editor.FrameKit;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.editor.TSTextNames;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This is the Frame window for the sql editor. It contains the SQLEditor window
 * and the results window.
 * 
 * @author Jeff Tassin
 */
public class SQLFrame extends EditorFrame implements DbModelListener {
	/** the frame icon for this frame */
	public static ImageIcon FRAME_ICON;

	/** the underlying database connection */
	private TSConnection m_connection;

	private SQLHistory m_history = new SQLHistory(Integer.parseInt(SQLDefaultSettings.SQLHISTORY));

	private static final int TOOLBAR_RUNSQL_INDEX = 7; // index where we put
														// SQLFrame specific
														// toolbar buttons

	/** the frame icon for this frame */
	private static ImageIcon[] m_buffericons = new ImageIcon[9];

	static {
		m_buffericons[0] = TSGuiToolbox.loadImage("query1_16.gif");
		m_buffericons[1] = TSGuiToolbox.loadImage("query2_16.gif");
		m_buffericons[2] = TSGuiToolbox.loadImage("query3_16.gif");
		m_buffericons[3] = TSGuiToolbox.loadImage("query4_16.gif");
		m_buffericons[4] = TSGuiToolbox.loadImage("query5_16.gif");
		m_buffericons[5] = TSGuiToolbox.loadImage("query6_16.gif");
		m_buffericons[6] = TSGuiToolbox.loadImage("query7_16.gif");
		m_buffericons[7] = TSGuiToolbox.loadImage("query8_16.gif");
		m_buffericons[8] = TSGuiToolbox.loadImage("query9_16.gif");

		FRAME_ICON = TSGuiToolbox.loadImage("incors/16x16/view.png");

	}

	/**
	 * Constructor
	 */
	public SQLFrame() {
		setTitle("SQL");
		setFrameIcon(FRAME_ICON);
	}

	/**
	 * Override so we can set the tab icon if the buffer is a SQL buffer
	 */
	public void addBuffer(Buffer buffer) {
		if (buffer instanceof SQLBuffer) {
			JTabbedPane tabpane = getTabbedPane();

			Container panel = createContainer(buffer);
			int pos = 0;
			for (int index = 0; index < tabpane.getTabCount(); index++) {
				Buffer buff = getBuffer(index);
				if (buff instanceof SQLBuffer) {
					pos++;
				} else {
					pos = index;
				}
			}
			tabpane.insertTab(buffer.getTitle(), null, panel, null, pos);

			selectBuffer(buffer);
		} else {
			super.addBuffer(buffer);
		}
		syncTabs();

	}

	/**
	 * Event that occurs when a buffer has changed
	 */
	public void bufferChanged(BufferEvent evt) {
		if (evt.getID() == BufferEvent.BUFFER_CHANGED && (evt.getBuffer() instanceof SQLBuffer)) {
			// eat the event
		} else
			super.bufferChanged(evt);
	}

	/**
	 * Override EditorFrame so we can provide a JLayeredPane as our editor
	 * container. We do this so we can popup a button panel on a top layer for
	 * canceling queries and displaying status
	 * 
	 * @param buffer
	 *            the editor buffer
	 * @return the container with the bufferPane added to it. The default
	 *         implementation simply returns the bufferPanel
	 */
	public Container createContainer(Buffer buffer) {
		/*
		 * JLayeredPane lp = new JLayeredPane( ); lp.setLayout( new
		 * SQLLayoutManager() ); lp.add( buffer.getContentPanel(), new
		 * Integer(0) );
		 * 
		 * JPanel statuspanel = new JPanel( new GridBagLayout() );
		 * statuspanel.setOpaque( false ); if ( buffer instanceof SQLBuffer ) {
		 * SQLBuffer sqlbuff = (SQLBuffer)buffer; // this basically centers the
		 * component vertically and horizontally GridBagConstraints c = new
		 * GridBagConstraints(); statuspanel.add( sqlbuff.getStatusComponent(),
		 * c ); } lp.add( statuspanel, new Integer(1) ); return lp;
		 */
		return buffer.getContentPanel();
	}

	/**
	 * Override if you wish to provide a specialized implementation of
	 * EditorController
	 */
	protected EditorController createController(BufferMgr buffMgr) {
		return new SQLController(m_connection, this, buffMgr);
	}

	/**
	 * Create the specialized menus for this frame. In addition to adding the
	 * standard text editor menus, we also add the SQL specific menus.
	 */
	protected void createMenu() {
		MenuTemplate template = getMenuTemplate();

		MenuDefinition filemenu = new MenuDefinition(I18N.getLocalizedMessage("File"));
		filemenu.setName("file");
		filemenu.add(i18n_createMenuItem("New Buffer", FrameKit.newBufferAction, null, null));
		filemenu.add(i18n_createMenuItem("New SQL", SQLKit.newSQLBuffer, null, null));
		filemenu.add(i18n_createMenuItem("Open", FrameKit.openFileAction, null, null));
		filemenu.add(i18n_createMenuItem("Open Into SQL", SQLKit.openFileIntoSQLBufferAction, null, null));
		filemenu.add(i18n_createMenuItem("Open New SQL", SQLKit.openFileAsNewSQLBufferAction, null, null));
		filemenu.add(i18n_createMenuItem("Save", FrameKit.saveFileAction, null, null));
		filemenu.add(i18n_createMenuItem("Save As", FrameKit.saveAsAction, null, null));
		filemenu.add(i18n_createMenuItem("Close Buffer", FrameKit.closeFileAction, null, null));

		template.add(filemenu);

		MenuDefinition editmenu = new MenuDefinition(I18N.getLocalizedMessage("Edit"));
		editmenu.setName("edit");

		editmenu.add(i18n_createMenuItem("Cut", TSTextNames.ID_CUT, null, null));
		editmenu.add(i18n_createMenuItem("Copy", TSTextNames.ID_COPY, null, null));
		editmenu.add(i18n_createMenuItem("Paste", TSTextNames.ID_PASTE, null, null));
		editmenu.add(i18n_createMenuItem("Delete", TSTextNames.ID_DELETE, null, null));
		editmenu.add(i18n_createMenuItem("Select All", TSTextNames.ID_SELECT_ALL, null, null));
		editmenu.addSeparator();
		editmenu.add(i18n_createMenuItem("Undo", TSTextNames.ID_UNDO, null, null));
		editmenu.add(i18n_createMenuItem("Redo", TSTextNames.ID_REDO, null, null));
		editmenu.addSeparator();
		editmenu.add(i18n_createMenuItem("Find", TSTextNames.ID_FIND, null, null));
		editmenu.add(i18n_createMenuItem("Replace", TSTextNames.ID_REPLACE, null, null));
		editmenu.add(i18n_createMenuItem("Goto", TSTextNames.ID_GOTO, null, null));
		template.add(editmenu);

		MenuDefinition sqlmenu = new MenuDefinition("SQL");
		sqlmenu.setName("sql");
		sqlmenu.add(i18n_createMenuItem("Execute", SQLNames.ID_EXECUTE_ALL, null));

		template.add(sqlmenu);

		MenuDefinition toolsmenu = new MenuDefinition(I18N.getLocalizedMessage("Tools"));
		toolsmenu.setName("tools");
		toolsmenu.add(i18n_createMenuItem("Preferences", TSTextNames.ID_PREFERENCES, null));
		template.add(toolsmenu);
	}

	/**
	 * Create the specialized toolbar for this frame. In addition to adding the
	 * standard text editor buttons, we also add the SQL specific buttons.
	 */
	protected void createToolBar() {
		TSToolBarTemplate template = getToolBarTemplate();
		template.add(i18n_createToolBarButton(SQLKit.newSQLBuffer, "incors/16x16/new_sql.png", "New SQL Buffer"));
		template.add(i18n_createToolBarButton(FrameKit.openFileAction, "incors/16x16/folder.png", "Open"));
		template.add(i18n_createToolBarButton(FrameKit.saveFileAction, "incors/16x16/disk_blue.png", "Save"));

		template.addSeparator();

		template.add(i18n_createToolBarButton(SQLNames.ID_CLEAR_BUFFER, "incors/16x16/clear.png", "Clear"));
		template.add(i18n_createToolBarButton(TSTextNames.ID_CUT, "incors/16x16/cut.png", "Cut"));
		template.add(i18n_createToolBarButton(TSTextNames.ID_COPY, "incors/16x16/copy.png", "Copy"));
		template.add(i18n_createToolBarButton(TSTextNames.ID_PASTE, "incors/16x16/paste.png", "Paste"));
		template.add(i18n_createToolBarButton(TSTextNames.ID_UNDO, "incors/16x16/undo.png", "Undo"));
		template.add(i18n_createToolBarButton(TSTextNames.ID_FIND, "incors/16x16/find_text.png", "Find"));
		template.addSeparator();

		template.add(i18n_createToolBarButton(SQLNames.ID_EXECUTE_ALL, "run_all16.gif", "Execute All"));
		template.add(i18n_createToolBarButton(SQLNames.ID_EXECUTE_CURRENT_ALL, "run_current_all16.gif",
				"Execute All From Current Position"));
		template.add(i18n_createToolBarButton(SQLNames.ID_EXECUTE_CURRENT, "run_current16.gif",
				"Execute Current Statement"));
		template.add(i18n_createToolBarButton(SQLNames.ID_STOP, "incors/16x16/stop.png", "Stop"));

		template.add(javax.swing.Box.createHorizontalStrut(5));

		JRadioButton btn = new JRadioButton(TSGuiToolbox.loadImage("incors/16x16/single_results_window.png"));
		btn.setName(SQLNames.ID_RESULTS_WINDOW_MODE);
		btn.setSelectedIcon(TSGuiToolbox.loadImage("incors/16x16/windows.png"));
		btn.setSelected(true);
		btn.setContentAreaFilled(false);
		Dimension d = new Dimension(24, 16);
		btn.setPreferredSize(d);
		btn.setMaximumSize(d);
		btn.setSize(d);
		btn.setSelected(false);
		btn.setToolTipText(I18N.getLocalizedMessage("Toggle_query_results_window_mode"));
		template.add(btn);

		template.addSeparator();
		template.add(i18n_createToolBarButton(SQLNames.ID_RUN_INSERT_MACRO, "incors/16x16/row_add.png",
				"Run Insert Macro"));
		template.add(i18n_createToolBarButton(SQLNames.ID_RUN_UPDATE_MACRO, "incors/16x16/row_update.png",
				"Run Update Macro"));
		template.add(i18n_createToolBarButton(SQLNames.ID_RUN_DELETE_MACRO, "incors/16x16/row_delete.png",
				"Run Delete Macro"));

		template.addSeparator();
		template.add(i18n_createToolBarButton(TSTextNames.ID_PREFERENCES, "incors/16x16/preferences.png", "Preferences"));

		// template.addSeparator();
		// javax.swing.JButton hbtn = i18n_createToolBarButton(
		// SQLNames.ID_HELP, "general/Help16.gif", "Help" );
		// com.jeta.foundation.help.HelpUtils.enableHelpOnButton( hbtn,
		// SQLNames.ID_HELP );
		// template.add( hbtn );
	}

	/**
	 * @return the underlying history object for this frame
	 */
	SQLHistory getHistory() {
		return m_history;
	}

	/**
	 * Override dispose so we can remove reference to the controller to allow
	 * for proper clean up
	 */
	public void dispose() {
		saveState(); // save frame state
		super.dispose();
	}

	/**
	 * DbModelListener implementation. Called when we get an event from the
	 * DbModel
	 */
	public void eventFired(DbModelEvent evt) {
		switch (evt.getID()) {
		case DbModelEvent.TABLE_RENAMED:
		case DbModelEvent.TABLE_DELETED:
		case DbModelEvent.TABLE_CREATED:
		case DbModelEvent.VIEW_CHANGED:
		case DbModelEvent.VIEW_CREATED:
		case DbModelEvent.SCHEMA_LOADED: {
			BufferMgr buffmgr = getBufferMgr();
			Collection c = buffmgr.getBuffers();
			Iterator iter = c.iterator();
			while (iter.hasNext()) {
				Buffer b = (Buffer) iter.next();
				if (b instanceof SQLBuffer) {
					SQLBuffer sqlbuff = (SQLBuffer) b;
					SQLKit kit = (SQLKit) sqlbuff.getEditor().getEditorKit();
					kit.clearCache();
				}
			}
		}
			break;

		case DbModelEvent.STATUS_UPDATE: {
			BufferMgr buffmgr = getBufferMgr();
			Collection c = buffmgr.getBuffers();
			Iterator iter = c.iterator();
			while (iter.hasNext()) {
				Buffer b = (Buffer) iter.next();
				if (b instanceof SQLBuffer)
					((SQLBuffer) b).updateStatus();
			}
		}
			break;
		}

	}

	protected ImageIcon getBufferIcon(int index) {
		if (index >= 0 && index < m_buffericons.length) {
			return m_buffericons[index];
		} else {
			return TSGuiToolbox.loadImage("query16.gif");
		}
	}

	public int getSQLBufferCount() {
		int sqlbuffcount = 0;
		BufferMgr buffmgr = getBufferMgr();
		Collection c = buffmgr.getBuffers();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			Buffer b = (Buffer) iter.next();
			if (b instanceof SQLBuffer)
				sqlbuffcount++;
		}
		return sqlbuffcount;
	}

	/**
	 * Creates the menu, toolbar, and content window for this frame
	 * 
	 * @param params
	 *            a 1 length array that contains the TSConnection object for
	 *            this window
	 */
	public void initializeModel(Object[] params) {
		m_connection = (TSConnection) params[0];
		/**
		 * register a listener to the TSConnection so we can get auto commit
		 * on/off events. We display the auto commit status in the SQL status
		 * bar
		 */
		m_connection.addModelListener(this);

		initialize();

		setTitle("SQL" + " - " + m_connection.getShortId());
		setShortTitle("SQL");

		loadState();

		SQLController controller = (SQLController) getController();
		SQLBuffer buffer = controller.createSQLBuffer();
	}

	/**
	 * Load the SQL history state
	 */
	private void loadState() {
		m_history.loadHistory(m_connection);
	}

	public void requestFocus() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Buffer buffer = getCurrentBuffer();
				if (buffer != null)
					buffer.getEditor().requestFocus();
			}
		});
	}

	/**
	 * Save the SQL history state
	 */
	private void saveState() {
		m_history.saveHistory(m_connection);
	}

	/**
	 * Sets the text in the current sql editor
	 */
	public void setText(String txt) {
		Buffer buff = getCurrentBuffer();
		if (buff != null && buff.getEditor() != null) {
			buff.getEditor().setText(txt);
		}
	}

	/**
	 * Updates the icon in each SQLBuffer tab so that it corresponds with the
	 * tab index.
	 */
	public void syncTabs() {
		JTabbedPane tabpane = getTabbedPane();
		for (int index = 0; index < tabpane.getTabCount(); index++) {
			Buffer buff = getBuffer(index);
			if (buff instanceof SQLBuffer) {
				tabpane.setIconAt(index, getBufferIcon(index));
			} else {
				tabpane.setIconAt(index, TSGuiToolbox.loadImage("incors/16x16/document.png"));
			}
		}
	}

	// layout manager for sql frame
	class SQLLayoutManager implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			int count = parent.getComponentCount();
			for (int index = 0; index < count; index++) {
				Component c = parent.getComponent(index);
				c.setSize(parent.getSize());
			}
		}

		public Dimension minimumLayoutSize(Container parent) {
			return parent.getSize();
		}

		public Dimension preferredLayoutSize(Container parent) {
			return parent.getSize();
		}

		public void removeLayoutComponent(Component comp) {
		}

	}

}
