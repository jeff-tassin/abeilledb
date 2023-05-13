/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.split;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * Layout manager for the layered pane. We have this layout so that we can
 * resize the split pane, but not change the position of the split thumb (which
 * is on a different layer).
 */
public class SplitLayoutManager implements LayoutManager {
	private CustomSplitPane m_split;

	public SplitLayoutManager(CustomSplitPane split) {
		m_split = split;
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public void layoutContainer(Container parent) {
		m_split.setSize(parent.getSize());
	}

	public Dimension minimumLayoutSize(Container parent) {
		return parent.getSize();
	}

	public Dimension preferredLayoutSize(Container parent) {
		return parent.getSize();
	}

	public void removeLayoutComponent(Component comp) {
	}

}
