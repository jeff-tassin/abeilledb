package com.jeta.foundation.gui.editor.options;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.components.*;

import com.jeta.foundation.gui.editor.KeyBindingMgr;
import com.jeta.foundation.gui.editor.KeyUtils;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * This is the controller for the KeyBindingView
 * 
 * @author Jeff Tassin
 */
public class KeyBindingController extends TSController implements JETARule {
	/** the view we are controlling */
	private KeyBindingView m_view;

	/** the data model for the view */
	private KeyBindingModel m_model;

	public KeyBindingController(KeyBindingView view) {
		super(view);
		m_view = view;
		m_model = m_view.getModel();

		assignAction(KeyBindingView.ID_EDIT, new EditBindingAction());
		assignAction(KeyBindingView.ID_REMOVE, new RemoveBindingAction());
		assignAction(KeyBindingView.ID_NEW, new CreateBindingAction());
		assignAction(KeyBindingView.ID_CLEAR, new ClearBindingAction());
		assignAction(KeyBindingView.ID_RESET_DEFAULTS, new ResetDefaultsAction());

		// listen for table selection events. we need to update the controls
		// when the user selects a different key binding on the table
		JTable table = m_view.getTable();
		ListSelectionModel lmodel = table.getSelectionModel();
		lmodel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {

			}
		});

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					invokeAction(KeyBindingView.ID_EDIT);
				}
			}
		});

	}

	/**
	 * Provide validation for the inputs to the key binding panel
	 * 
	 * @return an error message if the validation failed. Return null if the
	 *         validation succeeded
	 */
	public RuleResult check(Object[] params) {
		RuleResult result = RuleResult.SUCCESS;
		String desc = m_view.getName();
		if (desc == null || desc.length() == 0) {
			result = new RuleResult(I18N.getLocalizedMessage("Please provide name"));
		} else {
			for (int index = 0; index < desc.length(); index++) {
				char c = desc.charAt(index);
				if (!Character.isLetterOrDigit(c) && c != ' ') {
					result = new RuleResult(I18N.getLocalizedMessage("Invalid_name_only_alphanumeric_characters"));
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Clears the selected binding. Allows the user to clear the keystrokes that
	 * are bound to the selected action.
	 */
	public class ClearBindingAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			KeyBindingWrapper wrapper = m_view.getSelectedItem();
			if (wrapper != null) {
				MultiKeyBinding selectedbinding = wrapper.getBinding();
				if (selectedbinding == null)
					return;

				KeyUtils.changeBinding(selectedbinding, null);
				m_view.repaint();
			}
		}
	}

	/**
	 * Creates a new binding. This allows the user to select an action and input
	 * a new binding for that action.
	 */
	public class CreateBindingAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			KeyBindingWrapper wrapper = m_view.getSelectedItem();
			if (wrapper != null) {
				String actionname = wrapper.getActionName();
				MultiKeyBinding binding = new MultiKeyBinding((KeyStroke) null, actionname);
				m_model.addRow(wrapper.getKitClass(), binding);

				int lastrow = m_model.getRowCount() - 1;
				JTable table = m_view.getTable();
				Rectangle rect = table.getCellRect(lastrow, 0, true);
				table.scrollRectToVisible(rect);
				m_view.selectItem(lastrow);

				JComponent comp = (JComponent) m_view.getComponentByName(KeyBindingView.ID_EDIT);
				if (comp != null)
					comp.requestFocus();
			}
		}
	}

	/**
	 * Edits the selected binding. Allows the user to change the keystrokes that
	 * are bound to the selected action.
	 */
	public class EditBindingAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			KeyBindingWrapper wrapper = m_view.getSelectedItem();
			if (wrapper != null) {
				MultiKeyBinding selectedbinding = wrapper.getBinding();
				if (selectedbinding == null)
					return;

				KeySequenceDialog dlg = (KeySequenceDialog) TSGuiToolbox.createDialog(KeySequenceDialog.class, m_view,
						true);
				dlg.initialize(m_model, selectedbinding);
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
				if (dlg.isOk()) {
					KeyStroke[] seq = dlg.getKeySequence();
					KeyUtils.changeBinding(selectedbinding, seq);
					// KeyUtils.print( selectedbinding );
					m_view.repaint();
				}
			}
		}
	}

	/**
	 * Removes the selected action/keystroke binding. This option is only
	 * available if there is more than one binding defined for an action.
	 */
	public class RemoveBindingAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			// this probably does not work when table is sorted
			int index = m_view.getSelectedTableRow();
			index = m_view.convertTableToModelIndex(index);
			if (index >= 0) {
				KeyBindingWrapper wrapper = m_model.getRow(index);
				MultiKeyBinding binding = wrapper.getBinding();
				String actionname = binding.actionName;
				// prevent the user from deleting the last action (there can be
				// multiple bindings for the same action name )
				MultiKeyBinding[] bindings = m_model.getBindings(actionname);
				if (bindings.length > 1) {
					m_model.removeRow(index);
					m_view.repaint();
				} else {
					java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
					toolkit.beep();
				}
			}
		}
	}

	/**
	 * Resets the key bindings to the default settings
	 */
	public class ResetDefaultsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_model = new KeyBindingModel(m_model.getKitSet());
			m_view.setModel(m_model);
		}
	}

}
