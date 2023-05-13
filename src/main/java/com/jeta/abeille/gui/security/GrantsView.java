package com.jeta.abeille.gui.security;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.AbstractUser;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.gui.common.SchemaSelectorPanel;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.split.CustomSplitPane;
import com.jeta.foundation.gui.table.TablePopupMenu;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.TSTablePanel;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays a list of grants in the database
 * 
 * @author Jeff Tassin
 */
public abstract class GrantsView extends TSPanel {
	/**
	 * A lookup of DbObject (keys) to GrantViewDefinition objects (values). This
	 * is used to associate a grants view with a selected object type.
	 */
	private HashMap m_viewdefinitions = new HashMap();

	/**
	 * The current view definition
	 */
	private GrantsViewDefinition m_currentviewdef = null;

	/** the table that displays the users and groups */
	private JTable m_userstable;

	/** the model of the groups and users */
	private GroupsAndUsersModel m_usersmodel;

	/** the underlying database connection */
	private TSConnection m_connection;

	/** displays the catalogs currently opened in the connection */
	private JComboBox m_catalogscombo = new JComboBox();

	/**
	 * displays the object types we can display permissions for (e.g. table,
	 * view, sequence, function)
	 */
	private JComboBox m_typecombo = new JComboBox();

	/** displays the schemas in the database */
	private JComboBox m_schemascombo;

	/** the split pane */
	private CustomSplitPane m_split;

	private final int TEXTFIELD_WIDTH = 20;

	/** command ids */
	public static final String ID_RELOAD_GRANTS = "reload.grants";
	public static final String ID_COMMIT = "commit";
	public static final String ID_GRANT_SELECTION = "grant.selection";
	public static final String ID_REVOKE_SELECTION = "revoke.selection";
	public static final String ID_TOGGLE_SELECTION = "toggle.selection";

	/** component ids */
	public static final String ID_USER_RADIO = "user.radio";
	public static final String ID_GROUP_RADIO = "group.radio";
	public static final String ID_FILTER_FIELD = "filter";
	public static final String ID_TYPES_COMBO = "object.types.combo";
	public static final String ID_TABLES_COMBO = "object.tables.combo";
	public static final String ID_CATALOGS_COMBO = "object.catalogs.combo";

	/**
	 * ctor
	 */
	public GrantsView(TSConnection connection, UsersModel usersModel, GroupsModel groupsModel) {
		m_connection = connection;
		setLayout(new BorderLayout());
		add(createView(), BorderLayout.CENTER);

		m_usersmodel.setGroupsAndUsers(groupsModel, usersModel);
		reload();
	}

	public abstract GrantsModel createGrantsModel(DbObjectType objType);

	/**
	 * Creates the controls panel at the top of the view
	 */
	private JComponent createControlsView() {
		JPanel panel = new JPanel();
		JComponent leftcomp = createLeftControlsPanel();
		JComponent rightcomp = createRightControlsPanel();
		panel.add(leftcomp);
		panel.add(rightcomp);
		panel.setLayout(new MyControlsLayout(leftcomp, rightcomp));
		return panel;
	}

