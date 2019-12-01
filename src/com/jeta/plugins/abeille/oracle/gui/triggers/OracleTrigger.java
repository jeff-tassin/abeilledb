package com.jeta.plugins.abeille.oracle.gui.triggers;

import java.sql.Timestamp;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a trigger in Oracle
 * 
 * @author Jeff Tassin
 */
public class OracleTrigger {
	/**
	 * The name for this trigger
	 */
	private String m_name;

	/**
	 * The owner of this trigger
	 */
	private String m_owner;

	/**
	 * In oracle this is the TRIGGER_TYPE: BEFORE STATEMENT, BEFORE EACH ROW,
	 * BEFORE EVENT, AFTER STATEMENT, AFTER EACH ROW, and AFTER EVENT
	 */
	private String m_when;

	/**
	 * The triggering event
	 */
	private String m_event;

	/**
	 * Names used for referencing OLD and NEW column values from within the
	 * trigger
	 */
	private String m_referencing;

	/**
	 * An expression that must evaulate to true for the trigger to execute
	 */
	private String m_when_clause;

	/**
	 * Set to true if the trigger is enabled
	 */
	private boolean m_enabled;

	/**
	 * The action type of the trigger body (CALL or PL/SQL)
	 */
	private String m_action_type;

	/**
	 * The statement(s) executed when the trigger fires
	 */
	private String m_definition;

	/**
	 * ctor
	 */
	public OracleTrigger(String triggerName) {
		m_name = triggerName;
	}

	public String getName() {
		return m_name;
	}

	public String getOwner() {
		return m_owner;
	}

	public String getWhen() {
		return m_when;
	}

	public String getEvent() {
		return m_event;
	}

	public String getReferencing() {
		return m_referencing;
	}

	public String getWhenClause() {
		return m_when_clause;
	}

	public String getActionType() {
		return m_action_type;
	}

	public String getDefinition() {
		return m_definition;
	}

	public boolean isEnabled() {
		return m_enabled;
	}

	/*
	 * --------------------------------------------------------------------------
	 * -
	 */

	public void setName(String tname) {
		m_name = tname;
	}

	public void setOwner(String towner) {
		m_owner = towner;
	}

	public void setWhen(String when) {
		m_when = when;
	}

	public void setEvent(String event) {
		m_event = event;
	}

	public void setReferencing(String ref) {
		m_referencing = ref;
	}

	public void setWhenClause(String when_clause) {
		m_when_clause = when_clause;
	}

	public void setEnabled(boolean enabled) {
		m_enabled = enabled;
	}

	public void setActionType(String action_type) {
		m_action_type = action_type;
	}

	public void setDefinition(String def) {
		m_definition = def;
	}

}
