package com.jeta.abeille.gui.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.PopupList;
import com.jeta.foundation.gui.components.SortedListModel;

import com.jeta.foundation.i18n.I18N;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.ColumnMetaDataComparator;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableMetaDataComparator;
import com.jeta.abeille.database.model.TSConnection;

/**
 * This class allows the user to select a table and column from a set of tables.
 * 
 * @author Jeff Tassin
 */
public class ColumnSelectorPanel extends TSPanel {
	private TSComboBox m_columncombo;
	private TableSelectorPanel m_tableselectorpanel;

	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public ColumnSelectorPanel(TSConnection conn, TableSelectorModel model) {
		m_connection = conn;
		initialize(null, null);
		m_tableselectorpanel.setModel(model);
	}

	/**
	 * Support adding other components that have labels aligned with schema and
	 * table selector labels.
	 */
	public ColumnSelectorPanel(TSConnection conn, TableSelectorModel model, JLabel[] labels, JComponent[] comps) {
		m_connection = conn;
		initialize(labels, comps);
		m_tableselectorpanel.setModel(model);
	}

	/**
	 * @return the columns combo box
	 */
	public TSComboBox getColumnsCombo() {
		return m_columncombo;
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		Dimension d = m_tableselectorpanel.getPreferredSize();
		Dimension combod = m_columncombo.getPreferredSize();
		d.height += combod.height;
		return d;
	}

	/**
	 * @return the selected column.
	 */
	public ColumnMetaData getSelectedColumn() {
		return (ColumnMetaData) m_columncombo.getSelectedItem();
	}

	/**
	 * @return the selected table
	 */
	public TableId getSelectedTable() {
		return (TableId) m_tableselectorpanel.getTablesCombo().getSelectedItem();
	}

	/**
	 * @return the table selector panel
	 */
	public TableSelectorPanel getTableSelectorPanel() {
		return m_tableselectorpanel;
	}

	/**
	 * Creates and initializes the components on this panel
	 */
	void initialize(JLabel[] alabels, JComponent[] acomps) {
		JLabel[] labels = null;
		JComponent[] comps = null;
		m_columncombo = new TSComboBox();

		if (alabels != null && acomps != null) {
			labels = new JLabel[alabels.length + 1];
			comps = new JComponent[alabels.length + 1];

			labels[0] = new JLabel(I18N.getLocalizedMessage("Column"));
			comps[0] = m_columncombo;

			for (int index = 1; index < labels.length; index++) {
				labels[index] = alabels[index - 1];
				comps[index] = acomps[index - 1];
			}
		} else {
			labels = new JLabel[1];
			labels[0] = new JLabel(I18N.getLocalizedMessage("Column"));

			comps = new JComponent[1];
			comps[0] = m_columncombo;
		}

		PopupList slist = m_columncombo.getPopupList();
		slist.setRenderer(new MetaDataPopupRenderer(getConnection()));

		setLayout(new BorderLayout());
		m_tableselectorpanel = new TableSelectorPanel(getConnection(), labels, comps, false);

		add(m_tableselectorpanel, BorderLayout.NORTH);

		final TSComboBox tablescombo = m_tableselectorpanel.getTablesCombo();
		tablescombo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				loadColumns(m_tableselectorpanel.getCatalog(), m_tableselectorpanel.getSchema(),
						m_tableselectorpanel.getTableName());
			}
		});

	}

	/**
	 * Loads the columns for a given table into the columns combo box
	 * 
	 * @param schema
	 *            the selected schema that owns the table
	 * @param tableName
	 *            the name of the table whose columns we wish to load
	 */
	void loadColumns(Catalog catalog, Schema schema, String tableName) {
		String coltxt = m_columncombo.getText();
		m_columncombo.removeAllItems();

		SortedListModel listmodel = new SortedListModel();
		listmodel.setComparator(new ColumnMetaDataComparator());

		TableId id = new TableId(catalog, schema, tableName);
		TableMetaData tmd = m_tableselectorpanel.getModel().getTable(id);
		if (tmd != null) {
			Collection cols = tmd.getColumns();
			Iterator iter = cols.iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				listmodel.add(cmd);
			}
		}

		PopupList list = m_columncombo.getPopupList();
		list.setModel(listmodel);

		/** restore what was in the column combo */
		m_columncombo.setSelectedItem(coltxt);
		m_columncombo.selectEditorText();
	}

	/**
	 * Sets the column displayed in the panel
	 */
	public void setColumn(ColumnMetaData cmd) {
		TableId tableid = cmd.getParentTableId();
		if (tableid != null) {
			m_tableselectorpanel.setTableId(tableid);
			loadColumns(tableid.getCatalog(), tableid.getSchema(), tableid.getTableName());
		}
		m_columncombo.setSelectedItem(cmd);

		// System.out.println(
		// "ColumnSelectorPanel.setColumn   selected column: " +
		// m_columncombo.getSelectedItem() );
	}

}
