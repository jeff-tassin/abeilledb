/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.quickedit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.text.JTextComponent;

import com.jeta.foundation.gui.components.TSDialog;

/**
 * This class shows quick edit view in a dialog without a title bar We do this
 * to minimize screen real estate as well as provide a more seemless view of the
 * surrounding gui. The dialog is used mainly as a replacement for in-place
 * editing for JTables.
 * 
 * @author Jeff Tassin
 */
public class QuickEditDialog extends TSDialog {
	/** the main view for this dialog */
	private QuickEditView m_view;

	/**
	 * flag indicating if the user pressed the ID_EDITOR_BUTTON (...) on the
	 * view
	 */
	private boolean m_showmore = false;

	/**
	 * ctor
	 */
	public QuickEditDialog(Dialog owner, boolean bModal) {
		super(owner, bModal);
	}

	/**
	 * ctor
	 */
	public QuickEditDialog(Frame owner, boolean bModal) {
		super(owner, bModal);
	}

	public void dispose() {
		super.dispose();
		m_view.removeAll();
	}

	/**
	 * Returns the preferred size for this dialog
	 */
	public Dimension getPreferredSize() {
		return m_view.getPreferredSize();
	}

	/**
	 * @return the text in the editor
	 */
	public String getText() {
		return m_view.getText();
	}

	/**
	 * Creates and initializes the view for this dialog
	 * 
	 * @param editor
	 *            the text editor component that is used by the view
	 */
	public void initialize(JTextComponent editor) {
		setUndecorated(true);
		Container container = getContentPane();
		container.setLayout(new BorderLayout());

		// JTextComponent editor = new javax.swing.JTextField();
		m_view = new QuickEditView(editor);
		container.add(m_view, BorderLayout.NORTH);

		/*
		 * editor.addKeyListener( new KeyAdapter() { public void keyPressed(
		 * KeyEvent evt ) { if ( evt.getKeyCode() == KeyEvent.VK_ENTER ) {
		 * cmdOk(); } if ( evt.getKeyCode() == KeyEvent.VK_ESCAPE ) {
		 * cmdCancel(); }
		 * 
		 * } });
		 */

		JButton btn = (JButton) m_view.getComponentByName(QuickEditView.ID_EDITOR_BTN);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_showmore = true;
				cmdOk();
			}
		});

		btn = (JButton) m_view.getComponentByName(QuickEditView.ID_CLOSE_BTN);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cmdOk();
			}
		});

	}

	/**
	 * @return the flag indicating if the user pressed the ID_EDITOR_BUTTON (...)
	 *         on the view
	 */
	public boolean isShowMore() {
		return m_showmore;
	}

}
