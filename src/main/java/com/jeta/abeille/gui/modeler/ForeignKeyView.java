package com.jeta.abeille.gui.modeler;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.table.TableColumn;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.ForeignKeyConstraints;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSForeignKeys;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.common.TableSelectorPanel;
import com.jeta.abeille.gui.model.ModelerModel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.AbstractRule;

/**
 * This class displays the view for a single foreign key
 * 
 * @author Jeff Tassin
 */
public class ForeignKeyView extends TSPanel {
	/** the database connection */
	private TSConnection m_connection;

	/** the field for the key name */
	private JTextField m_namefield;

	/** action combo boxes */
	private JComboBox m_onupdatecombo;
	private JComboBox m_ondeletecombo;

	private JCheckBox m_deferrablecheck;
	private JRadioButton m_immediateradio;
	private JRadioButton m_deferredradio;

	/** panel for the tables that we can assign foreign keys to */
	private TableSelectorPanel m_tableselectorpanel;

	/** model for the tables that we can assign foreign keys to */
	private ModelerModel m_tableselectormodel;

	/**
	 * the id of the local table (the one with the foreign keys we are editing )
	 */
	private TableId m_localtableid;

	/** a collection of ColumnMetaData objects that make up the local table */
	private Collection m_localcolumns;

	private AssignedForeignKeyColumnsView m_assignedview;

	/** command ids */
	public static final String ID_EDIT_COLUMN_ASSIGNMENTS = "edit.col.assignments";

	/** component ids */
	public static final String ID_NAME_FIELD = "name.field";
	public static final String ID_DEFERRABLE_CHECK = "deferrable.check";
	public static final String ID_INITIALLY_IMMEDIATE = "initially.immediate.radio";
	public static final String ID_INITIALLY_DEFERRED = "initially.deferred.radio";

	/** constants */
	public static final String NO_ACTION = "NO ACTION";
	public static final String CASCADE = "CASCADE";
	public static final String SET_NULL = "SET NULL";
	public static final String SET_DEFAULT = "SET DEFAULT";
	public static final String RESTRICT = "RESTRICT";

	public static final String FEATURE_FOREIGN_KEY_ON_UPDATE = "checked.feature.foreign.key.on.update";
	public static final String FEATURE_FOREIGN_KEY_ON_DELETE = "checked.feature.foreign.key.on.delete";
	public static final String FEATURE_FOREIGN_KEY_DEFERRABLE = "checked.feature.foreign.key.deferrable";

	/**
	 * ctor for creating a new foreign key
	 * 
	 * @param tableselector
	 *            this is the model that contains the tables we can reference
	 *            for our foreign key. It includes both prototype and existing
	 *            tables.
	 * @param cols
	 *            a collection of ColumnMetaData objects that define the columns
	 *            in the currently modeled table.
	 */
	public ForeignKeyView(TSConnection connection, TableId localTable, ModelerModel tableselector, Collection cols) {
		assert (connection != null);
		assert (localTable != null);

		m_connection = connection;
		m_localtableid = localTable;
		m_tableselectormodel = tableselector;
		m_localcolumns = cols;
		createView();
		initialize(null);
	}

	/**
	 * ctor for editing an existing foreign key
	 * 
	 * @param tableselector
	 *            this is the model that contains the tables we can reference
	 *            for our foreign key. It includes both prototype and existing
	 *            tables.
	 * @param cols
	 *            a collection of ColumnMetaData objects that define the columns
	 *            in the currently modeled table.
	 */
	public ForeignKeyView(TSConnection connection, TableId localid, DbForeignKey fKey, ModelerModel tableselector,
			Collection cols) {
		assert (connection != null);
		assert (fKey != null);

		m_connection = connection;
		m_localtableid = localid;
		if (fKey.getLocalTableId() != null) {
			assert (localid.equals(fKey.getLocalTableId()));
		}
		assert (m_localtableid != null);

		m_tableselectormodel = tableselector;
		m_localcolumns = cols;

		createView();

		m_namefield.setText(fKey.getName());

		TableId reftableid = fKey.getReferenceTableId();
		m_tableselectorpanel.setTableId(reftableid);

		TableMetaData reftmd = tableselector.getTable(reftableid);
		m_assignedview.setForeignKey(fKey, cols, reftmd);

		// set constraints
		ForeignKeyConstraints fc = (ForeignKeyConstraints) fKey.getConstraints();
		initialize(fc);
	}

