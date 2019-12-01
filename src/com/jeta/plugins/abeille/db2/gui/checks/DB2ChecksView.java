package com.jeta.plugins.abeille.db2.gui.checks;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;

import com.jeta.foundation.i18n.I18N;

/**
 * View for displaying checks on a DB2 table.
 * 
 * @author Jeff Tassin
 */
public class DB2ChecksView extends TSPanel {
	/** the table that displays the triggers */
	private JTable m_table;

	/* model for the exported keys */
	private DB2ChecksModel m_model;

	/**
	 * ctor
	 */
	public DB2ChecksView(DB2ChecksModel model) {
		setLayout(new BorderLayout());
		m_model = model;
		add(createTable(), BorderLayout.CENTER);
		setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	/**
	 * Creates the JTable that displays the table triggers
	 * 
	 * @returns the table component
	 */
	private JComponent createTable() {
		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_model, true);
		m_table = tpanel.getTable();
		m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableUtils.setColumnWidths(m_table);

		TableUtils.setColumnWidth(m_table, DB2ChecksModel.NAME_COLUMN, 15);
		TableUtils.setColumnWidth(m_table, DB2ChecksModel.CREATED_COLUMN, 20);
		TableUtils.setColumnWidth(m_table, DB2ChecksModel.PATH_COLUMN, 20);
		TableUtils.setColumnWidth(m_table, DB2ChecksModel.DEFINITION_COLUMN, 60);

		return tpanel;
	}

	/**
	 * @return the underlying data model
	 */
	public DB2ChecksModel getModel() {
		return m_model;
	}

}
