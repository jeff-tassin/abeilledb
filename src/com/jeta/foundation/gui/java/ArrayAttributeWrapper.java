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
 * This class is used to handle a single primitive in an array of primitives at
 * a given index.
 * 
 * @author Jeff Tassin
 */
public class ArrayAttributeWrapper implements JavaWrapper {
	private String m_name; // this is the name of the attribute (e.g. m_name or
							// m_icon )
	private String m_value; // this is the string value of the attribute

	private Icon m_icon; // this is an icon representing the type of attribute

	private String m_typename; // this is a string value for the type (e.g. int,
								// short, byte, String)
	private Object m_array; // this is the object instance that contains the
							// given attribute
	private Class m_class;
	private int m_index;

	/**
	 * ctor
	 */
	public ArrayAttributeWrapper(Object array, int index) {
		m_array = array;
		m_index = index;
		assert (m_array != null);

		Class ftype = m_array.getClass();
		assert (ftype.isArray());
		m_class = ftype.getComponentType();
		m_icon = AttributeWrapper.getIcon(m_class);
		m_name = AttributeWrapper.getTypeName(m_class) + "[" + index + "]";
		m_typename = null;

		m_value = String.valueOf(Array.get(m_array, index));
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
		return m_value;
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
		try {
			String sval = (String) obj;
			Object pobj = AttributeWrapper.getPrimitiveObject(m_class, sval);
			Array.set(m_array, m_index, pobj);
			m_value = String.valueOf(Array.get(m_array, m_index));
		} catch (IllegalArgumentException ie) {
			ie.printStackTrace();
		}
	}
}
