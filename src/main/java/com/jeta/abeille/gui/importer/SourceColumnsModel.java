package com.jeta.abeille.gui.importer;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.foundation.i18n.I18N;

/**
 * 
 * @author Jeff Tassin
 */
class SourceColumnsModel extends AbstractTableModel {
	/**
	 * Array of source column objects
	 */
	private ArrayList m_data = new ArrayList();

	/** an array of column names of the GUI table (not the database table) */
	private String[] m_colnames;

	/** this is the types of columns of the GUI table (not the database table) */
	private Class[] m_coltypes;

	// columns
	public static final int COLUMN_NAME_COLUMN = 0;

	/**
	 * ctor.
	 */
	public SourceColumnsModel() {

		String[] cols = new String[1];
		cols[COLUMN_NAME_COLUMN] = I18N.getLocalizedMessage("Column");

		m_colnames = cols;
		Class[] types = { ColumnMetaData.class };
		m_coltypes = types;

	}

	public SourceColumnsModel(QueryResultSet rset) {
		this();
		try {
			ColumnMetaData[] cols = rset.getColumnMetaData();
			for (int index = 0; index < cols.length; index++) {
				m_data.add(new SourceColumn(cols[index], index));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the number of columns in this model
	 */
	public int getColumnCount() {
		return m_colnames.length;
	}

	public SourceColumn getRow(int index) {
		return (SourceColumn) m_data.get(index);
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
	 * @return the cell value at the given row and column
	 */
	public Object getValueAt(int row, int column) {
		return m_data.get(row);
	}

}
