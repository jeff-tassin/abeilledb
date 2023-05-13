package com.jeta.abeille.gui.keys;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.jeta.abeille.gui.common.MetaDataTableModel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;

import com.jeta.foundation.i18n.I18N;

/**
 * @author Jeff Tassin
 */
public class KeysView extends TSPanel {
	/** the table that displays the triggers */
	private JTable m_table;

	/* model for the exported keys */
	private MetaDataTableModel m_model;

	/**
	 * ctor
	 */
	public KeysView(MetaDataTableModel model) {
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
	public Key getSelectedIndex() {
		int row = m_table.getSelectedRow();
		if (row >= 0) {
			return (Key) m_model.getRow(row);
		} else {
			return null;
		}
	}
}
