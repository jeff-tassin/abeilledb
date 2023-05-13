package com.jeta.foundation.gui.components.maskedtextfield;

import java.util.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextUI;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.*;
import javax.accessibility.Accessible;
import java.awt.event.*;
import javax.swing.text.*;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the UI class for the TSMaskedTextField
 * 
 * 
 * @author Jeff Tassin
 */
public class TSMaskedTextFieldUI extends BasicTextUI implements FocusListener, MouseListener, KeyListener {
	private static final Color SELECTED_BACKGROUND = new Color(225, 225, 255);
	private final static int HEIGHT_PADDING = 0;

	/**
    *
    */
	public TSMaskedTextFieldUI() {

	}

	public Dimension getMinimumSize(JComponent c) {
		return getPreferredSize(c);
	}

	public Dimension getPreferredSize(JComponent c) {
		TSMaskedTextField w = null;
		if (c instanceof TSMaskedTextField)
			w = (TSMaskedTextField) c;
		else
			return null;

		Font f = w.getFont();
		FontMetrics metrics = w.getFontMetrics(f);

		int width = 0;
		int height = 0;
		Iterator iter = w.getFieldComponents();
		while (iter.hasNext()) {
			MaskComponent mask = (MaskComponent) iter.next();
			String item = mask.toString();
			Rectangle2D rect = metrics.getStringBounds(item, w.getGraphics());
			width += rect.getBounds().width;
			if (height == 0)
				height = rect.getBounds().height;
		}

		Insets insets = c.getInsets();
		Dimension d = new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom
				+ HEIGHT_PADDING);
		return d;
	}

	public void installUI(JComponent c) {
		c.addMouseListener(this);
		c.addKeyListener(this);
		c.addFocusListener(this);
	}

	/**
	 * Implementation of focus listener
	 */
	public void focusGained(FocusEvent e) {
		Object sobj = e.getSource();
		if (sobj instanceof TSMaskedTextField) {
			((JComponent) sobj).repaint();
		}
	}

	/**
	 * Implementation of focus listener
	 */
	public void focusLost(FocusEvent e) {
		Object sobj = e.getSource();
		if (sobj instanceof TSMaskedTextField) {
			((JComponent) sobj).repaint();
		}
	}

	/**
	 * KeyListener implementation
	 */
	public void keyPressed(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			Object sobj = e.getSource();
			if (sobj instanceof TSMaskedTextField) {
				TSMaskedTextField tf = (TSMaskedTextField) sobj;
				InputMaskComponent input = tf.getSelectedInputField();
				if (input != null) {
					if (!input.handleKeyEvent(e)) {
						if (e.getKeyCode() == e.VK_LEFT || e.getKeyCode() == e.VK_RIGHT) {
							if (e.getKeyCode() == e.VK_LEFT)
								tf.navigateLeft();
							else
								tf.navigateRight();
						}
					}
				}
				tf.repaint();
			}
		}

	}

	public void keyReleased(KeyEvent e) {

	}

	public void keyTyped(KeyEvent e) {

	}

	public void update(Graphics g, JComponent c) {
		_paint(g, c);
	}

	/**
	 * Renders the masked text field
	 */
	public void _paint(Graphics g, JComponent c) {
		TSMaskedTextField w = null;
		if (c instanceof TSMaskedTextField)
			w = (TSMaskedTextField) c;
		else
			return;

		Insets insets = c.getInsets();

		Graphics2D g2 = (Graphics2D) g;
		Font font = w.getFont();
		g2.setFont(font);
		FontMetrics metrics = g2.getFontMetrics();

		int height = w.getHeight();
		int line_height = metrics.getHeight();
		int width = w.getWidth();
		int y = height - (height - line_height) / 2 - metrics.getDescent();

		Color text_color = null;
		if (w.isEnabled()) {
			text_color = w.getForeground();
			g2.setColor(w.getBackground());
		} else {
			text_color = UIManager.getColor("TextField.inactiveForeground");
			g2.setColor(UIManager.getColor("TextField.inactiveBackground"));
		}

		g2.fillRect(0, 0, width, height);

		if (w.isNull()) {
			// if the masked field has a null value, then let's just display a
			// grayed out null string
			// if the component has focus, then draw selection background
			if (w.hasFocus()) {
				int focusy1 = y - metrics.getAscent() - 3;
				int focusy2 = y + metrics.getDescent();
				// int highlightwidth = metrics.stringWidth( nullstr );
				g2.setColor(SELECTED_BACKGROUND);
				g2.fillRect(0, focusy1, width, focusy2 - focusy1);
			}
			g2.setColor(text_color);
			// g2.drawString( nullstr, insets.left, y );
		} else {
			int x = 0;
			Iterator iter = w.getFieldComponents();
			while (iter.hasNext()) {
				MaskComponent mask = (MaskComponent) iter.next();
				String item = mask.toString();
				if (mask instanceof InputMaskComponent) {
					InputMaskComponent inputcomp = (InputMaskComponent) mask;
					// if the mask component is selected, then fill the
					// selection rectangle
					if (inputcomp.isSelected()) {
						int focusy1 = y - metrics.getAscent() - 3;
						int focusy2 = y + metrics.getDescent();

						// the input component may allow selecting a single
						// digit within the item.
						// so, let's see if that is the case and handle
						// accordingly
						String preselection = inputcomp.getPreSelection(); // the
																			// string
																			// before
																			// the
																			// selected
																			// digit
																			// (can
																			// be
																			// empty)
						String selection = inputcomp.getSelection(); // the
																		// selected
																		// string

						int xoffset = 0;
						if (preselection != null && preselection.length() > 0)
							xoffset = metrics.stringWidth(preselection);

						int highlightwidth = metrics.stringWidth(selection);
						// if the component has focus, then draw selection
						// background
						if (w.hasFocus()) {
							g2.setColor(SELECTED_BACKGROUND);
							g2.fillRect(x + xoffset, focusy1, highlightwidth, focusy2 - focusy1);

							// g2.setColor( Color.blue );
							// g2.drawRect( x + xoffset, focusy1,
							// highlightwidth, focusy2 - focusy1);
						}
					}
				}
				g2.setColor(text_color);
				g2.drawString(item, x, y);
				x += metrics.stringWidth(item);
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		TSMaskedTextField w = (TSMaskedTextField) e.getSource();
		Font f = w.getFont();
		FontMetrics metrics = w.getFontMetrics(f);
		int mousex = e.getX();
		// find the InputMaskComponent that is closest to x
		int mindelta_x = -1;
		int x1 = 0;
		int x2 = 0;

		int selected_pos = 0; // this determines the digit within the component
								// that we clicked

		InputMaskComponent inputcomponent = null;
		Iterator iter = w.getFieldComponents();
		while (iter.hasNext()) {
			MaskComponent mask = (MaskComponent) iter.next();
			String item = mask.toString();
			x2 = x1 + metrics.stringWidth(item);
			if (mask instanceof InputMaskComponent) {
				if (mousex >= x1 && mousex <= x2) {
					// then this is it
					inputcomponent = (InputMaskComponent) mask;
					// okay, if we hit the component directly, then find out the
					// closest digit we clicked on
					int xpos = x1;
					for (int index = 0; index < item.length(); index++) {
						xpos += metrics.charWidth(item.charAt(index));
						if (mousex < xpos) {
							selected_pos = index;
							break;
						}
					}
					break;
				}
				int delta_x1 = Math.abs(mousex - x1);
				int delta_x2 = Math.abs(mousex - x2);

				// if first time through loop, then lets assume first element is
				// the one
				if (mindelta_x == -1) {
					mindelta_x = delta_x1;
					inputcomponent = (InputMaskComponent) mask;
					selected_pos = 0;
				}

				if (delta_x1 < mindelta_x) {
					mindelta_x = delta_x1;
					inputcomponent = (InputMaskComponent) mask;
					selected_pos = 0;
				}

				if (delta_x2 < mindelta_x) {
					mindelta_x = delta_x2;
					inputcomponent = (InputMaskComponent) mask;
					selected_pos = -1;
				}
			}
			x1 = x2;
		}

		if (inputcomponent != null) {
			w.deselectAll();
			if (e.getClickCount() == 2) {
				if (inputcomponent instanceof NumericMaskComponent) {
					((NumericMaskComponent) inputcomponent).toggleInputMode();
				}
			}
			inputcomponent.setSelected(true, selected_pos);
		}

		w.requestFocus();
		w.repaint();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	protected String getPropertyPrefix() {
		return "TextField";
	}

}
