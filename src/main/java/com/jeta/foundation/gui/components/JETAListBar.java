/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.jeta.foundation.gui.layouts.ColumnLayout;

/**
 * 
 * @author Jeff Tassin
 */
public class JETAListBar extends TSPanel {

	/** the current view */
	private ListBarView m_currentview;

	/** the top view */
	private TSPanel m_listview = new TSPanel();
	/** the container for the actual views */
	private TSPanel m_viewcontainer = new TSPanel();

	/** the list of views available on the button bar */
	private LinkedList m_views = new LinkedList();

	private LinkedList m_listeners = new LinkedList();

	/**
	 * ctor
	 */
	public JETAListBar() {
		setLayout(new BorderLayout());
		m_listview.setLayout(new ColumnLayout());
		m_viewcontainer.setLayout(new CardLayout());
		add(m_listview, BorderLayout.NORTH);
		add(m_viewcontainer, BorderLayout.CENTER);

		m_listview.setOpaque(true);
		m_listview.setBackground(Color.white);
		m_listview.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		/*
		 * m_tabpane.addChangeListener( new ChangeListener() { public void
		 * stateChanged(ChangeEvent e) { if ( m_listeners != null ) {
		 * ActionEvent evt = new ActionEvent( this, 0, "view.changed" );
		 * Iterator iter = m_listeners.iterator(); while( iter.hasNext() ) {
		 * ActionListener listener = (ActionListener)iter.next();
		 * listener.actionPerformed( evt ); } } } } );
		 */
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

		btn.setFont(javax.swing.UIManager.getFont("Table.font"));
		ListBarView bbview = new ListBarView(btn, view, template);

		JLabel label = new JLabel(caption, icon, javax.swing.SwingConstants.LEFT);
		label.setFont(javax.swing.UIManager.getFont("Table.font"));
		m_listview.add(label);
		m_listview.add(javax.swing.Box.createVerticalStrut(7));
		m_views.add(bbview);

		if (m_currentview == null)
			m_currentview = bbview;

		m_viewcontainer.add(bbview, caption);
		// CardLayout layout = (CardLayout)m_viewcontainer.getLayout();
		// assert( caption != null );
		// layout.addLayoutComponent( bbview, caption );
	}

	public void dispose() {
		removeAll();
		m_listeners.clear();
		m_listeners = null;
		m_currentview = null;
		m_views.clear();
		m_views = null;
	}

	/**
	 * @return the currently active view in the bar
	 */
	public JComponent getCurrentView() {
		if (m_currentview != null)
			return m_currentview.getView();
		else
			return null;
	}

	static class ListBarView extends TSPanel {
		/** the button that will display the view */
		private JButton m_btn;
		/** the view associated with this button */
		private JComponent m_view;

		/** the toolbar associated with this view */
		private JToolBar m_toolbar;

		/**
		 * ctor
		 */
		public ListBarView(JButton btn, JComponent view, TSToolBarTemplate template) {
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
