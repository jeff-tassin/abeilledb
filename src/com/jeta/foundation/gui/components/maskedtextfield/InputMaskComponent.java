package com.jeta.foundation.gui.components.maskedtextfield;

import java.awt.event.*;
import java.util.*;

/**
 * This is a component that makes up a TSMaskedTextField. This component can
 * accept input from keyboard and mouse
 * 
 * @author Jeff Tassin
 */
abstract public class InputMaskComponent extends MaskComponent {
	private boolean m_bSelected; // whether this component is selected or not
	private LinkedList m_listeners = new LinkedList(); // listeners interested
														// in change events

	public static final String VALUE_CHANGE_EVENT = "valuechangeevent";

	/**
	 * Adds a listener to receive change events
	 * 
	 * @param listener
	 *            the listener interested in the change events
	 * 
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	public void decrement() {

	}

	public void increment() {

	}

	/**
	 * @return true if this component is selected
	 */
	public boolean isSelected() {
		return m_bSelected;
	}

	/**
	 * This component must respond to key events.
	 * 
	 * @return true if this component handled the event or not
	 */
	public abstract boolean handleKeyEvent(KeyEvent evt);

	/**
	 * If this component supports selecting individual characters within the
	 * component, then we return the string up to the selected item.
	 * 
	 * @return the string up to the selected character within this component
	 */
	public abstract String getPreSelection();

	/**
	 * @return the selected string within this component. Because some
	 *         components allow selecting single characters (or sets of
	 *         characters) within the component, we return the string that
	 *         determines the selection.
	 */
	public abstract String getSelection(); // the selected string

	/**
	 * Notifies all action listeners of change events
	 */
	protected void notifyListeners(ActionEvent evt) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			ActionListener listener = (ActionListener) iter.next();
			listener.actionPerformed(evt);
		}
	}

	/**
	 * The method is called when this component gets focus from the component to
	 * the immediate right
	 */
	public void setFocusLeft() {
		setSelected(true);
	}

	/**
	 * This method is called when this component gets focus from the component
	 * to the immediate left
	 */
	public void setFocusRight() {
		setSelected(true);
	}

	/**
	 * Set's this item to selected
	 */
	public void setSelected(boolean bSelected) {
		m_bSelected = bSelected;
	}

	/**
	 * Set's this item to selected. This method also allows the caller to
	 * specify a digit within the string to be selected. If a derived class
	 * supports this behavior, it can override this method. Otherwise, the
	 * default is just to select the entier component
	 */
	public void setSelected(boolean bSelected, int charPos) {
		setSelected(bSelected);
	}

	/**
	 * @return the string representation of this component
	 */
	abstract public String toString();

}
