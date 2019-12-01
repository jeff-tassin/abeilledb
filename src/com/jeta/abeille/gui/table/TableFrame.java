package com.jeta.abeille.gui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;

import java.sql.SQLException;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbModelEvent;
import com.jeta.abeille.database.model.DbModelListener;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.View;
import com.jeta.abeille.database.model.ViewService;

import com.jeta.abeille.gui.common.DefaultTableSelectorModel;
import com.jeta.abeille.gui.common.TableSelectorPanel;
import com.jeta.abeille.gui.update.InstanceNames;
import com.jeta.abeille.gui.views.ViewInfoView;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.abeille.licensemgr.AbeilleLicenseUtils;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseUtils;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays the properties for a given table or view This includes
 * columns, foreign keys, triggers, indices, rules, etc. If the table id refers
 * to a database view, then we only show the columns, JDBC, and view query. We
 * listen for DbModel events because this frame is a singleton and is always
 * active. We need to know if a table has been deleted/changed so we can update
 * the table selector
 * 
 * @author Jeff Tassin
 */
public class TableFrame extends TSInternalFrame implements DbModelListener {
	/** the frame icon for this frame */
	public static ImageIcon FRAME_ICON;

	/** the database connection */
	private TSConnection m_connection;

	/** a GUI for displaying the attributes of a database TABLE */
	private TableView m_tableview;

	/**
	 * a GUI for displaying the attributes of a database VIEW
	 */
	private ViewInfoView m_viewview;

	/** the loaded table id */
	private TableId m_tableid;

	/**
	 * This object is the container for the active view ( either a TableView or
	 * ViewInfoView ) The active view is determined from the table id entered by
	 * the user. If the id is for a TABLE, then the TableView is visible. If the
	 * id is for a VIEW, then the ViewInfoView is visible
	 */
	private JComponent m_viewcontainer;

	/** the schema and table combo boxes */
	private TableSelectorPanel m_tableselectorpanel;

	public static final String ID_RELOAD = "btn.reload";
	public static final String ID_TABLE_SELECTOR = "table.selector";
	public static final String ID_SHOW_TABLE_FORM = "table.show.form";
	public static final String ID_QUERY_TABLE = "table.query.all";

	static {
		FRAME_ICON = TSGuiToolbox.loadImage("incors/16x16/table_sql_check.png");
	}

	/**
	 * ctor
	 */
	public TableFrame() {
		super(I18N.getLocalizedMessage("Table"));
		setFrameIcon(FRAME_ICON);
	}

	/**
	 * Creates the buttons
	 */
	public JPanel createButtonsPanel() {
		int STRUT_SIZE = 5;

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		JButton reloadbtn = new JButton(I18N.getLocalizedMessage("Reload"));
		reloadbtn.setIcon(TSGuiToolbox.loadImage("incors/16x16/refresh.png"));
		reloadbtn.setName(ID_RELOAD);
		panel.add(reloadbtn);

		Dimension d = new Dimension(32, 21);
		JButton btn = new JButton(TSGuiToolbox.loadImage("incors/16x16/form_blue.png"));
		btn.setPreferredSize(d);
		setCommandHandler(btn, ID_SHOW_TABLE_FORM);
		btn.setToolTipText(InstanceNames.ID_NAME);
		panel.add(btn);
		btn = new JButton(TSGuiToolbox.loadImage("incors/16x16/table_sql_view.png"));
		setCommandHandler(btn, ID_QUERY_TABLE);
		btn.setPreferredSize(d);
		btn.setToolTipText(I18N.getLocalizedMessage("select_star"));
		panel.add(btn);
		panel.add(javax.swing.Box.createHorizontalStrut(STRUT_SIZE));
		return panel;
	}

