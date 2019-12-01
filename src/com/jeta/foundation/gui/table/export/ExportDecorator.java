package com.jeta.foundation.gui.table.export;

import java.io.Writer;
import java.io.IOException;

import javax.swing.table.TableModel;

/**
 * This interface is used for decorator classes that wish to decorate output
 * from a query results export
 * 
 * @author Jeff Tassin
 */
public interface ExportDecorator {

	/**
	 * Outputs a row of query results.
	 */
	public void write(Writer writer, TableModel tableModel, int row, int col) throws IOException;

}
