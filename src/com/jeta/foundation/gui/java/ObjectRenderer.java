/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;

public class ObjectRenderer extends JLabel implements TreeCellRenderer {
	public ObjectRenderer() {
		super("");
		setOpaque(true);
		setBackground(UIManager.getColor("Tree.background"));
		setFont(UIManager.getFont("Tree.font"));
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {

		if (sel) {
			setForeground(UIManager.getColor("Tree.selectionForeground"));
			setBackground(UIManager.getColor("Tree.selectionBackground"));
		} else {
			setForeground(UIManager.getColor("Tree.foreground"));
			setBackground(UIManager.getColor("Tree.background"));
		}

		DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) value;
		Object userobj = tnode.getUserObject();
		if (userobj instanceof JavaWrapper) {
			JavaWrapper wrapper = (JavaWrapper) userobj;
			if (wrapper.getTypeName() == null)
				setText(wrapper.getName());
			else
				setText(wrapper.getName() + " : " + wrapper.getTypeName());

			setIcon(wrapper.getIcon());
		} else {
			setText("");
			setIcon(null);
			setForeground(UIManager.getColor("Tree.foreground"));
			setBackground(UIManager.getColor("Tree.background"));
		}
		return this;
	}

	void initialize() {
		setLayout(new FlowLayout());
		// DefaultTreeCellRenderer m = new DefaultTreeCellRenderer();
	}

}
