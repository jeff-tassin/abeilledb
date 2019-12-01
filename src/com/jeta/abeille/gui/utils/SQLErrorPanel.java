package com.jeta.abeille.gui.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSStatusBar;

import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This class is used to display a SQLException. It provides a text area to
 * display the exception information as well as a text area to display the SQL
 * that caused the exception
 * 
 * @author Jeff Tassin
 */
public class SQLErrorPanel extends TSPanel {
	private JLabel m_error_label = new JLabel();

	/**
	 * ctor
	 */
	public SQLErrorPanel() {

	}

	public static String format(Exception e, String sql) {
		if (e == null)
			return "";

		StringBuffer result = new StringBuffer();
		result.append(e.getClass().getName());
		result.append("\n");
		String errormsg = e.getLocalizedMessage();
		if (errormsg != null && errormsg.length() > 1024) {
			result.append(errormsg.substring(0, 1024));
			result.append("...");
		} else
			result.append(errormsg);

		return result.toString();

	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(5, 8);
	}

	/**
	 * Initializes the panel with the given data.
	 * 
	 * @param preMsg
	 *            a message to prepend to the error message text
	 * @param e
	 *            the exception that was thrown. The error message of this
	 *            exception will be displayed.
	 * @param sql
	 *            the sql string the caused the exception. This string is also
	 *            displayed in the pane
	 */
	void initialize(String preMsg, Exception e, String sql, boolean rollback) {
		setLayout(new BorderLayout());

		JTextArea msgcomp = TSEditorUtils.createTextArea();
		msgcomp.setLineWrap(true);
		msgcomp.setWrapStyleWord(true);

		StringBuffer msg = new StringBuffer();
		if (preMsg != null)
			msg.append(preMsg);
		msg.append(SQLErrorPanel.format(e, sql));

		msgcomp.setText(msg.toString());

		JScrollPane msgpane = new JScrollPane(msgcomp);

		if (sql != null) {
			// JTextArea sqlcomp = TSEditorUtils.createTextArea();
			// sqlcomp.setLineWrap( true );
			// sqlcomp.setText( sql );
			// JScrollPane sqlpane = new JScrollPane( sqlcomp );
			// m_tab.addTab( I18N.getLocalizedMessage("SQL"), sqlpane );
		}

		add(msgpane, BorderLayout.CENTER);

		if (rollback) {
			TSStatusBar statusbar = new TSStatusBar();
			String tmsg = I18N.getLocalizedMessage("Transaction_rolledback");
			TSCell cell1 = new TSCell("main", tmsg);
			cell1.setText(tmsg);
			cell1.setHorizontalAlignment(SwingConstants.LEFT);
			cell1.setMain(true);
			statusbar.addCell(cell1);
			add(statusbar, BorderLayout.SOUTH);
		}

		JLabel label = null;
		if (e instanceof SQLException)
			m_error_label.setText(I18N.getLocalizedMessage("SQL Exception"));
		else
			m_error_label.setText(I18N.getLocalizedMessage("Error"));

		m_error_label.setIcon(javax.swing.UIManager.getIcon("OptionPane.errorIcon"));
		m_error_label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));
		add(m_error_label, BorderLayout.NORTH);

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	/**
	 * Sets the caption message at the top of this panel.
	 */
	public void setErrorCaption(String msg) {
		m_error_label.setText(msg);
	}

}
