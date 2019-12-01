/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Dimension;

import java.awt.event.KeyEvent;

import java.util.Iterator;

import com.jeta.foundation.gui.components.maskedtextfield.MaskComponent;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This is the base class for time based masked input fields.
 * 
 * 
 * @author Jeff Tassin
 */
public abstract class TSTimeBase extends TSNumericMaskComposite {
	public TSTimeBase() {
		// addKeyListener(this);
	}

	public void processKeyEvent(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_INSERT) {
			setNull(false);
			setNow();
		} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			setNull(true);
		} else
			super.processKeyEvent(e);

	}

	public abstract void setNow();

	/**
	 * Provide our own preferred size because the date mask can have different
	 * lengths depending on the month. This causes a resize of the component
	 * which looks annoying. Here we calculate a constant resize regardless of
	 * the month
	 */
	public Dimension getPreferredSize() {
		/** first let's let the component ui calculate the preferred height */
		Dimension d = super.getPreferredSize();

		int numchars = 0;
		Iterator iter = getFieldComponents();
		while (iter.hasNext()) {
			MaskComponent mask = (MaskComponent) iter.next();
			String item = mask.toString();
			numchars += item.length();
		}

		d.width = TSGuiToolbox.calculateAverageTextWidth(this, numchars);
		return d;
	}

}
