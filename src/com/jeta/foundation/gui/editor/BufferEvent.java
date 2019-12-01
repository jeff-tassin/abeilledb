/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

/**
 * This class defines an event used for changes to a Buffer
 * 
 * @author Jeff Tassin
 */
public class BufferEvent {
	/** the id of the event */
	private int m_id;

	/** the buffer the event is associated with */
	private Buffer m_buffer;

	// event ids
	public static final int BUFFER_CREATED = 1;
	public static final int BUFFER_DELETED = 2;
	public static final int BUFFER_CHANGED = 3;
	public static final int BUFFER_SELECTED = 4;
	public static final int BUFFER_NAME_CHANGED = 5;
	public static final int SELECT_NEXT_BUFFER = 6;
	public static final int SELECT_PREV_BUFFER = 7;

	/**
	 * ctor
	 * 
	 * @param name
	 *            the name of the event
	 */
	public BufferEvent(int id) {
		m_id = id;
	}

	/**
	 * ctor
	 * 
	 * @param name
	 *            the name of the event
	 */
	public BufferEvent(int id, Buffer buffer) {
		m_id = id;
		m_buffer = buffer;
	}

	/**
	 * @return the buffer that is associated with this event
	 */
	public Buffer getBuffer() {
		return m_buffer;
	}

	/**
	 * @return the name of this event
	 */
	public int getID() {
		return m_id;
	}
}
