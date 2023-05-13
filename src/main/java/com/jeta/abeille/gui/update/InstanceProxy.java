package com.jeta.abeille.gui.update;

import java.sql.SQLException;

import com.jeta.abeille.database.model.ColumnMetaData;

/**
 * This interface defines a service for getting values from an instance and set
 * of instances in the instance view/controller.
 * 
 * @author Jeff Tassin
 */
public interface InstanceProxy {
	/**
	 * Sets the current row pointer to the first instance in the set
	 */
	public boolean first() throws SQLException;

	/**
	 * @return the current row
	 */
	public int getRow() throws SQLException;

	/**
	 * @return the total row count if known. If the row count is not known, -1
	 *         is returned
	 */
	public int getRowCount();

	/**
	 * @return the maximum row downloaded so far. The will change for large
	 *         result sets. This means that as more results are downloaded, the
	 *         total downloaded so far (i.e. the max row) will be updated. We do
	 *         this because there is currently no *portable* way to know the
	 *         size of a large resultset.
	 */
	public int getMaxRow();

	/**
	 * Gets binary data for the given column. Note this is only for handling
	 * binary objects provided that the given column is a binary object type.
	 */
	public byte[] getBinaryData(String columnName) throws SQLException;

	/**
	 * Gets clob data for the given column. Note this is only for handling
	 * binary objects provided that the given column is a clob type.
	 */
	public String getClobData(String columnName) throws SQLException;

	/**
	 * @return true if there are no instances in the proxy
	 */
	public boolean isEmpty() throws SQLException;

	/**
	 * @return true if we are on the first row in the instance set
	 */
	public boolean isFirst() throws SQLException;

	/**
	 * @return true if we are on the last row in the instance set
	 */
	public boolean isLast() throws SQLException;

	/**
	 * @return true if the result set is scrollable or not
	 */
	public boolean isScrollable() throws SQLException;

	/**
	 * Moves the row pointer to the last instance in the set
	 */
	public boolean last() throws SQLException;

	/**
	 * Moves the row pointer to the next instance in the set
	 */
	public boolean next() throws SQLException;

	/**
	 * Moves the row pointer to the previous instance in the set
	 */
	public boolean previous() throws SQLException;

	/**
	 * Sets the value of the given instance component with the value associated
	 * with the given column meta data
	 */
	public void setValue(InstanceComponent comp, ColumnMetaData cmd) throws SQLException;

}
