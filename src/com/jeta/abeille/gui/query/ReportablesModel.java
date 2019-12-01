package com.jeta.abeille.gui.query;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.query.Reportable;

/**
 * This class manages the reportables for a given ad hoc query
 * 
 * @author Jeff Tassin
 */
class ReportablesModel extends AbstractTableModel {
	/**
	 * Array of reportable objects
	 */
	private ArrayList m_data;

	/**
	 * Provide for efficient lookups of reportables
	 */
	private TreeSet m_lookup;

	private String[] m_colnames; // an array of column names of the GUI table
									// (not the database table)
	private Class[] m_coltypes; // this is the types of columns of the GUI table
								// (not the database table)

	private TSConnection m_connection;

	/**
	 * Time stamp for keeping track of modifications
	 */
	private long m_time_stamp = 0;

	// columns
	public static final int TABLE_NAME_COLUMN = 0;
	public static final int COLUMN_NAME_COLUMN = 1;
	public static final int OUTPUT_NAME_COLUMN = 2;

	/**
	 * ctor.
	 */
	public ReportablesModel(QueryModel query) {
		super();

		m_connection = query.getConnection();
		m_data = new ArrayList();
		m_lookup = new TreeSet();

		String[] cols = new String[3];

		cols[0] = I18N.getLocalizedMessage("Table");
		cols[1] = I18N.getLocalizedMessage("Column");
		cols[2] = I18N.getLocalizedMessage("Output Name");

		m_colnames = cols;
		Class[] types = { TableMetaData.class, ColumnMetaData.class, String.class };
		m_coltypes = types;

		Collection c = query.getReportables();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			addReportable((Reportable) iter.next());
		}
		m_time_stamp = 0;
	}

	/**
	 * Adds the given Reportable object to the table
	 */
	public void addReportable(Reportable report) {
		if (!m_lookup.contains(report)) {
			if (m_data == null)
				m_data = new ArrayList();

			m_data.add(report);
			m_lookup.add(report);
			fireTableRowsInserted(m_data.size() - 1, m_data.size() - 1);
			touchTimeStamp();
		}
	}

	/**
	 * @return the number of columns in this model
	 */
	public int getColumnCount() {
		return m_colnames.length;
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return all reportables in this model
	 */
	public Collection getReportables() {
		return m_data;
	}

	/**
	 * @return the number of objects in this model
	 */
	public int getRowCount() {
		return m_data.size();
	}

	/**
	 * @return the name of a column at a given index
	 */
	public String getColumnName(int column) {
		return m_colnames[column];
	}

	/**
	 * @return the type of column at a given index
	 */
	public Class getColumnClass(int column) {
		return m_coltypes[column];
	}

	/**
	 * DocumentOwner Implementation
	 */
	public long getLastModifiedTime() {
		return m_time_stamp;
	}

	/**
	 * @return the object at the given row in the model
	 */
	public Reportable getRow(int row) {
		return (Reportable) m_data.get(row);
	}

	/**
	 * @return true if the given cell is editable by the user
	 */
	public boolean isCellEditable(int row, int column) {
		return (column == OUTPUT_NAME_COLUMN);
	}

	/**
	 * @return the cell value at the given row and column
	 */
	public Object getValueAt(int row, int column) {
		// tablename, columnname
		Reportable report = getRow(row);
		if (column == TABLE_NAME_COLUMN) // table name
			return report.getTableId();
		else if (column == COLUMN_NAME_COLUMN) // columnname
			return report.getColumn();
		else if (column == OUTPUT_NAME_COLUMN) // output name
			return report.getOutputName();
		else
			return "";
	}

	/**
	 * Removes the given constraint objects found at the given rows The rows are
	 * in model coordinates
	 */
	public void remove(int[] rows) {
		if (rows.length == 0)
			return;

		// we do this because the rows might not be contiguous
		Reportable[] reportables = new Reportable[rows.length];
		for (int index = 0; index < rows.length; index++) {
			reportables[index] = getRow(rows[index]);
		}

		for (int index = 0; index < reportables.length; index++) {
			int row = m_data.indexOf(reportables[index]);
			Reportable reportable = (Reportable) m_data.remove(row);
			m_lookup.remove(reportable);
		}
		touchTimeStamp();

		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * Remove all data items from this model
	 */
	public void removeAll() {
		m_data.clear();
		m_lookup.clear();
		touchTimeStamp();

		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * Moves the object found at the old index to the new index
	 */
	public void reorder(int newIndex, int oldIndex) {
		Object obj = m_data.remove(oldIndex);
		m_data.add(newIndex, obj);
		touchTimeStamp();

		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * Saves the reportables back to the query object
	 */
	public void saveState(QueryModel query) {
		// first clear all constraints
		query.removeReportables();
		for (int index = 0; index < getRowCount(); index++)
			query.addReportable(getRow(index));
	}

	/**
	 * Sets the value at the given row
	 */
	public void setValueAt(Object aValue, int row, int column) {
		// if ( row >=0 && row < getRowCount() )
		// {
		// Reportable r = (Reportable)aValue;
		// m_data.set( row, r );
		// fireTableRowsUpdated( row, row );
		// }
		if (column == OUTPUT_NAME_COLUMN) {
			Reportable rep = getRow(row);
			if (rep != null) {
				rep.setOutputName((String) aValue);
				touchTimeStamp();
				fireTableRowsUpdated(row, row);
			}
		} else if (aValue instanceof Reportable) {
			m_data.set(row, aValue);
			touchTimeStamp();
			fireTableRowsUpdated(row, row);
		}

	}

	public void touchTimeStamp() {
		m_time_stamp = System.currentTimeMillis();
	}

}
