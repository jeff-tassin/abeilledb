package com.jeta.abeille.gui.indexes.mysql;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.MetaDataPopupRenderer;
import com.jeta.abeille.gui.common.MetaDataTableRenderer;

import com.jeta.abeille.gui.indexes.IndexColumn;
import com.jeta.abeille.gui.indexes.TableIndex;

import com.jeta.foundation.gui.components.TextFieldwButtonPanel;
import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSListPanel;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.layouts.AssignmentPanelLayout;

import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class implements a view for a single index on a MySQL table
 * 
 * @author Jeff Tassin
 */
public class MySQLIndexView extends TSPanel {
	/** the data connection */
	private TSConnection m_connection;

	/** the table id for this index */
	private TableId m_tableid;

	private JTextField m_namefield = new JTextField();
	private JCheckBox m_uniquecheck = new JCheckBox(I18N.getLocalizedMessage("Unique"));

	private TSComboBox m_typecombo = new TSComboBox();

	private DefaultTableModel m_assignedmodel;
	private JTable m_assignedtable;

	private DefaultTableModel m_availablemodel;
	private JTable m_availabletable;

	/** component ids */
	public static final String ID_AVAILABLE_TABLE = "available.table";
	public static final String ID_ASSIGNED_TABLE = "assigned.table";

	/** command ids */
	public static final String ID_ADD_COLUMN = "add.column";
	public static final String ID_REMOVE_COLUMN = "remove.column";

	/** dummy object for adding rows */
	private static Object[] m_row1 = new Object[1];
	private static Object[] m_row2 = new Object[2];

