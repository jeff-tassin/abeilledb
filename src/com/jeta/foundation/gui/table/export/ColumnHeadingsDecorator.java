package com.jeta.foundation.gui.table.export;

import java.io.Writer;
import java.io.IOException;

import javax.swing.table.TableModel;

/**
 * This class is used to format column headings from a table
 * 
 * @author Jeff Tassin
 */
public class ColumnHeadingsDecorator implements ExportDecorator {
	/** the model that controls the export */
	private ExportModel m_model;

	/** the index of the last column that is included in the export */
	private int m_lastcolumn;

	/**
	 * ctor
	 */
	public ColumnHeadingsDecorator(ExportModel model) {
		m_model = model;
		m_lastcolumn = 0;
		for (int index = 0; index < m_model.getColumnCount(); index++) {
			ColumnExportSetting setting = m_model.getColumnExportSetting(index);
			if (setting.isIncluded())
				m_lastcolumn = index;
		}
	}

	/**
	 * ExportDecorator implementation Outputs the column header
	 */
	public void write(Writer writer, TableModel model, int row, int col) throws IOException {
		if (m_model.isTransposed()) {
			ColumnExportSetting setting = m_model.getColumnExportSetting(col);
			if (setting.isIncluded()) {
				writer.write(setting.getColumnName());
				writer.write(m_model.getColumnNameDelimiter());
			}
		} else {
			for (int colindex = 0; colindex < m_model.getColumnCount(); colindex++) {
				ColumnExportSetting setting = m_model.getColumnExportSetting(colindex);
				if (setting.isIncluded()) {
					writer.write(setting.getColumnName());
					if (colindex < m_lastcolumn)
						writer.write(m_model.getColumnNameDelimiter());
				}
			}
		}
	}

}
