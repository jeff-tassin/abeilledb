/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.canvas;

import java.awt.Component;

/**
 * Event class for clients that wish to get events when changes occur to the
 * ModelCanvas. This includes changes to size/position of components on the
 * canvas.
 * 
 * @author Jeff Tassin
 */
public class CanvasEvent {
	private int m_id;
	private Component m_comp;

	/** event ids */
	public static final int ID_COMPONENT_MOVED = 1;
	public static final int ID_COMPONENT_RESIZED = 2;
	public static final int ID_COMPONENT_ADDED = 3;
	public static final int ID_COMPONENT_REMOVED = 4;
	public static final int ID_CANVAS_CHANGED = 5;

	public CanvasEvent(int id, Component src) {
		m_id = id;
		m_comp = src;
	}

	public int getID() {
		return m_id;
	}

	public Component getComponent() {
		return m_comp;
	}

	void setComponent(Component src) {
		m_comp = src;
	}
}
