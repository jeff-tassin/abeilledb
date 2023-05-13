package com.jeta.foundation.gui.editor.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;

import com.jeta.foundation.gui.editor.KitKeyBindingModel;
import com.jeta.foundation.gui.editor.macros.MacroMgrDialog;
import com.jeta.foundation.gui.editor.macros.MacroMgrModel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * Controller for the EditorOptionsView
 */
public class EditorOptionsController extends TSController {
	/** the view we are controlling */
	private EditorOptionsView m_view;

	/** the model for the view */
	private EditorOptionsModel m_model;

	/**
	 * ctor
	 */
	public EditorOptionsController(EditorOptionsView view) {
		super(view);
		m_view = view;
		m_model = m_view.getModel();

		assignAction(EditorOptionsView.ID_EDIT_BINDINGS, new EditBindingsAction());
		assignAction(EditorOptionsView.ID_CREATE_BINDINGS, new CreateBindingsAction());
		assignAction(EditorOptionsView.ID_EDIT_MACROS, new EditMacrosAction());
		assignAction(EditorOptionsView.ID_DELETE_BINDINGS, new DeleteBindingsAction());

	}

	/**
	 * Creates a new key bindings based on the currently selected one
	 */
	public class CreateBindingsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String editor = m_view.getSelectedEditor();
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);

			KeyBindingModel model = new KeyBindingModel(editor, m_model.getKitSet());
			model.setNew(true);
			final KeyBindingView view = new KeyBindingView(model);
			dlg.setPrimaryPanel(view);
			KeyBindingController controller = new KeyBindingController(view);
			view.setController(controller);
			dlg.addValidator(controller);
			dlg.addValidator(new JETARule() {
				public RuleResult check(Object[] params) {
					RuleResult result = RuleResult.SUCCESS;
					String name = view.getName();
					if (name != null) {
						name = name.trim();
						Collection current_names = m_model.getBindingNames();
						Iterator iter = current_names.iterator();
						while (iter.hasNext()) {
							String binding_name = (String) iter.next();
							if (name.equalsIgnoreCase(binding_name)) {
								result = new RuleResult(I18N.getLocalizedMessage("Please provide a unique name"));
								break;
							}
						}
					}
					return result;
				}
			});

			dlg.setTitle(I18N.getLocalizedMessage("New Key Bindings"));

			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				try {
					view.saveToModel();

					model.setNew(false);
					m_view.addKeyBindings(model);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Deletes the selected key bindings. The marks the bindings for deletion.
	 * The actual deletion occurs when the EditorOptionsView is closed.
	 */
	public class DeleteBindingsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String editor = m_view.getSelectedEditor();
			if (editor != null && !editor.equals(KitKeyBindingModel.DEFAULT_BINDING)) {
				String title = I18N.getLocalizedMessage("Confirm");
				String msg = I18N.format("Delete_1", editor);
				int result = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					m_view.deleteBindings(editor);
				}
			}
		}
	}

	/**
	 * Edits the current key bindings
	 */
	public class EditBindingsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String editor = m_view.getSelectedEditor();
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);

			KeyBindingModel model = (KeyBindingModel) m_model.getKeyBindingModel(editor).clone();
			KeyBindingView view = new KeyBindingView(model);
			dlg.setPrimaryPanel(view);
			KeyBindingController controller = new KeyBindingController(view);
			view.setController(controller);

			dlg.setSize(dlg.getPreferredSize());
			dlg.setTitle(I18N.getLocalizedMessage("Edit Key Bindings"));
			dlg.showCenter();
			if (dlg.isOk()) {
				try {
					m_model.setKitKeyBindingModel(editor, view.getModel());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Invokes the edit macros dialog
	 */
	public class EditMacrosAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			MacroMgrDialog dlg = (MacroMgrDialog) TSGuiToolbox.createDialog(MacroMgrDialog.class, m_view, true);
			dlg.initialize(m_model.getMacroModel());
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
		}
	}

}
