package com.jeta.abeille.gui.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JRadioButton;

import com.jeta.foundation.gui.components.TSDialog;

/**
 * This is a dialog that asks the user to commit or rollback any changes. We
 * invoke this dialog in sitations where a window might be closing that has
 * uncommitted data.
 * 
 * @author Jeff Tassin
 */
public class CommitDialog extends TSDialog {
	/** the gui component panel */
	private CommitPanel m_panel;

	public static int COMMIT = 1;
	public static int ROLLBACK = 2;

	/**
	 * ctor
	 */
	public CommitDialog(java.awt.Frame owner, boolean bModal) {
		super(owner, bModal);
	}

	/**
	 * ctor
	 */
	public CommitDialog(java.awt.Dialog owner, boolean bModal) {
		super(owner, bModal);
	}

	/**
	 * @return the action selected by the user
	 */
	public int getResult() {
		JRadioButton btn = (JRadioButton) m_panel.getComponentByName(CommitPanel.ID_COMMIT);
		if (btn.isSelected())
			return COMMIT;
		else
			return ROLLBACK;
	}

	/**
	 * Creates the controls for this dialog
	 * 
	 * @param msg
	 *            the message to show in the dialog
	 */
	public void initialize(String msg) {
		m_panel = new CommitPanel(msg);
		setPrimaryPanel(m_panel);

		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JButton okbtn = getOkButton();
				okbtn.setEnabled(true);
			}
		};

		JRadioButton btn = (JRadioButton) m_panel.getComponentByName(CommitPanel.ID_COMMIT);
		btn.addActionListener(listener);
		btn = (JRadioButton) m_panel.getComponentByName(CommitPanel.ID_ROLLBACK);
		btn.addActionListener(listener);

		JButton okbtn = getOkButton();
		okbtn.setEnabled(false);
		okbtn.requestFocus();
	}

}
