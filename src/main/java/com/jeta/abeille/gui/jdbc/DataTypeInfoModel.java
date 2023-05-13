package com.jeta.abeille.gui.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.table.DefaultTableModel;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.utils.TSUtils;

/**
 * This is a table model for displaying data types in a database.
 * 
 * @author Jeff Tassin
 */
public class DataTypeInfoModel extends DefaultTableModel {
	public DataTypeInfoModel(TSConnection conn) throws SQLException {
		DatabaseMetaData dbmetadata = conn.getMetaData();
		ResultSet rset = dbmetadata.getTypeInfo();

		DefaultTableModel tmodel = new DefaultTableModel();
		ResultSetMetaData metadata = rset.getMetaData();

		int index_offset = -1;
		for (int index = 1; index <= metadata.getColumnCount(); index++) {
			String colname = metadata.getColumnName(index);
			addColumn(colname);

			if (index_offset < 0 && "DATA_TYPE".equalsIgnoreCase(colname)) {
				addColumn("JDBC_TYPE");
				index_offset = index;
			}
		}

		while (rset.next()) {
			Object[] row = new Object[metadata.getColumnCount() + 1];
			for (int index = 1; index <= metadata.getColumnCount(); index++) {
				if ((index - 1) < index_offset) {
					row[index - 1] = rset.getObject(index);
				} else if ((index - 1) == index_offset) {
					try {
						int jdbc_type = rset.getInt("DATA_TYPE");
						String jdbc_label = DbUtils.getJDBCTypeName(jdbc_type);
						if (!"UNKNOWN".equalsIgnoreCase(jdbc_label)) {
							row[index - 1] = jdbc_label;
						}
						row[index] = rset.getObject(index);
					} catch (SQLException se) {
						TSUtils.printException(se);
					}
				} else {
					row[index] = rset.getObject(index);
				}
			}
			addRow(row);
		}
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}

}
