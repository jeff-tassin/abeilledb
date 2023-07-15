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


	public void truncate(int newLength) {
		if ( newLength > m_values.length ) {
			// no - op
			// throw new IllegalArgumentException(String.format("RowInstance.truncate newLength(%s) > m_values.length(%s)", newLength, m_values.length));
		} else if ( newLength < m_values.length ) {
			Object[] truncated = new Object[newLength];
			System.arraycopy(m_values, 0, truncated, 0, newLength);
			m_values = truncated;
		}
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

	public Integer getLength() {
		return m_values.length;
	}

}
