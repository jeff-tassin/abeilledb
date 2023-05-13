package com.jeta.abeille.gui.update;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.SimpleDateFormat;

import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.jeta.foundation.gui.calendar.CalendarPanel;
import com.jeta.foundation.gui.components.TSDialog;

/**
 * This dialog allows the user to edit DATE data types in a database.
 * 
 * @author Jeff Tassin
 */
public class DateDialog extends TSDialog {
	CalendarPanel m_calPanel;

	public DateDialog(Frame owner, boolean bmodal) {
		super(owner, bmodal);
		initialize();
	}

	/**
	 * Creates the controls for this dialog
	 * 
	 * @return a panel that contains the controls
	 */
	private JPanel createControlsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		m_calPanel = new CalendarPanel();
		panel.add(m_calPanel, BorderLayout.NORTH);

		m_calPanel.addPropertyChangeListener(CalendarPanel.DATE_VALUE, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				setDialogTitle();
			}
		});
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		return panel;
	}

	/**
	 * @return the underlying calendar object
	 */
	public Calendar getCalendar() {
		return m_calPanel.getCalendar();
	}

	/**
	 * Creates and initializes the controls on this dialog
	 */
	private void initialize() {
		setPrimaryPanel(createControlsPanel());
	}

	/**
	 * Sets the displayed date to the given calendar
	 */
	public void setCalendar(Calendar cal) {
		m_calPanel.setCalendar(cal);
		setDialogTitle();
	}

	/**
	 * Sets the dialog title to the current date
	 */
	void setDialogTitle() {
		Calendar c = m_calPanel.getCalendar();
		SimpleDateFormat format = new SimpleDateFormat("MMMMM dd, yyyy");
		setTitle(format.format(c.getTime()));
	}

}
