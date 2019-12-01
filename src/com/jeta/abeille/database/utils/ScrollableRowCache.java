package com.jeta.abeille.database.utils;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class caches data from a result set. Note that this class is not thread
 * safe. So, if you have multiple views accessing the same underlying result
 * set, you need to make sure they move the result set cursor in the same
 * thread.
 * 
 * @author Jeff Tassin
 */
public class ScrollableRowCache extends RowCache {
	// the size of a block
	// the DEBUG_BLOCK_SIZE is set to a small value for testing
	private static final int DEBUG_BLOCK_SIZE = 25;
	private static final int DEFAULT_BLOCK_SIZE = 100;
	private int m_blocksize = DEFAULT_BLOCK_SIZE;

	/**
	 * this is the number of rows returned in the result set. We don't know this
	 * value until we download the entire result set
	 */
	private int m_rowcount = -1;
	private int m_counter;

	/**
	 * this is the current row (zero based). we don't use the result set row
	 * because multiple views might be using the same result set
	 */
	private int m_currentrow;

	/** this is the max row we've downloaded so far (zero based) */
	private int m_maxrow = 0;

	// 3 blocks of instances (each block size )
	private Block[] m_cache = new Block[3];

	/** the underlying result set */
	private ResultSet m_resultset;

	/** a reference to a result set and the underlying connection */
	private ResultSetReference m_resultref;

	/**
	 * the column metadata objects associated with the metadata of the result
	 * set
	 */
	private ColumnMetaData[] m_columns = null;

	/** the current catalog */
	private Catalog m_catalog;

	/**
	 * This is an array of Booleans used to force a column index to a string. We
	 * tag this in case resultset.getObject(index) throws and exception. We
	 * assume that resultset.getString(index) always succeeds. PostgreSQL will
	 * throw an exception for getObject in some cases.
	 */
	private ArrayList m_cast_to_string = new ArrayList();

	/**
	 * ctor This constructor allows the user to create an artifical result set
	 * from a single row instance
	 */
	ScrollableRowCache(Catalog catalog, ColumnMetaData[] columns, RowInstance instance) {
		/** let's set the block size to a randome number for testing */
		if (TSUtils.isTest()) {
			m_blocksize = 1 + (int) (Math.random() * 100);
			TSUtils.printMessage("ScrollableRowCache setting block size to " + m_blocksize);
		}

		m_catalog = catalog;

		m_columns = columns;
		m_currentrow = 0;
		m_rowcount = 1;
		m_maxrow = 0;

		Block block = new Block(0, 1);
		block.setRow(0, instance);
		m_cache[0] = block;
	}

	/**
	 * ctor
	 */
	ScrollableRowCache(Catalog catalog, ResultSetReference rsetref) throws SQLException {
		m_catalog = catalog;
		if (TSUtils.isDebug()) {
			m_blocksize = DEBUG_BLOCK_SIZE;
		}

		if (rsetref != null) {
			TSUtils.printMessage(">>>>>>>>>>>>>>>>  Created Scrollable for " + rsetref.getSQL());
			m_resultset = rsetref.getResultSet();
			m_resultref = rsetref;

			m_currentrow = 0;

			if (isBeforeFirst() && isAfterLast()) {
				m_rowcount = 0;
			} else {
				// load the first block
				getRowInstance(0);
			}
		} else {
			// create empty row cache
			m_columns = new ColumnMetaData[0];
			m_currentrow = 0;
			m_rowcount = 0;
			m_maxrow = 0;
			Block block = new Block(0, 1);
			m_cache[0] = block;
		}
	}

	/**
	 * @param colIndex
	 *            the 1-based index to check
	 * @return true if the values for the given column in the result set for
	 *         this cache should be always be retrieved by getString instead of
	 *         getObject. We default to getObject, but if an error occurs, we
	 *         flag the column as requiring getString.
	 */
	private boolean castToString(int colIndex) {
		int index = colIndex - 1;
		TSUtils.ensureSize(m_cast_to_string, colIndex);
		Boolean result = (Boolean) m_cast_to_string.get(index);
		return Boolean.TRUE.equals(result);
	}

