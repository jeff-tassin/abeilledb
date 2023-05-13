package com.jeta.abeille.gui.update;

import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.text.DecimalFormat;

import java.util.Calendar;

import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.foundation.gui.components.TSTimeField;
import com.jeta.foundation.gui.components.TSTimeSpinner;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class represents a date component in the UpdateFrame This component has
 * an icon and a text field [icon][TSTimeSpinner]
 * 
 * @author Jeff Tassin
 */
public class UpdateTimeComponent extends InstanceComponent implements ActionListener {
	public UpdateTimeComponent(String fieldName, int dataType) {
		super(fieldName, dataType);

		TSTimeSpinner field = new TSTimeSpinner() {
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				TSTimeField tmfield = getTimeField();
				Dimension dc = tmfield.getPreferredSize();
				d.width = dc.width + d.height;
				return d;
			}
		};

		field.getTimeField().addActionListener(this);
		setComponent(field);
		clear();
	}

	/**
	 * Called in response to time change events from the time field
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(TSTimeField.VALUE_CHANGE_EVENT)) {
			setModified(true);
		}
	}

	/**
	 * Clears the current control
	 */
	public void clear() {
		TSTimeField timefield = getTimeField();
		timefield.setNow();
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
		if (isNull()) {
			pstmt.setNull(count, java.sql.Types.TIME);
		} else {
			TSTimeField field = getTimeField();
			// System.out.println( "UpdateTimeComponet setting time:  hours = "
			// + field.getHours() );
			Calendar c = field.getCalendar();
			java.sql.Time result = new java.sql.Time(c.getTimeInMillis());
			// System.out.println( "UpdateTimeComponet setting time: " + result
			// );
			pstmt.setTime(count, result);
		}
	}

	/**
	 * Helper routine to convert to an SQL string. This is in TIME('HH:MM:SS')
	 * format
	 */
	public String toSQLString(SQLFormatter formatter) {
		TSTimeField field = getTimeField();
		return UpdateTimeComponent.toSQLString(field);
	}

	/**
	 * @return the sql representation of this time. This is in TIME('HH:MM:SS')
	 */
	public static String toSQLString(TSTimeField field) {
		if (field.isNull())
			return "null";

		DecimalFormat format = new DecimalFormat("00");
		StringBuffer sql = new StringBuffer();
		sql.append("TIME('");
		sql.append(format.format(field.getHours()));
		sql.append(":");
		sql.append(format.format(field.getMinutes()));
		sql.append(":");
		sql.append(format.format(field.getSeconds()));
		sql.append("')");
		return sql.toString();
	}

	TSTimeField getTimeField() {
		TSTimeSpinner spinner = (TSTimeSpinner) getComponent();
		return spinner.getTimeField();
	}

	/**
	 * @return true if the underlying time field is null
	 */
	public boolean isNull() {
		return getTimeField().isNull();
	}

	/**
	 * Sets the value for the given time field. We assume that the value is
	 * always a java.sql.Time object.
	 */
	public void setValue(Object value) {
		super.setValue(value);

		// @todo we probably need to move this to a calendar implementation
		TSTimeField timefield = getTimeField();
		if (value == null) {
			timefield.setNull(true);
		} else {
			timefield.setNull(false);
			java.sql.Time tm = (java.sql.Time) value;
			Calendar c = Calendar.getInstance();
			c.setTime(tm);
			timefield.set(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
		}
		setModified(false);
	}

	/**
	 * Gets the value that is displayed in the component and calls setValue on
	 * this object.
	 */
	public void syncValue() {
		// ignore here
	}

}
