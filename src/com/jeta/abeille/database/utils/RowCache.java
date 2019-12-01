package com.jeta.abeille.database.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.jeta.abeille.database.model.ColumnMetaData;

/**
 * This class caches data from a result set.
 * 
 * @author Jeff Tassin
 */
public abstract class RowCache {
	/**
	 * @return true if the cache currently contains the given row. Row is zero
	 *         based
	 */
	public abstract boolean contains(int row);

	/**
	 * Moves the result set cursor to the first row in the set
	 */
	public abstract boolean first() throws SQLException;

	/**
	 * @return the data for the given column as a byte array for the current
	 *         record. Note that the column must be a binary stream type.
	 */
	public abstract byte[] getBinaryData(String columnName) throws SQLException;

	/**
	 * @return the clob data for the given column for the current record. Note
	 *         that the column must be a clob object
	 */
	public abstract String getClobData(String columnName) throws SQLException;

	/**
	 * @return the column metadata for the result
	 */
	public abstract ColumnMetaData[] getColumnMetaData() throws SQLException;

	/**
	 * @return the maximum row downloaded so far (zero based)
	 */
	public abstract int getMaxRow();

	/**
	 * @return the underlying result set. Caution! Do not manipulate this result
	 *         set directly (i.e. first, last, etc). Multiple views might be
	 *         using the same result set.
	 */
	public abstract ResultSet getResultSet();

	/**
	 * @return the underlying result set. Caution! Do not manipulate this result
	 *         set directly (i.e. first, last, etc). Multiple views might be
	 *         using the same result set.
	 */
	public abstract ResultSetReference getResultSetReference();

	/**
	 * @return the current row of the result set (this is 0 based)
	 */
	public abstract int getRow() throws SQLException;

	/**
	 * @return the number of rows in the result set
	 */
	public abstract int getRowCount();

	/**
	 * @param row
	 *            the row to retrieve ( zero based )
	 * @return the row instance at the given row
	 */
	public abstract RowInstance getRowInstance(int row) throws SQLException;

	/**
	 * @return true if the current result set cursor is after the last row
	 */
	public abstract boolean isAfterLast() throws SQLException;

	/**
	 * @return true if the current result set cursor is before the first row
	 */
	public abstract boolean isBeforeFirst() throws SQLException;

	/**
	 * @return true if the current result set cursor is at the first row
	 */
	public abstract boolean isFirst() throws SQLException;

	/**
	 * @return true if the current result set cursor is at the last row
	 */
	public abstract boolean isLast() throws SQLException;

	/**
	 * @return true if the result set has no rows
	 */
	public abstract boolean isEmpty();

	/**
	 * @return true if we know the exact row count in the result set
	 */
	public abstract boolean isRowCountKnown();

	/**
	 * Moves the result set cursor to the last row
	 */
	public abstract boolean last() throws SQLException;

	/**
	 * Moves the result set cursor to the next row
	 */
	public abstract boolean next() throws SQLException;

	/**
	 * Moves the result set cursor to the next row
	 */
	public abstract boolean previous() throws SQLException;

	/**
	 * Sets the current row in the result set
	 * 
	 * @param row
	 *            the row to set (zero based)
	 */
	public abstract boolean setRow(int row) throws SQLException;

	/**
	 * Tells the row cache to set the isRowCountKnown flag to true (even if the
	 * result set is not fully downloaded). This also sets the max row and last
	 * row to the current max row. This is primarly used when the user makes
	 * changes to the result set and the set has not been fully downloaded. In
	 * this case, we simply truncate to prevent problems.
	 */
	public abstract void truncateResults();

	/**
	 * This class defines a block of rows that we downloaded from the result
	 * set. We use this block to cache data from a query. The block size is
	 * arbitrary (but is typically on the order of 100 rows)
	 */
	static class Block {
		/** the offset from the beginning of the result set */
		private int m_offset;
		private int m_blocksize;

		/** the array of row instances contained by this block */
		private RowInstance[] m_rows;

		/**
		 * ctor
		 */
		public Block(int offset, int blocksize) {
			m_offset = offset;
			m_blocksize = blocksize;
			m_rows = new RowInstance[blocksize];
		}

		/**
		 * @return true if this block contains the given row
		 */
		public boolean contains(int row) {
			int index = row - m_offset;
			return (index >= 0 && index < m_blocksize);
		}

		/**
		 * Contains the starting row of this block
		 */
		public int getOffset() {
			return m_offset;
		}

		/**
		 * @return the rowinstance at the given row. Null is returned if the row
		 *         is invalid
		 */
		public RowInstance getRow(int row) {
			int index = row - m_offset;
			if (index >= 0 && index < m_blocksize)
				return m_rows[index];
			else {
				// System.out.println("**** ERROR:    row = " + row );
				assert (false);
				return null;
			}
		}

		/**
		 * Sets the instance at the given row
		 */
		public void setRow(int row, RowInstance instance) {
			int index = row - m_offset;
			m_rows[index] = instance;
		}
	}

}
