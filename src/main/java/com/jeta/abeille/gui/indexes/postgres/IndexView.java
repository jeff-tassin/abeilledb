package com.jeta.abeille.gui.indexes.postgres;

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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.MetaDataPopupRenderer;

import com.jeta.abeille.gui.indexes.TableIndex;

import com.jeta.foundation.gui.components.TextFieldwButtonPanel;
import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSListPanel;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.layouts.AssignmentPanelLayout;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class implements a view for a single index on a table
 * 
 * @author Jeff Tassin
 */
public class IndexView extends TSPanel {
	/** the data connection */
	private TSConnection m_connection;

	/** the table id for this index */
	private TableId m_tableid;

	private JTextField m_namefield = new JTextField();
	private JCheckBox m_uniquecheck = new JCheckBox(I18N.getLocalizedMessage("Unique"));
	private JCheckBox m_primarycheck = new JCheckBox(I18N.getLocalizedMessage("Primary"));

	/** check box for converting this index into a functional index */
	private JCheckBox m_functioncheck = new JCheckBox(I18N.getLocalizedMessage("Functional"));

	/**
	 * text field that allows the user to assign a function to this
	 * functionindex
	 */
	private JTextField m_functionfield = new JTextField();

	private TSComboBox m_typecombo = new TSComboBox();

	private TSListPanel m_indexcolslist = new TSListPanel();
	private TSListPanel m_tablecolslist = new TSListPanel();

	/** component ids */
	public static final String ID_AVAILABLE_COLS_LIST = "available.cols.list";
	public static final String ID_ASSIGNED_COLS_LIST = "assigned.cols.list";
	public static final String ID_FUNCTION_FIELD = "function.name.field";

	/** command ids */
	public static final String ID_ADD_COLUMN = "add.column";
	public static final String ID_REMOVE_COLUMN = "remove.column";
	public static final String ID_SHOW_PROCEDURE_BROWSER = "show.procedure.browser";

	/**
	 * ctor
	 */
	public IndexView(TSConnection connection, TableId tableId) {
		m_connection = connection;
		m_tableid = tableId;
		setLayout(new BorderLayout());
		add(createPropertiesPanel(), BorderLayout.NORTH);
		add(createColumnsPanel(), BorderLayout.CENTER);

		loadColumns(tableId);

		setController(new IndexViewController(this));

		/** add supported PostgreSQL index types to type combo */
		String firsttype = null;
		Collection itypes = IndexTypes.getTypes();
		Iterator iter = itypes.iterator();
		while (iter.hasNext()) {
			String itype = (String) iter.next();
			m_typecombo.addItem(itype);
			if (firsttype == null)
				firsttype = itype;
		}
		m_typecombo.setSelectedItem(firsttype);
	}

	/**
	 * ctor
	 */
	public IndexView(TSConnection connection, TableId tableId, TableIndex index) {
		this(connection, tableId);
		if (index != null) {
			loadData(index);
			m_namefield.setEnabled(false);
		}
	}

	/**
	 * create the top view
	 */
	private JComponent createColumnsPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
		m_tablecolslist.setHeadingText(I18N.getLocalizedMessage("Table Columns"));
		JList tablelist = m_tablecolslist.getJList();
		tablelist.setName(ID_AVAILABLE_COLS_LIST);
		tablelist.setCellRenderer(MetaDataPopupRenderer.createInstance(m_connection));
		panel.add(m_tablecolslist);

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

		m_indexcolslist.setHeadingText(I18N.getLocalizedMessage("Assigned Columns"));
		JList indexcolslist = m_indexcolslist.getJList();
		indexcolslist.setName(ID_ASSIGNED_COLS_LIST);
		indexcolslist.setCellRenderer(MetaDataPopupRenderer.createInstance(m_connection));
		panel.add(m_indexcolslist);

