package com.jeta.abeille.gui.indexes;

/**
 * This class identifies the column name and any other information for a column
 * in a given index. For example, in MySQL you can specify a length attribute
 * for a given column. In PostgreSQL, you can specify an operator for a column.
 */
public class IndexColumn {
	/** the name of the column */
	private String m_name;

	/** any attributes that are associated with the column in the given index */
	private Object m_attribute;

	/**
	 * ctor
	 */
	public IndexColumn() {

	}

	/**
	 * ctor
	 */
	public IndexColumn(String name) {
		m_name = name;
	}

	/**
	 * ctor
	 */
	public IndexColumn(String name, Object prop) {
		m_name = name;
		m_attribute = prop;
	}

	public String getName() {
		return m_name;
	}

	public Object getAttribute() {
		return m_attribute;
	}

	public void setAttribute(Object attr) {
		m_attribute = attr;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String toString() {
		return m_name;
	}

}
