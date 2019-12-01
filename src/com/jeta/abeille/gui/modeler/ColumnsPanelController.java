package com.jeta.abeille.gui.modeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.open.gui.framework.UIDirector;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * This is the controller for the ColumnsPanel GUI. It handles all button/gui
 * events.
 * 
 * @author Jeff Tassin
 */
public class ColumnsPanelController extends TSController {
	/** this is the panel that this controller controls */
	private ColumnsPanel m_view;
	private ColumnPanelValidator m_colvalidator;

	/**
	 * Constructor
	 */
	public ColumnsPanelController(ColumnsPanel panel) {
		super(panel);
		m_view = panel;

		panel.setUIDirector(new ColumnsViewDirector());

		assignAction(ColumnsPanel.ID_ADD_COLUMN, new AddColumnAction());
		assignAction(ColumnsPanel.ID_EDIT_COLUMN, new EditColumnAction());
		assignAction(ColumnsPanel.ID_REMOVE_COLUMN, new RemoveColumnAction());
		assignAction(ColumnsPanel.ID_MOVE_UP, new MoveUpAction());
		assignAction(ColumnsPanel.ID_MOVE_DOWN, new MoveDownAction());

		// goes in the controller
		JTable table = (JTable) m_view.getComponentByName(ColumnsPanel.ID_COLUMNS_TABLE);
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					editSelectedItem();
				}
			}
		});

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false), ColumnsPanel.ID_REMOVE_COLUMN);
		table.getActionMap().put(ColumnsPanel.ID_REMOVE_COLUMN,
				new TSController.DelegateAction(ColumnsPanel.ID_REMOVE_COLUMN));

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "enter.action");
		table.getActionMap().put("enter.action", new EnterAction());

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, false), "tab.action");
		table.getActionMap().put("tab.action", new TabAction());

		// table.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_TAB,
		// java.awt.event.InputEvent.CTRL_MASK, false ), "ctrl.tab.action" );
		// table.getActionMap().put( "ctrl.tab.action", new CtrlTabAction() );
	}

	/**
	 * Edits the selected column data
	 */
	public void editSelectedItem() {
		ColumnInfo info = m_view.getSelectedItem();
		if (info != null) {
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
			dlg.setTitle(I18N.getLocalizedMessage("Modify Column"));
			ColumnsGuiModel model = m_view.getModel();
			ModelerFactory factory = ModelerFactory.getFactory(model.getConnection());
			ColumnPanel panel = factory.createColumnPanel(model.getConnection(), model.getTableId(), info, true);
			dlg.setPrimaryPanel(panel);
			dlg.setSize(dlg.getPreferredSize());

			dlg.addValidator(panel);
			dlg.addValidator(getColumnValidator(panel, false));
			dlg.showCenter();
			if (dlg.isOk()) {
				info = panel.getColumnInfo();
				int row = getColumnsTable().getSelectedRow();
				getModel().removeRow(row);
				getModel().insertRow(info, row);
				m_view.notifyModelChanged();
			}
		}
	}

	private ColumnPanelValidator getColumnValidator(ColumnPanel panel, boolean isNew) {
		if (m_colvalidator == null)
			m_colvalidator = new ColumnPanelValidator();

		m_colvalidator.setPanel(panel, isNew);
		return m_colvalidator;
	}

	/**
	 * Inserts a new column info object in the ColumnsGuiModel and starts
	 * editing the name cell
	 */
	private void insertNewColumn() {
		JTable table = getColumnsTable();
		ColumnsGuiModel model = getModel();
		model.addRow(new ColumnInfo("", java.sql.Types.VARCHAR, "VARCHAR", 0, 0, false, false));
		model.fireTableDataChanged();

		int row = model.getRowCount() - 1;
		if (row >= 0) {
			table.setRowSelectionInterval(row, row);
			table.setColumnSelectionInterval(ColumnsGuiModel.NAME_COLUMN, ColumnsGuiModel.NAME_COLUMN);
			TableUtils.ensureRowIsVisible(table, row);
			table.editCellAt(row, ColumnsGuiModel.NAME_COLUMN);
		}
	}

	/**
	 * Brings up the new column dialog box and allows the user to add a column
	 * to this table
	 */
	public class AddColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Runnable gui_update = new Runnable() {
				public void run() {
					JTable table = getColumnsTable();
					m_view.stopCellEditing();
					table.requestFocus();
					insertNewColumn();
				}
			};

			javax.swing.SwingUtilities.invokeLater(gui_update);
			/*
			 * TSDialog dlg = (TSDialog)TSGuiToolbox.createDialog(
			 * TSDialog.class, true ); dlg.setTitle( I18N.getLocalizedMessage(
			 * "Add Column" ) );
			 * 
			 * ColumnsGuiModel model = m_view.getModel(); ModelerFactory factory
			 * = ModelerFactory.getFactory( model.getConnection() ); ColumnPanel
			 * panel = factory.createColumnPanel( model.getConnection(),
			 * model.getTableId(), null, true ); dlg.setPrimaryPanel( panel );
			 * dlg.addValidator( panel ); dlg.addValidator( getColumnValidator(
			 * panel, true ) );
			 * 
			 * dlg.setSize( dlg.getPreferredSize() ); dlg.showCenter(); if (
			 * dlg.isOk() ) { getModel().addRow( panel.getColumnInfo() );
			 * m_view.notifyModelChanged(); }
			 */
		}
	}

	/**
	 * @return the JTable component that contains the database columns in the
	 *         columns panel
	 */
	public JTable getColumnsTable() {
		return m_view.getTable();
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return getModel().getConnection();
	}

	/**
	 * @return the underlying database model
	 */
	public ColumnsGuiModel getModel() {
		return m_view.getModel();
	}

	/**
	 * Toggles the nullable flag for a given column metadata object. Assumes the
	 * object is part of the column model
	 */
	public void toggleNullable(ColumnInfo info) {
		info.setNullable(!info.isNullable());
		getModel().fireTableDataChanged();
	}

	/**
	 * Responsible for enabling/disabling components on the ColumnsView based on
	 * the state of the model
	 */
	public class ColumnsViewDirector implements UIDirector {
		public void updateComponents(java.util.EventObject evt) {
			ColumnInfo info = m_view.getSelectedItem();
			int rowcount = getModel().getRowCount();

			if (info == null) {
				m_view.enableComponent(ColumnsPanel.ID_EDIT_COLUMN, false);
				m_view.enableComponent(ColumnsPanel.ID_REMOVE_COLUMN, false);
				m_view.enableComponent(ColumnsPanel.ID_MOVE_UP, false);
				m_view.enableComponent(ColumnsPanel.ID_MOVE_DOWN, false);
			} else {

				m_view.enableComponent(ColumnsPanel.ID_EDIT_COLUMN, true);
				m_view.enableComponent(ColumnsPanel.ID_REMOVE_COLUMN, true);

				if (rowcount < 2) {
					m_view.enableComponent(ColumnsPanel.ID_MOVE_UP, false);
					m_view.enableComponent(ColumnsPanel.ID_MOVE_DOWN, false);
				} else {
					m_view.enableComponent(ColumnsPanel.ID_MOVE_UP, true);
					m_view.enableComponent(ColumnsPanel.ID_MOVE_DOWN, true);
				}
			}
		}
	}

	/**
	 * Validator that makes sure the column name entered by the user does not
	 * conflict with another column.
	 */
	public class ColumnPanelValidator implements JETARule {
		private java.lang.ref.WeakReference m_panelref;
		private boolean m_isnew;

		public void setPanel(ColumnPanel panel, boolean isNew) {
			m_panelref = new java.lang.ref.WeakReference(panel);
			m_isnew = isNew;
		}

		public RuleResult check(Object[] params) {
			if (m_panelref == null)
				return RuleResult.SUCCESS;

			TSDatabase dbimpl = (TSDatabase) getConnection().getImplementation(TSDatabase.COMPONENT_ID);
			ColumnPanel panel = (ColumnPanel) m_panelref.get();

			if (panel != null) {
				// check if the column data type requires size and/or precision

				String typename = panel.getTypeName();
				DataTypeInfo dtype = dbimpl.getDataTypeInfo(typename);

				if (dtype != null) {
					if (dtype.isPrecisionRequired()) {
						if (panel.getColumnSize() == 0) {
							return new RuleResult(I18N.getLocalizedMessage("Size is required for this data type"));
						}
					}

					if (dtype.isScaleRequired()) {
						if (panel.getScale() == 0) {
							return new RuleResult(I18N.getLocalizedMessage("Scale is required for this data type"));
						}
					}
				}

				// check if the column name is unique
				int name_count = 0;
				String colname = panel.getColumnName();
				Collection cols = m_view.getColumns();
				Iterator iter = cols.iterator();
				while (iter.hasNext()) {
					ColumnMetaData cmd = (ColumnMetaData) iter.next();

					if (I18N.equalsIgnoreCase(colname, cmd.getName()))
						name_count++;
				}

				if (name_count > 1 || (m_isnew && name_count == 1)) {
					return new RuleResult(I18N.getLocalizedMessage("Column name is already assigned"));
				}
			}

			return RuleResult.SUCCESS;
		}
	}

	/**
	 * Edits the selected item
	 */
	public class EditColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			editSelectedItem();
		}
	}

	/**
	 * When the user presses the enter key, we move the selected row to the next
	 * row. If the currently selected row is the last row, we insert a new
	 * column
	 */
	public class EnterAction extends javax.swing.AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			m_view.stopCellEditing();
			JTable table = getColumnsTable();
			int row = table.getSelectedRow();
			if (row >= 0) {
				if (row == table.getRowCount() - 1) {
					insertNewColumn();
				} else {
					row++;
					table.setRowSelectionInterval(row, row);
					table.setColumnSelectionInterval(ColumnsGuiModel.NAME_COLUMN, ColumnsGuiModel.NAME_COLUMN);
				}
			}
		}
	}

	/**
	 * Moves the selected column up in the column order
	 */
	public class MoveUpAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTable table = getColumnsTable();
			int row = table.getSelectedRow();
			if (row >= 1) {
				ColumnInfo info = (ColumnInfo) getModel().removeRow(row);
				row--;
				getModel().insertRow(info, row);
				getModel().fireTableDataChanged();
				table.setRowSelectionInterval(row, row);
				table.setColumnSelectionInterval(0, ColumnsGuiModel.NULLABLE_COLUMN);
				TableUtils.ensureRowIsVisible(table, row);
				table.repaint();
			}
		}
	}

	/**
	 * Moves the selected column down in the column order
	 */
	public class MoveDownAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTable table = getColumnsTable();
			int row = table.getSelectedRow();
			if (row < (getModel().getRowCount() - 1)) {
				ColumnInfo info = (ColumnInfo) getModel().removeRow(row);
				row++;
				getModel().insertRow(info, row);
				getModel().fireTableDataChanged();
				table.setRowSelectionInterval(row, row);
				table.setColumnSelectionInterval(0, ColumnsGuiModel.NULLABLE_COLUMN);
				TableUtils.ensureRowIsVisible(table, row);
				table.repaint();
			}
		}

	}

	/**
	 * Removes the selected column from the model
	 */
	public class RemoveColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			/** list of ColumnInfo objects to remove */
			LinkedList removeobjs = new LinkedList();

			ColumnsGuiModel model = getModel();
			int[] rows = getColumnsTable().getSelectedRows();
			for (int index = 0; index < rows.length; index++) {
				ColumnInfo info = (ColumnInfo) model.getRow(rows[index]);
				if (info != null) {
					removeobjs.add(info);
				}
			}

			Iterator iter = removeobjs.iterator();
			while (iter.hasNext()) {
				ColumnInfo info = (ColumnInfo) iter.next();
				model.remove(info);
			}

			if (rows.length > 0) {
				int newrow = rows[0] - 1;
				newrow++;
				if (newrow < 0)
					newrow = 0;

				if (getColumnsTable().getRowCount() > 0) {
					if (newrow >= getColumnsTable().getRowCount())
						newrow = getColumnsTable().getRowCount() - 1;

					ListSelectionModel selectionModel = getColumnsTable().getSelectionModel();
					selectionModel.setSelectionInterval(newrow, newrow);
				}
				getColumnsTable().repaint();
			}
		}
	}

	/**
	 * When the user presses the tab key, we move the selected column to the
	 * next column. If the currently selected row is the last row, we insert a
	 * new column
	 */
	public class TabAction extends javax.swing.AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			m_view.stopCellEditing();
			JTable table = getColumnsTable();
			int row = table.getSelectedRow();
			int col = table.getSelectedColumn();
			ColumnsGuiModel model = getModel();
			if (row >= 0 && col >= 0) {
				if (col == (model.getColumnCount() - 1)) {
					if (row == table.getRowCount() - 1) {
						insertNewColumn();
					} else {
						row++;
						table.setRowSelectionInterval(row, row);
						table.setColumnSelectionInterval(ColumnsGuiModel.NAME_COLUMN, ColumnsGuiModel.NAME_COLUMN);
					}
				} else {
					col++;
					table.setRowSelectionInterval(row, row);
					table.setColumnSelectionInterval(col, col);
				}
			}
		}
	}

	/**
	 * When the user presses the ctrl-tab key, we move focus to the ok button
	 */
	public class CtrlTabAction extends javax.swing.AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			System.out.println("ColumnsPanelController.ctrl-tab event");
		}
	}

}
