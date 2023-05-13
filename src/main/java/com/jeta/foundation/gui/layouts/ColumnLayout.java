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
 * This is the layout manager that is used to layout components in a single
 * column (vertically) [comp1] [comp2] ... [compN] Each component is sized to
 * the width of the parent and aligned left. The component height is set to its
 * preferred height.
 * 
 * @author Jeff Tassin
 */
public class ColumnLayout implements LayoutManager {

	public ColumnLayout() {
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		Dimension sz = parent.getSize();
		int width = sz.width - insets.left - insets.right;
		int height = sz.height - insets.top - insets.bottom;

		int x = insets.left;
		int y = insets.top;
		for (int index = 0; index < parent.getComponentCount(); index++) {
			Component comp = parent.getComponent(index);
			Dimension d = comp.getPreferredSize();
			d.width = width;

			comp.setSize(d);
			comp.setLocation(x, y);

			y += d.height;
		}

	}

	public Dimension minimumLayoutSize(Container parent) {

		Insets insets = parent.getInsets();
		Dimension sz = parent.getSize();

		Dimension result = new Dimension();
		result.width = insets.left + insets.right;
		result.height = insets.top + insets.bottom;

		int max_width = 0;

		for (int index = 0; index < parent.getComponentCount(); index++) {
			Component comp = parent.getComponent(index);
			Dimension d = comp.getMinimumSize();
			if (max_width < d.width)
				max_width = d.width;
			result.height += d.height;
		}
		result.width += max_width;
		return result;
	}

	public Dimension preferredLayoutSize(Container parent) {
		Insets insets = parent.getInsets();
		Dimension sz = parent.getSize();

		Dimension result = new Dimension();
		result.width = insets.left + insets.right;
		result.height = insets.top + insets.bottom;

		int max_width = 0;

		for (int index = 0; index < parent.getComponentCount(); index++) {
			Component comp = parent.getComponent(index);
			Dimension d = comp.getPreferredSize();
			if (max_width < d.width)
				max_width = d.width;
			result.height += d.height;
		}
		result.width += max_width;
		return result;
	}

	public void removeLayoutComponent(Component comp) {
	}
}
