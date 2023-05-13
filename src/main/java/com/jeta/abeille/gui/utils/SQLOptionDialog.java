package com.jeta.abeille.gui.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays a dialog used when performing a SQL operation that has a
 * simple yes/no option. This is different the the standard drop dialog because
 * there is no check box.
 * 
 * [icon][msg] [show sql] [ ok ] [ cancel ]
 * 
 * @author Jeff Tassin
 */
public class SQLOptionDialog extends SQLCommandDialog {
	private JLabel m_msg = new JLabel();

	/**
	 * ctor
	 */
	public SQLOptionDialog(TSConnection conn, Frame owner, boolean bModal) {
		super(conn, owner, bModal);
		initialize();
	}

	/**
	 * ctor
	 */
	public SQLOptionDialog(TSConnection conn, Dialog owner, boolean bModal) {
		super(conn, owner, bModal);
		initialize();
	}

	/**
	 * Creates a SQL command dialog instance
	 */
	public static SQLOptionDialog createOptionDialog(TSConnection conn, boolean bmodal) {
		assert (conn != null);

		return (SQLOptionDialog) SQLCommandDialog.createDialog(SQLOptionDialog.class, conn, (Component) null, bmodal);
	}

	/**
	 * Creates a SQL command dialog instance
	 */
	public static SQLOptionDialog createOptionDialog(TSConnection conn, TSInternalFrame iframe, boolean bmodal) {
		assert (conn != null);

		if (iframe == null)
			return (SQLOptionDialog) SQLCommandDialog.createDialog(SQLOptionDialog.class, conn, (Component) null,
					bmodal);
		else
			return (SQLOptionDialog) SQLCommandDialog.createDialog(SQLOptionDialog.class, conn, iframe.getDelegate(),
					bmodal);
	}

	/**
	 * Creates a SQL command dialog instance
	 */
	public static SQLOptionDialog createOptionDialog(TSConnection conn, Component owner, boolean bmodal) {
		assert (conn != null);

		return (SQLOptionDialog) SQLCommandDialog.createDialog(SQLOptionDialog.class, conn, owner, bmodal);
	}

	/**
	 * Initializes the components on this dialog
	 */
	private void initialize() {
		String title = I18N.getLocalizedMessage("Confirm");
		m_msg.setIcon(javax.swing.UIManager.getIcon("OptionPane.questionIcon"));

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(m_msg, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		setPrimaryPanel(panel);
		setTitle(title);
		setInitialFocusComponent(getCloseButton());
		setOkText(I18N.getLocalizedMessage("Yes"));
		setCloseText(I18N.getLocalizedMessage("No"));
	}

	/**
	 * @return the pref size for this dialog
	 */
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		Dimension d2 = TSGuiToolbox.getWindowDimension(5, 4);
		if (d.width < d2.width)
			d.width = d2.width;

		return d;
	}

	/**
	 * Sets the main message to display in the dialog
	 */
	public void setMessage(String msg) {
		m_msg.setText(msg);
		setSize(getPreferredSize());
	}

}
