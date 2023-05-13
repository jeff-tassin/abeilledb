/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.canvas;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Rectangle;

public class ShadowDecorator {
	public static final int SHADOW_THICKNESS = 3;
	public static final int SHADOW_OFFSET = 4;

	public static void paintComponent(Graphics g2, JComponent c) {
		// Graphics2D g2 = (Graphics2D)g;
		Rectangle rect = new Rectangle(c.getWidth() - SHADOW_THICKNESS, SHADOW_OFFSET, SHADOW_THICKNESS, c.getHeight()
				- SHADOW_OFFSET + SHADOW_THICKNESS);

		g2.setColor(Color.black);
		g2.fillRect(rect.x, rect.y, rect.width, rect.height);

		rect = new Rectangle(SHADOW_OFFSET, c.getHeight() - SHADOW_THICKNESS, c.getWidth() + SHADOW_THICKNESS,
				SHADOW_THICKNESS);
		g2.fillRect(rect.x, rect.y, rect.width, rect.height);
	}

	public static int getComponentWidth(JComponent c) {
		return c.getWidth() - SHADOW_THICKNESS;
	}

	public static int getComponentHeight(JComponent c) {
		return c.getHeight() - SHADOW_THICKNESS;
	}
}
