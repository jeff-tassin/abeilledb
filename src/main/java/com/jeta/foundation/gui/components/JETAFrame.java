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

import com.jeta.open.gui.framework.JETAController;
import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.UIDirector;

import com.jeta.open.registry.JETARegistry;

import com.jeta.open.support.ComponentFinder;
import com.jeta.open.support.ComponentFinderFactory;
import com.jeta.open.support.CompositeComponentFinder;
import com.jeta.open.support.DefaultComponentFinder;

/**
 * @author Jeff Tassin
 */
public class JETAFrame extends JFrame implements JETAContainer {
	private JMenuBar m_menuBar = new JMenuBar();
	private JToolBar m_toolBar = new JToolBar();

	private CompositeComponentFinder m_finder;

	private UIDirector m_uidirector;

	private TSToolBarTemplate m_toolbarTemplate = new InternalFrameToolBarTemplate();
	private MenuTemplate m_menuTemplate = new InternalFrameMenuTemplate();

	private JETAController m_controller;

	public JETAFrame(String caption) {
		super(caption);

		m_toolBar.setFloatable(false);

		setResizable(true);
		setDefaultCloseOperation(JFrameEx.DO_NOTHING_ON_CLOSE);
		setJMenuBar(m_menuBar);

		Container contentpane = getContentPane();
		contentpane.setLayout(new BorderLayout());
		contentpane.add(m_toolBar, BorderLayout.NORTH);
	}

	/**
	 * Creates a component finder that is used to locate child components in
	 * this panel by name.
	 */
	protected ComponentFinder createComponentFinder() {
		if (m_finder == null) {
			CompositeComponentFinder finder = new CompositeComponentFinder();
			finder.add(new DefaultComponentFinder(getJMenuBar()));
			finder.add(new DefaultComponentFinder(m_toolBar));
			m_finder = finder;
		}
		return m_finder;
	}

	public void dispose() {
		super.dispose();
		removeAll();
	}

	public void enableComponent(String commandId, boolean bEnable) {
		Collection comps = getComponentsByName(commandId);
		Iterator iter = comps.iterator();
		while (iter.hasNext()) {
			Component comp = (Component) iter.next();
			if (bEnable != comp.isEnabled())
				comp.setEnabled(bEnable);
		}
	}

	protected TSToolBarTemplate createToolBarTemplate() {
		return new InternalFrameToolBarTemplate();
	}

	protected MenuTemplate createMenuTemplate() {
		return new InternalFrameMenuTemplate();
	}

	public Component getComponentByName(String compName) {
		return getComponentFinder().getComponentByName(compName);
	}

	public Collection getComponentsByName(String compName) {
		return getComponentFinder().getComponentsByName(compName);
	}

	/**
	 * Returns the component finder associated with this panel
	 * 
	 * @return the component finder associated with this panel
	 */
	protected ComponentFinder getComponentFinder() {
		if (m_finder == null) {
			m_finder = (CompositeComponentFinder) createComponentFinder();
		}
		return m_finder;
	}

	public JETAController getController() {
		return m_controller;
	}

	public TSToolBarTemplate getToolBarTemplate() {
		if (m_toolbarTemplate == null) {
			m_toolbarTemplate = createToolBarTemplate();
		}
		return m_toolbarTemplate;
	}

	public MenuTemplate getMenuTemplate() {
		if (m_menuTemplate == null) {
			m_menuTemplate = createMenuTemplate();
		}
		return m_menuTemplate;
	}

	public UIDirector getUIDirector() {
		return m_uidirector;
	}

	public JToolBar getToolBar() {
		return m_toolBar;
	}

	public void revalidate() {
		Object content = getContentPane();
		if (content instanceof JComponent) {
			((JComponent) content).revalidate();
		}
	}

	public void removeToolBar() {
		Container contentpane = getContentPane();
		contentpane.remove(m_toolBar);
	}

	public void setController(JETAController controller) {
		m_controller = controller;
	}

	public void setToolBar(JToolBar toolbar) {
		removeToolBar();
		Container contentpane = getContentPane();
		contentpane.add(toolbar, BorderLayout.NORTH);
		m_toolBar = toolbar;
		CompositeComponentFinder finder = (CompositeComponentFinder) getComponentFinder();
		finder.add(new DefaultComponentFinder(toolbar));
	}

	public void setUIDirector(UIDirector uidirector) {
		m_uidirector = uidirector;
	}

	public void updateUI() {
		Object content = getContentPane();
		if (content instanceof JComponent) {
			((JComponent) content).updateUI();
		}
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

	class InternalFrameMenuTemplate extends BasicMenuTemplate {
		public void add(MenuDefinition menu) {
			super.add(menu);
			getJMenuBar().add(menu.createMenu());
		}

		public void add(MenuDefinition menu, int index) {
			super.add(menu, index);
			getJMenuBar().add(menu.createMenu(), index);
		}
	}

}
