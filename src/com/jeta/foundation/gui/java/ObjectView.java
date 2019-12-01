/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import java.util.Enumeration;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.treetable.JTreeTable;
import com.jeta.foundation.i18n.I18N;

/**
 * This class provides a tree view on a Java object. It uses reflection to
 * introspect the members of a Java object and display them in the tree.
 * 
 * @author Jeff Tassin
 */
public class ObjectView extends TSPanel {
	private JTreeTable m_table; // this is the main control (tree/table combo)
	private ObjectModel m_model;

	/** text field at top of view that displays object class name */
	private JTextField m_objectname;

	public static final String OBJECT_TREETABLE = "objectview.objecttree";

	/**
	 * ctor
	 */
	public ObjectView(ObjectModel model) {
		m_model = model;
		m_table = new JTreeTable(model);
		m_table.setName(OBJECT_TREETABLE);

		m_objectname = new JTextField();
		// m_objectname.setEnabled( false );
		m_objectname.setEditable(false);
		// No intercell spacing
		// m_table.setIntercellSpacing(new Dimension(10, 0));

		JTree tree = m_table.getTree();
		tree.setRootVisible(false);
		tree.setCellRenderer(new ObjectRenderer());
		tree.putClientProperty("JTree.lineStyle", "Angled");
		tree.setShowsRootHandles(true);

		JScrollPane scrollpane = new JScrollPane(m_table);
		scrollpane.getViewport().setBackground(Color.white);

		setLayout(new BorderLayout(2, 2));
		add(m_objectname, BorderLayout.NORTH);
		add(scrollpane, BorderLayout.CENTER);

		setController(new ObjectController(this));

		m_model.addTreeModelListener(new TreeModelListener() {

			public void treeNodesChanged(TreeModelEvent e) {
				updateView();
				m_model.notifyListeners();
			}

			public void treeNodesInserted(TreeModelEvent e) {

			}

			public void treeNodesRemoved(TreeModelEvent e) {

			}

			public void treeStructureChanged(TreeModelEvent e) {
				updateView();
				m_model.notifyListeners();
			}
		});

		AbstractTableModel tmodel = (AbstractTableModel) m_table.getModel();
		tmodel.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				updateView();
				m_model.notifyListeners();
			}
		});
	}

	/**
	 * Expands all nodes in the object treetable
	 */
	public void expandAll() {
		expandNode((DefaultMutableTreeNode) m_model.getRoot(), true);
	}

	/**
	 * Expands a given node in the tree table
	 */
	public void expandNode(DefaultMutableTreeNode parentNode, boolean bRecursive) {
		if (parentNode != null) {
			m_table.getTree().expandPath(new TreePath(parentNode.getPath()));
			if (bRecursive)
				for (Enumeration e = parentNode.children(); e.hasMoreElements();) {
					DefaultMutableTreeNode childnode = (DefaultMutableTreeNode) e.nextElement();
					expandNode(childnode, bRecursive);
				}
		}
	}

	/**
	 * @return the underlying treetable model
	 */
	public ObjectModel getModel() {
		return m_model;
	}

	/**
	 * @return the currently selected node. Null is returned if no node is
	 *         selected
	 */
	DefaultMutableTreeNode getSelectedNode() {
		TreePath path = m_table.getTree().getSelectionPath();
		if (path == null)
			return null;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		return node;
	}

	/**
	 * Sets the text message
	 */
	public void setText(String txt) {
		m_objectname.setText(txt);
	}

	/**
	 * Updates the view based on the model
	 */
	void updateView() {
		Object obj = m_model.getObject();
		if (obj == null) {
			m_objectname.setText("");
		} else {
			m_objectname.setText(obj.getClass().getName());
		}
	}

}
