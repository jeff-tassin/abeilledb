/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.util.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.*;

import java.awt.*;
import java.awt.geom.*;
import javax.accessibility.Accessible;
import java.awt.event.*;
import javax.swing.border.*;

/**
 * This is a standard status bar for the application. Each status bar is made up
 * of TSCell elements for displaying individual status information.
 * [cell1][cell2]....[cellN]
 * 
 * 
 * @author Jeff Tassin
 */
public class TSStatusBar extends JPanel {
	private LinkedList m_cells = new LinkedList();

	public TSStatusBar() {
		// super( new BorderLayout() );
		setFont(new Font("Arial", Font.PLAIN, 12));

		JButton btn = new JButton();
		setBackground(btn.getBackground());
		setLayout(new StatusBarLayout());
	}

	/**
	 * Adds a cell to this status bar
	 */
	public void addCell(Component cell) {
		cell.setFont(getFont());
		cell.setForeground(Color.black);
		m_cells.add(cell);
		add(cell);
		// refreshPanel();
	}

	/**
	 * @param cellname
	 *            the name of the cell
	 * @return the cell object that has the given name. Null is returned if the
	 *         cell is not found.
	 */
	public Component getCell(String cellname) {
		Iterator iter = m_cells.iterator();
		while (iter.hasNext()) {
			Component cell = (Component) iter.next();
			if (cell.getName().equals(cellname))
				return cell;
		}
		return null;
	}

	public Dimension getMinimumSize(Component c) {
		return new Dimension(12, 12);
	}

	/**
	 * @return the preferred size for this status bar Currently I only calculate
	 *         the height, since the layout manager should ignore the width and
	 *         set it the to the frame width.
	 */
	public Dimension getPreferredSize() {

		int height = 0;
		int width = 0;
		Iterator iter = m_cells.iterator();
		while (iter.hasNext()) {
			Component cell = (Component) iter.next();
			Dimension d = cell.getPreferredSize();
			if (height < d.height)
				height = d.height;

			width += d.width;
		}

		if (height == 0 || width == 0)
			return getMinimumSize();
		else {
			return new Dimension(width, height);
		}
	}

	/**
	 * Reloads the cells on the bar.
	 */
	private void refreshPanel() {
		// Layout cells
		removeAll();
		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();
		// gc.gridx = GridBagConstraints.RELATIVE;
		gc.gridwidth = 1;
		gc.gridheight = 1;

		int index = 0;
		Iterator iter = m_cells.iterator();
		while (iter.hasNext()) {
			Component c = (Component) iter.next();
			// boolean main = CELL_MAIN.equals(c.getName());
			// if (main)
			// {
			// gc.fill = GridBagConstraints.HORIZONTAL;
			// gc.weightx = 1.0;
			// }
			gc.gridx = index;
			gc.fill = GridBagConstraints.VERTICAL;
			gc.weightx = 0.0;
			if (c instanceof TSCell) {
				TSCell cell = (TSCell) c;
				if (cell.isMain()) {
					gc.fill = GridBagConstraints.BOTH;
					gc.weightx = 1.0;
				}
			}
			panel.add(c, gc);
			index++;

			// if (main)
			// {
			// gc.fill = GridBagConstraints.NONE;
			// gc.weightx = 0;
			// }
		}

		add(panel, BorderLayout.CENTER);
	}

	/**
	 * Layout manager for this status bar
	 */
	class StatusBarLayout implements LayoutManager {
		private Dimension m_minsize = new Dimension(10, 10);

		public void addLayoutComponent(String name, Component comp) {

		}

		public void layoutContainer(Container parent) {
			TSCell maincell = null;

			int totalwidth = 0;
			int count = parent.getComponentCount();
			int maxheight = 0;
			for (int index = 0; index < count; index++) {
				Component c = (Component) parent.getComponent(index);
				Dimension d = c.getPreferredSize();
				if (c instanceof TSCell) {
					TSCell cell = (TSCell) c;
					if (cell.isMain()) {
						maincell = cell;
					}
				}
				if (maxheight < d.height)
					maxheight = d.height;

				c.setSize(d);
				totalwidth += d.width;
			}

			if (maincell != null && totalwidth < parent.getWidth()) {
				Dimension d = maincell.getSize();
				maincell.setSize(d.width + (parent.getWidth() - totalwidth), d.height);
			}

			int x = 0;
			for (int index = 0; index < count; index++) {
				Component c = (Component) parent.getComponent(index);
				c.setLocation(x, 0);
				Dimension d = c.getSize();
				// d.height = maxheight;
				d.height = parent.getHeight();
				c.setSize(d);
				x += c.getWidth();
			}
		}

		public Dimension minimumLayoutSize(Container parent) {
			return m_minsize;
		}

		public Dimension preferredLayoutSize(Container parent) {
			return TSStatusBar.this.getPreferredSize();
		}

		public void removeLayoutComponent(Component comp) {
		}
	}

}
