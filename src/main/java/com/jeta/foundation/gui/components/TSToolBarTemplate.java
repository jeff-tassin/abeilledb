/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Component;
import javax.swing.JToolBar.Separator;
import javax.swing.JToolBar;
import java.util.ArrayList;
import javax.swing.filechooser.FileFilter;
import java.util.Hashtable;
import java.io.File;
import javax.swing.JComponent;
import com.jeta.foundation.gui.components.GenericFileFilter;

public class TSToolBarTemplate {
	private ArrayList m_components = new ArrayList();

	public TSToolBarTemplate() {
	}

	public void add(Component c) {
		m_components.add(c);
	}

	public void clear() {
		m_components.clear();
	}

	/**
	 * Inserts a component before the specified index Note that indexes are zero
	 * based.
	 * 
	 * @param comp
	 *            the component to insert.
	 * @param index
	 *            the index before which to insert the component
	 */
	public void add(JComponent comp, int index) {
		m_components.add(index, comp);
	}

	public void addSeparator() {
		JToolBar.Separator sep = new JToolBar.Separator(null);
		sep.setOrientation(javax.swing.JSeparator.VERTICAL);
		add(sep);
	}

	public int getComponentCount() {
		return m_components.size();
	}

	public Component getComponentAt(int index) {
		return (Component) m_components.get(index);
	}

}
