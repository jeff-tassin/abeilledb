package com.jeta.abeille.database.utils;

import java.sql.Clob;
import java.sql.Blob;
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
 * This class caches data from a forward only result set. Everything gets cached
 * here - including LOB objects
 * 
 * @author Jeff Tassin
 */
public class ForwardOnlyRowCache extends RowCache {
	// the size of a block
	// the DEBUG_BLOCK_SIZE is set to a small value for testing
	private static final int DEFAULT_BLOCK_SIZE = 30;
	private int m_blocksize = DEFAULT_BLOCK_SIZE;

	/**
	 * this is the number of rows returned in the result set. We don't know this
	 * value until we download the entire result set
	 */
	private int m_rowcount = -1;

	/**
	 * this is the current row (zero based). we don't use the result set row
	 * because multiple views might be using the same result set
	 */
	private int m_currentrow;

	/** this is the max row we've downloaded so far (zero based) */
	private int m_maxrow = 0;

	// 3 blocks of instances (each block size )
	private ArrayList m_cache = new ArrayList();

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
	 * this flag controls whether the cache will automatically download blob and
	 * clob types when it encounteres them. This is needed when loading multiple
	 * resultsets for a single statement for those databases that don't support
	 * this
	 */
	private boolean m_force_cache = false;

	/**
	 * This is an array of Booleans used to force a column index to a string. We
	 * tag this in case resultset.getObject(index) throws and exception. We
	 * assume that resultset.getString(index) always succeeds. PostgreSQL will
	 * throw an exception for getObject in some cases.
	 */
	private ArrayList m_cast_to_string = new ArrayList();

	/**
	 * ctor
	 */
	public ForwardOnlyRowCache(ResultSetReference rsetref) throws SQLException {
		this(null, rsetref, false);
	}

	/**
	 * ctor
	 */
	public ForwardOnlyRowCache(Catalog catalog, ResultSetReference rsetref) throws SQLException {
		this(catalog, rsetref, false);
	}

	/**
	 * ctor
	 */
	public ForwardOnlyRowCache(Catalog catalog, ResultSetReference rsetref, boolean force_cache) throws SQLException {
		TSUtils.printMessage(">>>>>>>>>>>>>>>>  Created ForwardOnlyRowCache for " + rsetref.getSQL());
		m_catalog = catalog;
		m_force_cache = force_cache;
		m_resultset = rsetref.getResultSet();
		m_resultref = rsetref;

		/** let's set the block size to a randome number for testing */
		if (TSUtils.isTest()) {
			m_blocksize = 1 + (int) (Math.random() * 100);
			TSUtils.printMessage("ForwardOnlyRowCache setting block size to " + m_blocksize);
		}

		m_currentrow = 0;
		// load the first block
		getRowInstance(0);
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
		int block_index = getBlockIndex(row);
		return (block_index < m_cache.size());
	}

	/**
	 * Moves the result set cursor to the first row in the set
	 */
	public boolean first() throws SQLException {
		if (isEmpty())
			return false;
		else {
			m_currentrow = 0;
			return true;
		}
	}

	/**
	 * @return the data for the given column as a byte array. Note that the
	 *         column must be a binary stream type.
	 */
	public byte[] getBinaryData(String columnName) throws SQLException {
		assert (false);
		return null;
	}

	/**
	 * @return the clob data for the given column. Note that the column must be
	 *         a clob object
	 */
	public String getClobData(String columnName) throws SQLException {
		assert (false);
		return null;
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
		if (m_cache.size() == 0)
			return null;

		Block result = null;
		int block_index = getBlockIndex(row);
		if (block_index < m_cache.size()) {
			result = (Block) m_cache.get(block_index);
			assert (result != null);
		}
		return result;
	}

