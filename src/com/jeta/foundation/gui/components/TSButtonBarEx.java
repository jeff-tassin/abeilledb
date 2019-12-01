/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class implements an extended button bar. This button bar behaves like a
 * normal button bar, but it allows the user to assocatiate a toolbar (more
 * specifically a group of toolbar buttons) with each view.
 * 
 * @author Jeff Tassin
 */
public class TSButtonBarEx extends TSPanel {

	/** the current view */
	private ButtonBarView m_currentview;

	/** the list of views available on the button bar */
	private LinkedList m_views = new LinkedList();

	private JTabbedPane m_tabpane = new JTabbedPane(JTabbedPane.BOTTOM);

	private LinkedList m_listeners = new LinkedList();

	/**
	 * ctor
	 */
	public TSButtonBarEx() {
		setLayout(new BorderLayout());
		m_tabpane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (m_listeners != null) {
					ActionEvent evt = new ActionEvent(this, 0, "view.changed");
					Iterator iter = m_listeners.iterator();
					while (iter.hasNext()) {
						ActionListener listener = (ActionListener) iter.next();
						listener.actionPerformed(evt);
					}
				}
			}
		});
	}

	/**
	 * Adds a listener that gets called when a view is selected
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Adds a view to the bar.
	 * 
	 * @param caption
	 *            the text that appears in the button that will activate this
	 *            view
	 * @param view
	 *            the view that will be displayed for this button.
	 */
	public void addView(String caption, JComponent view) {
		addView(caption, view, null, null);
	}

	/**
	 * Adds a view to the bar.
	 * 
	 * @param caption
	 *            the text that appears in the button that will activate this
	 *            view
	 * @param view
	 *            the view that will be displayed for this button.
	 */
	public void addView(String caption, JComponent view, ImageIcon icon, TSToolBarTemplate template) {
		JButton btn = new JButton(caption);
		if (icon != null)
			btn.setIcon(icon);

		btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		/*
		 * btn.addActionListener( new ActionListener() { public void
		 * actionPerformed( ActionEvent evt ) { JButton btn =
		 * (JButton)evt.getSource(); if ( btn != m_currentview.getButton() ) {
		 * Iterator iter = m_views.iterator(); while( iter.hasNext() ) {
		 * ButtonBarView view = (ButtonBarView)iter.next(); if ( btn ==
		 * view.getButton() ) { m_views.remove( m_currentview );
		 * m_views.addFirst( m_currentview ); m_currentview = view;
		 * updateView(); break; } } } } });
		 */

		btn.setFont(javax.swing.UIManager.getFont("Table.font"));
		ButtonBarView bbview = new ButtonBarView(btn, view, template);

		m_views.add(bbview);

		if (m_currentview == null)
			m_currentview = bbview;

	}

	public void dispose() {
		removeAll();
		if (m_listeners != null) {
			m_listeners.clear();
			m_listeners = null;
		}
		m_currentview = null;

		if (m_views != null) {
			m_views.clear();
			m_views = null;
		}
		m_tabpane.removeAll();
	}

	/**
	 * @return the currently active view in the bar
	 */
	public JComponent getCurrentView() {
		ButtonBarView view = (ButtonBarView) m_tabpane.getSelectedComponent();
		if (view == null)
			return null;
		else
			return view.getView();
	}

	/**
	 * Updates the button bar
	 */
	public void updateView() {
		removeAll();

		if (m_currentview != null) {
			if (m_views.size() > 1) {
				m_tabpane.removeAll();
				m_tabpane.addTab(m_currentview.getButton().getText(), m_currentview.getButton().getIcon(),
						m_currentview);

				Iterator iter = m_views.iterator();
				while (iter.hasNext()) {
					ButtonBarView view = (ButtonBarView) iter.next();
					if (view != m_currentview) {
						m_tabpane.addTab(view.getButton().getText(), view.getButton().getIcon(), view);
					}
				}
				add(m_tabpane, BorderLayout.CENTER);
			} else {
				add(m_currentview, BorderLayout.CENTER);
			}
		}

		revalidate();
		repaint();
	}

	static class ButtonBarView extends TSPanel {
		/** the button that will display the view */
		private JButton m_btn;
		/** the view associated with this button */
		private JComponent m_view;

		/** the toolbar associated with this view */
		private JToolBar m_toolbar;

		/**
		 * ctor
		 */
		public ButtonBarView(JButton btn, JComponent view, TSToolBarTemplate template) {
			m_btn = btn;
			m_view = view;

			m_toolbar = new JToolBar();
			m_toolbar.setFloatable(false);
			JLabel label = new JLabel(btn.getText());
			label.setIcon(btn.getIcon());
			m_toolbar.add(javax.swing.Box.createHorizontalStrut(10));
			m_toolbar.add(label);
			m_toolbar.add(javax.swing.Box.createHorizontalStrut(24));
			if (template != null) {
				for (int index = 0; index < template.getComponentCount(); index++) {
					m_toolbar.add(template.getComponentAt(index));
				}
			}
			m_toolbar.add(javax.swing.Box.createVerticalStrut(24));

			setLayout(new BorderLayout());
			add(m_toolbar, BorderLayout.NORTH);
			add(m_view, BorderLayout.CENTER);
		}

		/**
		 * @return the button associated with this view
		 */
		public JButton getButton() {
			return m_btn;
		}

		/**
		 * @return the view that is displayed in the ButtonBar when the view is
		 *         selected
		 */
		public JComponent getView() {
			return m_view;
		}

		/**
		 * @return the toolbar associated with this view
		 */
		public JToolBar getToolBar() {
			return m_toolbar;
		}

	}

}
