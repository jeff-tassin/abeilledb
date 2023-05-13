package com.jeta.abeille.gui.update;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Calendar;

import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.foundation.gui.components.TSDateField;
import com.jeta.foundation.gui.components.TSDateSpinner;
import com.jeta.foundation.gui.components.TSTimeField;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class represents a date component in the UpdateFrame This component has
 * an icon and a text field [icon][TSCalendarField]
 * 
 * @author Jeff Tassin
 */
public class UpdateDateComponent extends InstanceComponent implements ActionListener {
	public UpdateDateComponent(String fieldName, int dataType) {
		super(fieldName, dataType);

		TSDateSpinner field = new TSDateSpinner() {
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();

				TSDateField datefield = getDateField();
				Dimension dc = datefield.getPreferredSize();
				d.width = dc.width + d.height;
				return d;
			}
		};

		field.getDateField().addActionListener(this);
		setComponent(field);
		setIcon("incors/16x16/calendar.png");
		getIconButton().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				DateDialog dlg = (DateDialog) TSGuiToolbox.createDialog(DateDialog.class, UpdateDateComponent.this,
						true);
				TSDateField datefield = getDateField();
				dlg.setCalendar(datefield.getDate());
				dlg.setSize(dlg.getPreferredSize());

				dlg.showCenter();
				if (dlg.isOk()) {
					datefield = getDateField();
					Calendar c = dlg.getCalendar();
					datefield.setDay(c.get(Calendar.DAY_OF_MONTH));
					datefield.setMonth(c.get(Calendar.MONTH));
					datefield.setYear(c.get(Calendar.YEAR));
					setModified(true);
				}
			}
		});
		clear();
	}

	/**
	 * Clears the current control
	 */
	public void clear() {
		TSDateField datefield = getDateField();
		datefield.setNow();
	}

	/**
	 * Called in response to time change events from the time field
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(TSTimeField.VALUE_CHANGE_EVENT)) {
			setModified(true);
		}
	}

	public TSDateField getDateField() {
		// TSDatePanel panel = (TSDatePanel)getComponent();
		TSDateSpinner panel = (TSDateSpinner) getComponent();
		return panel.getDateField();
	}

	public boolean isNull() {
		return getDateField().isNull(); // if the underlying date field is null
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
			pstmt.setNull(count, java.sql.Types.DATE);
		} else {
			TSDateField field = getDateField();
			// String result = formatter.prepareDate( field.getMonth() + 1,
			// field.getDay(), field.getYear() );
			// pstmt.setString( count, result );

			Calendar c = field.getCalendar();
			java.sql.Date result = new java.sql.Date(c.getTimeInMillis());
			pstmt.setDate(count, result);
		}
	}

	/**
	 * @return the sql representation of this date.
	 */
	public String toSQLString(SQLFormatter formatter) {
		if (isNull())
			return "null";

		TSDateField field = getDateField();
		return formatter.formatDate(field.getMonth() + 1, field.getDay(), field.getYear());
	}

	/**
	 * Sets the value for the given date field. We assume that the value is
	 * always a java.sql.Date object
	 */
	public void setValue(Object value) {
		super.setValue(value);

		TSDateField datefield = getDateField();
		if (value == null) {
			datefield.setNull(true);
		} else {
			datefield.setNull(false);
			java.util.Date date = (java.util.Date) value;
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			datefield.setDay(c.get(Calendar.DAY_OF_MONTH));
			datefield.setMonth(c.get(Calendar.MONTH));
			datefield.setYear(c.get(Calendar.YEAR));
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
