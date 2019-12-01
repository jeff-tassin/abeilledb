package com.jeta.abeille.gui.update;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class defines the parameters used for displaying a column component in
 * the InstanceView
 * 
 * @author Jeff Tassin
 */
public class ColumnSettings implements JETAExternalizable {
	static final long serialVersionUID = 6778470953035150959L;

	public static int VERSION = 1;

	/** the column meta data for this settings object */
	private ColumnMetaData m_cmd;

	/** if non-null, the name to display instead of the column name on the form */
	private String m_displayname;

	/** determines if this column is displayed on the InstanceView */
	private boolean m_visible;

	/** the handler component for the column data */
	private ColumnHandler m_handler; // the handler component for column data

	/**
	 * if true, then the instance view will automatically fill up any available
	 * space on the view by increasing the height of this column
	 */
	private boolean m_autoheight;

	/**
	 * This is used for those instanceviews that require the modelindex to be
	 * saved. Specifically, this is needed when the InstanceView is based on a
	 * SQL query result. The only reliable way to match columns to the settings
	 * is to use the model index. The formbuilder and tableinstance don't use
	 * this.
	 */
	private int m_modelindex;

	/**
	 * ctor
	 */
	public ColumnSettings() {

	}

	/**
	 * ctor
	 */
	public ColumnSettings(ColumnSettings cs) {
		this(cs.getColumnMetaData(), cs.isVisible(), cs.getColumnHandler());
		m_modelindex = cs.m_modelindex;
	}

	/**
	 * ctor
	 */
	public ColumnSettings(ColumnMetaData cmd, boolean bVisible) {
		this(cmd, bVisible, new DefaultColumnHandler());
	}

	/**
	 * ctor
	 */
	public ColumnSettings(ColumnMetaData cmd, boolean bVisible, ColumnHandler handler) {
		m_cmd = cmd;
		m_visible = bVisible;
		m_handler = handler;
	}

	/**
	 * Equals implementation
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ColumnMetaData) {
			return ((ColumnMetaData) obj).equals(m_cmd);
		} else if (obj instanceof ColumnSettings) {
			if (m_cmd != null) {
				return m_cmd.equals(((ColumnSettings) obj).m_cmd);
			}
		}
		return false;
	}

	/**
	 * @return the column handler
	 */
	public ColumnHandler getColumnHandler() {
		return m_handler;
	}

	/**
	 * @return the underlying column meta data
	 */
	public ColumnMetaData getColumnMetaData() {
		return m_cmd;
	}

	/**
	 * @return the column name
	 */
	public String getColumnName() {
		return m_cmd.getColumnName();
	}

	/**
	 * @return the datatype for the underlying column metadata.
	 */
	public int getType() {
		return m_cmd.getType();
	}

	/** @return the name to display instead of the column name on the form */
	public String getDisplayName() {
		return m_displayname;
	}

	/**
	 * @return the index of the column as found in the data model
	 */
	public int getModelIndex() {
		return m_modelindex;
	}

	/**
	 * @return the column name
	 */
	public String getTableName() {
		TableId tableid = m_cmd.getTableId();
		if (tableid == null)
			return null;
		else
			return tableid.getTableName();
	}

	/**
	 * @return the auto width flag
	 */
	public boolean isAutoHeight() {
		return m_autoheight;
	}

	/**
	 * @return true if this column is visible in the instance view
	 */
	public boolean isVisible() {
		return m_visible;
	}

	/**
	 * Prints this object to the console
	 */
	public void print() {
		System.out.println("column meta data: " + m_cmd);
		System.out.println(" isVisible: " + m_visible);
		System.out.println(" handler: " + m_handler);
		System.out.println(" autoheight: " + m_autoheight);
	}

	/**
	 * Sets the auto height flag
	 */
	public void setAutoHeight(boolean bAuto) {
		m_autoheight = bAuto;
	}

	/**
	 * Sets the column handler
	 */
	public void setColumnHandler(ColumnHandler handler) {
		m_handler = handler;
	}

	/** Sets the name to display instead of the column name on the form */
	public void setDisplayName(String displayName) {
		m_displayname = displayName;
	}

	/**
	 * Sets the index of the column as found in the data model
	 */
	public void setModelIndex(int modelindex) {
		m_modelindex = modelindex;
	}

	/**
	 * Shows/Hides this column in the instance view
	 */
	public void setVisible(boolean bVisible) {
		m_visible = bVisible;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_cmd = (ColumnMetaData) in.readObject();
		m_displayname = (String) in.readObject();
		m_visible = in.readBoolean();
		m_handler = (ColumnHandler) in.readObject();
		m_autoheight = in.readBoolean();
		m_modelindex = in.readInt();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {

		out.writeInt(VERSION);
		out.writeObject(m_cmd);
		out.writeObject(m_displayname);
		out.writeBoolean(m_visible);
		out.writeObject(m_handler);
		out.writeBoolean(m_autoheight);
		out.writeInt(m_modelindex);
	}

}
