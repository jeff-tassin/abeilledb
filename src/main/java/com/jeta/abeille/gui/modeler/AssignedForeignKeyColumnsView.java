package com.jeta.abeille.gui.modeler;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.common.MetaDataTableRenderer;

import com.jeta.foundation.gui.components.TSHeaderPanel;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This panel has a single table that shows the columns that are mapped to one
 * another in a foreign key.
 * 
 * @author Jeff Tassin
 */
public class AssignedForeignKeyColumnsView extends TSPanel {
	/** the id of the table that owns this foreign key */
	private TableId m_localtableid;

	/** the columns assigned in this key */
	private DefaultTableModel m_assignedmodel;

	/** displays the columns assigned in this key */
	private JTable m_assignedtable;

	private TSConnection m_connection;

	/** component ids */
	public static final String ID_ASSIGNED_TABLE = "assigned.table";

	/**
	 * ctor
	 * 
	 * @param localtableid
	 *            the table id of the local table
	 * @param showHeading
	 *            if true, we show a heading field that displays
	 *            "Assigned Columns"
	 */
	public AssignedForeignKeyColumnsView(TSConnection conn, TableId localtableid, boolean showHeading) {
		m_connection = conn;
		m_localtableid = localtableid;
		setLayout(new BorderLayout());
		add(createView(showHeading), BorderLayout.CENTER);
	}

	/**
	 * Assigns the given column from the local table to the primary key column
	 * of the reference table
	 */
	public void assignColumn(ColumnMetaData assignee, ColumnMetaData primaryCol) {
		if (primaryCol == null)
			return;

		for (int row = 0; row < m_assignedmodel.getRowCount(); row++) {
			if (primaryCol.equals(m_assignedmodel.getValueAt(row, 1))) {
				m_assignedmodel.setValueAt(assignee, row, 0);
			}
		}
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
		if (primaryCol == null)
			return;

		for (int row = 0; row < m_assignedmodel.getRowCount(); row++) {
			if (primaryCol.equals(m_assignedmodel.getValueAt(row, 1))) {
				m_assignedmodel.setValueAt(null, row, 0);
			}
		}
	}

	/**
	 * Creates the component that displays the list of assigned columns that we
	 * define the foreign key
	 */
	private JComponent createView(boolean showHeading) {
		m_assignedmodel = new DefaultTableModel() {
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};

		Object[] colnames = new Object[2];
		colnames[0] = m_localtableid.getTableName();
		colnames[1] = "";
		m_assignedmodel.setColumnIdentifiers(colnames);

		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_assignedmodel, false);
		m_assignedtable = tpanel.getTable();
		m_assignedtable.setName(ID_ASSIGNED_TABLE);
		m_assignedtable.setShowGrid(false);
		tpanel.getScrollPane(m_assignedtable).getViewport()
				.setBackground(javax.swing.UIManager.getColor("Table.background"));

		TableColumn tcol = m_assignedtable.getColumnModel().getColumn(0);
		tcol.setCellRenderer(MetaDataTableRenderer.createRenderer(getConnection()));
		tcol = m_assignedtable.getColumnModel().getColumn(1);
		tcol.setCellRenderer(MetaDataTableRenderer.createRenderer(getConnection()));

