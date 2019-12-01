package com.jeta.abeille.gui.utils;

import java.awt.BorderLayout;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.sql.SQLBuffer;
import com.jeta.abeille.gui.sql.SQLMediator;
import com.jeta.abeille.gui.sql.SQLMediatorEvent;
import com.jeta.abeille.gui.sql.SQLMediatorListener;
import com.jeta.abeille.licensemgr.AbeilleLicenseUtils;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays a dialog with a SQL that the user can edit in place.
 * 
 * @author Jeff Tassin
 */
public class AdHocSQLDialog extends SQLCommandDialog implements SQLMediatorListener {
	/** the view that displays the SQL */
	private SQLCommandView m_view;

	/** the sql to execute */
	private String m_sql;

	/** used by the SQLMediator */
	private SQLBuffer m_buffer;

	/**
	 * the class that spawns the worker thread that runs the SQL command in the
	 * background
	 */
	private SQLMediator m_mediator;

	/**
	 * ctor
	 */
	public AdHocSQLDialog(TSConnection conn, java.awt.Frame owner, boolean bModal) {
		super(conn, owner, bModal);
	}

	/**
	 * ctor
	 */
	public AdHocSQLDialog(TSConnection conn, java.awt.Dialog owner, boolean bModal) {
		super(conn, owner, bModal);
	}

	/**
	 * Creates a SQLDialog instance
	 */
	public static AdHocSQLDialog createAdHocSQLDialog(TSConnection conn, String sql, boolean bmodal) {
		AdHocSQLDialog dlg = (AdHocSQLDialog) SQLCommandDialog.createDialog(AdHocSQLDialog.class, conn,
				(java.awt.Component) null, bmodal);
		dlg.initialize(sql);
		TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
		return dlg;
	}

	/**
	 * This method determines if the dialog should check the license manager and
	 * disable certain features. Override if you wish to provide custom license
	 * checks.
	 */
	protected boolean checkLicense() {
		return true;
	}

	/**
	 * Override cmdCancel and check if a query is currently being run in the
	 * background.
	 */
	public void cmdCancel() {
		if (m_buffer == null || !m_buffer.isBusy())
			super.cmdCancel();
	}

	/**
	 * Close the dialog and set the ok flag
	 */
	public void cmdOk() {
		if (handleOk()) {
			invokeCommand();
		}
	}

	/**
	 * Initializes the dialog
	 */
	public void initialize(String sql) {
		assert (getConnection() != null);
		m_sql = sql;
		showSQLButton(false);
		setOkText(I18N.getLocalizedMessage("Commit"));
		if (sql == null || sql.length() == 0) {
			sql = I18N.getLocalizedMessage("No changes to commit");
			getOkButton().setEnabled(false);
		}
		m_view = new SQLCommandView(getConnection(), sql, true);
		setPrimaryPanel(m_view);

		if (checkLicense()) {
			LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
			if (jlm.isEvaluation() || AbeilleLicenseUtils.isBasic()) {
				getOkButton().setEnabled(false);
				m_view.getEditor().setEnabled(false);
				JPanel btnpanel = getButtonPanel();

				JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
				panel.add(javax.swing.Box.createHorizontalStrut(10));
				if (jlm.isEvaluation()) {
					panel.add(new JLabel(I18N.getLocalizedMessage("Editor_disabled_eval")));
				} else {
					panel.add(new JLabel(I18N.getLocalizedMessage("Editor_disabled_standard")));
				}
				btnpanel.add(panel, BorderLayout.WEST);
			}
		}
	}

	/**
	 * Gets called if an exception is thrown when the user tries to invoke the
	 * SQL in the editor. Override if you wish to provide more specialized
	 * handling.
	 */
	protected void handleInvocationException(SQLException se) {
		SQLErrorDialog.showErrorDialog(this, se, null);
	}

	/**
	 * Disables the editor and invokes the SQL
	 */
	private void invokeCommand() {
		try {
			if (m_buffer == null) {
				m_buffer = new SQLBuffer(getConnection());
				m_buffer.setEditor(m_view.getEditor());
			}

			m_buffer.setBusy(true);
			m_mediator = new SQLMediator(m_buffer.getConnectionReference(), m_buffer, this);
			m_mediator.start();
		} catch (SQLException se) {
			m_buffer.setBusy(false);
			m_mediator = null;
			handleInvocationException(se);
		}
	}

	/**
	 * SQLMediatorListener implementation. Shows the results of the given query
	 * by launching the sql results frame
	 * 
	 * @param mediator
	 *            the sql mediator that ran the command
	 */
	public void notifyEvent(SQLMediatorEvent evt) {
		if (evt.getID() == SQLMediatorEvent.ID_COMMAND_FINISHED) {
			SQLMediator mediator = evt.getMediator();
			if (mediator.getResult() == SQLMediator.SUCCESS) {
				try {
					m_buffer.getConnectionReference().commit();
				} catch (Exception e) {
					TSUtils.printException(e);
				}
				m_mediator = null;
				m_buffer = null;
				setOk(true);
				dispose();
			} else if (mediator.getResult() == SQLMediator.ERROR) {
				m_buffer.setBusy(false);
				// mediator should have rolled back the connection
				// SQLErrorDialog.showErrorDialog( mediator.getException(),
				// mediator.getLastSQL() );
				Exception e = mediator.getException();
				if (e == null)
					handleInvocationException(new SQLException(I18N.getLocalizedMessage("Unknown")));
				else if (e instanceof SQLException)
					handleInvocationException((SQLException) e);
				else
					handleInvocationException(new SQLException(e.getMessage()));
			} else if (mediator.getResult() == SQLMediator.CANCELED) {
				m_buffer.setBusy(false);
				// ingore
			}
		}
	}

	/**
    */
	protected boolean validateLicense() {
		return true;
	}
}
