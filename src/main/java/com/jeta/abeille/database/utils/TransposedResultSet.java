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

}
