/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.utils;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is used to implement an unmodifiable collection. Any caller can
 * get an instance of this collection (a singleton) when they need to return an
 * empty unmodifiable collection.
 * 
 * @author Jeff Tassin
 */
public class EmptyCollection extends AbstractCollection {
	private static EmptyCollection m_singleton = new EmptyCollection();
	private static EmptyIterator m_iterator = new EmptyIterator();

	private EmptyCollection() {
		// no op
	}

	public static Collection getInstance() {
		return m_singleton;
	}

	public Iterator iterator() {
		return m_iterator;
	}

	public int size() {
		return 0;
	}

	private static class EmptyIterator implements Iterator {
		public boolean hasNext() {
			return false;
		}

		public Object next() {
			throw new NoSuchElementException();
		}

		public void remove() {
			// no op
		}
	}
}
