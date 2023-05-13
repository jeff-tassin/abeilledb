/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import org.netbeans.editor.Utilities;

/**
 * This class is used to manage buffers for any container that has multiple
 * editor windows
 * 
 * @author Jeff Tassin
 */
public class BufferMgr {
	private ArrayList m_buffers = new ArrayList(); // list of all opened buffers
	private Buffer m_currentbuffer; // the buffer that the user is currently
									// editing
	private LinkedList m_mrulist = new LinkedList(); // most recently used
														// buffer list

	/**
	 * a list of buffer listeners. these listeners get events when a buffer is
	 * acted up (i.e. modified, changed, closed )
	 */
	private LinkedList m_bufferlisteners = new LinkedList();

	/**
	 * Determines whether this buffer will allow more/less than a single buffer.
	 * If singleton is true, then we allow only one buffer and it cannot be
	 * closed. (This happens when a buffer is in a dialog)
	 */
	private boolean m_singleton = false;

	/**
	 * ctor
	 */
	public BufferMgr() {

	}

	/**
	 * ctor Creates a buffer manager with the given buffer
	 * 
	 * @param frameWindow
	 *            the frame window (or dialog) that contains the buffers
	 * @param buff
	 *            the buffer object to add to the manager
	 * @param singleton
	 *            sets whether this buffer will allow more/less than a single
	 *            buffer. If singleton is true, then we allow only one buffer
	 *            and it cannot be closed.
	 * 
	 */
	public BufferMgr(Buffer buff, boolean singleton) {
		addBuffer(buff);
		m_singleton = singleton;
		m_currentbuffer = buff;
	}

	/**
	 * Adds a buffer to the list
	 */
	public void addBuffer(Buffer buff) {
		if (!isSingleton()) {
			assert (!m_buffers.contains(buff));
			m_buffers.add(buff);
			updateBufferNames();
		}
	}

	/**
	 * Adds a listener to the list of buffer listeners
	 */
	public void addBufferListener(BufferListener listener) {
		m_bufferlisteners.add(listener);
	}

	/**
	 * Deletes the buffer from this manager
	 */
	public void deleteBuffer(Buffer buff) {
		if (!isSingleton()) {
			Buffer mrubuff = getMostRecentBuffer();

			m_buffers.remove(buff);
			m_mrulist.remove(buff);

			m_currentbuffer = null;

			fireBufferEvent(new BufferEvent(BufferEvent.BUFFER_DELETED, buff));

			if (mrubuff != null) {
				selectBuffer(mrubuff);
			}
			buff.close();
			updateBufferNames();
		}
	}

