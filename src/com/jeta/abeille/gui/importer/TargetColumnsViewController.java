package com.jeta.abeille.gui.importer;

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.table.TSTablePanel;

/**
 * The controller class for the TargetColumnsView
 * 
 * @author Jeff Tassin
 */
public class TargetColumnsViewController extends TSController {

	private TargetColumnsView m_view;

	private TargetColumnsModel m_model;

	private TargetDropListener m_droplistener;
	private DropTarget m_droptarget;
	private DropTarget m_scrolldroptarget;

	/*
	 * ctor
	 */
	public TargetColumnsViewController(TargetColumnsView view) {
		super(view);
		m_view = view;
		m_model = view.getModel();

		// drag and drop for table
		TSTablePanel tpanel = m_view.getTablePanel();
		JTable table = tpanel.getTable();

		m_droplistener = new TargetDropListener(m_view);
		m_droptarget = new DropTarget(table, m_droplistener);

		JScrollPane scroll = tpanel.getScrollPane(table);
		m_scrolldroptarget = new DropTarget(scroll, new TargetDropListener(m_view));

		DeleteAction deleteaction = new DeleteAction();
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false),
				ImportBuilderNames.ID_REMOVE_TARGET_COLUMN);
		table.getActionMap().put(ImportBuilderNames.ID_REMOVE_TARGET_COLUMN, deleteaction);
	}

	/**
	 * When use hits delete key on targets table
	 */
	public class DeleteAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			JTable table = m_view.getTable();
			int[] cols = table.getSelectedColumns();
			if (cols.length == 1 && cols[0] == TargetColumnsModel.VALUE_COLUMN) {
				int[] rows = table.getSelectedRows();
				m_model.removeSource(rows);
			} else {
				int[] rows = table.getSelectedRows();
				m_model.remove(rows);
			}
		}
	}

}
