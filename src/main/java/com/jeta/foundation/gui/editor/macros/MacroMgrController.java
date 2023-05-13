package com.jeta.foundation.gui.editor.macros;

import java.util.Iterator;
import java.util.Collection;

import java.awt.Dimension;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.text.Document;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.xml.XMLUtils;

/**
 * This is the controller for the MacroMgrPanel
 * 
 * @author Jeff Tassin
 */
public class MacroMgrController extends TSController {
	private MacroMgrPanel m_view;
	private MacroMgrModel m_model;

	private static final String MACROS_DIR = "macros";

	public MacroMgrController(MacroMgrPanel view) {
		super(view);
		m_view = view;
		m_model = m_view.getGuiModel();

		assignAction(MacroMgrPanel.ID_EDIT, new EditMacroAction());
		assignAction(MacroMgrPanel.ID_REMOVE, new RemoveMacroAction());
		assignAction(MacroMgrPanel.ID_NEW, new NewMacroAction());

		// listen for table selection events. we need to update the controls
		// when the user selects a different key binding on the table
		JTable table = m_view.getTable();
		ListSelectionModel lmodel = table.getSelectionModel();
		lmodel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {

			}
		});
	}

	/**
	 * Invokes the MacroPanel dialog to allow the user to edit the given macro.
	 * This method allows the caller to create a new macro as well (by setting
	 * the param to null )
	 * 
	 * @param macro
	 *            the macro to edit. If this value is null, we assume that we
	 *            are creating a new macro
	 */
	private void editMacro(Macro macro) {
		final TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
		MacroPanel view = new MacroPanel(m_model.getKitSet(), macro);
		final MacroController controller = new MacroController(view, m_model);
		view.setController(controller);
		dlg.setPrimaryPanel(view);
		dlg.setSize(dlg.getPreferredSize());
		dlg.setTitle(I18N.getLocalizedMessage("Macro Builder"));
		if (macro == null) {
			dlg.setInitialFocusComponent(view.getNameField());
		} else {
			dlg.setInitialFocusComponent(view.getEditorPane());
		}

		dlg.addValidator(controller);
		dlg.showCenter();
		if (dlg.isOk()) {
			Macro newmacro = view.getMacro();
			if (macro == null) // then it is a new macro
			{
				m_model.addMacro(newmacro);
				m_view.repaint();
			} else {
				int index = m_view.getSelectedTableRow();
				int modelrow = m_view.convertTableToModelIndex(index);
				// changing the macro may have resulted in changing the kitclass
				// that it belongs
				// to. For example, the user may have added actions that are
				// defined in a different kit
				// class than the original actions.
				m_model.setMacro(index, newmacro);
				m_view.repaint();
			}
		}
	}

	/**
	 * Edits the selected macro. Allows the user to change the command or macro
	 * name
	 */
	public class EditMacroAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Macro selectedmacro = m_view.getSelectedItem();
			if (selectedmacro == null)
				return;
			editMacro(selectedmacro);
		}
	}

	/**
	 * Creates a new macro.
	 */
	public class NewMacroAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			editMacro(null);
		}
	}

	/**
	 * Removes the selected macro. This option is only available if the macro is
	 * not a default macro.
	 */
	public class RemoveMacroAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			// this probably does not work when table is sorted
			int index = m_view.getSelectedTableRow();
			index = m_view.convertTableToModelIndex(index);
			if (index >= 0) {
				m_model.removeRow(index);
				m_view.repaint();
			}
		}
	}

}
