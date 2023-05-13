package com.jeta.abeille.gui.importer;

import java.awt.BorderLayout;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import com.jeta.abeille.gui.common.MetaDataTableRenderer;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a list of columns used in the ImportBuilder
 * 
 * @author Jeff Tassin
 */
public class TargetColumnsView extends TSPanel {
	private TSTablePanel m_tablepanel;
	private JTable m_table;
	private TargetColumnsModel m_model;

	/*
	 * ctor
	 */
	public TargetColumnsView(TargetColumnsModel model) {
		m_model = model;
		assert (m_model != null);

		setLayout(new BorderLayout());

		m_tablepanel = TableUtils.createSimpleTable(m_model, false);
		add(m_tablepanel, BorderLayout.CENTER);

		m_table = m_tablepanel.getTable();
		TableColumnModel cmodel = m_table.getColumnModel();
		// cmodel.getColumn( TargetColumnsModel.TABLE_NAME_COLUMN
		// ).setCellRenderer( new MetaDataTableRenderer() );
		// cmodel.getColumn( TargetColumnsModel.COLUMN_NAME_COLUMN
		// ).setCellRenderer( new MetaDataTableRenderer() );
		// cmodel.getColumn( TargetColumnsModel.VALUE_COLUMN ).setCellRenderer(
		// new MetaDataTableRenderer() );
	}

	/**
	 * @return the underlying data model for the view
	 */
	public TargetColumnsModel getModel() {
		return m_model;
	}

	/**
	 * @return the main table for this view
	 */
	public JTable getTable() {
		return m_table;
	}

	/**
	 * @return the table panel that contains the main table for the view
	 */
	public TSTablePanel getTablePanel() {
		return m_tablepanel;
	}

}
