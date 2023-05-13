package com.jeta.abeille.gui.formbuilder;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import com.jeta.abeille.database.model.ColumnMetaData;

import com.jeta.abeille.gui.common.ColumnSelectorPanel;
import com.jeta.abeille.gui.common.SimpleTableSelectorModel;

import com.jeta.abeille.gui.update.InstanceOptionsModel;
import com.jeta.abeille.gui.update.InstanceOptionsView;
import com.jeta.abeille.gui.update.InstanceOptionsViewController;

import com.jeta.foundation.gui.components.BasicPopupMenu;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the controller for the columns view
 * 
 * @author Jeff Tassin
 */
public class ColumnsViewController extends InstanceOptionsViewController {
	/** the view that we are controlling */
	private ColumnsView m_view;

	/** the frame that contains the view */
	private FormBuilderFrame m_frame;

	/**
	 * this is the popup context menu that shows copy/cut/paste and others for
	 * reporables
	 */
	private BasicPopupMenu m_popup;

	/** drag-n-drop targets */
	private DropTarget m_droptarget;
	private DropTarget m_scrolldroptarget;
	private ColumnDropListener m_droplistener;

	public ColumnsViewController(FormBuilderFrame frame, ColumnsView view) {
		super(view);
		m_frame = frame;
		m_view = view;

		// drag and drop for table
		m_droplistener = new ColumnDropListener(m_view);
		m_droptarget = new DropTarget(m_view.getComponentByName(InstanceOptionsView.ID_COLUMNS_TABLE), m_droplistener);
		// for empty part of table
		m_scrolldroptarget = new DropTarget(m_view.getComponentByName(InstanceOptionsView.ID_TABLE_SCROLL),
				m_droplistener);

		ActionListener deleteaction = this.new DeleteAction();
		assignAction(FormNames.ID_REMOVE_COLUMN, deleteaction);
		assignAction(FormNames.ID_ADD_COLUMN, new AddColumnAction());

		JTable table = m_view.getTable();
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false), FormNames.ID_REMOVE_COLUMN);
		table.getActionMap().put(FormNames.ID_REMOVE_COLUMN,
				new TSController.DelegateAction(FormNames.ID_REMOVE_COLUMN));

		m_popup = new BasicPopupMenu(m_view);
		m_view.enableComponent(TSComponentNames.ID_CUT, false);
		m_view.enableComponent(TSComponentNames.ID_COPY, false);

		assignAction(TSComponentNames.ID_PASTE, new PasteAction());
		PopupHandler handler = new PopupHandler();
		table.addMouseListener(handler);
		JScrollPane scroll = (JScrollPane) m_view.getComponentByName(InstanceOptionsView.ID_TABLE_SCROLL);
		scroll.addMouseListener(handler);

		ColumnsViewUIDirector uidirector = new ColumnsViewUIDirector(m_view, m_frame);
		m_view.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * ------------------------------------ controller actions
	 * ------------------------------
	 */

	/**
	 * Adds a column to the form
	 */
	public class AddColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			FormModel formmodel = m_frame.getModel();
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
			SimpleTableSelectorModel selectormodel = new SimpleTableSelectorModel(m_frame.getConnection(),
					formmodel.getTables());
			ColumnSelectorPanel cpanel = new ColumnSelectorPanel(m_frame.getConnection(), selectormodel);
			dlg.setTitle(I18N.getLocalizedMessage("Add Column"));
			dlg.setPrimaryPanel(cpanel);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				ColumnMetaData cmd = cpanel.getSelectedColumn();
				if (cmd != null) {
					FormInstanceMetaData metadata = (FormInstanceMetaData) m_view.getMetaData();
					metadata.addColumn(cmd);
					InstanceOptionsModel guimodel = m_view.getGuiModel();
					guimodel.fireModelChanged();
					m_view.repaint();
				}
			}
		}
	}

	/**
	 * Paste any columns found on the clipboard into the columns view
	 */
	public class PasteAction implements ActionListener {
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

	/**
	 * Listener on the popup menu trigger
	 */
	public class PopupHandler extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				m_popup.show((JComponent) e.getSource(), e.getX(), e.getY());
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				m_popup.show((JComponent) e.getSource(), e.getX(), e.getY());
			}
		}
	}

}
