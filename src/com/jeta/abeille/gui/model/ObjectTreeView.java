package com.jeta.abeille.gui.model;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.dnd.DropTarget;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DbModelEvent;
import com.jeta.abeille.database.model.DbModelListener;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.support.ComponentFinder;
import com.jeta.open.support.CompositeComponentFinder;
import com.jeta.open.support.DefaultComponentFinder;

/**
 * This is the base class used for showing objects in the database. The primary
 * function for this class is as follows: provide hierarchial support (
 * organizing the objects in a JTree) allowing the user to create/edit/delete
 * folders allowing the user to drag/drop objects to different folders allowing
 * the user to drag/drop objects to different windows in the app
 * 
 * @author Jeff Tassin
 */
public abstract class ObjectTreeView extends TSPanel implements DbModelListener {
	private ObjectTree m_tree; // the tree object for this panel

	/**
	 * the content menu that pops up when user right clicks on query object or
	 * tree
	 */
	private DbClassPopupMenu m_popupmenu;

	/**
	 * this is the current component that is displayed in this view. Most of the
	 * time, this will be a JScrollPane (that contains a JTree). However, during
	 * DbModel initialization and reloading, some components will display a
	 * JPanel with the message 'Loading...' in this view. In that case, the
	 * m_content will point to that JPanel instead. We keep this reference
	 * around so we can remove it at a later time.
	 */
	private JComponent m_content = null;

	/**
	 * flag indicating if we have initialized this view. We don't initialize
	 * until the database model has been fully loaded
	 */
	private boolean m_initialized = false;

	/** the toolbar buttons for this view (can be null) */
	private TSToolBarTemplate m_toolbartemplate = null;

	/**
	 * ctor
	 */
	public ObjectTreeView() {
		setLayout(new BorderLayout());
		m_popupmenu = new DbClassPopupMenu();
		m_popupmenu.add(i18n_createMenuItem("Cut", TSComponentNames.ID_CUT, null));
		m_popupmenu.add(i18n_createMenuItem("Copy", TSComponentNames.ID_COPY, null));
		m_popupmenu.add(i18n_createMenuItem("Paste", TSComponentNames.ID_PASTE, null));
		m_popupmenu.addSeparator();
		m_popupmenu.add(i18n_createMenuItem("New Folder", ObjectTreeNames.ID_NEW_FOLDER, null));
		m_popupmenu.add(i18n_createMenuItem("Rename Folder", ObjectTreeNames.ID_RENAME_FOLDER, null));
		m_popupmenu.add(i18n_createMenuItem("Remove Folder", ObjectTreeNames.ID_REMOVE_FOLDER, null));
		m_popupmenu.addSeparator();
		m_popupmenu.add(i18n_createMenuItem("Reload", ObjectTreeNames.ID_RELOAD, null));
		m_popupmenu.addSeparator();
	}

	/**
	 * Sets the connection used by this view
	 */
	public void addConnection(TSConnection connection) {
		expandRootNode();
		ObjectTreeModel model = getModel();
		model.addConnection(connection);
		ObjectTreeNode servernode = model.getConnectionNode(connection);
		getTree().expandNode(servernode, false);
		connection.removeModelListener(this);
		connection.addModelListener(this);
	}

	/**
	 * Adds a menu item to this view's popup menu
	 */
	public void addPopupItem(JMenuItem popupItem) {
		m_popupmenu.add(popupItem);
	}

	/**
	 * Saves the state for the connection and removes it from the model
	 */
	public void closeConnection(TSConnection tsconn) {
		ObjectTreeModel model = getModel();
		model.saveHeirarchy(tsconn);
		tsconn.removeModelListener(this);
		ObjectTreeNode cnode = model.getConnectionNode(tsconn);
		if (cnode != null) {
			model.removeNodeFromParent(cnode);
		}
	}

	/**
	 * Helper method to create a button
	 */
	protected JButton _createToolBarButton(String iconName, String id, String tooltip) {
		JButton btn = i18n_createButton(null, id, iconName);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setPreferredSize(TSGuiToolbox.getToolbarButtonSize());
		btn.setMaximumSize(TSGuiToolbox.getToolbarButtonSize());
		if (tooltip != null)
			btn.setToolTipText(tooltip);

		return btn;
	}

