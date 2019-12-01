package com.jeta.abeille.gui.modeler;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * The data model for foreign keys on a table
 * 
 * @author Jeff Tassin
 */
public class ForeignKeysModel extends AbstractTableModel {
	/** an array of User objects */
	private ArrayList m_data;

	/** an array of column names for the table */
	private String[] m_colnames;

	/** an array of column types for the table */
	private Class[] m_coltypes;

	/** The database connection */
	private TSConnection m_connection;

	/** set to true if we are editing a prototype table */
	private boolean m_proto;

	/**
	 * this value is set only if we are viewing an existing table using the
	 * TableView
	 */
	private TableId m_tableid;

	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int REF_TABLE_COLUMN = 1;
	static final int COLUMNS_COLUMN = 2;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public ForeignKeysModel(TSConnection connection) {
		super();

		m_connection = connection;
		m_data = new ArrayList();

		String[] values = { I18N.getLocalizedMessage("Key Name"), I18N.getLocalizedMessage("Reference Table"),
				I18N.getLocalizedMessage("Assigned Columns") };

		m_colnames = values;
		Class[] types = { String.class, TableId.class, String.class };
		m_coltypes = types;
	}

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public ForeignKeysModel(TSConnection connection, boolean bproto) {
		this(connection);
		m_proto = bproto;
	}

	/**
	 * Adds a foreign key to the model
	 */
	public void addKey(DbForeignKey fkey) {
		ForeignKeyWrapper wrapper = new ForeignKeyWrapper(fkey);
		m_data.add(wrapper);
		fireTableRowsInserted(m_data.size() - 1, m_data.size() - 1);
	}

	/**
	 * Deletes a foreign key from the model
	 */
	public void deleteKey(ForeignKeyWrapper fkey) {
		int row = m_data.indexOf(fkey);
		if (row >= 0) {
			m_data.remove(row);
			fireTableRowsDeleted(row, row);
		}
	}

	/**
	 * @return the number of columns in this model
	 */
	public int getColumnCount() {
		return m_colnames.length;
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return a collection of DbForeignKey objects
	 */
	public Collection getForeignKeys() {
		java.util.LinkedList list = new java.util.LinkedList();
		for (int row = 0; row < m_data.size(); row++) {
			ForeignKeyWrapper wrapper = (ForeignKeyWrapper) m_data.get(row);
			list.add(wrapper.getForeignKey());
		}
		return list;
	}

	/**
	 * @return the number of rows objects in this model
	 */
	public int getRowCount() {
		return m_data.size();
	}

	/**
	 * @return the name of a column at a given index
	 */
	public String getColumnName(int column) {
		return m_colnames[column];
	}

	/**
	 * @return the type of column at a given index
	 */
	public Class getColumnClass(int column) {
		return m_coltypes[column];
	}

	/**
	 * @return the table id we are currently displaying ONLY if this is an
	 *         existing and not a prototype table
	 */
	public TableId getTableId() {
		assert (!isPrototype());
		return m_tableid;
	}

	/**
	 * @return the foreign key wrapper at the given row
	 */
	public ForeignKeyWrapper getRow(int row) {
		if (row >= 0 && row < m_data.size()) {
			return (ForeignKeyWrapper) m_data.get(row);
		} else
			return null;
	}

	/**
	 * @return the object at the given column
	 */
	public Object getValueAt(int row, int column) {
		ForeignKeyWrapper wrapper = (ForeignKeyWrapper) m_data.get(row);
		if (column == NAME_COLUMN) {
			return wrapper.getName();
		} else if (column == REF_TABLE_COLUMN) {
			return wrapper.getReferenceTableId();
		} else if (column == COLUMNS_COLUMN) {
			return wrapper.getAssignedColumns();
		} else
			return "";
	}

	/**
	 * @return true if the table is a prototype
	 */
	public boolean isPrototype() {
		return m_proto;
	}

	/**
	 * Loads the foreign keys from the given table into the model
	 * 
	 * @param tmd
	 *            the table whose foreign keys we wish to load
	 */
	public void loadForeignKeys(TableMetaData tmd) {
		removeAll();

		// now load up the model with the foreign keys of the table (if the
		// table is an existing table )
		if (tmd != null) {
			Iterator iter = tmd.getForeignKeys().iterator();
			while (iter.hasNext()) {
				DbForeignKey dbfkey = (DbForeignKey) iter.next();
				addKey(dbfkey);
			}

			m_tableid = tmd.getTableId();
		} else {
			m_tableid = null;
		}

		fireTableDataChanged();
	}

	/**
	 * Modifies an existing foreign key in the model
	 */
	public void modifyKey(DbForeignKey newKey, DbForeignKey oldKey) {
		for (int index = 0; index < m_data.size(); index++) {
			ForeignKeyWrapper wrapper = (ForeignKeyWrapper) m_data.get(index);
			if (wrapper != null && wrapper.getForeignKey() == oldKey) {
				wrapper.setForeignKey(newKey);
				fireTableRowsUpdated(index, index);
				break;
			}
		}
	}

	/**
	 * Remove all data items from this model
	 */
	public void removeAll() {
		m_data.clear();
	}

	public void setTable(TableMetaData tmd) {
		loadForeignKeys(tmd);
	}

	/**
	 * Loads the foreign keys for the existing table in the current connection
	 */
	public void setTableId(TableId tableId) {
		setTable(m_connection.getTable(tableId));
	}

}
