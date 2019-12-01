/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyVetoException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

import com.jeta.foundation.i18n.I18N;

import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.gui.framework.*;
import com.jeta.open.gui.utils.JETAToolbox;

/**
 * This is the main frame window for the application. It contains internal frame
 * windows. For each window, it shows a tab at the bottom of the frame. This
 * allows the user to click the tab to bring that window to the front of the
 * order. It also manages the Window menu. As new windows are added/removed, the
 * windows menu is updated.
 * 
 * @author Jeff Tassin
 */
public abstract class TSWorkspaceFrame extends JETAFrame implements JETAFrameListener {

	/** list of all frames in the workspace */
	private ArrayList m_frames = new ArrayList();

	/** this list of frames that have a single instance. */
	private HashSet m_singletonFrames = new HashSet();

	/**
	 * Flag gets set to true when the system is exiting.
	 */
	private boolean m_is_exiting = false;

	/** component ids */
	public static final String COMPONENT_ID = "jeta.TSWorkspaceFrame";
	public static final String ID_USE_INTERNAL_FRAMES = "jeta.internalframes.workspace";

	/** command ids */
	public static final String ID_EXIT = "Exit";

	private JETAFrameManager m_framemanager;

	/**
	 * Object that refers to the currently active frame window.
	 */
	private Object m_active_frame;

