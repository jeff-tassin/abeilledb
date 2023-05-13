package com.jeta.foundation.gui.components.maskedtextfield;

import java.util.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * 
 * @author Jeff Tassin
 */
public abstract class TSMaskedTextField extends JTextComponent {
	private LinkedList m_fieldcomponents; // this is a list of mask components
											// that make up the field
	private HashMap m_masks; // so we can locate by id
	private boolean m_isNull; // we allow the field to contain null values
	private boolean m_bsetdefaultMask; // determines if we set the default input
										// mask or not

	// standard initialization
	{
		m_isNull = false;
		m_fieldcomponents = new LinkedList();
		m_masks = new HashMap();
		m_bsetdefaultMask = false;
	}

	/**
    */
	public TSMaskedTextField() {
		JTextField tf = new JTextField();
		setFont(tf.getFont());
		setBackground(javax.swing.UIManager.getColor("TextField.background"));
		setFocusable(true);

		// A JComponent by itself will not respond to key events, so we need to
		// borrow some from JTextField
		// setActionMap( tf.getActionMap() );
		// setInputMap( JComponent.WHEN_FOCUSED, tf.getInputMap() );

		// setBorder( BorderFactory.createEtchedBorder() );
		TSGuiToolbox.setTextFieldBorder(this);

		setRequestFocusEnabled(true);
		setCaret(new MyCaret());
	}

	/**
	 * Adds a mask to this field
	 * 
	 * @param mask
	 *            the mask component to add
	 */
	public void addMask(MaskComponent mask) {
		// make the first component selected if it is an input component
		if (!m_bsetdefaultMask) {
			if (getInputComponents().size() == 0 && mask instanceof InputMaskComponent) {
				((InputMaskComponent) mask).setSelected(true);
				m_bsetdefaultMask = true;
			}
		}
		m_fieldcomponents.add(mask);
		m_masks.put(mask.getId(), mask);
	}

	/**
	 * @return the mask with the specified id null is returned if mask cannot be
	 *         found
	 */
	public MaskComponent getMask(String id) {
		return (MaskComponent) m_masks.get(id);
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

	public boolean isFocusable() {
		return true;
	}

	/**
	 * @return true if this field is currently null
	 */
	public boolean isNull() {
		return m_isNull;
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

	/**
	 * Sets the value of this field to null. The field will display null as well
	 * 
	 * @param isNull
	 *            true if you want this field to be null, false otherwise
	 */
	public void setNull(boolean isNull) {
		m_isNull = isNull;
		repaint();
	}

	public void updateUI() {
		if (!(getUI() instanceof TSMaskedTextFieldUI)) {
			setUI(new TSMaskedTextFieldUI());
		}
		invalidate();
	}

	public static class MyCaret implements Caret {
		public void addChangeListener(javax.swing.event.ChangeListener l) {
		}

		public void deinstall(JTextComponent c) {
		}

		public int getBlinkRate() {
			return 0;
		}

		public int getDot() {
			return 0;
		}

		public Point getMagicCaretPosition() {
			return new Point(0, 0);
		}

		public int getMark() {
			return 0;
		}

		public void install(JTextComponent c) {
		}

		public boolean isSelectionVisible() {
			return true;
		}

		public boolean isVisible() {
			return true;
		}

		public void moveDot(int dot) {
		}

		public void paint(Graphics g) {
		}

		public void removeChangeListener(javax.swing.event.ChangeListener l) {
		}

		public void setBlinkRate(int rate) {
		}

		public void setDot(int dot) {
		}

		public void setMagicCaretPosition(Point p) {
		}

		public void setSelectionVisible(boolean v) {
		}

		public void setVisible(boolean v) {
		}
	}
}
