package com.jeta.abeille.gui.utils;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This is a dialog that asks the user to commit or rollback any changes. We
 * invoke this dialog in sitations where a window might be closing that has
 * uncommitted data.
 * 
 * @author Jeff Tassin
 */
public class CommitPanel extends TSPanel {

	public static final String ID_COMMIT = "commitpanel.commit";
	public static final String ID_ROLLBACK = "commitpanel.rollback";

	/**
	 * ctor
	 * 
	 * @param msg
	 *            the message that appears at the top of the panel
	 */
	public CommitPanel(String msg) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		ButtonGroup grp = new ButtonGroup();
		JRadioButton commitbtn = new JRadioButton(I18N.getLocalizedMessage("Commit"),
				TSGuiToolbox.loadImage("commit_unsel16.gif"));
		commitbtn.setRolloverIcon(TSGuiToolbox.loadImage("commit16.gif"));
		commitbtn.setSelectedIcon(TSGuiToolbox.loadImage("commit16.gif"));
		commitbtn.setName(ID_COMMIT);

		grp.add(commitbtn);

		JRadioButton rollbackbtn = new JRadioButton(I18N.getLocalizedMessage("Rollback"),
				TSGuiToolbox.loadImage("rollback_unsel16.gif"));
		rollbackbtn.setRolloverIcon(TSGuiToolbox.loadImage("rollback16.gif"));
		rollbackbtn.setSelectedIcon(TSGuiToolbox.loadImage("rollback16.gif"));
		rollbackbtn.setName(ID_ROLLBACK);
		grp.add(rollbackbtn);

		JPanel btnpanel = new JPanel();
		btnpanel.add(commitbtn);
		btnpanel.add(rollbackbtn);

		JPanel mainpanel = new JPanel();
		mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));

		JPanel msgpanel = new JPanel();
		msgpanel.add(new JLabel(msg));

		mainpanel.add(msgpanel);
		mainpanel.add(btnpanel);

		add(mainpanel, BorderLayout.NORTH);
	}

}
