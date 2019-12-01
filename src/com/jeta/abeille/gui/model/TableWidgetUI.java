package com.jeta.abeille.gui.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.print.PrinterGraphics;

import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.util.Iterator;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;

import com.jeta.abeille.database.model.ColumnMetaData;

import com.jeta.foundation.gui.canvas.ShadowDecorator;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.utils.TSUtils;

public class TableWidgetUI extends ComponentUI implements MouseListener, ComponentListener {
	// private HashMap m_fieldCoords = new HashMap();
	private int m_yeditPoint;
	private final static int FOOTERBARHEIGHT = 5;

	/**
	 * we can get away with this because Swing repaints should ALWAYS be done in
	 * the same thread
	 */
	private static Rectangle m_rect = new Rectangle();

	private static Dimension m_mindim = new Dimension(25, 25);

	public TableWidgetUI() {

	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		Object source = e.getSource();
		if (source instanceof TableWidget) {
			recalcLayout((TableWidget) source);
		}
	}

	public int getFieldY(TableWidget w, String fieldName) {
		Rectangle rect = null;
		int index = 0;
		if (fieldName != null) {
			Iterator iter = w.getColumns().iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				String fname = cmd.getColumnName();
				if (fieldName.equalsIgnoreCase(fname))
					break;

				index++;
			}
			rect = w.getJList().getCellBounds(index, index);
		}

		FontMetrics metrics = getFontMetrics(w);
		int y = getCaptionHeight(metrics);

		if (rect == null)
			y = -y / 2;
		else
			y = y + rect.y + rect.height / 2;

		JScrollPane pane = w.getScrollPane();
		Point pt = pane.getViewport().getViewPosition();
		y = y - pt.y;

		if (y < 10)
			y = 10;

		int footery = ShadowDecorator.getComponentHeight(w) - FOOTERBARHEIGHT / 2;
		if (y > footery)
			y = footery;

