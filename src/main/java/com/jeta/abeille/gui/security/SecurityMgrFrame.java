package com.jeta.abeille.gui.security;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.security.postgres.PostgresViewBuilder;
import com.jeta.abeille.gui.security.mysql.MySQLViewBuilder;
import com.jeta.abeille.gui.security.hsqldb.HSQLViewBuilder;

import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class displays the users/groups/grants for the current database
 * 
 * @author Jeff Tassin
 */
public class SecurityMgrFrame extends TSInternalFrame {
	/** the frame icon for this frame */
	private static ImageIcon m_frameicon;

	/** the database connection */
	private TSConnection m_connection;

	/**
	 * The main tab pane for the view;
	 */
	private JTabbedPane m_tabpane = new JTabbedPane();

	static {
		m_frameicon = TSGuiToolbox.loadImage("incors/16x16/businessman.png");
	}

	/**
	 * ctor
	 */
	public SecurityMgrFrame() {
		super(I18N.getLocalizedMessage("Users"));
		setShortTitle(I18N.getLocalizedMessage("Users"));
		setFrameIcon(m_frameicon);
	}

	/**
	 * creates the main view for this frame
	 */
	public JPanel createView() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 10));

		panel.add(m_tabpane, BorderLayout.CENTER);

		SecurityBuilder builder = SecurityBuilder.createInstance(m_connection);
		builder.buildFrame(this);
		return panel;
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the panel in the current tab
	 */
	public TSPanel getCurrentView() {
		return (TSPanel) m_tabpane.getSelectedComponent();
	}

	/**
	 * @return the tab pane for this frame
	 */
	public JTabbedPane getTabbedPane() {
		return m_tabpane;
	}

	/**
	 * Creates and initializes the components on this frame
	 * 
	 * @param params
	 *            the list of parameters need to initialize this frame
	 *            Currently, we expect an array (size == 1) where the first
	 *            parameter must be the database connection (TSConnection)
	 */
	public void initializeModel(Object[] params) {
		m_connection = (TSConnection) params[0];

		getContentPane().add(createView(), BorderLayout.CENTER);

		Dimension d = TSGuiToolbox.getWindowDimension(10, 20);
		TSGuiToolbox.setReasonableWindowSize(this.getDelegate(), d);
		setController(new SecurityMgrFrameController(this));
	}

}
