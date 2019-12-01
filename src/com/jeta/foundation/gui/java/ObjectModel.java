/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.jeta.foundation.gui.treetable.AbstractTreeTableModel;
import com.jeta.foundation.gui.treetable.TreeTableModel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This is the JTableTree view for an arbitrary Java object. It shows attributes
 * and nested java objects. Each nested Java object can be exanded as well The
 * JTree Table is organized as follows: column 1 column 2 root -attribute(1)
 * value -attribute(2) value -Java object(1) -attribute(1) value -attribute(2)
 * value -Java object(2) -attribute(1) value
 * 
 * @author Jeff Tassin
 */
public class ObjectModel extends AbstractTreeTableModel implements TreeTableModel {
	static protected String[] m_columnNames;
	static protected Class[] m_columnTypes;

	/** the object that we are displaying in the tree table */
	private Object m_object;

	/**
	 * a set of listeners (ActionListeners) that get notifications when the
	 * model changes
	 */
	private LinkedList m_listeners = new LinkedList();

	/** command for action events */
	public final static String CHANGE_EVENT = "objectmodel.change.event";

	private boolean m_allownotify = true;

	/**
	 * The flag that indicates if this model's cells are in-place editable
	 */
	private boolean m_editable = true;

	static {
		String[] cnames = { I18N.getLocalizedMessage("Member"), I18N.getLocalizedMessage("Value") };
		m_columnNames = cnames;
		Class[] ctypes = { TreeTableModel.class, String.class };
		m_columnTypes = ctypes;
	}

	/**
	 * ctor
	 */
	public ObjectModel(Object obj) {
		super(new DefaultMutableTreeNode());
		initialize(obj);
	}

	/**
	 * Add a generic action listener to get any type of events that result in a
	 * change to the model. The action event will have a command of
	 * ObjectModel.CHANGE_EVENT
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Notify the gui that the model has changed
	 */
	public void fireNodeChanged(DefaultMutableTreeNode node) {
		fireTreeStructureChanged(this, node.getPath(), null, null);
	}

