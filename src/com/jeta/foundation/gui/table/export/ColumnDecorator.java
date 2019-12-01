package com.jeta.foundation.gui.table.export;

import java.io.Writer;
import java.io.IOException;

import javax.swing.table.TableModel;

/**
 * This decorator simply writes out a column value from a table according to the
 * formatting options specified in the ColumnExportSetting
 * 
 * @author Jeff Tassin
 */
public class ColumnDecorator implements ExportDecorator {
	/** the model that defines/contains the export settings */
	private ExportModel m_model;
	/** the export setting for this column */
	private ColumnExportSetting m_colsetting;

	public ColumnDecorator(ColumnExportSetting setting, ExportModel model) {
		m_colsetting = setting;
		m_model = model;
	}

	/**
	 * ExportDecorator implementation.
	 */
	public void write(Writer writer, TableModel tableModel, int row, int col) throws IOException {
		if (m_colsetting.isIncluded()) {
			// we don't use the passed in column because the user may have
			// reordered the output
			int resultcol = m_colsetting.getColumnIndex(); // this the index
															// relative to the
															// query result
															// model
			Object value = tableModel.getValueAt(row, resultcol);
			if (value == null) {
				String nullsvalue = m_model.getNullsValue();
				if (nullsvalue != null)
					writer.write(nullsvalue);
			} else {
				writer.write(value.toString());
			}
		}

		// don't write the value delimiter if this is the last column
		if (m_model.isTransposed()) {
			if ((row + 1) < tableModel.getRowCount())
				writer.write(m_model.getValueDelimiter());
		}
	}

}
