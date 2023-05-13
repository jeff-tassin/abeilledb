package com.jeta.abeille.gui.modeler;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.MetaDataTableRenderer;

import com.jeta.foundation.gui.components.TSHeaderPanel;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * 
 * @author Jeff Tassin
 */
public class ForeignKeyColumnAssignmentView extends TSPanel {
	/** the local table id */
	private TableId m_localtableid;

	/** displays all columns in the local table */
	private JTable m_localtable;

	/** the columns in the local table */
	private DefaultTableModel m_localmodel;

	/** the underlying database connection */
	private TSConnection m_connection;

	private AssignedForeignKeyColumnsView m_assignedview;

	/** command ids */
	public static final String ID_ASSIGN_COLUMN = "add.column";
	public static final String ID_REMOVE_COLUMN = "remove.column";

	/** component ids */
	public static final String ID_AVAILABLE_TABLE = "available.table";

	/**
	 * ctor
	 */
	public ForeignKeyColumnAssignmentView(TSConnection conn, ColumnMetaData[][] assignments, TableId localid,
			Collection localCols, TableMetaData reftmd) {
		m_connection = conn;

		m_localtableid = localid;
		setLayout(new BorderLayout());
		add(createColumnsPanel(), BorderLayout.CENTER);
		setController(new ForeignKeyColumnAssignmentController(this));

		addLocalColumns(localCols);
		m_assignedview.setForeignKey(assignments, localCols, reftmd);

		JTable table = m_assignedview.getTable();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
			table.setColumnSelectionInterval(0, 1);
		}

		if (m_localtable.getRowCount() > 0) {
			m_localtable.setRowSelectionInterval(0, 0);
			m_localtable.setColumnSelectionInterval(0, 0);
		}
	}

	/**
	 * Adds the collection of columns to the local table
	 * 
	 * @param cols
	 *            a collection of ColumnMetaData objects
	 */
	void addLocalColumns(Collection cols) {
		m_localmodel.setRowCount(0);
		Object[] row = new Object[1];
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			row[0] = iter.next();
			m_localmodel.addRow(row);
		}
	}

	/**
	 * Assigns the given column from the local table to the primary key column
	 * of the reference table
	 */
	public void assignColumn(ColumnMetaData assignee, ColumnMetaData primaryCol) {
		m_assignedview.assignColumn(assignee, primaryCol);
	}

	/**
	 * Clears the assignment for the given primary key column of the reference
	 * table
	 * 
	 * @param primaryCol
	 *            the column in the assigned table whose assignment we wish to
	 *            clear
	 */
	public void clearAssignment(ColumnMetaData primaryCol) {
		m_assignedview.clearAssignment(primaryCol);
	}

	/**
	 * Creates the component that displays the list of assigned columns in the
	 * foreign key
	 */
	private JComponent createAssignedColumnsPanel() {
		m_assignedview = new AssignedForeignKeyColumnsView(m_connection, m_localtableid, true);
		return m_assignedview;
	}

	/**
	 * Creates the component that displays the list of available columns that we
	 * can use to create the foreign key
	 */
	private JComponent createAvailableColumnsPanel() {
		m_localmodel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		m_localmodel.addColumn(m_localtableid.getTableName());

		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_localmodel, false);
		m_localtable = tpanel.getTable();
		m_localtable.setName(ID_AVAILABLE_TABLE);
		tpanel.getScrollPane(m_localtable).getViewport()
				.setBackground(javax.swing.UIManager.getColor("Table.background"));

		m_localtable.setShowGrid(false);

		TableColumn tcol = m_localtable.getColumnModel().getColumn(0);
		tcol.setCellRenderer(MetaDataTableRenderer.createRenderer(m_connection));

		TSHeaderPanel headerpanel = new TSHeaderPanel(tpanel);
		headerpanel.setHeadingText(I18N.getLocalizedMessage("Available Columns"));
		return headerpanel;
	}

	/**
	 * Creates the columns panel at the bottom half of the view This panel
	 * contains two lists [available cols][move btns][assigned cols]
	 * 
	 */
	private JComponent createColumnsPanel() {
		JPanel panel = new JPanel();

		JPanel btnpanel = new JPanel();
		btnpanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		btnpanel.setLayout(new BoxLayout(btnpanel, BoxLayout.Y_AXIS));
		JButton addbtn = createButton(TSGuiToolbox.loadImage("navigation/Forward16.gif"), ID_ASSIGN_COLUMN);
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

		JComponent left = createAvailableColumnsPanel();
		JComponent right = createAssignedColumnsPanel();

		panel.add(left);
		panel.add(btnpanel);
		panel.add(right);

		panel.setLayout(new AssignmentPanelLayout(left, btnpanel, right));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));
		return panel;
	}

	/**
	 * @return the columns assignments
	 */
	public ColumnMetaData[][] getAssignments() {
		return m_assignedview.getAssignments();
	}

	/**
	 * @return the preferred size for the view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(7, 8);
	}

	/**
	 * @return the column meta data object that is selected in the available
	 *         columns list. Null is returned if no object is selected.
	 */
	public ColumnMetaData getSelectedLocalColumn() {
		int row = m_localtable.getSelectedRow();
		if (row >= 0)
			return (ColumnMetaData) m_localtable.getValueAt(row, 0);
		else
			return null;
	}

	/**
	 * @return the column meta data object that is selected in the assigned
	 *         columns table Null is returned if no row is selected.
	 */
	public ColumnMetaData getSelectedPrimaryKeyColumn() {
		return m_assignedview.getSelectedPrimaryKeyColumn();
	}

	/**
	 * This is the layout manager that is used to layout two list boxes (left
	 * and right). This is used to build assignment GUI's where the user can
	 * select/assign items from one list to another. The left and right
	 * components are used to hold the lists. The middle component is used for
	 * the container that contains the assignment buttons. [list][button
	 * panel][list]
	 */
	public class AssignmentPanelLayout implements java.awt.LayoutManager {
		private Dimension m_min = new Dimension(50, 50);

		private Component m_left;
		private Component m_middle;
		private Component m_right;

		public AssignmentPanelLayout(Component left, Component middle, Component right) {
			m_left = left;
			m_middle = middle;
			m_right = right;
		}

		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			java.awt.Insets insets = parent.getInsets();

			Dimension sz = parent.getSize();
			sz.width = sz.width - insets.left - insets.right;
			sz.height = sz.height - insets.top - insets.bottom;

			int y = insets.top;

			int leftwidth = TSGuiToolbox.calculateAverageTextWidth(m_left, 25);
			m_left.setSize(leftwidth, sz.height);
			m_left.setLocation(insets.left, y);

			Dimension md = m_middle.getPreferredSize();
			m_middle.setSize(md.width, sz.height);
			int my = (sz.height - md.height) / 2;
			if (my < 0)
				my = 0;
			m_middle.setLocation(insets.left + leftwidth, my);

			int rightwidth = sz.width - leftwidth - md.width;
			m_right.setSize(rightwidth, sz.height);
			m_right.setLocation(insets.left + leftwidth + md.width, y);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return m_min;
		}

		public Dimension preferredLayoutSize(Container parent) {
			return m_min;
		}

		public void removeLayoutComponent(Component comp) {
		}
	}
}
