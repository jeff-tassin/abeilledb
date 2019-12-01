/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

public interface JETAFrameManager {

	/**
	 * Called when the frame is activated.
	 */
	public void activateFrame(TSInternalFrame iframe);

	/**
	 * Adds the window to the desktop
	 */
	public void addWindow(TSInternalFrame iframe);

	public WindowDelegate createWindow(TSInternalFrame frame, String caption);

	public TSInternalFrame[] getAllFrames();

	/**
	 * @return the frame at the given tab index. Null is returned if the index
	 *         is invalid
	 */
	public TSInternalFrame getFrameAt(int index);

	public Dimension getWorkspaceSize();

	/**
	 * Removes the window from this workspace. This does not dispose of the
	 * frame.
	 */
	public void removeWindow(TSInternalFrame iframe);

	/**
	 * Moves the frame to the top of the Z-order and sets it selected. Deselects
	 * all other frames
	 */
	public void selectFrame(TSInternalFrame frame);

	/**
	 * Updates the workspace if a title has changed in a frame. This is mainly
	 * for tabbed panes that need to update their tab titles.
	 */
	public void updateTitle(TSInternalFrame iframe);

	public void updateUI();

}
