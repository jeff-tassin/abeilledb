package com.jeta.abeille.gui.utils;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.i18n.I18N;

/**
 * This is a simple panel that asks the user if they wish to commit any changes
 * to the database.
 * 
 * @author Jeff Tassin
 */
public class ConfirmCommitPanel extends TSPanel {
	private JLabel m_label;
	private JCheckBox m_checkbox;

	private static JButton m_sizer = new JButton(I18N.getLocalizedMessage("Cancel"));

	/**
	 * ctor
	 */
	public ConfirmCommitPanel() {
		this(I18N.getLocalizedMessage("Confirm_Commit"), I18N.getLocalizedMessage("Dont_show_this_message_again"));
	}

	/**
	 * ctor
	 * 
	 * @param msg
	 *            the message that appears on the dialog
	 * @param cboxmsg
	 *            the check box label
	 */
	public ConfirmCommitPanel(String msg, String cboxmsg) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		m_label = new JLabel(msg);
		m_checkbox = new JCheckBox(cboxmsg);

		add(m_label);
		add(javax.swing.Box.createVerticalStrut(5));
		add(m_checkbox);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}

	/**
	 * @return the checkbox component
	 */
	public JCheckBox getCheckBox() {
		return m_checkbox;
	}

	/**
	 * @return the preferred size for the dialog
	 */
	public Dimension getPreferredSize() {
		Font f = m_label.getFont();
		FontMetrics metrics = m_label.getFontMetrics(f);
		int width = metrics.stringWidth(m_label.getText()) + 40;
		int height = metrics.getHeight() * 4 + 10;

		Dimension d = m_sizer.getPreferredSize();
		if (width < (d.width * 3 + 20))
			width = d.width * 3 + 20;

		return new Dimension(width, height);
	}

	public boolean isShowCommitDialog() {
		return !m_checkbox.isSelected();
	}

	public void setMessage(String msg) {
		m_label.setText(msg);
	}
}
