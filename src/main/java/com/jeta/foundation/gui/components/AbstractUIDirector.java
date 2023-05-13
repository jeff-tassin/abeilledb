/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.lang.ref.WeakReference;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is used to keep a refrence to a view for UIDirectors. We do this
 * to faciliate garbage collection of views.
 * 
 * @author Jeff Tassin
 */
public abstract class AbstractUIDirector implements UIDirector {
	/**
	 * Keep a weak refrence to the view
	 */
	private WeakReference m_viewref;

	public AbstractUIDirector(Object view) {
		m_viewref = new WeakReference(view);
	}

	public Object getView() {
		return m_viewref.get();
	}

}
