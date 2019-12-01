package com.jeta.abeille.gui.modeler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdGetter;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.common.TableSelectorPanel;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.modeler.mysql.MySQLTableEditorPanel;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.app.ObjectStore;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;
import com.jeta.foundation.utils.TSUtils;

/**
 * This dialog is used for editing/creating database tables.
 * 
 * @author Jeff Tassin
 */
public class TableEditorDialog extends TSDialog implements TableIdGetter, JETARule {
	private TSConnection m_connection; // the database connection

	/** the model used for selecting tables */
	private ModelerModel m_tableselector;

	/** the main panel for this dialog */
	private TableEditorPanel m_editorpanel;

	/** the table selector panel at the top of the dialog */
	private TableSelectorPanel m_tableselectorpanel;

	/** the id of the table that we are editing if this is not a new table */
	private TableId m_editingid;

	/** command ids */
	public static final String ID_RELOAD = "reload.button";

	/**
	 * ctor
	 * 
	 * @param owner
	 *            the owner frame that is invoking this dialog
	 * @param tmd
	 *            the table meta data to show. This can be null if creating a
	 *            new table
	 * @param conn
	 *            the underlying database connection
	 * @param tablemodel
	 *            the model that contains the existing tables we can make
	 *            relationships to
	 */
	public TableEditorDialog(Frame owner, TableMetaData tmd, TSConnection conn, ModelerModel tablemodel) {
		super((Frame) owner, true);
		// System.out.println( "TableEditorDialog.isEditable: " + beditable );
		m_connection = conn;
		m_tableselector = tablemodel;

		Container container = getDialogContentPanel();
		container.setLayout(new BorderLayout());

		m_tableselectorpanel = createTableSelectorPanel();
		container.add(m_tableselectorpanel, BorderLayout.NORTH);

		if (tmd == null) {
			Schema defaultschema = m_connection.getCurrentSchema(m_connection.getCurrentCatalog());
			TableId tableid = new TableId(getCatalog(), defaultschema, "");
			tmd = new TableMetaData(tableid);
			m_editingid = null;
		} else {
			m_editingid = tmd.getTableId();
		}

		initialize(tmd.getTableId());

		if (m_editingid != null)
			m_tableselectorpanel.setTableId(m_editingid);

		TableEditorDialogController controller = new TableEditorDialogController(m_connection, m_tableselector, this);
		setController(controller);
		addValidator(controller);

	}

	public void cmdOk() {
		m_editorpanel.stopEditing();
		super.cmdOk();
	}

	/**
	 * Creates the schema and table combo boxes at the top of this panel. Allows
	 * the user to select a different schema and table to view
	 * 
	 * @param the
	 *            newly created panel
	 */
	TableSelectorPanel createTableSelectorPanel() {
		TableSelectorPanel tspanel = new TableSelectorPanel(m_connection);

		TSComboBox catalogsbox = tspanel.getCatalogsCombo();
		TSComboBox schemabox = tspanel.getSchemasCombo();
		TSComboBox tablename = tspanel.getTablesCombo();
		tablename.setValidating(false);

		ControlsAlignLayout layout = tspanel.getControlsLayout();

		layout.setMaxTextFieldWidth(catalogsbox, 25);
		layout.setMaxTextFieldWidth(schemabox, 25);
		layout.setMaxTextFieldWidth(tablename, 25);

		// now add the stick and reload buttons
		JPanel panel = tspanel.getControlsPanel();
		tspanel.setModel(m_tableselector);
		return tspanel;
	}

	/**
	 * Override dispose so we can store the last used table id
	 */
	public void dispose() {
		super.dispose();
		TableId lastid = getTableId();
		try {
			ObjectStore os = (ObjectStore) m_connection.getObjectStore();
			os.store(ModelerNames.ID_LAST_TABLE_ID, lastid);
		} catch (Exception e) {
			// noop
		}
	}

	public Catalog getCatalog() {
		return m_tableselector.getCurrentCatalog();
	}

	/**
	 * @return the editor panel
	 */
	public TableEditorPanel getTableEditorPanel() {
		return m_editorpanel;
	}

