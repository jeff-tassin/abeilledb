package com.jeta.abeille.gui.modeler;

import java.util.Collection;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableId;

import com.jeta.abeille.gui.common.TableSelectorPanel;
import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.i18n.I18N;

/**
 * This is a dialog that allows the user to select the columns from an existing
 * table to import into a new table definition. The user can optionally select
 * to import the primary and foreign key definitions.
 * 
 * @author Jeff Tassin
 */
public class ImportDialog extends TSDialog {
	private TableSelectorPanel m_tableselector; // this panel contains the
												// schema and table combo boxes
	private JCheckBox m_importprimarykey;
	private JCheckBox m_importforeignkeys;

	public ImportDialog(Dialog owner, boolean bmodal) {
		super(owner, bmodal);
	}

	public ImportDialog(Frame owner, boolean bmodal) {
		super(owner, bmodal);
	}

	/**
	 * Creates the components that will make up this dialog
	 */
	private JPanel createControlsPanel() {
		JLabel[] labels = new JLabel[2];
		JComponent[] comps = new JComponent[2];

		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Import"));
		labels[1] = new JLabel("");

		m_importprimarykey = new JCheckBox(I18N.getLocalizedMessage("Primary Keys"));
		m_importforeignkeys = new JCheckBox(I18N.getLocalizedMessage("Foreign Keys"));
		comps[0] = m_importprimarykey;
		comps[1] = m_importforeignkeys;

		m_tableselector = new TableSelectorPanel(null, labels, comps, false);
		return m_tableselector;
	}

	/**
	 * @return the selected schema name
	 */
	public String getSchemaName() {
		return m_tableselector.getSchemaName();
	}

	/**
	 * @return the id of the selected table
	 */
	public TableId createTableId(TSConnection conn) {
		return m_tableselector.createTableId(conn);
	}

	/**
	 * @return the selected table name
	 */
	public String getTableName() {
		return m_tableselector.getTableName();
	}

	/**
	 * @return true if the import foreign key checkbox is checked
	 */
	public boolean importForeignKeys() {
		return m_importforeignkeys.isSelected();
	}

	/**
	 * @return true if the import primary key checkbox is checked
	 */
	public boolean importPrimaryKey() {
		return m_importprimarykey.isSelected();
	}

	/**
	 * Initializes this dialog. Creates the components for the dialog and loads
	 * the data used by the dialog.
	 */
	public void initialize(TableSelectorModel tableSelectorModel) {
		setTitle(I18N.getLocalizedMessage("Import"));

		Container container = this.getDialogContentPanel();
		container.setLayout(new BorderLayout());
		container.add(createControlsPanel(), BorderLayout.NORTH);

		m_tableselector.setModel(tableSelectorModel);

		Collection schemas = tableSelectorModel.getSchemas(tableSelectorModel.getCurrentCatalog());
		JComponent comp = (JComponent) m_tableselector.getComponentByName(TableSelectorPanel.ID_SCHEMAS_COMBO);
		if (schemas.size() <= 1)
			comp = (JComponent) m_tableselector.getComponentByName(TableSelectorPanel.ID_TABLES_COMBO);
		setInitialFocusComponent(comp);
	}

}
