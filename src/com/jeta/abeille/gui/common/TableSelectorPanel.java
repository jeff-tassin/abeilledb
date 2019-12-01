package com.jeta.abeille.gui.common;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.SchemaComparator;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableNameComparator;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.MetaDataPopupRenderer;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.SortedListModel;
import com.jeta.foundation.gui.components.PopupList;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is a panel that contains a schema and table combo box to allow the user
 * to select a table from the database. This interface appears frequently in the
 * application, so we have a dedicated class that the caller can use to create a
 * dialog with these components. The client can add other components as well by
 * passing them into the constructor. The dialog is laid out as follows:
 * 
 * Schema: ComboBox Table: ComboBox Comp1 Label: JComponent1 (these components
 * can go above the schema/table as well ) Comp2 Label: JComponent2
 * 
 * @author Jeff Tassin
 */
public class TableSelectorPanel extends SchemaSelectorPanel {
	/** an empty list model we reuse when an invalid schema is selected */
	private SortedListModel m_empty = new SortedListModel();

	/** the id of the tables combo box */
	public static final String ID_TABLES_COMBO = ID_OBJECTS_COMBO;

	/**
	 * preserve the table name when the user changes the schema. Changing the
	 * schema causes a reload
	 */
	public boolean m_preservetablename = false;

	/**
	 * ctor
	 */
	public TableSelectorPanel(TSConnection conn) {
		super(conn);
		initialize(null, null, false);
	}

	/**
	 * Support adding other components that have labels aligned with schema and
	 * table selector labels.
	 */
	public TableSelectorPanel(TSConnection conn, JLabel[] labels, JComponent[] comps) {
		super(conn);
		initialize(labels, comps, true);
	}

	/**
	 * Support adding other components that have labels aligned with schema and
	 * table selector labels.
	 * 
	 * @param labels
	 *            an array of JLabels to add to this panel
	 * @param comps
	 *            an array of JComponents to add to this panel (aligned
	 *            horizonally with the corresponding JLabel from the labels
	 *            array.
	 * @param bfirst
	 *            set to true if you want the labels and components to appear
	 *            before the schema and reftable components, false otherwise
	 */
	public TableSelectorPanel(TSConnection conn, JLabel[] labels, JComponent[] comps, boolean bfirst) {
		super(conn);
		initialize(labels, comps, bfirst);
	}

	/**
	 * Called by base class when catalog combo is changed
	 */
	protected void catalogChanged() {
		String tablename = getTableName();
		super.catalogChanged();

		Schema schema = getModel().getCurrentSchema(getCatalog());
		assert (schema != null);
		setSchema(schema);
		reloadTables(schema);
		if (m_preservetablename) {
			getTablesCombo().setSelectedItem(tablename);
		}
	}

	/**
	 * @return the combobox object used to specify the table
	 */
	public TSComboBox getTablesCombo() {
		return getObjectsCombo();
	}

	/**
	 * @return the selected table name
	 */
	public String getTableName() {
		return getTablesCombo().getText().trim();
	}

	/**
	 * @return the id of the selected table
	 */
	public TableId createTableId(TSConnection conn) {
		return new TableId(getCatalog(), getSchema(), getTableName());
	}

	/**
	 * Create the controls for this panel and loads them with the necessary data
	 * 
	 * @param labels
	 *            an array of JLabels to add to this panel
	 * @param comps
	 *            an array of JComponents to add to this panel (aligned
	 *            horizonally with the corresponding JLabel from the labels
	 *            array.
	 * @param bfirst
	 *            set to true if you want the labels and components to appear
	 *            before the schema and reftable components, false otherwise
	 */
	protected void initialize(JLabel[] labels, JComponent[] comps, boolean bfirst) {
		super.initialize(labels, comps, bfirst);
		getTablesCombo().setName(ID_TABLES_COMBO);
		getObjectsLabel().setText(I18N.getLocalizedDialogLabel("Table"));

		getSchemasCombo().addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (isSchemaReload()) {
					String tablename = getTableName();
					reloadTables(getSchema());
					if (m_preservetablename) {
						getTablesCombo().setSelectedItem(tablename);
					}
				}
			}
		});

		PopupList list = getTablesCombo().getPopupList();
		list.setRenderer(new MetaDataPopupRenderer(getConnection()));
		list.setComparator(new TableNameComparator());
	}

	public void preserveTableName(boolean preserve) {
		m_preservetablename = preserve;
	}

	/**
	 * Reloads the combos from the model
	 */
	public void reload() {
		super.reload();
		Catalog catalog = getCatalog();
		Schema schema = getSchema();
		String tablename = getTableName();
		if (schema != null) {
			TableId seltableid = null;
			if (tablename.length() > 0) {
				seltableid = new TableId(catalog, schema, tablename);
			} else {
				seltableid = new TableId(catalog, schema, null);
			}
			_setTableId(seltableid);
		}
	}

	/**
	 * Reloads the tables for the selected schema
	 */
	private void reloadTables(Schema schema) {
		Catalog catalog = getCatalog();
		SortedListModel listmodel = getModel().getTables(getCatalog(), schema);
		if (listmodel == null)
			listmodel = new SortedListModel();
		PopupList list = getTablesCombo().getPopupList();
		list.setModel(listmodel);

		if (TSUtils.isDebug()) {
			TSUtils.printMessage("TableSelectorPanel.reloadTables: catalog: " + catalog + "  schema:  " + schema);
			getConnection().validate(catalog, schema);
		}
	}

	/**
	 * Sets the data model for this view
	 * 
	 * @param model
	 *            the model to set. Reloads the view
	 */
	public void setModel(TableSelectorModel model) {
		super.setModel(model);
		try {
			Catalog cat = model.getCurrentCatalog();
			setCatalog(cat);
			Schema schema = model.getCurrentSchema(cat);
			setSchema(schema);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Sets the selected table id in this panel
	 */
	public void _setTableId(TableId tableId) {
		if (tableId == null) {
			setCatalog(null);
			setSchema(null);
			getTablesCombo().setSelectedItem(null);
		} else {
			setCatalog(tableId.getCatalog());
			reloadSchemas(tableId.getCatalog());
			setSchema(tableId.getSchema());
			reloadTables(tableId.getSchema());
			getTablesCombo().setSelectedItem(tableId.getTableName());
		}
	}

	/**
	 * Sets the selected table id in this panel
	 */
	public void setTableId(TableId tableId) {
		try {
			_setTableId(tableId);
		} catch (Exception e) {
			TSUtils.printException(e);
			reload();
		}
	}

}
