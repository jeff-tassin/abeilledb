package com.jeta.foundation.gui.table.export;

import java.io.Writer;
import java.io.IOException;

import javax.swing.table.TableModel;

/**
 * This class is used to format table data for the a row of query data. The
 * caller to configure formatting options such as text delimiters, value
 * delimiters, column headers, etc.
 * 
 * @author Jeff Tassin
 */
public class RowDecorator implements ExportDecorator {
	/** the model that contains most of the export settings */
	private ExportModel m_exportmodel;

	/**
	 * the array of column settings that control which columns are exported and
	 * how
	 */
	private ExportDecorator[] m_columndecorators;

	/**
	 * ctor Sets up the decorators for each row of results
	 * 
	 * @param preLine
	 *            the decorator used to prepend every row of results
	 * @param postLine
	 *            the decorator used to append every row of results
	 * @param columns
	 *            the decorators that govern every column of results
	 */
	public RowDecorator(ExportModel model, ExportDecorator[] columns) {
		m_exportmodel = model;
		m_columndecorators = columns;
	}

	/**
	 * @return the value delimiter for each value
	 */
	public String getValueDelimiter() {
		return m_exportmodel.getValueDelimiter();
	}

	/**
	 * ExportDecorator implementation Outputs a row of query results.
	 */
	public void write(Writer writer, TableModel tableModel, int row, int col) throws IOException {
		outputColumns(writer, tableModel, row);
	}

	/**
	 * Iterate over the decorators for each column in the result set and allow
	 * each one to output each column
	 */
	private void outputColumns(Writer writer, TableModel tableModel, int row) throws IOException {
		for (int col = 0; col < m_columndecorators.length; col++) {
			ExportDecorator dec = m_columndecorators[col];
			dec.write(writer, tableModel, row, col);
			// don't write the value delimiter if this is the last column
			if ((col + 1) < m_columndecorators.length)
				writer.write(getValueDelimiter());
		}
	}

}
