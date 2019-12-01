package com.jeta.abeille.gui.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableMetaData;

import com.jeta.foundation.i18n.I18N;

/**
 * This class represents the columns in a database table for the target of an
 * import.
 * 
 * @author Jeff Tassin
 */
public class TargetColumnsModel extends AbstractTableModel {
	/**
	 * Array of source column objects
	 */
	private ArrayList m_data = new ArrayList();

	/** an array of column names of the GUI table (not the database table) */
	private String[] m_colnames;

	/** this is the types of columns of the GUI table (not the database table) */
	private Class[] m_coltypes;

	/** table columns */
	public static final int TABLE_NAME_COLUMN = 0;
	public static final int COLUMN_NAME_COLUMN = 1;
	public static final int VALUE_COLUMN = 2;

	/**
	 * ctor.
	 */
	public TargetColumnsModel() {
		String[] cols = new String[3];
		cols[TABLE_NAME_COLUMN] = I18N.getLocalizedMessage("Table Name");
		cols[COLUMN_NAME_COLUMN] = I18N.getLocalizedMessage("Column Name");
		cols[VALUE_COLUMN] = I18N.getLocalizedMessage("Value");

		m_colnames = cols;

		Class[] types = { TableMetaData.class, ColumnMetaData.class, ColumnMetaData.class };
		m_coltypes = types;
	}

	/**
	 * ctor.
	 * 
	 * @param targets
	 *            a collection of TargetColumnInfo objects
	 */
	public TargetColumnsModel(Collection targets) {
		this();
		if (targets != null) {
			Iterator iter = targets.iterator();
			while (iter.hasNext()) {
				addColumn((TargetColumnInfo) iter.next());
			}
		}
	}

	public void addColumn(TargetColumnInfo info) {
		if (!m_data.contains(info)) {
			m_data.add(info);
			fireTableRowsInserted(m_data.size() - 1, m_data.size() - 1);
		}
	}

	/**
	 * @return the set of target column info objects in this model
	 */
	public Collection getTargetColumns() {
		return m_data;
	}

	/**
	 * @return the number of columns in this model
	 */
	public int getColumnCount() {
		return m_colnames.length;
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
	 * @return the object at the given row in the model
	 */
	public TargetColumnInfo getRow(int row) {
		return (TargetColumnInfo) m_data.get(row);
	}

	/**
	 * @return the cell value at the given row and column
	 */
	public Object getValueAt(int row, int column) {
		// tablename, columnname
		TargetColumnInfo target = getRow(row);
		if (column == TABLE_NAME_COLUMN) // table name
			return target.getTableId();
		else if (column == COLUMN_NAME_COLUMN) // columnname
			return target.getTarget();
		else if (column == VALUE_COLUMN) // output name
			return target.getSourceColumn();
		else
			return "";
	}

	public void removeSource(int[] rows) {
		for (int index = 0; index < rows.length; index++) {
			TargetColumnInfo info = getRow(rows[index]);
			info.setSourceColumn(null);
		}
		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * Removes the given objects found at the given rows
	 */
	public void remove(int[] rows) {
		if (rows.length == 0)
			return;

		TargetColumnInfo[] tc = new TargetColumnInfo[rows.length];
		for (int index = 0; index < rows.length; index++) {
			tc[index] = getRow(rows[index]);
		}

		for (int index = 0; index < tc.length; index++) {
			int row = m_data.indexOf(tc[index]);
			m_data.remove(row);
		}
		fireTableChanged(new TableModelEvent(this));
	}

	public void setTargetValue(int row, SourceColumn sc) {
		TargetColumnInfo info = getRow(row);
		info.setSourceColumn(sc);
		fireTableRowsUpdated(row, row);
	}

}
