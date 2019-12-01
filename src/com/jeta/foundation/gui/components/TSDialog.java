/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Dialog;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.KeyStroke;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.gui.framework.JETADialog;
import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.JETAController;
import com.jeta.open.gui.framework.UIDirector;

import com.jeta.open.gui.utils.JETAToolbox;

import com.jeta.open.support.ComponentFinder;
import com.jeta.open.support.CompositeComponentFinder;
import com.jeta.open.support.DefaultComponentFinder;

/**
 * This is a base class for all dialogs in the system. It provides a skeleton
 * dialog box with a cancel and ok button. Callers can add their own containers
 * to this dialog's content panel.
 * 
 * @author Jeff Tassin
 */
public class TSDialog extends JETADialog implements TSMenuBuilder, JETAContainer {
	private JMenuBar m_menuBar;
	private JToolBar m_toolBar;

	private CompositeComponentFinder m_finder;
	private UIDirector m_uidirector;

	private TSToolBarTemplate m_toolbarTemplate = new TSDialogToolBarTemplate();
	private MenuTemplate m_menuTemplate = new TSDialogMenuTemplate();

	public TSDialog(Dialog owner, boolean bModal) {
		super(owner, bModal);
	}

	public TSDialog(Frame owner, boolean bModal) {
		super(owner, bModal);
	}

	protected void _initialize() {
		super._initialize();

		m_menuBar = new JMenuBar();
		m_toolBar = new JToolBar();

		m_finder = new CompositeComponentFinder();

		m_finder.add(new DefaultComponentFinder(m_menuBar));
		m_finder.add(new DefaultComponentFinder(m_toolBar));
		m_finder.add(new DefaultComponentFinder(getContentPane()));
	}

	/**
	 * Implemenation of TSMenuBuilder interface
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
	 * Implementation of TSToolBarBuilder interface
	 */
	public JButton createToolBarButton(String cmdId, String imageName, String toolTip) {
		JButton button = new JButton(TSGuiToolbox.loadImage(imageName)) {
			public boolean isFocusTraversable() {
				return false;
			}
		};

		button.setActionCommand(cmdId);
		button.setName(cmdId);
		if (!JETAToolbox.isOSX()) {
			button.setMargin(new java.awt.Insets(1, 2, 1, 2));
		}

		if (toolTip != null)
			button.setToolTipText(toolTip);
		return button;
	}

	/**
	 * Enables/Disables all menu/toolbar buttons on the frame
	 * 
	 * @param bEnable
	 *            true/false to enable/disable
	 */
	public void enableAll(boolean bEnable) {

	}

	/**
	 * Creates and enables the menu bar for this dialog. This is a one time
	 * method that you should call immediately after creating the dialog (if you
	 * want a menu bar). Most dialogs don't have menus so we don't automatically
	 * do this for every dialog.
	 */
	public void enableMenuBar() {
		m_menuTemplate = new TSDialogMenuTemplate();
		setJMenuBar(m_menuBar);
	}

	/**
	 * Creates and enables the tool bar for this dialog. This is a one time
	 * method that you should call immediately after creating the dialog (if you
	 * want a tool bar). Most dialogs don't have menus so we don't automatically
	 * do this for every dialog.
	 */
	public void enableToolBar() {
		m_toolbarTemplate = new TSDialogToolBarTemplate();
		m_toolBar.setFloatable(false);
		getContentPane().add(m_toolBar, BorderLayout.NORTH);
	}

	public UIDirector getUIDirector() {
		return m_uidirector;
	}

	public void setUIDirector(UIDirector uidirector) {
		m_uidirector = uidirector;
	}

	public void enableComponent(String commandId, boolean bEnable) {
		Collection comps = getComponentsByName(commandId);
		Iterator iter = comps.iterator();
		while (iter.hasNext()) {
			Component comp = (Component) iter.next();
			if (bEnable != comp.isEnabled())
				comp.setEnabled(bEnable);
		}
	}

	public Component getComponentByName(String compName) {
		return m_finder.getComponentByName(compName);
	}

	public Collection getComponentsByName(String compName) {
		return m_finder.getComponentsByName(compName);
	}

	/**
	 * Returns the topmost parent frame window for the application
	 * 
	 * @return the topmost parent frame window
	 */
	public JFrame getParentFrame() {
		return TSWorkspaceFrame.getInstance();
	}

	public JMenuBar getMenuBar() {
		return m_menuBar;
	}

	public TSToolBarTemplate getToolBarTemplate() {
		return m_toolbarTemplate;
	}

	public MenuTemplate getMenuTemplate() {
		return m_menuTemplate;
	}

	/**
	 * For debugging
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		if (TSUtils.isDebug()) {
			TSUtils.printDebugMessage("TSDialog.finalize: " + getTitle());
		}
	}

	public JMenuItem i18n_createMenuItem(String itemText, KeyStroke keyAccelerator) {
		return i18n_createMenuItem(itemText, itemText, keyAccelerator);
	}

	public JMenuItem i18n_createMenuItem(String itemText, String actionCmd, KeyStroke keyAccelerator) {
		return createMenuItem(I18N.getLocalizedMessage(itemText), actionCmd, keyAccelerator);
	}

	/**
	 * Creates a toolbar button on this dialogs toolbar. The toolTip is
	 * automatically loaded from the locale resource file. Note that you must
	 * call enableToolBar for the toolbar to show up on the dialog
	 */
	public JButton i18n_createToolBarButton(String cmd, String imageFile, String toolTip) {
		return createToolBarButton(cmd, imageFile, I18N.getLocalizedMessage(toolTip));
	}

	/**
	 * Shows a close hyperlink at the bottom of the dialog instead of the close
	 * button. This is for dialogs with no ok button.
	 */
	public void showCloseLink2() {
		javax.swing.JLabel label = new javax.swing.JLabel("<html><body><u>Close</u></body></html>");
		label.setIcon(TSGuiToolbox.loadImage("incors/16x16/close.png"));
		label.setHorizontalTextPosition(javax.swing.JLabel.LEFT);
		label.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

		java.awt.Container btnpanel = getButtonPanel();
		btnpanel.removeAll();
		label.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 10, 10));
		btnpanel.add(label);

		label.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				cmdCancel();
			}
		});
	}

	/**
	 * Shows a close hyperlink at the bottom of the dialog instead of the close
	 * button. This is for dialogs with no ok button.
	 */
	public void showCloseLink() {
		JButton btn = getOkButton();
		btn.setVisible(false);

		btn = getCloseButton();
		btn.setText(I18N.getLocalizedMessage("Close"));
	}

	public void setCommandId(AbstractButton btn, String id) {
		btn.setActionCommand(id);
		btn.setName(id);
	}

	public void setBottomPanelVisible(boolean bvis) {
		getButtonPanel().setVisible(bvis);
	}

	/**
	 * Updates the components in the dialog based on the model state.
	 */
	public void updateComponents(java.util.EventObject evt) {
		if (m_uidirector != null)
			m_uidirector.updateComponents(evt);
		super.updateComponents(evt);
	}

	/**
	 * This is the toolbar template for the dialog.
	 */
	class TSDialogToolBarTemplate extends TSToolBarTemplate {
		public void add(Component c) {
			super.add(c);
			m_toolBar.add(c);
		}
	}

	/**
	 * This is the menu template for the dialog
	 */
	class TSDialogMenuTemplate extends BasicMenuTemplate {
		public void add(MenuDefinition menu) {
			super.add(menu);
			m_menuBar.add(menu.createMenu());
		}
	}
}
