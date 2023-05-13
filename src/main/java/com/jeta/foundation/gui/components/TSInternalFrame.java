/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.JInternalFrame;
import java.lang.reflect.Method;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.TSComponent;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.JETAController;

import com.jeta.open.gui.framework.UIDirector;
import com.jeta.open.gui.utils.JETAToolbox;

/**
 * This is the base class for all internal frames in the application. It
 * provides toolbar, menu, and command handling support. This class implements
 * two interfaces: TSComponent and TSWindow TSComponent is used to get
 * startup/shutdown events from the application. TSWindow is used to allow
 * arbitrary controllers to enable/disable components (toolbar buttons and
 * menus)
 * 
 * @author Jeff Tassin
 */
public abstract class TSInternalFrame implements JETAContainer, TSComponent, TSToolBarBuilder, TSMenuBuilder {
	private String m_group; // the group identifier for this window

	private JETAController m_controller;
	private LinkedList m_controllers = new LinkedList(); // for multiple
															// controllers

	private boolean m_singleton = false; // true if there should be only one
											// instance of this particular frame
	private String m_shortTitle; // an abbreviated title caption text

	/**
	 * this is an id that allows client code to locate windows belonging to the
	 * same class
	 */
	private Object m_windowId;

	/**
	 * this is either a JInternalFrameEx (for internal frames) or a JFrameEx for
	 * destktop frames
	 */
	private WindowDelegate m_delegate;

	private LinkedList m_framelisteners = new LinkedList();

	/** the icon for the frame */
	private Icon m_frameicon;

	/** the preferred size */
	private Dimension m_preferredsize = new Dimension(600, 400);

	private JFrameListener m_window_listener = new JFrameListener(this);

	public TSInternalFrame(String titleCaption) {
		TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
		JETAFrameManager framemgr = wsframe.getFrameManager();
		m_delegate = framemgr.createWindow(this, titleCaption);
		if (m_delegate instanceof JFrameEx)
			((JFrameEx) m_delegate).addWindowListener(m_window_listener);
	}

	/**
	 * Called when this frame has been activated
	 */
	protected void activated() {
		// no op
	}

	/**
	 * Adds a controller to the list of controllers that can handle events for
	 * this dialog. When an menu or toolbar event occurs, the event is routed to
	 * each controller added to this frame. Once a controller is found that
	 * handles the event, the event routing is considered complete and no other
	 * controllers are evaluated.
	 * 
	 * @param controller
	 *            the controller to add
	 */
	public void addController(JETAController controller) {
		if (m_controller == null)
			m_controller = controller;
		m_controllers.add(controller);
	}

	/**
	 * Adds a listener to this frame window
	 */
	public void addFrameListener(JETAFrameListener listener) {
		m_framelisteners.add(listener);
	}

	public Rectangle getBounds() {
		return m_delegate.getBounds();
	}

	public Container getContentPane() {
		return m_delegate.getContentPane();
	}

	/**
	 * @return the frame window we are wrapping
	 */
	public Container getDelegate() {
		return (Container) m_delegate;
	}

	public Collection getFrameListeners() {
		return m_framelisteners;
	}

	public Point getLocationOnScreen() {
		return m_delegate.getLocationOnScreen();
	}

	public Dimension getPreferredSize() {
		return m_delegate.getPreferredSize();
	}

	public int getX() {
		return m_delegate.getX();
	}

	public int getY() {
		return m_delegate.getY();
	}

	public JToolBar getToolBar() {
		return m_delegate.getToolBar();
	}

	protected void addMenu(MenuDefinition menu) {
		getMenuTemplate().add(menu);
	}

	/**
	 * Implemenation of TSMenuBuilder interface
	 */
	public JMenuItem createMenuItem(String itemText, String actionCmd, KeyStroke keyStroke) {
		JMenuItem item = new JMenuItem(itemText);
		item.setName(actionCmd);
		item.setActionCommand(actionCmd);
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

		button.setName(cmdId);
		button.setActionCommand(cmdId);

		if (!JETAToolbox.isOSX()) {
			button.setMargin(new java.awt.Insets(1, 2, 1, 2));
		}

		if (toolTip != null)
			button.setToolTipText(toolTip);
		return button;
	}

