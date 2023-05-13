/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.border.BevelBorder;

/**
 * This class is used to display a small rectangular button on a panel. The
 * button is used to select a color for those dialogs that support color
 * selection/preferences.
 * 
 * @author Jeff Tassin
 */
public class JETAInkWellEx extends AbstractButton {
	private Color m_color;

	/**
	 * ctor
	 */
	public JETAInkWellEx() {
		this(Color.black);
	}

	/**
	 * ctor
	 */
	public JETAInkWellEx(Color color) {
		if (color == null)
			m_color = Color.black;
		else
			m_color = color;

		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		Dimension d = new Dimension(16, 16);
		setSize(d);
		setPreferredSize(d);
		setModel(new javax.swing.DefaultButtonModel());
		addMouseListener(new javax.swing.plaf.basic.BasicButtonListener(this));
		addActionListener(new ColorAction());
	}

	/**
	 * @return the color for this ink well
	 */
	public Color getColor() {
		return m_color;
	}

	/**
	 * Paints the button
	 */
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(getColor());
		Rectangle rect = g.getClipBounds();
		g2.fillRect(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Sets the color for this ink well
	 */
	public void setColor(Color color) {
		Color old_color = m_color;
		m_color = color;
		repaint();
	}

	private static Component getParentWindow(Component comp) {
		while (comp != null) {
			if (comp instanceof java.awt.Window)
				return comp;
			comp = comp.getParent();
		}
		return null;
	}

	public static class ColorAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JETAInkWellEx comp = (JETAInkWellEx) evt.getSource();
			Color c = JColorChooser.showDialog(getParentWindow(comp), "Color Chooser", comp.getColor());
			if (c != null) {
				Color old_color = comp.m_color;
				comp.setColor(c);
				comp.firePropertyChange("color", c, old_color);
			}
		}
	}

}
