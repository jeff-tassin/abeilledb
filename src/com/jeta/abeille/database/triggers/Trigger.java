package com.jeta.abeille.database.triggers;

import java.util.ArrayList;
import java.util.Collection;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.procedures.ProcedureParameter;

/**
 * This is the definition for a trigger in the database
 * 
 * @author Jeff Tassin
 */
public class Trigger {
	/** the name of the trigger */
	private String m_name;

	/** the id for the table this trigger is bound to */
	private TableId m_tableid;

	/** a key that uniquely identifies this trigger */
	private Object m_key;

	/**
	 * a key that identifies the procedure that is invoked when the trigger
	 * fires
	 */
	private Object m_procedureKey;

	/** this is used ONLY when creating a procedure. */
	private String m_procname;

	/** this is used ONLY when creating a trigger */
	private String m_procargs;

	/** arguments to pass to the procedure when the trigger is fires */
	private ArrayList m_parameters;

	/** BEFORE - UPDATE,INSERT, and/or DELETE */
	private int m_eventmask;

	/** event mask constants */
	public static final int BEFORE_EVENT = 0x2;
	public static final int INSERT_EVENT = 0x4;
	public static final int DELETE_EVENT = 0x8;
	public static final int UPDATE_EVENT = 0x10;

	/**
	 * ctor
	 * 
	 * @param key
	 *            a unique ( vendor specified ) key that identifies this trigger
	 *            in the database
	 * @param tableId
	 *            the tableid this trigger is assigned to
	 */
	public Trigger(Object key, TableId tableId) {
		m_key = key;
		m_tableid = tableId;
	}

	/**
	 * Adds the given mask flag the trigger. The flag must be one of the defined
	 * EVENT mask constrants (update, insert, delete )
	 */
	public void addEventMask(int flag) {
		m_eventmask |= flag;
	}

	/**
	 * Adds a parameter that is passed to the procedure when the trigger is
	 * fired.
	 * 
	 * @param param
	 *            the parameter to add to this trigger definition
	 */
	public void addParameter(ProcedureParameter param) {
		if (m_parameters == null)
			m_parameters = new ArrayList();

		m_parameters.add(param);
	}

	/**
	 * @return the comma-separated list of function args for the trigger. NOTE:
	 *         this is ONLY used when creating a trigger.
	 */
	public String getFunctionArgs() {
		return m_procargs;
	}

	/**
	 * @return the name of the function for the trigger. NOTE: this is ONLY used
	 *         when creating a trigger.
	 */
	public String getFunctionName() {
		return m_procname;
	}

	/**
	 * @return the name of the trigger
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return a collection of parameters ( ProcedureParameter objects) that are
	 *         passed to the procedure when the trigger is fired.
	 */
	public Collection getParameters() {
		if (m_parameters == null)
			m_parameters = new ArrayList();

		return m_parameters;
	}

	/**
	 * @return the key used to indentify the procedure in the database
	 */
	public Object getProcedureKey() {
		return m_procedureKey;
	}

	/**
	 * @return the id of the table that this trigger is bound to
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * @return true if this trigger is fired after the event
	 */
	public boolean isAfter() {
		return !isBefore();
	}

	/**
	 * @return true if this trigger is fired before the event
	 */
	public boolean isBefore() {
		return ((m_eventmask & BEFORE_EVENT) > 0);
	}

	/**
	 * @return true if this trigger is fired on a delete
	 */
	public boolean isDeleteEvent() {
		return ((m_eventmask & DELETE_EVENT) > 0);
	}

	/**
	 * @return true if this trigger is fired on an insert
	 */
	public boolean isInsertEvent() {
		return ((m_eventmask & INSERT_EVENT) > 0);
	}

	/**
	 * @return true if this trigger is fired on an update
	 */
	public boolean isUpdateEvent() {
		return ((m_eventmask & UPDATE_EVENT) > 0);
	}

	/**
	 * Sets the name of the trigger
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * Sets the key used to indentify the procedure in the database
	 */
	public void setProcedureKey(Object key) {
		m_procedureKey = key;
	}

	/**
	 * Sets the id of the table that this trigger is bound to
	 */
	public void setTableId(TableId tableId) {
		m_tableid = tableId;
	}

	/**
	 * Sets if the trigger is fired on a DELETE
	 */
	public void setDelete(boolean delete) {
		if (delete)
			m_eventmask |= DELETE_EVENT;
		else
			m_eventmask &= (~DELETE_EVENT);
	}

	/**
	 * Sets when the trigger is fired (BEFORE or AFTER)
	 */
	public void setBefore(boolean before) {
		if (before)
			m_eventmask |= BEFORE_EVENT;
		else
			m_eventmask &= (~BEFORE_EVENT);
	}

	/**
	 * Sets the comma-separated list of function args for the trigger. NOTE:
	 * this is ONLY used when creating a trigger.
	 */
	public void setFunctionArgs(String args) {
		m_procargs = args;
	}

	/**
	 * Sets the name of the function for the trigger. NOTE: this is ONLY used
	 * when creating a trigger.
	 */
	public void setFunctionName(String funcname) {
		m_procname = funcname;
	}

	/**
	 * Sets if the trigger is fired on an INSERT
	 */
	public void setInsert(boolean insert) {
		if (insert)
			m_eventmask |= INSERT_EVENT;
		else
			m_eventmask &= (~INSERT_EVENT);
	}

	/**
	 * Sets if the trigger is fired on an UPDATE
	 */
	public void setUpdate(boolean update) {
		if (update)
			m_eventmask |= UPDATE_EVENT;
		else
			m_eventmask &= (~UPDATE_EVENT);
	}

}
