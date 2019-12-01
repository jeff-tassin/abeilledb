package com.jeta.foundation.gui.table.export;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.ArrayList;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class contains the settings that define how an individual value in a
 * table column is exported (e.g. copied to clipboard).
 * 
 * @author Jeff Tassin
 */
public class ColumnExportModel implements JETAExternalizable {
	static final long serialVersionUID = 8386382637800145474L;

	public static int VERSION = 1;

	private ArrayList m_data = new ArrayList();

	/**
	 * ctor for serialization
	 */
	public ColumnExportModel() {

	}

	/**
	 * Adds a setting to the list
	 */
	public void addSetting(ColumnExportSetting setting) {
		m_data.add(setting);
	}

	/**
	 * @return the number of columns that we are exorting
	 */
	public int getColumnCount() {
		return m_data.size();
	}

	/**
	 * @return the export settings for a given column
	 */
	public ColumnExportSetting getColumnExportSetting(int colIndex) {
		return (ColumnExportSetting) m_data.get(colIndex);
	}

	/**
	 * Moves the object found at the old index to the new index
	 */
	public void reorder(int newIndex, int oldIndex) {
		Object obj = m_data.remove(oldIndex);
		m_data.add(newIndex, obj);
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_data = (ArrayList) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_data);
	}

}
