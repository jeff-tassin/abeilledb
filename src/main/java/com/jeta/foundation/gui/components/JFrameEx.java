/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.lang.ref.WeakReference;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.gui.framework.UIDirector;

import com.jeta.open.support.ComponentFinder;
import com.jeta.open.support.CompositeComponentFinder;
import com.jeta.open.support.DefaultComponentFinder;

/**
 * We extend JFrame here simply to have a way to navigate to our TSInternalFrame
 * container.
 * 
 * @author Jeff Tassin
 */
public class JFrameEx extends JETAFrame implements WindowDelegate {
	private WeakReference m_frameref;

	/** the preferred size for this frame */
	private Dimension m_preferredsize = new Dimension(600, 400);

	public JFrameEx(TSInternalFrame wrapper, String caption) {
		super(caption);
		m_frameref = new WeakReference(wrapper);
	}

	public void finalize() throws Throwable {
		super.finalize();
		if (TSUtils.isDebug()) {
			TSUtils.printMessage("JFrameEx.finalize: " + getTitle());
		}
	}

	public TSInternalFrame getTSInternalFrame() {
		return (TSInternalFrame) m_frameref.get();
	}

	public Dimension getPreferredSize() {
		return m_preferredsize;
	}

	public boolean isMinimum() {
		return isIcon();
	}

	public boolean isIcon() {
		return ((getExtendedState() & JFrameEx.ICONIFIED) != 0);
	}

	public boolean isMaximum() {
		return ((getExtendedState() & JFrameEx.MAXIMIZED_BOTH) != 0);
	}

	public boolean isVisible() {
		return isShowing();
	}

	public void setWindowSize(int width, int height) {
		setSize(width, height);
	}

	public void setWindowLocation(int x, int y) {
		setLocation(x, y);
	}

	public void setWindowBounds(int x, int y, int width, int height) {
		setBounds(x, y, width, height);
	}

	/**
	 * Sets the frame icon
	 */
	public void setFrameIcon(Icon icon) {
		if (icon != null)
			setIconImage(((ImageIcon) icon).getImage());
	}

	public void setIcon(boolean bmin) {
		try {
			if (bmin) {
				setExtendedState(JFrameEx.ICONIFIED);
			} else {
				setExtendedState(JFrameEx.NORMAL);
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	public void setMaximum(boolean bval) {
		try {
			setExtendedState(JFrameEx.MAXIMIZED_BOTH);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	public void setPreferredSize(Dimension d) {
		m_preferredsize = d;
	}

	public void setSelected(boolean bselected) {
		try {
			if (bselected) {
				show();
				toFront();
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Override the show method so we can restore the frame if it is minimized
	 */
	public void show() {
		if (isIcon())
			setIcon(false);

		super.show();
	}

}
