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

public class ResizeHandle extends BufferedImageWidget {
	private String m_tag;
	private static Dimension m_size = new Dimension(8, 8);
	private PositionController m_pc = new PositionController();

	public ResizeHandle() {
		this.setPreferredSize(m_size);
		this.setSize(m_size);
		this.addMouseListener(m_pc);
		this.addMouseMotionListener(m_pc);
	}

	public ResizeHandle(String tag) {
		this();
		m_tag = tag;

	}

	public void buildWidget(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(Color.gray);
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
		g2.fill(rect);
	}

	public void setTag(String tag) {
		m_tag = tag;
	}

	public String getTag() {
		return m_tag;
	}

}
