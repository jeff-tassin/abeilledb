package com.jeta.abeille.gui.utils;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays a dialog used when performing a DROP operation on a
 * database object. Most DROPS have a cascade option. So, we have this confirm
 * dialog that shows a cascade check box along with a message indicating the
 * object we are about to drop. The dialog has the following layout:
 * 
 * [msg] [ x cascade check ] [show sql] [ ok ] [ cancel ]
 * 
 * @author Jeff Tassin
 */
public class DropDialog extends SQLCommandDialog {
	/** the main view for this dialog */
	private ConfirmCommitPanel m_commitpanel;

	/**
	 * ctor
	 */
	public DropDialog(TSConnection conn, Frame owner, boolean bModal) {
		super(conn, owner, bModal);
	}

	/**
	 * ctor
	 */
	public DropDialog(TSConnection conn, Dialog owner, boolean bModal) {
		super(conn, owner, bModal);
	}

	/**
	 * Creates a SQL command dialog instance
	 */
	public static DropDialog createDropDialog(TSConnection conn, boolean bmodal) {
		assert (conn != null);
		return (DropDialog) SQLCommandDialog.createDialog(DropDialog.class, conn, (Component) null, bmodal);
	}

	/**
	 * Creates a SQL command dialog instance
	 */
	public static DropDialog createDropDialog(TSConnection conn, TSInternalFrame iframe, boolean bmodal) {
		assert (conn != null);

		if (iframe == null)
			return (DropDialog) SQLCommandDialog.createDialog(DropDialog.class, conn, (Component) null, bmodal);
		else
			return (DropDialog) SQLCommandDialog.createDialog(DropDialog.class, conn, iframe.getDelegate(), bmodal);
	}

	/**
	 * Creates a SQL command dialog instance
	 */
	public static DropDialog createDropDialog(TSConnection conn, Component owner, boolean bmodal) {
		assert (conn != null);

		return (DropDialog) SQLCommandDialog.createDialog(DropDialog.class, conn, owner, bmodal);
	}

	/**
	 * @return true if the cascade check is selected
	 */
	public boolean isCascade() {
		return m_commitpanel.getCheckBox().isSelected();
	}

	/**
	 * Initializes the components on this dialog
	 */
	private void initialize() {
		if (m_commitpanel == null) {
			String title = I18N.getLocalizedMessage("Confirm Drop");
			String cboxtxt = I18N.getLocalizedMessage("Cascade");
			m_commitpanel = new ConfirmCommitPanel("", cboxtxt);
			setPrimaryPanel(m_commitpanel);
			setTitle(title);
			setInitialFocusComponent(getCloseButton());
		}
	}

	/**
	 * Enables or disables the cascade check box
	 */
	public void setCascadeEnabled(boolean enable) {
		m_commitpanel.getCheckBox().setEnabled(enable);
	}

	/**
	 * Sets the database connection
	 */
	public void setConnection(TSConnection conn) {
		super.setConnection(conn);
		assert (conn != null);
		initialize();
		if (conn.getDatabase().equals(Database.MYSQL)) {
			setCascadeEnabled(false);
		}
	}

	/**
	 * Sets the main message to display in the dialog
	 */
	public void setMessage(String msg) {
		m_commitpanel.setMessage(msg);
		setSize(getPreferredSize());
	}

}
