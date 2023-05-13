package com.jeta.abeille.gui.utils;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;

import com.jeta.foundation.i18n.I18N;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is a utility dialog that can be used to display a SQLException.
 * 
 * @author Jeff Tassin
 */
public class SQLErrorDialog extends TSDialog {
	/** message to be prepended to error */
	private String m_msg;

	/** the main content panel */
	private SQLErrorPanel m_panel;

	/**
	 * ctor
	 */
	public SQLErrorDialog(Dialog owner, boolean bmodal) {
		super(owner, bmodal);
	}

	/**
	 * ctor
	 */
	public SQLErrorDialog(Frame owner, boolean bmodal) {
		super(owner, bmodal);
	}

	public static String format(Exception e, String sql) {
		return SQLErrorPanel.format(e, sql);
	}

	/**
	 * Initializes this dialog with the exception object.
	 */
	public void initialize(Exception e) {
		initialize((Exception) e, null);
	}

	/**
	 * @param sql
	 *            the sql string that caused the error ( can be null )
	 * @param rollback
	 *            a flag that indicates the the error caused the transaction to
	 *            be automatically rolled back
	 */
	public void initialize(Exception e, String sql, boolean rollback) {
		_initialize((Exception) e, sql, rollback);
	}

	/**
	 * Initializes this dialog
	 */
	public void initialize(Exception e, String sql) {
		_initialize(e, sql, false);
	}

	/**
	 * @param sql
	 *            the sql string that caused the error ( can be null )
	 * @param rollback
	 *            a flag that indicates the the error caused the transaction to
	 *            be automatically rolled back
	 */
	private void _initialize(Exception e, String sql, boolean rollback) {
		TSUtils.printException(e);

		setTitle(I18N.getLocalizedMessage("Error"));
		m_panel = new SQLErrorPanel();
		m_panel.initialize(m_msg, e, sql, rollback);
		setPrimaryPanel(m_panel);

		showCloseLink();
	}

	/**
	 * Adds a message to be prepended to the sql error message
	 */
	public void prependMessage(String str) {
		m_msg = str;
	}

	/**
	 * Helper method that shows a sql exception
	 * 
	 */
	public static void showErrorDialog(Component parent, Exception e, String sql) {
		SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, parent, true);
		dlg.initialize(e, sql);
		dlg.setSize(dlg.getPreferredSize());
		dlg.showCenter();
	}

	/**
	 * Helper method that shows a sql exception
	 * 
	 */
	public static void showErrorDialog(TSInternalFrame parent, Exception e, String sql) {
		SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, parent, true);
		dlg.initialize(e, sql);
		dlg.setSize(dlg.getPreferredSize());
		dlg.showCenter();
	}

	/**
	 * Helper method that shows a sql exception in a dialog AND logs the
	 * exception to the application logger.
	 * 
	 * @param e
	 *            the exception to display
	 * @param sql
	 *            the sql that caused the exception
	 * @param rollback
	 *            a flag that indicates the the error caused the transaction to
	 *            be automatically rolled back
	 */
	public static void logErrorDialog(Exception e, String sql, boolean rollback) {
		SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class,
				TSWorkspaceFrame.getInstance(), true);
		dlg.initialize(e, sql, rollback);
		dlg.setSize(dlg.getPreferredSize());
		dlg.showCenter();

		Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
		if (logger != null && e != null) {
			String msg = e.getLocalizedMessage();
			if (msg == null || msg.length() == 0) {
				logger.log(Level.WARNING, e.getMessage());
			} else {
				logger.log(Level.WARNING, msg);
			}
		}
	}

	/**
	 * Sets the caption message at the top of the error panel.
	 */
	public void setErrorCaption(String msg) {
		m_panel.setErrorCaption(msg);
	}

}
