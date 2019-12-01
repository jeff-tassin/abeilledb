/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;

import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.treetable.JTreeTable;

/**
 * This is the main controller for the ObjectView. It handles all user events
 * 
 * @author Jeff Tassin
 */
public class ObjectController extends TSController {

	/** the view we are controlling */
	private ObjectView m_view;

	/**
	 * ctor
	 */
	public ObjectController(ObjectView view) {
		super(view);
		m_view = view;
		JTreeTable table = (JTreeTable) m_view.getComponentByName(ObjectView.OBJECT_TREETABLE);
		JTree tree = table.getTree();
		tree.addTreeWillExpandListener(new TreeWillExpandListener() {
			public void treeWillCollapse(TreeExpansionEvent event) {

			}

			/**
			 * Evaluate the Wrapper to determine if we need to load its children
			 */
			public void treeWillExpand(TreeExpansionEvent event) {
				System.out.println("----------------- tree will expand ------------------ ");
				TreePath path = event.getPath();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				Object userobj = node.getUserObject();
				if (userobj instanceof JavaWrapper) {
					JavaWrapper wrapper = (JavaWrapper) node.getUserObject();
					if (!wrapper.isLoaded()) {
						node.removeAllChildren();
						wrapper.loadChildren(node);
					}
				}
			}

			/**
			 * Evaluate the Wrapper to determine if we need to load its children
			 */
			public void treeWillExpand1(TreeExpansionEvent event) {
				// System.out.println(
				// "----------------- tree will expand ------------------ " );
				/*
				 * TreePath path = event.getPath(); DefaultMutableTreeNode node
				 * = (DefaultMutableTreeNode)path.getLastPathComponent(); Object
				 * userobj = node.getUserObject(); if ( userobj instanceof
				 * ObjectWrapper ) { ObjectWrapper wrapper =
				 * (ObjectWrapper)node.getUserObject(); if ( !wrapper.isLoaded()
				 * ) { node.removeAllChildren(); m_model.loadObject( node,
				 * wrapper.getObject(), wrapper.getObject().getClass() );
				 * wrapper.setLoaded( true );
				 * 
				 * 
				 * Enumeration children = node.children(); while(
				 * children.hasMoreElements() ) { DefaultMutableTreeNode child =
				 * (DefaultMutableTreeNode)children.nextElement(); Object
				 * childobj = child.getUserObject(); if ( childobj instanceof
				 * ObjectWrapper ) { ObjectWrapper childwrapper =
				 * (ObjectWrapper)childobj; if ( !childwrapper.isLoaded() ) {
				 * child.removeAllChildren(); Object childuserobj =
				 * childwrapper.getObject(); if ( childuserobj != null )
				 * m_model.loadObject( child, childuserobj,
				 * childuserobj.getClass() ); childwrapper.setLoaded( true ); }
				 * } } } }
				 */

			}
		});

		// add mouse listener to get double click events
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					DefaultMutableTreeNode node = m_view.getSelectedNode();
					if (node != null) {
						Object userobj = node.getUserObject();
						JavaWrapper wrapper = (JavaWrapper) userobj;
						wrapper.edit();
						if (wrapper.hasChildren() && (node.getChildCount() == 0))
							node.add(new DefaultMutableTreeNode());
						m_view.getModel().fireNodeChanged(node);
						m_view.expandNode(node, false);
					}
				}
			}
		});

	}

}
