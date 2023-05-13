package com.jeta.abeille.gui.update;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;

/**
 * This is the controller for the InstanceOptionsView GUI. It handles all
 * button/gui events.
 * 
 * @author Jeff Tassin
 */
public class InstanceOptionsViewController extends TSController {
	private InstanceOptionsView m_view; // this is the panel that this
										// controller controls

	/**
	 * Constructor
	 */
	public InstanceOptionsViewController(InstanceOptionsView panel) {
		super(panel);
		m_view = panel;
		assignAction(InstanceOptionsView.ID_EDIT_COLUMN, new EditAction());
		assignAction(InstanceOptionsView.ID_MOVE_UP, new MoveUpAction());
		assignAction(InstanceOptionsView.ID_MOVE_DOWN, new MoveDownAction());
		assignAction(InstanceOptionsView.ID_RESET_DEFAULTS, new ResetAction());

		JTable table = m_view.getTable();
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					JTable table = m_view.getTable();
					int row = table.rowAtPoint(evt.getPoint());
					if (row >= 0) {
						ColumnSettings info = m_view.getItem(row);
						editColumnSettings(row, info);
					}
				}
			}
		});

		InstanceOptionsUIDirector uidirector = new InstanceOptionsUIDirector(m_view);
		m_view.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Invokes the editor dialog that allows the user to customize the settings
	 * for the selected column.
	 * 
	 * @param index
	 *            the index that the column settings info is located
	 */
	protected void editColumnSettings(int index, ColumnSettings info) {

		if (info != null) {
			InstanceOptionsModel guimodel = m_view.getGuiModel();
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
			dlg.setTitle(I18N.getLocalizedMessage("Column Settings"));
			int modelindex = m_view.convertTableToModelIndex(index);
			final ColumnSettingsPanel contentpanel = new ColumnSettingsPanel(info, modelindex + 1,
					guimodel.getRowCount());
			dlg.addValidator((JETARule) contentpanel.getController());
			dlg.setPrimaryPanel(contentpanel);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				ColumnSettings newinfo = contentpanel.getColumnSettings();

				/*
				 * we allow only one column to have auto height, so lets disable
				 * any other columns that have auto height set
				 */
				if (newinfo.isAutoHeight()) {
					for (int row = 0; row < guimodel.getRowCount(); row++) {
						ColumnSettings setting = guimodel.getRow(row);
						setting.setAutoHeight(false);
					}
				}

				int order = contentpanel.getOrder();
				guimodel.reorder(order - 1, modelindex);
				guimodel.setRow(order - 1, newinfo);

				m_view.getTable().repaint();
				guimodel.save();
			}
		}
	}

	/**
	 * Deletes the selected column setting object from the model
	 */
	public class DeleteAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			LinkedList settings = new LinkedList();
			int[] rows = m_view.getSelectedRows();

			for (int index = 0; index < rows.length; index++) {
				ColumnSettings info = m_view.getItem(rows[index]);
				if (info != null)
					settings.add(info);
			}

			InstanceOptionsModel model = m_view.getGuiModel();
			Iterator iter = settings.iterator();
			while (iter.hasNext()) {
				ColumnSettings setting = (ColumnSettings) iter.next();
				model.remove(setting);
			}

			JComponent comp = (JComponent) m_view.getComponentByName(InstanceOptionsView.ID_TABLE_SCROLL);
			comp.repaint();
		}
	}

	/**
	 * Invokes a dialog that allows the user to edit the selected column setting
	 */
	public class EditAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			int index = m_view.getSelectedRow();
			ColumnSettings info = m_view.getItem(index);
			editColumnSettings(index, info);
		}
	}

	/**
	 * Moves the selected column up a row
	 */
	public class MoveUpAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			int index = m_view.getSelectedRow();
			if (index > 0) {
				ColumnSettings info = m_view.getItem(index);
				InstanceOptionsModel model = m_view.getGuiModel();
				model.reorder(index - 1, index);
				index--;
				JTable table = (JTable) m_view.getComponentByName(InstanceOptionsView.ID_COLUMNS_TABLE);
				ListSelectionModel selectionModel = table.getSelectionModel();
				selectionModel.setSelectionInterval(index, index);
				table.repaint();
			}
		}
	}

	/**
	 * Moves the selected column down a row
	 */
	public class MoveDownAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			InstanceOptionsModel model = m_view.getGuiModel();
			int index = m_view.getSelectedRow();
			if (index >= 0 && index < (model.getRowCount() - 1)) {
				ColumnSettings info = m_view.getItem(index);
				model.reorder(index + 1, index);
				index++;
				JTable table = (JTable) m_view.getComponentByName(InstanceOptionsView.ID_COLUMNS_TABLE);
				ListSelectionModel selectionModel = table.getSelectionModel();
				selectionModel.setSelectionInterval(index, index);
				table.repaint();
			}
		}
	}

	/**
	 * Resets the options to the default settings
	 */
	public class ResetAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			InstanceOptionsModel model = m_view.getGuiModel();
			model.reset();
		}
	}

}
