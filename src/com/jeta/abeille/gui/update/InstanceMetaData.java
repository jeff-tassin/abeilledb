package com.jeta.abeille.gui.update;

import java.util.ArrayList;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.LinkModel;

import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * This class defines the meta data used for the InstanceView. All columns for a
 * given result (i.e. TableMetaData or ResultSetMetaData from a query) are
 * represented. This class also maintains the column settings for a given column
 * (e.g. whether the column is visible or not).
 * 
 * @author Jeff Tassin
 */
public abstract class InstanceMetaData {
	private ArrayList m_data = new ArrayList();

	/**
	 * ctor
	 */
	public InstanceMetaData() {

	}

	/**
	 * Adds a column setting to the meta data model
	 */
	protected void addColumnSettings(ColumnSettings ci) {
	    m_data.add(ci);
	}

	/**
	 * @return the number of columns in the model
	 */
	public int getColumnCount() {
		return m_data.size();
	}

	/**
	 * @return an array of column settings for this model
	 */
	public ColumnSettings[] getColumnSettings() {
		return (ColumnSettings[]) m_data.toArray(new ColumnSettings[0]);
	}

	/**
	 * Gets the column settings at the given index
	 */
	public ColumnSettings getColumnSettings(int colIndex) {
		return (ColumnSettings) m_data.get(colIndex);
	}

	/**
	 * @return the column metadata at the given column index
	 */
	public ColumnMetaData getColumnMetaData(int colIndex) {
		ColumnSettings ci = getColumnSettings(colIndex);
		return ci.getColumnMetaData();
	}

	/**
	 * @return the index of the columnsetting that contains the given column
	 *         metadata. -1 is returned if the column metadata cannot be found
	 */
	protected int getIndex(ColumnMetaData cmd) {
		if (cmd == null)
			return -1;

		// @todo we really should cache this
		for (int index = 0; index < m_data.size(); index++) {
			ColumnSettings settings = (ColumnSettings) m_data.get(index);
			if (cmd.equals(settings.getColumnMetaData())) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * @return the index of the given column settings object. -1 is returned if
	 *         the object is not in the model
	 */
	public int getIndex(ColumnSettings ci) {
		if (ci == null)
			return -1;

		ColumnMetaData cmd = ci.getColumnMetaData();
		if (cmd == null)
			return -1;

		for (int index = 0; index < m_data.size(); index++) {
			ColumnSettings settings = (ColumnSettings) m_data.get(index);
			if (cmd.equals(settings.getColumnMetaData())) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * @return the model that defines the links between this instance view and
	 *         any existing tables in the database
	 */
	public abstract LinkModel getLinkModel();

	/**
	 * @return a unique identifier for this model. If this is a table model,
	 *         then the UID is basically the schema.tablename. If this is a
	 *         query result, then the UID is the query string. If this is from a
	 *         saved query, then this is the UID of the saved query
	 */
	public abstract String getUID();

	/**
	 * @return true if the given column name is link to another table (this is
	 *         either a foreign key or a column from different table from the
	 *         form builder)
	 */
	public boolean isLink(ColumnMetaData cmd) {
		return false;
	}

	/**
	 * @return true if the given column name is a primary key
	 */
	public boolean isPrimaryKey(ColumnMetaData cmd) {
		return false;
	}

	/**
	 * Removes all settings in the model
	 */
	public void removeAll() {
		m_data.clear();
	}

	/**
	 * Removes the object at the given index
	 */
	public ColumnSettings remove(int index) {
		return (ColumnSettings) m_data.remove(index);
	}

	/**
	 * Moves the object found at the old index to the new index
	 */
	public void reorder(int newIndex, int oldIndex) {
		Object obj = m_data.remove(oldIndex);
		m_data.add(newIndex, obj);
	}

	/**
	 * Resets the model to the default settings
	 */
	public abstract void reset();

	/**
	 * Sets the column settings at the given index
	 */
	protected void setColumnSettings(int colIndex, ColumnSettings settigns) {

	}

	/**
	 * Sets the option info at the given row. Any exising object at the give
	 * index is overwritten
	 * 
	 * @param index
	 *            the index in this model to set the object at
	 * @param info
	 *            the object to set.
	 */
	public void setRow(int index, ColumnSettings info) {
		m_data.set(index, info);
	}

}
