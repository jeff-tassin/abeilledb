/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.canvas;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;

public class BufferedImageWidget extends JComponent {
	private BufferedImage m_bimage = null;

	public BufferedImageWidget() {

	}

	/**
	 * This is called when we need to rebuild the widget. Widgets are drawn onto
	 * a buffered image, so when we resize, we need to recreate the image.
	 * 
	 * @param g
	 *            the graphics context of the buffered image for this widget
	 */
	public void buildWidget(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(Color.blue);
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
		g2.fill(rect);
	}

	public BufferedImage createBufferedImage() {
		return new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (m_bimage == null) {
			m_bimage = createBufferedImage();
			Graphics bg = m_bimage.createGraphics();
			buildWidget(bg);
			bg.dispose();
			paintComponent(g);
		} else {
			Graphics g2 = (Graphics) g;
			g2.drawImage(m_bimage, 0, 0, null);
		}
	}

	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		m_bimage = null;
	}
}