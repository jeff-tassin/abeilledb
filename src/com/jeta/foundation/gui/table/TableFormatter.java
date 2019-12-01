/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

/**
 * This class is used to format table data for the clipboard. The caller to
 * configure formatting options such as text delimiters, value delimiters,
 * column headers, etc.
 * 
 * @author Jeff Tassin
 */
public class TableFormatter {
	/**
	 * Flag that determines if rows and columns should be transposed
	 */
	private boolean m_transpose;

	/**
	 * String that is placed between values
	 */
	private String m_valuedelimiter;

	/**
	 * Flag that determines if column names are displayed
	 */
	private boolean m_showcolumnnames;

	/**
	 * String that is placed between column names
	 */
	private String m_columnnamedelimiter;

	private static final String NEWLINE = "\n";

	/**
	 * Formats a matrix of data according to the properties set for this class.
	 * 
	 * @param data
	 *            the table data to format
	 * @param columnnames
	 *            the array of column names that corresponds to the data
	 * @return the formatted result
	 */
	String formatData(Object[][] data, String[] columnnames) {
		if (isTranspose()) {
			return formatTransposed(data, columnnames);
		} else {
			return formatStandard(data, columnnames);
		}
	}

	/**
	 * Formats a matrix of data in the same layout as the data is passed in.
	 * That is, the output will have the same rows/columns layout as the data
	 * (as opposed to the formatTransposed method)
	 * 
	 * @param data
	 *            the table data to format
	 * @param columnnames
	 *            the array of column names that corresponds to the data
	 */
	String formatStandard(Object[][] data, String[] columnnames) {
		StringBuffer result = new StringBuffer();
		if (isShowColumnNames()) {
			StringBuffer line = new StringBuffer();
			for (int col = 0; col < columnnames.length; col++) {
				line.append(columnnames[col]);
				if ((col + 1) < columnnames.length)
					line.append(getColumnNameDelimiter());
			}
			line.append(NEWLINE);
			result.append(line);
		}

		for (int row = 0; row < data.length; row++) {
			StringBuffer line = new StringBuffer();
			for (int col = 0; col < data[0].length; col++) {
				Object obj = data[row][col];
				if (obj != null)
					line.append(obj.toString());

				if ((col + 1) < data[0].length)
					line.append(getValueDelimiter());
			}
			line.append(NEWLINE);
			result.append(line);
		}
		return result.toString();
	}

	/**
	 * Formats a matrix of data that is transposed to the layout of the data
	 * that is passed in. That is, the output will have the rows/columns
	 * swapped.
	 * 
	 * @param data
	 *            the table data to format
	 * @param columnnames
	 *            the array of column names that corresponds to the data
	 */
	String formatTransposed(Object[][] data, String[] columnnames) {
		StringBuffer result = new StringBuffer();
		for (int col = 0; col < data[0].length; col++) {
			StringBuffer line = new StringBuffer();
			if (isShowColumnNames()) {
				line.append(columnnames[col]);
				line.append(getColumnNameDelimiter());
			}

			for (int row = 0; row < data.length; row++) {
				Object obj = data[row][col];
				if (obj != null)
					line.append(obj.toString());

				if ((row + 1) < data.length)
					line.append(getValueDelimiter());

			}
			line.append(NEWLINE);
			result.append(line);
		}
		return result.toString();
	}

	/**
	 * @return the delimiter string to be used between column names
	 */
	public String getColumnNameDelimiter() {
		return m_columnnamedelimiter;
	}

	/**
	 * @return the string to use to delimit values in the output.
	 */
	public String getValueDelimiter() {
		return m_valuedelimiter;
	}

	/**
	 * @return the flags that determines if the column names should be displayed
	 */
	public boolean isShowColumnNames() {
		return m_showcolumnnames;
	}

	/**
	 * @return the flag that detemines if the output is transposed
	 */
	public boolean isTranspose() {
		return m_transpose;
	}

	/**
	 * Sets the delimiter string to be used between column names if the show
	 * headers flag is true.
	 */
	public void setColumnNameDelimiter(String delim) {
		m_columnnamedelimiter = delim;
	}

	/**
    *
    */
	public void setShowColumnNames(boolean show) {
		m_showcolumnnames = show;
	}

	/**
	 * Sets the flag that determines if rows and columsn should be transposed
	 */
	public void setTranspose(boolean btranspose) {
		m_transpose = btranspose;
	}

	/**
	 * Sets the string to use to delimit values in the output. Typically, this
	 * is a single character string, but you could provide more than that if
	 * needed.
	 * 
	 * @param delim
	 *            the string to delimit the values with
	 */
	public void setValueDelimiter(String delim) {
		m_valuedelimiter = delim;
	}

}
