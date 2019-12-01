package com.jeta.plugins.abeille.db2.gui.checks;

import java.sql.Timestamp;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a check constraint in DB2
 * 
 * @author Jeff Tassin
 */
public class DB2CheckConstraint {
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
	 * Function path at the time the trigger was defined. Used in resolving
	 * functions and types.
	 */
	private String m_functionpath;

	/**
	 * Time at which the trigger was defined. Used in resolving functions and
	 * types.
	 */
	private Timestamp m_timestamp;

	/**
	 * ctor
	 */
	public DB2CheckConstraint(String triggerName) {
		m_name = triggerName;
	}

	/**
	 * The name of the user who defined the trigger
	 */
	public String getDefiner() {
		return m_definer;
	}

	/**
	 * Sets the SQL definition for this constraint.
	 * 
	 */
	public String getDefinition() {
		return m_definition;
	}

	public String getFunctionPath() {
		return m_functionpath;
	}

	/**
	 * The name of this constraint
	 */
	public String getName() {
		return m_name;
	}

	public Timestamp getTimestamp() {
		return m_timestamp;
	}

	/**
	 * The name of the user who defined the trigger
	 */
	public void setDefiner(String definer) {
		m_definer = definer;
	}

	/**
	 * Sets the SQL definition for this constraint
	 */
	public void setDefinition(String def) {
		m_definition = def;
	}

	public void setFunctionPath(String func_path) {
		m_functionpath = func_path;
	}

	public void setTimestamp(Timestamp ts) {
		m_timestamp = ts;
	}

}
