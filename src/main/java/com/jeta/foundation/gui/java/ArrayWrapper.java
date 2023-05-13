/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.awt.Toolkit;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class is used to display an array object in the ObjectTree.
 * 
 * @author Jeff Tassin
 */
public class ArrayWrapper implements JavaWrapper {
	private String m_name;
	private Object m_array;
	private String m_typename;
	private Icon m_icon;
	private boolean m_loaded;
	private Class m_arrayclass;

	private Field m_field; // the field object that represents this object
	private Object m_parent; // the object that contains the array

	/**
	 * ctor
	 * 
	 * @param field
	 *            the field that describes the object
	 * @param obj
	 *            the object that contains the given field (and hence this
	 *            object)
	 */
	public ArrayWrapper(Field field, Object parentObject) {
		m_field = field;
		m_parent = parentObject;
		m_name = field.getName();
		Class ftype = field.getType();
		assert (ftype.isArray());
		m_arrayclass = ftype.getComponentType();
		try {
			m_array = field.get(parentObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		m_icon = TSGuiToolbox.loadImage("javaarray16.gif");
		m_loaded = false;

		if (m_array == null)
			m_typename = m_arrayclass.getName() + "[]";
		else
			m_typename = m_arrayclass.getName() + "[" + Array.getLength(m_array) + "]";
	}

	/**
	 * Invokes a dialog that allows the user to set the array length or set the
	 * array to null
	 */
	public void edit() {
		String arraylength = javax.swing.JOptionPane.showInputDialog(null,
				I18N.getLocalizedMessage("Input Array Length (Max)"));
		if (arraylength != null) {
			try {
				short len = Short.parseShort(arraylength);
				Object newobj = Array.newInstance(m_arrayclass, len);

				if (m_array != null) {
					int currentlen = Array.getLength(m_array);
					if (currentlen < len)
						System.arraycopy(m_array, 0, newobj, 0, currentlen);
					else
						System.arraycopy(m_array, 0, newobj, 0, len);
				}

				System.out.println("created new array: " + len + "  type = " + m_arrayclass);
				m_array = newobj;
				m_loaded = false;
				m_typename = m_arrayclass.getName() + "[" + Array.getLength(m_array) + "]";

				m_field.set(m_parent, m_array);
			} catch (Exception e) {
				e.printStackTrace();
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}

	/**
	 * @return the icon for this wrapper as displayed in the tree
	 */
	public Icon getIcon() {
		return m_icon;
	}

	/**
	 * @return the field name for the variable holding this wrapper value
	 */
	public String getName() {
		return m_name;
	}

	public Object getObject() {
		return m_array;
	}

	public String getTypeName() {
		return m_typename;
	}

	/**
    * 
    */
	public String getDisplayValue() {
		if (m_array == null)
			return null;
		else
			return "";
	}

	/**
	 * @return true if this node has children
	 */
	public boolean hasChildren() {
		if (m_array == null)
			return false;
		else
			return (Array.getLength(m_array) > 0);
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	/** @return true if this wrapper is readonly. No in place editing */
	public boolean isReadOnly() {
		return true;
	}

	/**
	 * Loads the child objects for this wrapper
	 */
	public void loadChildren(DefaultMutableTreeNode parent) {
		m_loaded = true;
		if (m_array != null) {
			int len = Array.getLength(m_array);
			for (int index = 0; index < len; index++) {
				if (m_arrayclass.isPrimitive()) {
					ArrayAttributeWrapper wrapper = new ArrayAttributeWrapper(m_array, index);
					DefaultMutableTreeNode node = new DefaultMutableTreeNode();
					node.setUserObject(wrapper);
					parent.add(node);
				} else {
					ArrayObjectWrapper wrapper = new ArrayObjectWrapper(m_array, index);
					DefaultMutableTreeNode node = new DefaultMutableTreeNode();
					node.setUserObject(wrapper);
					parent.add(node);
				}
			}
		}
	}

	/**
	 * Sets the value for this object
	 */
	public void setValue(Object obj) {

	}

}
