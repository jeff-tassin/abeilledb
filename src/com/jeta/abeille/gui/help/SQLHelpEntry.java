package com.jeta.abeille.gui.help;

public class SQLHelpEntry implements Comparable {
	private String m_topicid;
	private String m_displayLabel;
	private String m_url;

	public SQLHelpEntry(String topicId, String displayLabel, String url) {
		m_topicid = topicId;
		m_displayLabel = displayLabel;
		m_url = url;
	}

	public SQLHelpEntry(SQLReferenceType reftype, String url) {
		this(reftype.getTopicId(), reftype.getTopicId(), url);
	}

	public int compareTo(Object obj) {
		if (obj instanceof SQLHelpEntry) {
			SQLHelpEntry entry = (SQLHelpEntry) obj;
			return m_displayLabel.compareTo(entry.m_displayLabel);
		} else
			return -1;
	}

	public String getTopicId() {
		return m_topicid;
	}

	public String getDisplayLabel() {
		return m_displayLabel;
	}

	public String getUrl() {
		return m_url;
	}

	public int hashCode() {
		return m_topicid.hashCode();
	}

	public String toString() {
		return m_displayLabel;
	}
}
