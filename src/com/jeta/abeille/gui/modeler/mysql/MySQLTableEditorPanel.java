package com.jeta.abeille.gui.modeler.mysql;

import javax.swing.JTabbedPane;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdGetter;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.model.ModelerModel;

import com.jeta.abeille.gui.modeler.TableEditorPanel;

import com.jeta.abeille.gui.table.mysql.MySQLTableAttributesView;
import com.jeta.plugins.abeille.mysql.MySQLTableAttributes;

/**
 * This panel is used for editing/creating MySQL database tables.
 * 
 * @author Jeff Tassin
 */
public class MySQLTableEditorPanel extends TableEditorPanel {
	/** MySQL table attributes view */
	private MySQLTableAttributesView m_attributesview;

	private MySQLTableAttributes m_attributes = null;

	/**
	 * ctor
	 */
	public MySQLTableEditorPanel(TableMetaData tmd, TSConnection conn, ModelerModel tablemodel, TableIdGetter idgetter) {
		super(tmd, conn, tablemodel, idgetter);

		MySQLTableAttributes attr = null;
		if (tmd != null)
			attr = (MySQLTableAttributes) tmd.getAttributes();

		initialize(attr);
	}

	/**
	 * Creates a new TableMetaData object for the given definition.
	 * 
	 * @return the TableMetaData object that is described by this dialog.
	 */
	public TableMetaData createTableMetaData(TableId tableId) {

		TableMetaData tmd = super.createTableMetaData(tableId);
		m_attributesview.toTable(tmd);
		return tmd;
	}

	/**
	 * Initializes the panel
	 */
	private void initialize(MySQLTableAttributes attr) {
		JTabbedPane tabpane = getTabbedPane();
		m_attributesview = new MySQLTableAttributesView(getConnection(), true, attr);
		tabpane.insertTab("MySQL", null, m_attributesview, null, 1);
	}
}