	/**
	 * @return true if the cache currently contains the given row. Row is zero
	 *         based
	 */
	public synchronized boolean contains(int row) {
		boolean bresult = false;
		for (int index = 0; index < m_cache.length; index++) {
			Block b = m_cache[index];
			if (b != null && b.contains(row)) {
				bresult = true;
				break;
			}
		}
		return bresult;
	}

	/**
	 * Moves the result set cursor to the first row in the set
	 */
	public boolean first() throws SQLException {
		ResultSet resultset = getResultSet();

		if (isEmpty())
			return false;
		else {
			m_currentrow = 0;
			return resultset.first();
		}
	}

	/**
	 * @return the data for the given column as a byte array. Note that the
	 *         column must be a binary stream type.
	 */
	public byte[] getBinaryData(String columnName) throws SQLException {
		ResultSet rset = getResultSet();
		rset.absolute(m_currentrow + 1);
		return DbUtils.getBinaryData(rset, columnName);
	}

	/**
	 * @return the clob data for the given column. Note that the column must be
	 *         a clob object
	 */
	public String getClobData(String columnName) throws SQLException {
		ResultSet rset = getResultSet();
		rset.absolute(m_currentrow + 1);
		Object obj = rset.getObject(columnName);
		if (obj instanceof Clob) {
			return DbUtils.getCharacterData((Clob) obj);
		} else {
			return DbUtils.getCharacterData(rset, columnName);
		}
	}

	/**
	 * Iterates over the block cache and searches for the block that contains
	 * the given row. If the block is found, it is returned. Furthermore, the
	 * block is moved to the top of the cache.
	 * 
	 * @param row
	 *            the zero based row
	 */
	private Block getBlock(int row) {
		Block result = null;
		for (int index = 0; index < m_cache.length; index++) {
			Block b = m_cache[index];
			if (b != null && b.contains(row)) {
				if (index != 0) {
					// now reorder blocks if the result is not at the zero
					// position in the cache
					// shift blocks 0 to index-1 down 1
					System.arraycopy(m_cache, 0, m_cache, 1, index);
					m_cache[0] = b;
				}
				result = b;
				break;
			}
		}

		return result;
	}

	/**
	 * @return the column metadata for the result
	 */
	public synchronized ColumnMetaData[] getColumnMetaData() throws SQLException {
		if (m_columns == null) {
			ConnectionReference conref = m_resultref.getConnectionReference();
			TSConnection tsconn = conref.getTSConnection();
			m_columns = DbUtils.createColumnMetaData(tsconn, m_catalog, m_resultref.getMetaData());
		}
		return m_columns;
	}

	/**
	 * @return the maximum row downloaded so far (zero based)
	 */
	public int getMaxRow() {
		return m_maxrow;
	}

	/**
	 * @return the underlying result set. Caution! Do not manipulate this result
	 *         set directly (i.e. first, last, etc). Multiple views might be
	 *         using the same result set.
	 */
	public ResultSet getResultSet() {
		return m_resultset;
	}

	/**
	 * @return the underlying result set. Caution! Do not manipulate this result
	 *         set directly (i.e. first, last, etc). Multiple views might be
	 *         using the same result set.
	 */
	public ResultSetReference getResultSetReference() {
		return m_resultref;
	}

	/**
	 * @return the current row of the result set (this is 0 based)
	 */
	public int getRow() throws SQLException {
		return m_currentrow;
		// ResultSet resultset = getResultSet();
		// return resultset.getRow() - 1;
	}

	/**
	 * @return the number of rows in the result set
	 */
	public int getRowCount() {
		return m_rowcount;
	}

	/**
	 * @param row
	 *            the row to retrieve ( zero based )
	 * @return the row instance at the given row
	 */
	public synchronized RowInstance getRowInstance(int row) throws SQLException {
		Block block = getBlock(row);
		if (block == null) {
			block = loadBlock(row);
		}
		RowInstance instance = block.getRow(row);
		if (instance == null) {
			TSUtils.printMessage("RowCache.getRowInstance returned null instance:  row = " + row);
		}
		return instance;
	}