	protected void dispose() {
		TSUtils.printDebugMessage("TSInternalFrame.dispose: " + getTitle());

		JETAComponentCleanser cleanser = new JETAComponentCleanser();
		if (m_delegate instanceof Container)
			cleanser.cleanse((Container) m_delegate);
		else {
			System.out.println("unable to cleanse delegate: " + m_delegate);
		}
		m_delegate.dispose();
		m_windowId = null;
		m_framelisteners.clear();
	}

	protected void finalize() throws Throwable {
		super.finalize();
		if (TSUtils.isDebug()) {
			TSUtils.printDebugMessage("TSInternalFrame.finalize: " + getTitle());
		}
	}

	/**
	 * Implementation of TSWindow.enableAll Enables/Disables all menu/toolbar
	 * buttons on the frame
	 * 
	 * @param bEnable
	 *            true/false to enable/disable
	 */
	public void enableAll(EnableEnum ee, boolean bEnable) {
		if (ee == null)
			return;

		if (ee.isEnableMenus()) {
			MenuTemplate mt = m_delegate.getMenuTemplate();
			for (int index = 0; index < mt.getMenuCount(); index++) {
				MenuDefinition mdef = mt.getMenuAt(index);
				for (int pos = 0; pos < mdef.getItemCount(); pos++) {
					Object obj = mdef.getItem(pos);
					if (obj instanceof JMenuItem)
						((JMenuItem) obj).setEnabled(bEnable);
					else if (obj instanceof MenuDefinition) {
						assert (false);
					}
				}
			}
		}

		if (ee.isEnableToolBars()) {
			TSToolBarTemplate tt = m_delegate.getToolBarTemplate();
			for (int index = 0; index < tt.getComponentCount(); index++) {
				Component comp = tt.getComponentAt(index);
				if (comp instanceof Container) {
					enableContainer((Container) comp, bEnable);
				} else
					comp.setEnabled(bEnable);
			}
		}

		if (ee.isEnableContent()) {
			Container cc = m_delegate.getContentPane();
			enableContainer(cc, bEnable);
		}
	}

	public static void enableContainer(Container cc, boolean bEnable) {
		if (cc == null)
			return;

		cc.setEnabled(bEnable);
		if (cc instanceof JMenu) {
			JMenu menu = (JMenu) cc;
			for (int index = 0; index < menu.getItemCount(); index++) {
				JMenuItem item = menu.getItem(index);
				enableContainer(item, bEnable);
			}
		} else {
			for (int index = 0; index < cc.getComponentCount(); index++) {
				Component comp = cc.getComponent(index);
				if (comp instanceof Container)
					enableContainer((Container) comp, bEnable);
				else
					comp.setEnabled(bEnable);
			}
		}
	}

	/**
	 * Implementatio of TSWindow.enableComponent Enables/Disables the
	 * menu/toolbar button associated with the commandid
	 * 
	 * @param commandId
	 *            the id of the command whose button/menu item to enable/disable
	 * @param bEnable
	 *            true/false to enable/disable
	 */
	public void enableComponent(String commandId, boolean bEnable) {
		m_delegate.enableComponent(commandId, bEnable);
	}

	/** return the named component */
	public Component getComponentByName(String componentName) {
		return m_delegate.getComponentByName(componentName);
	}

	/** return the named component */
	public Collection getComponentsByName(String componentName) {
		return m_delegate.getComponentsByName(componentName);
	}

	/**
	 * Override to supply your own controller
	 */
	public JETAController getController() {
		return m_controller;
	}

	/**
	 * @return an abbreviated version of the title bar text. This is mainly used
	 *         to set the text in the TWorkspace tabs and menus.
	 */
	public String getShortTitle() {
		if (m_shortTitle == null) {
			return getTitle();
		} else
			return m_shortTitle;
	}

	/**
	 * @return the frame title
	 */
	public String getTitle() {
		return m_delegate.getTitle();
	}