	/**
	 * Create the table selector panel used to select the schema/table for this
	 * view
	 */
	public JPanel createTableSelectorPanel() {
		m_tableselectorpanel = new TableSelectorPanel(m_connection);
		m_tableselectorpanel.setName(ID_TABLE_SELECTOR);

		TSComboBox catbox = m_tableselectorpanel.getCatalogsCombo();
		TSComboBox schemabox = m_tableselectorpanel.getSchemasCombo();
		TSComboBox tablename = m_tableselectorpanel.getTablesCombo();
		tablename.setValidating(false);
		ControlsAlignLayout layout = m_tableselectorpanel.getControlsLayout();

		layout.setMaxTextFieldWidth(catbox, 25);
		layout.setMaxTextFieldWidth(schemabox, 25);
		layout.setMaxTextFieldWidth(tablename, 25);

		// now add the stick and reload buttons
		JPanel panel = m_tableselectorpanel.getControlsPanel();

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.EAST;
		c.gridx = 2;
		c.gridy = 2;

		JPanel btnspanel = createButtonsPanel();
		panel.add(btnspanel, c);

		m_tableselectorpanel.setModel(new DefaultTableSelectorModel(m_connection));
		// m_tableselectorpanel.setBorder( BorderFactory.createEmptyBorder( 10,
		// 5, 10, 0 ) );
		return m_tableselectorpanel;
	}

	/**
	 * Creates the view that displays the properties (e.g. columns, keys, rules,
	 * etc) for the selected table. This view is database dependent.
	 */
	public TableView createTableView(TSConnection conn) {
		if (Database.POSTGRESQL.equals(conn.getDatabase())) {
			return new com.jeta.abeille.gui.table.postgres.PostgresTableView(conn);
		} else if (Database.MYSQL.equals(conn.getDatabase())) {
			return new com.jeta.abeille.gui.table.mysql.MySQLTableView(conn);
		} else if (Database.DB2.equals(conn.getDatabase())) {
			return new com.jeta.plugins.abeille.db2.gui.table.DB2TableView(conn);
		} else if (Database.ORACLE.equals(conn.getDatabase())) {
			return new com.jeta.plugins.abeille.oracle.gui.table.OracleTableView(conn);
		} else if (Database.POINTBASE.equals(conn.getDatabase())) {
			return new com.jeta.plugins.abeille.pointbase.gui.table.PointBaseTableView(conn);
		} else {
			return new com.jeta.abeille.gui.table.generic.GenericTableView(conn);
		}
	}

