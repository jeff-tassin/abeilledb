package com.jeta.abeille.gui.main;

import javax.swing.LookAndFeel;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.licensemgr.AbeilleLicenseUtils;

import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.UIDirector;

import com.jeta.foundation.gui.components.JPanelFrame;
import com.jeta.foundation.interfaces.license.LicenseUtils;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is responsible for updating the menu/toolbars on the MainFrame
 * 
 * @author Jeff Tassin
 */
public class MainFrameUIDirector implements UIDirector {
	/** the frame window that we are updating components for */
	private MainFrame m_frame;

	/**
	 * ctor
	 */
	public MainFrameUIDirector(MainFrame frame) {
		m_frame = frame;
	}

	/**
	 * Enables the look and feel menu items based on whether the passed in look
	 * and feel is supported on the given platform.
	 * 
	 * @param lookandfeel
	 *            the look and feel name (as defined in MainFrameNames.) This
	 *            name corresponds to the menu name as well.
	 */
	protected void enableLookAndFeel(String lookandfeel) {
		boolean bresult = isAvailableLookAndFeel(lookandfeel);
		m_frame.enableComponent(lookandfeel, bresult);
	}

	/**
	 * A utility function that layers on top of the LookAndFeel's
	 * isSupportedLookAndFeel() method. Returns true if the LookAndFeel is
	 * supported. Returns false if the LookAndFeel is not supported and/or if
	 * there is any kind of error checking if the LookAndFeel is supported.
	 * 
	 * The L&F menu will use this method to detemine whether the various L&F
	 * options should be active or inactive.
	 * 
	 * Borrowed from SwingSet2.java example program
	 */
	protected boolean isAvailableLookAndFeel(String laf) {
		try {
			Class lnfClass = Class.forName(laf);
			LookAndFeel newLAF = (LookAndFeel) (lnfClass.newInstance());
			return newLAF.isSupportedLookAndFeel();
		} catch (Exception e) {
			// If ANYTHING weird happens, return false
			return false;
		}
	}

	/**
	 * Updates the menu/toolbar buttons for the frame
	 */
	public void updateComponents(java.util.EventObject evt) {

		TSConnection connection = m_frame.getConnection();
		if (connection == null) {
			m_frame.enableComponent(MainFrameNames.ID_MODEL_VIEW, false);
			m_frame.enableComponent(MainFrameNames.ID_SQL, false);
			m_frame.enableComponent(MainFrameNames.ID_QUERY_BUILDER, false);
			m_frame.enableComponent(MainFrameNames.ID_DISCONNECT, false);
			m_frame.enableComponent(MainFrameNames.ID_TABLE_PROPERTIES, false);
			m_frame.enableComponent(MainFrameNames.ID_DRIVER_INFO, false);
			m_frame.enableComponent(MainFrameNames.ID_SECURITY_MGR, false);
			m_frame.enableComponent(MainFrameNames.ID_SET_CURRENT_SCHEMA, false);
			m_frame.enableComponent(MainFrameNames.ID_TOGGLE_CONNECTION, false);
		} else // we have a current connection
		{
			m_frame.enableComponent(MainFrameNames.ID_MODEL_VIEW, true);
			m_frame.enableComponent(MainFrameNames.ID_SQL, true);
			m_frame.enableComponent(MainFrameNames.ID_QUERY_BUILDER, true);
			m_frame.enableComponent(MainFrameNames.ID_DISCONNECT, true);
			m_frame.enableComponent(MainFrameNames.ID_TABLE_PROPERTIES, true);
			m_frame.enableComponent(MainFrameNames.ID_DRIVER_INFO, true);

			m_frame.enableComponent(MainFrameNames.ID_TOGGLE_CONNECTION, m_frame.getConnectionCount() > 1);

			/**
			 * if we are running Abeille lite or the database does not support
			 * creating a table
			 */
			TSDatabase db = (TSDatabase) connection.getImplementation(TSDatabase.COMPONENT_ID);
			if (AbeilleLicenseUtils.isBasic() || !db.supportsFeature(MainFrameNames.ID_SECURITY_MGR)) {
				m_frame.enableComponent(MainFrameNames.ID_SECURITY_MGR, false);
			} else {
				m_frame.enableComponent(MainFrameNames.ID_SECURITY_MGR, true);
			}

			if (connection.supportsSchemas() || connection.supportsCatalogs())
				m_frame.enableComponent(MainFrameNames.ID_SET_CURRENT_SCHEMA, true);
			else
				m_frame.enableComponent(MainFrameNames.ID_SET_CURRENT_SCHEMA, false);

		}

		Object obj = m_frame.getTabbedPane().getSelectedComponent();

		if (obj instanceof JETAContainer) {
			UIDirector uidirector = ((JETAContainer) obj).getUIDirector();
			if (uidirector != null)
				uidirector.updateComponents(evt);
		}
	}

}
