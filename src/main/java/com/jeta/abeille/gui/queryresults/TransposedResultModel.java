package com.jeta.abeille.gui.queryresults;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.MetaDataTableModel;
import com.jeta.foundation.i18n.I18N;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * This table model is for displaying a single row from a ResultSet in a
 * transposed view.
 * 
 * @author Jeff Tassin
 */
public class TransposedResultModel extends MetaDataTableModel {

	/**
	 * @param tsconn  the database connection
	 * @param rset the result set whose row we wish to display. This result set
	 *            must have a valid current row
	 */
	public TransposedResultModel(TSConnection tsconn, ResultSet rset) {
		super(tsconn, null);

		String[] colnames = new String[2];
		colnames[0] = I18N.getLocalizedMessage("Name");
		colnames[1] = I18N.getLocalizedMessage("Value");

		Class[] coltypes = new Class[2];
		coltypes[0] = String.class;
		coltypes[1] = String.class;

		setColumnNames(colnames);
		setColumnTypes(coltypes);

		try {
			ResultSetMetaData metadata = rset.getMetaData();
			for (int index = 1; index <= metadata.getColumnCount(); index++) {
				String colname = metadata.getColumnName(index);
				String[] row = new String[2];
				row[0] = colname;
				row[1] = rset.getString(index);
				addRow(row);
			}
		} catch (Exception e) {

		}
	}

	/**
	 * @return the value at the given row column.
	 */
	public Object getValueAt(int row, int col) {
		String[] obj = (String[]) getRow(row);
		return obj[col];
	}
}
