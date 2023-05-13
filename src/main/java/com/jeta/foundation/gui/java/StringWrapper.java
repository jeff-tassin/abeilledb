/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is used to display an object's attribute. We extend the definition
 * of attribute to include the String class as well as primitives.
 * 
 * @author Jeff Tassin
 */
public class StringWrapper implements JavaWrapper {
	private String m_name; // this is the name of the attribute (e.g. m_name or
							// m_icon )
	private String m_value; // this is the string value of the attribute
	private Icon m_icon; // this is an icon representing the type of attribute
	private String m_typename; // this is a string value for the type (e.g. int,
								// short, byte, String)
	private Field m_field; // this is the Field object that defines this
							// attribute
	private Object m_parent; // this is the object instance that contains the
								// given attribute

	/**
	 * ctor
	 * 
	 * @param field
	 *            the Field object that represents the attribute
	 * @param obj
	 *            the object that contains this attribute
	 */
	public StringWrapper(Field field, Object obj) {
		m_field = field;
		m_parent = obj;
		initialize();
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
		return m_value;
	}

	/**
	 * @return the type of the attribute as a string (e.g. int, short, char )
	 */
	public String getTypeName() {
		return m_typename;
	}

	/**
	 * Initializes this class from the given field and object
	 */
	void initialize() {
		if (m_field == null) {
			m_name = "String";
			m_value = (String) m_parent;
		} else {
			m_name = m_field.getName();
			try {
				m_value = (String) m_field.get(m_parent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		m_icon = TSGuiToolbox.loadImage("dtype_varchar16.gif");
		m_typename = "String";
	}

	/**
	 * Attributes never have children
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
		if (m_field == null)
			return false;
		else {
			int modifiers = m_field.getModifiers();
			return (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers));
		}
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
	public void setValue(Object obj) throws IllegalAccessException {
		m_value = (String) obj;
		if (m_field != null)
			m_field.set(m_parent, m_value);
	}
}
