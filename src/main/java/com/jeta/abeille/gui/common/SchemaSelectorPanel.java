package com.jeta.abeille.gui.common;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.CatalogComparator;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.SchemaComparator;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.MetaDataPopupRenderer;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.SortedListModel;
import com.jeta.foundation.gui.components.PopupList;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is a panel that contains a schema combo box and a generic object combo
 * box to allow the user to select a database object in a given schema. This
 * interface appears frequently in the application, so we have a dedicated class
 * that the caller can use to create a dialog with these components. The client
 * can add other components as well by passing them into the constructor. The
 * dialog is laid out as follows:
 * 
 * Schema: ComboBox Object: ComboBox Comp1 Label: JComponent1 (these components
 * can go above the schema/table as well ) Comp2 Label: JComponent2
 * 
 * @author Jeff Tassin
 */
public class SchemaSelectorPanel extends TSPanel {
	/**
	 * Displays all catalogs in the system
	 */
	private TSComboBox m_catalogscombo;

	/**
	 * Displays all schemas for the current catalog
	 */
	private TSComboBox m_schemascombo;

	/**
	 * Combo box that displays the objects for the given schema
	 */
	private TSComboBox m_objectcombo;

	/**
	 * The label for the object combo box
	 */
	private JLabel m_objectlabel;

	private JLabel[] m_labels;
	private JComponent[] m_components;

	private ControlsAlignLayout m_layout;

	/**
	 * this is the panel that is the direct parent of the combo boxes it might
	 * not be the same as SchemaSelectorPanel.this
	 */
	private JPanel m_controlspanel;

	/**
	 * Sets the flag that enables if the objects should be reloaded
	 * automatically in the objects combo when the schema changes.
	 */
	private boolean m_schemareload = true;

	/** the database connection */
	private TSConnection m_connection;

	/** the data model */
	private TableSelectorModel m_model;

	/** component ids */
	public static final String ID_CATALOGS_COMBO = "catalogscombo";
	public static final String ID_SCHEMAS_COMBO = "schemascombo";
	public static final String ID_OBJECTS_COMBO = "objectscombo";

	public SchemaSelectorPanel() {
		initialize(null, null, false);
		m_objectcombo.setVisible(false);
	}

	/**
	 * ctor
	 */
	protected SchemaSelectorPanel(TSConnection conn) {
		m_connection = conn;
	}

	/**
	 * Support adding other components that have labels aligned with schema and
	 * object selector labels.
	 * 
	 * @param labels
	 *            an array of JLabels to add to this panel
	 * @param comps
	 *            an array of JComponents to add to this panel (aligned
	 *            horizonally with the corresponding JLabel from the labels
	 *            array.
	 * @param bfirst
	 *            set to true if you want the labels and components to appear
	 *            before the schema and object components, false otherwise
	 */
	SchemaSelectorPanel(TSConnection conn, JLabel[] labels, JComponent[] comps, boolean bfirst) {
		this(conn);
		initialize(labels, comps, bfirst);
		setConnection(conn);
	}

