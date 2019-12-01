/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.calendar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Calendar;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import com.jeta.foundation.gui.canvas.ShadowDecorator;
import com.jeta.foundation.i18n.I18N;

/**
 * This is a widget class that displays a Gregorian calendar. It support Locales
 * 
 * 
 * FYI: Day of week: 1 = Sunday, 2 = Monday, 3 = Tuesday, 4 = Wednesday, 5 =
 * Thursday, 6 = Friday, 7 = Saturday
 * 
 * January = 0
 * 
 * @author Jeff Tassin
 */
public class CalendarWidgetUI extends ComponentUI implements MouseListener, KeyListener, FocusListener {
	private static final int CELL_SPACING = 0;
	private static final String[] m_weekday = new String[7];

	private static final Color selectedColor = new Color(0, 0, 128);
	private static final Color sundayColor = new Color(225, 225, 225);

	static {
		m_weekday[0] = I18N.getLocalizedMessage("Sun");
		m_weekday[1] = I18N.getLocalizedMessage("Mon");
		m_weekday[2] = I18N.getLocalizedMessage("Tue");
		m_weekday[3] = I18N.getLocalizedMessage("Wed");
		m_weekday[4] = I18N.getLocalizedMessage("Thu");
		m_weekday[5] = I18N.getLocalizedMessage("Fri");
		m_weekday[6] = I18N.getLocalizedMessage("Sat");
	}

	public CalendarWidgetUI() {

	}

	/**
	 * @return the cell width for a day cell in the calendar
	 */
	int getCellWidth(FontMetrics metrics) {
		return metrics.stringWidth("M") * 4;
	}

	/**
	 * @return the cell height for a day cell in the calendar
	 */
	int getCellHeight(FontMetrics metrics) {
		return metrics.stringWidth("M") * 3;
	}

	public Dimension getMinimumSize(JComponent c) {
		return new Dimension(25, 25);
	}

	/**
	 * @param the
	 *            font metrics used for the labels
	 * @return the height of the day of week labels at top of calendar
	 * 
	 */
	int getCaptionHeight(FontMetrics metrics) {
		return metrics.getHeight() + 10;
	}

	public Dimension getPreferredSize(JComponent comp) {
		CalendarWidget c = null;
		if (comp instanceof CalendarWidget)
			c = (CalendarWidget) comp;
		else
			return null;

		Font font = c.getFont();
		FontMetrics metrics = c.getFontMetrics(font);

		// a calendar can have 4 or 5 rows
		// cell width 3x2 characters
		int numrows = getNumRows(c.getCalendar());
		int cellwidth = getCellWidth(metrics);
		int cellheight = getCellHeight(metrics);
		return new Dimension(cellwidth * 7 + 10 + ShadowDecorator.SHADOW_THICKNESS, cellheight * numrows + 10
				+ getCaptionHeight(metrics) + ShadowDecorator.SHADOW_THICKNESS);
	}

	/**
	 * @return the number of rows in the calendar for the given month this will
	 *         always be either 5 or 6
	 */
	int getNumRows(Calendar cal) {

		return 6; // always return 6
		// int daysinmonth = getDaysInMonth( cal );
		// if (daysinmonth - (28 + 7 - getFirstWeekDayOfMonth( cal ) + 1 ) > 0 )
		// return 6;
		// else
		// return 5;
	}

	public void installUI(JComponent c) {
		c.addMouseListener(this);
		c.addKeyListener(this);
		c.addFocusListener(this);
	}

	/**
	 * Focus listener. We simply repaint when we get focus events. This allows
	 * us to redraw focus rectangle if we have focus
	 */
	public void focusGained(FocusEvent e) {
		Object comp = e.getSource();
		CalendarWidget c = null;
		if (comp instanceof CalendarWidget)
			c = (CalendarWidget) comp;
		else
			return;

		c.repaint();
	}

	/**
	 * Focus listener. We simply repaint when we get focus events. This allows
	 * us to redraw focus rectangle if we have focus
	 */
	public void focusLost(FocusEvent e) {
		Object comp = e.getSource();
		CalendarWidget c = null;
		if (comp instanceof CalendarWidget)
			c = (CalendarWidget) comp;
		else
			return;

		c.repaint();
	}

