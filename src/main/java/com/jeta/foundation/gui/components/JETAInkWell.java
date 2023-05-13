/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

/**
 * This class is used to display a small rectangular button on a panel. The
 * button is used to select a color for those dialogs that support color
 * selection/preferences.
 * 
 * @author Jeff Tassin
 */
public class JETAInkWell extends AbstractButton {
	private Color m_color;

	/**
	 * ctor
	 */
	public JETAInkWell(Color color) {
		m_color = color;
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		Dimension d = new Dimension(16, 16);
		setSize(d);
		setPreferredSize(d);
		setModel(new javax.swing.DefaultButtonModel());
		addMouseListener(new javax.swing.plaf.basic.BasicButtonListener(this));
	}

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

	public void setColor(Color color) {
		m_color = color;
		repaint();
	}

}