	/**
	 * Sets the frame icon
	 */
	public Icon getFrameIcon() {
		return m_frameicon;
	}

	/**
	 * Gets the group name for this window. This allows the framework to perform
	 * operations on a group of windows (e.g. when closing a database
	 * connection, close all open windows for that connection but not windows
	 * for other connections )
	 * 
	 * @return the name of the group that this window belongs to
	 */
	public String getGroup() {
		return m_group;
	}

	public JMenuItem getMenuItem(String commandId) {
		assert (false);
		// return (JMenuItem)m_menuMap.get( commandId );
		return null;
	}

	public JFrame getParentFrame() {
		return TSWorkspaceFrame.getInstance();
	}

	public MenuTemplate getMenuTemplate() {
		return m_delegate.getMenuTemplate();
	}

	public int getWidth() {
		return m_delegate.getWidth();
	}

	public int getHeight() {
		return m_delegate.getHeight();
	}

	/**
	 * @return the window id of this frame. This is useful for programmatically
	 *         locating an internal window when several of the same class are
	 *         visible.
	 */
	public Object getWindowId() {
		return m_windowId;
	}

	public TSToolBarTemplate getToolBarTemplate() {
		return m_delegate.getToolBarTemplate();
	}

	public JMenuItem i18n_createMenuItem(String itemText, String actionCmd, KeyStroke keyAccelerator) {
		return createMenuItem(I18N.getLocalizedMessage(itemText), actionCmd, keyAccelerator);
	}

	/***
	 * Creates a menu item that is connected to our controller routing
	 * framework.
	 * 
	 * @param itemText
	 *            the text for the item. This is a resource string that is
	 *            automatically converted to I18N
	 * @param actionCmd
	 *            the action command for the item
	 * @param keyAccelerator
	 *            the key accelerator (can be null)
	 * @param imageName
	 *            the name of an image file to set (can be null)
	 * 
	 */
	public JMenuItem i18n_createMenuItem(String itemText, String actionCmd, KeyStroke keyAccelerator, String imageName) {
		JMenuItem item = createMenuItem(I18N.getLocalizedMessage(itemText), actionCmd, keyAccelerator);
		if (imageName != null)
			item.setIcon(TSGuiToolbox.loadImage(imageName));

		return item;
	}

	public JButton i18n_createToolBarButton(String cmd, String imageFile, String toolTip) {
		return createToolBarButton(cmd, imageFile, I18N.getLocalizedMessage(toolTip));
	}

	/**
	 * This method is intended to be overridden by the derived class. It allows
	 * the caller to pass in user data for the internal frame after the frame is
	 * constructed.
	 * 
	 * @param params
	 *            an array of objects that are expected by the derived internal
	 *            frame.
	 */
	public void initializeModel(Object[] params) {
		// empty
	}

	/**
	 * @return the flag determining whether this frame's derived will have one
	 *         or multiple instances.
	 */
	public boolean isSingleton() {
		return m_singleton;
	}

	public boolean isVisible() {
		return m_delegate.isVisible();
	}

	public void pack() {
		m_delegate.pack();
	}

	/**
	 * Removes all registered controllers added to this frame
	 */
	public void removeAllControllers() {

	}

	/**
	 * Adds a listener to this frame window
	 */
	public void removeFrameListener(JETAFrameListener listener) {
		m_framelisteners.remove(listener);
	}

	public void repaint() {
		m_delegate.repaint();
	}

	public void requestFocus() {
		// no op

	}

	public void revalidate() {
		m_delegate.revalidate();
	}

	public void setBounds(int x, int y, int width, int height) {
		m_delegate.setWindowBounds(x, y, width, height);
	}

	public void setBounds(Rectangle rect) {
		m_delegate.setWindowBounds(rect.x, rect.y, rect.width, rect.height);
	}

	/**
    * 
    */
	protected void setCommandHandler(AbstractButton btn, String commandId) {
		btn.setName(commandId);
		btn.setActionCommand(commandId);
	}

	/**
	 * Sets the controller for this frame
	 */
	public void setController(JETAController controller) {
		m_controller = controller;
	}