	/**
	 * KeyListener implementation
	 */
	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	/**
	 * MouseListener implementation
	 */
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {

		Object comp = e.getSource();
		CalendarWidget widget = null;
		if (comp instanceof CalendarWidget)
			widget = (CalendarWidget) comp;
		else
			return;

		int mousex = e.getX();
		int mousey = e.getY();
		FontMetrics metrics = widget.getGraphics().getFontMetrics();
		int cellwidth = getCellWidth(metrics);
		int cellheight = getCellHeight(metrics);

		int col = mousex / (cellwidth + CELL_SPACING) + 1;
		int row = (mousey - getCaptionHeight(metrics)) / (cellheight + CELL_SPACING) + 1;

		int cellnumber = (row - 1) * 7 + col;
		Calendar cal = (Calendar) widget.getCalendar().clone();
		int day = cellnumber - CalendarWidget.getFirstWeekDayOfMonth(cal) + 1;
		if (day >= 1 && day <= CalendarWidget.getDaysInMonth(cal)) {
			cal.set(Calendar.DAY_OF_MONTH, day);
			widget.setCalendar(cal);
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * Paints the calendar component
	 */
	public void paint(Graphics g, JComponent comp) {
		CalendarWidget c = null;
		if (comp instanceof CalendarWidget)
			c = (CalendarWidget) comp;
		else
			return;

		Calendar cal = c.getCalendar();

		Graphics2D g2 = (Graphics2D) g;
		Font font = c.getFont();
		g2.setFont(font);
		FontMetrics metrics = g2.getFontMetrics();

		int cellwidth = getCellWidth(metrics);
		int cellheight = getCellHeight(metrics);
		g2.setColor(Color.black);

		// draw captions
		for (int index = 0; index < 7; index++) {
			int captionwidth = metrics.stringWidth(m_weekday[index]);
			int captiony = metrics.getHeight() - metrics.getDescent() + 5;
			g2.drawString(m_weekday[index], (cellwidth - captionwidth) / 2 + cellwidth * index, captiony);
		}

		int numrows = getNumRows(cal);

		int currentday = cal.get(Calendar.DAY_OF_MONTH);
		int x = 0;
		int y = 0;
		int firstweekday = CalendarWidget.getFirstWeekDayOfMonth(cal);
		// System.out.println( "firstweekday = " + firstweekday );
		int daysinmonth = CalendarWidget.getDaysInMonth(cal);
		for (int day = 1; day <= daysinmonth; day++) {
			cal.set(Calendar.DAY_OF_MONTH, day);
			int corrected_day = firstweekday + day - 1;

			int row = (corrected_day - 1) / 7;
			x = ((corrected_day - 1) % 7) * (cellwidth + CELL_SPACING);
			y = (cellheight + CELL_SPACING) * row + getCaptionHeight(metrics);
			if (corrected_day >= firstweekday) {
				if (day == currentday)
					drawCell(g2, cal, x, y, metrics, true);
				else
					drawCell(g2, cal, x, y, metrics, false);
			}
		}

		if (c.hasFocus()) {
			int row = (currentday - 1) / 7;
			x = ((currentday - 1) % 7) * (cellwidth + CELL_SPACING);
			y = (cellheight + CELL_SPACING) * row + getCaptionHeight(metrics);
			g2.setColor(Color.blue);
			g2.drawRect(x, y, cellwidth, cellheight);
		}

		cal.set(Calendar.DAY_OF_MONTH, currentday);
	}

	void drawCell(Graphics2D g2, Calendar cal, int x, int y, FontMetrics metrics, boolean bSelected) {

		int cellwidth = getCellWidth(metrics);
		int cellheight = getCellHeight(metrics);

		int texty = y + 5 + metrics.getHeight() - metrics.getDescent();
		int textx = x + 5;

		int day = cal.get(Calendar.DAY_OF_MONTH);
		if (bSelected) {
			g2.setColor(selectedColor);
			g2.fillRect(x, y, cellwidth, cellheight);
			g2.setColor(Color.white);
			g2.drawString(String.valueOf(day), textx, texty);
		} else {
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				g2.setColor(sundayColor);
				g2.fillRect(x, y, cellwidth, cellheight);
				g2.setColor(Color.red.darker());
				g2.drawString(String.valueOf(day), textx, texty);
			} else {
				g2.setColor(Color.white);
				g2.fillRect(x, y, cellwidth, cellheight);
				g2.setColor(Color.black);
				g2.drawString(String.valueOf(day), textx, texty);
			}
		}
		g2.setColor(Color.black);
		g2.drawRect(x, y, cellwidth, cellheight);

	}

}
