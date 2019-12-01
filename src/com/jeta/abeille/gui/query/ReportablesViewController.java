package com.jeta.abeille.gui.query;

import java.awt.Toolkit;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.dnd.DropTarget;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.ColumnSelectorPanel;
import com.jeta.abeille.gui.common.DefaultTableSelectorModel;
import com.jeta.abeille.gui.common.TableSelectorPanel;
import com.jeta.abeille.gui.model.DbObjectTransfer;
import com.jeta.abeille.gui.model.MultiTransferable;

import com.jeta.abeille.query.Reportable;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the controller for the query builder window. It is specifically used
 * to handle events for adding/editing/removing query reportables. It also is
 * responsible for enabling/disabling menu/toolbar components for reportables.
 * 
 * @author Jeff Tassin
 */
public class ReportablesViewController extends TSController {

	/** the view that we are controlling */
	private ReportablesView m_view;

	/** the model for this view */
	private ReportablesModel m_model;

	/** the database connection */
	private TSConnection m_connection;

	/** The drop target for the view */
	private DropTarget m_droptarget;

	/** needed because if an object is dragged over the empty part of table */
	private DropTarget m_scrolldroptarget;

	private ReportableDropListener m_droplistener;

	/**
	 * ctor
	 */
	public ReportablesViewController(TSConnection connection, ReportablesView view) {
		super(view);
		m_connection = connection;
		m_view = view;
		m_model = m_view.getModel();

		DeleteAction deleteaction = new DeleteAction();
		PasteAction pasteaction = new PasteAction();
		CopyAction copyaction = new CopyAction();
		CutAction cutaction = new CutAction();

		assignAction(QueryNames.ID_ADD_REPORTABLE, new AddReportableAction());
		assignAction(QueryNames.ID_DELETE_ITEM, deleteaction);
		assignAction(QueryNames.ID_MOVE_DOWN, new MoveDownAction());
		assignAction(QueryNames.ID_MOVE_UP, new MoveUpAction());
		assignAction(TSComponentNames.ID_PASTE, pasteaction);
		assignAction(TSComponentNames.ID_COPY, copyaction);
		assignAction(TSComponentNames.ID_CUT, cutaction);

		m_droplistener = new ReportableDropListener(m_view);
		JTable table = (JTable) m_view.getComponentByName(ReportablesView.ID_REPORTABLES_TABLE);
		// drag and drop for table
		m_droptarget = new DropTarget(table, m_droplistener);

		JScrollPane scroll = (JScrollPane) m_view.getComponentByName(ReportablesView.ID_REPORTABLES_SCROLL);
		// for empty part of table
		m_scrolldroptarget = new DropTarget(scroll, m_droplistener);

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false), QueryNames.ID_REMOVE_REPORTABLE);
		table.getActionMap().put(QueryNames.ID_REMOVE_REPORTABLE, deleteaction);

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, false),
				TSComponentNames.ID_PASTE);
		table.getActionMap().put(TSComponentNames.ID_PASTE, pasteaction);

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false),
				TSComponentNames.ID_COPY);
		table.getActionMap().put(TSComponentNames.ID_COPY, copyaction);

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK, false),
				TSComponentNames.ID_CUT);
		table.getActionMap().put(TSComponentNames.ID_CUT, cutaction);

		/**
		 * listen for mouse double-click events so we can edit a selected
		 * reportable
		 */
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					JTable table = m_view.getTable();
					int row = table.rowAtPoint(evt.getPoint());
					if (row >= 0) {
						Reportable reportable = m_model.getRow(row);
						if (reportable != null) {
							reportable = invokeReportableDialog(reportable);
							if (reportable != null) {
								m_model.setValueAt(reportable, row, 0);
							}
						}
					}
				}
			}
		});

	}

	/**
	 * @return the database connection
	 */
	TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * Invokes the column selector that allows the user to choose a column as a
	 * reportable for the query.
	 * 
	 * @param reportable
	 *            the reportable to edit. If this is null, we assume we are
	 *            adding a new reportable
	 * @return the selected column (As a Reportable object) Null is returned if
	 *         the user canceled the dialog
	 */
	Reportable invokeReportableDialog(Reportable reportable) {
		TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, TSWorkspaceFrame.getInstance(), true);
		dlg.setTitle(I18N.getLocalizedMessage("Add Reportable"));

		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel("Output Name");

		JComponent[] comps = new JComponent[1];
		JTextField outputfield = new JTextField();

		if (reportable != null && reportable.getOutputName() != null)
			outputfield.setText(reportable.getOutputName());

		comps[0] = outputfield;

		ColumnSelectorPanel cpanel = new ColumnSelectorPanel(getConnection(), new DefaultTableSelectorModel(
				getConnection()), labels, comps);
		if (reportable != null)
			cpanel.setColumn(reportable.getColumn());

		dlg.setPrimaryPanel(cpanel);
		dlg.setSize(dlg.getPreferredSize());

		dlg.showCenter();
		if (dlg.isOk()) {
			Reportable result = null;
			ColumnMetaData cmd = cpanel.getSelectedColumn();
			TableSelectorPanel tpanel = cpanel.getTableSelectorPanel();

			TableId tableid = tpanel.createTableId(getConnection());
			if (cmd == null) {
				TSComboBox ccombo = cpanel.getColumnsCombo();
				String colname = ccombo.getText();
				if (colname != null)
					colname = colname.trim();

				// let's create a simulated column here to allow the user to
				// type in arbitrary column
				// names in case they aren't picked up in the metadata. The OID
				// in postresql is an example
				// of when this can happen.
				cmd = new ColumnMetaData(colname, java.sql.Types.OTHER, 0, tableid, 0);

			}

			String colname = cmd.getColumnName();
			TableMetaData tmd = getConnection().getTable(tableid);
			if (tmd == null || tmd.getColumn(colname) == null) {
				String msg = "";
				String title = I18N.getLocalizedMessage("Error");
				if (tmd == null) {
					msg = I18N.getLocalizedMessage("Invalid Table Name");
				} else {
					msg = I18N.getLocalizedMessage("Invalid Column Name");
				}

				JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			} else {
				result = new Reportable(cmd);
				result.setOutputName(outputfield.getText());
			}
			return result;
		} else
			return null;
	}

	/**
	 * Adds a reportable to the view/model
	 */
	public class AddReportableAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			Reportable r = invokeReportableDialog(null);
			if (r != null)
				m_model.addReportable(r);
		}
	}

	/**
	 * Copies any selected reportables onto the clipboard
	 */
	public class CopyAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			try {
				MultiTransferable mt = new MultiTransferable();
				int[] rows = m_view.getSelectedRows();
				// because the table is sortable, we have to convert the table
				// row index
				// to the corresponding model row index
				for (int index = 0; index < rows.length; index++) {
					int modelrow = m_view.convertTableToModelIndex(rows[index]);
					Reportable rep = m_model.getRow(modelrow);
					if (rep != null)
						DbObjectTransfer.addTransferable(mt, rep.getColumn());
				}

				if (mt.size() > 0) {
					Toolkit kit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = kit.getSystemClipboard();
					clipboard.setContents(mt, null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Cuts any selected reportables onto the clipboard
	 */
	public class CutAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			try {
				ReportablesViewController.this.invokeAction(TSComponentNames.ID_COPY);
				ReportablesViewController.this.invokeAction(QueryNames.ID_DELETE_ITEM);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * When use hits delete key on constraint table
	 */
	public class DeleteAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			int[] rows = m_view.getSelectedRows();
			m_model.remove(rows);
		}
	}

	/**
	 * Moves the selected column down a row
	 */
	public class MoveDownAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			int row = m_view.getSelectedRow();
			if (row >= 0 && row < (m_model.getRowCount() - 1)) {
				Reportable r = m_model.getRow(row);
				if (r != null) {
					m_model.reorder(row + 1, row);
					row++;
					JTable table = m_view.getTable();
					table.setRowSelectionInterval(row, row);
					table.setColumnSelectionInterval(ReportablesModel.TABLE_NAME_COLUMN,
							ReportablesModel.OUTPUT_NAME_COLUMN);
					table.repaint();
				}
			}
		}
	}

	/**
	 * Moves the selected table/column up a row
	 */
	public class MoveUpAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			int row = m_view.getSelectedRow();
			if (row > 0) {
				Reportable r = m_model.getRow(row);
				if (r != null) {
					m_model.reorder(row - 1, row);
					row--;
					JTable table = m_view.getTable();
					// ListSelectionModel selectionModel =
					// table.getSelectionModel();
					table.setRowSelectionInterval(row, row);
					table.setColumnSelectionInterval(ReportablesModel.TABLE_NAME_COLUMN,
							ReportablesModel.OUTPUT_NAME_COLUMN);
					table.repaint();
				}
			}
		}
	}

	/**
	 * Paste any tables or columns found on the clipboard into the reportables
	 * view
	 */
	public class PasteAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			try {
				// System.out.println( "paste action performed" );
				Toolkit kit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = kit.getSystemClipboard();
				Transferable transferable = clipboard.getContents(null);
				m_droplistener.drop(transferable);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
