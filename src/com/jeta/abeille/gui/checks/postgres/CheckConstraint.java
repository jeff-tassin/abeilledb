package com.jeta.abeille.gui.checks.postgres;

import com.jeta.abeille.database.model.TableId;

/**
 * This class represents a CheckConstraint in the database for a given table
 * 
 * @author Jeff Tassin
 */
public class CheckConstraint {
	/** the check name */
	private String m_name;

	/** the check expression */
	private String m_expression;

	/** the table id this constraint is assigned to */
	private TableId m_tableid;

	/**
	 * ctor
	 */
	public CheckConstraint(TableId tableid) {
		m_tableid = tableid;
	}

	/**
	 * ctor
	 */
	public CheckConstraint(TableId tableid, String name, String expression) {
		m_tableid = tableid;
		m_name = name;
		m_expression = expression;
	}

	/**
	 * @return the expression for this constraint
	 */
	public String getExpression() {
		return m_expression;
	}

	/**
	 * @return the name for this constraint
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return the table id this check is assigned to
	 */
	public TableId getTableId() {
		return m_tableid;
	}
}
