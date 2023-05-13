package com.jeta.abeille.gui.model.utils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jeta.foundation.gui.components.StyledBannerView;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a Message with a checkbox. The message describes the
 * function of user-defined links. The checkbox allows the user to indicate that
 * this message should not be displayed again.
 * 
 * @author Jeff Tassin
 */
public class UserLinkInfoView extends TSPanel {
	public static final String ID_SHOW_USER_INFO_VIEW = "userlink.info.view.show";

	private JCheckBox m_show_view;

	/**
	 * ctor
	 */
	public UserLinkInfoView() {
		createView();
	}

	private void createView() {
		setLayout(new BorderLayout());

		java.util.LinkedList paras = new java.util.LinkedList();

		String msg = "User Defined Links establish relationships for existing tables\nthat don't have foreign key relationships. The Data Browser\nand Query Builder utilize these types of links.  This operation\ndoes not define a foreign key in the database. To create a\nforeign key, you must do it manually (see the help).";

		paras.add(new StyledBannerView.Paragraph("title", new StyledBannerView.Run("none", I18N
				.getLocalizedDialogLabel("Added User Defined Link"))));
		paras.addAll(StyledBannerView.createParagraphs("normal", msg));

		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		m_show_view = new JCheckBox(I18N.getLocalizedMessage("Dont_show_this_message_again"));
		bottom.add(m_show_view);

		StyledBannerView view = new StyledBannerView(paras, bottom) {
			protected JPanel createBottomPanel(JPanel content) {
				return content;
			}
		};

		add(view, BorderLayout.CENTER);
	}

	/**
	 * @return true if this view should be displayed
	 */
	public boolean isShowMessage() {
		return !m_show_view.isSelected();
	}

}
