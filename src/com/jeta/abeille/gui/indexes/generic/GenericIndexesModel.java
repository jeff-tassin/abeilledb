package com.jeta.abeille.gui.indexes.generic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbVersion;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.gui.command.SQLCommand;
import com.jeta.abeille.gui.common.MetaDataTableModel;

import com.jeta.abeille.gui.indexes.TableIndex;
import com.jeta.abeille.gui.indexes.IndexColumn;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is the table model for the MySQL IndexesView. It gets the list of
 * indices a given table id and stores them in this model.
 * 
 * @author Jeff Tassin
 */
public class GenericIndexesModel extends MetaDataTableModel {

	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int COLUMNS_COLUMN = 1;
	static final int UNIQUE_COLUMN = 2;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 */
	public GenericIndexesModel(TSConnection connection, TableId tableId) {
		super(connection, tableId);

		String[] names = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Columns"),
				I18N.getLocalizedMessage("Unique") };

		Class[] types = { String.class, String.class, Boolean.class };

		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * Creates an index in the database
	 */
	public void createIndex(TableIndex index) throws SQLException {
		// String sql = MySQLIndexesModel.getCreateSQL( index );
		// SQLCommand.runModalCommand( getConnection(),
		// I18N.format("Creating_index_on_1",
		// getTableId().getFullyQualifiedName() ), sql );
	}

	/**
	 * Drops the index in the database
	 */
	public void dropIndex(TableIndex index) throws SQLException {
		// String sql = MySQLIndexesModel.getDropSQL( index );
		// SQLCommand.runModalCommand( getConnection(),
		// I18N.format("Dropping_index_1", index.getName() ), sql );
	}

	/**
	 * @return the SQL string to create the given index
	 */
	public static String getCreateSQL(TableIndex index) {
		StringBuffer sqlbuff = new StringBuffer();
		return sqlbuff.toString();
	}

	/**
	 * @return the SQL string to drop the given index
	 */
	public static String getDropSQL(TableIndex index) {
		StringBuffer sqlbuffer = new StringBuffer("DROP INDEX ");
		sqlbuffer.append(index.getName());
		sqlbuffer.append(" ON ");
		sqlbuffer.append(index.getTableId().getFullyQualifiedName());
		return sqlbuffer.toString();
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/** "Name", "Columns", "Unique" */
		TableIndex index = (TableIndex) getRow(row);
		if (column == NAME_COLUMN) {
			return index.getName();
		} else if (column == COLUMNS_COLUMN) {
			return index.getColumnsString();
		} else if (column == UNIQUE_COLUMN) {
			return Boolean.valueOf(index.isUnique());
		} else
			return "";
	}

	/**
	 * Drops and then re-creates an index in the database. Assumes that the
	 * index name has not changed.
	 */
	public void modifyIndex(TableIndex newindex, TableIndex oldindex) throws SQLException {
		// /String sql1 = MySQLIndexesModel.getDropSQL( oldindex );
		// String sql2 = MySQLIndexesModel.getCreateSQL( newindex );
		// SQLCommand.runMetaDataCommand( getConnection(),
		// I18N.format("Modifying_index_1", newindex.getName() ), sql1, sql2 );
	}

	/**
	 * Rebuilds the selected index
	 */
	public void rebuildIndex(TableIndex index) throws SQLException {

	}

	/**
	 * Rebuilds all indexes for the current table
	 */
	public void rebuildAllIndexes() throws SQLException {

	}

	/**
	 * Reloads the model
	 */
	public void reload() {
		removeAll();
		if (getTableId() == null) {
			fireTableDataChanged();
			return;
		}

		try {
			TSConnection tsconn = getConnection();
			Connection conn = tsconn.getMetaDataConnection();
			DatabaseMetaData metadata = conn.getMetaData();
			TableId tableid = getTableId();
			ResultSet rset = metadata.getIndexInfo(tableid.getCatalog().getName(), tableid.getSchema().getName(),
					tableid.getTableName(), false, false);

			LinkedHashMap indexes = new LinkedHashMap();
			while (rset.next()) {
				short index_type = rset.getShort("TYPE");
				if (index_type != DatabaseMetaData.tableIndexStatistic) {
					String colname = rset.getString("COLUMN_NAME");
					short pos = rset.getShort("ORDINAL_POSITION");
					pos--; // we use zero-based ordinal positions in the app

					String idxname = rset.getString("INDEX_NAME");
					boolean nonunique = rset.getBoolean("NON_UNIQUE");
					String tablename = rset.getString("TABLE_NAME");
					if (TSUtils.isDebug()) {
						if (!tablename.equalsIgnoreCase(tableid.getTableName())) {
							TSUtils.printMessage(">>>>>>ERROR. indexes: tablename: " + tablename + "   tableid.name: "
									+ tableid.getTableName());
							assert (false);
						}
					}

					TableIndex index = (TableIndex) indexes.get(idxname);
					if (index == null) {
						index = new TableIndex(tableid, idxname);
						index.setUnique(!nonunique);
						indexes.put(idxname, index);
					}
					assert (pos >= 0);
					index.setColumn(pos, new IndexColumn(colname));
				}

			}
			rset.close();

			Iterator iter = indexes.values().iterator();
			while (iter.hasNext()) {
				addRow(iter.next());
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		fireTableDataChanged();
	}

	/**
	 * Sets the current table id for the model. Reloads the indices.
	 */
	public void setTableId(TableId tableId) {
		super.setTableId(tableId);
		reload();
	}

}
