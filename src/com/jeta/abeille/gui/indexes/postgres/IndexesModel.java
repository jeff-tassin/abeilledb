package com.jeta.abeille.gui.indexes.postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.procedures.StoredProcedureService;
import com.jeta.abeille.database.procedures.StoredProcedure;

import com.jeta.abeille.gui.command.SQLCommand;
import com.jeta.abeille.gui.common.MetaDataTableModel;

import com.jeta.abeille.gui.indexes.TableIndex;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is the table model for the IndexesView. It gets the list of
 * indices a given table id and stores them in this model.
 * 
 * @author Jeff Tassin
 */
public class IndexesModel extends MetaDataTableModel {
	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int COLUMNS_COLUMN = 1;
	static final int PRIMARY_KEY_COLUMN = 2;
	static final int UNIQUE_COLUMN = 3;
	static final int TYPE_COLUMN = 4;
	static final int FUNCTION_COLUMN = 5;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 */
	public IndexesModel(TSConnection connection, TableId tableId) {
		super(connection, tableId);

		String[] names = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Columns"),
				I18N.getLocalizedMessage("Primary Key"), I18N.getLocalizedMessage("Unique"),
				I18N.getLocalizedMessage("Type"), I18N.getLocalizedMessage("Function") };

		Class[] types = { String.class, String.class, Boolean.class, Boolean.class, String.class, String.class };

		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * Creates an index in the database
	 */
	public void createIndex(TableIndex index) throws SQLException {
		String sql = IndexesModel.getCreateSQL(index);
		SQLCommand.runModalCommand(getConnection(),
				I18N.format("Creating_index_on_1", getTableId().getFullyQualifiedName()), sql);
	}

	/**
	 * Drops the index in the database
	 */
	public void dropIndex(TableIndex index, boolean cascade) throws SQLException {
		String sql = IndexesModel.getDropSQL(index, cascade);
		SQLCommand.runModalCommand(getConnection(), I18N.format("Dropping_index_1", index.getName()), sql);
	}

	/**
	 * @return the SQL string to create the given index
	 */
	public static String getCreateSQL(TableIndex index) {
		StringBuffer sqlbuff = new StringBuffer();

		// CREATE UNIQUE INDEX title_idx ON films (title);

		sqlbuff.append("CREATE");
		if (index.isUnique())
			sqlbuff.append(" UNIQUE");

		sqlbuff.append(" INDEX ");
		sqlbuff.append(index.getName());
		sqlbuff.append(" ON ");
		sqlbuff.append(index.getTableId().getFullyQualifiedName());

		sqlbuff.append(" USING ");
		sqlbuff.append(index.getType());

		sqlbuff.append(" ( ");
		if (index.isFunctional()) {
			sqlbuff.append(index.getFunction());
			sqlbuff.append("( ");
		}

		Collection cols = index.getIndexColumns();
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			String colname = (String) iter.next();
			sqlbuff.append(colname);
			if (iter.hasNext())
				sqlbuff.append(", ");
		}
		if (index.isFunctional())
			sqlbuff.append(" )");

		sqlbuff.append(" );");

