package com.jeta.abeille.gui.update;

import java.util.*;
import java.text.*;
import java.sql.*;

import java.awt.*;
import javax.swing.*;

import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.abeille.database.model.*;

import com.jeta.abeille.database.utils.SQLFormatter;

/**
 * This class is the base class for components in the update frame
 * [icon][JTextField]
 * 
 * @author Jeff Tassin
 */
public abstract class InstanceComponent extends TSPanel {
	/** if this component has an icon to the left */
	private JButton m_iconbtn;

	/**
	 * the component that displays the value for the given column in the
	 * database
	 */
	private JComponent m_component;

	/** the value */
	private Object m_value;

	/** flag that indicates whether the user modified this value or not */
	private boolean m_modified;

	/**
	 * this is the name of the field(column) that this component represents in
	 * the view
	 */
	private String m_fieldname;

	/** the data type of the column */
	private int m_data_type;

	/** this property is fired when the component has been modified */
	public static final String MODIFIED_PROPERTY = "modified";

	public InstanceComponent(String fieldName, int data_type) {
		m_fieldname = fieldName;
		m_data_type = data_type;
		setLayout(new InstanceComponentLayoutManager());
	}

	/**
	 * Clears the current control
	 */
	public void clear() {
		setValue(null);
	}

	/**
	 * @return the component that is responsible for containing or rendering the
	 *         database value on the screen. In some cases, this could be a
	 *         scroll pane instead of the actual component.
	 */
	public JComponent getComponent() {
		return m_component;
	}

	/**
	 * @return the component that actually renders the data value. Some
	 *         components are contained in a scroll pane. If that is the case,
	 *         the properly gets the component contained in the scroll
	 */
	public JComponent getVisualComponent() {
		return getComponent();
	}

	/**
	 * @return the x location of the component
	 */
	public int getComponentX() {
		return 17; // always 17 to account for icon
	}

	/**
	 * @return the data type for this column
	 */
	public int getDataType() {
		return m_data_type;
	}

	/**
	 * @return the name of the field(column) that this component is bound to in
	 *         the view
	 */
	public String getFieldName() {
		return m_fieldname;
	}

	/**
	 * @return the icon button next to the text field
	 */
	public JButton getIconButton() {
		return m_iconbtn;
	}

	/**
	 * @return the parent window object that owns this component. This call
	 *         traverses the parent hierarchy until a Window is found
	 */
	public Window getParentWindow() {

		Component c = getParent();
		while (c != null && !(c instanceof Window)) {
			c = c.getParent();
		}

		if (c instanceof Window)
			return (Window) c;
		else
			return null;
	}

	/**
	 * @return the preferred size for this component
	 */
	public Dimension getPreferredSize() {
		Dimension d = getComponent().getPreferredSize();
		d.width += getComponentX();
		return d;
	}

	/**
	 * @return the current value object for this component
	 */
	public Object getValue() {
		return m_value;
	}

	/**
	 * @return true if this value was modified by the user
	 */
	public boolean isModified() {
		return m_modified;
	}

	public boolean isNull() {
		return (getValue() == null);
	}

	/**
	 * Writes the components value into the prepared statement using the
	 * formatting logic specific for the component.
	 * 
	 * @param count
	 *            the (1-based) count which the component is based in the
	 *            prepared statement
	 * @param pstmt
	 *            the prepared statement to write to
	 * @param formatter
	 *            the SQL formatter that controls any formatting (this can be
	 *            vendor specific)
	 */
	abstract public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter)
			throws SQLException;

	/**
	 * Sets the JComponent for this Frame component.
	 */
	public void setComponent(JComponent comp) {
		m_component = comp;
		add(comp);
	}

	/**
	 * Sets the icon for this frame component
	 */
	public void setIcon(String iconName) {
		if (m_iconbtn == null) {
			m_iconbtn = new JButton();
			m_iconbtn.setSize(16, 16);
			m_iconbtn.setFocusPainted(false);
			m_iconbtn.setBorderPainted(false);
			m_iconbtn.setOpaque(false);
			m_iconbtn.setContentAreaFilled(false);
			m_iconbtn.setFocusable(false);
			add(m_iconbtn);
		}

		m_iconbtn.setIcon(TSGuiToolbox.loadImage(iconName));
	}

	public void setModified(boolean bModified) {
		boolean oldval = m_modified;
		m_modified = bModified;
		firePropertyChange(MODIFIED_PROPERTY, oldval, m_modified);
	}

	public void setValue(Object obj) {
		m_value = obj;
	}

	/**
	 * Sets the width for this component. This width corresponds to the width of
	 * the parent frame window. Some specializations of this class may want to
	 * set their width to the frame width. They can override this method if they
	 * want that behavoir.
	 * 
	 * @param width
	 *            this is the width to set the component if you want to have it
	 *            resized with the parent window.
	 */
	public void setWidth(int width) {
	}

	/**
	 * Gets the value that is displayed in the component and calls setValue on
	 * this object.
	 */
	public abstract void syncValue();

	public String toString() {
		Object val = getValue();
		if (val != null)
			return val.toString();
		else
			return null;
	}

	/**
	 * @return the value of this component as it would be represented in a SQL
	 *         statement. For integral types, it is just the value converted to
	 *         text.
	 */
	public String toSQLString(SQLFormatter formatter) {
		if (isNull())
			return "null";
		else
			return toString();
	}

	/**
	 * The layout manager for this component
	 */
	class InstanceComponentLayoutManager implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			JComponent component = getComponent();
			JComponent iconbtn = getIconButton();

			Dimension d = getPreferredSize();
			d.width = d.width - getComponentX();
			component.setSize(d);

			if (iconbtn != null) {

				Dimension btnd = iconbtn.getSize();
				Dimension fd = component.getSize();
				if (fd.height > btnd.height) {
					// iconbtn.setLocation(0, (fd.height - btnd.height)/2);
					iconbtn.setLocation(0, 0);
					component.setLocation(getComponentX(), 0);
				} else {
					component.setLocation(getComponentX(), (btnd.height - fd.height) / 2);
					iconbtn.setLocation(0, 0);
				}
			} else {
				component.setLocation(getComponentX(), 0);
			}

		}

		public Dimension minimumLayoutSize(Container parent) {
			return preferredLayoutSize(parent);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return getPreferredSize();
		}

		public void removeLayoutComponent(Component comp) {
		}

	}

}
