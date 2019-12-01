package com.jeta.abeille.gui.query;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.query.QueryConstraint;
import com.jeta.abeille.query.LogicalConnective;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class manages the constraints for a given ad hoc query
 * 
 * @author Jeff Tassin
 */
class ConstraintModel extends AbstractTableModel {
	private ArrayList m_data; // an array of Constraint objects
	private String[] m_colnames; // an array of column names of the GUI table
									// (not the database table)
	private Class[] m_coltypes; // this is the types of columns of the GUI table
								// (not the database table)

	/** The database connection */
	private TSConnection m_connection;

	private QueryModel m_querymodel;

	/**
	 * Time stamp for keeping track of modifications
	 */
	private long m_time_stamp = 0;

	/** column definitions */
	static final int LOGIC_COLUMN = 0;
	static final int CONSTRAINT_COLUMN = 1;

	/**
	 * ctor.
	 */
	public ConstraintModel(QueryModel query) {
		super();

		m_data = new ArrayList();
		m_querymodel = query;
		m_connection = query.getConnection();

		String[] values = { "", I18N.getLocalizedMessage("Constraint") };
		m_colnames = values;
		Class[] types = { String.class, String.class };
		m_coltypes = types;

		Collection c = m_querymodel.getConstraints();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			QueryConstraint qc = (QueryConstraint) iter.next();
			addRow(qc);
		}
		m_time_stamp = 0;
	}

	/**
	 * Adds the given Option info object to the table
	 */
	public void addRow(QueryConstraint qc) {
		if (m_data == null)
			m_data = new ArrayList();
		m_data.add(qc);
		fireTableRowsInserted(m_data.size() - 1, m_data.size() - 1);
		touchTimeStamp();
	}

	public Catalog getCatalog() {
		return m_querymodel.getCatalog();
	}

	public Schema getSchema() {
		return m_querymodel.getSchema();
	}

	/**
	 * @return the number of columns in this model
	 */
	public int getColumnCount() {
		return m_colnames.length;
	}

	/**
	 * @return all the constraints in this model
	 */
	public Collection getConstraints() {
		return m_data;
	}

	/**
	 * DocumentOwner Implementation
	 */
	public long getLastModifiedTime() {
		return m_time_stamp;
	}

	/**
	 * @return the number of contraint objects in this model
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
	public QueryConstraint getRow(int row) {
		return (QueryConstraint) m_data.get(row);
	}

	public Object getValueAt(int row, int column) {
		// "Logical Connective", "Constraint"
		QueryConstraint constraint = getRow(row);
		if (column == LOGIC_COLUMN) // logical connective
		{
			if (row == 0) // no connective for first row
				return "";
			else
				return constraint.getLogicalConnective().toString();
		} else if (column == CONSTRAINT_COLUMN) // columnname
		{
			return constraint;
		} else
			return "";
	}

	/**
	 * Flag that indicates if all table and column names should be fully
	 * qualfied with catalog.schema in the generated SQL
	 */
	public boolean isQualified() {
		return m_querymodel.isQualified();
	}

	/**
	 * Removes the given constraint objects found at the given rows
	 */
	public void remove(int row) {
		int[] rows = new int[1];
		rows[0] = row;
		remove(rows);
	}

	/**
	 * Removes the given constraint objects found at the given rows
	 */
	public void remove(int[] rows) {
		if (rows.length == 0)
			return;

		// we have to do this because the rows we are deleting might not be
		// contiguous
		QueryConstraint[] constraints = new QueryConstraint[rows.length];
		for (int index = 0; index < rows.length; index++) {
			constraints[index] = getRow(rows[index]);
		}

		for (int index = 0; index < constraints.length; index++) {
			int row = m_data.indexOf(constraints[index]);
			m_data.remove(row);
		}
		fireTableChanged(new TableModelEvent(this));
		touchTimeStamp();
	}

	/**
	 * Remove all data items from this model
	 */
	public void removeAll() {
		m_data.clear();
		touchTimeStamp();
	}

	/**
	 * Moves the object found at the old index to the new index
	 */
	public void reorder(int newIndex, int oldIndex) {
		Object obj = m_data.remove(oldIndex);
		m_data.add(newIndex, obj);
		fireTableChanged(new TableModelEvent(this));
		touchTimeStamp();
	}

	/**
	 * Saves the constraints back to the query object
	 */
	public void saveState(QueryModel query) {
		// first clear all constraints
		query.removeConstraints();
		for (int index = 0; index < getRowCount(); index++)
			query.addConstraint(getRow(index));
	}

	/**
	 * Called when done in-place editing
	 */
	public void setValueAt(Object aValue, int row, int column) {
		if (column == CONSTRAINT_COLUMN) {
			String constrainttxt = (String) aValue;
			m_data.set(row, new QueryConstraint(constrainttxt, m_connection, getCatalog(), getSchema()));
			fireTableRowsUpdated(row, row);
			touchTimeStamp();
		}
	}

	/**
	 * Sets the catalog and schema for this proxy
	 */
	public void set(TSConnection tsconn, Catalog catalog, Schema schema) {
		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			QueryConstraint qc = (QueryConstraint) iter.next();
			qc.set(tsconn, catalog, schema);
		}
		fireTableChanged(new TableModelEvent(this));
		touchTimeStamp();
	}

	/**
	 * Changes a logical connective from AND to OR or OR to AND
	 */
	public void toggleLogicalConnective(int row) {
		QueryConstraint qc = getRow(row);
		if (qc != null) {
			LogicalConnective lc = qc.getLogicalConnective();
			if (lc == LogicalConnective.AND)
				qc.setLogicalConnective(LogicalConnective.OR);
			else
				qc.setLogicalConnective(LogicalConnective.AND);

			fireTableChanged(new TableModelEvent(this));
			touchTimeStamp();
		}
	}

	public void touchTimeStamp() {
		m_time_stamp = System.currentTimeMillis();
	}
}
