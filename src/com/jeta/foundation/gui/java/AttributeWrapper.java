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
public class AttributeWrapper implements JavaWrapper {
	private String m_name; // this is the name of the attribute (e.g. m_name or
							// m_icon )
	private String m_value; // this is the string value of the attribute
	private Icon m_icon; // this is an icon representing the type of attribute
	private String m_typename; // this is a string value for the type (e.g. int,
								// short, byte, String)
	private Field m_field; // this is the Field object that defines this
							// attribute
	private Object m_object; // this is the object instance that contains the
								// given attribute

	/**
	 * ctor
	 * 
	 * @param field
	 *            the Field object that represents the attribute
	 * @param obj
	 *            the object that contains this attribute
	 */
	public AttributeWrapper(Field field, Object obj) {
		m_field = field;
		m_object = obj;
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

	/** @return the actual object managed by the wrapper */
	public Object getObject() {
		return getPrimitiveObject(m_field.getType(), getDisplayValue());
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
		try {
			Class ftype = m_field.getType();
			m_name = m_field.getName();
			m_icon = getIcon(ftype);
			m_typename = getTypeName(ftype);
			if (ftype == boolean.class) {
				m_value = String.valueOf(m_field.getBoolean(m_object));
			} else if (ftype == char.class) {
				m_value = String.valueOf(m_field.getChar(m_object));
			} else if (ftype == byte.class) {
				m_value = String.valueOf(m_field.getByte(m_object));
			} else if (ftype == short.class) {
				m_value = String.valueOf(m_field.getShort(m_object));
			} else if (ftype == int.class) {
				m_value = String.valueOf(m_field.getInt(m_object));
			} else if (ftype == long.class) {
				m_value = String.valueOf(m_field.getLong(m_object));
			} else if (ftype == float.class) {
				m_value = String.valueOf(m_field.getFloat(m_object));
			} else if (ftype == double.class) {
				m_value = String.valueOf(m_field.getDouble(m_object));
			} else {
				assert (false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the name of the primitive type
	 */
	static String getTypeName(Class ftype) {
		String typename = null;

		if (ftype == boolean.class) {
			typename = "boolean";
		} else if (ftype == char.class) {
			typename = "char";
		} else if (ftype == byte.class) {
			typename = "byte";
		} else if (ftype == short.class) {
			typename = "short";
		} else if (ftype == int.class) {
			typename = "int";
		} else if (ftype == long.class) {
			typename = "long";
		} else if (ftype == float.class) {
			typename = "float";
		} else if (ftype == double.class) {
			typename = "double";
		} else {
			assert (false);
		}
		return typename;
	}

	/**
	 * @return an icon for the given primitive
	 */
	static Icon getIcon(Class ftype) {
		if (!ftype.isPrimitive() && ftype != String.class) {
			System.out.println("bad attribute type: " + ftype);
			assert (false);
		}
		ImageIcon icon = null;

		if (ftype == boolean.class) {
			icon = TSGuiToolbox.loadImage("dtype_bool16.gif");
		} else if (ftype == char.class) {
			icon = TSGuiToolbox.loadImage("dtype_char16.gif");
		} else if (ftype == byte.class) {
			icon = TSGuiToolbox.loadImage("dtype_char16.gif");
		} else if (ftype == short.class) {
			icon = TSGuiToolbox.loadImage("dtype_smallint16.gif");
		} else if (ftype == int.class) {
			icon = TSGuiToolbox.loadImage("dtype_int16.gif");
		} else if (ftype == long.class) {
			icon = TSGuiToolbox.loadImage("dtype_bigint16.gif");
		} else if (ftype == float.class) {
			icon = TSGuiToolbox.loadImage("dtype_real16.gif");
		} else if (ftype == double.class) {
			icon = TSGuiToolbox.loadImage("dtype_decimal16.gif");
		} else {
			TSUtils._assert(false);
		}

		return icon;
	}

	/**
	 * Given a string and a primitive class type, get the corresponding
	 * primitive wrapper type (i.e. char to Character )
	 */
	static Object getPrimitiveObject(Class ftype, String sValue) {
		Object result = null;

		if (ftype == boolean.class) {
			result = new Boolean(sValue);
		} else if (ftype == char.class) {
			result = new Character(sValue.charAt(0));
		} else if (ftype == byte.class) {
			result = new Byte(sValue);
		} else if (ftype == short.class) {
			result = new Short(sValue);
		} else if (ftype == int.class) {
			result = new Integer(sValue);
		} else if (ftype == long.class) {
			result = new Long(sValue);
		} else if (ftype == float.class) {
			result = new Float(sValue);
		} else if (ftype == double.class) {
			result = new Double(sValue);
		} else {
			assert (false);
		}

		return result;
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
		int modifiers = m_field.getModifiers();
		return (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers));
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
			String sValue = (String) obj;
			Class ftype = m_field.getType();
			Object pobj = getPrimitiveObject(ftype, sValue);
			m_field.set(m_object, pobj);
			m_value = pobj.toString();
		} catch (IllegalAccessException ie) {
			ie.printStackTrace();
		}

	}
}
