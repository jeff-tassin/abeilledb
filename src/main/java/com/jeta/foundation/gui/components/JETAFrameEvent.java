/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Component;
import java.awt.event.WindowEvent;
import javax.swing.event.InternalFrameEvent;

public class JETAFrameEvent {
	private Object m_src;

	public JETAFrameEvent(InternalFrameEvent evt, TSInternalFrame src) {
		m_src = src;
	}

	public JETAFrameEvent(WindowEvent evt, TSInternalFrame src) {
		m_src = src;
	}

	public JETAFrameEvent(Object src) {
		m_src = src;
	}

	public Object getSource() {
		return m_src;
	}
}
