package com.jeta.foundation.gui.components.maskedtextfield;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

import com.jeta.foundation.i18n.*;

/**
 * 
 * @author Jeff Tassin
 */
public abstract class TSMaskedTextField2 extends JComponent {

	private LinkedList m_fieldcomponents; // this is a list of mask components
											// that make up the field

	// standard initialization
	{
		m_fieldcomponents = new LinkedList();
	}

	/**
    */
	public TSMaskedTextField2() {
		JTextField tf = new JTextField();
		setFont(tf.getFont());
		setUI(new TSMaskedTextFieldUI());
		setBackground(Color.white);

		// A JComponent by itself will not respond to key events, so we need to
		// borrow some from JTextField
		setActionMap(tf.getActionMap());
		setInputMap(JComponent.WHEN_FOCUSED, tf.getInputMap());
	}

	/**
	 * Adds a mask to this field
	 * 
	 * @param mask
	 *            the mask component to add
	 */
	public void addMask(MaskComponent mask) {
		m_fieldcomponents.add(mask);
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
		System.out.println("navigateLeft");

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
		System.out.println("navigateRight");
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

}