	/**
	 * ctor
	 */
	public MySQLIndexView(TSConnection connection, TableId tableId) {
		m_connection = connection;
		m_tableid = tableId;
		setLayout(new BorderLayout());
		add(createPropertiesPanel(), BorderLayout.NORTH);
		add(createColumnsPanel(), BorderLayout.CENTER);

		loadColumns(tableId);

		setController(new MySQLIndexViewController(this));

		/** add supported MySQL index types to type combo */
		String firsttype = "FULLTEXT";
		Collection itypes = MySQLIndexTypes.getTypes();
		Iterator iter = itypes.iterator();
		while (iter.hasNext()) {
			String itype = (String) iter.next();
			m_typecombo.addItem(itype);
			if (firsttype == null)
				firsttype = itype;
		}
		m_typecombo.setSelectedItem(firsttype);

		m_uniquecheck.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (m_uniquecheck.isSelected()) {
					m_typecombo.setSelectedItem("BTREE");
					m_typecombo.setEnabled(false);
				} else {
					m_typecombo.setSelectedItem("FULLTEXT");
					m_typecombo.setEnabled(true);
				}
			}
		});
	}

	/**
	 * ctor
	 */
	public MySQLIndexView(TSConnection connection, TableId tableId, TableIndex index) {
		this(connection, tableId);
		if (index != null) {
			loadData(index);
			m_namefield.setEnabled(false);
		}
	}

	/**
	 * Assigns a column to this index
	 */
	void assignColumn(ColumnMetaData cmd) {
		if (cmd != null) {
			if (!TableUtils.contains(m_assignedtable, cmd, 0)) {
				m_row2[0] = cmd;
				m_row2[1] = null;
				m_assignedmodel.addRow(m_row2);
			}
		}
	}

	/**
	 * Creates the column assignment table
	 */
	private JComponent createAssignmentTable() {
		m_assignedmodel = new DefaultTableModel();
		m_assignedmodel.addColumn(I18N.getLocalizedMessage("Assigned Columns"));
		m_assignedmodel.addColumn(I18N.getLocalizedMessage("Size"));

		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_assignedmodel, false);
		m_assignedtable = tpanel.getTable();
		m_assignedtable.setName(ID_ASSIGNED_TABLE);
		tpanel.getScrollPane(m_assignedtable).getViewport()
				.setBackground(javax.swing.UIManager.getColor("Table.background"));

		m_assignedtable.setShowGrid(false);

		TableUtils.setColumnWidth(m_assignedtable, 1, 5);

		TableColumn tcol = m_assignedtable.getColumnModel().getColumn(0);
		tcol.setCellRenderer(MetaDataTableRenderer.createRenderer(m_connection));
		return tpanel;
	}

	/**
	 * Create the table that displays the available columns we can choose for
	 * the index
	 */
	private JComponent createAvailableTable() {
		m_availablemodel = new DefaultTableModel();
		m_availablemodel.addColumn(I18N.getLocalizedMessage("Available Columns"));

		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_availablemodel, false);
		m_availabletable = tpanel.getTable();
		m_availabletable.setName(ID_AVAILABLE_TABLE);
		tpanel.getScrollPane(m_availabletable).getViewport()
				.setBackground(javax.swing.UIManager.getColor("Table.background"));

		m_availabletable.setShowGrid(false);

		TableColumn tcol = m_availabletable.getColumnModel().getColumn(0);
		tcol.setCellRenderer(MetaDataTableRenderer.createRenderer(m_connection));
		return tpanel;
	}

	/**
	 * create the top view
	 */
	private JComponent createColumnsPanel() {
		JPanel panel = new JPanel();

		JComponent availpanel = createAvailableTable();
		panel.add(availpanel);

		JPanel btnpanel = new JPanel();
		btnpanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
		btnpanel.setLayout(new BoxLayout(btnpanel, BoxLayout.Y_AXIS));
		JButton addbtn = createButton(TSGuiToolbox.loadImage("navigation/Forward16.gif"), ID_ADD_COLUMN);
		JButton removebtn = createButton(TSGuiToolbox.loadImage("navigation/Back16.gif"), ID_REMOVE_COLUMN);

		Dimension d = new Dimension(32, 24);
		addbtn.setPreferredSize(d);
		addbtn.setMinimumSize(d);
		addbtn.setMaximumSize(d);

		removebtn.setPreferredSize(d);
		removebtn.setMinimumSize(d);
		removebtn.setMaximumSize(d);

		btnpanel.add(addbtn);
		btnpanel.add(removebtn);
		panel.add(btnpanel);

		JComponent assignedview = createAssignmentTable();
		panel.add(assignedview);

		panel.setLayout(new AssignmentPanelLayout(availpanel, btnpanel, assignedview));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 10, 15));
		return panel;
	}

	/**
	 * create the top view
	 */
	private JComponent createPropertiesPanel() {

		Component[] left = new Component[2];
		left[0] = new JLabel(I18N.getLocalizedMessage("Name"));
		left[1] = new JLabel(I18N.getLocalizedMessage("Type"));

		JPanel type_panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
		type_panel.add(m_typecombo);
		type_panel.add(Box.createHorizontalStrut(20));
		type_panel.add(m_uniquecheck);
		// m_typecombo.setEnabled( false );

		Component[] right = new Component[3];
		right[0] = m_namefield;
		right[1] = type_panel;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_namefield, 30);
		layout.setMaxTextFieldWidth(m_typecombo, 15);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, left, right, new java.awt.Insets(10, 10, 0, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * @return a collection of column names (IndexColumn objects) that are
	 *         assigned to this index
	 */
	public Collection getAssignedColumns() throws NumberFormatException {
		TableUtils.stopEditing(m_assignedtable);

		LinkedList result = new LinkedList();

		for (int row = 0; row < m_assignedmodel.getRowCount(); row++) {
			IndexColumn icol = new IndexColumn();
			ColumnMetaData cmd = (ColumnMetaData) m_assignedmodel.getValueAt(row, 0);
			icol.setName(cmd.getColumnName());
			String lenstr = (String) m_assignedmodel.getValueAt(row, 1);
			if (lenstr != null && lenstr.trim().length() > 0) {
				icol.setAttribute(TSUtils.getInteger(Integer.parseInt(lenstr)));
			}
			result.add(icol);
		}
		return result;
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the name entered for this index
	 */
	public String getName() {
		return TSUtils.fastTrim(m_namefield.getText());
	}

	/**
	 * Creates and returns an index based on the component values in this view
	 * 
	 * @return the newly created index
	 */
	public TableIndex createTableIndex() {
		String iname = getName();
		boolean unique = m_uniquecheck.isSelected();
		String type = getType();
		Collection columns = getAssignedColumns();
		TableIndex index = new TableIndex(m_tableid);
		index.setName(iname);
		index.setUnique(unique);
		index.setType(type);
		index.setIndexColumns(columns);
		return index;
	}

	/**
	 * @return the type entered for this index
	 */
	public String getType() {
		return TSUtils.fastTrim(m_typecombo.getText());
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(7, 13);
	}

	/**
	 * Loads the columns from the given table id into the 'Available Cols' list
	 */
	private void loadColumns(TableId id) {
		m_availablemodel.setRowCount(0);

		TableMetaData tmd = m_connection.getTable(id);
		if (tmd != null) {
			for (int index = 0; index < tmd.getColumnCount(); index++) {
				ColumnMetaData cmd = tmd.getColumn(index);
				m_row1[0] = cmd;
				m_availablemodel.addRow(m_row1);
			}
		}
	}

	/**
	 * Loads the data from an existing index into the view
	 */
	private void loadData(TableIndex index) {
		if (index == null || m_tableid == null)
			return;

		m_namefield.setText(index.getName());
		m_uniquecheck.setSelected(index.isUnique());
		m_typecombo.setSelectedItem(index.getType());
		m_typecombo.setEnabled(!index.isUnique());

		m_assignedmodel.setRowCount(0);

		TableMetaData tmd = m_connection.getTable(m_tableid);
		if (tmd != null) {
			Collection cols = index.getIndexColumns();
			Iterator iter = cols.iterator();
			while (iter.hasNext()) {
				IndexColumn icol = (IndexColumn) iter.next();
				ColumnMetaData cmd = tmd.getColumn(icol.getName());
				if (cmd != null) {
					m_row2[0] = cmd;
					Integer len = (Integer) icol.getAttribute();
					if (len == null || len.intValue() == 0)
						m_row2[1] = null;
					else
						m_row2[1] = len;
					m_assignedmodel.addRow(m_row2);
				}
			}
		}

	}

}
