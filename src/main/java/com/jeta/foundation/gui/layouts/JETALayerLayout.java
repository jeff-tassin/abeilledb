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
 * This layout manager is used for JLayeredPanes. It simply makes all components
 * in the container the width/height of the container. The top most components
 * must not be opaque.
 * 
 * @author Jeff Tassin
 */
public class JETALayerLayout implements LayoutManager {
	/**
	 * @param name
	 * @param comp
	 */
	public void addLayoutComponent(String name, Component comp) {
	}

	/**
	 * @param parent
	 */
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		for (int index = 0; index < parent.getComponentCount(); index++) {
			Component comp = parent.getComponent(index);
			comp.setLocation(insets.left, insets.top);
			comp.setSize(parent.getWidth() - insets.left - insets.right, parent.getHeight() - insets.top
					- insets.bottom);
		}
	}

	/**
	 * @param parent
	 * @return
	 */
	public Dimension minimumLayoutSize(Container parent) {
		Insets insets = parent.getInsets();
		int min_width = 0;
		int min_height = 0;
		for (int index = 0; index < parent.getComponentCount(); index++) {
			Component comp = parent.getComponent(index);
			Dimension minsz = comp.getMinimumSize();
			min_width = Math.max(minsz.width, min_width);
			min_height = Math.max(minsz.height, min_height);
		}
		return new Dimension(min_width + insets.left + insets.right + 2, min_height + insets.top + insets.bottom + 2);
	}

	/**
	 * @param parent
	 * @return
	 */
	public Dimension minimumLayoutSizeOld(Container parent) {
		Insets insets = parent.getInsets();
		int min_width = parent.getWidth();
		int min_height = parent.getHeight();
		for (int index = 0; index < parent.getComponentCount(); index++) {
			Component comp = parent.getComponent(index);
			if (min_width > comp.getWidth())
				min_width = comp.getWidth();

			if (min_height > comp.getHeight())
				min_height = comp.getHeight();
		}

		return new Dimension(min_width + insets.left + insets.right + 2, min_height + insets.top + insets.bottom + 2);
	}

	/**
	 * @param parent
	 * @return
	 */
	public Dimension preferredLayoutSize(Container parent) {
		Insets insets = parent.getInsets();
		int width = 0;
		int height = 0;
		for (int index = 0; index < parent.getComponentCount(); index++) {
			Component comp = parent.getComponent(index);
			Dimension sz = comp.getPreferredSize();
			width = Math.max(width, sz.width);
			height = Math.max(height, sz.height);
		}
		return new Dimension(width + insets.left + insets.right + 2, height + insets.top + insets.bottom + 2);
	}

	/**
	 * @param comp
	 */
	public void removeLayoutComponent(Component comp) {
	}

}
