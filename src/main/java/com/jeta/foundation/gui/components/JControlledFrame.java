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
 * We extend JFrameEx to support full component finders for the content pane.
 * 
 * @author Jeff Tassin
 */
public class JControlledFrame extends JFrameEx {
	public JControlledFrame(TSInternalFrame wrapper, String caption) {
		super(wrapper, caption);
	}

	/**
	 * Creates a component finder that is used to locate child components in
	 * this panel by name.
	 */
	protected ComponentFinder createComponentFinder() {
		CompositeComponentFinder finder = new CompositeComponentFinder();
		finder.add(new DefaultComponentFinder(getJMenuBar()));
		finder.add(new DefaultComponentFinder(getToolBar()));
		finder.add(new DefaultComponentFinder(getContentPane()));
		return finder;
	}

}