	/**
	 * Searches for a buffer in this collection that has a file with the same
	 * path/name as the given file
	 */
	public Buffer findBuffer(File f) {
		if (f == null)
			return null;

		Collection c = getBuffers();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			Buffer buffer = (Buffer) iter.next();
			if (f.equals(buffer.getFile()))
				return buffer;
		}
		return null;
	}

	/**
	 * @param bufferName
	 *            the name of the buffer to location
	 * @return the buffer object that has the given name
	 */
	public Buffer findBufferByTitle(String bufferTitle) {
		Collection c = getBuffers();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			Buffer b = (Buffer) iter.next();
			if (bufferTitle.equals(b.getTitle()))
				return b;
		}
		return null;
	}

	/**
	 * Sends a buffer event to all buffer listeners registered with this manager
	 * 
	 * @param evt
	 *            the event to send
	 */
	public void fireBufferEvent(BufferEvent evt) {
		// we clone because the event might cause the listener to
		// remove itself from the list
		LinkedList list = (LinkedList) m_bufferlisteners.clone();
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			BufferListener listener = (BufferListener) iter.next();
			listener.bufferChanged(evt);
		}
	}

	/**
	 * @returns the buffer that is associated with the given editor
	 */
	public Buffer getBuffer(JEditorPane editor) {
		Buffer result = null;
		Iterator iter = m_buffers.iterator();
		while (iter.hasNext()) {
			Buffer buffer = (Buffer) iter.next();
			if (buffer.getEditor() == editor) {
				result = buffer;
				break;
			}
		}
		return result;
	}

	public int getBufferCount() {
		return m_buffers.size();
	}

	/**
	 * @return the collection of open buffers (Buffer objects)
	 */
	public Collection getBuffers() {
		return m_buffers;
	}

	/**
	 * @return the current buffer that is visible. If no buffer is visible, null
	 *         is returned.
	 */
	public Buffer getCurrentBuffer() {
		return m_currentbuffer;
	}

	public int getCurrentBufferPosition() {
		if (m_currentbuffer == null)
			return -1;

		for (int index = 0; index < m_buffers.size(); index++) {
			Buffer buff = (Buffer) m_buffers.get(index);
			if (buff == m_currentbuffer)
				return index;
		}

		return -1;
	}

	/**
	 * @return the current editor that is visible. If no editor is current, null
	 *         is returned.
	 */
	public JEditorPane getCurrentEditor() {
		if (m_currentbuffer == null)
			return null;
		else
			return m_currentbuffer.getEditor();
	}

	/**
	 * @return the most recently used buffer (other the the current buffer )
	 *         Null is returned if there is no MRU buffer
	 */
	public Buffer getMostRecentBuffer() {
		if (m_mrulist.size() == 0)
			return null;

		Buffer result = (Buffer) m_mrulist.getFirst();
		return result;
	}

	/**
	 * @returns whether this buffermgr allows more than a single buffer
	 */
	public boolean isSingleton() {
		return m_singleton;
	}

	/**
	 * Removes a listener to the list of buffer listeners
	 */
	public void removeBufferListener(BufferListener listener) {
		m_bufferlisteners.remove(listener);
	}

	/**
	 * Selects the given buffer object in the frame. That is, it is made visible
	 * so the user can edit its contents. Any current buffer is removed from the
	 * frame (and cached in memory).
	 * 
	 * @param buffer
	 *            the buffer object to display
	 */
	public void selectBuffer(Buffer buffer) {
		if (m_currentbuffer == buffer)
			return;

		if (m_currentbuffer != null) {
			m_mrulist.remove(m_currentbuffer);
			m_mrulist.addFirst(m_currentbuffer);
		}

		m_currentbuffer = buffer;

		final JEditorPane editor = buffer.getEditor();
		editor.repaint();

		Document doc = editor.getDocument();
		Boolean bmod = (Boolean) doc.getProperty(TSTextNames.MODIFIED);

		m_mrulist.remove(buffer);
		fireBufferEvent(new BufferEvent(BufferEvent.BUFFER_SELECTED, buffer));

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				editor.requestFocus();
				Utilities.requestFocus(editor);
			}
		});

	}

	/**
	 * Selects the next buffer. If the current buffer is the last buffer, then
	 * the first buffer is selected
	 */
	public void selectNextBuffer() {
		fireBufferEvent(new BufferEvent(BufferEvent.SELECT_NEXT_BUFFER));
	}

	/**
	 * Selects the next buffer. If the current buffer is the first buffer, then
	 * the last buffer is selected
	 */
	public void selectPrevBuffer() {
		fireBufferEvent(new BufferEvent(BufferEvent.SELECT_PREV_BUFFER));
	}

	/**
	 * Called when a buffer has been added or removed. In this case, we need to
	 * make sure that any buffers with the same name have their titles changed
	 * accordingly
	 */
	private void updateBufferNames() {
		// modify the buffer title if there is a buffer with the same name

		HashMap fmap = new HashMap();
		Iterator iter = m_buffers.iterator();
		int index = 0;
		while (iter.hasNext()) {
			Buffer buffer = (Buffer) iter.next();
			String title = buffer.getName();
			if (title == null)
				title = "";

			buffer.setTitle(title);
			fireBufferEvent(new BufferEvent(BufferEvent.BUFFER_NAME_CHANGED, buffer));

			/*
			 * if ( index == 0 ) { LinkedList list = new LinkedList(); list.add(
			 * buffer ); fmap.put( buffer.getName(), list ); } else { Object obj
			 * = fmap.get( buffer.getName() ); LinkedList list =
			 * (LinkedList)obj; if ( list == null ) { list = new LinkedList();
			 * list.add( buffer ); fmap.put( buffer.getName(), list ); } else {
			 * title = title + "<" + String.valueOf(list.size()+1) + ">";
			 * list.add( buffer ); } }
			 * 
			 * if ( !title.equals( buffer.getTitle() ) ) { buffer.setTitle(
			 * title ); fireBufferEvent( new BufferEvent(
			 * BufferEvent.BUFFER_NAME_CHANGED, buffer ) ); }
			 */
			index++;
		}
	}

}