	/**
	 * Creates a component finder that is used to locate child components in
	 * this panel by name.
	 */
	protected ComponentFinder createComponentFinder() {
		CompositeComponentFinder finder = new CompositeComponentFinder(new DefaultComponentFinder(this));
		/** must add menus here because they are not owned by this container */
		finder.add(new DefaultComponentFinder(m_popupmenu));
		return finder;
	}

	/**
	 * Override so we can update our toolbar template as well as child buttons
	 * 
	 * @param commandId
	 *            the id of the command whose button to enable/disable
	 * @param bEnable
	 *            true/false to enable/disable
	 */
	public void enableComponent(String commandId, boolean bEnable) {
		super.enableComponent(commandId, bEnable);
		TSToolBarTemplate template = getToolBarTemplate();
		if (template != null) {
			for (int index = 0; index < template.getComponentCount(); index++) {
				java.awt.Component comp = template.getComponentAt(index);
				if (commandId.equals(comp.getName())) {
					comp.setEnabled(bEnable);
					break;
				}
			}
		}
	}

	/**
	 * Called when we get an event from the DbModel
	 */
	private void eventFired2(DbModelEvent evt) {
	}

	/**
	 * Expands the root node
	 */
	public void expandRootNode() {
		ObjectTreeNode root = getModel().getRootNode();
		ObjectTree tree = getTree();
		tree.expandPath(new TreePath(root.getPath()));
		/*
		 * for( int index=0; index < root.getChildCount(); index++ ) {
		 * ObjectTreeNode child = (ObjectTreeNode)root.getChildAt(index);
		 * tree.expandNode( child, false ); }
		 */
	}

	public Catalog getCatalog(ObjectTreeNode node) {
		return getModel().getCatalog(node);
	}

	/**
	 * @return the underlying tree model
	 */
	public ObjectTreeModel getModel() {
		if (m_tree == null)
			return null;
		else
			return (ObjectTreeModel) m_tree.getModel();
	}

	/**
	 * @return the underlying context menu object
	 */
	public DbClassPopupMenu getPopupMenu() {
		return m_popupmenu;
	}

	/**
	 * @return the parent node that is the ancestor of the given node.
	 */
	public ObjectTreeNode getBaseNode(ObjectTreeNode selectedNode) {
		return getModel().getBaseNode(selectedNode);
	}

	/**
	 * Retrieves the default parent for the given node. If the node is null or
	 * it's default parent cannot be found, then we retreive the default parent
	 * for the current catalog or schema.
	 */
	public ObjectTreeNode getBaseNode(TSConnection conn, ObjectTreeNode node, boolean useDefault) {
		return getModel().getBaseNode(conn, node, useDefault);
	}

	/**
	 * @return the schema associated with the given node
	 */
	public Schema getSchema(ObjectTreeNode node) {
		return getModel().getSchema(node);
	}

	/**
	 * @return the selected tree node. Null is returned if no node is selected
	 */
	public ObjectTreeNode getSelectedNode() {
		return getTree().getSelectedNode();
	}

	/**
	 * @return the selected user object only if the object is of type c
	 */
	public Object getSelectedUserObject(Class c) {
		ObjectTreeNode node = getSelectedNode();
		if (node != null) {
			Object obj = node.getUserObject();
			if (obj != null && c.isAssignableFrom(obj.getClass()))
				return obj;
		}
		return null;
	}

	/**
	 * @return the toolbar template for this view. May be null
	 */
	public TSToolBarTemplate getToolBarTemplate() {
		return m_toolbartemplate;
	}

	/**
	 * @return the underlying JTree object
	 */
	public ObjectTree getTree() {
		return m_tree;
	}

	/**
	 * @return the flag indicating if this model has been initialized yet
	 */
	public boolean isInitialized() {
		return m_initialized;
	}

