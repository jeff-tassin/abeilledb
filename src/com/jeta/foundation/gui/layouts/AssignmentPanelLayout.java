/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import java.awt.LayoutManager;

/**
 * This is the layout manager that is used to layout two list boxes (left and
 * right). This is used to build assignment GUI's where the user can
 * select/assign items from one list to another. The left and right components
 * are used to hold the lists. The middle component is used for the container
 * that contains the assignment buttons. [list][button panel][list]
 */
public class AssignmentPanelLayout implements LayoutManager {
	private Dimension m_min = new Dimension(50, 50);

	private Component m_left;
	private Component m_middle;
	private Component m_right;

	public AssignmentPanelLayout(Component left, Component middle, Component right) {
		m_left = left;
		m_middle = middle;
		m_right = right;
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();

		Dimension sz = parent.getSize();
		sz.width = sz.width - insets.left - insets.right;
		sz.height = sz.height - insets.top - insets.bottom;

		int y = insets.top;

		Dimension md = m_middle.getPreferredSize();

		int leftwidth = (sz.width - md.width) / 2;

		m_left.setSize(leftwidth, sz.height);
		m_left.setLocation(insets.left, y);

		m_middle.setSize(md.width, sz.height);
		m_middle.setLocation(insets.left + leftwidth, (sz.height - md.height) / 2);

		m_right.setSize(leftwidth, sz.height);
		m_right.setLocation(insets.left + leftwidth + md.width, y);

	}

	public Dimension minimumLayoutSize(Container parent) {
		return m_min;
	}

	public Dimension preferredLayoutSize(Container parent) {
		return m_min;
	}

	public void removeLayoutComponent(Component comp) {
	}
}
