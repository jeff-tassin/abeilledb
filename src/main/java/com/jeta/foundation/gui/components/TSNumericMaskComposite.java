/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import java.lang.*;

import com.jeta.foundation.gui.components.maskedtextfield.*;
import com.jeta.foundation.componentmgr.*;
import com.jeta.foundation.plugin.PluginMgr;

/**
 * This represents a text field that has a group of numeric masks. We provide an
 * implementation because we want to handle the case when the input mode changes
 * in one mask we want to update the others
 * 
 * @author Jeff Tassin
 */
public class TSNumericMaskComposite extends TSMaskedTextField implements ActionListener {
	private LinkedList m_listeners = new LinkedList(); // listeners interested
														// in change events
	public static final String VALUE_CHANGE_EVENT = "valuechanged";

	public TSNumericMaskComposite() {
	}

	/**
	 * Adds a listener to receive change events
	 * 
	 * @param listener
	 *            the listener interested in the change events
	 * 
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Override TSMaskedTextField so we can set the action listener for an
	 * TimeCompnentMasks
	 * 
	 * @param mask
	 *            the mask component to add
	 */
	public void addMask(MaskComponent mask) {
		super.addMask(mask);
		if (mask instanceof InputMaskComponent) {
			InputMaskComponent comp = (InputMaskComponent) mask;
			comp.addActionListener(this);
		}
	}

	/**
	 * This method gets called in response to events from the individual masks
	 */
	public void actionPerformed(ActionEvent evt) {
		// when we get a mode change on one component, then reset all
		if (evt.getActionCommand().equals(NumericMaskComponent.MODECHANGE_EVT)) {
			NumericMaskComponent mask = (NumericMaskComponent) evt.getSource();
			int mode = mask.getInputMode();
			LinkedList list = getInputComponents();
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof NumericMaskComponent) {
					NumericMaskComponent maskcomp = (NumericMaskComponent) obj;
					maskcomp.setInputMode(mode);
					if (maskcomp != mask)
						maskcomp.setInputPos(0);
				}
			}
		}

		// forward events to any listeners that are interested
		if (evt.getActionCommand().equals(InputMaskComponent.VALUE_CHANGE_EVENT))
			evt = new ActionEvent(this, 0, VALUE_CHANGE_EVENT);
		notifyListeners(evt);
		repaint();
	}

	/**
	 * Notifies all action listeners of change events
	 */
	protected void notifyListeners(ActionEvent evt) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			ActionListener listener = (ActionListener) iter.next();
			listener.actionPerformed(evt);
		}
	}

	/**
	 * Sets the value of this field to null. The field will display null as well
	 * 
	 * @param isNull
	 *            true if you want this field to be null, false otherwise
	 */
	public void setNull(boolean isNull) {
		// we override here so we can send the event back to the listeners
		super.setNull(isNull);
		ActionEvent evt = new ActionEvent(this, 0, VALUE_CHANGE_EVENT);
		notifyListeners(evt);
	}

}