		return y;
	}

	public void recalcLayout(TableWidget w) {
		JComponent c = w.getListContainer();
		Font font = w.getFont();
		if (font != null) {
			FontMetrics metrics = w.getFontMetrics(font);
			int caption_height = getCaptionHeight(metrics);
			c.setLocation(1, caption_height + 1);
			c.setSize(w.getWidth() - 4, w.getHeight() - caption_height - 4 - FOOTERBARHEIGHT);
			c.doLayout();
			JScrollPane pane = (JScrollPane) c;
			int count = pane.getComponentCount();
			for (int index = 0; index < count; index++) {
				JComponent child = (JComponent) pane.getComponent(index);
				child.doLayout();
			}
		}
	}

	public void componentShown(ComponentEvent e) {
	}

	protected void editingCompleted(JComponent c, boolean bResult, String sVal) {
		if (bResult && c instanceof TableWidget) {
			TableWidget w = (TableWidget) c;
			String currentval = getFieldFromY(w, m_yeditPoint);
			if (currentval != null) {
				System.out.println("replace  " + currentval + "  with " + sVal);
			}
		}

	}

	public Rectangle getEditorBoundingRect(JComponent w, String fieldName) {
		// return new Rectangle( w.getX() + getMarginX( w ), w.getY() +
		// getFieldY(fieldName), w.getWidth()*2, getLineHeight( w ) );
		return null;
	}

	FontMetrics getFontMetrics(JComponent c) {
		Font f = c.getFont();
		return c.getFontMetrics(f);
	}

	public static TableWidget getJListParent(JList list) {
		Component c = list;
		while (c != null) {
			if (c instanceof TableWidget)
				return (TableWidget) c;
			else if (c instanceof Frame)
				return null;
			else
				c = c.getParent();
		}
		return null;
	}

	public Dimension getMinimumSize(JComponent c) {
		return m_mindim;
	}

	/**
	 * Calculates the preferred size for a widget
	 */
	public Dimension getPreferredSize(JComponent c) {
		TableWidget w = null;
		if (c instanceof TableWidget)
			w = (TableWidget) c;
		else
			return null;

		String lfield = getLongestField(w);
		lfield += "      ";
		if (lfield.length() > 25)
			lfield = lfield.substring(25);

		int lfield_len = lfield.length();
		if (lfield_len < 10)
			lfield_len = 10;

		Font font = c.getFont();
		FontMetrics metrics = c.getFontMetrics(font);

		int caption_height = getCaptionHeight(metrics);

		// TableMetaData tmd = w.getTableMetaData();
		// int columncount = tmd.getColumnCount() + 1;

		int columncount = w.getColumnCount();
		if (columncount > 10)
			columncount = 10;
		int body_height = getBodyHeight(w, metrics, columncount);

		int width = TSGuiToolbox.calculateAverageTextWidth(c, lfield_len);
		int height = body_height + caption_height;

		// add footer bar
		height += FOOTERBARHEIGHT;

		// add shadow and 10 pixels just for a little extra padding
		width = width + 10 + ShadowDecorator.SHADOW_THICKNESS;
		height += ShadowDecorator.SHADOW_THICKNESS;
		return new Dimension(width, height);
	}

	int getCaptionHeight(FontMetrics metrics) {
		int line_height = metrics.getHeight();
		return line_height + metrics.getDescent() * 3 / 2;
	}

	/**
	 * Returns the y position of the font for the caption
	 */
	int getCaptionY(FontMetrics metrics) {
		int caption_height = getCaptionHeight(metrics);
		int line_height = metrics.getHeight();
		return caption_height - (caption_height - line_height) / 2 - metrics.getDescent();
	}

	int getBodyHeight(TableWidget tw, FontMetrics metrics, int fieldCount) {
		int line_height = metrics.getHeight();
		return line_height / 2 + line_height * fieldCount;
	}

	int getFirstFieldY(FontMetrics metrics) {
		int caption_height = getCaptionHeight(metrics);
		int line_height = metrics.getHeight();
		return caption_height + line_height - metrics.getDescent() + metrics.getLeading();
	}

	public String getFieldFromY(TableWidget w, int y) {
		String result = null;
		Font font = w.getFont();
		FontMetrics metrics = w.getFontMetrics(font);
		int fieldy = getFirstFieldY(metrics) - metrics.getAscent();
		Iterator iter = w.getColumns().iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			String fieldname = cmd.getColumnName();
			if (y >= fieldy && y <= (fieldy + metrics.getHeight())) {
				result = fieldname;
				break;
			}
			fieldy += metrics.getHeight();
		}
		return result;
	}

	String getLongestField(TableWidget tw) {
		String longest = tw.getTableName();
		Iterator iter = tw.getColumns().iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			assert (cmd != null);
			String fieldname = cmd.getColumnName();
			if (longest.length() < fieldname.length())
				longest = fieldname;
		}
		return longest;
	}

	int getMarginX(JComponent c) {
		Font font = c.getFont();
		FontMetrics metrics = c.getFontMetrics(font);
		int marginx = metrics.stringWidth("M") / 2;
		return marginx;
	}

	public void installUI(JComponent c) {
		c.removeMouseListener(this);
		c.addMouseListener(this);
		// c.addComponentListener( this );
		if (c instanceof TableWidget) {
			TableWidget w = (TableWidget) c;
			JList list = w.getJList();
			list.removeMouseListener(this);
			list.addMouseListener(this);
		}
	}

	public void paint(Graphics g, JComponent c) {
		TableWidget w = null;
		if (c instanceof TableWidget)
			w = (TableWidget) c;
		else
			return;

		Font font = c.getFont();
		FontMetrics metrics = c.getFontMetrics(font);
		g.setFont(font);

		int caption_height = getCaptionHeight(metrics);
		int marginx = getMarginX(w);
		int line_height = metrics.getHeight();

		int x = 0;
		int y = 0;

		// draw caption rect
		// the caption color changes depending on whether the table exists
		TableWidgetModel model = w.getModel();
		Color captioncolor = model.getBackground();
		Color textcolor = model.getForeground();

		g.setColor(captioncolor);

		m_rect.setBounds(0, 0, ShadowDecorator.getComponentWidth(c), caption_height);
		if (model.isPaintGradient()) {
			Graphics2D g2 = (Graphics2D) g;
			Paint gradient = model.getGradient(caption_height);
			java.awt.Paint old_paint = g2.getPaint();
			g2.setPaint(gradient);
			g.fillRect(m_rect.x, m_rect.y, m_rect.width, m_rect.height);
			g2.setPaint(old_paint);
		} else {
			g.fillRect(m_rect.x, m_rect.y, m_rect.width, m_rect.height);
		}

		g.setColor(Color.black);
		g.drawRect(m_rect.x, m_rect.y, m_rect.width, m_rect.height);

		// draw body
		m_rect.setBounds(0, 0, ShadowDecorator.getComponentWidth(c), ShadowDecorator.getComponentHeight(c));
		g.drawRect(m_rect.x, m_rect.y, m_rect.width, m_rect.height);

		// print caption
		y = getCaptionY(metrics);
		int component_height = ShadowDecorator.getComponentHeight(c);

		g.setColor(textcolor);
		if (TSUtils.isDebug()) {
			g.drawString(w.getTableId().getFullyQualifiedName(), marginx, y);
		} else {
			g.drawString(w.getTableName(), marginx, y);
		}

		// draw footerbar
		int footery = ShadowDecorator.getComponentHeight(c) - FOOTERBARHEIGHT;
		int footerwidth = ShadowDecorator.getComponentWidth(c) - 1;

		// rect = new Rectangle( 1, footery , footerwidth, FOOTERBARHEIGHT );
		m_rect.setBounds(1, footery, footerwidth, FOOTERBARHEIGHT);
		g.setColor(captioncolor);
		g.fillRect(m_rect.x, m_rect.y, m_rect.width, m_rect.height);
		g.setColor(Color.black);
		g.drawLine(1, footery, footerwidth, footery);
		/** if we are printing, don't show the 3D footer bar */
		if (!(g instanceof PrinterGraphics)) {
			g.setColor(Color.white);
			g.drawLine(1, footery + 1, footerwidth, footery + 1);
			g.drawLine(1, footery + 1, 1, footery + FOOTERBARHEIGHT - 1);
			g.setColor(Color.gray);
			g.drawLine(2, footery + FOOTERBARHEIGHT - 1, footerwidth, footery + FOOTERBARHEIGHT - 1);
		}
		ShadowDecorator.paintComponent(g, c);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			// mouse double clicked, so set editing mode
			Object source = e.getSource();
			if (source instanceof TableWidget) {
				TableWidget w = (TableWidget) source;
				// startEditor( source, getTextAt( source, e.getY() );
				startCaptionEditor(w, e.getY());
			} else if (source instanceof JList) {
				if (true)
					return;

				JList list = (JList) source;
				int index = list.getSelectedIndex();
				Point pt = list.indexToLocation(index);

				FontMetrics metrics = getFontMetrics(list);
				TableWidget w = getJListParent(list);
				if (w != null) {
					// Frame f = getParentFrame(w);
					pt = SwingUtilities.convertPoint(list, pt, w);
					Rectangle cellrect = list.getCellBounds(0, 0);
					Rectangle rect = new Rectangle(w.getX() + pt.x + 1, pt.y + w.getY(), w.getWidth() * 2,
							(int) cellrect.getHeight());
					// startEditor( w, (String)list.getSelectedValue(), rect );
					// this.m_editField.setFont( list.getFont() );
				}
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	void startCaptionEditor(TableWidget w, int y) {
		FontMetrics metrics = getFontMetrics(w);
		if (y <= getCaptionHeight(metrics)) {
			// ok, we can edit the caption
			int recty = getCaptionY(metrics) - metrics.getAscent();
			// Rectangle rect = new Rectangle( w.getX() + getMarginX(w), recty +
			// w.getY(), w.getWidth()*2, metrics.getHeight() );
			// startEditor( w, w.getTableName(), rect );
		}
	}

}
