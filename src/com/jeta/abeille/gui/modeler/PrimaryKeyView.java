package com.jeta.abeille.gui.modeler;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumnModel;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This panel displays the columns that make up a primary key for a table.
 * 
 * @author Jeff Tassin
 */
public class PrimaryKeyView extends TSPanel {
	private JTextField m_primaryKeyField;

	/** displays the columns that make up the primary key */
	private JTable m_pktable;
	/** this model defines the columns that make up the primary key */
	private ColumnsGuiModel m_pkmodel;

	/** the database connection */
	private TSConnection m_connection;

	/**
	 * this is the model of the available columns for the primary key. This
	 * model contains all columns in the table we are defining this key for
	 */
	private ColumnsGuiModel m_colsmodel;

	/**
	 * The table id. This is only valid if we are editing an existing table.
	 */
	private TableId m_tableid;

	public static final String ID_EDIT_PRIMARY_KEY = "edit.primary.key";
	public static final String ID_DELETE_PRIMARY_KEY = "delete.primary.key";

	public static final String ID_ALTER_TABLE_PRIMARY_KEY = "checked.feature.alter.table.primary.key";

	/**
	 * ctor
	 */
	public PrimaryKeyView(TSConnection conn, ColumnsGuiModel colmodel, String keyName, boolean prototype) {
		m_connection = conn;
		m_colsmodel = colmodel;

		setLayout(new BorderLayout());
		add(createPrimaryKeyPanel(prototype), BorderLayout.CENTER);
		if (keyName != null) {
			m_primaryKeyField.setText(keyName);
		}
	}

	/**
	 * Helper method to create a button
	 */
	private JButton _createButton(String id, String iconName, String tooltip) {
		JButton btn = i18n_createToolBarButton(iconName, id, tooltip);
		if (tooltip != null)
			btn.setToolTipText(tooltip);
		return btn;
	}

	/**
	 * Creates the toolbar buttons panel
	 */
	private JComponent createButtonPanel() {
		javax.swing.JToolBar toolbar = new javax.swing.JToolBar();
		toolbar.setFloatable(false);

		JButton btn = _createButton(ID_EDIT_PRIMARY_KEY, "incors/16x16/document_edit.png",
				I18N.getLocalizedMessage("Modify Primary Key"));
		toolbar.add(btn);

		btn = _createButton(ID_DELETE_PRIMARY_KEY, "incors/16x16/document_delete.png",
				I18N.getLocalizedMessage("Drop Primary Key"));
		toolbar.add(btn);

		return toolbar;
	}

	/**
	 * Creates the primary key panel. Called by initialize.
	 */
	private JPanel createPrimaryKeyPanel(boolean prototype) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel keyname = new JLabel(I18N.getResource("Key Name"));
		JLabel[] labels = { keyname };
		m_primaryKeyField = new JTextField(15);
		m_primaryKeyField.setEnabled(prototype);

		JComponent[] textfields = { m_primaryKeyField };

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_primaryKeyField, 20);
		JPanel controlspane = TSGuiToolbox.alignLabelTextRows(layout, labels, textfields);
		controlspane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		if (!m_connection.getDatabase().equals(Database.MYSQL)) {
			panel.add(controlspane, BorderLayout.NORTH);
		}

		JPanel colspanel = new JPanel(new BorderLayout());
		TSDatabase db = (TSDatabase) m_connection.getImplementation(TSDatabase.COMPONENT_ID);
		if (prototype) {
			colspanel.add(createButtonPanel(), BorderLayout.NORTH);
		}

		String[] cols = new String[1];
		cols[0] = I18N.getLocalizedMessage("Columns");

		m_pkmodel = new ColumnsGuiModel(m_connection, true, null, true);
		m_pktable = new JTable(m_pkmodel);
		TableColumnModel cmodel = m_pktable.getColumnModel();
		cmodel.getColumn(0).setWidth(32);
		cmodel.getColumn(0).setMaxWidth(32);
		cmodel.getColumn(0).setMinWidth(32);
		cmodel.getColumn(0).setPreferredWidth(32);
		cmodel.getColumn(0).setHeaderRenderer(new PrimaryKeyRenderer(true));
		cmodel.getColumn(0).setCellRenderer(new PrimaryKeyRenderer(false));

		cmodel.getColumn(2).setCellRenderer(new DataTypeRenderer());
		JScrollPane scrollpane = new JScrollPane(m_pktable);
		m_pktable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		colspanel.add(scrollpane, BorderLayout.CENTER);

		panel.add(colspanel, BorderLayout.CENTER);

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * @return the data model that defines the columns that are in the table
	 *         that contains this primary key
	 */
	public ColumnsGuiModel getColumnsModel() {
		return m_colsmodel;
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the model that defines the columns in the primary key
	 */
	public ColumnsGuiModel getPrimaryKeyColumns() {
		return m_pkmodel;
	}

	/**
	 * @return the number of columns defined in the key
	 */
	public int getPrimaryKeyColumnsCount() {
		return m_pkmodel.getRowCount();
	}

	/**
	 * @return the name of the primary key entered by the user
	 */
	public String getPrimaryKeyName() {
		return TSUtils.fastTrim(m_primaryKeyField.getText());
	}

	/**
	 * @return the table id. This is valid only if we are editing/viewing an
	 *         existing table
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * Loads the data from a ColumnsGuiModel. This is used when prototyping.
	 */
	public void loadData(ColumnsGuiModel cmodel) {
		m_pkmodel.removeAll();
		m_tableid = null;
		if (cmodel != null) {
			m_tableid = cmodel.getTableId();
			for (int row = 0; row < cmodel.getRowCount(); row++) {
				ColumnInfo info = (ColumnInfo) cmodel.getRow(row);
				if (info.isPrimaryKey()) {
					m_pkmodel.addRow(info);
				}
			}
		}

		if (m_connection != null || m_tableid != null) {
			TableMetaData tmd = m_connection.getTable(m_tableid);
			if (tmd != null) {
				DbKey dbkey = tmd.getPrimaryKey();
				if (dbkey != null) {
					m_primaryKeyField.setText(dbkey.getKeyName());
				}
			}
		}
	}

}