	/**
	 * @return true if the current result set cursor is after the last row
	 */
	public boolean isAfterLast() throws SQLException {
		if (isRowCountKnown()) {
			if (m_resultset == null) {
				if (isEmpty())
					return true;
				else
					return (m_currentrow > 0);
			} else {
				if (isEmpty()) {
					return true;
				} else {
					m_resultset.absolute(m_currentrow + 1);
					return m_resultset.isAfterLast();
				}
			}
		} else {
			return false;
		}
	}

	/**
	 * @return true if the current result set cursor is before the first row
	 */
	public boolean isBeforeFirst() throws SQLException {
		if (m_resultset == null) {
			if (isEmpty())
				return true;
			else
				return (m_currentrow < 0);
		} else {
			if (isEmpty()) {
				return true;
			} else {
				m_resultset.absolute(m_currentrow + 1);
				return m_resultset.isBeforeFirst();
			}
		}
	}

	/**
	 * @return true if the current result set cursor is at the first row
	 */
	public boolean isFirst() throws SQLException {
		if (m_resultset == null) {
			if (isEmpty())
				return false;
			else
				return (m_currentrow == 0);
		} else {
			if (isEmpty()) {
				return false;
			} else {
				m_resultset.absolute(m_currentrow + 1);
				return m_resultset.isFirst();
			}
		}

	}

	/**
	 * @return true if the current result set cursor is at the last row
	 */
	public boolean isLast() throws SQLException {
		if (isRowCountKnown()) {
			if (m_resultset == null) {
				if (isEmpty())
					return false;
				else
					return (m_currentrow > 0);
			} else {
				if (isEmpty()) {
					return false;
				} else {
					m_resultset.absolute(m_currentrow + 1);
					return m_resultset.isLast();
				}
			}
		} else {
			return false;
		}
	}

	/**
	 * @return true if the result set has no rows
	 */
	public boolean isEmpty() {
		return (m_rowcount == 0);
	}

	/**
	 * @return true if we know the exact row count in the result set
	 */
	public boolean isRowCountKnown() {
		return (m_rowcount != -1);
	}

	/**
	 * Moves the result set cursor to the last row
	 */
	public boolean last() throws SQLException {
		ResultSet resultset = getResultSet();

		boolean bresult = resultset.last();
		assert (bresult);
		m_rowcount = resultset.getRow();
		m_maxrow = m_rowcount;
		m_counter = m_rowcount - 1;

		TSUtils.printMessage("RowCache.last   row = " + m_rowcount);
		m_currentrow = resultset.getRow() - 1;

		return bresult;
	}

	/**
	 * Loads a block of rows
	 */
	private synchronized Block loadBlock(int row) throws SQLException {
		long last_hit = 0;
		int cache_index = 0;
		for (int index = 0; index < m_cache.length; index++) {
			Block b = m_cache[index];
			if (b == null) {
				cache_index = index;
				break;
			} else if (b.contains(row)) {
				// we should not be here
				assert (false);
				return b;
			} else {
				cache_index = index;
			}
		}

		// block_start
		int block_start = (row / m_blocksize) * m_blocksize;
		Block block = loadBlock(block_start, m_blocksize);
		// int maxrow = block.getOffset() + m_blocksize;
		// if ( m_maxrow < maxrow )
		// m_maxrow = maxrow;
		if (m_maxrow < m_counter)
			m_maxrow = m_counter;

		m_cache[cache_index] = block;
		return block;
	}

