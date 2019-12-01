package com.jeta.foundation.gui.table.export;

import java.io.Writer;
import java.io.IOException;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.table.TableModel;

/**
 * This class is used to iterate over a table model and forward each row to the
 * row decorators. This class will read all results from the model until either
 * the model has no more data or isCanceled call returns true.
 * 
 * @author Jeff Tassin
 */
public class ResultsIterator extends DecoratorComposite {
	/**
	 * this is the maximum number of rows that this decorator should output. set
	 * to -1 to output all rows
	 */
	private int m_maxrows;

	/** set to true to cancel the iteration */
	private boolean m_canceled = false;

	/**
	 * ctor
	 */
	public ResultsIterator() {
		m_maxrows = -1;
	}

	/**
	 * ctor
	 */
	public ResultsIterator(int maxRows) {
		m_maxrows = maxRows;
	}

	/**
	 * @return true only if the user explicitly canceled the operation
	 */
	public boolean isCanceled() {
		return m_canceled;
	}

	/**
	 * Override if you want to provide special cancel handling
	 */
	protected boolean continueExport(Writer writer, TableModel model, int row, int col) {
		if (isCanceled())
			return false;

		if (m_maxrows > 0 && row >= m_maxrows)
			return false;
		else
			return true;
	}

	/**
	 * Sets the canceled flag. The iteration loop should detect this and exit
	 */
	public synchronized void setCanceled(boolean bcancel) {
		m_canceled = bcancel;
	}

	/**
	 * ExportDecorator implementation. Outputs a row of query results.
	 */
	public void write(Writer writer, TableModel tableModel, int row, int col) throws IOException {
		// row must always be zero
		assert (row == 0);
		while (continueExport(writer, tableModel, row, col) && (row < tableModel.getRowCount())) {
			if (row > 0)
				writer.write("\n");

			Collection c = getDecorators();
			Iterator iter = c.iterator();
			while (iter.hasNext()) {
				ExportDecorator dec = (ExportDecorator) iter.next();
				dec.write(writer, tableModel, row, col);
				wroteRow(row);
			}
			row++;
		}
	}

	/**
	 * Specialized classes can override this method to update any status
	 * components
	 */
	protected void wroteRow(int row) {
		// no op
	}
}
