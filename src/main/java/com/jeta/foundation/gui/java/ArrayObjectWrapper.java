/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.Icon;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is used to handle a single object in an array of objects at a
 * given index.
 * 
 * @author Jeff Tassin
 */
public class ArrayObjectWrapper implements JavaWrapper {
	private String m_name; // this is the name of the attribute (e.g. m_name or
							// m_icon )

	private String m_typename; // this is a string value for the type (e.g. int,
								// short, byte, String)
	private Class m_class;

	private Object m_array; // this is the array instance that contains the
							// given attribute
	private int m_index; // the index in the array
	private Icon m_icon;

	private ObjectWrapper m_objectwrapper;

	/**
	 * ctor
	 */
	public ArrayObjectWrapper(Object array, int index) {
		m_array = array;
		m_index = index;
		assert (m_array != null);

		Class ftype = m_array.getClass();
		assert (ftype.isArray());
		m_class = ftype.getComponentType();

		m_icon = TSGuiToolbox.loadImage("javaobject16.gif");
		m_name = m_class.getName() + "[" + index + "]";
		m_typename = null;
	}

	/**
	 * For the attribute wrapper we just ignore edit since we allow in cell
	 * editing
	 */
	public void edit() {
		// no op
	}

	/**
	 * @return the icon that indicates the type of attribute
	 */
	public Icon getIcon() {
		return m_icon;
	}

	/**
	 * @return the name of the attribute (e.g. m_icon or m_lastname )
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return the string value of the attribute
	 */
	public String getDisplayValue() {
		Object obj = getObject();
		if (obj == null)
			return "null";
		else
			return obj.toString();
	}

	public Object getObject() {
		return Array.get(m_array, m_index);
	}

	/**
	 * @return the type of the attribute as a string (e.g. int, short, char )
	 */
	public String getTypeName() {
		return m_typename;
	}

	/**
	 * No children
	 */
	public boolean hasChildren() {
		return false;
	}

	public boolean isLoaded() {
		return true;
	}

	/**
	 * @return true if the given object is readonly ( final or static )
	 */
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * Loads the child objects for this wrapper
	 */
	public void loadChildren(DefaultMutableTreeNode parent) {
		// no op
	}

	/**
	 * Sets the value for this attribute
	 */
	public void setValue(Object obj) {
	}
}
