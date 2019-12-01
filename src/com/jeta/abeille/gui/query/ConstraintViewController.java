package com.jeta.abeille.gui.query;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.awt.dnd.DropTarget;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.jeta.abeille.query.QueryConstraint;
import com.jeta.abeille.gui.common.DefaultTableSelectorModel;
import com.jeta.abeille.gui.model.MultiTransferable;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.editor.TSTextDialog;
import com.jeta.foundation.gui.quickedit.QuickEditDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the controller for the query builder window. It is specifically used
 * to handle events for adding/editing/removing constraints. It also is
 * responsible for enabling/disabling menu/toolbar components for constraints.x
 * 
 * @author Jeff Tassin
 */
public class ConstraintViewController extends TSController {
	/** the frame window that we are controlling */
	private QueryBuilderFrame m_frame;

	/** the view that we are controlling */
	private ConstraintView m_view;

	/** the underyling data model */
	private ConstraintModel m_model;

	/** The drop target for this view */
	private DropTarget m_droptarget;
	/** needed because if an object is dragged over the empty part of table */
	private DropTarget m_scrolldroptarget;

	/** the drop listener */
	private ConstraintDropListener m_droplistener;

	/**
	 * ctor
	 */
	public ConstraintViewController(QueryBuilderFrame frame, ConstraintView view) {
		super(view);
		m_frame = frame;
		m_view = view;
		m_model = m_view.getModel();

		DeleteAction delaction = new DeleteAction();

		assignAction(QueryNames.ID_ADD_CONSTRAINT, new AddAction());
		assignAction(QueryNames.ID_DELETE_ITEM, delaction);
		assignAction(QueryNames.ID_MOVE_DOWN, new MoveDownAction());
		assignAction(QueryNames.ID_MOVE_UP, new MoveUpAction());

		JTable table = m_view.getTable();
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false), QueryNames.ID_REMOVE_CONSTRAINT);
		table.getActionMap().put(QueryNames.ID_REMOVE_CONSTRAINT, delaction);

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					JTable table = m_view.getTable();
					int row = table.rowAtPoint(evt.getPoint());
					int col = table.columnAtPoint(evt.getPoint());
					if (row >= 0) {
						if (col == ConstraintModel.LOGIC_COLUMN) {
							m_model.toggleLogicalConnective(row);
							table.setRowSelectionInterval(row, row);
						} else if (col == ConstraintModel.CONSTRAINT_COLUMN) {
							invokeTableCellEditor(row, col);
						}
					}
				}
			}
		});

		JScrollPane scroll = (JScrollPane) m_view.getComponentByName(ConstraintView.ID_SCROLL_PANE);

		PasteAction pasteaction = new PasteAction();
		CopyAction copyaction = new CopyAction();
		CutAction cutaction = new CutAction();

		assignAction(TSComponentNames.ID_PASTE, pasteaction);
		assignAction(TSComponentNames.ID_COPY, copyaction);
		assignAction(TSComponentNames.ID_CUT, cutaction);

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, false),
				TSComponentNames.ID_PASTE);
		table.getActionMap().put(TSComponentNames.ID_PASTE, pasteaction);

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false),
				TSComponentNames.ID_COPY);
		table.getActionMap().put(TSComponentNames.ID_COPY, copyaction);

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK, false),
				TSComponentNames.ID_CUT);
		table.getActionMap().put(TSComponentNames.ID_CUT, cutaction);

		// drag and drop for table
		m_droplistener = new ConstraintDropListener(m_view);

		m_droptarget = new DropTarget(table, m_droplistener);
		m_scrolldroptarget = new DropTarget(scroll, new ConstraintDropListener(m_view));
	}

	/**
	 * Invokes the quick editor dialog for a given table cell. This dialog has a
	 * much more flexible interface than the standard in-place editor for a
	 * table.
	 */
	private void invokeTableCellEditor(int row, int col) {
		JTable table = m_view.getTable();

		int frame_width = m_frame.getWidth();
		Point frame_pt = m_frame.getLocationOnScreen();

		Rectangle rect = table.getCellRect(row, col, true);
		QuickEditDialog dlg = (QuickEditDialog) TSGuiToolbox.createDialog(QuickEditDialog.class, m_frame, true);

		JEditorPane editor = TSEditorUtils.createEditor(new ConstraintKit(m_frame.getConnection(),
				new DefaultTableSelectorModel(m_frame.getConnection())));
		TSGuiToolbox.setTextFieldBorder(editor);

		Object obj = m_model.getValueAt(row, col);
		if (obj != null) {
			// editor has problems with empty strings
			String objs = obj.toString();
			if (objs != null) // yes, toString() can return null
				editor.setText(objs);
		}

		dlg.initialize(editor);

		Dimension d = dlg.getPreferredSize();

		Point pt = new Point(rect.x, rect.y);
		SwingUtilities.convertPointToScreen(pt, table);

		d.width = rect.width * 2;

		if ((pt.x + d.width) > (frame_pt.x + frame_width))
			d.width = frame_width - (pt.x - frame_pt.x) - 10;

		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		if ((pt.x + d.width) > screensize.width) {
			// the table cell editor is too long and off screen, so resize
			d.width = screensize.width - pt.x;

			if (d.width < 100) {
				// table editor is too small, resize to fit screen
				d.width = 100;
				pt.x = screensize.width - d.width - 10;
			}
		}
		editor.setCaretPosition(editor.getDocument().getLength());

		dlg.setSize(d);
		dlg.setLocation(pt);
		dlg.setInitialFocusComponent(editor);
		dlg.show();
		if (dlg.isShowMore()) {
			// the user pressed the ... button, so the dialog goes away and we
			// show a text dialog
			String sval = dlg.getText();

			dlg.dispose();

			TSTextDialog txtdlg = (TSTextDialog) TSGuiToolbox.createDialog(TSTextDialog.class, m_frame, true);
			txtdlg.setTitle(I18N.getLocalizedMessage("Query Constraint"));
			txtdlg.initialize(com.jeta.abeille.gui.sql.SQLKit.class);
			// txtdlg.setEditor( editor );
			txtdlg.setText(editor.getText());

			TSGuiToolbox.centerFrame(txtdlg, 0.75f, 0.5f);
			txtdlg.show();
			if (txtdlg.isOk()) {
				String cctxt = stripNewlines(txtdlg.getText());
				if (TSUtils.fastTrim(cctxt).length() == 0)
					m_model.remove(row);
				else
					m_model.setValueAt(cctxt, row, col);
			} else {
				QueryConstraint qc = (QueryConstraint) m_model.getValueAt(row, ConstraintModel.CONSTRAINT_COLUMN);
				if (TSUtils.fastTrim(qc.toString()).length() == 0)
					m_model.remove(row);
			}
		} else if (dlg.isOk()) {
			String cctxt = stripNewlines(dlg.getText());
			if (TSUtils.fastTrim(cctxt).length() == 0)
				m_model.remove(row);
			else
				m_model.setValueAt(cctxt, row, col);
		} else {
			QueryConstraint qc = (QueryConstraint) m_model.getValueAt(row, ConstraintModel.CONSTRAINT_COLUMN);
			if (TSUtils.fastTrim(qc.toString()).length() == 0)
				m_model.remove(row);

		}
	}

	/**
	 * Removes any CR or LF characters found in text since we allow only single
	 * lines of text in the constraint
	 */
	private String stripNewlines(String sval) {
		StringBuffer result = new StringBuffer();
		for (int index = 0; index < sval.length(); index++) {
			char c = sval.charAt(index);
			if (c != '\n' && c != '\r')
				result.append(c);
		}
		return result.toString();
	}

	/**
	 * ------------------------------------ controller actions
	 * ------------------------------
	 */

	/**
	 * Adds a constraint to the view/model
	 */
	public class AddAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_model.addRow(new QueryConstraint());
			int row = m_model.getRowCount() - 1;
			invokeTableCellEditor(row, ConstraintModel.CONSTRAINT_COLUMN);
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
				for (int index = 0; index < rows.length; index++) {
					QueryConstraint qc = m_model.getRow(rows[index]);
					if (qc != null) {
						mt.addData(QueryFlavor.QUERY_CONSTRAINT, qc);
						if (index > 0)
							mt.addData(DataFlavor.stringFlavor, qc.getLogicalConnective().toString());

						mt.addData(DataFlavor.stringFlavor, qc.toString());

						if ((index + 1) < rows.length)
							mt.addData(DataFlavor.stringFlavor, "\n");

					}
				}

				Toolkit kit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = kit.getSystemClipboard();
				clipboard.setContents(mt, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * When user selects cut menu item
	 */
	public class CutAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			invokeAction(TSComponentNames.ID_COPY);
			invokeAction(QueryNames.ID_DELETE_ITEM);
		}
	}

	/**
	 * When use hits delete key on constraint table
	 */
	public class DeleteAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			m_model.remove(m_view.getSelectedRows());
		}
	}

	/**
	 * Moves the selected column down a row
	 */
	public class MoveDownAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			int row = m_view.getSelectedRow();
			if (row >= 0 && row < (m_model.getRowCount() - 1)) {
				QueryConstraint qc = m_model.getRow(row);
				if (qc != null) {
					m_model.reorder(row + 1, row);
					row++;
					JTable table = m_view.getTable();
					ListSelectionModel selectionModel = table.getSelectionModel();
					selectionModel.setSelectionInterval(row, row);
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
				QueryConstraint qc = m_model.getRow(row);
				if (qc != null) {
					m_model.reorder(row - 1, row);
					row--;
					JTable table = m_view.getTable();
					ListSelectionModel selectionModel = table.getSelectionModel();
					selectionModel.setSelectionInterval(row, row);
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
