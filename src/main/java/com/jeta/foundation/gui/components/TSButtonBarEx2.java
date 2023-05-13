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
import javax.swing.JToolBar;

/**
 * This class implements an extended button bar. This button bar behaves like a
 * normal button bar, but it allows the user to assocatiate a toolbar (more
 * specifically a group of toolbar buttons) with each view.
 * 
 * @author Jeff Tassin
 */
public class TSButtonBarEx2 extends TSPanel {

	/** the current view */
	private ButtonBarView m_currentview;

	/** the list of views available on the button bar */
	private LinkedList m_views = new LinkedList();

	/**
	 * ctor
	 */
	public TSButtonBarEx2() {
		setLayout(new BorderLayout());
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
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JButton btn = (JButton) evt.getSource();
				if (btn != m_currentview.getButton()) {
					Iterator iter = m_views.iterator();
					while (iter.hasNext()) {
						ButtonBarView view = (ButtonBarView) iter.next();
						if (btn == view.getButton()) {
							m_views.remove(m_currentview);
							m_views.addFirst(m_currentview);
							m_currentview = view;
							updateView();
							break;
						}
					}
				}
			}
		});

		btn.setFont(javax.swing.UIManager.getFont("Table.font"));
		ButtonBarView bbview = new ButtonBarView(btn, view, template);

		m_views.add(bbview);

		if (m_currentview == null)
			m_currentview = bbview;

	}

	/**
	 * Updates the button bar
	 */
	public void updateView() {
		removeAll();

		if (m_currentview != null) {
			// add( m_currentview.getButton(), BorderLayout.NORTH );
			add(m_currentview.getToolBar(), BorderLayout.NORTH);
			add(m_currentview.getView(), BorderLayout.CENTER);
			if (m_views.size() > 1) {
				JPanel panel = new JPanel(new GridLayout(m_views.size() - 1, 1));
				Iterator iter = m_views.iterator();
				while (iter.hasNext()) {
					ButtonBarView view = (ButtonBarView) iter.next();
					if (view != m_currentview) {
						panel.add(view.getButton());
					}
				}
				add(panel, BorderLayout.SOUTH);
			}
		}

		revalidate();
		repaint();
	}

	static class ButtonBarView {
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
