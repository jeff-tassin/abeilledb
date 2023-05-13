/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.print;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.border.MatteBorder;

import com.jeta.foundation.gui.components.TSPanel;

public class PagePreview extends TSPanel {
	protected int m_w;
	protected int m_h;
	protected Image m_source;
	protected Image m_img;

	public PagePreview() {

	}

	public PagePreview(int w, int h, Image source) {
		setImage(w, h, source);
	}

	public void setImage(int w, int h, Image source) {
		m_w = w;
		m_h = h;
		m_source = source;
		m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
		m_img.flush();
		setBackground(Color.white);
		setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
	}

	public void setScaledSize(int w, int h) {
		m_w = w;
		m_h = h;
		m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
		repaint();
	}

	public Dimension getPreferredSize() {
		Insets ins = getInsets();
		return new Dimension(m_w + ins.left + ins.right, m_h + ins.top + ins.bottom);
	}

	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public void paint(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(m_img, 0, 0, this);
		paintBorder(g);
	}
}