	/**
	 * ctor
	 */
	public TSWorkspaceFrame(String frameTitle) {
		super(frameTitle);

		ComponentMgr.registerComponent(COMPONENT_ID, this);

		/**
		 * Start a timer that updates the controls of the active window every
		 * second
		 */
		ActionListener uiupdater = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				timerEvent();
			}
		};

		int delay = 750; // milliseconds
		new javax.swing.Timer(delay, uiupdater).start();

		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				m_active_frame = TSWorkspaceFrame.this;
			}

			public void windowDeactivated(WindowEvent e) {
				m_active_frame = null;
			}

		});

	}

	/**
	 * Updates the workspace UI when a frame is selected
	 */
	protected void activateInternalFrame(TSInternalFrame frame) {
		if (frame.getDelegate() instanceof javax.swing.JFrame)
			m_active_frame = frame;

		getFrameManager().activateFrame(frame);
		frame.activated();

		JETAController controller = frame.getController();
		if (controller != null)
			controller.updateComponents(null);
	}

	/**
	 * Adds the window to this workspace. The window is automatically made
	 * visible
	 * 
	 * @param frame
	 *            the frame to add
	 */
	public void addWindow(TSInternalFrame frame) {
		addWindow(frame, true);
	}

	/**
	 * Adds the window to this workspace. The window is made visible only if the
	 * show flag is set to true.
	 * 
	 * @param frame
	 *            the frame to add
	 */
	public void addWindow(TSInternalFrame frame, boolean bshow) {
		try {
			getFrameManager().addWindow(frame);
			frame.removeFrameListener(this);
			frame.addFrameListener(this);
			if (bshow) {
				getFrameManager().selectFrame(frame);
				activateInternalFrame(frame);
				frame.repaint();
			}

			if (frame.isSingleton())
				m_singletonFrames.add(frame);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Centers the given frame in the desktop
	 */
	public void centerWindow(TSInternalFrame iframe) {
		if (iframe == null)
			return;

		int width = iframe.getWidth();
		int height = iframe.getHeight();

		Dimension dd = getFrameManager().getWorkspaceSize();

		if (iframe.getWidth() > dd.width)
			width = dd.width - 50;

		if (iframe.getHeight() > dd.height)
			height = dd.height - 50;

		if (iframe.getWidth() < 50)
			width = 50;

		if (iframe.getHeight() < 50)
			height = 50;

		int x = (dd.width - width) / 2;
		int y = (dd.height - height) / 2;

		iframe.setBounds(x, y, width, height);
	}

	/**
	 * Closes the window in this workspace. If the frame is a singleton, it is
	 * merely hidden and the desktop updated.
	 * 
	 * @return false if the frame wants to cancel the close (see tryCloseFrame)
	 */
	public boolean closeWindow(TSInternalFrame iframe) {
		if (iframe.isSingleton()) {
			iframe.setVisible(false);
			getFrameManager().removeWindow(iframe);
		} else {
			show(iframe);
			if (iframe.tryCloseFrame()) {
				getFrameManager().removeWindow(iframe);
				iframe.dispose();
			} else {
				return false;
			}
		}
		repaint();
		return true;
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

	public JMenuItem i18n_createMenuItem(String itemText, KeyStroke keyAccelerator) {
		return i18n_createMenuItem(itemText, itemText, keyAccelerator);
	}

	public JMenuItem i18n_createMenuItem(String itemText, String actionCmd, KeyStroke keyAccelerator) {
		return createMenuItem(I18N.getLocalizedMessage(itemText), actionCmd, keyAccelerator);
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

	public JButton i18n_createToolBarButton(String cmd, String imageFile, String toolTip) {
		return createToolBarButton(cmd, imageFile, I18N.getLocalizedMessage(toolTip));
	}

	/**
	 * Creates the top level menus for the workspace that are shared across all
	 * windows
	 */
	protected void createMenus() {
		MenuTemplate template = this.getMenuTemplate();
		MenuDefinition menu = new MenuDefinition(I18N.getLocalizedMessage("File"));
		menu.add(i18n_createMenuItem(ID_EXIT, null));
		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Window"));
		template.add(menu);
	}

	public TSInternalFrame createFrame(Class frameClass) {
		try {
			return (TSInternalFrame) frameClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates an internal frame for the desktop and set's the frame id. The
	 * frame is not added though. You must cal addWindow separately
	 * 
	 * @param frameClass
	 *            the class of a TSInternalWindow derivedclass to create.
	 * @param bSingleton
	 *            set to true if the newly created window is a singleton or not
	 * @param groupId
	 *            id of the window's group
	 */
	public TSInternalFrame createInternalFrame(Class frameClass, boolean bSingleton, Object groupId) {
		TSInternalFrame frame = createFrame(frameClass);
		frame.setSingleton(bSingleton);
		if (groupId != null)
			frame.setWindowId(groupId);
		return frame;
	}

	/**
	 * Updates the workspace UI when a frame is de-selected
	 */
	protected void deactivateInternalFrame(TSInternalFrame frame) {
		m_active_frame = null;
		JETAController controller = frame.getController();
		if (controller != null)
			controller.updateComponents(null);

	}

	/**
	 * Disposes all child windows owned by this frame. This is mainly called
	 * during shutdown so that each internal frame can clean up appropriately.
	 */
	public void disposeAll() {
		Iterator iter = m_singletonFrames.iterator();
		while (iter.hasNext()) {
			TSInternalFrame frame = (TSInternalFrame) iter.next();
			closeWindow(frame);
			frame.dispose();
			iter.remove();
		}

		TSInternalFrame[] frames = getFrameManager().getAllFrames();
		for (int index = 0; index < frames.length; index++) {
			TSInternalFrame frame = frames[index];
			closeWindow(frame);
		}
	}

	/**
	 * Diposes the frame and removes it from the desktop
	 * 
	 * @return false if the user canceled the dispose in tryCloseFrame
	 */
	public boolean disposeFrame(TSInternalFrame iframe) {
		if (iframe.isSingleton()) {
			show(iframe);
			if (!iframe.tryCloseFrame())
				return false;
		}

		if (!closeWindow(iframe))
			return false;

		if (iframe.isSingleton()) {
			iframe.dispose();
		}
		m_singletonFrames.remove(iframe);
		return true;
	}

	/**
	 * @return a collection of all frames (TSInternalFrames) in the workspace.
	 *         This includes hidden singleton frames.
	 */
	public abstract Collection getAllFrames(Object windowId);

	public TSInternalFrame getFrameAt(int index) {
		return getFrameManager().getFrameAt(index);
	}

	protected JETAFrameManager createFrameManager() {
		return new JFrameManager(this);
	}

	public JETAFrameManager getFrameManager() {
		if (m_framemanager == null) {
			m_framemanager = createFrameManager();
		}
		return m_framemanager;
	}

	/**
	 * @return the internal size of the workspace. This is really the size of
	 *         the desktop
	 */
	public Dimension getWorkspaceSize() {
		return getFrameManager().getWorkspaceSize();
	}

	/**
	 * @return the one an only instance of this workspace frame
	 */
	public static TSWorkspaceFrame getInstance() {
		return (TSWorkspaceFrame) ComponentMgr.lookup(COMPONENT_ID);
	}

	/**
	 * @return the internal frame instance that contains the given delegate
	 */
	public TSInternalFrame getInternalFrame(Component delegate) {
		TSInternalFrame iframe = null;
		if (delegate instanceof JFrameEx) {
			JFrameEx frame = (JFrameEx) delegate;
			iframe = frame.getTSInternalFrame();
		} else if (delegate instanceof JPanelFrame) {
			JPanelFrame frame = (JPanelFrame) delegate;
			iframe = frame.getTSInternalFrame();
		}
		assert (iframe != null);
		return iframe;
	}

	/**
	 * @return the singleton frame that has the given class and window id.
	 */
	public TSInternalFrame getSingletonFrame(Class c, Object windowId) {
		Iterator iter = m_singletonFrames.iterator();
		while (iter.hasNext()) {
			TSInternalFrame frame = (TSInternalFrame) iter.next();
			if (frame.getClass() == c && windowId.equals(frame.getWindowId())) {
				return frame;
			}
		}
		return null;
	}

	/**
	 * Return all singleton frames in the workspace (whether they are showing or
	 * not )
	 */
	public Collection getSingletonFrames() {
		return m_singletonFrames;
	}

	/**
	 * @return the all singleton frame that has the given class
	 */
	public Collection getSingletonFrames(Class c) {
		LinkedList results = new LinkedList();
		Iterator iter = m_singletonFrames.iterator();
		while (iter.hasNext()) {
			TSInternalFrame frame = (TSInternalFrame) iter.next();
			if (frame.getClass() == c) {
				results.add(frame);
			}
		}
		return results;
	}

	/**
	 * Return true if the file is opened in the workspace.
	 */
	public boolean isDocumentOpened(java.io.File f) {
		return false;
	}

	/**
	 * Returns the flag that indicates if we are exiting.
	 */
	public boolean isExiting() {
		return m_is_exiting;
	}

	/**
	 * @return true if we are running in external frame mode
	 */
	public boolean isExternalFrames() {
		return true;
	}

	/**
	 * @return true if we are running in internal frame mode
	 */
	public boolean isInternalFrames() {
		return false;
	}

	public void jetaFrameActivated(JETAFrameEvent e) {
		activateInternalFrame((TSInternalFrame) e.getSource());
	}

	public void jetaFrameClosing(JETAFrameEvent e) {
		Object src = e.getSource();
		if (src instanceof TSInternalFrame) {
			TSInternalFrame iframe = (TSInternalFrame) src;
			if (!(iframe.getDelegate() instanceof JPanelFrame)) {
				closeWindow(iframe);
			}
		}
	}

	public void jetaFrameClosed(JETAFrameEvent e) {
	}

	public void jetaFrameDeactivated(JETAFrameEvent e) {
		deactivateInternalFrame((TSInternalFrame) e.getSource());
	}

	public void jetaFrameDeiconified(JETAFrameEvent e) {
	}

	public void jetaFrameIconified(JETAFrameEvent e) {
	}

	public void jetaFrameOpened(JETAFrameEvent e) {
	}

	/**
	 * Sets the frame manager for this workspace
	 */
	protected void setFrameManager(JETAFrameManager fmgr) {
		m_framemanager = fmgr;
	}

	/**
	 * Sets the flag that indicates if we are exiting.
	 */
	protected void setExiting(boolean isExiting) {
		m_is_exiting = isExiting;
	}

	/**
	 * Sets a component's size/position relative to this workspace's size.
	 * 
	 * @param frame
	 *            the frame whose size we want to set
	 * @param pctWidth
	 *            the percent of this workspace's content panel's width
	 * @param pctHeight
	 *            the percent of this workspace's content panel's height
	 * @param minSize
	 *            the minimum size to set the component
	 */
	public void setRelative(TSInternalFrame frame, float pctWidth, float pctHeight, Dimension minSize) {
		if (frame.getDelegate() instanceof JFrame) {
			Dimension d = getWorkspaceSize();
			d.width = (int) ((float) d.width * pctWidth);
			d.height = (int) ((float) d.height * pctHeight);

			if (d.width < minSize.width || d.height < minSize.height)
				d = minSize;

			frame.setSize(d);

			int x = 0;
			int y = 0;
			Dimension dws = getWorkspaceSize();
			if (frame.getWidth() < dws.width)
				x = (dws.width - frame.getWidth()) / 2;

			if (frame.getHeight() < dws.height)
				y = (dws.height - frame.getHeight()) / 2;

			frame.setLocation(x, y);
		}
	}

	public void show(TSInternalFrame frame) {
		boolean bfound = false;
		TSInternalFrame[] frames = getFrameManager().getAllFrames();
		for (int index = 0; index < frames.length; index++) {
			if (frame == frames[index]) {
				int height = frame.getHeight();
				int width = frame.getWidth();
				frame.setVisible(true);

				getFrameManager().selectFrame(frame);

				bfound = true;
				break;
			}
		}

		// if not found, try the singletons
		if (!bfound) {
			Iterator iter = m_singletonFrames.iterator();
			while (iter.hasNext()) {
				TSInternalFrame sframe = (TSInternalFrame) iter.next();
				if (frame == sframe) {
					addWindow(frame, true);
					break;
				}
			}
		}
	}

	/**
	 * This method is used to show a singleton frame. Specialized workspaces
	 * should override this method to provide custom behavior. This is mainly
	 * used to provide deferred creation/loading for frames until they are
	 * explicitly requested by the user.
	 * 
	 * @param frameClass
	 *            the window class to show
	 * @param params
	 *            an object that defines the param(s) to initialize the window
	 *            with if it must be created
	 */
	public TSInternalFrame show(Class frameClass, Object params) {
		return null;
	}

	/**
	 * This method gets called every second by a swing timer. It is mainly used
	 * to update the UI based on model state changes.
	 */
	protected void timerEvent() {
		if (m_active_frame == this) {
			JETAController controller = getController();
			if (controller != null) {
				UIDirector uidirector = getUIDirector();
				if (uidirector != null)
					uidirector.updateComponents(null);
			}
		} else if (m_active_frame instanceof TSInternalFrame) {
			((TSInternalFrame) m_active_frame).timerEvent();
		}
	}

	public void updateTitle(TSInternalFrame iframe) {
		getFrameManager().updateTitle(iframe);
	}

}