	private void initialize(ForeignKeyConstraints fc) {
		TSForeignKeys fkimpl = (TSForeignKeys) m_connection.getImplementation(TSForeignKeys.COMPONENT_ID);
		Object[] actions = (Object[]) fkimpl.getSupportedFeature(FEATURE_FOREIGN_KEY_ON_UPDATE);
		if (actions == null) {
			m_onupdatecombo.setEnabled(false);
			m_onupdatecombo.addItem(NO_ACTION);
		} else {
			TSGuiToolbox.addItems(m_onupdatecombo, actions);
		}

		actions = (Object[]) fkimpl.getSupportedFeature(FEATURE_FOREIGN_KEY_ON_DELETE);
		if (actions == null) {
			m_ondeletecombo.setEnabled(false);
			m_ondeletecombo.addItem(NO_ACTION);
		} else {
			TSGuiToolbox.addItems(m_ondeletecombo, actions);
		}

		if (m_deferrablecheck != null) {
			Boolean candefer = (Boolean) fkimpl.getSupportedFeature(FEATURE_FOREIGN_KEY_DEFERRABLE, Boolean.FALSE);
			if (candefer.booleanValue()) {
				m_deferrablecheck.setEnabled(true);
			} else {
				m_deferrablecheck.setEnabled(false);
				m_deferrablecheck.setSelected(false);
				m_deferredradio.setEnabled(false);
				m_immediateradio.setEnabled(false);
			}
		}

		if (fc != null) {
			if (m_deferrablecheck != null) {
				if (fc.isDeferrable() && m_deferrablecheck.isEnabled()) {
					m_deferrablecheck.setSelected(true);
					if (fc.isInitiallyDeferred()) {
						m_deferredradio.setSelected(true);
					} else {
						m_immediateradio.setSelected(true);
					}
				}
			}

			if (m_onupdatecombo.isEnabled()) {
				int updatea = fc.getUpdateAction();
				setActionCombo(m_onupdatecombo, updatea);
			}

			if (m_ondeletecombo.isEnabled()) {
				int deletea = fc.getDeleteAction();
				setActionCombo(m_ondeletecombo, deletea);
			}
		}
	}