		if (showHeading) {
			TSHeaderPanel headerpanel = new TSHeaderPanel(tpanel);
			headerpanel.setHeadingText(I18N.getLocalizedMessage("Assigned Columns"));
			return headerpanel;
		} else {
			return tpanel;
		}
	}

	/**
	 * @return the assignments for the foreign key
	 */
	public ColumnMetaData[][] getAssignments() {
		ColumnMetaData[][] results = new ColumnMetaData[m_assignedmodel.getRowCount()][2];

		for (int row = 0; row < m_assignedmodel.getRowCount(); row++) {
			ColumnMetaData localcmd = (ColumnMetaData) m_assignedmodel.getValueAt(row, 0);
			ColumnMetaData refcmd = (ColumnMetaData) m_assignedmodel.getValueAt(row, 1);
			results[row][0] = localcmd;
			results[row][1] = refcmd;
		}
		return results;
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(10, 5);
	}

	/**
	 * @return the column meta data object of the primary key that is selected
	 *         in the reference columns list. Null is returned if no object is
	 *         selected.
	 */
	public ColumnMetaData getSelectedPrimaryKeyColumn() {
		int row = m_assignedtable.getSelectedRow();
		if (row >= 0)
			return (ColumnMetaData) m_assignedtable.getValueAt(row, 1);
		else
			return null;
	}

	/**
	 * @return the table component
	 */
	JTable getTable() {
		return m_assignedtable;
	}

	/**
	 * Sets the foreign key to display in this view
	 * 
	 * @param localColumns
	 *            a collection of ColumnMetaData objects that make up the
	 *            columns in the local table Note we don't pass the
	 *            TableMetaData for the local table here because the user can
	 *            change the table name in the editor and this could cause
	 *            problems looking up the tableMetadata is that table id has
	 *            changed.
	 */
	public void setForeignKey(DbForeignKey fKey, Collection localColumns, TableMetaData reftmd) {
		m_assignedmodel.setRowCount(0);

		if (reftmd != null) {
			TableColumn tcol = m_assignedtable.getColumnModel().getColumn(1);
			tcol.setHeaderValue(reftmd.getTableName());
		}

		if (localColumns != null && reftmd != null && fKey != null) {
			HashMap colmap = DbUtils.createColumnMap(localColumns);
			DbKey localkey = fKey.getLocalKey();
			Collection cols = localkey.getColumns();
			Iterator iter = cols.iterator();
			while (iter.hasNext()) {
				String localcol = (String) iter.next();
				String refcol = (String) fKey.getAssignedPrimaryKeyColumnName(localcol);

				ColumnMetaData localcmd = (ColumnMetaData) colmap.get(localcol);
				ColumnMetaData refcmd = reftmd.getColumn(refcol);

				Object[] row = new Object[2];
				row[0] = localcmd;
				row[1] = refcmd;

				m_assignedmodel.addRow(row);
			}
		}

	}

	/**
	 * Sets the foreign key to display in this view
	 */
	public void setForeignKey(ColumnMetaData[][] assignments, Collection localColumns, TableMetaData reftmd) {
		m_assignedmodel.setRowCount(0);

		if (reftmd != null) {
			TableColumn tcol = m_assignedtable.getColumnModel().getColumn(1);
			tcol.setHeaderValue(reftmd.getTableName());
		}

		if (reftmd != null) {
			for (int index = 0; index < assignments.length; index++) {
				ColumnMetaData localcmd = assignments[index][0];
				ColumnMetaData refcmd = assignments[index][1];

				// assert( localcmd != null );
				assert (refcmd != null);
				m_assignedmodel.addRow(assignments[index]);
			}
		}
	}

	/**
	 * Sets the reference table. This clears any entries in the JTable and
	 * reloads the columns from the reference table primary key
	 */
	void setReferenceTable(TableMetaData reftmd) {
		m_assignedmodel.setRowCount(0);

		if (reftmd != null) {
			TableColumn tcol = m_assignedtable.getColumnModel().getColumn(0);
			tcol.setHeaderValue(m_localtableid.getTableName());
			tcol = m_assignedtable.getColumnModel().getColumn(1);
			tcol.setHeaderValue(reftmd.getTableName());

			DbKey pk = reftmd.getPrimaryKey();
			if (pk != null) {
				Collection cols = pk.getColumns();
				Iterator iter = cols.iterator();
				while (iter.hasNext()) {
					String colname = (String) iter.next();
					ColumnMetaData cmd = reftmd.getColumn(colname);
					if (cmd != null) {
						Object[] row = new Object[2];
						row[0] = null;
						row[1] = cmd;
						m_assignedmodel.addRow(row);
					}
				}
			}
		} else {
			TableColumn tcol = m_assignedtable.getColumnModel().getColumn(1);
			tcol.setHeaderValue("");
		}

		if (m_assignedtable.getRowCount() > 0) {
			m_assignedtable.setRowSelectionInterval(0, 0);
			m_assignedtable.setColumnSelectionInterval(0, 1);
		}
	}

	/**
	 * Copies the assignment values from the GUI to the given foreign key
	 */
	public void toForeignKey(DbForeignKey fkey) {
		fkey.clearAssignments();
		for (int row = 0; row < m_assignedmodel.getRowCount(); row++) {
			ColumnMetaData localcmd = (ColumnMetaData) m_assignedmodel.getValueAt(row, 0);
			ColumnMetaData refcmd = (ColumnMetaData) m_assignedmodel.getValueAt(row, 1);
			fkey.assignForeignKeyColumn(localcmd.getColumnName(), refcmd.getColumnName());
		}
	}

	/**
	 * Validates the data in the view. Null is returned if the input is valid.
	 */
	public String validateInputs() {
		// @toto make sure all columns are assigned

		for (int row = 0; row < m_assignedmodel.getRowCount(); row++) {
			ColumnMetaData cmd = (ColumnMetaData) m_assignedmodel.getValueAt(row, 0);
			if (cmd == null) {
				return I18N.getLocalizedMessage("Null columns not allowed");
			}
		}
		return null;
	}
}
