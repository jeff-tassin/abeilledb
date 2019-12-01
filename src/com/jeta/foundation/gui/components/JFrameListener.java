/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.Collection;
import java.util.Iterator;

public class JFrameListener implements WindowListener {
	private TSInternalFrame m_iframe;

	public JFrameListener(TSInternalFrame iframe) {
		m_iframe = iframe;
	}

	Collection getFrameListeners() {
		return m_iframe.getFrameListeners();
	}

	public void windowActivated(WindowEvent e) {
		JETAFrameEvent evt = new JETAFrameEvent(e, m_iframe);
		Iterator iter = getFrameListeners().iterator();
		while (iter.hasNext()) {
			JETAFrameListener listener = (JETAFrameListener) iter.next();
			listener.jetaFrameActivated(evt);
		}
	}

	public void windowClosed(WindowEvent e) {
		JETAFrameEvent evt = new JETAFrameEvent(e, m_iframe);

		Collection listeners = getFrameListeners();
		if (listeners.size() > 0)
			listeners = new java.util.LinkedList(listeners);

		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			JETAFrameListener listener = (JETAFrameListener) iter.next();
			listener.jetaFrameClosed(evt);
		}
	}

	public void windowClosing(JETAFrameEvent evt) {
		Collection listeners = getFrameListeners();
		if (listeners.size() > 0)
			listeners = new java.util.LinkedList(listeners);
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			JETAFrameListener listener = (JETAFrameListener) iter.next();
			listener.jetaFrameClosing(evt);
		}
	}

	public void windowClosing(WindowEvent e) {
		JETAFrameEvent evt = new JETAFrameEvent(e, m_iframe);
		windowClosing(evt);
	}

	public void windowDeactivated(WindowEvent e) {
		JETAFrameEvent evt = new JETAFrameEvent(e, m_iframe);
		Iterator iter = getFrameListeners().iterator();
		while (iter.hasNext()) {
			JETAFrameListener listener = (JETAFrameListener) iter.next();
			listener.jetaFrameDeactivated(evt);
		}

	}

	public void windowDeiconified(WindowEvent e) {
		JETAFrameEvent evt = new JETAFrameEvent(e, m_iframe);
		Iterator iter = getFrameListeners().iterator();
		while (iter.hasNext()) {
			JETAFrameListener listener = (JETAFrameListener) iter.next();
			listener.jetaFrameDeiconified(evt);
		}

	}

	public void windowIconified(WindowEvent e) {
		JETAFrameEvent evt = new JETAFrameEvent(e, m_iframe);
		Iterator iter = getFrameListeners().iterator();
		while (iter.hasNext()) {
			JETAFrameListener listener = (JETAFrameListener) iter.next();
			listener.jetaFrameIconified(evt);
		}

	}

	public void windowOpened(WindowEvent e) {
		JETAFrameEvent evt = new JETAFrameEvent(e, m_iframe);
		Iterator iter = getFrameListeners().iterator();
		while (iter.hasNext()) {
			JETAFrameListener listener = (JETAFrameListener) iter.next();
			listener.jetaFrameOpened(evt);
		}
	}
}
