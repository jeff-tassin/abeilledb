package com.jeta.foundation.gui.table.export;

import java.io.Writer;
import java.io.IOException;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.table.TableModel;

/**
 * This class is used to iterate over a table model and forward each column/row
 * to the row decorators. This class will read all results from the model until
 * either the model has no more data or isCanceled call returns true.
 * 
 * @author Jeff Tassin
 */
public class TransposeIterator extends DecoratorComposite {
	/**
	 * this is the maximum number of rows that this decorator should output. set
	 * to -1 to output all rows
	 */
	private int m_maxrows;

	/** sets the column decorator for the output */
	private ExportDecorator m_columndec;

	/** the export model */
	private ExportModel m_model;

	private ExportDecorator[] m_valdecs;

	private LiteralDecorator m_preline;
	private LiteralDecorator m_postline;

	/**
	 * Ctor
	 */
	public TransposeIterator(int maxRows, ExportDecorator columnDec, ExportDecorator[] coldecs, ExportModel model) {
		m_maxrows = maxRows;
		m_columndec = columnDec;
		m_model = model;
		m_valdecs = coldecs;

		m_preline = new LiteralDecorator(ExportUtils.parseLeft(ExportNames.LINE_EXPRESSION, model.getLineDecorator()));
		m_postline = new LiteralDecorator(ExportUtils.parseRight(ExportNames.LINE_EXPRESSION, model.getLineDecorator()));
	}

	/**
	 * Override if you want to provide special cancel handling
	 */
	protected boolean isCanceled(Writer writer, TableModel model, int row, int col) {
		if (m_maxrows > 0 && row >= m_maxrows)
			return true;
		else
			return false;
	}

	/**
	 * ExportDecorator implementation Outputs a row of query results.
	 */
	public void write(Writer writer, TableModel tableModel, int row, int col) throws IOException {
		// row must always be zero
		assert (row == 0);
		for (col = 0; col < tableModel.getColumnCount(); col++) {
			row = 0;
			ExportDecorator valdec = m_valdecs[col];
			ColumnExportSetting setting = m_model.getColumnExportSetting(col);
			if (setting.isIncluded()) {
				// line pre-decorator
				m_preline.write(writer, tableModel, row, col);

				// write out column decorators
				if (m_model.isShowColumnNames())
					m_columndec.write(writer, tableModel, 0, col);

				// now write out all rows for the given column
				while (!isCanceled(writer, tableModel, row, col) && (row < tableModel.getRowCount())) {
					valdec.write(writer, tableModel, row, col);
					row++;
				}
				// line post-decorator
				m_postline.write(writer, tableModel, row, col);
				writer.write(ExportNames.NEWLINE);
			}// setting.included
		} // for(...)
	}

}