		panel.setLayout(new AssignmentPanelLayout(m_tablecolslist, btnpanel, m_indexcolslist));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 10, 15));
		return panel;
	}

	/**
	 * create the top view
	 */
	private JComponent createPropertiesPanel() {

		Component[] left = new Component[4];
		left[0] = new JLabel(I18N.getLocalizedMessage("Name"));
		left[1] = m_primarycheck;
		left[2] = m_functioncheck;
		left[3] = new JLabel(I18N.getLocalizedMessage("Type"));

		// always disabled because we don't allow the user to edit indexes for
		// the primary key
		m_primarycheck.setEnabled(false);
		m_typecombo.setEnabled(false);

		JButton fbtn = createButton(TSGuiToolbox.loadImage("ellipsis16.gif"), ID_SHOW_PROCEDURE_BROWSER);
		m_functionfield = new JTextField();
		m_functionfield.setName(ID_FUNCTION_FIELD);
		TextFieldwButtonPanel functionpanel = new TextFieldwButtonPanel(m_functionfield, fbtn);

		Component[] right = new Component[4];
		right[0] = m_namefield;
		right[1] = m_uniquecheck;
		right[2] = functionpanel;
		right[3] = m_typecombo;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_namefield, 30);
		layout.setMaxTextFieldWidth(functionpanel, 30);
		layout.setMaxTextFieldWidth(m_typecombo, 15);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, left, right, new java.awt.Insets(10, 10, 0, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * @return a collection of column names (String objects) that are assigned
	 *         to this index
	 */
	public Collection getAssignedColumns() {
		LinkedList result = new LinkedList();
		DefaultListModel listmodel = (DefaultListModel) m_indexcolslist.getModel();
		for (int index = 0; index < listmodel.size(); index++) {
			ColumnMetaData cmd = (ColumnMetaData) listmodel.getElementAt(index);
			result.add(cmd.getColumnName());
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
	 * @return the name of the function in the function text box
	 */
	public String getFunctionName() {
		return m_functionfield.getText().trim();
	}

	/**
	 * @return the name entered for this index
	 */
	public String getName() {
		return m_namefield.getText().trim();
	}

	/**
	 * Creates and returns an index based on the component values in this view
	 * 
	 * @return the newly created index
	 */
	public TableIndex getTableIndex() {
		String iname = getName();
		boolean unique = m_uniquecheck.isSelected();

		boolean functional = m_functioncheck.isSelected();

		String function = m_functionfield.getText().trim();

		String type = getType();

		Collection columns = getAssignedColumns();

		TableIndex index = new TableIndex(m_tableid);
		index.setName(iname);
		index.setUnique(unique);
		index.setFunctional(functional);
		index.setFunction(function);
		index.setType(type);
		index.setIndexColumns(columns);
		return index;
	}

	/**
	 * @return the type entered for this index
	 */
	public String getType() {
		return m_typecombo.getText().trim();
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(6, 15);
	}

	/**
	 * @return true if the functional check box is selected
	 */
	public boolean isFunctional() {
		return m_functioncheck.isSelected();
	}

	/**
	 * Loads the columns from the given table id into the 'Available Cols' list
	 */
	private void loadColumns(TableId id) {
		DefaultListModel listmodel = (DefaultListModel) m_tablecolslist.getModel();
		listmodel.removeAllElements();

		TableMetaData tmd = m_connection.getTable(id);
		if (tmd != null) {
			for (int index = 0; index < tmd.getColumnCount(); index++) {
				ColumnMetaData cmd = tmd.getColumn(index);
				listmodel.addElement(cmd);
			}
		}
	}

	/**
	 * Loads the data from an existing index into the view
	 */
	private void loadData(TableIndex index) {
		if (index == null || m_tableid == null)
			return;

		TableMetaData tmd = m_connection.getTable(m_tableid);
		if (tmd != null) {
			m_namefield.setText(index.getName());
			m_uniquecheck.setSelected(index.isUnique());
			m_primarycheck.setSelected(index.isPrimaryKey());
			m_typecombo.setSelectedItem(index.getType());

			String fname = index.getFunction();
			if (fname != null && fname.trim().charAt(0) != '-') {
				m_functioncheck.setSelected(true);
			} else {
				m_functioncheck.setSelected(false);
			}
			m_functionfield.setText(fname);

			DefaultListModel listmodel = (DefaultListModel) m_indexcolslist.getModel();
			listmodel.removeAllElements();
			Iterator iter = index.getIndexColumns().iterator();
			while (iter.hasNext()) {
				String colname = (String) iter.next();
				ColumnMetaData cmd = tmd.getColumn(colname);
				if (cmd != null) {
					listmodel.addElement(cmd);
				}
			}
		}
	}

}
