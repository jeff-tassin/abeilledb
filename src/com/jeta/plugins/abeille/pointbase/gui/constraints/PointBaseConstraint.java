package com.jeta.plugins.abeille.pointbase.gui.constraints;

import java.sql.Timestamp;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a constraint in an PointBase table or view
 * 
 * @author Jeff Tassin
 */
public class PointBaseConstraint {
	/**
	 * The name for this check
	 */
	private String m_name;

	/**
	 * The statement(s) executed when the trigger fires
	 */
	private String m_definition;

	/**
	 * ctor
	 */
	public PointBaseConstraint() {

	}

	public String getName() {
		return m_name;
	}

	public String getDefinition() {
		return m_definition;
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

}
