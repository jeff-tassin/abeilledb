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
import com.jeta.open.gui.framework.JETAPanel;

import com.jeta.open.support.ComponentFinder;
import com.jeta.open.support.CompositeComponentFinder;
import com.jeta.open.support.DefaultComponentFinder;

/**
 * @author Jeff Tassin
 */
public class JPanelFrame extends JETAPanel implements WindowDelegate {
	private WeakReference m_frameref;
	private String m_title;
	private JToolBar m_toolbar = null;
	private TSToolBarTemplate m_toolbarTemplate = new InternalFrameToolBarTemplate();

	private MenuTemplate m_menuTemplate = new InternalFrameMenuTemplate();
	private JPanel m_content = new JPanel(new BorderLayout());

	public JPanelFrame(TSInternalFrame wrapper, String caption) {
		m_frameref = new WeakReference(wrapper);
		m_title = caption;
		setLayout(new BorderLayout());
	}

	/**
	 * Creates a component finder that is used to locate child components in
	 * this panel by name.
	 */
	protected ComponentFinder createComponentFinder() {
		CompositeComponentFinder finder = new CompositeComponentFinder(new DefaultComponentFinder(this));
		/** must add menus here because they are not owned by this container */
		finder.add(m_menuTemplate.getComponentFinder());
		return finder;
	}

	public TSInternalFrame getTSInternalFrame() {
		return (TSInternalFrame) m_frameref.get();
	}

	public void dispose() {

	}

	public Container getContentPane() {
		return this;
	}

	public TSToolBarTemplate getToolBarTemplate() {
		return m_toolbarTemplate;
	}

	public MenuTemplate getMenuTemplate() {
		return m_menuTemplate;
	}

	public JToolBar getToolBar() {
		if (m_toolbar == null) {
			m_toolbar = new JToolBar();
			m_toolbar.setFloatable(false);
			JPanelFrame.this.add(m_toolbar, BorderLayout.NORTH);
		}
		return m_toolbar;
	}

	public String getTitle() {
		return m_title;
	}

	public boolean isIcon() {
		return false;
	}

	public boolean isMaximum() {
		return false;
	}

	public boolean isMinimum() {
		return false;
	}

	public void setWindowSize(int width, int height) {
		// ignore
	}

	public void setWindowLocation(int x, int y) {
		// ignore
	}

	public void setWindowBounds(int x, int y, int width, int height) {
		// ignore
	}

	/**
	 * Sets the window as maximized.
	 */
	public void setMaximum(boolean bval) {
	}

	public void setIcon(boolean bval) {
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public void show() {
		super.show();
	}

	public void pack() {
		assert (false);
	}

	public void setFrameIcon(Icon icon) {

	}

	class InternalFrameToolBarTemplate extends TSToolBarTemplate {
		public void add(Component c) {
			super.add(c);
			getToolBar().add(c);
		}

		public void add(JComponent comp, int index) {
			super.add(comp, index);
			getToolBar().add(comp, index);
		}
	}

	class InternalFrameMenuTemplate extends DynamicMenuTemplate {
		public void add(MenuDefinition menu) {
			super.add(menu);
		}

		public void add(MenuDefinition menu, int index) {
			super.add(menu, index);
		}
	}

}
