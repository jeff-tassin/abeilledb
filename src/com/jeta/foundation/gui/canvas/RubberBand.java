/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.canvas;

import java.awt.Rectangle;
import java.awt.Container;
import java.lang.Math;
import java.awt.Graphics;

public class RubberBand {
	private boolean m_isDragging;
	private int m_anchorX;
	private int m_anchorY;
	private Container m_canvas;
	private int m_width;
	private int m_height;

	public RubberBand(Container canvas) {
		m_isDragging = false;
		m_canvas = canvas;
	}

	public void doDrag(int mouseX, int mouseY) {
		repaint();
		m_width = mouseX - m_anchorX;
		m_height = mouseY - m_anchorY;
		repaint();
	}

	public int getAbsWidth() {
		return Math.abs(m_width);
	}

	public int getAbsHeight() {
		return Math.abs(m_height);
	}

	public Rectangle getBounds() {
		return new Rectangle(getOrgX(), getOrgY(), getAbsWidth(), getAbsHeight());
	}

	public int getOrgX() {
		if (m_width < 0)
			return (m_anchorX + m_width);
		else
			return m_anchorX;
	}

	public int getOrgY() {
		if (m_height < 0)
			return (m_anchorY + m_height);
		else
			return m_anchorY;
	}

	public boolean isDragging() {
		return m_isDragging;
	}

	public void repaint() {
		m_canvas.repaint(getOrgX() - 1, getOrgY() - 1, getAbsWidth() + 2, getAbsHeight() + 2);
	}

	public void startDrag(int mouseX, int mouseY) {
		m_anchorX = mouseX;
		m_anchorY = mouseY;
		m_width = 0;
		m_height = 0;
		m_isDragging = true;
	}

	public void stopDrag(int mouseX, int mouseY) {
		m_isDragging = false;
		repaint();
	}

	public void paint(Graphics g) {
		g.drawRect(getOrgX(), getOrgY(), getAbsWidth(), getAbsHeight());
	}

}