package com.jeta.abeille.gui.rules.postgres;

/**
 * This class represents a Rules in the database for a given table
 * 
 * @author Jeff Tassin
 */
public class Rule {
	/** the rule */
	private String m_name;

	/** the actual SQL for the rule */
	private String m_expression;

	/**
	 * ctor
	 */
	public Rule() {

	}

	/**
	 * ctor
	 */
	public Rule(String name, String ruleExpression) {
		m_name = name;
		m_expression = ruleExpression;
	}

	/**
	 * @return the expression for this rule
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

}
