package com.jeta.abeille.gui.export;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.JRadioButton;
import javax.swing.JTable;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;

/**
 * This is the controller for the ExportPanel
 * 
 * @author Jeff Tassin
 */
public class ExportPanelController extends TSController {
	private ExportPanel m_view; // the panel window we are controlling
	private SQLExportModel m_model;

	public ExportPanelController(ExportPanel view) {
		super(view);
		m_view = view;
		m_model = view.getModel();
		assignAction(ExportNames.ID_MOVE_UP, new MoveUpAction());
		assignAction(ExportNames.ID_MOVE_DOWN, new MoveDownAction());
		assignAction(ExportNames.ID_SELECT_FILE, new SelectFileAction());

		ActionListener export_target_listener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JRadioButton rbtn = (JRadioButton) m_view.getComponentByName(ExportNames.ID_TARGET_FILE);
				if (rbtn.isSelected()) {
					m_view.enableComponent(ExportNames.ID_FILENAME_FIELD, true);
					m_view.enableComponent(ExportNames.ID_SELECT_FILE, true);
				} else {
					m_view.enableComponent(ExportNames.ID_FILENAME_FIELD, false);
					m_view.enableComponent(ExportNames.ID_SELECT_FILE, false);
				}
			}
		};

		JRadioButton rbtn = (JRadioButton) view.getComponentByName(ExportNames.ID_TARGET_CLIPBOARD);
		rbtn.addActionListener(export_target_listener);

		rbtn = (JRadioButton) view.getComponentByName(ExportNames.ID_TARGET_FILE);
		rbtn.addActionListener(export_target_listener);

		// rbtn = (JRadioButton)view.getComponentByName(
		// ExportNames.ID_TARGET_DATABASE_TABLE );
		// rbtn.addActionListener( export_target_listener );

	}

	/**
	 * Moves the selected column down one row
	 */
	public class MoveDownAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ColumnOptionsModel cmodel = m_model.getColumnOptionsModel();
			int row = m_view.getSelectedRow();
			if (row < (cmodel.getRowCount() - 1)) {
				int newrow = row + 1;
				cmodel.reorder(newrow, row);
				m_view.selectRow(newrow);
			}
		}
	}

	/**
	 * Moves the selected column down one row
	 */
	public class MoveUpAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ColumnOptionsModel cmodel = m_model.getColumnOptionsModel();
			int row = m_view.getSelectedRow();
			if (row > 0) {
				int newrow = row - 1;
				cmodel.reorder(newrow, row);
				m_view.selectRow(newrow);
			}
		}
	}

	/**
	 * Opens a file chooser dialog that allows the user to select an output file
	 * for the export
	 */
	public class SelectFileAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			File f = TSFileChooserFactory.showOpenDialog();
			if (f != null) {
				m_view.setOutputFile(f.getPath());
			}
		}
	}
}
