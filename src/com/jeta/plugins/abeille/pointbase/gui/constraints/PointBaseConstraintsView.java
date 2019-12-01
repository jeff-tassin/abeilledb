package com.jeta.plugins.abeille.pointbase.gui.constraints;

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
 * View for displaying check constraints on a PointBase table.
 * 
 * @author Jeff Tassin
 */
public class PointBaseConstraintsView extends TSPanel {
	/** the table that displays the triggers */
	private JTable m_table;

	/* model for the exported keys */
	private PointBaseConstraintsModel m_model;

	/**
	 * ctor
	 */
	public PointBaseConstraintsView(PointBaseConstraintsModel model) {
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

		// try to set some reasonable initial widths of the table columns here
		TableUtils.setColumnWidth(m_table, PointBaseConstraintsModel.DEFINITION_COLUMN, 75);

		return tpanel;
	}

	/**
	 * @return the underlying data model
	 */
	public PointBaseConstraintsModel getModel() {
		return m_model;
	}

}
