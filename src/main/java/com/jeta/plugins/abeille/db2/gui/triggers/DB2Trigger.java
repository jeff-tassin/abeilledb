package com.jeta.plugins.abeille.db2.gui.triggers;

import java.sql.Timestamp;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a trigger in DB2
 * 
 * @author Jeff Tassin
 */
public class DB2Trigger {
	/**
	 * The name for this trigger
	 */
	private String m_name;

	/**
	 * Authorization ID under which the trigger was defined.
	 */
	private String m_definer;

	/**
	 * The full text of the CREATE TRIGGER statement, exactly as typed.
	 */
	private String m_definition;

	/**
	 * Event that fires the trigger. I = Insert D = Delete U = Update
	 */
	private char m_event;

	/**
	 * Function path at the time the trigger was defined. Used in resolving
	 * functions and types.
	 */
	private String m_functionpath;

	/**
	 * Trigger is executed once per: S = Statement R = Row
	 */
	private char m_granularity;

	/**
	 * Time at which the trigger was defined. Used in resolving functions and
	 * types.
	 */
	private Timestamp m_timestamp;

	/**
	 * Flag that indicates if this trigger is valid or inoperative(and must be
	 * re-created)
	 */
	private boolean m_valid;

	/**
	 * Time when triggered actions are applied to the base table, relative to
	 * the event that fired the trigger: A = Trigger applied after event B =
	 * Trigger applied before event I = Trigger applied instead of event
	 */
	private char m_when;

	/**
	 * ctor
	 */
	public DB2Trigger(String triggerName) {
		m_name = triggerName;
	}

	/**
	 * Time when triggered actions are applied to the base table, relative to
	 * the event that fired the trigger: A = AFTER B = BEFORE I = INSTEAD
	 */
	public static String getWhenDescription(char when) {
		if (when == 'A')
			return DbUtils.I18N_AFTER;
		else if (when == 'B')
			return DbUtils.I18N_BEFORE;
		else if (when == 'I')
			return DbUtils.I18N_INSTEAD;
		else
			return I18N.getLocalizedMessage("Unknown");
	}

	/**
	 * Event that fires the trigger. I = Insert D = Delete U = Update
	 */
	public static String getEventDescription(char event) {
		if (event == 'I')
			return DbUtils.I18N_INSERT;
		else if (event == 'D')
			return DbUtils.I18N_DELETE;
		else if (event == 'U')
			return DbUtils.I18N_UPDATE;
		else
			return I18N.getLocalizedMessage("Unknown");
	}

	/**
	 * Trigger is executed once per: S = Statement R = Row
	 */
	public static String getGranularityDescription(char gran) {
		if (gran == 'S')
			return DbUtils.I18N_STATEMENT;
		else if (gran == 'R')
			return DbUtils.I18N_ROW;
		else
			return I18N.getLocalizedMessage("Unknown");
	}

	/**
	 * The name of the user who defined the trigger
	 */
	public String getDefiner() {
		return m_definer;
	}

	/**
	 * Sets the SQL definition for this trigger. This is the actual CREATE
	 * TRIGGER ... statement used to create this trigger.
	 */
	public String getDefinition() {
		return m_definition;
	}

	public char getEvent() {
		return m_event;
	}

	public String getFunctionPath() {
		return m_functionpath;
	}

	public char getGranularity() {
		return m_granularity;
	}

	/**
	 * The name of this trigger
	 */
	public String getName() {
		return m_name;
	}

	public Timestamp getTimestamp() {
		return m_timestamp;
	}

	public boolean isValid() {
		return m_valid;
	}

	public char getWhen() {
		return m_when;
	}

	/**
	 * The name of the user who defined the trigger
	 */
	public void setDefiner(String definer) {
		m_definer = definer;
	}

	/**
	 * Sets the SQL definition for this trigger. This is the actual CREATE
	 * TRIGGER ... statement used to create this trigger.
	 */
	public void setDefinition(String def) {
		m_definition = def;
	}

	public void setEvent(char event) {
		m_event = event;
	}

	public void setFunctionPath(String func_path) {
		m_functionpath = func_path;
	}

	public void setGranularity(char gran) {
		m_granularity = gran;
	}

	public void setTimestamp(Timestamp ts) {
		m_timestamp = ts;
	}

	public void setValid(boolean bvalid) {
		m_valid = bvalid;
	}

	public void setWhen(char twhen) {
		m_when = twhen;
	}
}
