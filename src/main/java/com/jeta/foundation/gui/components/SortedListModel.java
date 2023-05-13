/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import com.jeta.foundation.utils.TSUtils;

/**
 * A list model that sorts its items
 * 
 * @author Jeff Tassin
 */
public class SortedListModel extends AbstractListModel {
	/** the ordered set of data */
	private TreeSet m_model;

	/** a cache of the data so we can lookup by index */
	private Object[] m_cache = null;

	/**
	 * ctor. Standard behavior sorts items in a case insensitive order
	 */
	public SortedListModel() {
		m_model = new TreeSet(String.CASE_INSENSITIVE_ORDER);
	}

	/**
	 * ctor
	 */
	public SortedListModel(TreeSet set) {
		m_model = set;
	}

	public Comparator getComparator() {
		if (m_model == null)
			return null;
		else
			return m_model.comparator();
	}

	/**
	 * @return the number of items in this model
	 */
	public int getSize() {
		if (m_model == null)
			return 0;
		else
			return m_model.size();
	}

	/**
	 * This method is potentially very slow
	 * 
	 * @return the element at the given index
	 */
	public Object getElementAt(int index) {
		if (index < 0 || index >= m_model.size())
			return null;

		if (m_cache == null)
			m_cache = m_model.toArray();

		if (m_cache.length != m_model.size()) {
			TSUtils.printDebugMessage("Error.  SortedListModel  cache size != model size");
			m_cache = m_model.toArray();
		}

		return m_cache[index];
	}

	/**
	 * Adds an item to the list
	 */
	public void add(Object element) {
		if (m_model.add(element)) {
			m_cache = null;
			fireContentsChanged(this, 0, getSize());
		}
	}

	/**
	 * Adds a set of items to the list
	 */
	public void addAll(Object elements[]) {
		m_cache = null;

		Collection c = Arrays.asList(elements);
		m_model.addAll(c);
		fireContentsChanged(this, 0, getSize());
	}

	/**
	 * Adds a set of items to the list
	 */
	public void addAll(Collection elements) {
		m_cache = null;

		m_model.addAll(elements);
		fireContentsChanged(this, 0, getSize());
	}

	public void clear() {
		m_cache = null;

		m_model.clear();
		fireContentsChanged(this, 0, getSize());
	}

	public boolean contains(Object element) {
		return m_model.contains(element);
	}

	public boolean containsString(String value) {
		Iterator iter = m_model.iterator();
		while (iter.hasNext()) {
			String val = (String) iter.next().toString();
			if (val.compareToIgnoreCase(value) == 0)
				return true;
		}

		return false;
	}

	public Object firstElement() {
		// Return the appropriate element
		return m_model.first();
	}

	public Iterator iterator() {
		return m_model.iterator();
	}

	public Object lastElement() {
		// Return the appropriate element
		return m_model.last();
	}

	/**
	 * @return the underlying data
	 */
	TreeSet getModel() {
		return m_model;
	}

	public boolean removeElement(Object element) {
		boolean removed = m_model.remove(element);
		if (removed) {
			m_cache = null;

			fireContentsChanged(this, 0, getSize());
		}
		return removed;
	}

	/**
	 * Sets the comparator for the model
	 */
	public void setComparator(Comparator c) {
		m_cache = null;

		m_model = new TreeSet(c);
	}

	/**
	 * Sets the underlying data model
	 */
	public void setTreeSet(TreeSet treeset) {
		m_model = treeset;
		m_cache = null;
	}
}
