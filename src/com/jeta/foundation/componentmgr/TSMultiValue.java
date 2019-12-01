/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.componentmgr;

import java.util.ArrayList;

/**
 * This object can be used with the TSEvent to contain multiple values if
 * required by the event
 * 
 * @author Jeff Tassin
 */
public class TSMultiValue {
	private ArrayList m_list;

	/**
	 * ctor
	 */
	public TSMultiValue() {

	}

	/**
	 * ctor that puts two objects in the collection
	 */
	public TSMultiValue(Object obj1, Object obj2) {
		add(obj1);
		add(obj2);
	}

	/**
	 * Adds an object to this multi value
	 */
	public void add(Object obj) {
		if (m_list == null)
			m_list = new ArrayList();

		m_list.add(obj);
	}

	/**
	 * @return the number of items in the list
	 */
	public int getCount() {
		if (m_list == null)
			return 0;
		else
			return m_list.size();
	}

	/**
	 * @return the object at the specified index
	 */
	public Object getObjectAt(int index) {
		if (m_list == null)
			return null;
		else
			return m_list.get(index);
	}

}