	/**
	 * Factory
	 */
	public static SchemaSelectorPanel createInstance(TSConnection tsconn, TableSelectorModel model) {
		SchemaSelectorPanel panel = new SchemaSelectorPanel(tsconn, null, null, false);
		panel.setModel(model);
		panel.m_objectcombo.setVisible(false);
		return panel;
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * Creates the controls for this panel
	 * 
	 * @param labels
	 *            an array of JLabels to add to this panel
	 * @param comps
	 *            an array of JComponents to add to this panel (aligned
	 *            horizonally with the corresponding JLabel from the labels
	 *            array.
	 * @param bfirst
	 *            set to true if you want the labels and components to appear
	 *            before the schema and object components, false otherwise
	 */
	private JPanel initializeControlsPanel(JLabel[] addlabels, JComponent[] addcomps, boolean bfirst) {
		m_catalogscombo = new TSComboBox();
		m_catalogscombo.setName(ID_CATALOGS_COMBO);
		PopupList clist = m_catalogscombo.getPopupList();
		SortedListModel clistmodel = clist.getModel();
		clistmodel.setComparator(new CatalogComparator());

		m_schemascombo = new TSComboBox();
		m_schemascombo.setName(ID_SCHEMAS_COMBO);
		PopupList slist = m_schemascombo.getPopupList();
		SortedListModel listmodel = slist.getModel();
		listmodel.setComparator(new SchemaComparator());

		m_objectcombo = new TSComboBox();
		m_objectcombo.setName(ID_OBJECTS_COMBO);

		JLabel[] labels = null;
		JComponent[] controls = null;
		m_objectlabel = new JLabel();

		if (addlabels == null || addcomps == null) {
			controls = new JComponent[3];
			controls[0] = m_catalogscombo;
			controls[1] = m_schemascombo;
			controls[2] = m_objectcombo;

			labels = new JLabel[3];
			labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Database"));
			labels[1] = new JLabel(I18N.getLocalizedDialogLabel("Schema"));
			labels[2] = m_objectlabel;
		} else {
			labels = new JLabel[3 + addlabels.length];
			controls = new JComponent[3 + addcomps.length];

			if (bfirst) // the labels that were passed in go first
			{
				for (int index = 0; index < addlabels.length; index++) {
					labels[index] = addlabels[index];
					controls[index] = addcomps[index];
				}

				controls[labels.length - 3] = m_catalogscombo;
				controls[labels.length - 2] = m_schemascombo;
				controls[labels.length - 1] = m_objectcombo;
				labels[controls.length - 3] = new JLabel(I18N.getLocalizedDialogLabel("Database"));
				labels[controls.length - 2] = new JLabel(I18N.getLocalizedDialogLabel("Schema"));
				labels[controls.length - 1] = m_objectlabel;
			} else // the schema and objects go first, then the passed in
					// components
			{

				controls[0] = m_catalogscombo;
				controls[1] = m_schemascombo;
				controls[2] = m_objectcombo;
				labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Database"));
				labels[1] = new JLabel(I18N.getLocalizedDialogLabel("Schema"));
				labels[2] = m_objectlabel;
				for (int index = 0; index < addlabels.length; index++) {
					labels[index + 3] = addlabels[index];
					controls[index + 3] = addcomps[index];
				}
			}
		}

		m_catalogscombo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				catalogChanged();
			}
		});

