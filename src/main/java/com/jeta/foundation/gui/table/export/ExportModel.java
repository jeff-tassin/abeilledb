package com.jeta.foundation.gui.table.export;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;

import javax.swing.table.TableModel;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * Represents the data that we wish to export to a file. This data is always the
 * result of a query.
 * 
 * @author Jeff Tassin
 */
public class ExportModel implements JETAExternalizable {
	static final long serialVersionUID = 8195930484083676484L;

	public static int VERSION = 1;

	/**
	 * The string used to separate values in the exported file Typically either
	 * tabs or commas
	 */
	private String m_valuedelimiter = " ";

	/**
	 * The character used to delimit text values in the exported file Typically
	 * either single or double quotes
	 */
	private char m_textdelimiter;

	/** determines if column headers are exported or not */
	private boolean m_showcolumnnames;

	/** the delimiter between column names */
	private String m_columnnamedelimiter;

	/** the model that determines how each column value is formatted */
	private transient ColumnExportModel m_columnmodel;

	/**
	 * Flag that determines if the output is transposed. That is, if the rows
	 * and columns are switched.
	 */
	private boolean m_transpose;

	/**
	 * This expression determines how each line of values is outout. It is
	 * typcially an expression like <literal>$line<literal> where $line is a
	 * line of values. Any additional text is specified by the <literals>
	 */
	private String m_linedecorator;

	/**
	 * This value is used when exporting a null value. Set to some non-null
	 * value if you want to substitute some text for null values. Otherwise,
	 * nothing is written when null values are encountered
	 */
	private String m_nullsvalue;

	public static final String COMPONENT_ID = "jeta.ExportModel";

	/**
	 * ctor
	 */
	public ExportModel() {
		m_valuedelimiter = ExportDefaults.VALUE_DELIMITER;
		m_textdelimiter = ExportDefaults.TEXT_DELIMITER;
	}

	/**
	 * ctor
	 * 
	 * @param model
	 *            the model that contains the data we will export
	 */
	public ExportModel(TableModel tableModel) {
		this();
		// now create the ColumnExportModel
		ColumnExportModel cmodel = new ColumnExportModel();
		for (int col = 0; col < tableModel.getColumnCount(); col++) {
			ColumnExportSetting setting = new ColumnExportSetting(tableModel.getColumnName(col), col, true, null);
			cmodel.addSetting(setting);
		}
		setColumnExportModel(cmodel);
	}

	/**
	 * @return the number of columns that we are exorting
	 */
	public int getColumnCount() {
		return m_columnmodel.getColumnCount();
	}

	/**
	 * @return the export settings for a given column
	 */
	public ColumnExportSetting getColumnExportSetting(int colIndex) {
		return m_columnmodel.getColumnExportSetting(colIndex);
	}

	/**
	 * @return the delimiter used to separate column names in the output
	 */
	public String getColumnNameDelimiter() {
		return m_columnnamedelimiter;
	}

	/**
	 * @return the set of columns (ColumnExportSetting objects) used to form the
	 *         output. Only columns that have the included flag set to true are
	 *         returned. Furthermore, the set is the correct order.
	 */
	public Collection getIncludedColumns() {
		LinkedList result = new LinkedList();
		for (int index = 0; index < getColumnCount(); index++) {
			ColumnExportSetting cs = getColumnExportSetting(index);
			if (cs.isIncluded())
				result.add(cs);
		}

		return result;
	}

	/**
	 * Gets the expression used to control how each row of values is output to
	 * the stream.
	 */
	public String getLineDecorator() {
		return m_linedecorator;
	}

	/**
	 * This value is used when exporting a null value. Set to some non-null
	 * value if you want to substitute some text for null values. Otherwise,
	 * nothing is written when null values are encountered
	 */
	public String getNullsValue() {
		return m_nullsvalue;
	}

	/**
	 * @return the text used to delimit text values in the export file. This is
	 *         typically either a single or double quotes.
	 */
	public char getTextDelimiter() {
		return m_textdelimiter;
	}

	/**
	 * @return the string used to separate values in the exported files.
	 *         Typically this is either commas or tabs
	 */
	public String getValueDelimiter() {
		return m_valuedelimiter;
	}

	/**
	 * @return the flag that determines if we should how the column headers in
	 *         the exported results
	 */
	public boolean isShowColumnNames() {
		return m_showcolumnnames;
	}

	/**
	 * @return the flag that determines if the output is transposed
	 */
	public boolean isTransposed() {
		return m_transpose;
	}

	/**
	 * Sets the column export model
	 */
	public void setColumnExportModel(ColumnExportModel model) {
		m_columnmodel = model;
	}

	/**
	 * Sets the delimiter between the column names
	 */
	public void setColumnNameDelimiter(String delim) {
		m_columnnamedelimiter = delim;
	}

	/**
	 * Sets the expression used to control how each row of values is output to
	 * the stream.
	 */
	public void setLineDecorator(String decorator) {
		m_linedecorator = decorator;
	}

	/**
	 * This value is used when exporting a null value. Set to some non-null
	 * value if you want to substitute some text for null values. Otherwise,
	 * nothing is written when null values are encountered
	 */
	public void setNullsValue(String nullsValue) {
		m_nullsvalue = nullsValue;
	}

	/**
	 * Sets the flag that determines if we should how the column names in the
	 * exported results
	 */
	public void setShowColumnNames(boolean bShow) {
		m_showcolumnnames = bShow;
	}

	/**
	 * Sets the character used to delimit text string values
	 */
	public void setTextDelimiter(char delim) {
		m_textdelimiter = delim;
	}

	/**
	 * Flag that determines if the output should be transposed. That is, the
	 * rows and columns switched.
	 */
	public void setTranspose(boolean btranspose) {
		m_transpose = btranspose;
	}

	/**
	 * Sets the character used to delimit values
	 */
	public void setValueDelimiter(String delim) {
		m_valuedelimiter = delim;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_valuedelimiter = (String) in.readObject();
		m_textdelimiter = in.readChar();
		m_showcolumnnames = in.readBoolean();
		m_columnnamedelimiter = (String) in.readObject();
		m_transpose = in.readBoolean();
		m_linedecorator = (String) in.readObject();
		m_nullsvalue = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeObject(m_valuedelimiter);
		out.writeChar(m_textdelimiter);
		out.writeBoolean(m_showcolumnnames);
		out.writeObject(m_columnnamedelimiter);
		out.writeBoolean(m_transpose);
		out.writeObject(m_linedecorator);
		out.writeObject(m_nullsvalue);
	}

}
