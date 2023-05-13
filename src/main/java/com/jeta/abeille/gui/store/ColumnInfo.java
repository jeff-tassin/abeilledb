package com.jeta.abeille.gui.store;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class is used by the EditTableDialog represent a column in a TABLE
 * 
 * @author Jeff Tassin
 */
public class ColumnInfo extends ColumnMetaData implements JETAExternalizable {
	static final long serialVersionUID = -7509657144554491189L;

	public static int VERSION = 1;

	private boolean m_primarykey;
	private DataTypeInfo m_datatype;

	/**
	 * we store these values so we don't have to recreate the columnsizestring
	 * every time
	 */
	private transient int m_lastsize;
	private transient int m_lastscale;
	private transient String m_lastcolumnsize;

	/**
	 * ctor only for serialization
	 */
	public ColumnInfo() {

	}

	/**
	 * copy ctor
	 */
	public ColumnInfo(ColumnMetaData cmd) {
		super(cmd);
		m_primarykey = false;
	}

	/**
	 * ctor
	 */
	public ColumnInfo(String colName, int dataType, String typeName, int colSize, int precision, boolean bNullable,
			boolean bPrimary) {
		super(colName, dataType, typeName, colSize, null, bNullable);
		setScale(precision);
		m_primarykey = bPrimary;
	}

	/**
	 * @return the string that represents the column size. This includes the
	 *         scale as well as the precision (if the scale is non-zero).
	 */
	public String getColumnSizeString() {
		int scale = getScale();
		int colsize = getColumnSize();

		if (m_lastsize != colsize || m_lastscale != scale || m_lastcolumnsize == null) {
			if (colsize > 0 || scale > 0) {
				StringBuffer sbuff = new StringBuffer();
				sbuff.append(colsize);
				if (scale > 0) {
					sbuff.append(",");
					sbuff.append(scale);
				}

				m_lastcolumnsize = sbuff.toString();
			} else {
				m_lastcolumnsize = "";
			}
			m_lastsize = colsize;
			m_lastscale = scale;
		}
		return m_lastcolumnsize;
	}

	/**
	 * @return true if this column is part of a primary key
	 */
	public boolean isPrimaryKey() {
		return m_primarykey;
	}

	/**
	 * Sets the flag that indicates whether this column is part of the primary
	 * key
	 */
	public void setPrimaryKey(boolean isPrimary) {
		m_primarykey = isPrimary;
		if (isPrimary)
			setNullable(false);
	}

	public void setTypeName(String tName) {
		super.setTypeName(tName);
		m_datatype = null;
	}

	/**
	 * @return a data structure that defines type info for this column
	 */
	public DataTypeInfo getDataType(TSConnection conn) {
		if (m_datatype == null) {
			String name = getTypeName();
			m_datatype = new DataTypeInfo(DbUtils.getDataTypeInfo(conn, name, true));
			if (getType() != 0)
				m_datatype.setDataType(getType());
		}
		return m_datatype;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		super.readExternal(in);
		int version = in.readInt();
		m_primarykey = in.readBoolean();
		m_datatype = (DataTypeInfo) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeBoolean(m_primarykey);
		out.writeObject(m_datatype);
	}

}
