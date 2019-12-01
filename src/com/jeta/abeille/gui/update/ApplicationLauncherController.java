package com.jeta.abeille.gui.update;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.jeta.abeille.database.model.TSResultSet;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.utils.TSUtils;

/**
 * 
 * @author Jeff Tassin
 */
public class ApplicationLauncherController extends TSController {
	LOBComponent m_component;
	String m_application; // the application to launch to handle the given
							// object

	public ApplicationLauncherController(InstanceView view, LOBComponent comp, String app) {
		super(view);
		m_component = comp;
		m_application = app;
	}

	/**
	 * Sets the value represented by this component into the prepared statement.
	 * Different controllers for the LOBComponent will handle this differently.
	 * 
	 * @param count
	 *            the parameter index to set
	 * @param pstmt
	 *            the prepared statement to act on
	 */
	public void prepareStatement(int count, PreparedStatement pstmt) throws SQLException {
		TSUtils._assert(false);
	}
}