	/**
	 * @return the selected table id
	 */
	public TableId getTableId() {
		/**
		 * we return the m_editingid because the user might have changed the
		 * table name in the current dialog. However, other dialog components
		 * such as the foreign key view need to get the TableMetaData from the
		 * given table selector model. The problem is the that table selector
		 * model does not update any table name changes until the dialog
		 * returns, so we need to return the old table id if it exists. The new
		 * table id will be updated when the user closes the dialog and
		 * createTableMetaData is called
		 */
		if (m_editingid == null) {
			return m_tableselectorpanel.createTableId(m_connection);
		} else {
			return m_editingid;
		}
	}

	/**
	 * @return the table meta data described by this dialog
	 */
	public TableMetaData createTableMetaData() {
		TableMetaData tmd = m_editorpanel.createTableMetaData(m_tableselectorpanel.createTableId(m_connection));
		return tmd;
	}

	/**
	 * Initializes the components on this view
	 */
	private void initialize(TableId id) {
		setCloseText(I18N.getLocalizedMessage("Cancel"));

		TableMetaData tmd = m_tableselector.getTable(id);
		if (tmd == null) {
			tmd = new TableMetaData(new TableId(getCatalog(), m_connection.getCurrentSchema(), ""));
			setTitle(I18N.getLocalizedMessage("New Table"));
		} else {
			setTitle(I18N.format("Table_Properties_1", id.getFullyQualifiedName()));
		}
		Container container = getDialogContentPanel();

		repaint();

		if (m_editorpanel != null) {
			container.remove(m_editorpanel);
		}

		if (m_connection.getDatabase().equals(Database.MYSQL)) {
			m_editorpanel = new MySQLTableEditorPanel(tmd, m_connection, m_tableselector, this);
		} else {
			m_editorpanel = new TableEditorPanel(tmd, m_connection, m_tableselector, this);
		}
		container.add(m_editorpanel, BorderLayout.CENTER);

		TSComboBox cbox = (TSComboBox) getComponentByName(TableSelectorPanel.ID_CATALOGS_COMBO);
		cbox.setSelectedItem(tmd.getCatalog());

		cbox = (TSComboBox) getComponentByName(TableSelectorPanel.ID_SCHEMAS_COMBO);
		cbox.setSelectedItem(tmd.getSchema());

		cbox = (TSComboBox) getComponentByName(TableSelectorPanel.ID_TABLES_COMBO);
		cbox.setSelectedItem(tmd.getTableName());

		/** don't reload the tables when the user changes the schema */
		m_tableselectorpanel.preserveTableName(true);

		invalidate();
		validate();
	}

	/**
	 * Creates and intializes the panels on this dialog.
	 * 
	 * @return true if the table was sucessfully loaded. false is returned if
	 *         the table meta data could not be found for the given table id
	 */
	boolean reload(TableId id) {
		initialize(id);
		return true;
	}

	/**
	 * Checks all components for valid input.
	 * 
	 * @return an error message if a component fails validation. Otherwise, null
	 *         is returned if everything is ok
	 */
	public RuleResult check(Object[] params) {
		TableId id = m_tableselectorpanel.createTableId(m_connection);
		if (id.getTableName().length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Table Name"));
		}

		if (m_editingid == null || !m_editingid.equals(id)) {
			// this is a new table or the user changed the table name, so
			// so let's make sure the model does not already have a table
			// with the same id
			TableMetaData tmd = m_tableselector.getTable(id);
			if (m_tableselector.getTable(id) != null) {
				// a table already exists with this id
				return new RuleResult(I18N.format("Invalid_table_name_already_exists_1", id.getTableName()));
			} else {
				if (TSUtils.isDebug()) {
					try {
						// com.jeta.abeille.gui.model.ModelerModel modeler =
						// com.jeta.abeille.gui.model.ModelerModel.createInstance(
						// m_connection );
						// modeler.print();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		Schema schema = m_tableselectorpanel.getSchema();
		if (schema != null)
			schema = m_connection.getSchema(getCatalog(), schema.getName());

		if (schema == null) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Schema"));
		}

		return m_editorpanel.check(params);
	}

}
