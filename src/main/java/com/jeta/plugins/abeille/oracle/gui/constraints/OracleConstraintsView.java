package com.jeta.plugins.abeille.oracle.gui.constraints;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * View for displaying check constraints on a Oracle table.
 * 
 * @author Jeff Tassin
 */
public class OracleConstraintsView extends TSPanel {
	/** the table that displays the triggers */
	private JTable m_table;

	/* model for the exported keys */
	private OracleConstraintsModel m_model;

	/**
	 * ctor
	 */
	public OracleConstraintsView(OracleConstraintsModel model) {
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

		// TableUtils.setIconDecorator( m_table,
		// OracleConstraintsModel.NAME_COLUMN,
		// TSGuiToolbox.loadImage("constraint16.gif") );

		// try to set some reasonable initial widths of the table columns here
		TableUtils.setColumnWidth(m_table, OracleConstraintsModel.NAME_COLUMN, 25);
		TableUtils.setColumnWidth(m_table, OracleConstraintsModel.TYPE_COLUMN, 10);
		TableUtils.setColumnWidth(m_table, OracleConstraintsModel.DEFINITION_COLUMN, 45);
		TableUtils.setColumnWidth(m_table, OracleConstraintsModel.LAST_MODIFIED, 25);

		return tpanel;
	}

	/**
	 * @return the underlying data model
	 */
	public OracleConstraintsModel getModel() {
		return m_model;
	}

}
