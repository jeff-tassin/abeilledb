package com.jeta.abeille.gui.main;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class displays the global system info for the connection/application
 * 
 * @author Jeff Tassin
 */
public class SystemInfoFrame extends TSInternalFrame {
	/** the frame icon for this frame */
	public static ImageIcon FRAME_ICON;

	/** the database connection */
	private TSConnection m_connection;

	static {
		FRAME_ICON = TSGuiToolbox.loadImage("incors/16x16/information.png");
	}

	/**
	 * ctor
	 */
	public SystemInfoFrame() {
		super(I18N.getLocalizedMessage("System Information"));
		setFrameIcon(FRAME_ICON);
		setShortTitle(I18N.getLocalizedMessage("System Information"));
	}

	/**
	 * creates the main view for this frame
	 */
	public JPanel createView() {
		return SystemInfoView.createView(m_connection);
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
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
	}

}
