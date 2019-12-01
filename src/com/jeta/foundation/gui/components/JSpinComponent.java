/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import java.awt.geom.*;
import java.awt.event.*;

/**
 * This class represents a spin button component (either the up or down button)
 * 
 * @author Jeff Tassin
 */
public class JSpinComponent extends JComponent implements ActionListener {
	private int PRESSED_IN = 1;
	private int RELEASED = 0;
	private int PRESSED_OUT = 3;

	public static int SPIN_EVENT = 4;

	private boolean bup;
	private int m_state;
	private javax.swing.Timer m_timer;
	private LinkedList m_listeners = new LinkedList();

	JSpinComponent(boolean up) {
		bup = up;
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		m_state = RELEASED;
		m_timer = new javax.swing.Timer(250, this);
		m_timer.stop();
	}

	/**
	 * Action events from the Timer
	 */
	public void actionPerformed(ActionEvent e) {
		sendEvent();
	}

	/**
	 * Receive events from this component
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(Color.darkGray);

		int x2 = getWidth();
		int y2 = getHeight();

		if (m_state == PRESSED_IN) {
			g2.setPaint(Color.gray);
			g2.fillRect(0, 0, x2, y2);
		}

		g.drawLine(0, 0, 0, y2);
		g.drawLine(0, 0, x2, 0);
		g.drawLine(x2 - 1, 0, x2 - 1, y2 - 1);
		g.drawLine(0, y2 - 1, x2 - 1, y2 - 1);

		if (m_state != PRESSED_IN) {
			g2.setPaint(Color.white);
			g2.drawLine(0 + 1, 0 + 1, x2 - 1, 1);
			g2.drawLine(0 + 1, 0 + 1, 1, y2 - 1);
			g2.drawLine(x2, 0, x2, y2);
			g2.drawLine(0, y2, x2, y2);
		}

		drawArrow(g);
	}

	private void drawArrow(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		GeneralPath arrowhead;

		int[] x3 = new int[3];
		int[] y3 = new int[3];

		if (bup) {
			x3[0] = 2 * getWidth() / 10;
			y3[0] = getWidth() * 6 / 20 + (getHeight() - getWidth() * 6 / 20) / 2;

			x3[1] = getWidth() - x3[0];
			y3[1] = y3[0];

			x3[2] = getWidth() / 2;
			y3[2] = (getHeight() - getWidth() * 6 / 20) / 2;

		} else {
			x3[0] = 2 * getWidth() / 10;
			y3[0] = (getHeight() - getWidth() * 6 / 20) / 2;

			x3[1] = getWidth() - x3[0];
			y3[1] = y3[0];

			x3[2] = getWidth() / 2;
			y3[2] = getWidth() * 6 / 20 + y3[0];
		}

		arrowhead = new GeneralPath(GeneralPath.WIND_EVEN_ODD, x3.length);
		arrowhead.moveTo(x3[0], y3[0]);

		for (int index = 1; index < x3.length; index++)
			arrowhead.lineTo(x3[index], y3[index]);
		arrowhead.closePath();

		g2.setPaint(Color.black);
		g2.fill(arrowhead);
	}

	public void processMouseEvent(MouseEvent evt) {

		if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
			m_state = PRESSED_IN;
			repaint();
			sendEvent();
			startTimer();
		} else if (evt.getID() == MouseEvent.MOUSE_RELEASED) {
			System.out.println("Mouse released");
			m_state = RELEASED;
			repaint();
			stopTimer();
		} else if (evt.getID() == MouseEvent.MOUSE_ENTERED) {
			if (m_state == PRESSED_OUT) {
				m_state = PRESSED_IN;
				repaint();
				sendEvent();
				startTimer();
			}
		} else if (evt.getID() == MouseEvent.MOUSE_EXITED) {
			if (m_state == PRESSED_IN) {
				m_state = PRESSED_OUT;
				repaint();
				stopTimer();
			}
		}

	}

	/**
	 * Receive events from this component
	 */
	public void removeActionListener(ActionListener listener) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			if (listener == iter.next()) {
				iter.remove();
			}
		}
	}

	private void sendEvent() {
		ActionEvent evt = new ActionEvent(this, SPIN_EVENT, "spinevent");
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			ActionListener listener = (ActionListener) iter.next();
			listener.actionPerformed(evt);
		}

	}

	private void startTimer() {
		m_timer.start();
	}

	private void stopTimer() {
		m_timer.stop();
	}

}
