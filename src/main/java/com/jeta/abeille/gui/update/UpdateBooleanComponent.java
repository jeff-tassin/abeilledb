package com.jeta.abeille.gui.update;

import javax.swing.*;
import java.util.*;
import java.text.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;

import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.database.model.*;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a bit component in the UpdateFrame
 * 
 * @author Jeff Tassin
 */
public class UpdateBooleanComponent extends InstanceComponent {
	// these are the possible states for this component. note that
	// a boolean data type can have a null value, so we need that state
	private static final int TRUE = 1;
	private static final int FALSE = 0;
	private static final int NULL = 2;
	private int m_state; // the current state of the component

	private static final String TRUE_STR = I18N.getLocalizedMessage("true");
	private static final String FALSE_STR = I18N.getLocalizedMessage("false");

	public UpdateBooleanComponent(String fieldName, int dataType) {
		super(fieldName, dataType);
		// JCheckBox field = TSGuiToolbox.createCheckBox( "" );
		JCheckBox field = new JCheckBox("");
		field.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		JTextField txtfield = new JTextField();
		field.setFont(txtfield.getFont());
		setComponent(field);

	}

	/**
	 * We override so we can set our height to the same as a text field This
	 * makes it look a little nicer on the form
	 */
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		// JTextField txtfield = new JTextField();
		// d.height = txtfield.getPreferredSize().height;
		// System.out.println(
		// "UpdateBooleanComponent getPreferredSize  height = " + d.height );
		return d;
	}

	/**
	 * @return null if the current value is null
	 */
	public boolean isNull() {
		return getValue() == null;
	}

	/**
	 * Sets the value represented by this component into the prepared statement
	 * 
	 * @param count
	 *            the parameter index to set
	 * @param pstmt
	 *            the prepared statement to act on
	 */
	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		if (m_state == NULL) {
			pstmt.setNull(count, getDataType());
		} else {
			JCheckBox cbox = (JCheckBox) getComponent();
			pstmt.setBoolean(count, cbox.isSelected());
		}
	}

	/**
	 * Cycles to the next state for the checkbox States true --> false --> null
	 * --> true
	 */
	public void setNextState() {
		if (m_state == TRUE)
			setState(FALSE);
		else if (m_state == FALSE)
			setState(NULL);
		else
			setState(TRUE);
	}

	/**
	 * Sets the current state for this component. Updates the text in the
	 * checkbox as well as the checkbox selected state
	 */
	private void setState(int state) {
		JCheckBox cbox = (JCheckBox) getComponent();
		m_state = state;
		if (state == TRUE) {
			cbox.setSelected(true);
			cbox.setText(TRUE_STR);
		} else if (state == FALSE) {
			cbox.setSelected(false);
			cbox.setText("null");
		} else {
			cbox.setSelected(false);
			cbox.setText(FALSE_STR);
		}
	}

	/**
	 * Sets the value for the given date field. We assume that the value is
	 * always a Boolean.sql.Date object
	 */
	public void setValue(Object value) {
		super.setValue(value);
		setModified(false);
		if (value == null) {
			setState(NULL);
		} else {
			boolean bval = ((Boolean) value).booleanValue();
			if (bval)
				setState(TRUE);
			else
				setState(FALSE);
		}
	}

	/**
	 * Gets the value that is displayed in the component and calls setValue on
	 * this object.
	 */
	public void syncValue() {
		// ignore here
		JCheckBox cbox = (JCheckBox) getComponent();
		if (cbox.isSelected()) {
			setValue(Boolean.TRUE);
		} else {
			if (cbox.getText().equals("null"))
				setValue(null);
			else
				setValue(Boolean.FALSE);
		}
	}

	/**
	 * Override JCheckBox so we can implement our own state semantics
	 * 
	 * @todo override keyboard handling
	 */
	class CheckBox extends JCheckBox {
		protected void processMouseEvent(MouseEvent evt) {
			if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
				setNextState();
				// don't forward mouse events to superclass
				setModified(true);
			}
		}
	}

}
