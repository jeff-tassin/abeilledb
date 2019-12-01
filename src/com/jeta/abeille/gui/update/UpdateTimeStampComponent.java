package com.jeta.abeille.gui.update;

import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.text.DecimalFormat;

import java.util.Calendar;

import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.foundation.gui.components.TSTimeField;
import com.jeta.foundation.gui.components.TSTimeStampField;
import com.jeta.foundation.gui.components.TSTimeStampSpinner;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class represents a date component in the UpdateFrame This component has
 * an icon and a text field [icon][TSCalendarField]
 * 
 * @author Jeff Tassin
 */
public class UpdateTimeStampComponent extends InstanceComponent implements ActionListener {
	public UpdateTimeStampComponent(String fieldName, int dataType) {
		super(fieldName, dataType);

		TSTimeStampSpinner field = new TSTimeStampSpinner() {
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				TSTimeStampField tmfield = getTimeStampField();
				Dimension dc = tmfield.getPreferredSize();
				d.width = dc.width + d.height;
				return d;
			}
		};

		field.getTimeStampField().addActionListener(this);
		setComponent(field);
		setIcon("incors/16x16/calendar.png");
		getIconButton().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				DateDialog dlg = (DateDialog) TSGuiToolbox.createDialog(DateDialog.class,
						UpdateTimeStampComponent.this, true);
				TSTimeStampField tsfield = getTimeStampField();
				dlg.setCalendar(tsfield.getCalendar());

				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
				if (dlg.isOk()) {
					Calendar c = dlg.getCalendar();
					tsfield.setDay(c.get(Calendar.DAY_OF_MONTH));
					tsfield.setMonth(c.get(Calendar.MONTH));
					tsfield.setYear(c.get(Calendar.YEAR));
					setModified(true);
				}
			}
		});

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
		TSTimeStampField tsfield = getTimeStampField();
		tsfield.setNow();
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
			pstmt.setNull(count, java.sql.Types.TIMESTAMP);
		} else {
			TSTimeStampField field = getTimeStampField();
			Calendar c = field.getCalendar();
			java.sql.Timestamp result = new java.sql.Timestamp(c.getTimeInMillis());
			result.setNanos(0);
			pstmt.setTimestamp(count, result);
		}
	}

	/**
	 * Return an SQL representation of a time stamp
	 */
	public String toSQLString(SQLFormatter formatter) {
		if (isNull())
			return "null";

		TSTimeStampField field = getTimeStampField();

		DecimalFormat format = new DecimalFormat("00");
		StringBuffer sql = new StringBuffer();
		sql.append("TIMESTAMP( ");

		format = new DecimalFormat("0000");
		sql.append("DATE('");
		sql.append(format.format(field.getYear()));
		sql.append("-");
		format = new DecimalFormat("00");
		sql.append(format.format(field.getMonth() + 1));
		sql.append("-");
		sql.append(format.format(field.getDay()));
		sql.append("'), ");

		sql.append("TIME('");
		sql.append(format.format(field.getHours()));
		sql.append(":");
		sql.append(format.format(field.getMinutes()));
		sql.append(":");
		sql.append(format.format(field.getSeconds()));
		sql.append("'))");

		return sql.toString();

	}

	public TSTimeStampField getTimeStampField() {
		TSTimeStampSpinner spinner = (TSTimeStampSpinner) getComponent();
		return spinner.getTimeStampField();
	}

	/**
	 * Sets the timestampe for this field We are expecting java.sql.TimeStamp
	 * object to be passed in
	 * 
	 * @param value
	 *            the value to set (should be java.sql.TimeStamp)
	 */
	public void setValue(Object value) {
		super.setValue(value);
		// value java.sql.TimeStamp
		java.sql.Timestamp ts = (java.sql.Timestamp) value;
		TSTimeStampField timestamp = getTimeStampField();
		if (value == null) {
			timestamp.setNull(true);
		} else {
			Calendar c = Calendar.getInstance();
			c.setTime(ts);
			timestamp.setNull(false);
			timestamp.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
					c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), 0);

		}
		setModified(false);
	}

	public boolean isNull() {
		return getTimeStampField().isNull();
	}

	/**
	 * Gets the value that is displayed in the component and calls setValue on
	 * this object.
	 */
	public void syncValue() {
		// ignore here
	}

}
