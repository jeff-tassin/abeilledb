package com.jeta.abeille.gui.queryresults;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.database.utils.TransposedResultSet;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableSettings;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.utils.TSUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for working with query results
 * 
 * @author Jeff Tassin
 */
public class QueryUtils {

	/**
	 * Runs a query against the metadata connection.
	 */
	public static AbstractTablePanel createQueryResultsView(String viewId, TSConnection tsconn, String sql)
			throws SQLException {
		Statement stmt = tsconn.createStatement();
		ResultSet rset = stmt.executeQuery(sql);
		QueryResultsModel model = new QueryResultsModel(tsconn, new ResultSetReference(new ConnectionReference(tsconn,
				stmt.getConnection()), stmt, rset, sql));
		model.last();
		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(model, true);
		restoreTableSettings(viewId, tsconn, tpanel);
		return tpanel;
	}

	/**
	 * Runs a query against the metadata connection.
	 */
	public static AbstractTablePanel createTransposedQueryResultsView(String viewId, TSConnection tsconn, String sql)
			throws SQLException {
		Statement stmt = tsconn.createStatement();
		ResultSet rset = stmt.executeQuery(sql);
		QueryResultsModel model = new QueryResultsModel(tsconn, new ResultSetReference(new ConnectionReference(tsconn,
				stmt.getConnection()), stmt, new TransposedResultSet(rset), sql));

		model.last();
		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(model, true);
		restoreTableSettings(viewId, tsconn, tpanel);
		return tpanel;
	}

	/**
	 * This method gets the current row in the result set and creates a
	 * transposed table using the values in the row. So, the row becomes two
	 * column (name,value) and each column becomes a row in the table. Note: the
	 * result set must contain a valid row and only the current row is
	 * displayed.
	 */
	public static AbstractTablePanel createTransposedResultView(String viewId, TSConnection tsconn, ResultSet rset)
			throws SQLException {
		//TransposedResultsModel model = new TransposedResultsModel(tsconn, rset);
		QueryResultsModel model = new QueryResultsModel(tsconn, new ResultSetReference(new ConnectionReference(tsconn,
				tsconn.getWriteConnection()), null, new TransposedResultSet(rset), null));
		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(model, true);
		restoreTableSettings(viewId, tsconn, tpanel);
		return tpanel;
	}

	/**
	 * Runs a query against the metadata connection.
	 */
	public static AbstractTablePanel createMetaDataResultsView(String viewId, TSConnection tsconn, String sql)
			throws SQLException {
		Statement stmt = tsconn.createScrollableMetaDataStatement();
		ResultSet rset = stmt.executeQuery(sql);
		QueryResultsModel model = new QueryResultsModel(tsconn, new ResultSetReference(new ConnectionReference(tsconn,
				stmt.getConnection()), stmt, rset, sql));
		model.last();
		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(model, true);
		// QueryResultsView view = new QueryResultsView( model );
		restoreTableSettings(viewId, tsconn, tpanel);
		return tpanel;
	}

	/**
	 * Builds a table for a given result set
	 */
	public static AbstractTablePanel createMetaDataResultsView(TSConnection tsconn, ResultSet rset) throws SQLException {
		DefaultTableModel tmodel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		ResultSetMetaData metadata = rset.getMetaData();

		for (int index = 1; index <= metadata.getColumnCount(); index++) {
			tmodel.addColumn(metadata.getColumnName(index));
		}

		while (rset.next()) {
			Object[] row = new Object[metadata.getColumnCount()];
			for (int index = 1; index <= metadata.getColumnCount(); index++) {
				row[index - 1] = rset.getObject(index);
			}
			tmodel.addRow(row);
		}

		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(tmodel, true);
		JTable table = tpanel.getTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableUtils.setColumnWidths(table);
		return tpanel;
	}



	/**
	 * Restores the table column widths to their previous settings for the given
	 * view id
	 */
	public static void restoreTableSettings(String viewId, TSConnection tsconn, AbstractTablePanel tablepanel) {
		try {
			ObjectStore ostore = tsconn.getObjectStore();
			TableSettings settings = (TableSettings) ostore.load(viewId);
			if (settings != null)
				TableUtils.restoreTableSettings(tablepanel, settings);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Stores the table column widths in the object store for the given view id
	 */
	public static void storeTableSettings(String viewId, TSConnection tsconn, AbstractTablePanel tablepanel) {
		try {
			ObjectStore ostore = tsconn.getObjectStore();
			ostore.store(viewId, TableUtils.getTableSettings(tablepanel));
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}
}