	/**
	 * This is only needed to support docking frames
	 */
	public void setDelegate(WindowDelegate delegate) {
		if (m_delegate instanceof JFrameEx)
			((JFrameEx) m_delegate).removeWindowListener(m_window_listener);

		m_delegate = delegate;
		if (m_delegate instanceof JFrameEx)
			((JFrameEx) m_delegate).addWindowListener(m_window_listener);
	}

	/**
	 * Sets the frame icon
	 */
	public void setFrameIcon(Icon icon) {
		m_delegate.setFrameIcon(icon);
		m_frameicon = icon;
	}

	/**
	 * Sets the group name for this window. This allows the framework to perform
	 * operations on a group of windows (e.g. when closing a database
	 * connection, close all open windows for that connection but not windows
	 * for other connections )
	 * 
	 * @param groupName
	 *            the name of the group that this window belongs to
	 */
	public void setGroup(String groupName) {
		m_group = groupName;
	}

	public void setLocation(int x, int y) {
		m_delegate.setWindowLocation(x, y);
	}

	private void setMaximum(boolean bval) {
		// m_delegate.setMaximum( bval );
	}

	/*
	 * public void setMenuTemplate( MenuTemplate template ) { m_menuTemplate =
	 * template; }
	 */

	public void setPreferredSize(Dimension d) {
		// m_delegate.setPreferredSize( d );
	}

	/**
	 * Sets the flag determining whether this frame's derived will have one or
	 * multiple instances. Caution: This is only called from TSWorkspaceFrame
	 * 
	 * @param bSingleton
	 *            true if this frame's derived class will have only one instance
	 *            on the desktop
	 */
	void setSingleton(boolean bSingleton) {
		m_singleton = bSingleton;
	}

	/**
	 * @param title
	 *            an abbreviated version of the title bar text. This is mainly
	 *            used to set the text in the TWorkspace tabs and menus.
	 */
	public void setShortTitle(String title) {
		m_shortTitle = title;
	}

	public void setSize(Dimension d) {
		if (d != null)
			setSize(d.width, d.height);
	}

	public void setSize(int width, int height) {
		m_delegate.setWindowSize(width, height);
	}

	/**
	 * Sets the title to the frame
	 */
	public void setTitle(String title) {
		m_delegate.setTitle(title);
		TSWorkspaceFrame.getInstance().updateTitle(this);
	}

	public void setVisible(boolean bvis) {
		if (m_delegate instanceof JFrameEx)
			m_delegate.setVisible(bvis);
	}

	/**
	 * Sets the window id of this frame. This is useful for programmatically
	 * locating an internal window when several of the same class are visible.
	 */
	void setWindowId(Object windowId) {
		m_windowId = windowId;
	}

	public void startup() {
	}

	public void shutdown() {
	}

	/**
	 * This method gets called every second by a swing timer. It is mainly used
	 * to update the UI based on model state changes.
	 */
	protected void timerEvent() {
		JETAController controller = getController();
		if (controller != null) {
			UIDirector uidirector = getUIDirector();
			if (uidirector != null)
				uidirector.updateComponents(null);
		}
	}

	/**
	 * Returns the UIDirector for this container. UIDirectors are part of this
	 * framework and are responsible for enabling/disabling components based on
	 * the program state. For example, menu items and toolbar buttons must be
	 * enabled or disabled depending on the current state of the frame window.
	 * UIDirectors handle this logic.
	 * 
	 * @return the UIDirector
	 */
	public UIDirector getUIDirector() {
		return m_delegate.getUIDirector();
	}

	public void setUIDirector(UIDirector uidirector) {
		m_delegate.setUIDirector(uidirector);
	}

	/**
	 * Called by the TS framework before closing the window. Specialized frames
	 * should override if they wish to perform special processing. This method
	 * should not call setVisible(false) or dispose. That is handled by the
	 * frame work.
	 */
	public boolean tryCloseFrame() {
		return true;
	}

	public void updateUI() {
		m_delegate.updateUI();
	}
}
