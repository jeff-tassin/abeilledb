package com.jeta.abeille.gui.sql;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.TreeMap;

import javax.swing.JTable;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.abeille.gui.update.InstanceMetaData;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.update.TableInstanceMetaData;
import com.jeta.abeille.gui.update.TableInstanceViewBuilder;

import com.jeta.foundation.gui.table.TableSorter;

/**
 * This is an factory class for creating and saving TableInstance settings based
 * on the SQL results window configuration. If our SQLResults window shows data
 * from a query against a single table, then we allow the user to load the
 * instance frame for the visible data and edit the data. Also, the ordering of
 * the columns in the sql results is reflected in the instance form.
 * Furthermore, any instance frame settings for that table should be reflected
 * in this view as well.
 * 
 * @author Jeff Tassin
 */
public class TableQueryInstanceViewBuilder extends TableInstanceViewBuilder {
	/**
	 * this is the table that contains the data we will show in the instance
	 * frame.
	 */
	private JTable m_table;

	/**
	 * the sql results. we need this primarily to get the underlying result set
	 * when accessing binary data
	 */
	private SQLResultsModel m_sqlmodel;

	/**
	 * ctor
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param id
	 *            the table id that we are creating the metadata for
	 * @param tableModel
	 *            this is the table model for the SQL results view. This allows
	 *            us to get the visible columns, their ordering, as well as the
	 *            sorting of the data we will show in the instance view
	 * @param row
	 *            the current row in the query result
	 */
	public TableQueryInstanceViewBuilder(SQLResultsModel sqlmodel, TSConnection connection, TableId id, JTable table,
			int row) throws SQLException {
		super(connection, id);
		m_sqlmodel = sqlmodel;
		m_table = table;
		setInitialRow(row);
		_getMetaData();
	}

	/**
	 * Creates the InstanceMetaData object for the InstanceView/Model
	 */
	private InstanceMetaData _getMetaData() throws SQLException {
		if (m_metadata == null) {
			ResultSetReference resultref = m_sqlmodel.getResultSetReference();
			ResultSetMetaData metadata = resultref.getMetaData();
			ColumnMetaData[] cols = DbUtils.createColumnMetaData(getConnection(), m_tableid.getCatalog(), metadata);
			TreeMap colset = new TreeMap();
			for (int index = 0; index < cols.length; index++) {
				ColumnMetaData cmd = cols[index];
				// set the table id in case the JDBC driver does not set the
				// table in the result set metadata
				// (postresql is guilty of doing this so we need to manually set
				// )
				cmd.setParentTableId(m_tableid);
				colset.put(cmd.getColumnName(), cmd);
			}

			ColumnMetaData[] presets = new ColumnMetaData[m_table.getColumnCount()];
			for (int index = 0; index < m_table.getColumnCount(); index++) {
				String colname = m_table.getColumnName(index);
				ColumnMetaData cmd = (ColumnMetaData) colset.get(colname);
				assert (cmd != null);
				presets[index] = cmd;
			}
			m_metadata = new MyTableInstanceMetaData(getConnection(), m_tableid, presets);
		}

		return m_metadata;
	}

	public InstanceMetaData getMetaData() {
		return m_metadata;
	}

	/**
	 * @return the view for this instance data
	 */
	public InstanceView getView() {
		if (_getView() == null) {
			InstanceModel model = new InstanceModel(getConnection(), getMetaData());
			model.setInstanceProxy(new QueryInstanceProxy(m_sqlmodel, model, m_table, getInitialRow()));
			setView(new InstanceView(model));
		}
		return _getView();
	}

	public class MyTableInstanceMetaData extends TableInstanceMetaData {

		public MyTableInstanceMetaData(TSConnection conn, TableId tableId, ColumnMetaData[] cols) {
			super(conn, tableId, cols);
		}

		/**
		 * Saves the settings to the persistent store.
		 */
		public void saveSettings() {
			// let's not store the settings just yet
		}
	}
}
