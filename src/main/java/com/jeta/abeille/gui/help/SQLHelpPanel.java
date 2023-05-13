package com.jeta.abeille.gui.help;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.net.URL;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

public class SQLHelpPanel extends TSPanel {
	private SQLReferenceType m_sql_ref_type;
	private TSConnection m_connection;

	public SQLHelpPanel(TSConnection conn, JPanel delegate, SQLReferenceType refType) {
		m_connection = conn;
		m_sql_ref_type = refType;

		SQLReference ref = null;
		SQLReferenceService factory = (SQLReferenceService) conn.getImplementation(SQLReferenceService.COMPONENT_ID);
		if (factory != null)
			ref = factory.getReference();

		setLayout(new BorderLayout());
		add(delegate, BorderLayout.CENTER);
		if (ref != null && ref.supportsContent(refType)) {
			m_sql_ref_type = refType;
			JPanel btnpanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
			javax.swing.JLabel label = new javax.swing.JLabel("<html><body><u>SQL Reference</u></body></html>");
			label.setIcon(TSGuiToolbox.loadImage("incors/16x16/data_scroll.png"));
			label.setHorizontalTextPosition(javax.swing.JLabel.LEFT);
			label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			btnpanel.add(label);
			label.addMouseListener(new HelpLinkListener());
			add(btnpanel, BorderLayout.SOUTH);
		} else {
			add(Box.createVerticalStrut(5), BorderLayout.SOUTH);
		}
	}

	private class HelpLinkListener extends MouseAdapter {
		public void mouseClicked(MouseEvent evt) {
			SQLReference ref = null;
			SQLReferenceService factory = (SQLReferenceService) m_connection
					.getImplementation(SQLReferenceService.COMPONENT_ID);
			if (factory != null)
				ref = factory.getReference();

			if (ref != null) {
				URL url = ref.getContent(m_sql_ref_type);
				if (url != null) {
					TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
					SQLReferenceFrame frame = (SQLReferenceFrame) wsframe.show(SQLReferenceFrame.class, m_connection);
					frame.showReference(url);
				}
			}
		}
	}
}
