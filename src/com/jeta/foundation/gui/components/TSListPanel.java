/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;

/**
 * This is a JPanel that contains a JList with a table-like heading.
 * 
 * @author Jeff Tassin
 */
public class TSListPanel extends TSHeaderPanel {
	private JList m_list; // the list

	public TSListPanel() {
		super(new JList(new DefaultListModel()));
		m_list = (JList) getComponent();
		JScrollPane scrollpane = new JScrollPane(m_list);
		initialize(scrollpane);
	}

	/**
	 * @return the underlying list component
	 */
	public JList getJList() {
		return m_list;
	}

	/**
	 * @return the underlying list model
	 */
	public ListModel getModel() {
		return m_list.getModel();
	}
}
