package com.jeta.abeille.gui.update;

import javax.swing.text.JTextComponent;

/**
 * This interface is implemented by various InstanceComponents that use
 * JTextComponent types (either JTextField or JTextArea) as their data handling
 * components. For these JTextComponents, we need a way to determine if they
 * represent null data or an empty string. This method is used for that purpose
 * because getText() will not provide this distinction.
 * 
 * @author Jeff Tassin
 */
public interface InstanceTextAdapter {
	/**
	 * @return the underlying text component
	 */
	public JTextComponent getTextComponent();
}
