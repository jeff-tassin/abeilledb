/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.Component;
import java.awt.Container;
import java.awt.BorderLayout;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.gui.components.TSComponentFinder;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is a editor dervied from netbeans.
 * 
 * @author Jeff Tassin
 */
public class EditorFrame extends TSInternalFrame implements BufferListener {
	private static final int MENUBAR_TOOLS_INDEX = 2; // index where we put
														// SQLFrame specific
														// toolbar buttons
	private JMenu m_buffersmenu;
	private EditorController m_framecontroller; // the main controller for the
												// frame (each buffer has its
												// own controller)
	private BufferMgr m_buffermgr;
	private JTabbedPane m_tabpane = new JTabbedPane(JTabbedPane.BOTTOM);

	/**
	 * flag that tells our tab pane listener to update the buffer manager. In
	 * most cases the buffer manager already knows the buffer has changed
	 */
	private boolean m_listentabchange = true;

	/**
	 * Constructor
	 */
	public EditorFrame() {
		super("");
	}

	/**
	 * Adds a buffer to the tab pane for this frame
	 */
	public void addBuffer(Buffer buffer) {
		m_listentabchange = false;
		try {

			Container panel = createContainer(buffer);
			m_tabpane.addTab(buffer.getTitle(), panel);
			selectBuffer(buffer);
		} finally {
			m_listentabchange = true;
		}
	}

	/**
	 * Event that occurs when a buffer has changed
	 */
	public void bufferChanged(BufferEvent evt) {
		Buffer buffer = evt.getBuffer();
		int id = evt.getID();
		if (id == BufferEvent.BUFFER_CREATED) {
			addBuffer(buffer);
		} else if (id == BufferEvent.BUFFER_DELETED) {
			removeBuffer(buffer);
		} else if (id == BufferEvent.BUFFER_SELECTED) {
			selectBuffer(buffer);
		} else if (id == BufferEvent.BUFFER_CHANGED) {
			int tabindex = getTabIndex(buffer);
			if (tabindex >= 0) {
				String title = buffer.getTitle();
				if (buffer.isModified())
					title += "*";
				m_tabpane.setTitleAt(tabindex, title);
			}
		} else if (id == BufferEvent.BUFFER_NAME_CHANGED) {
			int tabindex = getTabIndex(buffer);
			if (tabindex >= 0) {
				String title = buffer.getTitle();
				if (buffer.isModified())
					title += "*";
				m_tabpane.setTitleAt(tabindex, title);
			}

		} else if (id == BufferEvent.SELECT_NEXT_BUFFER) {
			selectNextBuffer();
		} else if (id == BufferEvent.SELECT_PREV_BUFFER) {
			selectPrevBuffer();
		}
	}

	/**
	 * This method is here so that subclasses can override and provide there own
	 * containers. The most common is a JLayeredPane.
	 * 
	 * @param bufferPane
	 *            the buffer
	 * @return the container with the bufferPane added to it. The default
	 *         implementation simply returns the bufferPanel
	 */
	public Container createContainer(Buffer buffer) {
		return buffer.getContentPanel();
	}

	/**
	 * Override if you wish to provide a specialized implementation of
	 * EditorController
	 */
	protected EditorController createController(BufferMgr buffMgr) {
		return new EditorController(this, buffMgr);
	}

	/**
	 * Create the specialized menus for this frame. In addition to adding the
	 * standard text editor menus, we also add the SQL specific menus.
	 */
	protected void createMenu() {
		TSEditorUtils.buildMenu(this);

		MenuTemplate template = getMenuTemplate();

		MenuDefinition menu = template.getMenuAt(MENUBAR_TOOLS_INDEX); // get
																		// last
																		// menu
		menu.addSeparator();

		JMenuItem item = i18n_createMenuItem("Preferences", TSTextNames.ID_PREFERENCES, null);
		menu.add(item);

		// m_buffersmenu = new JMenu( I18N.getLocalizedMessage("Buffers") );
		// item = i18n_createMenuItem( "Select", FrameKit.switchBuffersAction,
		// null );
		// m_buffersmenu.add( item );
		// template.add( m_buffersmenu );
	}

	/**
	 * Create the specialized toolbar for this frame. In addition to adding the
	 * standard text editor buttons, we also add the SQL specific buttons.
	 */
	protected void createToolBar() {
		TSEditorUtils.buildToolBar(this);
	}

	/**
	 * @return the buffermgr associated with this frame
	 */
	public BufferMgr getBufferMgr() {
		return m_buffermgr;
	}

	/**
	 * @return the current buffer visible in the frame
	 */
	public Buffer getCurrentBuffer() {
		Buffer buff = m_buffermgr.getCurrentBuffer();
		return buff;
	}

