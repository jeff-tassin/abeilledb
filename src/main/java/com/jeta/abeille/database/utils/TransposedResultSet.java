package com.jeta.abeille.database.utils;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


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

                row.setObject(0, metadata.getColumnName(j+1));
                if (!qset.isEmpty()) {
                    row.setObject(i, qset.getRowInstance(i-1).getObject(j));
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

    public void dump() {
        System.out.println("Columns by Name---------");
        for (Map.Entry<String,Integer> entry : m_columnsByName.entrySet()) {
            System.out.println( entry.getKey() + " = " + entry.getValue() );
        }
        System.out.println("Columns by Index---------");
        for (Map.Entry<Integer, ColumnMetaData> entry : m_columnsByIndex.entrySet()) {
            System.out.println( entry.getKey() + " = " + entry.getValue().getColumnName() );
        }
        System.out.println("Rows---------");
        for (Map.Entry<Integer,RowInstance> entry : m_rows.entrySet()) {
            RowInstance row = entry.getValue();
            System.out.println( entry.getKey() + ":  length=" + row.getLength() );
            for( int col = 0; col < row.getLength(); col++ ) {
                String colName = m_columnsByIndex.get(col+1).getColumnName();
                System.out.println( "   " + colName + " = " + row.getObject(col) );
            }
            break;
        }
    }

}
