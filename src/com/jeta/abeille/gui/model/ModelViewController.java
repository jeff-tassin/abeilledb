package com.jeta.abeille.gui.model;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.command.CommandRunner;

import com.jeta.abeille.gui.common.TableSelectorDialog;
import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.common.DefaultTableSelectorModel;
import com.jeta.abeille.gui.modeler.ModelerUtils;
import com.jeta.abeille.gui.modeler.TableEditorDialog;
import com.jeta.abeille.gui.update.ShowInstanceFrameAction;
import com.jeta.abeille.gui.update.TableInstanceViewBuilder;

import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.componentmgr.TSNotifier;
import com.jeta.foundation.gui.components.BasicPopupMenu;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the controller for the ModelView window
 * 
 * @author Jeff Tassin
 */
public class ModelViewController extends TSController implements ViewGetter {
	/**
	 * The view we are controlling
	 */
	private ModelView m_view;

	/** the drop target for drag-drop */
	private DropTarget m_droptarget;

	/** the drop listener for drag-drop */
	private ModelViewDropListener m_droplistener;

	/**
	 * The modeler for this connection
	 */
	private ModelerModel m_modeler;

	/**
	 * ctor
	 */
	public ModelViewController(ModelView view, ModelerModel modeler) {
		super(view);
		m_view = view;
		m_modeler = modeler;

		RemoveFromViewAction removeaction = new RemoveFromViewAction(this);
		CopyAction copyaction = new CopyAction(this);
		PasteAction pasteaction = new PasteAction(this);
		CutAction cutaction = new CutAction(this);

		assignAction(ModelViewNames.ID_REMOVE_FROM_VIEW, removeaction);
		assignAction(TSComponentNames.ID_CUT, cutaction);
		assignAction(TSComponentNames.ID_COPY, copyaction);
		assignAction(TSComponentNames.ID_PASTE, pasteaction);
		assignAction(ModelViewNames.ID_COPY_AS_NEW, new CopyAsNewAction());
		assignAction(ModelViewNames.ID_SELECT_ALL, new SelectAllAction(this));

		MyEditTableAction editaction = new MyEditTableAction();
		assignAction(ModelViewNames.ID_TABLE_PROPERTIES, editaction);

		// add standard key stroke handlers for cut, copy, and paste
		view.getInputMap()
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false), ModelViewNames.ID_REMOVE_FROM_VIEW);
		view.getActionMap().put(ModelViewNames.ID_REMOVE_FROM_VIEW, removeaction);

		view.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), ModelViewNames.ID_TABLE_PROPERTIES);
		view.getActionMap().put(ModelViewNames.ID_TABLE_PROPERTIES, editaction);

		view.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false),
				TSComponentNames.ID_COPY);
		view.getActionMap().put(TSComponentNames.ID_COPY, copyaction);

		view.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, false),
				TSComponentNames.ID_PASTE);
		view.getActionMap().put(TSComponentNames.ID_PASTE, pasteaction);

		m_droplistener = createDropListener();
		m_droptarget = new DropTarget(m_view, m_droplistener);
	}

	/**
	 * Initializes the drop listener for this controller
	 */
	protected ModelViewDropListener createDropListener() {
		// set up the drop listener for the view
		if (m_droplistener == null) {
			m_droplistener = new ModelViewDropListener(m_view);
		}
		return m_droplistener;
	}

	/**
	 * @return the drop listener that is responsible for handling drag and drop
	 *         operations
	 */
	public ModelViewDropListener getDropListener() {
		return m_droplistener;
	}

	/**
	 * @return the current view (ModelView)
	 */
	public ModelView getModelView() {
		return m_view;
	}

	public ModelerModel getModeler() {
		return m_modeler;
	}

	/**
	 * Traverses the ancestor heirarchy looking for a parent that is an instance
	 * of a BasicPopupMenu. If the popup is found, it is returned. Otherwise,
	 * null is returned.
	 */
	BasicPopupMenu getPopup(Object obj) {
		if (obj instanceof Component) {
			Component comp = (Component) obj;
			Container parent = comp.getParent();
			while (parent != null) {
				if (parent instanceof BasicPopupMenu)
					return (BasicPopupMenu) parent;

				parent = parent.getParent();
			}
		}

		return null;
	}

	/**
	 * ViewGetter implementation
	 */
	public Collection getViews() {
		LinkedList result = new LinkedList();
		result.add(m_view);
		return result;
	}

	/**
	 * Copies the selected table widgets to the system clipboard
	 */
	public class CopyAction extends AbstractAction {
		private ViewGetter m_getter;

		public CopyAction(ViewGetter getter) {
			super(TSComponentNames.ID_COPY);
			m_getter = getter;
		}

		public void actionPerformed(ActionEvent evt) {
			ModelView view = m_getter.getModelView();
			Collection items = view.getSelectedItems();
			Iterator iter = items.iterator();
			MultiTransferable mt = null;
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof TableWidget) {
					if (mt == null)
						mt = new MultiTransferable();

					TableWidget widget = (TableWidget) obj;
					if (getModeler().isPrototype(widget.getTableId())) {
						DbObjectTransfer.addTransferable(mt, widget);
						DbObjectTransfer.addPrototype(mt, widget.getConnection(), widget.getTableMetaData());
					} else {
						DbObjectTransfer.addTransferable(mt, widget);
						Collection inlinks = view.getModel().getLinkModel().getInLinks(widget.getTableId());
						Iterator liter = inlinks.iterator();
						while (liter.hasNext()) {
							Link link = (Link) liter.next();
							if (link.isUserDefined())
								DbObjectTransfer.addTransferable(mt, link);
						}
						Collection outlinks = view.getModel().getLinkModel().getOutLinks(widget.getTableId());
						liter = outlinks.iterator();
						while (liter.hasNext()) {
							Link link = (Link) liter.next();
							if (link.isUserDefined())
								DbObjectTransfer.addTransferable(mt, link);
						}

					}
				}
			}// end while

			if (mt != null) {
				Toolkit kit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = kit.getSystemClipboard();
				clipboard.setContents(mt, null);
			}
		}
	}

	/**
	 * Adds a prototype to the view/model/modeler
	 */
	public TableWidget addPrototype(TableMetaData tmd, Point pt) {
		if (tmd != null) {
			ModelerUtils.validateColumnSizes(tmd);
			if (getModeler().addTablePrototype(tmd)) {
				ModelViewModel viewmodel = m_view.getModel();
				TableWidget widget = viewmodel.addTable(tmd.getTableId(), pt.x, pt.y);
				m_view.deselectAll();
				m_view.selectComponent(widget);
				return widget;
			}
		}
		return null;
	}

	/**
	 * Copies the table metadata from the selected table widget and creates a
	 * new *unsaved* table widget based on the copied table
	 */
	public class CopyAsNewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Collection items = m_view.getSelectedItems();
			Iterator iter = items.iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof TableWidget) {
					TableWidget tw = (TableWidget) obj;
					TableMetaData tmd = (TableMetaData) tw.getTableMetaData().clone();

					TableId tableid = tmd.getTableId();
					String tablename = tableid.getTableName();
					tablename = getModeler().createTableName(tableid);
					if (tablename != null) {
						tableid = (TableId) tableid.changeName(tablename);
						tmd.setTableId(tableid);
						DbKey dbkey = tmd.getPrimaryKey();
						if (dbkey != null)
							dbkey.setKeyName("");

						TableWidget newtw = addPrototype(tmd, new Point(tw.getX() + 10, tw.getY() + 10));
						if (newtw != null) {
							newtw.setSize(tw.getSize());
							// deselect and reselect so we can update the resize
							// handle location
							// after we do a resize
							m_view.deselectComponent(newtw);
							m_view.selectComponent(newtw);
						}
					}
				}
			}
		}
	}

	/**
	 * Cuts the selected table widgets to the system clipboard
	 */
	public class CutAction extends AbstractAction {
		private ViewGetter m_getter;

		public CutAction(ViewGetter getter) {
			super(TSComponentNames.ID_CUT);
			m_getter = getter;
		}

		public void actionPerformed(ActionEvent evt) {
			invokeAction(TSComponentNames.ID_COPY);
			invokeAction(ModelViewNames.ID_REMOVE_FROM_VIEW);
		}
	}

	public class MyEditTableAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			TableMetaData tmd = m_view.getSelectedTable();
			if (tmd != null) {
				EditTableAction.editTable(getModeler(), tmd.getTableId());
			}
		}
	}

	/**
	 * Pastes any tables from the system clipboard
	 */
	public class PasteAction extends AbstractAction {
		private ViewGetter m_getter;

		public PasteAction(ViewGetter getter) {
			super(TSComponentNames.ID_PASTE);
			m_getter = getter;
		}

		public void actionPerformed(ActionEvent evt) {
			try {
				Toolkit kit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = kit.getSystemClipboard();
				Transferable transferable = clipboard.getContents(null);
				ModelView view = m_getter.getModelView();
				ModelViewController controller = (ModelViewController) view.getController();
				ModelViewDropListener droplistener = controller.getDropListener();

				Point droppt = null;
				// if the source of the event was the popup menu, then let's get
				// the point
				// at which the user right clicked.
				BasicPopupMenu popup = getPopup(evt.getSource());
				if (popup != null)
					droppt = popup.getPopupLocation();

				droplistener.drop(transferable, droppt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Removes the selected items from the view
	 */
	public static class RemoveFromViewAction extends AbstractAction {
		private ViewGetter m_getter;

		public RemoveFromViewAction(ViewGetter getter) {
			super(ModelViewNames.ID_REMOVE_FROM_VIEW);
			m_getter = getter;
		}

		public void actionPerformed(ActionEvent evt) {
			ModelView view = m_getter.getModelView();
			if (view != null) {
				ModelViewModel model = view.getModel();
				Collection c = view.getSelectedItems();
				Iterator iter = c.iterator();
				while (iter.hasNext()) {
					Component comp = (Component) iter.next();
					if (comp instanceof TableWidget) {
						TableWidget widget = (TableWidget) comp;
						model.removeWidget(widget);
					}
				}
			}
		}
	}

	/**
	 * MouseListener implementation. We listen for double clicks so we can
	 * invoke editor
	 */
	public class MouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
				Object obj = e.getSource();
				if (obj instanceof TableWidget) {
					TableWidget w = (TableWidget) obj;
					// editTable( m_view, w );
				}
			}
		}
	}

	/**
	 * Selects all widgets in the view
	 */
	public class SelectAllAction implements ActionListener {
		private ViewGetter m_getter;

		public SelectAllAction(ViewGetter getter) {
			m_getter = getter;
		}

		public void actionPerformed(ActionEvent evt) {
			ModelView view = m_getter.getModelView();
			if (view != null) {
				view.selectAll();
			}
		}
	}

}
