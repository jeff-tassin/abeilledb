package com.jeta.abeille.gui.model;

import java.util.ArrayList;

import com.jeta.abeille.database.model.TableId;

/**
 * Event class for ModelerModel
 * 
 * @author Jeff Tassin
 */
public class ModelerEvent {
	public final static int TABLE_CHANGED = 3;
	public final static int TABLE_RENAMED = 4;
	/** fired when a table is acutally deleted from the database */
	public final static int TABLE_DELETED = 5;
	public final static int TABLE_CREATED = 6;
	public final static int LINK_CREATED = 7;
	public final static int LINK_DELETED = 8;
	/**
	 * fired when a catalog has changed in the modeler. This is typically called
	 * when a catalog has been explicitly reloaded
	 */
	public final static int CATALOG_CHANGED = 9;

	/**
	 * the id of the event
	 */
	private int m_eventid;

	/** the id of the table that the event refers to */
	private TableId m_tableid;

	/** the list of parameters for this event */
	private ArrayList m_params = new ArrayList();

	/**
	 * ctor
	 */
	public ModelerEvent(int id, Object obj) {
		m_eventid = id;
		addParameter(obj);
	}

	/**
	 * ctor
	 */
	public ModelerEvent(int id, TableId tableId) {
		m_eventid = id;
		m_tableid = tableId;
		addParameter(tableId);
	}

	/**
	 * ctor (rename event)
	 */
	public ModelerEvent(TableId newId, TableId oldId) {
		m_eventid = TABLE_RENAMED;
		m_tableid = oldId;
		addParameter(oldId);
		addParameter(newId);
	}

	/**
	 * @return the event id
	 */
	public int getID() {
		return m_eventid;
	}

	/**
	 * @return the tableid
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * Adds a parameter to the event
	 */
	public void addParameter(Object obj) {
		m_params.add(obj);
	}

	/**
	 * Get the parameter at the given (zero-based) index. When tableid is not
	 * null, the zero index is always the tableid
	 * 
	 * @return the parameter at the given index
	 */
	public Object getParameter(int index) {
		return m_params.get(index);
	}

	/**
	 * @return the number of parameters in this event.
	 */
	public int getParameterCount() {
		return m_params.size();
	}
}
