package com.jeta.abeille.gui.indexes.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

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
public class MySQLIndexesModel extends MetaDataTableModel {

	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int COLUMNS_COLUMN = 1;
	static final int UNIQUE_COLUMN = 2;
	static final int TYPE_COLUMN = 3;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 */
	public MySQLIndexesModel(TSConnection connection, TableId tableId) {
		super(connection, tableId);

		String[] names = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Columns"),
				I18N.getLocalizedMessage("Unique"), I18N.getLocalizedMessage("Type") };

		Class[] types = { String.class, String.class, Boolean.class, String.class };

		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * Creates an index in the database
	 */
	public void createIndex(TableIndex index) throws SQLException {
		String sql = MySQLIndexesModel.getCreateSQL(index);
		SQLCommand.runModalCommand(getConnection(),
				I18N.format("Creating_index_on_1", getTableId().getFullyQualifiedName()), sql);
	}

	/**
	 * Drops the index in the database
	 */
	public void dropIndex(TableIndex index) throws SQLException {
		String sql = MySQLIndexesModel.getDropSQL(index);
		SQLCommand.runModalCommand(getConnection(), I18N.format("Dropping_index_1", index.getName()), sql);
	}

	/**
	 * @return the SQL string to create the given index
	 */
	public static String getCreateSQL(TableIndex index) {
		StringBuffer sqlbuff = new StringBuffer();

		// CREATE [UNIQUE|FULLTEXT] INDEX index_name ON tbl_name
		// (col_name[(length)],... )

		sqlbuff.append("CREATE");
		if (index.isUnique())
			sqlbuff.append(" UNIQUE");
		else {
			if (I18N.equalsIgnoreCase(index.getType(), "FULLTEXT")) {
				sqlbuff.append(" FULLTEXT");
			}
		}

		sqlbuff.append(" INDEX ");
		sqlbuff.append(index.getName());
		sqlbuff.append(" ON ");
		sqlbuff.append(index.getTableId().getFullyQualifiedName());

		sqlbuff.append(" ( ");

		Collection cols = index.getIndexColumns();
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			IndexColumn icol = (IndexColumn) iter.next();
			sqlbuff.append(icol.getName());
			Integer len = (Integer) icol.getAttribute();
			if (len != null) {
				sqlbuff.append("(");
				sqlbuff.append(len);
				sqlbuff.append(")");
			}
			if (iter.hasNext())
				sqlbuff.append(", ");
		}
		sqlbuff.append(" );");
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
		sqlbuffer.append(";");
		return sqlbuffer.toString();
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/** "Name", "Columns", "Unique", "Ascend", "Type" */
		TableIndex index = (TableIndex) getRow(row);
		if (column == NAME_COLUMN) {
			return index.getName();
		} else if (column == COLUMNS_COLUMN) {
			return index.getColumnsString();
		} else if (column == UNIQUE_COLUMN) {
			return Boolean.valueOf(index.isUnique());
		} else if (column == TYPE_COLUMN) {
			return index.getType();
		} else
			return "";
	}

	/**
	 * Drops and then re-creates an index in the database. Assumes that the
	 * index name has not changed.
	 */
	public void modifyIndex(TableIndex newindex, TableIndex oldindex) throws SQLException {
		String sql1 = MySQLIndexesModel.getDropSQL(oldindex);
		String sql2 = MySQLIndexesModel.getCreateSQL(newindex);
		SQLCommand
				.runMetaDataCommand(getConnection(), I18N.format("Modifying_index_1", newindex.getName()), sql1, sql2);
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
			// now get the index
			StringBuffer sqlbuff = new StringBuffer();

			sqlbuff.append("show index from ");
			sqlbuff.append(getTableId().getCatalog());
			sqlbuff.append(".");
			sqlbuff.append(getTableId().getTableName());

			TSDatabase db = (TSDatabase) getConnection().getImplementation(TSDatabase.COMPONENT_ID);
			DbVersion version = db.getVersion();

			Connection conn = getConnection().getMetaDataConnection();
			Statement stmt = null;
			try {
				HashMap indexes = new HashMap();
				stmt = conn.createStatement();
				ResultSet rset = stmt.executeQuery(sqlbuff.toString());
				while (rset.next()) {
					String keyname = rset.getString("KEY_NAME");
					long seq_in_index = rset.getLong("Seq_in_index");
					String colname = rset.getString("Column_name");
					long sub_part = rset.getLong("Sub_part");

					TableIndex tindex = (TableIndex) indexes.get(keyname);
					if (tindex == null) {
						long non_unique = rset.getLong("Non_unique");
						tindex = new TableIndex(getTableId());
						tindex.setName(keyname);
						if (keyname != null && I18N.equalsIgnoreCase(keyname, "PRIMARY")) {
							tindex.setPrimary(true);
						}

						tindex.setUnique((non_unique == 0));
						indexes.put(keyname, tindex);

						if (version.getMajor() == 4 && version.getMinor() >= 0 && version.getSub() >= 2) {
							String itype = rset.getString("Index_type");
							itype = itype.toUpperCase();
							tindex.setType(itype);
						} else {
							String itype = rset.getString("Comment");
							itype = itype.toUpperCase();
							tindex.setType(itype);
						}

						addRow(tindex);
					}
					int col_pos = (int) (seq_in_index - 1);
					IndexColumn icol = new IndexColumn(colname, TSUtils.getInteger((int) sub_part));
					tindex.setColumn(col_pos, icol);
				}

				if (getConnection().supportsTransactions()) {
					conn.commit();
				}
			} finally {
				if (stmt != null)
					stmt.close();
			}

		} catch (SQLException se) {
			TSUtils.printException(se);
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
