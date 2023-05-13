package com.jeta.foundation.gui.components.maskedtextfield;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.awt.geom.*;
import javax.swing.text.*;

import com.jeta.foundation.i18n.*;

/**
 * 
 * @author Jeff Tassin
 */
public abstract class TSMaskedField_Hybrid extends JTextField implements FocusListener, MouseListener, KeyListener,
		ActionListener {
	private LinkedList m_fieldcomponents; // this is a list of mask components
											// that make up the field

	// standard initialization
	{
		m_fieldcomponents = new LinkedList();
	}

	/**
    */
	public TSMaskedField_Hybrid() {
		// setCaret(null);
		// addMouseListener( this );
		// addKeyListener( this );
		addFocusListener(this);
		// setDocument( new TSMaskedTextDocument() );
	}

	/**
	 * Handle selection change events for InputMaskComponents
	 */
	public void actionPerformed(ActionEvent evt) {
		setText(getText());
		Object src = evt.getSource();
		if (src instanceof InputMaskComponent) {
			InputMaskComponent inputcomp = (InputMaskComponent) src;
			if (inputcomp.isSelected()) {
				selectComponent(inputcomp);
			}
		}
		// System.out.println( "actionPerformed  TSMaskedTextField = " +
		// getText() );
		repaint();
	}

	/**
	 * Adds a mask to this field
	 * 
	 * @param mask
	 *            the mask component to add
	 */
	public void addMask(MaskComponent mask) {
		m_fieldcomponents.add(mask);
		if (mask instanceof InputMaskComponent)
			((InputMaskComponent) mask).addActionListener(this);

		setText(getText());
	}

	/**
	 * Deselects any InputMaskComponents in this field
	 */
	public void deselectAll() {

		Iterator iter = getFieldComponents();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof InputMaskComponent) {
				((InputMaskComponent) obj).setSelected(false);
			}
		}

	}

	/**
	 * @return an interator to the set of components that make up this field
	 */
	public Iterator getFieldComponents() {
		return m_fieldcomponents.iterator();
	}

	/**
	 * @return a list of input components in this field. Ordered from left to
	 *         right
	 */
	public LinkedList getInputComponents() {
		LinkedList results = new LinkedList();
		Iterator iter = getFieldComponents();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof InputMaskComponent)
				results.add(obj);
		}
		return results;
	}

	/**
	 * @return the currently selected input field. Null is returned if no input
	 *         field is found or selected
	 */
	public InputMaskComponent getSelectedInputField() {
		Iterator iter = getFieldComponents();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof InputMaskComponent) {
				InputMaskComponent input = (InputMaskComponent) obj;
				if (input.isSelected())
					return input;
			}
		}
		return null;
	}

	/**
	 * Sets the selected mask component to the one left of the current one. If
	 * we are at the left most object, then wrap around to the rightmost
	 * component. This method is generally called from TSMaskedTextFieldUI in
	 * response to left key events
	 */
	public void navigateLeft() {
		// System.out.println( "navigateLeft" );

		int pos = 0;
		LinkedList inputcomponents = getInputComponents();
		ListIterator iter = inputcomponents.listIterator(0);
		while (iter.hasNext()) {
			InputMaskComponent input = (InputMaskComponent) iter.next();
			if (input.isSelected()) {
				input.setSelected(false);
				if (pos == 0) {
					input = (InputMaskComponent) inputcomponents.getLast();
					input.setFocusLeft();
				} else {
					iter.previous();
					input = (InputMaskComponent) iter.previous();
					input.setFocusLeft();
				}
				break;
			}
			pos++;
		}
	}

	/**
	 * Sets the selected mask component to the one right of the current one. If
	 * we are at the right most object, then wrap around to the leftmost
	 * component. This method is generally called from TSMaskedTextFieldUI in
	 * response to right key events
	 */
	public void navigateRight() {
		// System.out.println( "navigateRight" );
		int pos = 0;
		LinkedList inputcomponents = getInputComponents();
		ListIterator iter = inputcomponents.listIterator(0);
		while (iter.hasNext()) {
			InputMaskComponent input = (InputMaskComponent) iter.next();
			if (input.isSelected()) {
				input.setSelected(false);
				if (pos == (inputcomponents.size() - 1)) {
					input = (InputMaskComponent) inputcomponents.getFirst();
					input.setFocusRight();
				} else {
					input = (InputMaskComponent) iter.next();
					input.setFocusRight();
				}
				break;
			}
			pos++;
		}
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public Dimension getPreferredSize() {
		Font f = getFont();
		FontMetrics metrics = getFontMetrics(f);
		Rectangle2D rect = metrics.getStringBounds(getText(), getGraphics());
		int width = rect.getBounds().width;
		Dimension d = super.getPreferredSize();
		// d = new Dimension( width, d.height );
		return d;
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

	public String getText() {
		StringBuffer buff = new StringBuffer();
		Iterator iter = getFieldComponents();
		while (iter.hasNext()) {
			MaskComponent mask = (MaskComponent) iter.next();
			buff.append(mask.toString());
		}
		return buff.toString();
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

	public void processKeyEvent(KeyEvent evt) {
		if (evt.getID() == KeyEvent.KEY_PRESSED)
			keyPressed(evt);
	}

	public void processMouseEvent(MouseEvent evt) {
		if (evt.getID() == MouseEvent.MOUSE_PRESSED)
			mousePressed(evt);
	}

	public void processMouseMotionEvent(MouseEvent evt) {

	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void mousePressed(MouseEvent e) {

		int pos = viewToModel(new Point(e.getX(), e.getY()));
		System.out.println("clicked  pos = " + pos);

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

		System.out.println("mousepressed   inputcomp = " + inputcomponent.toString());
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

	public void setCursor(Cursor cur) {
		// eat this for now, so that we don't get the standard text component
		// cursor
	}

	/**
	 * Highlights the selected component
	 */
	public void selectComponent(InputMaskComponent inputComp) {
		System.out.println("---- select component -------");
		select(0, 0); // deselect all
		int xpos1 = 0;
		Iterator iter = m_fieldcomponents.iterator();
		while (iter.hasNext()) {
			MaskComponent comp = (MaskComponent) iter.next();
			String str = comp.toString();
			int xpos2 = str.length();
			if (comp == inputComp) {
				select(xpos1, xpos1 + xpos2);
				System.out.println("selectComponent event!!!  inputComp = " + inputComp.toString() + " xstart = "
						+ xpos1 + " xend = " + (xpos2 + xpos1));
				break;
			}
			xpos1 += xpos2;
		}
	}

	/**
	 * public class TSMaskedTextContent extends AbstractDocument.Content {
	 * Position createPosition(int offset) {
	 * 
	 * }
	 * 
	 * void getChars(int where, int len, Segment txt) {
	 * 
	 * }
	 * 
	 * String getString(int where, int len) {
	 * 
	 * }
	 * 
	 * UndoableEdit insertString(int where, String str) {
	 * 
	 * }
	 * 
	 * int length() {
	 * 
	 * }
	 * 
	 * UndoableEdit remove(int where, int nitems) {
	 * 
	 * } }
	 */
}
