package com.jeta.abeille.gui.update;

import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.utils.RowInstance;

/**
 * This class allows us to display a single (partial)instance in the
 * InstanceView. The caller must supply the values for the instance since this
 * proxy is not based upon a result set
 * 
 * @author Jeff Tassin
 */
public class SingletonInstanceProxy implements InstanceProxy {
	/**
	 * This is a hash of ColumnMetaData objects (keys) to index in the query
	 * result set. This allows us to quickly lookup the index of the column in
	 * the query results to get the value
	 */
	private HashMap m_columns = new HashMap();

	/** the one and only row instance */
	private RowInstance m_instance;

	/**
	 * ctor
	 */
	public SingletonInstanceProxy(ColumnMetaData[] columns, Object[] values) {
		assert (columns.length == values.length);

		m_instance = new RowInstance(values.length);

		// store column indices
		for (int index = 0; index < columns.length; index++) {
			ColumnMetaData cmd = columns[index];
			m_columns.put(cmd, new Integer(index));
			m_instance.setObject(index, values[index]);
		}
	}

	/**
	 * @return the index of the column found in the query result set
	 */
	public int getInstanceIndex(ColumnMetaData cmd) {
		Integer i = (Integer) m_columns.get(cmd);
		if (i == null)
			return -1;
		else
			return i.intValue();
	}

	/**
	 * Sets the current row pointer to the first instance in the set
	 */
	public boolean first() throws SQLException {
		return true;
	}

	/**
	 * @return the current row
	 */
	public int getRow() throws SQLException {
		return 0;
	}

	/**
	 * @return the total row count if known. If the row count is not known, -1
	 *         is returned
	 */
	public int getRowCount() {
		return 1;
	}

	/**
	 * @return the maximum row downloaded so far. The will change for large
	 *         result sets. This means that as more results are downloaded, the
	 *         total downloaded so far (i.e. the max row) will be updated. We do
	 *         this because there is currently no *portable* way to know the
	 *         size of a large resultset.
	 */
	public int getMaxRow() {
		return 1;
	}

	/**
	 * Gets a binary stream for the given column. Note this is only for handling
	 * binary objects provided that the given column is a binary object type.
	 */
	public byte[] getBinaryData(String columnName) throws SQLException {
		assert (false);
		return null;
	}

	/**
	 * Gets clob data for the given column. Note this is only for handling
	 * binary objects provided that the given column is a clob object type.
	 */
	public String getClobData(String columnName) throws SQLException {
		assert (false);
		return null;
	}

	/**
	 * @return true if there are no instances in the proxy
	 */
	public boolean isEmpty() throws SQLException {
		return false;
	}

	/**
	 * @return true if we are on the first row in the instance set
	 */
	public boolean isFirst() throws SQLException {
		return true;
	}

	/**
	 * @return true if we are on the last row in the instance set
	 */
	public boolean isLast() throws SQLException {
		return true;
	}

	/**
	 * Moves the row pointer to the last instance in the set
	 */
	public boolean last() throws SQLException {
		return true;
	}

	/**
	 * Moves the row pointer to the next instance in the set
	 */
	public boolean next() throws SQLException {
		return false;
	}

	/**
	 * Moves the row pointer to the previous instance in the set
	 */
	public boolean previous() throws SQLException {
		return false;
	}

	/**
	 * A single result can't be scrolled
	 */
	public boolean isScrollable() throws SQLException {
		return false;
	}

	/**
	 * Sets the value of the given instance component with the value associated
	 * with the given column meta data
	 */
	public void setValue(InstanceComponent comp, ColumnMetaData cmd) throws SQLException {
		assert (m_instance != null);
		int iindex = getInstanceIndex(cmd);
		// System.out.println( "singletondInstanceproxy.setValue: " +
		// cmd.getQualifiedName() + "   qset.row = " + qset.getRow() +
		// "  iindex = " + iindex );
		if (iindex >= 0) {
			comp.setValue(m_instance.getObject(iindex));
		}
	}
}