	/**
	 * Creates the controls panel to the top left of the view
	 */
	private JComponent createLeftControlsPanel() {
		JLabel[] labels = new JLabel[3];
		JComponent[] comps = new JComponent[3];

		m_catalogscombo.setName(ID_CATALOGS_COMBO);
		m_typecombo.setName(ID_TYPES_COMBO);

		Collection catalogs = m_connection.getCatalogs();
		Iterator iter = catalogs.iterator();
		while (iter.hasNext()) {
			m_catalogscombo.addItem(iter.next());
		}
		m_catalogscombo.setSelectedItem(m_connection.getDefaultCatalog());

		Database db = m_connection.getDatabase();
		if (db == Database.POSTGRESQL) {
			m_schemascombo = new JComboBox();

			labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Database"));
			comps[0] = m_catalogscombo;

			labels[1] = new JLabel(I18N.getLocalizedDialogLabel("Type"));
			comps[1] = m_typecombo;

			labels[2] = new JLabel(I18N.getLocalizedDialogLabel("Schema"));
			comps[2] = m_schemascombo;

			m_typecombo.addItem(DbObjectType.TABLE);
			m_typecombo.addItem(DbObjectType.VIEW);
			m_typecombo.addItem(DbObjectType.SEQUENCE);
		} else {
			labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Database"));
			comps[0] = m_catalogscombo;

			labels[1] = new JLabel(I18N.getLocalizedDialogLabel("Type"));
			comps[1] = m_typecombo;

			labels[2] = new JLabel(I18N.getLocalizedDialogLabel("Table"));
			TSComboBox tablescombo = new TSComboBox();
			// when the user selects the types combo to column, then this combo
			// will be enabled
			tablescombo.setEnabled(false);
			tablescombo.setName(ID_TABLES_COMBO);
			comps[2] = tablescombo;
		}

		try {
			if (m_connection.getDatabase() == Database.POSTGRESQL && m_connection.supportsSchemas()) {
				// only Postgres 7.3 or better can have security on functions
				m_typecombo.addItem(DbObjectType.FUNCTION);
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_catalogscombo, TEXTFIELD_WIDTH);
		layout.setMaxTextFieldWidth(m_typecombo, TEXTFIELD_WIDTH);
		layout.setMaxTextFieldWidth(comps[2], TEXTFIELD_WIDTH);

		JPanel result = new JPanel(new BorderLayout());
		result.add(TSGuiToolbox.alignLabelTextRows(layout, labels, comps), BorderLayout.NORTH);
		return result;
	}

	/**
	 * Creates the controls panel on the top right of the view. This panel
	 * contains the user combo, user/group radios and the command buttons.
	 */
	private JComponent createRightControlsPanel() {
		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Object Name Filter"));

		JPanel btnpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton reloadbtn = createButton(TSGuiToolbox.loadImage("incors/16x16/refresh.png"), ID_RELOAD_GRANTS);
		reloadbtn.setText(I18N.getLocalizedMessage("Show Grants"));

		JButton commitbtn = createButton(TSGuiToolbox.loadImage("incors/16x16/data_into.png"), ID_COMMIT);
		commitbtn.setText(I18N.getLocalizedMessage("Commit"));
		btnpanel.add(reloadbtn);
		btnpanel.add(commitbtn);

		JTextField filterfield = new JTextField();
		filterfield.setName(ID_FILTER_FIELD);
		JComponent[] comps = new JComponent[1];
		comps[0] = filterfield;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(filterfield, TEXTFIELD_WIDTH);
		JPanel fpanel = TSGuiToolbox.alignLabelTextRows(layout, labels, comps);

		JPanel result = new JPanel(new BorderLayout());
		result.add(fpanel, BorderLayout.NORTH);
		result.add(btnpanel, BorderLayout.CENTER);

		return result;
	}

	/**
	 * Create the table that displays the groups and users
	 */
	private JComponent createGroupsAndUsersView() {
		m_usersmodel = new GroupsAndUsersModel();
		m_userstable = TableUtils.createSortableTable(m_usersmodel);

		JPanel panel = new JPanel(new BorderLayout());

		JPanel headingpanel = new JPanel(new BorderLayout());
		// this is the list heading
		JLabel heading = new JLabel();
		heading.setForeground(java.awt.Color.black);
		heading.setText(I18N.getLocalizedMessage("User\\Group"));

		headingpanel.add(heading, BorderLayout.CENTER);
		headingpanel
				.setBorder(BorderFactory.createCompoundBorder(
						javax.swing.UIManager.getBorder("TableHeader.cellBorder"),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		panel.add(headingpanel, BorderLayout.NORTH);

		TableColumnModel cmodel = m_userstable.getColumnModel();
		cmodel.getColumn(GroupsAndUsersModel.ICON_COLUMN).setWidth(32);
		cmodel.getColumn(GroupsAndUsersModel.ICON_COLUMN).setMaxWidth(32);
		cmodel.getColumn(GroupsAndUsersModel.ICON_COLUMN).setMinWidth(32);
		cmodel.getColumn(GroupsAndUsersModel.ICON_COLUMN).setPreferredWidth(32);
		JScrollPane scroll = new JScrollPane(m_userstable);

		panel.add(scroll, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Creates and initializes the view
	 */
	private JComponent createView() {
		JPanel panel = new JPanel(new BorderLayout());

		m_split = new CustomSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
		m_split.setDividerLocation(0.2);
		m_split.add(createGroupsAndUsersView());

		panel.add(m_split, BorderLayout.CENTER);
		panel.add(createControlsView(), BorderLayout.NORTH);
		return panel;
	}

	/**
	 * Creates the view definition for the given object type and stores in the
	 * hash map.
	 */
	private GrantsViewDefinition createViewDefinition(DbObjectType objType) {
		GrantsModel model = createGrantsModel(objType);

		TSTablePanel tspanel = TableUtils.createSimpleTable(model, true);
		JTable table = tspanel.getTable();
		TableColumnModel colmodel = table.getColumnModel();
		for (int col = 0; col < table.getColumnCount(); col++) {
			TableCellRenderer delegate = table.getDefaultRenderer(model.getColumnClass(col));
			GrantsRenderer renderer = new GrantsRenderer(this, delegate);
			TableColumn tcol = colmodel.getColumn(col);
			tcol.setCellRenderer(renderer);

		}

		TableUtils.setColumnWidth(table, GrantsModel.NAME_COLUMN, 25);

		TablePopupMenu popup = tspanel.getPopupMenu();
		popup.addSeparator();
		popup.add(i18n_createMenuItem("Grant", ID_GRANT_SELECTION, null));
		popup.add(i18n_createMenuItem("Revoke", ID_REVOKE_SELECTION, null));
		popup.add(i18n_createMenuItem("Toggle", ID_TOGGLE_SELECTION, null));

		GrantsViewDefinition viewdef = new GrantsViewDefinition(model, tspanel);
		m_viewdefinitions.put(objType, viewdef);
		return viewdef;
	}

	public Catalog getCatalog() {
		return (Catalog) m_catalogscombo.getSelectedItem();
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the current view definition
	 */
	GrantsViewDefinition getCurrentViewDefinition() {
		return m_currentviewdef;
	}

	/**
	 * @return the object name filer
	 */
	public String getFilter() {
		JTextField ffield = (JTextField) getComponentByName(ID_FILTER_FIELD);
		return ffield.getText().trim();
	}

	/**
	 * @return the datamodel that displays the grants
	 */
	public GrantsModel getGrantsModel() {
		return m_currentviewdef.getGrantsModel();
	}

	/**
	 * @return the table that displays the grants
	 */
	public JTable getGrantsTable() {
		TSTablePanel panel = (TSTablePanel) m_currentviewdef.getViewComponent();
		return panel.getTable();
	}

	/**
	 * @return the selected object type
	 */
	public DbObjectType getSelectedObjectType() {
		return (DbObjectType) m_typecombo.getSelectedItem();
	}

	/**
	 * @return the selected schema
	 */
	public Schema getSelectedSchema() {
		if (m_schemascombo == null)
			return Schema.VIRTUAL_SCHEMA;
		else
			return (Schema) m_schemascombo.getSelectedItem();
	}

	/**
	 * @return the selected user
	 */
	public AbstractUser getSelectedUser() {
		int row = m_userstable.getSelectedRow();
		if (row >= 0) {
			// we need to convert to model coords in case table is sorted
			row = TableUtils.convertTableToModelIndex(m_userstable, row);
			return m_usersmodel.getUser(row);
		}

		return null;
	}

	/**
	 * @return the grant definition wrapper at the given row. Null is returned
	 *         if the row is invalid
	 */
	GrantDefinitionWrapper getGrantDefinitionWrapper(int row) {
		return (GrantDefinitionWrapper) getGrantsModel().getRow(row);
	}

	/**
	 * @return the view definition for the given object type
	 */
	private GrantsViewDefinition getViewDefinition(DbObjectType objType) {
		GrantsViewDefinition viewdef = (GrantsViewDefinition) m_viewdefinitions.get(objType);
		if (viewdef == null)
			viewdef = createViewDefinition(objType);

		return viewdef;
	}

	/**
	 * Loads the schemas combo with all schemas in the system
	 * 
	 * @param bgroups
	 *            if true, then the combo is loaded with groups instead of users
	 */
	public void reload() {
		if (m_schemascombo != null) {
			Collection schemas = m_connection.getModel(getCatalog()).getSchemas();
			Iterator iter = schemas.iterator();
			while (iter.hasNext()) {
				Schema schema = (Schema) iter.next();
				m_schemascombo.addItem(schema);
			}
			m_schemascombo.setSelectedItem(m_connection.getCurrentSchema());
		}
	}

	/**
	 * Refreshes the groups in the model
	 */
	public void refreshGroupsAndUsers() {
		AbstractUser user = getSelectedUser();
		m_usersmodel.reload();
		if (user == null) {
			if (m_userstable.getRowCount() > 0) {
				m_userstable.setRowSelectionInterval(0, 0);
				m_userstable.setColumnSelectionInterval(0, 1);
			}
		} else {
			boolean bfound = false;
			for (int row = 0; row < m_userstable.getRowCount(); row++) {
				int modelrow = TableUtils.convertTableToModelIndex(m_userstable, row);
				AbstractUser au = m_usersmodel.getUser(modelrow);
				if (user.equals(au)) {
					m_userstable.setRowSelectionInterval(row, row);
					m_userstable.setColumnSelectionInterval(0, 1);
					bfound = true;
					break;
				}
			}

			if (!bfound) {
				m_userstable.setRowSelectionInterval(0, 0);
				m_userstable.setColumnSelectionInterval(0, 1);
			}

		}
	}

	/**
	 * Sets the heading that is displayed above the grants table
	 */
	public void setGrantsHeading(String headingMsg) {
		GrantsViewDefinition viewdef = getCurrentViewDefinition();
		if (viewdef != null) {
			viewdef.setHeading(headingMsg);
		}
	}

	/**
	 * Sets the model type (either standard or function )
	 */
	public void setGrantsModelType(DbObjectType objType) {
		/**
		 * we have a view definition for every object type. This is needed
		 * because different object types can have different privileges and
		 * therefore require different tables/table models
		 */
		GrantsViewDefinition viewdef = getViewDefinition(objType);
		assert (viewdef != null);
		if (viewdef != null) {
			if (m_currentviewdef != null)
				m_currentviewdef.deactivate();

			m_currentviewdef = viewdef;
			int divlocation = m_split.getDividerLocation();
			m_split.setRightComponent(viewdef.getViewContainer());
			m_split.setDividerLocation(divlocation);
			m_split.revalidate();
		}
	}

	/**
	 * Override so we can update all of our cached views
	 */
	public void updateUI() {
		super.updateUI();

		if (m_viewdefinitions != null) {
			Iterator iter = m_viewdefinitions.values().iterator();
			while (iter.hasNext()) {
				GrantsViewDefinition gdef = (GrantsViewDefinition) iter.next();
				gdef.updateUI();
			}
		}
	}

	/**
	 * This class defines a table and table model for a given grants view. We
	 * need this because different database objects have different privileges.
	 * For example, a TABLE has a different set of privileges than a FUNCTION.
	 * Therefore, they will have different table models. We assoicated a
	 * GrantsViewDefinition instance with the type of object that is current
	 * selected.
	 */
	static class GrantsViewDefinition {
		/** the grants data model */
		private GrantsModel m_model;

		/** the view of the grants data */
		private JComponent m_view;

		private JComponent m_container;

		private JLabel m_heading;

		/**
		 * ctor
		 */
		public GrantsViewDefinition(GrantsModel model, JComponent view) {
			m_model = model;
			m_view = view;

			JPanel panel = new JPanel(new BorderLayout());
			JPanel headingpanel = new JPanel(new BorderLayout());
			// this is the list heading
			m_heading = new JLabel();
			m_heading.setForeground(java.awt.Color.black);
			m_heading.setText(I18N.getLocalizedMessage("Privileges"));

			headingpanel.add(m_heading, BorderLayout.CENTER);
			headingpanel.setBorder(BorderFactory.createCompoundBorder(
					javax.swing.UIManager.getBorder("TableHeader.cellBorder"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));

			panel.add(headingpanel, BorderLayout.NORTH);
			panel.add(m_view, BorderLayout.CENTER);
			m_container = panel;
		}

		/**
		 * Call when this view is no longer visible. We remove any data in the
		 * model to clear up any memory. This is ok because the next time this
		 * view is needed, the user must call reload to see anything.
		 */
		public void deactivate() {
			m_model.removeAll();
		}

		/**
		 * @return the grants model
		 */
		public GrantsModel getGrantsModel() {
			return m_model;
		}

		/**
		 * @return the component that displays the grants
		 */
		public JComponent getViewComponent() {
			return m_view;
		}

		/**
		 * @return the container that contains the component that displays the
		 *         grants
		 */
		public Container getViewContainer() {
			return m_container;
		}

		public void setHeading(String msg) {
			m_heading.setText(msg);
		}

		public void updateUI() {
			m_container.updateUI();
		}
	}

	/**
	 * 
	 * This is a special layout for the top controls panel. This is used because
	 * it was difficult to get the layout precision we need with the standard
	 * layouts
	 */
	private class MyControlsLayout implements LayoutManager {
		private Component m_left;
		private Component m_right;
		private Dimension m_min = new Dimension(50, 50);

		public MyControlsLayout(Component left, Component right) {
			m_left = left;
			m_right = right;
		}

		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			Insets insets = parent.getInsets();

			Dimension sz = parent.getSize();
			sz.width = sz.width - insets.left - insets.right;
			sz.height = sz.height - insets.top - insets.bottom;

			int y = insets.top;
			int leftwidth = 300;

			m_left.setSize(leftwidth, sz.height);
			m_left.setLocation(insets.left, y);
			m_right.setSize(sz.width - leftwidth, sz.height);
			m_right.setLocation(insets.left + leftwidth + 10, y);
		}

		public Dimension minimumLayoutSize(Container parent) {
			Dimension d = m_typecombo.getPreferredSize();
			d.height *= 4;
			return d;
		}

		public Dimension preferredLayoutSize(Container parent) {
			Dimension d = m_typecombo.getPreferredSize();
			d.height *= 4;
			return d;
		}

		public void removeLayoutComponent(Component comp) {
		}

	}
}