	/**
	 * @return the buffer at the given tab index
	 */
	public Buffer getBuffer(int tabIndex) {
		if (tabIndex >= 0 && tabIndex < m_tabpane.getTabCount()) {
			Component tabcomp = m_tabpane.getComponentAt(tabIndex);

			Collection buffers = m_buffermgr.getBuffers();
			Iterator iter = buffers.iterator();
			while (iter.hasNext()) {
				Buffer buffer = (Buffer) iter.next();
				Component comp = buffer.getContentPanel();
				while (!(comp instanceof JTabbedPane) && (comp != null)) {
					if (comp == tabcomp) {
						return buffer;
					}
					comp = comp.getParent();
				}
			}
		}
		return null;
	}

	/**
	 * Returns the tab index of the tab that contains the given buffer.
	 * 
	 * @param buffer
	 *            the buffer whose tab index we wish to return
	 * @return the tab index for the given buffer
	 */
	public int getTabIndex(Buffer buffer) {
		if (buffer == null)
			return -1;

		for (int index = 0; index < m_tabpane.getTabCount(); index++) {
			Component tabcomp = m_tabpane.getComponentAt(index);

			// now walk up the ancestor hiearchy for the buffer and see if
			// any parent window is the same as the tabcomp
			Component comp = buffer.getContentPanel();
			while (!(comp instanceof JTabbedPane) && (comp != null)) {
				if (comp == tabcomp)
					return index;

				comp = comp.getParent();
			}
		}
		return -1;
	}

	/**
	 * @return the tab pane instance that contains the buffers
	 */
	protected JTabbedPane getTabbedPane() {
		return m_tabpane;
	}

	/**
	 * Creates the menu, toolbar, and content window for this frame
	 */
	public void initialize() {
		Container container = getContentPane();
		createMenu();
		createToolBar();

		m_buffermgr = new BufferMgr();
		m_buffermgr.addBufferListener(this);

		// JComponent minibuff = new
		// com.jeta.foundation.gui.editor.minibuffer.MiniBuffer();
		// minibuff.setToolTipText( I18N.getLocalizedMessage("Mini_buff_tooltip"
		// ) );
		// minibuff.setName( TSTextNames.ID_MINIBUFFER );

		JPanel panel = new JPanel(new BorderLayout(3, 3));
		// panel.add( minibuff, BorderLayout.NORTH );
		panel.add(m_tabpane, BorderLayout.CENTER);

		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		getContentPane().add(panel);

		m_framecontroller = createController(m_buffermgr);
		addController(m_framecontroller);
		m_framecontroller.updateUI();

		/**
		 * Get tab change events
		 */
		m_tabpane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (m_listentabchange) {
					Container c = (Container) m_tabpane.getSelectedComponent();
					if (c != null) {
						JEditorPane comp = (JEditorPane) TSComponentFinder.getComponentByType(c, JEditorPane.class);
						Buffer buff = m_buffermgr.getBuffer(comp);
						if (buff != null) {
							m_buffermgr.selectBuffer(buff);
							m_framecontroller.updateUI();
						}
					}
				}
			}
		});

	}

	/**
	 * Removes the buffer from the tab pane
	 */
	public void removeBuffer(Buffer buffer) {
		m_listentabchange = false;
		try {
			int tabindex = getTabIndex(buffer);
			m_tabpane.removeTabAt(tabindex);
		} finally {
			m_listentabchange = true;
		}
	}

	/**
	 * Selects the given buffer. Makes its tab the current one in the tab pane.
	 * 
	 * @param buffer
	 *            the buffer to activate
	 */
	public void selectBuffer(Buffer buffer) {
		m_listentabchange = false;
		try {
			int tabindex = getTabIndex(buffer);
			if (tabindex != m_tabpane.getSelectedIndex()) {
				m_tabpane.setSelectedIndex(tabindex);
				m_framecontroller.updateUI();
			}

			if (buffer != null) {
				buffer.requestFocus();
			}

		} finally {
			m_listentabchange = true;
		}
	}

	/**
	 * Selects the next buffer. If the current buffer is the last buffer, then
	 * the first buffer is selected
	 */
	public void selectNextBuffer() {
		if (m_tabpane.getTabCount() <= 1)
			return;

		int pos = m_tabpane.getSelectedIndex();
		if (pos >= 0) {
			pos++;

			if (pos >= m_tabpane.getTabCount())
				pos = 0;

			m_tabpane.setSelectedIndex(pos);
		}
	}

	/**
	 * Selects the next buffer. If the current buffer is the first buffer, then
	 * the last buffer is selected
	 */
	public void selectPrevBuffer() {
		if (m_tabpane.getTabCount() <= 1)
			return;

		int pos = m_tabpane.getSelectedIndex();
		if (pos >= 0) {
			pos--;

			if (pos < 0)
				pos = m_tabpane.getTabCount() - 1;

			m_tabpane.setSelectedIndex(pos);
		}
	}

}