		return sqlbuff.toString();
	}

	/**
	 * @return the SQL string to drop the given index
	 */
	public static String getDropSQL(TableIndex index, boolean cascade) {
		StringBuffer sqlbuffer = new StringBuffer("DROP INDEX ");

		Schema schema = index.getTableId().getSchema();
		if (schema != Schema.VIRTUAL_SCHEMA) {
			sqlbuffer.append(schema.getName());
			sqlbuffer.append(".");
		}
		sqlbuffer.append(index.getName());
		if (cascade)
			sqlbuffer.append(" CASCADE");

		sqlbuffer.append(";");

		return sqlbuffer.toString();
	}

	/**
	 * @param indkey
	 *            the indkey of the pg_index table. This is a list of column
	 *            indexes (1-based) stored as a string. Each column is delimited
	 *            by a space
	 * @return a list of string objects that make up the columsn for this index
	 */
	private Collection getIndexColumns(String indkey) {

		LinkedList result = new LinkedList();

		TableMetaData tmd = getConnection().getTable(getTableId());
		if (tmd == null)// this should never happen
			return result;

		StringTokenizer st = new StringTokenizer(indkey);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int index = Integer.parseInt(token);
			if (index >= 1) {
				index--;
				ColumnMetaData cmd = tmd.getColumn(index);
				if (cmd != null) {
					result.add(cmd.getColumnName());
				}
			}
		}
		return result;
	}

	/**
	 * @return the name of the given index
	 */
	private String getIndexType(int relam) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getConnection().getMetaDataConnection();
			stmt = conn.createStatement();
			String sql = null;
			if (supportsSchemas()) {
				sql = "select amname from pg_catalog.pg_am where oid = " + String.valueOf(relam);
			} else {
				sql = "select amname from pg_am where oid = " + String.valueOf(relam);
			}

			ResultSet rset = stmt.executeQuery(sql);
			rset.next();
			return rset.getString("amname");
		} finally {
			try {
				if (stmt != null)
					stmt.close();

				if (conn != null)
					conn.commit();
			} catch (Exception e) {
				TSUtils.printException(e);
				getConnection().release(conn);
			}
		}
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/** "Name", "Columns", "Primary Key", "Unique", "Type", "Function" */
		TableIndex index = (TableIndex) getRow(row);
		if (column == NAME_COLUMN) {
			return index.getName();
		} else if (column == COLUMNS_COLUMN) {
			return index.getColumnsString();
		} else if (column == PRIMARY_KEY_COLUMN) {
			return Boolean.valueOf(index.isPrimaryKey());
		} else if (column == UNIQUE_COLUMN) {
			return Boolean.valueOf(index.isUnique());
		} else if (column == TYPE_COLUMN) {
			return index.getType();
		} else if (column == FUNCTION_COLUMN) {
			return index.getFunction();
		} else
			return "";
	}

	/**
	 * Drops and then re-creates an index in the database. Assumes that the
	 * index name has not changed.
	 */
	public void modifyIndex(TableIndex index) throws SQLException {
		String dropsql = IndexesModel.getDropSQL(index, false);
		String createsql = IndexesModel.getCreateSQL(index);

		LinkedList list = new LinkedList();
		list.add(dropsql);
		list.add(createsql);
		SQLCommand.runMetaDataCommand(getConnection(), I18N.format("Modifying_index_1", index.getName()), list);
	}

	/**
	 * Rebuilds the selected index
	 */
	public void rebuildIndex(TableIndex index) throws SQLException {
		Connection conn = getConnection().getMetaDataConnection();
		StringBuffer sqlbuff = new StringBuffer();
		sqlbuff.append("REINDEX INDEX ");
		sqlbuff.append(index.getSchemaQualifiedIndexName());
		SQLCommand
				.runMetaDataCommand(getConnection(), I18N.format("Rebuilding_1", index.getName()), sqlbuff.toString());
	}

	/**
	 * Rebuilds all indexes for the current table
	 */
	public void rebuildAllIndexes() throws SQLException {
		Connection conn = getConnection().getMetaDataConnection();
		StringBuffer sqlbuff = new StringBuffer();
		sqlbuff.append("REINDEX TABLE ");
		sqlbuff.append(getTableId().getFullyQualifiedName());
		SQLCommand.runMetaDataCommand(getConnection(),
				I18N.format("Rebuilding_all_indexes_for_1", getTableId().getTableName()), sqlbuff.toString());

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

			StringBuffer subsqlbuff = new StringBuffer();
			// with namespaces
			if (supportsSchemas()) {
				// select oid from pg_class where relname = 'foo' and
				// relkind='r' and relnamespace = (select oid from pg_namespace
				// where nspname='jeff')
				subsqlbuff.append("select oid from pg_class where relname = '");
				subsqlbuff.append(getTableId().getTableName());
				subsqlbuff.append("' and relkind='r' and relnamespace = (select oid from pg_namespace where nspname='");
				subsqlbuff.append(getTableId().getSchema().getName());
				subsqlbuff.append("')");
				sqlbuff.append("select * from pg_catalog.pg_index where indrelid=(");
			} else {
				// without namespaces
				// "select oid from pg_class where relname = 'foo' and relkind='r'";
				subsqlbuff.append("select oid from pg_class where relname = '");
				subsqlbuff.append(getTableId().getTableName());
				subsqlbuff.append("' and relkind='r'");
				sqlbuff.append("select * from pg_index where indrelid=(");
			}

			sqlbuff.append(subsqlbuff.toString());
			sqlbuff.append(")");

			Connection conn = getConnection().getMetaDataConnection();
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				ResultSet rset = stmt.executeQuery(sqlbuff.toString());
				while (rset.next()) {
					int indexrelid = rset.getInt("indexrelid");
					boolean isunique = rset.getBoolean("indisunique");
					boolean isprimary = rset.getBoolean("indisprimary");
					// procoid
					String procname = rset.getString("indproc");
					String indkey = rset.getString("indkey");

					TableIndex tindex = new TableIndex(getTableId());
					setIndexNameAndType(indexrelid, tindex);
					tindex.setPrimary(isprimary);
					tindex.setUnique(isunique);
					tindex.setIndexColumns(getIndexColumns(indkey));

					tindex.setFunction(procname);

					addRow(tindex);
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
	 * @return the name of the given index
	 */
	private void setIndexNameAndType(int indexrelid, TableIndex index) throws SQLException {
		Statement stmt = null;
		try {
			Connection conn = getConnection().getMetaDataConnection();
			stmt = conn.createStatement();
			String sql = null;
			if (supportsSchemas()) {
				sql = "select relam, relname from pg_catalog.pg_class where oid = " + String.valueOf(indexrelid);
			} else {
				sql = "select relam, relname from pg_class where oid = " + String.valueOf(indexrelid);
			}

			ResultSet rset = stmt.executeQuery(sql);
			rset.next();
			index.setName(rset.getString("relname"));
			index.setType(getIndexType(rset.getInt("relam")));
		} finally {
			if (stmt != null)
				stmt.close();
		}
	}

	/**
	 * Sets the current table id for the model. Reloads the indices.
	 */
	public void setTableId(TableId tableId) {
		super.setTableId(tableId);
		reload();
	}

}