	/**
	 * creates the main view for this frame
	 */
	public java.awt.Container createView() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 10));
		panel.add(createTableSelectorPanel(), BorderLayout.NORTH);
		m_tableview = createTableView(m_connection);
		panel.add(m_tableview, BorderLayout.CENTER);

		// set the immediate parent to the view
		m_viewcontainer = panel;
		return panel;
	}

	/**
	 * Called when we get an event from the DbModel
	 */
	public void eventFired(DbModelEvent evt) {
		if (evt.getID() != DbModelEvent.STATUS_UPDATE)
			m_tableselectorpanel.reload();
	}

	/**
	 * Searchs the given component and all of its child components for the first
	 * component that is a JTable instance
	 */
	private JTable findTable(Component comp) {
		if (comp instanceof JTable)
			return (JTable) comp;
		else if (comp instanceof Container) {
			Container cc = (Container) comp;
			for (int index = 0; index < cc.getComponentCount(); index++) {
				Component child = cc.getComponent(index);
				JTable table = findTable(child);
				if (table != null)
					return table;
			}
		}

		return null;
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the loaded table id. Null is returned if no table or an invalid
	 *         table was loaded
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * @return the id of the selected table
	 */
	public TableId getSelectedTableId() {
		TSConnection conn = getConnection();
		TableId tableid = m_tableselectorpanel.createTableId(conn);
		return tableid;
	}

	/**
	 * @return the GUI for displaying the attributes of a database TABLE
	 */
	public TableView getTableView() {
		return m_tableview;
	}

	/**
	 * @return the GUI for displaying the attributes of a database VIEW
	 */
	public ViewInfoView getViewInfoView() {
		return m_viewview;
	}

	/**
	 * Creates and initializes the components on this frame
	 * 
	 * @param params
	 *            the list of parameters need to initialize this frame
	 *            Currently, we expect an array (size == 2) where the first
	 *            parameter must be the database connection (TSConnection) and
	 *            the second parameter must be a TableId ( can be null)
	 */
	public void initializeModel(Object[] params) {
		m_connection = (TSConnection) params[0];
		setTitle("Table Properties" + " - " + m_connection.getShortId());
		setShortTitle("Table Properties");

		getContentPane().add(createView(), BorderLayout.CENTER);

		Dimension d = TSGuiToolbox.getWindowDimension(10, 20);
		TSGuiToolbox.setReasonableWindowSize(this.getDelegate(), d);
		setController(new TableFrameController(this));
		m_connection.addModelListener(this);
	}

	/**
	 * Loads the table in all of the views but does not update the table
	 * selector panel.
	 */
	public void loadTable(TableId tableId) {
		// first determine if the table id is for TABLE or a VIEW. If so, we
		// need
		// to update the frame to load the appropriate GUI for the tableid
		try {

			m_tableid = tableId;
			ViewService viewsrv = (ViewService) m_connection.getImplementation(ViewService.COMPONENT_ID);
			View view = viewsrv.getView(tableId);
			if (view != null && !AbeilleLicenseUtils.isBasic() && (m_connection.getDatabase() == Database.POSTGRESQL)) {
				if (m_viewview == null)
					m_viewview = new ViewInfoView(m_connection);

				m_viewview.setView(view);

				if (!m_viewcontainer.isAncestorOf(m_viewview)) {
					m_viewcontainer.remove(m_tableview);
					m_viewcontainer.add(m_viewview);
					m_viewcontainer.revalidate();
					m_viewcontainer.repaint();
				}
			} else {
				TableMetaData tmd = m_connection.getTable(tableId);
				m_tableview.setTableId(tableId);
				if (!m_viewcontainer.isAncestorOf(m_tableview)) {
					if (m_viewview != null)
						m_viewcontainer.remove(m_viewview);

					m_viewcontainer.add(m_tableview);
					m_viewcontainer.revalidate();
					m_viewcontainer.repaint();
				}
			}

			try {
				// m_connection.getMetaDataConnection().commit();
				m_connection.jetaCommit(m_connection.getMetaDataConnection());
				if (tableId != null) {
					m_connection.fireStatusUpdate(tableId.getCatalog());
				}
			} catch (SQLException e) {
				TSUtils.printException(e);
			}

		} catch (Exception e) {
			SQLErrorDialog.showErrorDialog(this, e, null);
		}

		/**
		 * make sure the row counts in each tab are now updated to the latest
		 * values
		 */
		syncRowCountValues();
	}

	public void requestFocus() {
		final java.awt.Component comp = (java.awt.Component) m_tableselectorpanel
				.getComponentByName(TableSelectorPanel.ID_TABLES_COMBO);
		if (comp != null) {
			if (comp instanceof TSComboBox) {
				((TSComboBox) comp).selectEditorText();
			}
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					comp.requestFocus();
				}
			});

		} else {
			assert (false);
		}
	}

	/**
	 * Sets the current table id for the frame. All views are updated to display
	 * the properties for the given table
	 */
	public void setTableId(TableId tableId) {
		assert (tableId.getCatalog() != null);
		loadTable(tableId);
		m_tableselectorpanel.setTableId(tableId);
	}

	private String getTabLabel(String label, int rowcount) {
		if (label == null)
			return "";

		StringBuffer newlabel = new StringBuffer();
		if (rowcount == 0) {
			newlabel.append("<html><p>");
			newlabel.append(label);
			newlabel.append("</p></html>");
		} else {
			newlabel.append("<html><b>");
			newlabel.append(label);
			newlabel.append("</b></html>");
		}
		return newlabel.toString();
	}

	/**
	 * Updates the row count values for each tab in the view
	 */
	void syncRowCountValues() {
		JTabbedPane tabpane = m_tableview.getTabbedPane();
		for (int tabindex = 0; tabindex < tabpane.getTabCount(); tabindex++) {
			Component c = tabpane.getComponentAt(tabindex);
			if (c instanceof Container) {
				Container cc = (Container) c;
				JTable table = findTable(cc);
				String label = m_tableview.getTitle(cc);
				if (table != null) {
					int rowcount = table.getRowCount();
					if (rowcount <= 0) {
						tabpane.setTitleAt(tabindex, getTabLabel(label, rowcount));
					} else {
						tabpane.setTitleAt(tabindex, getTabLabel(label, rowcount));
					}
				} else {
					tabpane.setTitleAt(tabindex, label);
				}
			}
		}
	}

}