	/**
	 * Creates a block object and fills the object by reading the row instances
	 * from the result set (offset is zero based)
	 */
	private synchronized Block loadBlock(int offset, int blockSize) throws SQLException {
		// System.out.println( "*** RowCache.loadBock  offset      " + offset );
		ResultSet resultset = getResultSet();

		ResultSetMetaData md = resultset.getMetaData();
		boolean[] use_string = new boolean[md.getColumnCount()];
		for (int index = 1; index <= md.getColumnCount(); index++) {
			int ctype = md.getColumnType(index);
			if (DbUtils.isJDBCType(ctype))
				use_string[index - 1] = false;
			else
				use_string[index - 1] = true;
		}

		Block block = new Block(offset, blockSize);
		// set the pointer of the result set to be the starting offset
		if (resultset.absolute(offset + 1)) {
			for (int rowindex = 0; rowindex < blockSize; rowindex++) {
				RowInstance row = new RowInstance(md.getColumnCount());
				int colcount = md.getColumnCount();
				for (int index = 1; index <= colcount; index++) {
					Object obj = null;
					try {
						if (castToString(index)) {
							obj = resultset.getString(index);
						} else {
							obj = resultset.getObject(index);
						}
					} catch (Throwable e) {
						try {
							obj = resultset.getString(index);
							setCastToString(index, true);
							TSUtils.printMessage(">>>>>>>>  ScrollableRowCache.force cast to string for result set index: "
									+ index);
						} catch (Throwable ohboy) {
							obj = "#ERROR#: " + ohboy.getMessage();
						}
					}
					row.setObject(index - 1, obj);
				}
				block.setRow(offset + rowindex, row);

				int count = offset + rowindex;
				if (m_counter < count)
					m_counter = count;

				// result set offsets are 1 based, so the we do this last
				// because the offset we are passing in is zero based
				if (!resultset.next()) {
					m_rowcount = m_counter + 1;
					// System.out.println(
					// "we are at the end of the result set    rowcount = " +
					// m_rowcount );
					break;
				}
			}
		} else {
			// System.out.println(
			// "we are at the end of the result set  (resultset.absolute failed) setting m_counter = "
			// + m_counter + "  offset: " + offset );
			if (offset == 0) {
				m_maxrow = 0;
				m_rowcount = 0; // result set is empty
			} else {
				m_rowcount = m_counter + 1;
			}
		}
		return block;
	}

	/**
	 * Moves the result set cursor to the next row
	 */
	public boolean next() throws SQLException {
		ResultSet resultset = getResultSet();

		boolean aresult = resultset.absolute(m_currentrow + 1);
		// System.out.println( "Rowcache.next abs.result = " + aresult +
		// "  currentrow = " + m_currentrow + "  resutlset.row " +
		// resultset.getRow() );

		boolean bresult = resultset.next();
		if (bresult) {
			// System.out.println( "Rowcach.got next row: " + resultset.getRow()
			// );
			m_currentrow = resultset.getRow() - 1;
			// load the block into the cache
			getRowInstance(resultset.getRow() - 1);
		}

		return bresult;
	}

	/**
	 * Moves the result set cursor to the next row
	 */
	public boolean previous() throws SQLException {
		ResultSet resultset = getResultSet();

		resultset.absolute(m_currentrow + 1);
		boolean bresult = resultset.previous();
		if (bresult) {
			m_currentrow = resultset.getRow() - 1;
			// load the block into the cache
			getRowInstance(resultset.getRow() - 1);
		}

		return bresult;
	}

	/**
	 * Flags a column in the result set for this cache if we should always
	 * retrieved the value by getString instead of getObject. We default to
	 * getObject, but if an error occurs, we flag the column as requiring
	 * getString.
	 * 
	 * @param colIndex
	 *            the 1-based index to check
	 */
	private void setCastToString(int colIndex, boolean bval) {
		int index = colIndex - 1;
		TSUtils.ensureSize(m_cast_to_string, colIndex);
		m_cast_to_string.set(index, Boolean.valueOf(bval));
	}

	/**
	 * Sets the current row in the result set
	 * 
	 * @param row
	 *            the row to set (zero based)
	 */
	public boolean setRow(int row) throws SQLException {
		ResultSet resultset = getResultSet();
		boolean bresult = resultset.absolute(row + 1);
		if (bresult)
			m_currentrow = row;

		return bresult;
	}

	/**
	 * Tells the row cache to set the isRowCountKnown flag to true (even if the
	 * result set is not fully downloaded). This also sets the max row and last
	 * row to the current max row. This is primarly used when the user makes
	 * changes to the result set and the set has not been fully downloaded. In
	 * this case, we simply truncate to prevent problems.
	 */
	public void truncateResults() {
		m_rowcount = m_maxrow + 1;
	}

}
