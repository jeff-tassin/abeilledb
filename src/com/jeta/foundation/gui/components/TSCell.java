/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.BevelBorder;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;

/**
 * A cell is used in a TSStatusBar. It represents a single cell on the bar. A
 * bar is made up as follows: [cell1][cell2]...[cellN]
 * 
 * @author Jeff Tassin
 */
public class TSCell extends JLabel {
	String m_mask; // this is a mask that represents the maximum size for this
					// cell
					// for example, if this cell represents a time, you could
					// set the mask to 00:00:00
	Dimension m_maxsize;

	/**
	 * flag indicating if this is the main cell. The cell increases size with
	 * the status bar. But, it will never go below its mask size
	 */
	private boolean m_main;

	public TSCell(String name, String mask) {
		setName(name);
		Border b = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
				BorderFactory.createEmptyBorder(0, 2, 0, 2));

		setBorder(b);
		setOpaque(true);
		m_mask = mask;
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	public Dimension getPreferredSize() {
		if (m_maxsize == null)
			m_maxsize = new Dimension();
		Font f = getFont();
		if (f != null) {
			Border b = getBorder();
			if (b != null) {
				Insets insets = b.getBorderInsets(this);
				FontMetrics metrics = getFontMetrics(f);
				m_maxsize.height = metrics.getHeight() + insets.top + insets.bottom;
				m_maxsize.width = metrics.stringWidth(m_mask) + insets.left + insets.right;
			}
		}

		return m_maxsize;
	}

	/**
	 * @return a flag if this cell is the main cell
	 */
	public boolean isMain() {
		return m_main;
	}

	/**
	 * Sets this cell as the main cell on the status bar. Only one cell can be
	 * the main cell. Setting more than one cell as main will lead to undefined
	 * behavior.
	 */
	public void setMain(boolean bMain) {
		m_main = bMain;
	}
}
