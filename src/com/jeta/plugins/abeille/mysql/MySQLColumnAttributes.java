package com.jeta.plugins.abeille.mysql;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.ColumnAttributes;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class represents attributes for columns in a MySQL table.
 * 
 * @author Jeff Tassin
 */
public class MySQLColumnAttributes extends ColumnAttributes implements JETAExternalizable, Cloneable {
	static final long serialVersionUID = 681760921819613604L;

	public static int VERSION = 1;

	/**
	 * true if this column is an unsigned integral type
	 */
	private boolean m_unsigned;

	/**
	 * true if the zero fill flag is set for this column
	 */
	private boolean m_zerofill;

	/**
	 * The name of the column to place a newly created column or changed column
	 * in an existing table. This is only used when changing a table or column.
	 * We store the column which to place the new column after.
	 */
	private String m_altercolumn_position;

	/**
	 * The string for extra parameters for the column (e.g. enum and set
	 * parameters)
	 */
	private String m_parameters;

	/**
	 * ctor
	 */
	public MySQLColumnAttributes() {

	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		MySQLColumnAttributes attr = new MySQLColumnAttributes();
		attr.m_unsigned = m_unsigned;
		attr.m_zerofill = m_zerofill;
		attr.m_parameters = m_parameters;
		attr.m_altercolumn_position = m_altercolumn_position;
		return attr;
	}

	public String getAlterColumnPosition() {
		return m_altercolumn_position;
	}

	/**
	 * @return the string for extra parameters for the column (e.g. enum and set
	 *         parameters)
	 */
	public String getParameters() {
		return m_parameters;
	}

	/**
	 * @return true if this column is an unsigned integral type
	 */
	public boolean isUnsigned() {
		return m_unsigned;
	}

	/**
	 * @return true if the zero fill flag is set for this column
	 */
	public boolean isZeroFill() {
		return m_zerofill;
	}

	public void setAlterColumnPosition(String colname) {
		m_altercolumn_position = colname;
	}

	/**
	 * Sets the string for extra parameters for the column (e.g. enum and set
	 * parameters)
	 */
	public void setParameters(String params) {
		m_parameters = params;
	}

	public void setUnsigned(boolean bunsigned) {
		m_unsigned = bunsigned;
	}

	public void setZeroFill(boolean zerofill) {
		m_zerofill = zerofill;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_unsigned = in.readBoolean();
		m_zerofill = in.readBoolean();
		m_altercolumn_position = (String) in.readObject();
		m_parameters = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeBoolean(m_unsigned);
		out.writeBoolean(m_zerofill);
		out.writeObject(m_altercolumn_position);
		out.writeObject(m_parameters);
	}

}
