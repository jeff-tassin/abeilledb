package com.jeta.abeille.gui.formbuilder;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.DbObjectClassModel;
import com.jeta.abeille.gui.model.ObjectTreeModel;
import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.model.ObjectTreeViewController;
import com.jeta.abeille.gui.update.ShowInstanceFrameAction;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;

import com.jeta.open.gui.framework.JETAController;

/**
 * This is the main controller for the FormTreeView
 * 
 * @author Jeff Tassin
 */
public class FormTreeController extends JETAController {
	private FormTree m_view;

	public FormTreeController(FormTree view) {
		super(view);
		m_view = view;

		assignAction(FormNames.ID_SHOW_FORM, new ShowFormAction());
		assignAction(FormNames.ID_NEW_FORM, new NewFormAction());
		assignAction(FormNames.ID_EDIT_FORM, new EditFormAction());
		assignAction(FormNames.ID_RENAME_FORM, new RenameFormAction());
		assignAction(FormNames.ID_DELETE_FORM, new DeleteFormAction());

		m_view.getTree().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					editForm();
				}
			}
		});

	}

	/**
	 * Edits the selected form
	 */
	void editForm() {
		FormProxy formproxy = m_view.getSelectedForm();
		if (formproxy != null) {
			FormBuilderFrame frame = formproxy.getFrame();
			if (frame == null) {
				LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
				if (jlm.checkSessionTimeOut()) {
					frame = formproxy.createFrame();
				}
			} else {
				TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
				wsframe.show(frame);
			}
		}
	}

	/**
	 * Invoked when the user clicks the edit form menu item. Invokes the frame
	 * to edit a given form
	 */
	public class EditFormAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			editForm();
		}
	}

	/**
	 * Invoked when the user clicks the new form menu item. Invokes the dialog
	 * to create a new form
	 */
	public class NewFormAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ObjectTreeNode objnode = m_view.getSelectedNode();
			DbObjectClassModel classmodel = m_view.getClassModel(objnode);
			ObjectTreeNode defparent = classmodel.getValidParent(objnode);
			if (defparent != null && classmodel instanceof FormTreeModel) {
				FormTreeModel formmodel = (FormTreeModel) classmodel;
				TSConnection tsconn = m_view.getConnection(defparent);
				Catalog catalog = m_view.getCatalog(defparent);
				Schema schema = m_view.getSchema(defparent);
				if (schema != null && catalog != null) {
					// m_view.getTree().expandNode( defparent, false );
					FormProxy proxy = FormProxy.createInstance(catalog, schema, tsconn,
							I18N.getLocalizedMessage("New Form"));
					ObjectTreeNode newnode = formmodel.addForm(defparent, proxy);
					// m_view.getTree().setSelectionPath( new TreePath(
					// newnode.getPath() ) );
				} else {
					assert (false);
				}
			}
		}
	}

	/**
	 * Invoked when the user clicks the rename menu items. Invokes a dialog that
	 * allows the user to enter a new form name
	 */
	public class DeleteFormAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				ObjectTreeModel model = (ObjectTreeModel) m_view.getTree().getModel();
				TreePath[] selections = m_view.getTree().getSelectionPaths();
				if (selections.length > 0) {
					String title = I18N.getLocalizedMessage("Confirm");
					String msg = I18N.getLocalizedMessage("Delete the selected forms");
					int nresult = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
					if (nresult == JOptionPane.YES_OPTION) {
						for (int index = 0; index < selections.length; index++) {
							TreePath path = selections[index];
							ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
							Object userobj = node.getUserObject();
							if (userobj instanceof FormProxy) {
								FormProxy proxy = (FormProxy) userobj;
								proxy.deleteForm();
								model.removeNodeFromParent(node);
							}
						}
					}
				}
			} catch (Exception e) {
				TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, m_view.getTree(),
						true);
				dlg.initialize(null, e);
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
			}
		}
	}

	/**
	 * Renames the selected form
	 */
	public class RenameFormAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			FormProxy formproxy = m_view.getSelectedForm();
			if (formproxy != null) {
				String name = JOptionPane.showInputDialog(I18N.getLocalizedDialogLabel("Enter New Name"));
				if (name != null) {
					formproxy.setName(name);
				}
			}
		}
	}

	/**
	 * Invoked when the user wants to open the form from the tree view
	 */
	public class ShowFormAction extends ShowInstanceFrameAction {
		public void actionPerformed(ActionEvent evt) {
			FormProxy formproxy = m_view.getSelectedForm();
			if (formproxy != null) {

				FormModel model = (FormModel) formproxy.getModel();
				if (model.getAnchorTable() != null) {
					FormInstanceViewBuilder builder = new FormInstanceViewBuilder(model);
					showFrame(m_view.getConnection(m_view.getSelectedNode()), builder);
				}
			}
		}
	}

}
