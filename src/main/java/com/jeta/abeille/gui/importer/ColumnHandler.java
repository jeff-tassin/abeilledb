package com.jeta.abeille.gui.importer;

/**
 * A simple interface for handling imports
 * 
 * @author Jeff Tassin
 */
public interface ColumnHandler {

	/**
	 * @return the output for a given row
	 */
	public String getOutput(int row);
}
