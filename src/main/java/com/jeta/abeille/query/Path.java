package com.jeta.abeille.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.Link;

/**
 * This class is defines a single, linear path in a given query graph.
 * 
 * @author Jeff Tassin
 */
public class Path implements Cloneable {
	/** the set of PathLink that make up this path */
	private LinkedList m_links = new LinkedList();

	/**
	 * Adds the given link to this path
	 */
	public void add(PathLink link) {
		m_links.add(link);
	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		Path p = new Path();
		p.m_links = (LinkedList) m_links.clone();
		return p;
	}

	/**
	 * @return true if the given table is in the path
	 */
	public boolean contains(Link link) {
		if (link == null)
			return false;

		Iterator iter = m_links.iterator();
		while (iter.hasNext()) {
			PathLink plink = (PathLink) iter.next();
			if (plink.equals(link))
				return true;
		}
		return false;
	}

	/**
	 * @return the first table in the path. Null is returned if there are no
	 *         tables in the path
	 */
	public PathLink getFirst() {
		if (m_links.size() > 0)
			return (PathLink) m_links.getFirst();
		else
			return null;
	}

	/**
	 * @return the last table in the path. Null is returned if there are no
	 *         tables in the path
	 */
	public PathLink getLast() {
		if (m_links.size() > 0)
			return (PathLink) m_links.getLast();
		else
			return null;
	}

	/**
	 * @return the set of links that defines this path
	 */
	public Collection getLinks() {
		return m_links;
	}

	/**
	 * @return the number of tables in the path
	 */
	public int size() {
		return m_links.size();
	}

	/**
	 * Prints this path
	 */
	public void print() {
		System.out.print("Path: ");
		Iterator iter = m_links.iterator();
		while (iter.hasNext()) {
			PathLink plink = (PathLink) iter.next();
			plink.print();
			if (iter.hasNext())
				System.out.print("  --->  ");
		}
		System.out.println();
	}

	/**
	 * Removes the first table in this path
	 */
	public void removeFirst() {
		if (m_links.size() > 0)
			m_links.removeFirst();
	}

	/**
	 * Removes the last table in this path
	 */
	public void removeLast() {
		if (m_links.size() > 0)
			m_links.removeLast();
	}

}