	/**
	 * Create the panel that defines the actions for UPDATE and DELETE
	 */
	protected JComponent createActionsPanel() {
		m_onupdatecombo = new JComboBox();
		m_ondeletecombo = new JComboBox();

		JComponent[] left = new JComponent[2];
		left[0] = new JLabel(I18N.getLocalizedMessage("On Delete"));
		left[1] = new JLabel(I18N.getLocalizedMessage("On Update"));

		JComponent[] right = new JComponent[2];
		right[0] = m_ondeletecombo;
		right[1] = m_onupdatecombo;

		ControlsAlignLayout layout = new ControlsAlignLayout();

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, left, right);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(I18N.getLocalizedMessage("Action")),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		layout.setMaxTextFieldWidth(m_onupdatecombo, 15);
		layout.setMaxTextFieldWidth(m_ondeletecombo, 15);
		return panel;
	}

	/**
	 * Create the table that displays the column assignments for this foreign
	 * key
	 */
	public JComponent createColumnsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		javax.swing.JToolBar toolbar = new javax.swing.JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(new JLabel(I18N.getLocalizedMessage("Assigned Columns")));
		toolbar.add(javax.swing.Box.createHorizontalStrut(10));
		toolbar.add(i18n_createToolBarButton("incors/16x16/document_edit.png", ID_EDIT_COLUMN_ASSIGNMENTS,
				I18N.getLocalizedMessage("Edit Column Assignments")));

		m_assignedview = new AssignedForeignKeyColumnsView(m_connection, m_localtableid, false);

		panel.add(toolbar, BorderLayout.NORTH);
		panel.add(m_assignedview);

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * Create a constraints object based on the user input
	 */
	public ForeignKeyConstraints createConstraints() {
		ForeignKeyConstraints fc = new ForeignKeyConstraints();

		if (m_deferrablecheck != null) {
			if (m_deferrablecheck.isSelected())
				fc.setDeferrable(true);

			fc.setInitiallyDeferred(m_deferredradio.isSelected());
		}

		fc.setUpdateAction(getAction(m_onupdatecombo));
		fc.setDeleteAction(getAction(m_ondeletecombo));
		return fc;
	}

	/**
	 * Creates a foreign key from the information entered in the GUI components.
	 */
	public DbForeignKey createForeignKey() {
		DbForeignKey fkey = new DbForeignKey();
		fkey.setLocalTableId(m_localtableid);
		fkey.setKeyName(getName());
		fkey.setReferenceTableId(getReferenceTable());

		m_assignedview.toForeignKey(fkey);
		fkey.setConstraints(createConstraints());
		return fkey;
	}

	/**
	 * Creates the table selector panel at the top of the view.
	 */
	private JComponent createTableSelectorPanel() {
		m_namefield = new JTextField();
		m_namefield.setName(ID_NAME_FIELD);

		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Name"));

		JComponent[] comps = new JComponent[1];
		comps[0] = m_namefield;

		m_tableselectorpanel = new TableSelectorPanel(getConnection(), labels, comps, true);
		m_tableselectorpanel.setModel(m_tableselectormodel);
		m_tableselectorpanel.getObjectsLabel().setText(I18N.getLocalizedDialogLabel("Reference Table"));

		m_tableselectorpanel.setMaxTextFieldWidth(TableSelectorPanel.ID_CATALOGS_COMBO, 30);
		m_tableselectorpanel.setMaxTextFieldWidth(TableSelectorPanel.ID_TABLES_COMBO, 30);
		m_tableselectorpanel.setMaxTextFieldWidth(TableSelectorPanel.ID_SCHEMAS_COMBO, 30);

		ControlsAlignLayout layout = m_tableselectorpanel.getControlsLayout();
		layout.setMaxTextFieldWidth(m_namefield, 30);
		return m_tableselectorpanel;
	}

	/**
	 * Create the panel that contains the transactions and action panels.
	 */
	protected JPanel createPropertiesPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 5));
		panel.add(createActionsPanel(), BorderLayout.NORTH);
		panel.add(createTransactionsPanel(), BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		return panel;
	}

	/**
	 * Creates the table selector panel at the top of the view.
	 */
	private JComponent createTransactionsPanel() {

		JComponent[] left = new JComponent[2];
		m_deferrablecheck = new JCheckBox(I18N.getLocalizedMessage("Deferrable"));
		m_deferrablecheck.setName(ID_DEFERRABLE_CHECK);

		left[0] = m_deferrablecheck;
		left[1] = new JLabel("");

		JComponent[] right = new JComponent[2];

		javax.swing.ButtonGroup bgroup = new javax.swing.ButtonGroup();
		m_immediateradio = new JRadioButton(I18N.getLocalizedMessage("Initially Immediate"));
		m_deferredradio = new JRadioButton(I18N.getLocalizedMessage("Initially Deferred"));
		m_immediateradio.setName(ID_INITIALLY_IMMEDIATE);
		m_deferredradio.setName(ID_INITIALLY_DEFERRED);

		bgroup.add(m_immediateradio);
		bgroup.add(m_deferredradio);

		right[0] = m_immediateradio;
		right[1] = m_deferredradio;

		java.awt.Insets insets = new java.awt.Insets(0, 5, 0, 5);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(left, right, insets);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(I18N.getLocalizedMessage("Transactions")),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		return panel;
	}

	/**
	 * Creates and initializes the components on the view
	 */
	protected void createView() {
		setLayout(new BorderLayout(5, 5));

		add(createTableSelectorPanel(), BorderLayout.NORTH);
		add(createColumnsPanel(), BorderLayout.CENTER);
		add(createPropertiesPanel(), BorderLayout.SOUTH);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setController(new ForeignKeyViewController(this));
	}

	/**
	 * @return the action code for the selected item in the delete or update
	 *         action combobox
	 */
	private int getAction(JComboBox combo) {
		Object obj = combo.getSelectedItem();
		if (NO_ACTION.equals(obj))
			return ForeignKeyConstraints.NO_ACTION;
		else if (CASCADE.equals(obj))
			return ForeignKeyConstraints.CASCADE;
		else if (SET_NULL.equals(obj))
			return ForeignKeyConstraints.SET_NULL;
		else if (SET_DEFAULT.equals(obj))
			return ForeignKeyConstraints.SET_DEFAULT;
		else if (RESTRICT.equals(obj))
			return ForeignKeyConstraints.RESTRICT;
		else
			return ForeignKeyConstraints.NO_ACTION;
	}

	/**
	 * @return the assignment view
	 */
	AssignedForeignKeyColumnsView getAssignmentView() {
		return m_assignedview;
	}

	/**
	 * @return the assignments for the foreign key
	 */
	public ColumnMetaData[][] getAssignments() {
		return m_assignedview.getAssignments();
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return a collection of ColumnMetaData objects that make up the columns
	 *         of the local table.
	 */
	public Collection getLocalColumns() {
		return m_localcolumns;
	}

	/**
	 * @return the local table
	 */
	public TableId getLocalTableId() {
		return m_localtableid;
	}

	/**
	 * @return the foreign key name entered by the user
	 */
	public String getName() {
		return m_namefield.getText().trim();
	}

	/**
	 * @return the preferred size for the view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(6, 22);
	}

	/**
	 * @return the reference id of the selected table. Note that the id might be
	 *         invalid if the user typed an invalid table name. A tableid object
	 *         will still be created in this case
	 */
	public TableId getReferenceTable() {
		return m_tableselectorpanel.createTableId(m_connection);
	}

	/**
	 * @return the table selector model
	 */
	public ModelerModel getTableSelectorModel() {
		return m_tableselectormodel;
	}

	/**
	 * @return a validator for this view
	 */
	public AbstractRule getValidatorRule() {
		return new ForeignKeyValidatorRule();
	}

	/**
	 * Sets the selected item in the given action combo
	 */
	private void setActionCombo(JComboBox combo, int action) {
		switch (action) {
		case ForeignKeyConstraints.NO_ACTION:
			combo.setSelectedItem(NO_ACTION);
			break;

		case ForeignKeyConstraints.CASCADE:
			combo.setSelectedItem(CASCADE);
			break;

		case ForeignKeyConstraints.SET_NULL:
			combo.setSelectedItem(SET_NULL);
			break;

		case ForeignKeyConstraints.RESTRICT:
			combo.setSelectedItem(RESTRICT);
			break;

		case ForeignKeyConstraints.SET_DEFAULT:
			combo.setSelectedItem(SET_DEFAULT);
			break;
		}
	}

	/**
	 * Clears and them loads the assigned columns table with the primary key
	 * columns of the currently selected reference table in the table selector
	 * panel.
	 */
	void updateReferenceTable() {
		TableId reftableid = m_tableselectorpanel.createTableId(m_connection);
		TableMetaData reftmd = m_tableselectormodel.getTable(reftableid);
		m_assignedview.setReferenceTable(reftmd);
	}

	/**
	 * Validates the data in the view
	 */
	public String validateInputs() {
		String msg = m_assignedview.validateInputs();
		if (msg != null)
			return msg;

		return null;
	}

}
