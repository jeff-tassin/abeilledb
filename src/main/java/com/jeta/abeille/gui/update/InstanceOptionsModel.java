package com.jeta.abeille.gui.update;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;

import com.jeta.foundation.interfaces.app.ObjectStore;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class represents the columns in a database table for the OptionsPanel.
 * The OptionsPanel allows the user to configure which columsn in a table are
 * displayed in an InstanceView (as well as other options)
 * 
 * @author Jeff Tassin
 */
public class InstanceOptionsModel extends AbstractTableModel {
	String[] m_colnames; // an array of column names of the GUI table (not the
							// database table)
	Class[] m_coltypes; // this is the types of columns of the GUI table (not
						// the database table)

	private static final String INSTANCE_VIEW_OPTIONS = "jeta.abeille.update.instanceviewoptions";

	/**
	 * The model that defines the columns for our view
	 */
	private InstanceMetaData m_metadata;

	/** table columns */
	public static final int PRIMARY_KEY_COLUMN = 0;
	public static final int TABLE_NAME_COLUMN = 1;
	public static final int COLUMN_NAME_COLUMN = 2;
	public static final int DISPLAY_NAME_COLUMN = 3;
	public static final int HANDLER_COLUMN = 4;
	public static final int VISIBLE_COLUMN = 5;

	// icons
	static final ImageIcon m_pkimage;
	static final ImageIcon m_fkimage;

	static {
		m_pkimage = TSGuiToolbox.loadImage("primarykey16.gif");
		m_fkimage = TSGuiToolbox.loadImage("foreignkey16.gif");
	}

	/**
	 * ctor.
	 */
	public InstanceOptionsModel(InstanceMetaData model) {
		super();

		m_metadata = model;

		String[] values = new String[6];
		values[PRIMARY_KEY_COLUMN] = "";
		values[TABLE_NAME_COLUMN] = I18N.getLocalizedMessage("Table Name");
		values[COLUMN_NAME_COLUMN] = I18N.getLocalizedMessage("Column Name");
		values[DISPLAY_NAME_COLUMN] = I18N.getLocalizedMessage("Display Name");
		values[HANDLER_COLUMN] = I18N.getLocalizedMessage("Handler");
		values[VISIBLE_COLUMN] = I18N.getLocalizedMessage("Visible");
		m_colnames = values;
		Class[] types = { ImageIcon.class, String.class, String.class, String.class, String.class, Boolean.class };
		m_coltypes = types;
	}

	/**
	 * Fires an event that the metadata model has changed
	 */
	public void fireModelChanged() {
		fireTableDataChanged();
	}

	/**
	 * @return the number of columns in the JTable
	 */
	public int getColumnCount() {
		return m_colnames.length;
	}

	/**
	 * @return the number of rows in the JTable
	 */
	public int getRowCount() {
		return m_metadata.getColumnCount();
	}

	public String getColumnName(int column) {
		return m_colnames[column];
	}

	public Class getColumnClass(int column) {
		return m_coltypes[column];
	}

	/**
	 * @return the underlying metadata
	 */
	public InstanceMetaData getMetaData() {
		return m_metadata;
	}

	/**
	 * @return the object at the given row in the model
	 */
	public ColumnSettings getRow(int row) {
		return m_metadata.getColumnSettings(row);
	}

	public boolean isCellEditable(Object node, int column) {
		return false;
	}

	public Object getValueAt(int row, int column) {
		// "Primary/Foreign Key", "Column Name", "Order", "Visible", "Size"
		ColumnSettings csi = getRow(row);
		if (column == PRIMARY_KEY_COLUMN) // primary key
		{
			if (m_metadata.isPrimaryKey(csi.getColumnMetaData()))
				return m_pkimage;
			else if (m_metadata.isLink(csi.getColumnMetaData()))
				return m_fkimage;
			else
				return null;
		} else if (column == TABLE_NAME_COLUMN) // columnname
		{
			String tname = csi.getTableName();
			if (tname == null)
				return "";
			else
				return tname;
		} else if (column == COLUMN_NAME_COLUMN) // columnname
			return csi.getColumnName();
		else if (column == DISPLAY_NAME_COLUMN) {
			String dname = csi.getDisplayName();
			if (dname == null || dname.trim().length() == 0) {
				dname = csi.getColumnName();
			}
			return dname;
		} else if (column == HANDLER_COLUMN) // handler
			return csi.getColumnHandler().getName();
		else if (column == VISIBLE_COLUMN) // visible
		{
			return Boolean.valueOf(csi.isVisible());
		} else
			return "";
	}

	/**
	 * Removes the object at the given index
	 */
	public ColumnSettings remove(int index) {
		ColumnSettings result = m_metadata.remove(index);
		fireTableChanged(new TableModelEvent(this, index, index, 0, TableModelEvent.DELETE));
		return result;
	}

	/**
	 * Removes the given column settings object from the model
	 */
	public void remove(ColumnSettings ci) {
		int index = m_metadata.getIndex(ci);
		if (index >= 0) {
			remove(index);
		}
	}

	/**
	 * Resets the model to the default settings
	 */
	public void reset() {
		m_metadata.reset();
		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * Moves the object found at the old index to the new index
	 */
	public void reorder(int newIndex, int oldIndex) {
		m_metadata.reorder(newIndex, oldIndex);
		fireTableChanged(new TableModelEvent(this));
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
		m_metadata.setRow(index, info);
		fireTableChanged(new TableModelEvent(this, index, index, 0, TableModelEvent.UPDATE));
	}

	public void setValueAt(Object aValue, int row, int column) {
		// only needed if we enable editing in the table
	}

	/**
	 * Saves the user defined settings to the application state store
	 */
	public void save() {

	}
}
