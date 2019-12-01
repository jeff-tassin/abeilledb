/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.lang.ref.WeakReference;

import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.DefaultFocusManager;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.TextAction;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This is a combobox that provides enhanced selection as you type
 */
public class TSComboBox extends JPanel {
	private JTextField m_editor;
	private JButton m_btn;
	private PopupList m_popup = new PopupList(this);

	/**
	 * Flag that indicates if this combobox validates the typed input. If the
	 * user text is not found in the combo list (and the combo is validating),
	 * then the editor text will display red.
	 */
	private boolean m_validating = true;

	/**
	 * The set of listeners that get notified when an item is selected in the
	 * combo box
	 */
	private LinkedList m_listeners = new LinkedList();

	/**
	 * The action command that is sent in the ActionEvent to listeners (this is
	 * the same as JComboBox action command)
	 */
	private static final String m_actioncommand;

	/**
	 * flag that indicates if we are explicitly in the process of notifying
	 * listeners of a change in the combo box field
	 */
	private boolean m_notifying = false;

	private Action m_default_enter_action;
	private Action m_default_tab_action;

	static {
		// get the action command for a standard combo box so we can use it here
		JComboBox cbox = new JComboBox();
		m_actioncommand = cbox.getActionCommand();
	}

	/**
	 * ctor
	 */
	public TSComboBox() {
		m_btn = new ComboButton(TSGuiToolbox.loadImage("combobox16.gif"));
		m_editor = new ComboTextField();

		Dimension ed = m_editor.getPreferredSize();
		int height = ed.height;
		if (height == 0)
			height = 16;

		m_btn.setMaximumSize(new Dimension(20, height));
		m_btn.setPreferredSize(new Dimension(20, height));

		m_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!m_popup.isVisible()) {
					showPopup();
					m_editor.requestFocus();
				}
			}
		});

		// this listener gets events when the user selects an item in the combo
		// box
		m_popup.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					m_editor.setText(m_popup.getSelectedText());
				}
			}
		});

		// when the user double clicks, close the popup
		m_popup.getList().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				m_editor.setText(m_popup.getSelectedText());
				hidePopup();
				m_editor.requestFocus();
				// this is a major hack. the UI seems to have a focus listener
				// that reverts focus back
				// to the button. there is no apparent way to tell the UI to
				// undo the focus.
				// there is probabaly an elegant solution to this, but it is not
				// apparent
				// TSGuiToolbox.simulateMouseClick( m_editor, 1, 1 );
			}
		});

		m_popup.getList().addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				TSComboBox.this.keyTyped(e);
			}
		});

		KeyStroke tab_key = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, false);
		Object tabbind = m_editor.getInputMap().get(tab_key);
		if (tabbind != null) {
			m_default_tab_action = m_editor.getActionMap().get(tabbind);
		}

		m_editor.getInputMap().put(tab_key, "tabaction");
		m_editor.getActionMap().put("tabaction", new TabAction());

		KeyStroke enter_key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
		Object ebind = m_editor.getInputMap().get(enter_key);
		m_editor.getInputMap().put(enter_key, "enteraction");

		m_default_enter_action = m_editor.getActionMap().get(ebind);
		m_editor.getActionMap().put("enteraction", new EnterAction());

		setLayout(new BorderLayout());
		add(m_editor, BorderLayout.CENTER);
		add(m_btn, BorderLayout.EAST);
	}

	/**
	 * Adds a listener that gets an event when the user changes the selection or
	 * types in the edit field
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Adds an object to the combo list
	 */
	public void addItem(Object item) {
		m_popup.getModel().add(item);
	}

	/**
	 * Returns the action command that is included in the event sent to action
	 * listeners
	 */
	public String getActionCommand() {
		return m_actioncommand;
	}

	/**
	 * @return the button used to drop down the popup list
	 */
	public JButton getButton() {
		return m_btn;
	}

	/**
	 * @return the Editor component of this combo box
	 */
	public JTextField getEditor() {
		return m_editor;
	}

	public Font getFont() {
		if (m_editor == null)
			return super.getFont();
		else
			return m_editor.getFont();
	}

	public FontMetrics getFontMetrics(Font f) {
		if (m_editor == null)
			return super.getFontMetrics(f);
		else
			return m_editor.getFontMetrics(f);
	}

	public PopupList getPopupList() {
		return m_popup;
	}

	/**
	 * @return the preferred size for this combobox
	 */
	public Dimension getPreferredSize() {
		Dimension d = new Dimension(m_editor.getPreferredSize());
		d.width = TSGuiToolbox.calculateAverageTextWidth(m_editor, 15);
		d.width += m_btn.getWidth();
		return d;
	}

	/**
	 * @return the selected item in the combo box. If an invalid item is typed
	 *         in the edit field, null is returned
	 */
	public Object getSelectedItem() {
		return m_popup.getItem(getText());
	}

	public String getText() {
		return m_editor.getText().trim();
	}

	/**
	 * Hides the popup because we are finished typeing/editing
	 */
	public void hidePopup() {
		m_notifying = true;
		m_popup.setVisible(false);
		validateInput();
		notifyListeners();
		m_notifying = false;
	}

	/**
	 * @return the flag that indicates whether this combobox will validate the
	 *         user input or not.
	 */
	public boolean isValidating() {
		return m_validating;
	}

	/**
	 * @return true of the editor text is contained in the list model.
	 */
	public boolean isValueInModel() {
		return m_popup.getModel().containsString(getText());
	}

	/**
	 * Key adapter interface
	 */
	public void keyTyped(KeyEvent evt) {
		if (evt.getID() == KeyEvent.KEY_TYPED && evt.getKeyChar() != '\n' && evt.getKeyChar() != '\t') {
			/**
			 * we do this in case the user is typing a menu command. We don't
			 * want these
			 */
			if (!evt.isAltDown() && !evt.isControlDown() && !evt.isMetaDown()) {
				m_editor.setForeground(Color.black);
				showPopup();
				String str = m_editor.getText();
				m_popup.selectText(str);
				// if ( !TSGuiToolbox.isWindowsLookAndFeel() )
				m_editor.requestFocus();
			}
		}
	}

	/**
	 * Notifies any action listeners that an item has been selected in the combo
	 * box
	 */
	public void notifyListeners() {
		ActionEvent evt = new ActionEvent(this, 0, getActionCommand());
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			ActionListener listener = (ActionListener) iter.next();
			if (listener != null)
				listener.actionPerformed(evt);
		}
	}

	/**
	 * Called by the popup list when it is hidden. We need this to notify any
	 * listeners The ComponentListener.componentHidden method never seems be be
	 * called
	 */
	public void notifyPopupHidden(PopupList popup) {
		if (!m_notifying) {
			validateInput();
			notifyListeners();
		}
	}

	/**
	 * Removes all items from the combo list
	 */
	public void removeAllItems() {
		m_popup.getModel().clear();
	}

	public void removeNotify() {
		m_popup.setVisible(false);
		super.removeNotify();
	}

	/**
	 * Request focus.
	 */
	public void requestFocus() {
		m_editor.requestFocus();
	}

	/**
	 * Selects the text in the editor
	 */
	public void selectEditorText() {
		m_editor.selectAll();
	}

	/**
	 * Enables/Disables this component
	 * 
	 * @param bEnabled
	 *            true/false to enable/disable
	 */
	public void setEnabled(boolean bEnabled) {
		super.setEnabled(bEnabled);
		m_editor.setEditable(bEnabled);
		m_editor.setEnabled(bEnabled);
		m_btn.setEnabled(bEnabled);
	}

	/**
	 * Sets the currently selected item in the combo box
	 */
	public void setSelectedItem(Object obj) {
		// if ( obj != null && m_popup.getModel().containsString( obj.toString()
		// ) )
		if (obj == null)
			m_editor.setText("");
		else
			m_editor.setText(obj.toString());

		String str = m_editor.getText();
		m_popup.selectText(str);

		validateInput();
	}

	/**
	 * displays the popup menu
	 */
	void showPopup() {
		int x = m_editor.getX();
		int y = m_editor.getY() + m_editor.getHeight();
		Dimension d = m_popup.getPreferredSize();

		JList list = m_popup.getList();
		Font f = list.getFont();
		FontMetrics metrics = list.getFontMetrics(f);
		int numitems = m_popup.getModel().getSize();
		if (numitems < 4)
			numitems = 4;
		else if (numitems > 9)
			numitems = 9;

		int height = metrics.getHeight() * (numitems + 1);

		d.width = m_editor.getWidth();
		// if ( d.height > height )
		d.height = height;

		m_popup.setPreferredSize(d);
		m_popup.setMaximumSize(d);
		m_popup.setSize(d);
		// m_popup.show( this, x, y );
		m_popup.show(m_editor, x, y);

	}

	/**
	 * Sets the flag that indicates whether this combobox will validate the user
	 * input or not.
	 */
	public void setValidating(boolean validate) {
		m_validating = validate;
	}

	/**
	 * Checks the value in the text field and determines if it is located in the
	 * model. If the value is in the model, we set the text color to black. If
	 * the value is not in the model, we set the color to red.
	 */
	void validateInput() {
		if (m_popup.isVisible()) {
			m_editor.setForeground(Color.black);
			return;
		}

		if (isValidating()) {
			Object selectedvalue = m_popup.getSelectedValue();
			if (selectedvalue != null) {
				if (m_popup.isValid(m_editor.getText()))
					m_editor.setForeground(Color.black);
				else
					m_editor.setForeground(Color.red);

			} else
				m_editor.setForeground(Color.red);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	//
	public class ComboTextField extends JTextField {
		protected void processKeyEvent(KeyEvent e) {
			super.processKeyEvent(e);
			// handleKeyEvent( e );
			if (e.getID() == KeyEvent.KEY_TYPED) {
				TSComboBox.this.keyTyped(e);
			}
		}

		public boolean isManagingFocus() {
			return true;
		}
	}

	public class ComboButton extends JButton {
		public ComboButton(Icon icon) {
			super(icon);
		}

		protected void processMouseEvent(MouseEvent e) {
			if (e.getID() == MouseEvent.MOUSE_PRESSED) {
				// I need to trap the mouse events here because the JPopupMenu
				// also traps mouse events
				// and automatically hides the popup when the mouse is clicked
				// to another child window.
				// Without this code, I could never detect if the popup were
				// showing or not when the user
				// clicked the combo button.
				if (m_popup.isVisible()) {
					hidePopup();

					// this is a major hack. the UI seems to have a focus
					// listener that reverts focus back
					// to the button. there is no apparent way to tell the UI to
					// undo the focus.
					// there is probabaly an elegant solution to this, but it is
					// not apparent
					// TSGuiToolbox.simulateMouseClick( m_editor, 1, 1 );

					m_editor.requestFocus();
					e.consume();
					return;
				}
			}
			super.processMouseEvent(e);
		}

	}

	/**
	 * When user hits the enter key on the editor
	 */
	public class EnterAction extends TextAction {
		public EnterAction() {
			super("enteraction");
		}

		public void actionPerformed(ActionEvent evt) {
			String str = m_editor.getText();
			String matchtxt = m_popup.selectText(str);

			if (matchtxt != null && matchtxt.length() > 0) {
				m_editor.setText(matchtxt);
			}
			hidePopup();

			// this is a major hack. the UI seems to have a focus listener that
			// reverts focus back
			// to the button. there is no apparent way to tell the UI to undo
			// the focus.
			// there is probabaly an elegant solution to this, but it is not
			// apparent
			// TSGuiToolbox.simulateMouseClick( m_editor, 1, 1 );

			if (m_default_enter_action != null)
				m_default_enter_action.actionPerformed(evt);
		}
	}

	/**
	 * When user hits the tab key on the editor
	 */
	public class TabAction extends TextAction {
		public TabAction() {
			super("tabaction");
		}

		public void actionPerformed(ActionEvent evt) {
			// evt.consume();
			String str = m_editor.getText();

			PopupList.Result presult = m_popup.selectCommonText(str);
			m_editor.setText(presult.completion);
			if (presult.matches <= 1) {
				m_btn.requestFocus();
				hidePopup();
				if (m_default_tab_action != null) {
					m_default_tab_action.actionPerformed(evt);
				}
			}
		}
	}

}
