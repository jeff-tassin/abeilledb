package com.jeta.abeille.gui.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents the columns in a database table for the
 * EditTableDialog.
 * 
 * @author Jeff Tassin
 */
class ImportModel extends AbstractTableModel {
	ArrayList m_columns = new ArrayList(); // an array of ColumnInfo objects
	String[] m_colNames; // an array of column names of the GUI table (not the
							// database table)
	Class[] m_colTypes; // this is the types of columns of the GUI table (not
						// the database table)
	boolean m_primary;

	/** a map of column names (String key) to string values */
	private HashMap m_values = new HashMap();

	private TSConnection m_connection;
	private TableId m_tableid;

	private HashMap m_handlers = new HashMap();

	// column indexes
	public static final int PRIMARYKEY_COLUMN = 0;
	public static final int NAME_COLUMN = 1;
	public static final int DATATYPE_COLUMN = 2;
	public static final int VALUE_COLUMN = 3;

	static ImageIcon m_pkimage;

	static {
		m_pkimage = TSGuiToolbox.loadImage("primarykey16.gif");
	}

	/**
	 * Constructor for ColumnsGuiModel. This model is used for both the main
	 * column view for a TABLE and the primary key columns view.
	 * 
	 * @param bprimary
	 *            set to true if for the primary key columns view
	 * @param pkImage
	 *            the image icon used for primary keys in the view
	 * @param tmd
	 *            the TableMetaData if this model is used to show the main TABLE
	 *            columns.
	 * 
	 */
	public ImportModel(TSConnection connection, TableId tableid) {
		super();

		m_connection = connection;
		m_tableid = tableid;

		String colname = I18N.getLocalizedMessage("Column Name");
		String data_type = I18N.getLocalizedMessage("Data Type");
		String value = I18N.getLocalizedMessage("Value");

		String[] values = new String[4];
		values[PRIMARYKEY_COLUMN] = "";
		values[NAME_COLUMN] = colname;
		values[DATATYPE_COLUMN] = data_type;
		values[VALUE_COLUMN] = value;
		m_colNames = values;

		Class[] types = new Class[4];
		types[PRIMARYKEY_COLUMN] = ImageIcon.class;
		types[NAME_COLUMN] = String.class;
		types[DATATYPE_COLUMN] = JLabel.class;
		types[VALUE_COLUMN] = String.class;

		m_colTypes = types;

		loadTable();
	}

	/**
	 * Adds the given field info object to the table
	 */
	public void addRow(ColumnInfo fi) {
		m_columns.add(fi);
	}

	public int getColumnCount() {
		return m_colNames.length;
	}

	public int getRowCount() {
		return m_columns.size();
	}

	public String getColumnName(int column) {
		return m_colNames[column];
	}

	public Class getColumnClass(int column) {
		return m_colTypes[column];
	}

	/**
	 * @return the column handler for the given row
	 */
	public ColumnHandler getColumnHandler(int row) {
		String colname = (String) getValueAt(row, NAME_COLUMN);
		ColumnHandler handler = (ColumnHandler) m_handlers.get(colname);
		if (handler == null) {
			String val = (String) getValueAt(row, VALUE_COLUMN);
			System.out.println("getColumnHandler  row = " + row + "  val = " + val);
			int pos = val.indexOf("@COUNT");
			if (pos >= 0)
				handler = new CountingColumnHandler(val);
			else
				handler = new DefaultColumnHandler(val);

			m_handlers.put(colname, handler);
		}
		return handler;
	}

	/*
	 * @return a collection (currently an ArrayList) of ColumnInfo objects.
	 * 
	 * @deprecated
	 */
	public Collection getColumns() {
		return m_columns;
	}

	TSConnection getConnection() {
		return m_connection;
	}

	public ColumnInfo getRow(int row) {
		return (ColumnInfo) m_columns.get(row);
	}

	public TableId getTableId() {
		return m_tableid;
	}

	public Object getValueAt(int row, int column) {
		// "Primary Key", "Column Name", "Data Type", "Value"
		ColumnInfo ci = (ColumnInfo) m_columns.get(row);
		if (column == PRIMARYKEY_COLUMN) // primary key
		{
			if (ci.isPrimaryKey())
				return m_pkimage;
			else
				return null;
		} else if (column == NAME_COLUMN) // columnname
			return ci.getColumnName();
		else if (column == DATATYPE_COLUMN) // data type
		{
			assert (false);
			// return ci.getDataType();
			return null;
		} else if (column == VALUE_COLUMN) {
			String val = (String) m_values.get(ci.getColumnName());
			if (val == null)
				return "null";
			else
				return val;
		} else
			return "";

	}

	public boolean isCellEditable(int row, int column) {
		return (column == VALUE_COLUMN);
	}

	/**
	 * Populates this model with the given table definition
	 * 
	 * @param tmd
	 *            the table definition which to populate this model with
	 * @param bprimaryKeys
	 *            set to true if you wish to include the primary key definitions
	 *            in this model
	 * @param bforeignKeys
	 *            set to true if you wish to include the foreign key definitions
	 *            in this model
	 */
	public void loadTable() {

		TableMetaData tmd = m_connection.getTable(m_tableid);
		// now load the table meta data
		Iterator iter = tmd.getColumns().iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			ColumnInfo info = new ColumnInfo(cmd);
			if (tmd.isPrimaryKey(cmd.getFieldName()))
				info.setPrimaryKey(true);

			addRow(info);
		}

		// loadSettings();
	}

	public void insertRow(ColumnInfo info, int row) {
		if (row < 0)
			row = 0;

		if (row >= m_columns.size())
			m_columns.add(info);
		else
			m_columns.add(row, info);
	}

	/**
    *
    *
    */
	public void saveSettings() {

	}

	public void setValueAt(Object aValue, int row, int column) {
		// only needed if we enable editing in the table
		ColumnInfo ci = (ColumnInfo) m_columns.get(row);
		m_values.put(ci.getColumnName(), (String) aValue);
		m_handlers.clear();
	}
}
