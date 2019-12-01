package com.jeta.abeille.gui.model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.JETAContainerAdapter;

import com.jeta.open.gui.framework.UIDirector;

import com.jeta.foundation.gui.table.CopyListOptionsPanel;
import com.jeta.foundation.gui.table.export.ExportModel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.open.gui.utils.JETAToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the main controller for an ObjectTreeView. The main support here is
 * to handle events related to creating/managing folders in the tree
 * 
 * @author Jeff Tassin
 */
public class ObjectTreeViewController extends TSController {

	private ObjectTreeView m_view;

	public ObjectTreeViewController(ObjectTreeView view) {
		super(view);
		m_view = view;

		assignAction(TSComponentNames.ID_COPY, new CopyAction());
		assignAction(TSComponentNames.ID_COPY_SPECIAL, new CopySpecialAction());
		assignAction(TSComponentNames.ID_CUT, new CutAction());
		assignAction(TSComponentNames.ID_PASTE, new PasteAction());

		assignAction(ObjectTreeNames.ID_NEW_FOLDER, new NewFolderAction());
		assignAction(ObjectTreeNames.ID_RENAME_FOLDER, new RenameFolderAction());
		assignAction(ObjectTreeNames.ID_REMOVE_FOLDER, new RemoveFolderAction());
		assignAction(ObjectTreeNames.ID_RELOAD, new ReloadAction());

		final DbObjectTree otree = (DbObjectTree) m_view.getTree();
		// popup menu support
		otree.addMouseListener(new MouseAdapter() {

			void showPopup(MouseEvent e) {
				DbObjectTreeModel tmodel = (DbObjectTreeModel) otree.getModel();

				if (e.isPopupTrigger()) {
					DbClassPopupMenu popup = m_view.getPopupMenu();
					popup.clearMenus();

					TreePath[] paths = otree.getSelectionPaths();
					if (paths != null && paths.length == 1) {
						TreePath path = paths[0];
						ObjectTreeNode selnode = (ObjectTreeNode) path.getLastPathComponent();
						String class_key = tmodel.getClassKey(selnode);
						if (class_key != null) {
							DbObjectClassTree classtree = otree.getClassTree(class_key);
							if (classtree != null) {
								popup.addMenuItems(classtree);
								UIDirector uidirector = classtree.getUIDirector();
								if (uidirector != null)
									uidirector.updateComponents(null);
							}
						}
					}

					UIDirector uidirector = m_view.getUIDirector();
					if (uidirector != null)
						uidirector.updateComponents(null);

					Dimension d = popup.getSize();
					Point pt = new Point(e.getX(), e.getY());
					javax.swing.SwingUtilities.convertPointToScreen(pt, m_view.getTree());

					int y = e.getY();
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

					// for first time
					if (d.height == 0) {
						popup.setLocation(screenSize.width, screenSize.height);
						// popup.setVisible(true);
						popup.doLayout();
						popup.setVisible(false);
						popup.setLocation(0, 0);
						d = popup.getSize();
						if (pt.y > screenSize.height * 3 / 4)
							y = y - d.height;
					} else {
						if (pt.y + d.height > screenSize.height)
							y = y - d.height;
					}

					m_view.getPopupMenu().show(m_view.getTree(), e.getX(), y);
					/*
					 * if ( JETAToolbox.isOSX() ) { if ( paths != null )
					 * otree.setSelectionPaths( paths ); }
					 */

				}

			}

			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}
		});
	}

	/**
	 * Called when the user wants to copy the selected tree nodes to the
	 * clipboard
	 */
	public class CopyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ObjectTree otree = m_view.getTree();
			// we have to to this to support a more sophisticated drag-drop in
			// the tree
			MultiTransferable mt = (MultiTransferable) otree.createTransferable();
			if (mt != null) {
				Toolkit kit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = kit.getSystemClipboard();
				clipboard.setContents(mt, null);
			}
		}
	}

	/**
	 * Called when the user wants to copy the selected tree nodes to the
	 * clipboard using special formatting. Note that this only affects String
	 * flavors.
	 */
	public class CopySpecialAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			// the copy options dialog
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
			CopyListOptionsPanel panel = new CopyListOptionsPanel(false);
			dlg.setTitle(I18N.getLocalizedMessage("Copy Special"));
			dlg.setPrimaryPanel(panel);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				ObjectTree otree = m_view.getTree();
				// we have to to this to support a more sophisticated drag-drop
				// in the tree
				MultiTransferable mt = (MultiTransferable) otree.createTransferable();
				if (mt != null) {
					mt.setExportModel(panel.getModel());
					Toolkit kit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = kit.getSystemClipboard();
					clipboard.setContents(mt, null);
				}
			}
		}
	}

	/**
	 * Called when the user wants to cut the selected tree nodes to the
	 * clipboard Cut only works when cutting and pasting between the same tree.
	 * This is effectively a move operation. You can't cut from an object tree
	 * to some other component
	 */
	public class CutAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ObjectTree otree = m_view.getTree();
			// we have to to this to support a more sophisticated drag-drop in
			// the tree
			MultiTransferable mt = (MultiTransferable) otree.createTransferable();
			if (mt != null) {
				Toolkit kit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = kit.getSystemClipboard();
				ClipboardNotifier cn = new ClipboardNotifier(mt);
				clipboard.setContents(mt, cn);
				ObjectTree.setMoveFlag(mt, true);
				otree.setTransferable(mt);
			}
			otree.repaint();
		}
	}

	/**
	 * Called when the user wants to paste any (previously cut) tree nodes from
	 * the clipboard Cut only works when cutting and pasting between the same
	 * tree. This is effectively a move operation. You can't paste to an object
	 * tree to some other component
	 */
	public class PasteAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				ObjectTree otree = m_view.getTree();
				TreePath path = otree.getSelectionPath();
				if (path != null) {
					ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
					if (node != null) {
						Toolkit kit = Toolkit.getDefaultToolkit();
						Clipboard clipboard = kit.getSystemClipboard();
						Transferable transferable = (Transferable) clipboard.getContents(null);
						if (otree.canDrop(node, transferable)) {

							otree.drop(node, transferable);
							otree.setTransferable(null);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Called when the user wants to create a new folder in the object tree
	 */
	public class NewFolderAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ObjectTree tree = m_view.getTree();
			TreePath path = tree.getSelectionPath();
			if (path != null) {
				ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
				if (tree.nodeAllowsFolders(node)) {
					ObjectTreeModel model = (ObjectTreeModel) tree.getModel();
					ObjectTreeNode foldernode = model.createFolder(node, I18N.getLocalizedMessage("New Folder"));
					tree.expandNode(node, false);

					TreeNode[] tpath = foldernode.getPath();
					// System.out.println( "folder path length: " + tpath.length
					// );

					ObjectTreeNode lastitem = (ObjectTreeNode) tpath[tpath.length - 1];
					TreePath folderpath = new TreePath(tpath);
					tree.scrollPathToVisible(folderpath);
					tree.startEditingAtPath(folderpath);
				}
			} else {
				System.out.println("ObjectTreeViewController.path is null");
			}
		}

	}

	/**
	 * Action handler for the reload command. If the currently selected node is
	 * a schema, then we reload the entire model. Otherwise, if the node is a
	 * table, then we simply reload the table.
	 */
	public class ReloadAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ObjectTreeNode node = m_view.getTree().getSelectedNode();
			if (node != null) {
				ObjectTreeModel model = m_view.getModel();
				ObjectTreeNode classnode = model.getClassNode(node);
				if (classnode != null && m_view.getTree().canReload(node)) {
					DbObjectClassModel ocmodel = (DbObjectClassModel) classnode.getUserObject();
					TSConnection tsconn = model.getConnection(node);
					Catalog catalog = model.getCatalog(node);
					Schema schema = model.getSchema(node);
					model.saveState(classnode);
					ocmodel.reloadNode(node);
				}
			}
		}
	}

	/**
	 * Called when the user wants to rename a folder from the tree
	 */
	public class RenameFolderAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ObjectTree tree = m_view.getTree();
			TreePath path = tree.getSelectionPath();
			ObjectTreeNode node = m_view.getModel().getRootNode();
			if (path != null)
				node = (ObjectTreeNode) path.getLastPathComponent();

			Object userobj = node.getUserObject();
			if (userobj instanceof TreeFolder) {
				tree.startEditingAtPath(path);
			}
		}

	}

	/**
	 * Called when the user wants to remove a folder from the tree
	 */
	public class RemoveFolderAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {

			ObjectTree tree = m_view.getTree();
			TreePath path = tree.getSelectionPath();
			ObjectTreeModel model = m_view.getModel();
			ObjectTreeNode node = model.getRootNode();
			if (path != null)
				node = (ObjectTreeNode) path.getLastPathComponent();

			Object userobj = node.getUserObject();
			if (userobj instanceof TreeFolder) {
				if (isEmpty(node)) {
					ObjectTreeNode parent = (ObjectTreeNode) node.getParent();
					model.removeNodeFromParent(node);
					// model.nodeStructureChanged( parent );
				} else {
					String msg = I18N.getLocalizedMessage("Folder is not empty");
					String error = I18N.getLocalizedMessage("Error");
					JOptionPane.showMessageDialog(null, msg, error, JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		/**
		 * Iterates over all descendents of the given node. If any descendent is
		 * not a folder node, then we assume the node is not empty.
		 */
		boolean isEmpty(ObjectTreeNode node) {
			for (int index = 0; index < node.getChildCount(); index++) {
				ObjectTreeNode childnode = (ObjectTreeNode) node.getChildAt(index);
				Object userobj = childnode.getUserObject();
				if (userobj != null && !(userobj instanceof TreeFolder))
					return false;

				if (!isEmpty(childnode))
					return false;
			}

			return true;
		}

	}

	public class ClipboardNotifier implements ClipboardOwner {
		private MultiTransferable m_mt;

		public ClipboardNotifier(MultiTransferable mt) {
			m_mt = mt;
		}

		/**
		 * Called when this controller has lost ownership of clipboard
		 */
		public void lostOwnership(Clipboard clipboard, Transferable contents) {

			ObjectTree otree = m_view.getTree();
			// we have to to this to support a more sophisticated drag-drop in
			// the tree
			if (m_mt != null) {
				ObjectTree.setMoveFlag(m_mt, false);
				otree.setTransferable(null);
				otree.repaint();
			}
		}
	}

}
