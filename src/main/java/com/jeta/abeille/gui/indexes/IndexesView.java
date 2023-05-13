package com.jeta.abeille.gui.indexes;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolBar;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.gui.common.MetaDataTableModel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;

import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a list of indices assigned to a given table
 * 
 * @author Jeff Tassin
 */
public class IndexesView extends TSPanel {
	/** the table that displays the triggers */
	private JTable m_table;

	/* model for the triggers */
	private MetaDataTableModel m_model;

	public static final String ID_CREATE_INDEX = "create.index";
	public static final String ID_EDIT_INDEX = "edit.index";
	public static final String ID_DELETE_INDEX = "delete.index";
	public static final String ID_REINDEX_INDEX = "reindex.index";
	public static final String ID_REINDEX_TABLE = "reindex.table";
	public static final String ID_ALTER_TABLE_INDEXES = "checked.feature.alter.table.indexes";

	/**
	 * ctor
	 */
	public IndexesView(MetaDataTableModel model) {
		setLayout(new BorderLayout());
		m_model = model;

		TSConnection tsconn = m_model.getConnection();
		TSDatabase db = (TSDatabase) tsconn.getImplementation(TSDatabase.COMPONENT_ID);
		if (db.supportsFeature(ID_ALTER_TABLE_INDEXES)) {
			// add( createButtonPanel(), BorderLayout.NORTH );
		}
		add(createTable(), BorderLayout.CENTER);
		setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	/**
	 * Helper method to create a button
	 */
	private JButton _createButton(String id, String iconName, String tooltip) {
		JButton btn = i18n_createButton(null, id, iconName);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		if (tooltip != null)
			btn.setToolTipText(tooltip);

		return btn;
	}

	/**
	 * Creates the toolbar buttons panel
	 */
	private JComponent createButtonPanel() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		JButton btn = _createButton(ID_CREATE_INDEX, "general/New16.gif", I18N.getLocalizedMessage("Create Index"));
		toolbar.add(btn);

		btn = _createButton(ID_EDIT_INDEX, "general/Edit16.gif", I18N.getLocalizedMessage("Modify Index"));
		toolbar.add(btn);

		btn = _createButton(ID_DELETE_INDEX, "general/Delete16.gif", I18N.getLocalizedMessage("Drop Index"));
		toolbar.add(btn);

		toolbar.addSeparator();
		btn = _createButton(ID_REINDEX_INDEX, "reindex16.gif", I18N.getLocalizedMessage("Rebuild Index"));
		toolbar.add(btn);
		btn = _createButton(ID_REINDEX_TABLE, "reindex_all16.gif", I18N.getLocalizedMessage("Rebuild All Indexes"));
		toolbar.add(btn);
		return toolbar;
	}

	/**
	 * Creates the JTable that displays the table triggers
	 * 
	 * @returns the table component
	 */
	private JComponent createTable() {
		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_model, false);
		m_table = tpanel.getTable();
		return tpanel;
	}

	/**
	 * @return the underlying data model
	 */
	public MetaDataTableModel getModel() {
		return m_model;
	}

	/**
	 * @return the selected table index. Null is returned if no index is
	 *         selected
	 */
	public TableIndex getSelectedIndex() {
		int row = m_table.getSelectedRow();
		if (row >= 0) {
			return (TableIndex) m_model.getRow(row);
		} else {
			return null;
		}
	}
}
