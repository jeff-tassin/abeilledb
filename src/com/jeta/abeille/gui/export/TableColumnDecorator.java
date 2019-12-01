package com.jeta.abeille.gui.export;

import java.io.Writer;
import java.io.IOException;

import java.sql.SQLException;

import javax.swing.table.TableModel;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.foundation.gui.table.export.ColumnExportSetting;
import com.jeta.foundation.gui.table.export.ExportDecorator;
import com.jeta.foundation.gui.table.export.ExportUtils;

/**
 * This decorator simply writes out a column value from a result set according
 * to the formatting options specified in the ColumnExportSetting
 * 
 * @author Jeff Tassin
 */
public class TableColumnDecorator implements ExportDecorator {
	/** the format settings for each column */
	private TableColumnExportSetting m_colsetting;

	/** the model that contains most of the export settings */
	private SQLExportModel m_exportmodel;

	/** this is used to convert sql data values to strings */
	private SQLFormatter m_sqlformatter;

	/** the literal we prepend to each column value */
	private String m_preValue;
	/** the literal we append to each column value */
	private String m_postValue;

	/**
	 * ctor
	 */
	public TableColumnDecorator(TableColumnExportSetting setting, SQLExportModel exportModel, SQLFormatter formatter) {
		m_colsetting = setting;
		m_exportmodel = exportModel;
		m_sqlformatter = formatter;

		String preval = ExportUtils.parseLeft(ExportNames.VALUE_EXPRESSION, setting.getOutputExpression());
		if (preval.length() > 0)
			m_preValue = preval;

		String postval = ExportUtils.parseRight(ExportNames.VALUE_EXPRESSION, setting.getOutputExpression());
		if (postval.length() > 0)
			m_postValue = postval;
	}

	/**
	 * @return the delimiter used to 'quote' text values
	 */
	private char getTextDelimiter() {
		return m_exportmodel.getTextDelimiter();
	}

	/**
	 * ExportDecorator implementation.
	 */
	public void write(Writer writer, TableModel tableModel, int row, int col) throws IOException {
		try {
			if (m_colsetting.isIncluded()) {
				if (m_preValue != null)
					writer.write(m_preValue);

				// we don't use the passed in column because the user may have
				// reordered the output
				int resultcol = m_colsetting.getColumnIndex(); // this the index
																// relative to
																// the query
																// result model
				Object value = tableModel.getValueAt(row, resultcol);
				if (value == null) {
					String nullsvalue = m_exportmodel.getNullsValue();
					if (nullsvalue != null)
						writer.write(nullsvalue);
				} else {
					if (m_colsetting.isSQLFormat()) {
						int datatype = m_colsetting.getColumnType();
						writer.write(m_sqlformatter.format(value, datatype));
					} else {
						int datatype = m_colsetting.getColumnType();
						if (m_colsetting.isExportAsText()) {

							char c = getTextDelimiter();
							if (c == '\0') {
								writer.write(value.toString());
							} else {
								if (m_exportmodel.isSQLTextDelimit()) {
									writer.write(DbUtils.toSQL(value.toString(), getTextDelimiter()));
								} else {
									writer.write(getTextDelimiter());
									writer.write(value.toString());
									writer.write(getTextDelimiter());
								}
							}
						} else
							writer.write(value.toString());
					}
				}

				if (m_postValue != null)
					writer.write(m_postValue);
			}

		} catch (SQLException e) {
			// this is probably not the best way to handle this
			IOException ioe = new IOException(e.getLocalizedMessage());
			throw ioe;
		}
	}

}
