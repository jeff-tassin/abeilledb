package com.jeta.abeille.gui.procedures;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.ImageIcon;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.StoredProcedure;

import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class displays the properties for a given procedure/function
 * 
 * @author Jeff Tassin
 */
public class ProcedureFrame extends TSInternalFrame {
	/** the frame icon for this frame */
	private static ImageIcon m_frameicon;

	/** the database connection */
	private TSConnection m_connection;

	/** the main view for this frame */
	private ProcedureBrowser m_browser;

	static {
		m_frameicon = TSGuiToolbox.loadImage("incors/16x16/gear.png");
	}

	/**
	 * ctor
	 */
	public ProcedureFrame() {
		super(I18N.getLocalizedMessage("Procedure"));
		setFrameIcon(m_frameicon);
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
	 *            Currently, we expect an array (size == 2) where the first
	 *            parameter must be the database connection (TSConnection) and
	 *            the second parameter must be a StoredProcedure object ( can be
	 *            null)
	 */
	public void initializeModel(Object[] params) {
		m_connection = (TSConnection) params[0];
		setTitle("Procedure");
		setShortTitle("Procedure");

		m_browser = new ProcedureBrowser(m_connection);
		getContentPane().add(m_browser, BorderLayout.CENTER);

		setPreferredSize(m_browser.getPreferredSize());
		setSize(m_browser.getPreferredSize());
	}

	/**
	 * This method displays the given procedure in the frame. It is used when a
	 * caller wants to display a procedure from some other part of the program.
	 * Only the given procedure is shown. Procedures with the same name are not
	 * displayed.
	 */
	public void showProcedure(StoredProcedure proc) {
		m_browser.showProcedure(proc);
	}

}