	/**
	 * @return the block index for the given row
	 */
	private int getBlockIndex(int row) {
		return row / m_blocksize;
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
		if (isRowCountKnown()) {
			if (row >= m_rowcount)
				return null;
		}

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
		if (m_resultset == null) {
			if (isEmpty())
				return true;
			else
				return (m_currentrow > 0);
		} else {
			if (isEmpty()) {
				return true;
			} else {
				if (isRowCountKnown()) {
					return (m_currentrow >= getRowCount());
				} else {
					return false;
				}
			}
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
				return (m_currentrow < 0);
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
				return (m_currentrow == 0);
			}
		}

	}

	/**
	 * this flag controls whether the cache will automatically download blob and
	 * clob types when it encounteres them. This is needed when loading multiple
	 * resultsets for a single statement for those databases that don't support
	 * this
	 */
	public boolean isForceCache() {
		return m_force_cache;
	}

	/**
	 * @return true if the current result set cursor is at the last row
	 */
	public boolean isLast() throws SQLException {
		if (m_resultset == null) {
			if (isEmpty())
				return false;
			else
				return (m_currentrow > 0);
		} else {
			if (isEmpty()) {
				return false;
			} else {
				if (isRowCountKnown()) {
					return (m_currentrow == (getRowCount() - 1));
				} else {
					return false;
				}
			}
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
		if (isEmpty()) {
			return false;
		} else if (isRowCountKnown()) {
			m_currentrow = getRowCount() - 1;
			return true;
		} else {
			while (next())
				;
			return true;
		}
	}

	/**
	 * Loads a block of rows
	 */
	private synchronized Block loadBlock(int row) throws SQLException {
		long last_hit = 0;
		int block_index = getBlockIndex(row);

		// block_start
		int block_start = (row / m_blocksize) * m_blocksize;
		Block block = readBlock(block_start, m_blocksize);
		TSUtils.ensureSize(m_cache, block_index + 1);
		m_cache.set(block_index, block);
		return block;
	}

	/**
	 * Creates a block object and fills the object by reading the row instances
	 * from the result set (offset is zero based)
	 */
	private synchronized Block readBlock(int offset, int blockSize) throws SQLException {
		// System.out.println( "******** RowCache.readBlockloadBock  offset: " +
		// offset + "   blocksize: " + blockSize );
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
		if (resultset.next()) {
			for (int rowindex = 0; rowindex < blockSize; rowindex++) {
				RowInstance row = new RowInstance(md.getColumnCount());
				int colcount = md.getColumnCount();
				for (int index = 1; index <= colcount; index++) {
					Object obj = null;
					try {
						if (castToString(index))
							obj = resultset.getString(index);
						else
							obj = resultset.getObject(index);
					} catch (Throwable e) {
						obj = resultset.getString(index);
						setCastToString(index, true);
						TSUtils.printMessage(">>>>>>>>  ForwardOnlyRowCache.force cast to string for result set index: "
								+ index);
					}

					if (isForceCache()) {
						if (obj instanceof Blob) {
							row.setObject(index - 1, DbUtils.getBinaryData((Blob) obj));
						} else if (obj instanceof Clob) {
							row.setObject(index - 1, DbUtils.getCharacterData((Clob) obj));
						} else {
							row.setObject(index - 1, obj);
						}
					} else {
						/**
						 * defer blobs and clobs handling to the instance view
						 * and query results views
						 */
						row.setObject(index - 1, obj);
					}
				}

				// System.out.println(
				// "ForwardOnlyRowCache.loadBlock setRow:   offset: " + offset +
				// "  rowindex: " + rowindex + "  current_row: " +
				// resultset.getRow() );
				block.setRow(offset + rowindex, row);

				int curr_row = resultset.getRow();
				if ((rowindex + 1) < blockSize) {
					// result set offsets are 1 based, so the we do this last
					// because the offset we are passing in is zero based
					if (resultset.next()) {
						m_maxrow = resultset.getRow() - 1;
					} else {
						m_rowcount = curr_row;
						// System.out.println(
						// "we are at the end of the result set    rowcount = "
						// + m_rowcount );
						assert ((offset + rowindex + 1) == curr_row);
						break;
					}
				} else {
					m_maxrow = curr_row;
				}
			}
		} else {
			if (offset == 0) {
				m_maxrow = 0;
				m_rowcount = 0; // result set is empty
			} else {
				m_rowcount = offset;
			}
			// System.out.println(
			// "we are at the end of the result set rowcount = " + m_rowcount );

		}
		return block;
	}

	/**
	 * Moves the result set cursor to the next row
	 */
	public boolean next() throws SQLException {
		if (isEmpty())
			return false;

		boolean bresult = false;
		ResultSet resultset = getResultSet();
		if (isRowCountKnown()) {
			if ((m_currentrow + 1) < m_rowcount) {
				m_currentrow++;
				bresult = true;
			}
		} else {
			int curr_row = m_currentrow;
			curr_row++;
			RowInstance rowinstance = getRowInstance(curr_row);
			if (rowinstance != null) {
				m_currentrow = curr_row;
				bresult = true;
			}
		}
		return bresult;
	}

	/**
	 * Moves the result set cursor to the next row
	 */
	public boolean previous() throws SQLException {
		if (isEmpty())
			return false;

		ResultSet resultset = getResultSet();
		if (m_currentrow < 0) {
			return false;
		} else {
			m_currentrow--;
			return true;
		}
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
		if (isEmpty())
			return false;

		boolean bresult = false;
		if (isRowCountKnown()) {
			if (row >= 0 && row < m_rowcount) {
				m_currentrow = row;
				bresult = true;
			}
		} else {
			if (row > m_maxrow) {
				while (next()) {
					if (row <= m_maxrow) {
						bresult = true;
						break;
					}
				}
			} else {
				m_currentrow = row;
				bresult = true;
			}
		}
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
