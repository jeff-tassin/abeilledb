/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.dnd;

import java.awt.Component;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetContext;

/**
 * This is a support class that we need for drag and drop within the
 * application. Currently, we only support drag and drop (of database objects)
 * within the application. We need this support to handle bugs that exist in the
 * jdk for now.
 * 
 * @author Jeff Tassin
 */
public class DnDSupport {
	private static int m_targetactions;

	private static Component m_target;

	/** the object or collection of objects that we are dragging */
	private static Object m_localdragObject;

	/**
	 * Called by the local drop target to accept the drag
	 * 
	 */
	public static void acceptDrag(DropTargetEvent event, int actions) {
		m_targetactions = actions;
		m_target = event.getDropTargetContext().getComponent();
	}

	/**
	 * @return the object we are currently dragging.
	 */
	public static Object getLocalDragObject() {
		return m_localdragObject;
	}

	/**
	 * If the target is local, then this will return a valid non-null result
	 */
	public static Component getTarget() {
		return m_target;
	}

	public static int getTargetActions(DragSourceEvent event) {
		return m_targetactions;
	}

	/**
	 * Called by the local drop target to accept the drag
	 * 
	 */
	public static void rejectDrag(DropTargetEvent event) {
		m_targetactions = DnDConstants.ACTION_NONE;
		m_target = null;
	}

	/**
	 * Sets the local drag object
	 */
	public static void setLocalDragObject(Object obj) {
		m_localdragObject = obj;
	}

}
