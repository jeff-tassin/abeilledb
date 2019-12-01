package com.jeta.plugins.abeille.oracle.gui.constraints;

import java.sql.Timestamp;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a constraint in an Oracle table or view
 * 
 * @author Jeff Tassin
 */
public class OracleConstraint {
	/**
	 * The name for this trigger
	 */
	private String m_name;

	/**
	 * The statement(s) executed when the trigger fires
	 */
	private String m_definition;

	/**
	 * The last modified time
	 */
	private Timestamp m_lastmodified;

	/**
	 * The type of constraint C (check constraint on a table) U (unique key) V
	 * (with check option, on a view) O (with read only, on a view)
	 */
	public char m_type;

	/**
	 * ctor
	 */
	public OracleConstraint(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public String getDefinition() {
		return m_definition;
	}

	public Timestamp getLastModified() {
		return m_lastmodified;
	}

	/**
	 * @return the type of constraint
	 */
	public char getType() {
		return m_type;
	}

	/**
	 * @return a string description that describes the constraint type C (check
	 *         constraint on a table) U (unique key) V (with check option, on a
	 *         view) O (with read only, on a view)
	 */
	public static String getTypeDescription(char ctype) {
		if (ctype == 'C')
			return DbUtils.I18N_CHECK;
		else if (ctype == 'U')
			return DbUtils.I18N_UNIQUE;
		else if (ctype == 'V')
			return I18N.getLocalizedMessage("With Check Option");
		else if (ctype == 'O')
			return I18N.getLocalizedMessage("Read Only");
		else
			return I18N.getLocalizedMessage("Unknown");
	}

	/*
	 * --------------------------------------------------------------------------
	 * -
	 */
	public void setName(String tname) {
		m_name = tname;
	}

	public void setDefinition(String def) {
		m_definition = def;
	}

	public void setLastModified(Timestamp ts) {
		m_lastmodified = ts;
	}

	/**
	 * Sets the type of constraint
	 */
	public void setType(char ctype) {
		m_type = ctype;
	}
}
