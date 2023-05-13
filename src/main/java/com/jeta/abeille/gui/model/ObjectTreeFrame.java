package com.jeta.abeille.gui.model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DbModelEvent;
import com.jeta.abeille.database.model.DbModelListener;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.licensemgr.AbeilleLicenseUtils;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.JFrameEx;
import com.jeta.foundation.gui.components.TSButtonBarEx;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.WindowDelegate;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;

import com.jeta.foundation.utils.JETATimer;

import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

/**
 * This is the frame window that contains the view of objects in the database.
 * Currently, we only support showing tables and views.
 * 
 * @author Jeff Tassin
 */
public class ObjectTreeFrame extends TSInternalFrame {

	/** the frame icon for this frame */
	private static ImageIcon m_frameicon;

	/**
	 * The one and only view for this frame.
	 */
	private ObjectTreeView m_view;

	private FormPanel m_content_panel;

	static {
		m_frameicon = TSGuiToolbox.loadImage("incors/16x16/text_tree.png");
	}

	/**
	 * ctor
	 */
	public ObjectTreeFrame() {
		super("");
		setFrameIcon(m_frameicon);
	}

	/**
	 * Adds the given connection to the view
	 */
	public void addConnection(TSConnection conn) {
		m_view.addConnection(conn);
	}

	/**
	 * Saves the state for the connection and removes it from the model
	 */
	public void closeConnection(TSConnection tsconn) {
		m_view.closeConnection(tsconn);
	}

	/**
	 * Sets the connection needed by this frame.
	 * 
	 * @param params
	 *            a 1 length array. The zero element must contain the
	 *            TSConnection object
	 */
	public void initializeModel(Object[] params) {
		String title = I18N.getLocalizedMessage("Objects");
		setTitle(title);
		setShortTitle(title);

		m_view = new DbObjectTreeView();
		m_view.setController(new ObjectTreeViewController(m_view));
		m_view.setUIDirector(new ObjectTreeUIDirector(m_view));

		/**
		 * we need to do this to update the UI. This is done instead of the
		 * standard UIDirector timer events because updating the ObjectTree is
		 * too expensive to do every second or less
		 */
		ObjectTree tree = m_view.getTree();

		tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
				// updateComponents();
			}
		});

		m_content_panel = new FormPanel("com/jeta/abeille/gui/model/dbobjects.jfrm");
		FormAccessor faccessor = m_content_panel.getFormAccessor("main.form");
		faccessor.replaceBean("db.tree", m_view);
		getContentPane().add(m_content_panel, BorderLayout.CENTER);
		setController(new ObjectTreeFrameController(this));
		updateDockButton();
	}

	public TSPanel getCurrentView() {
		return m_view;
	}

	/**
	 * This is only needed to support docking frames
	 */
	public void setDelegate(WindowDelegate delegate) {
		Container cc = getContentPane();
		if (cc != null)
			cc.remove(m_content_panel);

		super.setDelegate(delegate);

		cc = getContentPane();
		if (cc != null && m_content_panel != null) {
			cc.add(m_content_panel, BorderLayout.CENTER);
			updateDockButton();
		}
	}

	private void updateDockButton() {
		if (m_content_panel != null) {
			ImageComponent btn = (ImageComponent) m_content_panel
					.getComponentByName(ObjectTreeFrameNames.ID_DOCK_FRAME);
			if (getDelegate() instanceof JFrameEx)
				btn.setIcon(TSGuiToolbox.loadImage("incors/16x16/pin_blue.png"));
			else
				btn.setIcon(TSGuiToolbox.loadImage("incors/16x16/windows.png"));
		}
	}

	/**
	 * This method gets called every second by a swing timer. It is mainly used
	 * to update the UI based on model state changes. We override here because
	 * the UIDirector for the object tree frame has some expensive operations
	 * and can cause too much CPU usage if called every second. Instead, we
	 * update the UI on table and tabbed window events.
	 */
	protected void timerEvent() {
		// no op
	}

	/**
	 * Updates the UIDirector
	 */
	private void updateComponents() {
		com.jeta.open.gui.framework.UIDirector uidirector = getUIDirector();
		if (uidirector != null)
			uidirector.updateComponents(null);
	}
}