	/**
	 * Reloads the model
	 */
	protected void loadClass(ObjectTreeNode classNode) {
		Object obj = classNode.getUserObject();

		if (obj instanceof DbObjectClassModel) {
			DbObjectClassModel classmodel = (DbObjectClassModel) obj;

			if (classNode.getChildCount() > 0) {
				ObjectTreeNode childnode = (ObjectTreeNode) classNode.getChildAt(0);
				if (childnode instanceof EmptyTreeNode)
					classNode.remove(0);
			}

			getTree().repaint();
			ObjectTreeModel model = getModel();
			model.refreshModel(classmodel.getConnection(), classmodel.getCatalog(), classmodel.getSchema(),
					classmodel.getClassKey());
		}
	}

	/**
	 * Reloads the model
	 */
	private void loadCatalog(TSConnection conn, Catalog cat) {
		assert (!conn.supportsSchemas());
		ObjectTreeNode basenode = getModel().getBaseNode(conn, cat, Schema.VIRTUAL_SCHEMA);
		assert (basenode != null);
		CatalogWrapper wrapper = (CatalogWrapper) basenode.getUserObject();
		basenode.removeAllChildren();
		getTree().repaint();
		ObjectTreeModel model = getModel();
		assert (wrapper.isLoaded());
	}

	/**
	 * Reloads the model
	 */
	private void loadSchema(TSConnection conn, Catalog cat, Schema schema) {
		ObjectTreeNode basenode = getModel().getBaseNode(conn, cat, schema);
		assert (basenode != null);
		SchemaWrapper wrapper = (SchemaWrapper) basenode.getUserObject();
		basenode.removeAllChildren();
		getTree().repaint();
		ObjectTreeModel model = getModel();
		assert (wrapper.isLoaded());
	}

	/**
	 * Sets the main content panel for this view
	 */
	public void setContentPane(JComponent comp) {
		if (m_content != null)
			remove(m_content);
		add(comp, BorderLayout.CENTER);
		m_content = comp;
		revalidate();
	}

	/**
	 * Sets the initialized flag
	 */
	protected void setInitialized(boolean binit) {
		m_initialized = binit;
	}

	/**
	 * Sets the toolbar template for this view
	 */
	public void setToolBarTemplate(TSToolBarTemplate template) {
		m_toolbartemplate = template;
	}

	/**
	 * Sets the tree used by this view
	 */
	public void setTree(ObjectTree tree) {
		assert (m_tree == null);
		m_tree = tree;
		JScrollPane scroll = new JScrollPane(tree);
		scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		setContentPane(scroll);

		tree.addTreeWillExpandListener(new TreeWillExpandListener() {
			public void treeWillCollapse(TreeExpansionEvent event) {

			}

			/**
			 * Evaluate the Wrapper to determine if we need to load its children
			 */
			public void treeWillExpand(TreeExpansionEvent event) {
				TreePath path = event.getPath();
				ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
				if (!node.isLoaded()) {
					Object userobj = node.getUserObject();
					if (userobj instanceof DbObjectClassModel) {

						DbObjectClassModel model = (DbObjectClassModel) userobj;
						loadClass(node);
						node.setLoaded(true);
					} else {
						// no op
						ObjectTreeModel model = getModel();
						model.refreshNode(node);
						node.setLoaded(true);
					}
				}
			}
		});

	}

	/**
	 * Special handling for tables
	 */
	public void eventFired(DbModelEvent evt) {

		TableId tableid = evt.getTableId();
		if (tableid != null) {
			TableTreeBaseModel tmodel = null;
			ObjectTreeNode cnode = getModel().getClassNode(evt.getConnection(), tableid.getCatalog(),
					tableid.getSchema(), TableTreeModel.CLASS_KEY);
			if (cnode != null) {
				tmodel = (TableTreeBaseModel) cnode.getUserObject();
			}

			if (evt.getID() == DbModelEvent.TABLE_CHANGED) {
				tmodel.tableChanged(evt.getConnection(), tableid);
			} else if (evt.getID() == DbModelEvent.TABLE_RENAMED) {
				tmodel.tableRenamed(evt.getConnection(), (TableId) evt.getParameter(1), tableid);
			} else if (evt.getID() == DbModelEvent.TABLE_DELETED) {
				tmodel.tableDeleted(evt.getConnection(), tableid);
			} else if (evt.getID() == DbModelEvent.TABLE_CREATED) {
				tmodel.tableCreated(evt.getConnection(), tableid);
			}
		}
	}

}
