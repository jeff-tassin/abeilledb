package com.jeta.abeille.database.procedures;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class represents a parameter used in a stored procedure
 * 
 * @author Jeff Tassin
 */
public class ProcedureParameter implements JETAExternalizable, Cloneable {
	static final long serialVersionUID = 3953367388208625401L;

	public static int VERSION = 1;

	/** the name of this parameter */
	private String m_name;

	/** the JDBC data type of this parameter (e.g. INTEGER, FLOAT, etc. ) */
	private int m_jdbctype;

	/** the data type as identified by the vendor */
	private String m_vendortype;

	/** the direction for the paramter: IN, OUT, INOUT, etc. */
	private ParameterDirection m_direction;

	/** a default value for the parameter */
	private String m_value;

	/**
	 * ctor
	 */
	public ProcedureParameter() {

	}

	/**
	 * ctor
	 * 
	 * @param name
	 *            the name of the parameter
	 * @param direction
	 *            the direction for the parameter
	 * @param type
	 *            the data type for the parameter ( as defined in
	 *            java.sql.Types)
	 * @param vendortype
	 *            a string representation of the data type as specified in
	 *            vendor parlance
	 */
	public ProcedureParameter(String name, ParameterDirection direction, int type, String vendortype) {
		m_name = name;
		m_direction = direction;
		m_jdbctype = type;
		m_vendortype = vendortype;
	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		ProcedureParameter param = new ProcedureParameter();
		param.m_name = m_name;
		param.m_jdbctype = m_jdbctype;
		param.m_vendortype = m_vendortype;
		param.m_direction = m_direction;
		param.m_value = m_value;
		return param;
	}

	/**
	 * @return the parameter direction (e.g. IN, OUT, INOUT, etc. )
	 */
	public ParameterDirection getDirection() {
		return m_direction;
	}

	/**
	 * @return the name of the parameter
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return the JDBC type for the paremeter
	 */
	public int getType() {
		return m_jdbctype;
	}

	/**
	 * @return the default value for the parameter
	 */
	public String getValue() {
		return m_value;
	}

	/**
	 * @return the type name
	 */
	public String getVendorType() {
		return m_vendortype;
	}

	/**
	 * Sets the name for this parameter
	 */
	public void setName(String paramName) {
		m_name = paramName;
	}

	/**
	 * Sets the default value for the parameter
	 */
	public void setValue(String value) {
		m_value = value;
	}

	public String toString() {
		return getVendorType();
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_name = (String) in.readObject();
		m_jdbctype = in.readInt();
		m_vendortype = (String) in.readObject();
		m_direction = (ParameterDirection) in.readObject();
		m_value = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_name);
		out.writeInt(m_jdbctype);
		out.writeObject(m_vendortype);
		out.writeObject(m_direction);
		out.writeObject(m_value);
	}

}
