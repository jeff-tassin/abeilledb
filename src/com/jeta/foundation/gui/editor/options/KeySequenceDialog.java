package com.jeta.foundation.gui.editor.options;

import java.lang.Math;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.netbeans.editor.*;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.editor.KitKeyBindingModel;
import com.jeta.foundation.gui.editor.KeyUtils;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETAContainer;

/**
 * This dialog allows the user to enter a keystroke sequence to assign to a
 * particular action
 * 
 * @author Jeff Tassin
 */
public class KeySequenceDialog extends TSDialog {
	/** panel that gets key stroke sequence */
	private com.jeta.foundation.gui.editor.options.KeySequenceInputPanel m_inputpanel;

	/** key binding model */
	private KeyBindingModel m_model;

	/** the binding that we are currently editing */
	private MultiKeyBinding m_binding;

	/** command ids */
	public static final String ID_CLEAR = "jeta.editor.options.KeySequenceDialog.clear";

	/**
	 * ctor
	 */
	public KeySequenceDialog(Dialog owner, boolean bModal) {
		super(owner, bModal);
		setTitle(I18N.getLocalizedMessage("Key Sequence"));

	}

	public void cmdOk() {
		KeyStroke[] seq = m_inputpanel.getKeySequence();
		String warn = getCollisionString(seq, false);
		if (warn == null)
			super.cmdOk();
		else
			m_inputpanel.setInfoText(warn);
	}

	/**
	 * @return the main panel for this dialog
	 */
	public KeySequenceInputPanel getInputPanel() {
		return m_inputpanel;
	}

	/**
	 * @return the set of keystrokes entered by the user
	 */
	public KeyStroke[] getKeySequence() {
		return m_inputpanel.getKeySequence();
	}

	/**
	 * Creates and initializes the controls for this dialog
	 */
	public void initialize(MultiKeyBinding binding) {
		m_inputpanel = new KeySequenceInputPanel();
		setPrimaryPanel(m_inputpanel);

		// add the clear button
		JPanel btnpanel = (JPanel) getOkButton().getParent();

		JButton clearbtn = new JButton(I18N.getLocalizedMessage("Clear"));
		clearbtn.setName(ID_CLEAR);
		btnpanel.add(clearbtn, 1);

		/**
		 * We get the property change event from the KeySequenceInputPanel when
		 * the user hits a keystroke. We then check for collisions.
		 */
		m_inputpanel.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (KeySequenceInputPanel.PROP_KEYSEQUENCE != evt.getPropertyName())
					return;

				KeyStroke[] seq = m_inputpanel.getKeySequence();
				String warn = getCollisionString(seq, true);
				m_inputpanel.setInfoText(warn == null ? "" : warn);
			}
		});

		/**
		 * so we can set the focus of the input panel
		 */
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent we) {
				m_inputpanel.requestFocus();
			}
		});

		if (binding != null) {
			String txt = "";
			if (binding.keys != null)
				txt = Utilities.keySequenceToString(binding.keys);
			else {
				// convert simple KeyStroke to KeyStroke[1]
				if (binding.key != null) {
					KeyStroke[] sequence = new KeyStroke[1];
					sequence[0] = binding.key;
					txt = Utilities.keySequenceToString(sequence);
				}
			}

			m_inputpanel.setKeyStrokesText(txt);
		}
		setController(new KeySequenceController(this));
	}

	/**
	 * Creates and initializes the controls for this dialog
	 */
	public void initialize(KeyBindingModel model, MultiKeyBinding binding) {
		m_model = model;
		initialize(binding);
	}

	/**
	 * Checks if the given key stroke sequence is assigned to any action. We
	 * allow partial checks to support multi key bindings. For example, a user
	 * may have Ctrl+Q S and Ctrl+Q E bound to different actions. Because,
	 * Ctrl+Q is common, we don't want to show an error message just because the
	 * user selected Ctrl+Q. We can set the bPartial flag to tell this method to
	 * allow partial matches.
	 * 
	 * @param seq
	 *            the key stroke sequence to check
	 * @param bPartial
	 *            set to true if you want to allow partial matches. You
	 *            typically set this when checking every time the user types a
	 *            key stroke. When the user hits ok, then set this parameter to
	 *            false.
	 */
	String getCollisionString(KeyStroke[] seq, boolean bPartial) {

		if (seq.length == 0 || m_model == null) {

			return null;
		}

		int count = m_model.getRowCount();

		for (int index = 0; index < count; index++) {
			// for all actions
			KeyBindingWrapper wrapper = m_model.getRow(index);
			MultiKeyBinding binding = wrapper.getBinding();
			if (binding == m_binding)
				continue;

			KeyStroke[] s1 = binding.keys;

			// System.out.println( "got binding  index = " + index );
			// KeyUtils.print( binding );

			if (s1 == null) {
				if (binding.key == null) {
					continue;
				}

				s1 = new KeyStroke[1];
				s1[0] = binding.key;
			}

			// s1 is the binding from the model
			// seq is what was typed by the user
			if (equals(s1, seq)) {
				if ((bPartial && seq.length >= s1.length) || !bPartial) {
					Object[] args = new Object[2];
					args[0] = Utilities.keySequenceToString(s1);
					args[1] = binding.actionName;
					return I18N.format("Collision_for_sequence_2", args);
				}
			}
		}
		return null; // no colliding sequence
	}

	/**
	 * Tests two key stroke sequences for equality
	 */
	private boolean equals(KeyStroke[] s1, KeyStroke[] s2) {
		if (s1 == null || s2 == null) {
			return false;
		}

		int l = Math.min(s1.length, s2.length);
		while (l-- > 0) {
			if (s1[l] == null || s2[l] == null || !s1[l].equals(s2[l]))
				return false;
		}
		return true;
	}

	/**
	 * The controller for this dialog
	 */
	public class KeySequenceController extends TSController {
		public KeySequenceController(JETAContainer view) {
			super(view);
			assignAction(KeySequenceDialog.ID_CLEAR, new ClearAction());
		}

		/**
		 * Clears the key binding
		 */
		public class ClearAction implements ActionListener {
			public void actionPerformed(ActionEvent evt) {
				m_inputpanel.clear(); // clear entered KeyStrokes, start again
				m_inputpanel.requestFocus();
			}
		}

	}

}
