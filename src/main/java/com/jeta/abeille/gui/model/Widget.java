package com.jeta.abeille.gui.model;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.image.*;

public class Widget extends JComponent {
	private int m_orgX;
	private int m_orgY;
	private int m_width;
	private int m_height;
	private BufferedImage m_bimage;
	private boolean m_binitialized = false;

	public Widget() {
		m_orgX = 50;
		m_orgY = 50;
		m_width = 100;
		m_height = 100;
	}

	void buildWidget(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		Paint pt = g2.getPaint();

		m_bimage = new BufferedImage(m_width, m_height, BufferedImage.TYPE_INT_RGB);
		Graphics2D bg = m_bimage.createGraphics();

		bg.setPaint(Color.cyan);
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, m_width, m_height);
		bg.fill(rect);
		bg.dispose();
	}

	public Rectangle getBounds() {
		return new Rectangle(m_orgX, m_orgY, m_width, m_height);
	}

	public int getWidth() {
		return m_width;
	}

	public int getHeight() {
		return m_height;
	}

	public int getX() {
		return m_orgX;
	}

	public int getY() {
		return m_orgY;
	}

	public void setOrigin(int x, int y) {
		m_orgX = x;
		m_orgY = y;
	}

	public void paintComponent(Graphics g) {
		if (m_binitialized) {
			System.out.println("paintWidget");
			Graphics2D g2 = (Graphics2D) g;
			g2.drawImage(m_bimage, m_orgX, m_orgY, null);
		} else {
			buildWidget(g);
			m_binitialized = true;
			paintComponent(g);
		}
	}
}
