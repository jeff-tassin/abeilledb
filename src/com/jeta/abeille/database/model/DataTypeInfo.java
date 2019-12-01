package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import javax.swing.ImageIcon;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

public class DataTypeInfo implements Comparable, Cloneable, JETAExternalizable {
	static final long serialVersionUID = -7983385737857583827L;

	public static int VERSION = 1;

	private transient ImageIcon m_icon;
	private String m_iconname;
	private String m_typename;
	private int m_datatype;

	private int m_precision;
	private short m_maxscale;
	private short m_minscale;

	private boolean m_supportsautoincrement = false;
	private boolean m_customprecision = false;
	private boolean m_customscale = false;
	private boolean m_requiredprecision = false;
	private boolean m_requiredscale = false;

	public static final String NO_CUSTOM_SIZE = "[]";
	public static final String CUSTOM_PRECISION = "[P]";
	public static final String CUSTOM_PRECISION_SCALE = "[P,S]";

	public static final String REQUIRED_CUSTOM_PRECISION = "(P)";
	public static final String REQUIRED_CUSTOM_PRECISION_SCALE = "(P,S)";

	/**
	 * ctor only for serialization
	 */
	public DataTypeInfo() {

	}

	/**
	 * copy ctor
	 */
	public DataTypeInfo(DataTypeInfo info) {
		m_icon = info.m_icon;
		m_iconname = info.m_iconname;
		m_typename = info.m_typename;
		m_datatype = info.m_datatype;

		m_precision = info.m_precision;
		m_maxscale = info.m_maxscale;
		m_minscale = info.m_minscale;

		m_supportsautoincrement = info.m_supportsautoincrement;
		m_customprecision = info.m_customprecision;
		m_customscale = info.m_customscale;
	}

	public DataTypeInfo(String typeName, String sizeDef) {
		this(typeName, 0, sizeDef, false);
	}

	public DataTypeInfo(String typeName, String sizeDef, String iconName) {
		this(typeName, 0, sizeDef, false);
		m_iconname = iconName;
		m_icon = TSGuiToolbox.loadImage(m_iconname);
	}

	public DataTypeInfo(String typeName, int dataType, String sizeDef) {
		this(typeName, dataType, sizeDef, false);
	}

	public DataTypeInfo(String typeName, int dataType, String sizeDef, boolean supportsAutoInc) {
		m_typename = typeName;
		m_datatype = dataType;
		m_supportsautoincrement = supportsAutoInc;

		m_iconname = DbUtils.getIconName(m_datatype);
		m_icon = DbUtils.getIcon(m_datatype);

		if (sizeDef.equals(NO_CUSTOM_SIZE)) {
			m_customprecision = false;
			m_customscale = false;
		} else if (sizeDef.equals(CUSTOM_PRECISION)) {
			m_customprecision = true;
			m_customscale = false;
		} else if (sizeDef.equals(CUSTOM_PRECISION_SCALE)) {
			m_customprecision = true;
			m_customscale = true;
		} else if (sizeDef.equals(REQUIRED_CUSTOM_PRECISION)) {
			m_customprecision = true;
			m_requiredprecision = true;
			m_customscale = false;
			m_requiredscale = false;
		} else if (sizeDef.equals(REQUIRED_CUSTOM_PRECISION_SCALE)) {
			m_customprecision = true;
			m_customscale = true;
			m_requiredprecision = true;
			m_requiredscale = true;
		} else {
			assert (false);
		}
	}

	public Object clone() {
		DataTypeInfo info = new DataTypeInfo(this);
		return info;
	}

	public int compareTo(Object o) {
		if (o instanceof DataTypeInfo) {
			DataTypeInfo di = (DataTypeInfo) o;
			return m_typename.compareToIgnoreCase(di.getTypeName());
		} else
			return -1;

	}

	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	/**
	 * @return the vendor type name
	 */
	public String getTypeName() {
		return m_typename;
	}

	/**
	 * @return the icon for this type
	 */
	public ImageIcon getIcon() {
		return m_icon;
	}

	public String getIconName() {
		return m_iconname;
	}

	public int getPrecision() {
		return m_precision;
	}

	/**
	 * @return the JDBC data type
	 */
	public int getType() {
		return m_datatype;
	}

	public int hashCode() {
		return m_typename.hashCode();
	}

	public boolean isPrecisionRequired() {
		return m_requiredprecision;
	}

	public boolean isScaleRequired() {
		return m_requiredscale;
	}

	/**
	 * Override serialize so we can load our icon properly
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		m_icon = TSGuiToolbox.loadImage(m_iconname);
	}

	public void setDataType(int data_type) {
		m_datatype = data_type;
		m_icon = DbUtils.getIcon(m_datatype);
	}

	public void setPrecision(int precision) {
		m_precision = precision;
	}

	public void setMinimumScale(short min_scale) {
		m_minscale = min_scale;
	}

	public void setMaximumScale(short max_scale) {
		m_maxscale = max_scale;
	}

	/**
	 * Sets the vendor type name
	 */
	public void setTypeName(String tName) {
		m_typename = tName;
	}

	public boolean supportsAutoIncrement() {
		return m_supportsautoincrement;
	}

	/**
	 * @return true if the user can specify a scale for this data type
	 */
	public boolean supportsCustomPrecision() {
		return m_customprecision;
	}

	/**
	 * @return true if the user can specify a scale for this data type
	 */
	public boolean supportsCustomScale() {
		return m_customscale;
	}

	public String toString() {
		return m_typename;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_iconname = (String) in.readObject();
		m_typename = (String) in.readObject();
		m_datatype = in.readInt();

		m_precision = in.readInt();
		m_maxscale = in.readShort();
		m_minscale = in.readShort();

		m_supportsautoincrement = in.readBoolean();
		m_customprecision = in.readBoolean();
		m_customscale = in.readBoolean();
		m_requiredprecision = in.readBoolean();
		m_requiredscale = in.readBoolean();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_iconname);
		out.writeObject(m_typename);
		out.writeInt(m_datatype);

		out.writeInt(m_precision);
		out.writeShort(m_maxscale);
		out.writeShort(m_minscale);

		out.writeBoolean(m_supportsautoincrement);
		out.writeBoolean(m_customprecision);
		out.writeBoolean(m_customscale);
		out.writeBoolean(m_requiredprecision);
		out.writeBoolean(m_requiredscale);
	}

}
