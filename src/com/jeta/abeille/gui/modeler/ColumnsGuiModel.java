package com.jeta.abeille.gui.modeler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.common.MetaDataTableModel;
import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents the columns in a database table for the
 * EditTableDialog.
 * 
 * @author Jeff Tassin
 */
public class ColumnsGuiModel extends MetaDataTableModel {
	/** the database connection */
	private TSConnection m_connection;

	boolean m_primary;

	/** set to true if we are editing a prototype table */
	private boolean m_proto;

	// column indexes
	public static final int PRIMARYKEY_COLUMN = 0;
	public static final int NAME_COLUMN = 1;
	public static final int DATATYPE_COLUMN = 2;
	public static final int SIZE_COLUMN = 3;
	public static final int NULLABLE_COLUMN = 4;

	static ImageIcon m_pkimage;
	static ImageIcon m_checkimage;
	static ImageIcon m_notcheckimage;

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
	public ColumnsGuiModel(TSConnection connection, boolean bprimary, TableMetaData tmd, boolean bprototype) {
		super(connection, null);
		m_connection = connection;
		m_primary = bprimary;
		m_proto = bprototype;

		if (m_primary) {
			String[] values = new String[3];
			values[PRIMARYKEY_COLUMN] = "";
			values[NAME_COLUMN] = I18N.getLocalizedMessage("Column Name");
			values[DATATYPE_COLUMN] = I18N.getLocalizedMessage("Data Type");

			setColumnNames(values);

			Class[] types = new Class[3];
			types[PRIMARYKEY_COLUMN] = ColumnInfo.class;
			types[NAME_COLUMN] = String.class;
			types[DATATYPE_COLUMN] = JLabel.class;

			setColumnTypes(types);
		} else {

			String colname = I18N.getLocalizedMessage("Column Name");
			String data_type = I18N.getLocalizedMessage("Data Type");
			String ssize = I18N.getLocalizedMessage("Size");
			String sdecimal = I18N.getLocalizedMessage("Decimal");
			String nulls = I18N.getLocalizedMessage("Nulls");

			String[] values = new String[5];
			values[PRIMARYKEY_COLUMN] = "";
			values[NAME_COLUMN] = colname;
			values[DATATYPE_COLUMN] = data_type;
			values[SIZE_COLUMN] = ssize;
			values[NULLABLE_COLUMN] = nulls;

			setColumnNames(values);

			Class[] types = new Class[5];
			types[PRIMARYKEY_COLUMN] = ColumnInfo.class;
			types[NAME_COLUMN] = String.class;
			types[DATATYPE_COLUMN] = JLabel.class;
			types[SIZE_COLUMN] = String.class;
			types[NULLABLE_COLUMN] = Boolean.class;

			setColumnTypes(types);
		}

		loadTable(tmd, true);
	}

	/**
	 * @return true if this model contains the given column name
	 */
	public boolean contains(String colname) {
		assert (colname != null);
		Iterator iter = getData().iterator();
		while (iter.hasNext()) {
			ColumnInfo ci = (ColumnInfo) iter.next();
			if (colname.equals(ci.getColumnName()))
				return true;
		}
		return false;
	}

	/**
	 * @return a collection of ColumnInfo objects that make up this model
	 */
	public Collection getColumns() {
		return getData();
	}

	/**
	 * @return the table id we are currently displaying ONLY if this is an
	 *         existing and not a prototype table
	 */
	public TableId getTableId() {
		return super.getTableId();
	}

	/**
	 * @return true if the given cell is editable
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (isPrototype()) {
			if (m_primary)
				return false;
			else
				return true;
		} else {
			return false;
		}
	}

	/**
	 * @return true if the table is a prototype
	 */
	public boolean isPrototype() {
		return m_proto;
	}

	/**
	 * Populates this model with the given table definition
	 * 
	 * @param tmd
	 *            the table definition which to populate this model with
	 * @param bprimaryKeys
	 *            set to true if you wish to include the primary key definitions
	 *            in this model
	 */
	public void loadTable(TableMetaData tmd, boolean bprimaryKeys) {
		removeAll();

		if (tmd != null) {
			// now load the table meta data
			Iterator iter = tmd.getColumns().iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				ColumnInfo info = new ColumnInfo(cmd);
				if (bprimaryKeys && tmd.isPrimaryKey(cmd.getFieldName()))
					info.setPrimaryKey(true);

				addRow(info);
			}
			super.setTableId(tmd.getTableId());
			fireTableDataChanged();
		} else {
			super.setTableId(null);
			fireTableDataChanged();
		}
	}

	public Object getValueAt(int row, int column) {
		// "Primary Key", "Column Name", "Data Type", "Size", "Nullable"
		ColumnInfo ci = (ColumnInfo) getRow(row);
		if (column == PRIMARYKEY_COLUMN) // primary key
		{
			return ci;
		} else if (column == NAME_COLUMN) // columnname
		{
			return ci.getColumnName();
		} else if (column == DATATYPE_COLUMN) // data type
		{
			return ci.getDataType(m_connection);
		} else if (column == SIZE_COLUMN) {
			return ci.getColumnSizeString();
		} else if (column == NULLABLE_COLUMN) {
			return Boolean.valueOf(ci.isNullable());
		} else
			return "";

	}

	/**
	 * Sets an existing table for this model
	 */
	public void setTableId(TableId tableid) {
		super.setTableId(tableid);

		TableMetaData tmd = null;
		if (tableid != null) {
			tmd = getConnection().getModel(tableid.getCatalog()).getTableEx(tableid,
					TableMetaData.LOAD_FOREIGN_KEYS | TableMetaData.LOAD_COLUMNS_EX);
		}
		setTableMetaData(tmd);
	}

	/**
	 * Sets the table metadata for this model
	 */
	public void setTableMetaData(TableMetaData tmd) {
		loadTable(tmd, true);
	}

	/**
	 * Sets the value at the given cell
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		ColumnInfo ci = (ColumnInfo) getRow(rowIndex);
		if (ci == null)
			return;

		if (columnIndex == PRIMARYKEY_COLUMN) {

		} else if (columnIndex == NAME_COLUMN) {
			ci.setColumnName((String) aValue);
		} else if (columnIndex == DATATYPE_COLUMN) {
			ci.setTypeName((String) aValue);
			/**
			 * you must do this because if there is an existing value for the
			 * data type, the renderer will render that type in the GUI
			 */
			ci.setType(0);
		} else if (columnIndex == SIZE_COLUMN) {
			String sizeval = TSUtils.fastTrim(TSUtils.strip((String) aValue, "()"));
			int pos = sizeval.indexOf(",");
			String ps = sizeval;
			String ss = "";
			if (pos >= 0) {
				if (pos > 0) {
					ps = sizeval.substring(0, pos);
				}
				ss = sizeval.substring(pos + 1, sizeval.length());
			}

			try {
				int precision = TSUtils.getInt(ps);
				ci.setColumnSize(precision);

				int scale = TSUtils.getInt(ss);
				ci.setScale(scale);
			} catch (Exception e) {
				// eat it here
				TSUtils.printException(e);
			}
		} else if (columnIndex == NULLABLE_COLUMN) {
			boolean nullable = ((Boolean) aValue).booleanValue();

			if (ci.isPrimaryKey()) {
				ci.setNullable(false);
			} else {
				ci.setNullable(nullable);
			}
		}
	}
}
