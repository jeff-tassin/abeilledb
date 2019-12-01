/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import java.util.Collection;

import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.UIDirector;

public interface WindowDelegate extends JETAContainer {
	public void dispose();

	public Rectangle getBounds();

	/** JETAContainer implementation */

	public void enableComponent(String commandId, boolean bEnable);

	public Component getComponentByName(String compName);

	public Collection getComponentsByName(String compName);

	public UIDirector getUIDirector();

	public TSInternalFrame getTSInternalFrame();

	public Container getContentPane();

	public Dimension getPreferredSize();

	public MenuTemplate getMenuTemplate();

	public TSToolBarTemplate getToolBarTemplate();

	public JToolBar getToolBar();

	public int getWidth();

	public int getHeight();

	public int getX();

	public int getY();

	public Point getLocationOnScreen();

	public String getTitle();

	public boolean isVisible();

	public void pack();

	public void repaint();

	public void revalidate();

	public void setFrameIcon(Icon icon);

	public void setTitle(String title);

	public void setVisible(boolean bvis);

	public void setWindowSize(int width, int height);

	public void setWindowLocation(int x, int y);

	public void setWindowBounds(int x, int y, int width, int height);

	public void updateUI();

	public void setUIDirector(UIDirector uidirector);
}
