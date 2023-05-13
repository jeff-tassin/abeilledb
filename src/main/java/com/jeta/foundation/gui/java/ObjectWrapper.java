/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.lang.reflect.Field;
import javax.swing.Icon;

import javax.swing.tree.DefaultMutableTreeNode;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class is used to display an object's name in the ObjectPanel.
 * 
 * @author Jeff Tassin
 */
public class ObjectWrapper implements JavaWrapper {
	private String m_name;
	private Object m_object;
	private String m_typename;
	private Icon m_icon;
	private boolean m_loaded;
	private Field m_field; // the field object that represents this object
	private Class m_class;
	private Object m_parent;

	/**
	 * ctor
	 * 
	 * @param field
	 *            the field that describes the object
	 * @param obj
	 *            the object that contains the given field (and hence this
	 *            object)
	 */
	public ObjectWrapper(Field field, Object object) {
		m_field = field;
		m_parent = object;

		m_name = field.getName();
		m_class = field.getType();
		String classname = m_class.getName();
		Package pkg = m_class.getPackage();

		m_typename = classname;
		if (pkg != null) {
			String pkgname = pkg.getName();
			int pos = classname.indexOf(pkgname);
			if (pos >= 0) {
				m_typename = classname.substring(pkgname.length() + 1, classname.length());
			}
		}

		try {
			m_object = field.get(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
		m_icon = TSGuiToolbox.loadImage("javaobject16.gif");
		m_loaded = false;

	}

	/**
	 * Invokes a dialog that allows the user to create a new object or set the
	 * current object to null.
	 */
	public void edit() {
		String classname = javax.swing.JOptionPane.showInputDialog(null, I18N.getLocalizedMessage("Input Class Name"),
				getTypeName());
		if (classname != null) {
			try {
				Class c = Class.forName(classname);
				Object obj = c.newInstance();
				setValue(obj);
				m_field.set(m_parent, obj); // the field object that represents
											// this object
				m_object = obj;
				if (obj != null)
					m_class = m_object.getClass();

				m_loaded = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Icon getIcon() {
		return m_icon;
	}

	public String getName() {
		return m_name;
	}

	public String getDisplayValue() {
		if (m_object == null)
			return null;
		else
			return m_object.toString();
	}

	public Object getObject() {
		return m_object;
	}

	public String getTypeName() {
		return m_typename;
	}

	public boolean hasChildren() {
		if (m_object == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	/**
	 * Loads the child objects for this wrapper
	 */
	public void loadChildren(DefaultMutableTreeNode parent) {
		m_loaded = true;
		System.out.println("ObjectWrapper.loadChildren   name = " + getName() + "  " + m_object);
		Class c = m_class;
		if (m_object != null)
			c = m_object.getClass();
		JavaTreeBuilder.buildNode(parent, m_object, c);
	}

	/** @return true if this wrapper is readonly. No in place editing */
	public boolean isReadOnly() {
		return true;
	}

	/**
	 * Sets the value for this object
	 */
	public void setValue(Object obj) throws IllegalAccessException {

	}
}
