/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.awt.BorderLayout;
import javax.swing.tree.DefaultMutableTreeNode;

import com.jeta.foundation.gui.components.TSInternalFrame;

/**
 * This is the Frame window for working with Java objects.
 * 
 * @author Jeff Tassin
 */
public class ObjectFrame extends TSInternalFrame {

	/**
	 * Constructor
	 */
	public ObjectFrame() {
		super("");
	}

	/**
    *
    */
	protected void createMenu() {

	}

	/**
    *
    */
	protected void createToolBar() {

	}

	/**
	 * Creates the menu, toolbar, and content window for this frame.
	 * 
	 * @param params
	 *            a 1 length array that contains the Object to display in this
	 *            frame
	 */
	public void initializeModel(Object[] params) {
		createMenu();
		createToolBar();

		ObjectModel model = new ObjectModel(new String());
		ObjectView panel = new ObjectView(model);
		getContentPane().add(panel, BorderLayout.CENTER);

		panel.expandNode((DefaultMutableTreeNode) model.getRoot(), false);

	}
}