		m_layout = new ControlsAlignLayout();
		return createControlsPanel(labels, controls, m_layout);
	}

	/**
	 * Called when the catalog combo is selected and changed.
	 */
	protected void catalogChanged() {
		Catalog catalog = getCatalog();
		reloadSchemas(catalog);
	}

	/**
	 * Assigns the newly created controls/labels arrays to our member variables
	 * and then creates the container for these controls.
	 */
	protected JPanel createControlsPanel(JLabel[] labels, JComponent[] comps, ControlsAlignLayout layout) {
		m_components = comps;
		m_labels = labels;
		return TSGuiToolbox.alignLabelTextRows(layout, labels, comps);
	}

	/**
	 * @return the schema selected by the user
	 */
	public Catalog getCatalog() {
		if (getConnection() == null)
			return null;

		if (getConnection().supportsCatalogs()) {
			if (m_catalogscombo.isEnabled()) {
				String cname = getCatalogName().trim();
				Catalog catalog = (Catalog) m_catalogscombo.getSelectedItem();
				if (catalog != null && cname.equalsIgnoreCase(catalog.getName())) {
					return catalog;
				} else {
					return Catalog.createInstance(cname);
				}
			} else {
				PopupList list = m_catalogscombo.getPopupList();
				SortedListModel listmodel = list.getModel();
				assert (listmodel.getSize() == 1);
				return (Catalog) listmodel.getElementAt(0);
			}
		} else {
			return Catalog.VIRTUAL_CATALOG;
		}
	}

	/**
	 * @return the selected catalog name
	 */
	public String getCatalogName() {
		return m_catalogscombo.getText();
	}

	/**
	 * @return the layout manager for the schema and objects combo
	 */
	public ControlsAlignLayout getControlsLayout() {
		return m_layout;
	}

	/**
	 * @return the panel that this the parent of the combo boxes. This is needed
	 *         in case you want to add a button that is aligned with one of the
	 *         boxes.
	 */
	public JPanel getControlsPanel() {
		return m_controlspanel;
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		// @todo check for cross platform compatibility in sizes
		int width = 0;
		int height = 0;
		int total = m_components.length;
		if (!m_objectcombo.isVisible())
			total--;

		for (int index = 0; index < total; index++) {
			JComponent comp = m_components[index];
			Dimension d = comp.getPreferredSize();
			height += d.height;
			height += 5;

			JLabel label = m_labels[index];
			d = label.getPreferredSize();

			if (width < d.width)
				width = d.width;
		}

		int maxcompwidth = 0;
		ControlsAlignLayout layout = getControlsLayout();
		Collection comps = layout.getComponents();
		Iterator iter = comps.iterator();
		while (iter.hasNext()) {
			java.awt.Component comp = (java.awt.Component) iter.next();
			int maxwidth = layout.getMaxFieldWidth(comp);
			if (maxcompwidth < maxwidth)
				maxcompwidth = maxwidth;
		}

		// the max field width was not set for any component
		if (maxcompwidth == 0) {
			maxcompwidth = TSGuiToolbox.calculateAverageTextWidth(m_components[0], 30);
		}

		int x = 0;
		if (m_labels[0] != null) {
			x = m_labels[0].getX();
		}

		int prefwidth = x + width + maxcompwidth + 32;
		int prefheight = height + 10;

		return new Dimension(prefwidth, prefheight);
	}

	/**
	 * @return the selected schema name
	 */
	public String getSchemaName() {
		return m_schemascombo.getText();
	}

	/**
	 * @return the schema selected by the user
	 */
	public Schema getSchema() {
		if (getConnection() == null)
			return null;

		if (getConnection().supportsSchemas()) {
			if (m_schemascombo.isEnabled()) {
				String sname = getSchemaName().trim();
				Schema schema = (Schema) m_schemascombo.getSelectedItem();
				if (schema != null && sname.equalsIgnoreCase(schema.getName())) {
					return schema;
				} else {
					return new Schema(sname);
				}
			} else {

				PopupList list = m_schemascombo.getPopupList();
				SortedListModel listmodel = list.getModel();
				if (listmodel.getSize() > 0) {
					assert (listmodel.getSize() == 1);
					return (Schema) listmodel.getElementAt(0);
				} else {
					return null;
				}
			}
		} else {
			return Schema.VIRTUAL_SCHEMA;
		}
	}

	/**
	 * @return the combobox object used to specify the catalog
	 */
	public TSComboBox getCatalogsCombo() {
		return m_catalogscombo;
	}

	/**
	 * @return the underlying data model for this panel
	 */
	public TableSelectorModel getModel() {
		return m_model;
	}

	/**
	 * @return the combobox object used to specify the schema
	 */
	public TSComboBox getSchemasCombo() {
		return m_schemascombo;
	}

	/**
	 * @return the combobox object used to specify the object
	 */
	public TSComboBox getObjectsCombo() {
		return m_objectcombo;
	}

	/**
	 * @return the combobox object used to specify the object
	 */
	public JLabel getObjectsLabel() {
		return m_objectlabel;
	}

	/**
	 * @return the selected object name
	 */
	public String getObjectName() {
		return m_objectcombo.getText().trim();
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
	 *            before the schema and refobject components, false otherwise
	 */
	protected void initialize(JLabel[] labels, JComponent[] comps, boolean bfirst) {
		m_labels = labels;
		m_components = comps;

		setLayout(new BorderLayout());
		m_controlspanel = initializeControlsPanel(labels, comps, bfirst);
		add(m_controlspanel, BorderLayout.NORTH);
	}

	/**
	 * @return the flag that enables if the objects should be reloaded
	 *         automatically in the objects combo when the schema changes.
	 */
	public boolean isSchemaReload() {
		return m_schemareload;
	}

	boolean isValid(Catalog catalog) {
		if (catalog == null)
			return false;

		Collection catalogs = getModel().getCatalogs();
		Iterator iter = catalogs.iterator();
		while (iter.hasNext()) {
			Catalog cat = (Catalog) iter.next();
			if (cat.equals(catalog)) {
				return true;
			}
		}
		return false;
	}

	boolean isValid(Catalog catalog, Schema schema) {
		if (catalog == null || schema == null)
			return false;

		Collection schemas = getModel().getSchemas(catalog);
		Iterator iter = schemas.iterator();
		while (iter.hasNext()) {
			Schema s = (Schema) iter.next();
			if (schema.equals(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reloads the combos from the model
	 */
	public void reload() {
		Catalog catalog = getCatalog();
		Schema schema = getSchema();
		if (TSUtils.isDebug()) {
			getConnection().validate(catalog, schema);
		}

		if (!isValid(catalog))
			catalog = m_model.getCurrentCatalog();

		setCatalogs(catalog, m_model.getCatalogs());

		if (!isValid(catalog, schema)) {
			schema = m_model.getCurrentSchema(catalog);
		}

		Collection schemas = m_model.getSchemas(catalog);
		setSchemas(schema, schemas);
	}

	/**
	 * Reloads the tables for the selected schema
	 */
	void reloadSchemas(Catalog catalog) {
		SortedListModel listmodel = new SortedListModel();
		Collection schemas = m_model.getSchemas(catalog);
		listmodel.setComparator(new SchemaComparator());
		listmodel.addAll(schemas);
		PopupList list = getSchemasCombo().getPopupList();
		list.setModel(listmodel);
	}

	/**
	 * Sets the catalog
	 * 
	 * @param catalog
	 *            the catalog to set
	 */
	public void setCatalog(Catalog catalog) {
		m_catalogscombo.setSelectedItem(catalog);
	}

	public void setConnection(TSConnection tsconn) {
		m_connection = tsconn;
		if (tsconn != null) {
			PopupList clist = m_catalogscombo.getPopupList();
			clist.setRenderer(new MetaDataPopupRenderer(getConnection()));
			PopupList slist = m_schemascombo.getPopupList();
			slist.setRenderer(new MetaDataPopupRenderer(getConnection()));
		}
	}

	/**
	 * Sets the data model for this view
	 * 
	 * @param catalogs
	 *            a collection of catalog objects
	 */
	public void setCatalogs(Catalog defaultCatalog, Collection catalogs) {
		PopupList list = m_catalogscombo.getPopupList();
		SortedListModel listmodel = list.getModel();
		Iterator iter = catalogs.iterator();
		while (iter.hasNext()) {
			Catalog cat = (Catalog) iter.next();
			listmodel.add(cat);
		}
		m_catalogscombo.setSelectedItem(defaultCatalog);
		if (catalogs.size() <= 1)
			m_catalogscombo.setEnabled(false);
	}

	/**
	 * Sets the components on this panel to enabled or disabled.
	 */
	public void setEnabled(boolean benabled) {
		m_schemascombo.setEnabled(benabled);
		m_objectcombo.setEnabled(benabled);
	}

	/**
	 * Sets the text field width in characters for either the schema box or the
	 * objects box
	 */
	public void setMaxTextFieldWidth(String objectId, int width) {
		ControlsAlignLayout layout = getControlsLayout();
		if (objectId.equals(m_schemascombo.getName())) {
			layout.setMaxTextFieldWidth(m_schemascombo, width);
		} else if (objectId.equals(m_objectcombo.getName())) {
			layout.setMaxTextFieldWidth(m_objectcombo, width);
		} else if (objectId.equals(m_catalogscombo.getName())) {
			layout.setMaxTextFieldWidth(m_catalogscombo, width);
		}
	}

	/**
	 * Sets the data model for this view
	 * 
	 * @param model
	 *            the model to set. Reloads the view
	 */
	public void setModel(TableSelectorModel model) {
		m_model = model;
		m_connection = model.getConnection();
		reload();
	}

	/**
	 * @return the combobox object used to specify the object
	 */
	public void setObjectsLabel(String label) {
		m_objectlabel.setText(label);
	}

	/**
	 * Sets the schema
	 * 
	 * @param schema
	 *            the schema to set
	 */
	public void setSchema(Schema schema) {
		m_schemascombo.setSelectedItem(schema);
	}

	/**
	 * Sets the data model for this view
	 * 
	 * @param schemas
	 *            a collection of Schema objects
	 */
	public void setSchemas(Schema defaultSchema, Collection schemas) {
		// now add schema to schema popup
		PopupList list = m_schemascombo.getPopupList();
		SortedListModel listmodel = list.getModel();

		Iterator iter = schemas.iterator();
		while (iter.hasNext()) {
			Schema sch = (Schema) iter.next();
			listmodel.add(sch);
			if (defaultSchema == null)
				defaultSchema = sch;
		}

		m_schemascombo.setSelectedItem(defaultSchema);
		if (schemas.size() <= 1)
			m_schemascombo.setEnabled(false);
		else
			m_schemascombo.setEnabled(true);
	}

	void validateSchema() {
		if (TSUtils.isDebug()) {
			boolean bfound = false;
			Catalog cat = getCatalog();
			Collection catalogs = getModel().getCatalogs();
			Iterator iter = catalogs.iterator();
			while (iter.hasNext()) {
				Catalog catalog = (Catalog) iter.next();
				if (cat.equals(catalog)) {
					bfound = true;
					break;
				}
			}

			assert (bfound);
			PopupList list = getSchemasCombo().getPopupList();
			SortedListModel listmodel = list.getModel();
			assert (listmodel.getSize() > 0);

			assert (getSchema() != null);
		}
	}

}
