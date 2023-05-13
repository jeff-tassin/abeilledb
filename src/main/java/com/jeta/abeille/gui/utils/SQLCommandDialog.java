package com.jeta.abeille.gui.utils;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Constructor;

import java.sql.SQLException;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.command.SQLCommand;
import com.jeta.abeille.gui.command.SQLFilter;
import com.jeta.abeille.licensemgr.AbeilleLicenseUtils;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class behaves exactly like an ordinary dialog box with one exception. It
 * displays a 'Show SQL' button next to the Ok and Cancel buttons. [ dialog
 * content ] [sql][ok][cancel]
 * 
 * This dialog is intended to be used with SQL commands. It allows the user to
 * see the SQL that will be run without actually sending the command to the
 * database.
 * 
 * @author Jeff Tassin
 */
public class SQLCommandDialog extends DatabaseDialog implements SQLFilter {
	/** this is a list of SQLDialogListener objects */
	private LinkedList m_listeners;

	/**
	 * This object is used to capture SQL commands instead of sending them to
	 * the database. It is used in SQL preview mode.
	 */
	private StringBuffer m_sqlbuff = new StringBuffer();

	public static final String ID_SQL_BUTTON = "sql.button";

	/**
	 * ctor
	 */
	public SQLCommandDialog(TSConnection conn, Frame owner, boolean bModal) {
		super(owner, bModal);
		assert (conn != null);
		setConnection(conn);
	}

	/**
	 * ctor
	 */
	public SQLCommandDialog(TSConnection conn, Dialog owner, boolean bModal) {
		super(owner, bModal);
		assert (conn != null);
		setConnection(conn);
	}

	/**
	 * Adds a listener to the list of listeners for this dialog
	 */
	public void addDialogListener(SQLDialogListener listener) {
		if (m_listeners == null)
			m_listeners = new LinkedList();

		assert (listener != null);
		assert (!m_listeners.contains(listener));

		m_listeners.add(listener);
	}

	/**
	 * Clears the sql buffer
	 */
	private void clearSQLBuffers() {
		m_sqlbuff.setLength(0);
	}

	/**
	 * Close the dialog and set the ok flag
	 */
	public void cmdOk() {
		if (handleOk()) {
			setOk(true);
			dispose();
		}
	}

	/**
	 * Creates a SQL command dialog instance
	 */
	public static SQLCommandDialog createDialog(TSConnection conn, boolean bmodal) {
		return SQLCommandDialog.createDialog(conn, (Component) null, bmodal);
	}

	/**
	 * Creates a SQL command dialog instance
	 */
	public static SQLCommandDialog createDialog(TSConnection conn, TSInternalFrame iframe, boolean bmodal) {
		if (iframe == null)
			return SQLCommandDialog.createDialog(SQLCommandDialog.class, conn, (Component) null, bmodal);
		else
			return SQLCommandDialog.createDialog(SQLCommandDialog.class, conn, iframe.getDelegate(), bmodal);
	}

	/**
	 * Creates a SQL command dialog instance
	 */
	public static SQLCommandDialog createDialog(TSConnection conn, Component owner, boolean bmodal) {
		return SQLCommandDialog.createDialog(SQLCommandDialog.class, conn, owner, bmodal);
	}

	/**
	 * Creates a SQL command dialog instance
	 */
	public static SQLCommandDialog createDialog(Class dlgclass, TSConnection conn, Component owner, boolean bmodal) {
		Class[] cparams = new Class[3];
		Object[] params = new Object[3];

		assert (SQLCommandDialog.class.isAssignableFrom(dlgclass));

		cparams[0] = TSConnection.class;
		params[0] = conn;

		if (owner instanceof Dialog) {
			cparams[1] = Dialog.class;
			params[1] = owner;
		} else if (owner instanceof Frame) {
			cparams[1] = Frame.class;
			params[1] = owner;
		} else {
			if (owner == null) {
				cparams[1] = Frame.class;
				params[1] = null;
			} else {
				Window win = SwingUtilities.getWindowAncestor(owner);
				if (win instanceof Dialog) {
					cparams[1] = Dialog.class;
					params[1] = win;
				} else if (win instanceof Frame) {
					cparams[1] = Frame.class;
					params[1] = win;
				} else {
					cparams[1] = Frame.class;
					params[1] = null;
				}
			}
		}
		cparams[2] = boolean.class;
		params[2] = Boolean.valueOf(bmodal);

		try {
			Constructor ctor = dlgclass.getConstructor(cparams);
			return (SQLCommandDialog) ctor.newInstance(params);
		} catch (java.lang.reflect.InvocationTargetException ite) {
			TSUtils.printException((Exception) ite.getCause());
		} catch (Exception e) {
			TSUtils.printException(e);
		}
		return null;
	}

