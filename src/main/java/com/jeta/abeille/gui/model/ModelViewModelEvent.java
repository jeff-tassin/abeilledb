package com.jeta.abeille.gui.model;

import java.util.ArrayList;

import com.jeta.abeille.database.model.TableId;

/**
 * Event class for ModelViewModelListener/Model
 * 
 * @author Jeff Tassin
 */
public class ModelViewModelEvent {
	// event ids
	public final static int TABLE_ADDED = 1;
	// this event occurs when the user removes a table from the view/model. NOT
	// when
	// the user drops the table in the database. That event is handled/fired by
	// the DbModel
	public final static int TABLE_REMOVED = 2;

	/**
	 * the id of the event
	 */
	private int m_eventid;

	/** the id of the table that the event refers to */
	private TableId m_tableid;

	/** the list of parameters for this event */
	private ArrayList m_params = new ArrayList();

	/**
	 * This is a generic event for the model that is not necessarily related to
	 * a table
	 */
	public ModelViewModelEvent(int id) {
		m_eventid = id;
	}

	/**
	 * ctor
	 */
	// public ModelViewModelEvent( int id, TableId tableId )
	// {
	// m_eventid = id;
	// m_tableid = tableId;
	// addParameter( tableId );
	// }

	/**
	 * ctor
	 */
	// public ModelViewModelEvent( int id, TableWidget widget )
	// {
	// m_eventid = id;
	// m_tableid = widget.getTableId();
	// addParameter( m_tableid );
	// addParameter( widget );
	// }

	/**
	 * ctor
	 */
	public ModelViewModelEvent(int id, Object value) {
		m_eventid = id;

		if (value instanceof TableWidget) {
			TableWidget widget = (TableWidget) value;
			m_tableid = widget.getTableId();
			addParameter(m_tableid);
			addParameter(widget);
		} else if (value instanceof TableId) {
			m_tableid = (TableId) value;
			addParameter(value);
		} else {
			addParameter(value);
		}
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
