package com.jeta.plugins.abeille.mysql;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.DataTypeInfo;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * Data Type information specific to MySQL
 * 
 * @author Jeff Tassin
 */
public class MySQLDataTypeInfo extends DataTypeInfo implements JETAExternalizable, Cloneable {
	static final long serialVersionUID = -253465807580690750L;

	public static int VERSION = 1;

	/** flag that indicates if this MySQL type supports unsigned values */
	private boolean m_supportsUnsigned = false;

	/** flag that indicates if this MySQL type supports zero fill */
	private boolean m_supportsZeroFill = false;

	/** flag that indicates if the type supports extra column attributes */
	private boolean m_supportsAttributes = false;

	/**
	 * ctor for serialization
	 */
	public MySQLDataTypeInfo() {

	}

	/**
	 * copy ctor
	 */
	public MySQLDataTypeInfo(MySQLDataTypeInfo info) {
		super(info);
		m_supportsUnsigned = info.m_supportsUnsigned;
		m_supportsZeroFill = info.m_supportsZeroFill;
		m_supportsAttributes = info.m_supportsAttributes;
	}

	/**
	 * ctor
	 */
	public MySQLDataTypeInfo(String typeName, String sizeDef, boolean supportsUnsigned, boolean supportsZeroFill) {
		this(typeName, sizeDef, supportsUnsigned, supportsZeroFill, false, false);
	}

	/**
	 * ctor
	 */
	public MySQLDataTypeInfo(String typeName, String sizeDef, int datatype) {
		this(typeName, sizeDef, false, false, false, false);
		setDataType(datatype);
	}

	public MySQLDataTypeInfo(String typeName, String sizeDef, boolean supportsUnsigned, boolean supportsZeroFill,
			boolean supportsAutoInc) {
		this(typeName, sizeDef, supportsUnsigned, supportsZeroFill, supportsAutoInc, false);
	}

	public MySQLDataTypeInfo(String typeName, String sizeDef, int datatype, boolean supportsUnsigned,
			boolean supportsZeroFill, boolean supportsAutoInc) {
		this(typeName, sizeDef, supportsUnsigned, supportsZeroFill, supportsAutoInc, false);
		setDataType(datatype);
	}

	public MySQLDataTypeInfo(String typeName, String sizeDef, boolean supportsUnsigned, boolean supportsZeroFill,
			boolean supportsAutoInc, boolean supportsAttributes) {
		super(typeName, 0, sizeDef, supportsAutoInc);

		m_supportsUnsigned = supportsUnsigned;
		m_supportsZeroFill = supportsZeroFill;
		m_supportsAttributes = supportsAttributes;
	}

	public Object clone() {
		return new MySQLDataTypeInfo(this);
	}

	public boolean supportsUnsigned() {
		return m_supportsUnsigned;
	}

	public boolean supportsZeroFill() {
		return m_supportsZeroFill;
	}

	public boolean supportsAttributes() {
		return m_supportsAttributes;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		super.readExternal(in);
		int version = in.readInt();
		m_supportsUnsigned = in.readBoolean();
		m_supportsZeroFill = in.readBoolean();
		m_supportsAttributes = in.readBoolean();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeBoolean(m_supportsUnsigned);
		out.writeBoolean(m_supportsZeroFill);
		out.writeBoolean(m_supportsAttributes);

	}

}
