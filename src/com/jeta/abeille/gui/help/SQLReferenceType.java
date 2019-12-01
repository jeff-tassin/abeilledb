package com.jeta.abeille.gui.help;

public class SQLReferenceType {
	private String m_topicid;

	public static final SQLReferenceType ALTER_TABLE_COLUMNS = new SQLReferenceType("columns");
	public static final SQLReferenceType CHECK_CONSTRAINTS = new SQLReferenceType("checks");
	public static final SQLReferenceType FOREIGN_KEYS = new SQLReferenceType("foreign keys");
	public static final SQLReferenceType RULES = new SQLReferenceType("rules");
	public static final SQLReferenceType FUNCTIONS = new SQLReferenceType("functions");
	public static final SQLReferenceType TRIGGERS = new SQLReferenceType("triggers");

	/**
	 * ctor
	 */
	private SQLReferenceType(String name) {
		m_topicid = name;
	}

	public int hashCode() {
		return m_topicid.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj instanceof SQLReferenceType) {
			SQLReferenceType stype = (SQLReferenceType) obj;
			return m_topicid.equals(stype.m_topicid);
		} else
			return false;
	}

	public String getTopicId() {
		return m_topicid;
	}

	public String toString() {
		return m_topicid;
	}
}
