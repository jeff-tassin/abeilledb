package com.jeta.abeille.gui.triggers;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.database.procedures.StoredProcedureService;

import com.jeta.abeille.database.triggers.Trigger;
import com.jeta.abeille.database.triggers.TriggerService;

import com.jeta.foundation.i18n.I18N;

/**
 * This class is the table model for the TriggersView. It gets the list of
 * triggers from the TriggerService for a given table id and stores them in this
 * model. This model stores the data directly as Trigger objects.
 * 
 * @author Jeff Tassin
 */
public class TriggersModel extends AbstractTableModel {
	/** an array of Trigger object */
	private ArrayList m_data;

	/** an array of column names for the table */
	private String[] m_colnames;

	/** an array of column types for the table */
	private Class[] m_coltypes;

	/** The database connection */
	private TSConnection m_connection;

	/** The id of the table we are showing the triggers for */
	private TableId m_tableid;

	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int WHEN_COLUMN = 1;
	static final int EVENT_COLUMN = 2;
	static final int PROCEDURE_COLUMN = 3;
	static final int ARGS_COLUMN = 4;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the triggers for
	 */
	public TriggersModel(TSConnection connection, TableId tableId) {
		super();

		m_connection = connection;
		m_data = new ArrayList();

		String[] values = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("When"),
				I18N.getLocalizedMessage("Event"), I18N.getLocalizedMessage("Procedure"),
				I18N.getLocalizedMessage("Arguments") };
		m_colnames = values;

		Class[] types = { String.class, String.class, String.class, String.class, String.class };
		m_coltypes = types;

		setTableId(m_tableid);
	}

	/**
	 * Adds the given trigger object to the table
	 */
	public void addRow(TriggerWrapper trig_info) {
		if (m_data == null)
			m_data = new ArrayList();

		m_data.add(trig_info);
		fireTableRowsInserted(m_data.size() - 1, m_data.size() - 1);
	}

	/**
	 * @return the number of columns in this model
	 */
	public int getColumnCount() {
		return m_colnames.length;
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the number of rows objects in this model
	 */
	public int getRowCount() {
		return m_data.size();
	}

	/**
	 * @return the name of a column at a given index
	 */
	public String getColumnName(int column) {
		return m_colnames[column];
	}

	/**
	 * @return the type of column at a given index
	 */
	public Class getColumnClass(int column) {
		return m_coltypes[column];
	}

	/**
	 * @return the object at the given row in the model
	 */
	public TriggerWrapper getRow(int row) {
		if (row >= 0 && row < m_data.size())
			return (TriggerWrapper) m_data.get(row);
		else
			return null;
	}

	/**
	 * @return the tableid we are displaying triggers for
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/** "Name", "When", "Event", "Procedure", "Arguments" */
		TriggerWrapper tinfo = getRow(row);
		if (column == NAME_COLUMN) {
			return tinfo.getName();
		} else if (column == WHEN_COLUMN) {
			return tinfo.getWhen();
		} else if (column == EVENT_COLUMN) {
			return tinfo.getEvent();
		} else if (column == PROCEDURE_COLUMN) {
			return tinfo.getProcedureName();
		} else if (column == ARGS_COLUMN) {
			return tinfo.getParametersString();
		} else
			return "";
	}

	/**
	 * Reload the model
	 */
	public void reload() {
		setTableId(m_tableid);
	}

	/**
	 * Remove all data items from this model
	 */
	public void removeAll() {
		m_data.clear();
	}

	/**
	 * Sets the tableid for this model
	 */
	public void setTableId(TableId tableid) {
		removeAll();
		m_tableid = tableid;

		if (tableid != null) {
			try {
				// we need to call the procedure service to get the procedure
				// name for the trigger.
				// the trigger does not store the procedure information
				// directly, rather it stores
				// the procedure key
				StoredProcedureService proc_srv = (StoredProcedureService) m_connection
						.getImplementation(StoredProcedureService.COMPONENT_ID);
				TriggerService triggersrv = (TriggerService) m_connection
						.getImplementation(TriggerService.COMPONENT_ID);
				Collection triggers = triggersrv.getTriggers(tableid);
				Iterator iter = triggers.iterator();
				while (iter.hasNext()) {
					Trigger trigger = (Trigger) iter.next();

					TriggerWrapper trig_info = new TriggerWrapper(trigger);
					Object proc_key = trigger.getProcedureKey();
					try {
						StoredProcedure proc = proc_srv.lookupProcedure(trigger.getProcedureKey());
						trig_info.setProcedureName(proc.getFullyQualifiedName());
					} catch (SQLException sqle) {
						// the procedure could not be found - this should not
						// happen
						trig_info.setProcedureName(I18N.getLocalizedMessage("Unknown"));
					}
					addRow(trig_info);
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}

		fireTableDataChanged();
	}

}
