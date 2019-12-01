/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * This interface is used to provide specialized implementations for building
 * toolbars. It allows builders to be developed to manage complex toolbars and
 * share those toolbars with various classes.
 * 
 * @author Jeff Tassin
 */
public interface TSToolBarBuilder {
	public JButton createToolBarButton(String cmdId, String imageName, String toolTip);

	public TSToolBarTemplate getToolBarTemplate();

	public JToolBar getToolBar();
}
