package com.jeta.abeille.query;

import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TableId;

/**
 * A path link is basically a database link but it is agnostic about the actual
 * direction. We keep a psuedo direction here only for our analysis. But keep in
 * mind the the direction here might be reversed from that in the actual link.
 * 
 * @author Jeff Tassin
 */
public class PathLink implements Comparable {
	/** the actual link that this path is based on */
	private Link m_link;

	/**
	 * the start table in this link. This corresponds to the direction of our
	 * search algorithm and NOT the actual link direction. Note that this could
	 * easily be reversed from the link
	 */
	private TableId m_startid;

	/** the end table in this link */
	private TableId m_endid;

	/**
	 * ctor
	 */
	public PathLink(TableId startid, TableId endid, Link link) {
		m_startid = startid;
		m_endid = endid;
		m_link = link;
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object obj) {
		if (obj == this) {
			return 0;
		}
		if (obj instanceof PathLink) {
			return m_link.compareTo(((PathLink) obj).m_link);
		} else if (obj instanceof Link) {
			return m_link.compareTo(obj);
		} else
			return -1;
	}

	/**
	 * Override Object implementation so we can test against our internal link
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof PathLink) {
			return m_link.equals(((PathLink) obj).m_link);
		} else if (obj instanceof Link) {
			return m_link.equals(obj);
		} else
			return false;

	}

	/**
	 * @return the underlying link
	 */
	public Link getLink() {
		return m_link;
	}

	/**
	 * @return the end table in this link
	 */
	public TableId getEndId() {
		return m_endid;
	}

	/**
	 * @return the start table in this link
	 */
	public TableId getStartId() {
		return m_startid;
	}

	public void print() {
		System.out.print(m_startid + " -> " + m_endid);
	}
}