	/**
	 * Method that gets called when the ok button is pressed
	 */
	protected boolean handleOk() {
		return (validateValidators() && validateListeners() && validateLicense() && validateSQLListeners());
	}

	/**
	 * Add our show SQL button
	 */
	protected void _initialize() {
		super._initialize();
		JPanel btnpanel = getButtonPanel();
		JButton showsql = new JButton(I18N.getLocalizedMessage("SQL"));
		showsql.setName(ID_SQL_BUTTON);
		showsql.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showSQL();
			}
		});
		btnpanel.add(showsql, 0);
		setCloseText(I18N.getLocalizedMessage("Cancel"));

	}

	/**
	 * Sets the message in the Ok button and dialog title to the given string.
	 */
	public void setMessage(String msg) {
		setOkText(msg);
		setTitle(msg);
	}

	public void showSQLButton(boolean bshow) {
		JButton btn = (JButton) getComponentByName(ID_SQL_BUTTON);
		btn.setVisible(bshow);
	}

	/**
	 * Displays any captured SQL in a dialog.
	 */
	private void showPreviewDialog() {
		SQLFilter oldfilter = SQLCommand.getFilter();
		try {
			SQLCommand.removeFilter(oldfilter);

			String sql = m_sqlbuff.toString().trim();
			if (sql.length() == 0) {
				sql = I18N.getLocalizedMessage("No changes to commit");
			}

			AdHocSQLDialog dlg = AdHocSQLDialog.createAdHocSQLDialog(getConnection(), sql, true);
			dlg.setTitle(I18N.format("sql_preview_title_1", getTitle()));
			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.addDialogListener(new SQLDialogListener() {
				public boolean cmdOk() throws SQLException {
					LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
					return !jlm.isEvaluation();
				}
			});
			dlg.showCenter();
			if (dlg.isOk()) {
				setOk(true);
				dispose();
			}
		} finally {
			SQLCommand.addFilter(oldfilter);
		}
	}

	/**
	 * This method runs the SQL command but traps the output instead of sending
	 * it to the database. This allows the user to preview the SQL
	 */
	public void showSQL() {
		try {
			clearSQLBuffers();
			SQLCommand.addFilter(this);
			if (validateValidators() && validateListeners() && validateSQLListeners())
				showPreviewDialog();
		} finally {
			SQLCommand.removeFilter(this);
		}
	}

	/**
	 * SQLFilter implementation
	 */
	public void sqlCommand(String sql) {
		m_sqlbuff.append(sql);
		m_sqlbuff.append("\n");
	}

	/**
	 * If we are running in evaluation mode, don't allow changing
	 */
	protected boolean validateLicense() {
		LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
		if (jlm.isEvaluation()) {
			jlm.postMessage(this, I18N.getLocalizedMessage("feature_disabled_evaluation"));
			return false;
		} else if (AbeilleLicenseUtils.isBasic()) {
			jlm.postMessage(this, I18N.getLocalizedMessage("feature_disabled_standard"));
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Iterates through the list of SQL Dialog listeners and allows them to
	 * process the inputs. For example, the user may be inputing parameters for
	 * a databse. We would like to perform the database operation. If the
	 * operation fails, we would like the dialog to remain on the screen so the
	 * user can make any appropriate adjustments to the dialog data. This is the
	 * purpose of the TSDialogListeners.
	 */
	protected boolean validateSQLListeners() {
		if (m_listeners != null) {
			Iterator iter = m_listeners.iterator();
			while (iter.hasNext()) {
				SQLDialogListener listener = (SQLDialogListener) iter.next();
				try {
					if (!listener.cmdOk())
						return false;
				} catch (SQLException sqe) {
					// @todo getSQL from SQLCommand filters
					SQLErrorDialog.showErrorDialog(this, sqe, null);
					return false;
				}
			}
		}
		return true;
	}

}
