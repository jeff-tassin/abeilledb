package com.jeta.abeille.gui.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.MetaDataTableRenderer;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.layouts.AssignmentPanelLayout;

import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.JETATableModel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * 
 * @author Jeff Tassin
 */
public class ColumnAssignmentPanel extends TSPanel {
	/** the table that displays the objects that we can assign */
	private JTable m_sourcetable;

	/** the data model for the source table */
	private JETATableModel m_sourcemodel;

	/** that table that displays the objects that have been assigned */
	private JTable m_assignedtable;
	/** the data model for the assigned table */
	private JETATableModel m_assignedmodel;

	private TSConnection m_connection;

	/** command ids */
	public static final String ID_ASSIGN_COLUMN = "assign.column";
	public static final String ID_REMOVE_COLUMN = "remove.column";

	/** component ids */
	public static final String ID_ASSIGNED_TABLE = "assigned.table";
	public static final String ID_SOURCE_TABLE = "source.table";

	/**
	 * ctor
	 */
	public ColumnAssignmentPanel(TSConnection conn) {
		m_connection = conn;
		setLayout(new BorderLayout());
		add(createView(), BorderLayout.CENTER);
		setController(new ColumnAssignmentPanelController(this));
		// addLocalColumns( localtmd.getColumns() );

		if (m_sourcetable.getRowCount() > 0) {
			m_sourcetable.setRowSelectionInterval(0, 0);
			m_sourcetable.setColumnSelectionInterval(0, 0);
		}

		if (m_assignedtable.getRowCount() > 0) {
			m_assignedtable.setRowSelectionInterval(0, 0);
			m_assignedtable.setColumnSelectionInterval(0, 1);
		}
	}

	/**
	 * Adds a column to the primary assigned list
	 */
	public void assignColumn(ColumnMetaData cmd) {
		if (!m_assignedmodel.contains(cmd)) {
			m_assignedmodel.addRow(cmd);
		}
	}

	/**
	 * Adds the collection of columns to the right table
	 * 
	 * @param cols
	 *            a collection of Objects
	 */
	public void addAssignedColumns(Collection cols) {
		m_assignedmodel.removeAll();
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			assert (obj instanceof ColumnMetaData);
			m_assignedmodel.addRow(obj);
		}
	}

	/**
	 * Adds the collection of columns to the left table
	 * 
	 * @param cols
	 *            a collection of Objects
	 */
	public void addSourceColumns(Collection cols) {
		m_sourcemodel.removeAll();
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			assert (obj instanceof ColumnMetaData);
			m_sourcemodel.addRow(obj);
		}
	}

	/**
	 * Removes a column from the assigned list
	 */
	public void clearAssignment(ColumnMetaData cmd) {
		m_assignedmodel.remove(cmd);
	}

	/**
	 * Creates the component that displays the list of assigned columns in the
	 * foreign key
	 */
	private JComponent createAssignedColumnsPanel() {
		m_assignedmodel = new JETATableModel();
		String[] names = new String[1];
		names[0] = I18N.getLocalizedMessage("Assigned Columns");
		m_assignedmodel.setColumnNames(names);

		Class[] coltypes = new Class[1];
		coltypes[0] = ColumnMetaData.class;
		m_assignedmodel.setColumnTypes(coltypes);

		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_assignedmodel, false);
		m_assignedtable = tpanel.getTable();
		m_assignedtable.setName(ID_ASSIGNED_TABLE);
		tpanel.getScrollPane(m_assignedtable).getViewport()
				.setBackground(javax.swing.UIManager.getColor("Table.background"));
		m_assignedtable.setShowGrid(false);
		TableColumn tcol = m_assignedtable.getColumnModel().getColumn(0);
		tcol.setCellRenderer(MetaDataTableRenderer.createRenderer(m_connection));
		return tpanel;
	}

	/**
	 * Creates the component that displays the list of available columns that we
	 * can use to create the foreign key
	 */
	private JComponent createSourceColumnsPanel() {
		m_sourcemodel = new JETATableModel();
		String[] names = new String[1];
		names[0] = I18N.getLocalizedMessage("Available Columns");
		m_sourcemodel.setColumnNames(names);

		Class[] coltypes = new Class[1];
		coltypes[0] = ColumnMetaData.class;
		m_sourcemodel.setColumnTypes(coltypes);

		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_sourcemodel, true);
		m_sourcetable = tpanel.getTable();
		m_sourcetable.setName(ID_SOURCE_TABLE);
		tpanel.getScrollPane(m_sourcetable).getViewport()
				.setBackground(javax.swing.UIManager.getColor("Table.background"));

		m_sourcetable.setShowGrid(false);

		TableColumn tcol = m_sourcetable.getColumnModel().getColumn(0);
		tcol.setCellRenderer(MetaDataTableRenderer.createRenderer(m_connection));

		return tpanel;
	}

	/**
	 * Creates the columns panel at the bottom half of the view This panel
	 * contains two lists [available cols][move btns][assigned cols]
	 * 
	 */
	private JComponent createView() {
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

		JComponent left = createSourceColumnsPanel();
		JComponent right = createAssignedColumnsPanel();

		panel.add(left);
		panel.add(btnpanel);
		panel.add(right);

		panel.setLayout(new AssignmentPanelLayout(left, btnpanel, right));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));
		return panel;
	}

	/**
	 * @return the preferred size for the view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(7, 8);
	}

	/**
	 * @return a collection of ColumnMetaData objects that are in the assigned
	 *         model
	 */
	public Collection getAssignedColumns() {
		return m_assignedmodel.getData();
	}

	/**
    */
	public Object getSelectedAssignedObject() {
		int row = m_assignedtable.getSelectedRow();
		if (row >= 0)
			return m_assignedtable.getValueAt(row, 0);
		else
			return null;
	}

	/**
    */
	public Object getSelectedSourceObject() {
		int row = m_sourcetable.getSelectedRow();
		if (row >= 0) {
			return m_sourcetable.getValueAt(row, 0);
		} else
			return null;
	}

}