	/**
	 * @return the number of children owned by a particular node
	 */
	public int getChildCount(Object node) {
		DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) node;
		return tnode.getChildCount();
	}

	/**
	 * @return the Nth child of the given node
	 */
	public Object getChild(Object node, int i) {
		DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) node;
		if (i < tnode.getChildCount())
			return tnode.getChildAt(i);
		else
			return null;
	}

	/**
	 * @return the number of columns in the table
	 */
	public int getColumnCount() {
		return m_columnNames.length;
	}

	/**
	 * @param column
	 *            the column whose name we wish to retrieve
	 * @return the column name for the given column
	 */
	public String getColumnName(int column) {
		return m_columnNames[column];
	}

	/**
	 * @param column
	 *            the column whose class we wish to retrieve
	 * @return the column class for the given column
	 */
	public Class getColumnClass(int column) {
		return m_columnTypes[column];
	}

	/**
	 * @return the edited object
	 */
	public byte[] getBytes() throws IOException {
		// DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
		// assert( root.getChildCount() == 1 );
		// DefaultMutableTreeNode objnode =
		// (DefaultMutableTreeNode)root.getChildAt(0);
		// Object obj = objnode.getUserObject();

		Object obj = m_object;
		if (m_object instanceof StringWrapper) {
			StringWrapper wrapper = (StringWrapper) m_object;
			obj = wrapper.getObject();
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		return bos.toByteArray();
	}

	/**
	 * @return the underlying root object of the model
	 */
	public Object getObject() {
		return m_object;
	}

	/**
	 * @return the value at the given column and given node
	 */
	public Object getValueAt(Object node, int column) {
		DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) node;
		Object obj = treenode.getUserObject();

		if (obj instanceof JavaWrapper) {
			JavaWrapper wrapper = (JavaWrapper) obj;
			if (column == 0)
				return wrapper.getName();
			else if (column == 1)
				return wrapper.getDisplayValue();
		} else
			return "";

		/*
		 * if ( obj instanceof AttributeWrapper ) { AttributeWrapper wrapper =
		 * (AttributeWrapper)obj; if ( column == 0 ) { return wrapper.getName();
		 * } else if ( column == 1 ) return wrapper.getValue(); } else if ( obj
		 * instanceof ObjectWrapper ) { ObjectWrapper wrapper =
		 * (ObjectWrapper)obj; if ( column == 0 ) return wrapper.getName(); else
		 * if ( column == 1 ) { if ( wrapper.getValue() == null ) return "null";
		 * else return ""; } }
		 */

		return "";

	}

	/**
	 * Initializes the model with the given object
	 */
	private void initialize(Object obj) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();

		if (obj == null) {
			m_object = obj;
		} else {
			if (obj.getClass() == String.class)
				m_object = new StringWrapper(null, obj); // special case for
															// string wrapper
			else
				m_object = obj;

			loadObject(root, obj, obj.getClass());
		}
	}

	/**
	 * Traverses the tree path to determine if the given user object is part of
	 * that path.
	 * 
	 * @return true if the user object is found in the tree path
	 */
	public boolean isAncestor(DefaultMutableTreeNode node, Object userobj) {
		// System.out.println( "********** isAncestor ************ " );
		TreeNode[] path = node.getPath();
		for (int index = 0; index < path.length; index++) {
			DefaultMutableTreeNode pathnode = (DefaultMutableTreeNode) path[index];
			Object obj = pathnode.getUserObject();
			// System.out.println( "    pathobj = " + obj + "   userobj = " +
			// userobj );
			// if ( obj instanceof ObjectWrapper &&
			// ((ObjectWrapper)obj).getObject() == userobj )
			// return true;
		}
		return false;
	}

	/**
	 * By default, make all columns in the tree editable
	 */
	public boolean isCellEditable(Object node, int column) {
		if (m_editable) {
			if (getColumnClass(column) == TreeTableModel.class)
				return true;

			boolean bresult = false;
			if (node instanceof DefaultMutableTreeNode) {
				Object userobj = ((DefaultMutableTreeNode) node).getUserObject();
				JavaWrapper wrapper = (JavaWrapper) userobj;
				bresult = !wrapper.isReadOnly();
			}
			return bresult;
		} else
			return false;
	}

	/**
	 * Loads an object into the tree
	 */
	void loadObject(DefaultMutableTreeNode parent, Object obj, Class c) {
		try {

			JavaTreeBuilder.buildNode(parent, obj, c);

			// now get all fields from inherited classes
			c = c.getSuperclass();
			if (c != null && c != Object.class) {
				loadObject(parent, obj, c);
			}
		} catch (Exception e) {
			System.out.println("   class = " + c.getName() + "   obj = " + obj);
			e.printStackTrace();
		}
	}

	/**
	 * Notifies all listeners that a model change event occurred
	 */
	public void notifyListeners() {
		if (m_allownotify) {
			ActionEvent evt = new ActionEvent(this, 0, CHANGE_EVENT);
			Iterator iter = m_listeners.iterator();
			while (iter.hasNext()) {
				ActionListener listener = (ActionListener) iter.next();
				listener.actionPerformed(evt);
			}
		}
	}

	/**
	 * Removes a previously added listener
	 */
	public void removeActionListener(ActionListener listener) {
		m_listeners.remove(listener);
	}

	/**
	 * Sets the flag that indicates if this model is in-place editable
	 */
	public void setEditable(boolean bedit) {
		m_editable = bedit;
	}

	/**
	 * Sets the object for this model and reloads the view
	 */
	public void setObject(Object obj) {
		m_allownotify = false;
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
		root.removeAllChildren();
		initialize(obj);
		fireNodeChanged(root);
		m_allownotify = true;
	}

	/**
	 * Called when in place edited by user
	 */
	public void setValueAt(Object aValue, Object node, int column) {
		if (column == 1 && node instanceof DefaultMutableTreeNode) {
			// aValue should always be a String object
			DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) node;
			Object userobj = tnode.getUserObject();
			if (userobj instanceof JavaWrapper) {
				JavaWrapper wrapper = (JavaWrapper) userobj;
				try {
					wrapper.setValue((String) aValue);
				} catch (Exception e) {
					TSGuiToolbox.showErrorDialog(null, e.getLocalizedMessage(), I18N.getLocalizedMessage("Error"));
				}
			}
		}
	}

	static class MyCircle {
		private int m_radius = 2;
		private boolean m_bool = true;
	}

	static class MyRect extends java.awt.Rectangle {
		int m_test = 5;
		private MyCircle m_cir = new MyCircle();

		public MyRect(int x, int y, int width, int height) {
			super(x, y, width, height);
		}
	}

}
