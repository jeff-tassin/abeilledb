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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

public class JFrameManager implements JETAFrameManager {
	private ArrayList m_frames = new ArrayList();

	protected JFrameManager() {

	}

	public JFrameManager(TSWorkspaceFrame wsframe) {
		JPanel emptypanel = new JPanel() {
			public Dimension getPreferredSize() {
				return new Dimension(100, 1);
			}
		};
		wsframe.getContentPane().add(emptypanel, BorderLayout.CENTER);
	}

	/**
	 * Called when the frame is activated.
	 */
	public void activateFrame(TSInternalFrame iframe) {

	}

	/**
	 * Adds the window to the list of windows owned by this manager.
	 */
	public void addWindow(TSInternalFrame iframe) {
		if (!m_frames.contains(iframe))
			m_frames.add(iframe);
	}

	public WindowDelegate createWindow(TSInternalFrame frame, String caption) {
		return new JFrameEx(frame, caption);
	}

	public TSInternalFrame[] getAllFrames() {
		return (TSInternalFrame[]) m_frames.toArray(new TSInternalFrame[0]);
	}

	/**
	 * @return the frame at the given tab index. Null is returned if the index
	 *         is invalid
	 */
	public TSInternalFrame getFrameAt(int index) {
		return null;
	}

	public Dimension getWorkspaceSize() {
		return java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	}

	/**
	 * Removes the window from this workspace
	 */
	public void removeWindow(TSInternalFrame iframe) {
		assert (iframe != null);
		JFrameEx jframe = (JFrameEx) iframe.getDelegate();
		m_frames.remove(iframe);
	}

	/**
	 * Moves the frame to the top of the Z-order and sets it selected. Deselects
	 * all other frames
	 */
	public void selectFrame(TSInternalFrame frame) {
		if (frame == null) {
			assert (false);
			return;
		}

		JFrameEx jframe = (JFrameEx) frame.getDelegate();
		jframe.show();
	}

	/**
	 * Updates the workspace if a title has changed in a frame. This is mainly
	 * for tabbed panes that need to update their tab titles.
	 */
	public void updateTitle(TSInternalFrame iframe) {
		// no op
	}

	public void updateUI() {

	}
}
