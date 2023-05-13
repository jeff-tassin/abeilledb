package com.jeta.abeille.gui.common;

import java.util.Collection;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

/**
 * This dialog allows the user to select a table and schema from the current
 * database.
 * 
 * @author Jeff Tassin
 */
public class TableSelectorDialog extends TSDialog {
	private TableSelectorPanel m_tableselector;

	/**
	 * ctor
	 */
	public TableSelectorDialog(Dialog owner, boolean bModal) {
		super((Dialog) owner, bModal);
	}

	/**
	 * ctor
	 */
	public TableSelectorDialog(Frame owner, boolean bModal) {
		super((Frame) owner, bModal);
	}

	/**
	 * Called when user hits ok button.
	 */
	public void cmdOk() {
		if (!isSchemaValid())
			TSGuiToolbox.showErrorDialog(I18N.getLocalizedMessage("Invalid Schema"));
		else
			super.cmdOk();
	}

	protected JPanel createControlsPanel(TSConnection conn) {
		TableSelectorView view = new TableSelectorView(conn);
		m_tableselector = view.getTableSelectorPanel();
		return view;
	}

	/**
	 * @return the selected schema
	 */
	public String getSchemaName() {
		return m_tableselector.getSchemaName();
	}

	/**
	 * @return the selected table id
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

	protected void initialize(TSConnection conn) {
		setPrimaryPanel(createControlsPanel(conn));
	}

	boolean isSchemaValid() {
		return m_tableselector.getSchemasCombo().isValueInModel();
	}

	boolean isTableNameValid() {
		return m_tableselector.getTablesCombo().isValueInModel();
	}

	/**
	 * Sets the datamodel for this dialog
	 * 
	 * @param model
	 *            the model to set
	 */
	public void setModel(TSConnection conn, TableSelectorModel model) {
		initialize(conn);

		m_tableselector.setModel(model);
		Collection schemas = model.getSchemas(model.getCurrentCatalog());
		JComponent comp = (JComponent) m_tableselector.getComponentByName(TableSelectorPanel.ID_SCHEMAS_COMBO);
		if (schemas.size() <= 1)
			comp = (JComponent) m_tableselector.getComponentByName(TableSelectorPanel.ID_TABLES_COMBO);

		setInitialFocusComponent(comp);
	}

}
