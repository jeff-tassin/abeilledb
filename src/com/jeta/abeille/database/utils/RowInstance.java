package com.jeta.abeille.database.utils;

/**
 * This class caches column values for a given row in a result set
 * 
 * @author Jeff Tassin
 */
public class RowInstance {

	private Object[] m_values;

	/**
	 * ctor
	 * 
	 * @param columns
	 *            the number of columns in this row
	 */
	public RowInstance(int columns) {
		m_values = new Object[columns];
	}

	/**
	 * @return the object at the given column index. Note: column indices are
	 *         ZERO based in this object.
	 */
	public Object getObject(int index) {
		return m_values[index];
	}

	/**
	 * Set the object at the given column index. Note: column indices are ZERO
	 * based in this object
	 */
	public void setObject(int index, Object value) {
		m_values[index] = value;
	}

}
