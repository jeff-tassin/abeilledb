/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.open.gui.framework.JETAPanel;
import com.jeta.open.gui.utils.JETAToolbox;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class TSPanel extends JETAPanel {
	private boolean m_editable = true;

	private LinkedList m_controllers;

	public TSPanel() {

	}

	public TSPanel(LayoutManager layout) {
		super(layout);
	}

	public void addController(TSController controller) {
		if (m_controllers == null)
			m_controllers = new LinkedList();

		m_controllers.add(controller);
	}

	public JButton createButton(Icon icon, String actionCmd) {
		JButton item = new JButton();
		item.setActionCommand(actionCmd);
		item.setName(actionCmd);
		if (icon != null)
			item.setIcon(icon);

		return item;
	}

	public JButton createButton(String itemText, String actionCmd) {
		JButton btn = new JButton(itemText);
		btn.setActionCommand(actionCmd);
		btn.setName(actionCmd);

		return btn;
	}

	public JButton createButton(Icon icon, String itemText, String actionCmd) {
		JButton btn = new JButton(itemText);
		btn.setActionCommand(actionCmd);
		btn.setName(actionCmd);
		if (icon != null)
			btn.setIcon(icon);

		return btn;
	}

	/**
	 * Creates a menu item and sets this panel as the action listener. This is
	 * mainly intended to support reusable panels that have popup menus.
	 * 
	 * @param itemText
	 *            the text to show for the menu item
	 * @param actionCmd
	 *            the name of the action that is fired when the menu item is
	 *            selected
	 * @param keyStroke
	 *            the keyboard accelerator
	 */
	public JMenuItem createMenuItem(String itemText, String actionCmd, KeyStroke keyStroke) {
		JMenuItem item = new JMenuItem(itemText);
		item.setActionCommand(actionCmd);
		item.setName(actionCmd);
		if (keyStroke != null)
			item.setAccelerator(keyStroke);

		return item;
	}

	/**
	 * @return the color for the inkwell that has the given name
	 */
	public Color getColor(String compId) {
		Component comp = getComponentByName(compId);
		if (comp instanceof JETAInkWellEx)
			return ((JETAInkWellEx) comp).getColor();
		else if (comp instanceof JETAColorWell)
			return ((JETAColorWell) comp).getColor();
		else
			return null;
	}

	/**
	 * Creates a button for this pane.
	 */
	public JButton i18n_createButton(String txt, String cmdId) {
		if (txt != null) {
			txt = I18N.getLocalizedMessage(txt);
		}
		return createButton(txt, cmdId);
	}

	/**
	 * Creates a button for this pane.
	 * 
	 * @param txt
	 *            the locale text identifier of the text to set for the button.
	 *            The actual text string is loaded from the
	 *            Messages_xx_XX.properties file
	 * @param cmdId
	 *            the command id to set for this button
	 * @param imageName
	 *            the name of an image to load and set for this button's icon
	 */
	public JButton i18n_createButton(String txt, String cmdId, String imageName) {
		if (txt != null) {
			txt = I18N.getLocalizedMessage(txt);
		}
		JButton button = createButton(txt, cmdId);
		if (imageName != null)
			button.setIcon(TSGuiToolbox.loadImage(imageName));
		return button;
	}

	/**
	 * Helper method to create a button intended for a toolbar that might be
	 * embedded in the panel.
	 */
	protected JButton i18n_createToolBarButton(String iconName, String id, String tooltip) {
		JButton btn = i18n_createButton(null, id, iconName);
		if (tooltip != null)
			btn.setToolTipText(tooltip);

		if (!JETAToolbox.isOSX()) {
			btn.setMargin(new java.awt.Insets(1, 2, 1, 2));
		}
		return btn;
	}

	/**
	 * Creates a menu item and sets this panel as the action listener. This is
	 * mainly intended to support reusable panels that have popup menus.
	 * 
	 * @param itemText
	 *            the locale text identifier of the text to set for the menu
	 *            item The actual text is loaded from the
	 *            Messages_xx_XX.properties file
	 * @param actionCmd
	 *            the name of the action that is fired when the menu item is
	 *            selected
	 * @param keyStroke
	 *            the keyboard accelerator
	 */
	public JMenuItem i18n_createMenuItem(String itemText, String actionCmd, KeyStroke keyStroke) {
		itemText = I18N.getLocalizedMessage(itemText);
		return createMenuItem(itemText, actionCmd, keyStroke);
	}

	/**
	 * @return true if the user can make changes to this panel
	 */
	public boolean isEditable() {
		return m_editable;
	}

	public void setColor(String compId, Color c) {
		Component comp = getComponentByName(compId);
		if (comp instanceof JETAInkWellEx)
			((JETAInkWellEx) comp).setColor(c);
		else if (comp instanceof JETAColorWell)
			((JETAColorWell) comp).setColor(c);
	}

	/**
	 * Sets the component name and registers it with the comopnent finder.
	 * Normally you don't have to call this method. The only time it is needed
	 * is when you need to locate components by name before they are added to
	 * the container. This happens sometimes during initialization.
	 */
	private void setComponentName(Component comp, String name) {
		comp.setName(name);
		// m_finder.registerComponent( comp );
		assert (false);
	}

	/**
	 * Sets the editable flag for this panel. Controls whether the user can edit
	 * the entries in the panel or not. Derived classes should override this
	 * method to provide specific behavior
	 */
	public void setEditable(boolean bEditable) {
		m_editable = bEditable;
		// if ( getController() != null )
		// getController().updateComponents();
	}

}
