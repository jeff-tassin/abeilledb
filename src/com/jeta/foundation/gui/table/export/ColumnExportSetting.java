package com.jeta.foundation.gui.table.export;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class defines how a column from a selection in a table is
 * copied/exported. We allow the user to use regular expression to control how a
 * column is saved.
 * 
 * @author Jeff Tassin
 */
public class ColumnExportSetting implements JETAExternalizable {
	static final long serialVersionUID = 2317469165452593598L;

	public static int VERSION = 1;

	/**
	 * The name of the column
	 */
	private String m_columnname; // the name of the column

	/**
	 * the index of the column in table model (add 1 and it is the index in the
	 * query metadata model )
	 */
	private int m_columnindex;

	/**
	 * flag that determines whether this column is included in the output
	 */
	private boolean m_include;

	/**
	 * the regular expression that determines the format of the saved column
	 */
	private String m_outputregex;

	/**
	 * ctor for serialization
	 */
	public ColumnExportSetting() {

	}

	/**
	 * ctor
	 */
	public ColumnExportSetting(String columnname, int columnindex, boolean include, String regex) {
		m_columnname = columnname;
		m_columnindex = columnindex;
		m_include = include;
		m_outputregex = regex;
	}

	/**
	 * @return the index of the column in the query results model that these
	 *         settings refer to
	 */
	public int getColumnIndex() {
		return m_columnindex;
	}

	/**
	 * @return the name of the column this class is referring to
	 */
	public String getColumnName() {
		return m_columnname;
	}

	/**
	 * @return true if this column is to be included in the output
	 */
	public boolean isIncluded() {
		return m_include;
	}

	/**
	 * @return the regular expression that determines for format of the saved
	 *         column
	 */
	public String getOutputExpression() {
		return m_outputregex;
	}

	/**
	 * Sets the index of the column in the query results model that these
	 * settings refer to
	 */
	public void setColumnIndex(int col) {
		m_columnindex = col;
	}

	/**
	 * Sets the flag that indicates if this column should be included in the
	 * output
	 */
	public void setIncluded(boolean included) {
		m_include = included;
	}

	/**
	 * Sets the regular expression that determines the format of the saved
	 * column (this includes the $value tag )
	 */
	public void setOutputExpression(String regex) {
		m_outputregex = regex;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_columnname = (String) in.readObject();
		m_columnindex = in.readInt();
		m_include = in.readBoolean();
		m_outputregex = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeObject(m_columnname);
		out.writeInt(m_columnindex);
		out.writeBoolean(m_include);
		out.writeObject(m_outputregex);
	}

}
