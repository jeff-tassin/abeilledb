package com.jeta.abeille.database.utils;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import java.sql.*;


public class TransposedResultSet extends AbstractResultSet {

    public TransposedResultSet(QueryResultSet qset) throws SQLException {
        ResultSetMetaData metadata = qset.getMetaData();

        ColumnMetaData cmd = new ColumnMetaData( "Column Name", Types.OTHER, 0, null, ResultSetMetaData.columnNoNulls );
        m_columnsByName.put( "Column Name", 1 );

        qset.first();
        int MAX_ROWS = 10;
        int i = 1;
        do {
            for (int j = 0; j < metadata.getColumnCount(); j++) {
                RowInstance row = m_rows.get(j);
                if ( row == null ) {
                    row = new RowInstance(MAX_ROWS+1);
                    m_rows.put(j, row);
                }

                if ( i == 1 ) {
                    row.setObject(0, metadata.getColumnName(j+1));
                } else if (!qset.isEmpty()) {
                    row.setObject(i-1, qset.getRowInstance(i-1).getObject(j));
                }
            }

            ColumnMetaData rcmd = new ColumnMetaData( "Row " + i, Types.OTHER, 0, null, ResultSetMetaData.columnNoNulls );
            m_columnsByName.put( rcmd.getColumnName(), i + 1 );
            m_columnsByIndex.put( i + 1, rcmd );
        } while( qset.next() && ++i <= MAX_ROWS );

        m_columnsByIndex.put(1, cmd );

        for( int k = 0; k < m_rows.size(); k++ ) {
          m_rows.get(k).truncate(i+1);
        }
        qset.first();
    }

}
