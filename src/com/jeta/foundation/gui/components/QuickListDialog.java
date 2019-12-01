/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.*;

public class QuickListDialog extends JDialog {
	private TSComboBox m_combo;
	private boolean m_bOk;

	public QuickListDialog(Frame frame, String title, String labelText) {
		super(frame, title, true);

		m_bOk = false;

		// buttons
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_bOk = false;
				QuickListDialog.this.setVisible(false);
			}
		});
		final JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_bOk = true;
				QuickListDialog.this.setVisible(false);
			}
		});

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel label = new JLabel(labelText);
		// label.setLabelFor(m_combo);
		panel.add(label);

		JPanel fpanel = new JPanel();
		fpanel.setLayout(new BorderLayout());
		m_combo = new TSComboBox();
		fpanel.add(m_combo, BorderLayout.NORTH);

		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		panel.add(fpanel);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(okButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(cancelButton);

		// Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.add(panel, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.SOUTH);
		pack();
	}

	public String getValue() {
		return m_combo.getText();
	}

	public boolean isOk() {
		return m_bOk;
	}
}
