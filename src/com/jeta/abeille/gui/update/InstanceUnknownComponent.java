package com.jeta.abeille.gui.update;

import java.util.*;
import java.text.*;
import java.sql.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.*;

import com.jeta.abeille.database.model.*;

/**
 * This class represents an unknown type for a table column. It behaves like a
 * text component, but does not allow editing
 * 
 * @author Jeff Tassin
 */
public class InstanceUnknownComponent extends BasicTextComponent {

	public InstanceUnknownComponent(String fieldName, int dataType) {
		super(fieldName, dataType);
		// remove the icon button that was added by the base class
		remove(getIconButton());
		getTextField().setEditable(false);
	}

	/**
	 * Sets the value represented by this component into the prepared statement
	 * 
	 * @param count
	 *            the parameter index to set
	 * @param pstmt
	 *            the prepared statement to act on
	 */
	public void prepareStatement(int count, PreparedStatement pstmt) throws SQLException {

	}

	/**
	 * @return a sql representation of this component value
	 */
	public String toSQLString() {
		return "null";
	}

	public void setValue(Object value) {
		super.setValue(value);
	}
}
