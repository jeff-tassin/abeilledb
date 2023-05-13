package com.jeta.abeille.gui.export;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.queryresults.QueryResultsModel;

import com.jeta.foundation.gui.table.TableSelection;
import com.jeta.foundation.gui.table.export.ColumnExportModel;
import com.jeta.foundation.gui.table.export.ColumnExportSetting;

import com.jeta.foundation.i18n.I18N;

/**
 * Represents the data that we wish to export to a file. This data is always the
 * result of a query.
 * 
 * @author Jeff Tassin
 */
public class ColumnOptionsModel extends AbstractTableModel {
	private ColumnExportModel m_data;
	private String[] m_colnames; // an array of column names for this table
	private Class[] m_coltypes; // this is the types of columns for this table

	public static final int TABLENAME_COLUMN = 0;
	public static final int COLUMNNAME_COLUMN = 1;
	public static final int COLUMNTYPE_COLUMN = 2;
	public static final int TEXT_COLUMN = 3;
	public static final int INCLUDE_COLUMN = 4;
	public static final int OUTPUT_COLUMN = 5;

	/**
	 * ctor
	 * 
	 * @param model
	 *            the model that contains the data we will export
	 */
	public ColumnOptionsModel(QueryResultsModel model) {
		this(model, null);
	}

	/**
	 * ctor
	 * 
	 * @param model
	 *            the model that contains the data we will export
	 */
	public ColumnOptionsModel(QueryResultsModel model, TableSelection selection) {
		m_data = new ColumnExportModel();

		String[] cols = { I18N.getLocalizedMessage("Table"), I18N.getLocalizedMessage("Column"),
				I18N.getLocalizedMessage("Data Type"), I18N.getLocalizedMessage("Text"),
				I18N.getLocalizedMessage("Include"), I18N.getLocalizedMessage("Output") };
		m_colnames = cols;

		Class[] types = { String.class, String.class, String.class, Boolean.class, Boolean.class, String.class };
		m_coltypes = types;

		loadData(model, selection);
	}

	/**
	 * Creates a default table column export setting based in the metadata and
	 * column (1 based)
	 * 
	 * @param metadata
	 *            teh result set metadata that we get the column settings from
	 * @param column
	 *            the 1-based column index
	 * @return a new TableColumnExportSetting object
	 */
	private TableColumnExportSetting createSetting(TSConnection conn, ResultSetMetaData metadata, int column)
			throws SQLException {
		String catname = metadata.getCatalogName(column);
		String schemaname = metadata.getSchemaName(column);
		String tablename = metadata.getTableName(column);
		String columnname = metadata.getColumnName(column);

		Catalog catalog = Catalog.createInstance(catname);
		DbModel dbmodel = conn.getModel(catalog);
		Schema schema = dbmodel.getSchemaInstance(schemaname);
		TableId tableid = new TableId(catalog, schema, tablename);
		TableColumnExportSetting setting = new TableColumnExportSetting(tableid, columnname, column - 1, true, false,
				ExportNames.VALUE_EXPRESSION);

		int coltype = metadata.getColumnType(column);
		setting.setColumnType(coltype);

		setting.setExportAsText(DbUtils.isAlpha(coltype));

		return setting;
	}

	/**
	 * @return the number of columns in the column options table
	 */
	public int getColumnCount() {
		return m_colnames.length;
	}

	/**
	 * @return the underlying data model
	 */
	public ColumnExportModel getColumnExportModel() {
		return m_data;
	}

	/**
	 * @return the number of rows in the column options table
	 */
	public int getRowCount() {
		return m_data.getColumnCount();
	}

	/**
	 * @return the name of the column in the options table at the given index
	 */
	public String getColumnName(int column) {
		return m_colnames[column];
	}

	/**
	 * @return the type of object for the given column
	 */
	public Class getColumnClass(int column) {
		return m_coltypes[column];
	}

	/**
	 * @return the column setting object for the given row in the table (Note:
	 *         In this case row corresponds to column index in the metadata)
	 */
	public TableColumnExportSetting getRow(int row) {
		return (TableColumnExportSetting) m_data.getColumnExportSetting(row);
	}

	/**
	 * @return the value at the given row and column.
	 */
	public Object getValueAt(int row, int column) {
		// "Table", "Column", "Include", "SQL", "Output"
		TableColumnExportSetting ces = getRow(row);
		if (column == TABLENAME_COLUMN) // table name
			return ces.getTableName();
		else if (column == COLUMNNAME_COLUMN) // columnname
			return ces.getColumnName();
		else if (column == COLUMNTYPE_COLUMN) // datatype
			return DbUtils.getJDBCTypeName((short) ces.getColumnType());
		else if (column == TEXT_COLUMN) // include
			return (ces.isExportAsText()) ? Boolean.TRUE : Boolean.FALSE;
		else if (column == INCLUDE_COLUMN) // include
			return (ces.isIncluded()) ? Boolean.TRUE : Boolean.FALSE;
		else if (column == OUTPUT_COLUMN) // output
			return ces.getOutputExpression();
		else
			return "";
	}

	/**
	 * @return true if a given table cell is editable or not
	 */
	public boolean isCellEditable(int row, int column) {
		if (column == OUTPUT_COLUMN || column == INCLUDE_COLUMN || column == TEXT_COLUMN)
			return true;
		else
			return false;
	}

	/**
	 * Loads the data from the query results model
	 */
	void loadData(QueryResultsModel model, TableSelection selection) {
		try {
			ResultSetReference resultref = model.getResultSetReference();
			ResultSetMetaData metadata = resultref.getMetaData();
			// DbModel dbmodel = model.getTSConnection().getModel();

			if (selection == null) {
				int count = metadata.getColumnCount();
				for (int column = 1; column <= count; column++) {
					m_data.addSetting(createSetting(model.getTSConnection(), metadata, column));
				}
			} else {
				int[] cols = selection.getColumnSpan();
				for (int index = 0; index < cols.length; index++) {
					int column = cols[index] + 1;
					TableColumnExportSetting setting = createSetting(model.getTSConnection(), metadata, column);
					m_data.addSetting(setting);
					// now, let's change the column index to that of the
					// TableSelection. The TableSelection
					// is responsible for translating to the correct resultset
					// column
					setting.setColumnIndex(index);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Moves the object found at the old index to the new index
	 */
	public void reorder(int newIndex, int oldIndex) {
		m_data.reorder(newIndex, oldIndex);
		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * Sets the value at the given row and column. This model does not support
	 * in place editing at this time.
	 */
	public void setValueAt(Object aValue, int row, int column) {
		// only needed if we enable editing in the table

		TableColumnExportSetting setting = getRow(row);
		if (column == TEXT_COLUMN) {
			Boolean bval = (Boolean) aValue;
			setting.setExportAsText(bval.booleanValue());
		} else if (column == INCLUDE_COLUMN) {
			Boolean bval = (Boolean) aValue;
			setting.setIncluded(bval.booleanValue());
		} else if (column == OUTPUT_COLUMN) {
			setting.setOutputExpression((String) aValue);
		}
	}

}
