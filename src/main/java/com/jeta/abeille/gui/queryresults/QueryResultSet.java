package com.jeta.abeille.gui.queryresults;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.database.utils.RowCache;
import com.jeta.abeille.database.utils.RowCacheFactory;
import com.jeta.abeille.database.utils.RowInstance;
import com.jeta.foundation.utils.TSUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * This class is a wrapper around the rowcache and the ResultSet
 * 
 * @author Jeff Tassin
 */
public class QueryResultSet {
	/** the row cache object */
	private RowCache m_rowcache;

	public QueryResultSet(Catalog catalog, ResultSetReference ref) throws SQLException {
		m_rowcache = RowCacheFactory.createInstance(catalog, ref);
	}

	public QueryResultSet(RowCache rowcache) {
		m_rowcache = rowcache;
	}

	/**
	 * Moves the result set pointer to the first row
	 */
	public boolean first() throws SQLException {
		return m_rowcache.first();
	}

	/**
	 * @return the column metadata for the query result
	 */
	public synchronized ColumnMetaData[] getColumnMetaData() throws SQLException {
		return m_rowcache.getColumnMetaData();
	}

	/**
	 * @return the data for the given column as a byte array. Note that the
	 *         column must be a binary stream type.
	 */
	public byte[] getBinaryData(String columnName) throws SQLException {
		return m_rowcache.getBinaryData(columnName);
	}

	/**
	 * @return the clob data for the given column. Note that the column must be
	 *         a clob object
	 */
	public String getClobData(String columnName) throws SQLException {
		return m_rowcache.getClobData(columnName);
	}

	/**
	 * @return true if the given result set is empty
	 */
	public boolean isEmpty() {
		return m_rowcache.isEmpty();
	}

	/**
	 * @return the maximum row downloaded so far (zero based)
	 */
	public int getMaxRow() {
		return m_rowcache.getMaxRow();
	}

	/**
	 * @return the underlying result set.
	 */
	public ResultSet getResultSet() {
		return m_rowcache.getResultSet();
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return m_rowcache.getResultSetReference().getMetaData();
	}

	/**
	 * @return the underlying result set reference object. Warning, you must
	 *         call addRef on this reference if you intend to use it for any
	 *         length of time
	 */
	public ResultSetReference getResultSetReference() {
		return m_rowcache.getResultSetReference();
	}

	/**
	 * @return the current row of the result set (this is 0 based)
	 */
	public int getRow() throws SQLException {
		return m_rowcache.getRow();
	}

	/**
	 * @return the number of rows in the result set. This value might be -1 if
	 *         the row count is unknown. For large result sets the total might
	 *         not be known until you scroll to the last row.
	 */
	public int getRowCount() {
		return m_rowcache.getRowCount();
	}

	/**
	 * @param row
	 *            the zero based row
	 * @return the row instance at the given row
	 */
	public RowInstance getRowInstance(int row) throws SQLException {
		return m_rowcache.getRowInstance(row);
	}

	/**
	 * @return the underlying SQL
	 */
	public String getSQL() {
		ResultSetReference rsetref = m_rowcache.getResultSetReference();
		if (rsetref == null)
			return null;
		else
			return rsetref.getSQL();
	}

	/**
	 * @return true if the current result set cursor is after the last row
	 */
	public boolean isAfterLast() throws SQLException {
		return m_rowcache.isAfterLast();
	}

	/**
	 * @return true if the current result set cursor is before the first row
	 */
	public boolean isBeforeFirst() throws SQLException {
		return m_rowcache.isBeforeFirst();
	}

	/**
	 * @return true if the current result set cursor is at the first row
	 */
	public boolean isFirst() throws SQLException {
		return m_rowcache.isFirst();
	}

	/**
	 * @return true if the current result set cursor is at the last row
	 */
	public boolean isLast() throws SQLException {
		return m_rowcache.isLast();
	}

	/**
	 * @return true if the total row count is known. For large result sets the
	 *         total might not be known until you scroll to the last row.
	 */
	public boolean isRowCountKnown() {
		return m_rowcache.isRowCountKnown();
	}

	/**
	 * @return true if the resultset is scrollable
	 */
	public boolean isScrollable() {
		try {
			ResultSet rset = getResultSet();
			return (rset.getType() != ResultSet.TYPE_FORWARD_ONLY);
		} catch (SQLException se) {
			TSUtils.printException(se);
			/** assume worst case */
			return true;
		}
	}

	/**
	 * Moves the result set cursor to the last row in the result set
	 */
	public boolean last() throws SQLException {
		return m_rowcache.last();
	}

	/**
	 * Moves the result set cursor to the next row
	 */
	public boolean next() throws SQLException {
		return m_rowcache.next();
	}

	/**
	 * Moves the result set cursor to the previous row
	 */
	public boolean previous() throws SQLException {
		return m_rowcache.previous();
	}

	/**
	 * Sets the current row in the result set
	 * 
	 * @param row
	 *            the row to set (zero based)
	 */
	public boolean setRow(int row) throws SQLException {
		return m_rowcache.setRow(row);
	}

	/**
	 * Tells the query results to set the isRowCountKnown flag to true (even if
	 * the result set is not fully downloaded). This also sets the max row and
	 * last row to the current max row. This is primarly used when the user
	 * makes changes to the result set and the set has not been fully
	 * downloaded. In this case, we simply truncate to prevent problems.
	 */
	public void truncateResults() {
		m_rowcache.truncateResults();
	}

}
