package com.jeta.abeille.gui.model;

import javax.swing.plaf.ComponentUI;
import javax.swing.JComponent;

import java.awt.*;
import javax.accessibility.Accessible;
import java.awt.event.*;

import com.jeta.foundation.gui.canvas.*;

public class TextLabelWidgetUI extends ComponentUI implements MouseListener {

	public TextLabelWidgetUI() {

	}

	protected void editingCompleted(JComponent c, boolean bResult, String sVal) {
		if (bResult) {
			TextLabelWidget w = null;
			if (c instanceof TextLabelWidget) {
				w = (TextLabelWidget) c;
				w.setText(sVal);
			}
		}
	}

	public Rectangle getEditorBoundingRect(JComponent w) {
		// return new Rectangle( w.getX() + getMarginX( w ),
		// getBoundingRectY(w), w.getWidth()*2, getLineHeight( w ) );
		return null;
	}

	public Dimension getMinimumSize(JComponent c) {
		return new Dimension(25, 25);
	}

	public Dimension getPreferredSize(JComponent c) {
		TextLabelWidget w = null;
		if (c instanceof TextLabelWidget)
			w = (TextLabelWidget) c;
		else
			return null;

		Font font = c.getFont();
		FontMetrics metrics = c.getFontMetrics(font);

		int width = 0;
		if (w.getText().length() > 15)
			width = metrics.stringWidth(w.getText().substring(15));
		else
			width = metrics.stringWidth(w.getText());

		int marginx = metrics.stringWidth("M") / 2;
		width = width + marginx * 2;
		int height = metrics.getHeight() + metrics.getDescent() * 2;

		// add shadow
		width += ShadowDecorator.SHADOW_THICKNESS;
		height += ShadowDecorator.SHADOW_THICKNESS;

		return new Dimension(width, height);
	}

	public String getText(JComponent c) {
		TextLabelWidget w = null;
		if (c instanceof TextLabelWidget) {
			w = (TextLabelWidget) c;
			return w.getText();
		} else
			return null;
	}

	public void installUI(JComponent c) {
		c.addMouseListener(this);
	}

	public void paint(Graphics g, JComponent c) {
		TextLabelWidget w = null;
		if (c instanceof TextLabelWidget)
			w = (TextLabelWidget) c;
		else
			return;

		Graphics2D bg = (Graphics2D) g;
		Font font = c.getFont();
		bg.setFont(font);
		FontMetrics metrics = bg.getFontMetrics();

		int width = ShadowDecorator.getComponentWidth(c);
		int height = ShadowDecorator.getComponentHeight(c);
		int marginx = metrics.stringWidth("M") / 2;
		int line_height = metrics.getHeight();

		bg.setPaint(c.getBackground());
		bg.fillRect(0, 0, width, height);
		int y = height - (height - line_height) / 2 - metrics.getDescent();
		bg.setColor(Color.black);
		bg.drawRect(0, 0, width, height);
		bg.setFont(font);
		bg.drawString(w.getText(), marginx, y);
		ShadowDecorator.paintComponent(g, c);
	}

	public int getBoundingRectY(JComponent c) {
		Font font = c.getFont();
		FontMetrics metrics = c.getFontMetrics(font);

		int height = ShadowDecorator.getComponentHeight(c);
		int line_height = metrics.getHeight();
		int y = height - (height - line_height) / 2 - line_height + metrics.getLeading();
		return c.getY() + y;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			System.out.println("mouse double clicked");
			// mouse double clicked, so set editing mode
			Object source = e.getSource();
			if (source instanceof TextLabelWidget) {
				TextLabelWidget w = (TextLabelWidget) source;
				// Rectangle rect = new Rectangle( w.getX() + ,
				// getBoundingRectY( w ), 25*getStringWidth( w, "M" ),
				// getLineHeight(w) );
				// startEditor( w, getText(w), getEditorBoundingRect(w) );
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

}
