package com.jeta.abeille.database.utils;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TransposedResultSet implements ResultSet {

    // 0-based index
    private Map<Integer,RowInstance> m_rows = new HashMap<>();

    // map of pseudo-column names to column index
    private Map<String, Integer> m_columnsByName = new HashMap<String, Integer>();

    // 1-based index
    private Map<Integer,ColumnMetaData> m_columnsByIndex = new HashMap<>();

    private ResultSetMetaData m_metadata = new TransposedMetaData();
    private int m_pos = -1;


    public TransposedResultSet(QueryResultSet qset) throws SQLException {
        ResultSetMetaData metadata = qset.getMetaData();

        ColumnMetaData cmd = new ColumnMetaData( "Column Name", Types.OTHER, 0, null, ResultSetMetaData.columnNoNulls );
        m_columnsByName.put( "Column Name", 1 );

        qset.first();
        qset.previous(); // move before first
        int MAX_ROWS = 20;
        int i = 1;
        for( ; i <= MAX_ROWS && qset.next(); i++ ) {
            for (int j = 0; j < metadata.getColumnCount(); j++) {
                RowInstance row = m_rows.get(j);
                if ( row == null ) {
                    row = new RowInstance(MAX_ROWS+1);
                    m_rows.put(j, row);
                }
                row.setObject(i, qset.getRowInstance(qset.getRow()).getObject(j));

                if ( i == 1 ) {
                    row.setObject(0, metadata.getColumnName(j+1));
                }
            }

            ColumnMetaData rcmd = new ColumnMetaData( "Row " + i, Types.OTHER, 0, null, ResultSetMetaData.columnNoNulls );
            m_columnsByName.put( rcmd.getColumnName(), i + 1 );
            m_columnsByIndex.put( i + 1, rcmd );
        }

        m_columnsByIndex.put(1, cmd );
        for( int k = 0; k < m_rows.size(); k++ ) {
          m_rows.get(k).truncate(i);
        }

        qset.first();
    }

    @Override
    public boolean next() throws SQLException {
        int nextPos = m_pos + 1;
        if ( nextPos >= m_rows.size() ) {
            return false;
        }
        m_pos = nextPos;
        return true;
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return val == null ? null : val.toString();
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return val != null && Boolean.parseBoolean(val.toString());
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return (byte)val;
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return (short)val;
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return (int)val;
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return (long)val;
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return (float)val;
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return (double)val;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        Object val = getObject(columnIndex);
        return val == null ? null : new BigDecimal(val.toString());
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return val == null ? null : val.toString().getBytes();
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return val ==  null ? null : (Date)val;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return val ==  null ? null : (Time)val;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        Object val = getObject(columnIndex);
        return val ==  null ? null : (Timestamp)val;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return val == null ? null : val.toString();
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return val != null && Boolean.parseBoolean(val.toString());
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return (byte)val;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return (short)val;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return (int)val;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return (long)val;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return (float)val;
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return (double)val;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        Object val = getObject(columnLabel);
        return val == null ? null : new BigDecimal(val.toString());
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return val == null ? null : val.toString().getBytes();
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return (Date)val;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return (Time)val;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        Object val = getObject(columnLabel);
        return (Timestamp)val;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public String getCursorName() throws SQLException {
        return null;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return m_metadata;
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        if ( m_pos < 0 || m_pos >= m_rows.size() ) {
            throw new SQLException("Invalid index " + m_pos );
        }
        return m_rows.get(m_pos).getObject(columnIndex-1);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        Integer idx = m_columnsByName.get(columnLabel);
        return idx == null ? null : getObject(idx);
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        Integer idx = m_columnsByName.get(columnLabel);
        if ( idx == null ) {
            throw new SQLException("Invalid column " + columnLabel );
        }
        return idx;
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return m_pos < 0;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return m_pos >= m_rows.size();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return m_pos == 0;
    }

    @Override
    public boolean isLast() throws SQLException {
        return m_pos == m_rows.size() - 1;
    }

    @Override
    public void beforeFirst() throws SQLException {
        m_pos = -1;

    }

    @Override
    public void afterLast() throws SQLException {
        m_pos = m_rows.size();
    }

    @Override
    public boolean first() throws SQLException {
        m_pos = 0;
        return true;
    }

    @Override
    public boolean last() throws SQLException {
        m_pos = m_rows.size() - 1;
        return true;
    }

    @Override
    public int getRow() throws SQLException {
        return m_pos + 1;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        m_pos = row - 1;
        return false;
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return false;
    }

    @Override
    public boolean previous() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {

    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {

    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {

    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {

    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {

    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {

    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {

    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {

    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {

    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {

    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {

    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {

    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {

    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {

    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {

    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {

    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {

    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {

    }

    @Override
    public void insertRow() throws SQLException {

    }

    @Override
    public void updateRow() throws SQLException {

    }

    @Override
    public void deleteRow() throws SQLException {

    }

    @Override
    public void refreshRow() throws SQLException {

    }

    @Override
    public void cancelRowUpdates() throws SQLException {

    }

    @Override
    public void moveToInsertRow() throws SQLException {

    }

    @Override
    public void moveToCurrentRow() throws SQLException {

    }

    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {

    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {

    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {

    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {

    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {

    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {

    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }


    /************************************************************************************************************************
     *
     ************************************************************************************************************************/
    class TransposedMetaData implements ResultSetMetaData {

        @Override
        public int getColumnCount() throws SQLException {
            return m_columnsByIndex.size();
        }

        @Override
        public boolean isAutoIncrement(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isCaseSensitive(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isSearchable(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isCurrency(int column) throws SQLException {
            return false;
        }

        @Override
        public int isNullable(int column) throws SQLException {
            return 0;
        }

        @Override
        public boolean isSigned(int column) throws SQLException {
            return false;
        }

        @Override
        public int getColumnDisplaySize(int column) throws SQLException {
            return 0;
        }

        @Override
        public String getColumnLabel(int column) throws SQLException {
            return getColumnName(column);
        }

        @Override
        public String getColumnName(int column) throws SQLException {
            return m_columnsByIndex.get(column).getColumnName();
        }

        @Override
        public String getSchemaName(int column) throws SQLException {
            return null;
        }

        @Override
        public int getPrecision(int column) throws SQLException {
            return 0;
        }

        @Override
        public int getScale(int column) throws SQLException {
            return 0;
        }

        @Override
        public String getTableName(int column) throws SQLException {
            return null;
        }

        @Override
        public String getCatalogName(int column) throws SQLException {
            return null;
        }

        @Override
        public int getColumnType(int column) throws SQLException {
            return Types.OTHER;
        }

        @Override
        public String getColumnTypeName(int column) throws SQLException {
            return "Object";
        }

        @Override
        public boolean isReadOnly(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isWritable(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isDefinitelyWritable(int column) throws SQLException {
            return false;
        }

        @Override
        public String getColumnClassName(int column) throws SQLException {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
