/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.utils;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class ControlsAlignLayout extends GridBagLayout {
	private HashMap m_components = new HashMap();
	private FontMetrics m_fm;

	public ControlsAlignLayout() {
		JTextField field = new JTextField();
		Font f = field.getFont();
		m_fm = field.getFontMetrics(f);
	}

	public void setMaxTextFieldWidth(Component field, int numChars) {
		assert (field != null);
		m_components.put(field, new Integer(numChars));
	}

	public void layoutContainer(Container parent) {
		super.layoutContainer(parent);
		Iterator iter = m_components.keySet().iterator();
		while (iter.hasNext()) {
			Component field = (Component) iter.next();
			Font f = field.getFont();
			// FontMetrics fm = field.getFontMetrics( f );
			Integer ifieldsize = (Integer) m_components.get(field);
			int fsize = ifieldsize.intValue();
			int maxlen = m_fm.stringWidth("M") * fsize;
			if (field.getWidth() > maxlen) {
				field.setSize(maxlen, field.getHeight());
			}
		}
	}

	/**
	 * @return the components this layout is reponsible for sizing
	 */
	public Collection getComponents() {
		return m_components.keySet();
	}

	/**
	 * @return the width in pixels of the component if it's width is managed
	 *         here
	 */
	public int getMaxFieldWidth(Component field) {
		Integer ifieldsize = (Integer) m_components.get(field);
		if (ifieldsize == null) {
			return field.getWidth();
		} else {
			int fsize = ifieldsize.intValue();
			int maxlen = m_fm.stringWidth("M") * fsize;
			return maxlen;
		}
	}

}
