package com.jeta.abeille.gui.sql;

import java.awt.BorderLayout;
import java.awt.Color;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.jeta.foundation.gui.components.TSStatusBar;
import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSTimeField;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This panel is used when a SQL query or (set of sql statements) takes a 'long'
 * time to finish. The panel is popped up over the sql editor. It has a status
 * pane and a stop button. It provides some user feed back that is not a modal
 * dialog. It allows the user to continue using the app while the sql command is
 * processed.
 * 
 * @author Jeff Tassin
 */
public class StatusPopup extends TSPanel {
	public final static String STATUS_POS = "statuspos";
	public final static String TIME_POS = "timepos";
	public final static String STOP_BUTTON = "stop.button";

	/** the statubar */
	private TSStatusBar m_statusbar;

	/** stop button */
	private JButton m_stopbtn;

	/**
	 * ctor
	 */
	public StatusPopup() {
		m_statusbar = new TSStatusBar();
		TSCell cell1 = new TSCell(STATUS_POS, "Processing line: ######");
		m_statusbar.addCell(cell1);

		String status = I18N.format("processing_line_1", new Integer(1));
		cell1.setText(status);

		// TSCell cell2 = new TSCell( TIME_POS, "##.##.##" );
		// cell2.setText( "00:00:00" );
		// TSTimeField tf = new TSTimeField();
		// tf.setName( TIME_POS );
		TSCell tf = new TSCell(TIME_POS, " # ##.##.## #");

		m_statusbar.addCell(tf);
		// m_statusbar.addCell( Box.createHorizontalStrut(4) );
		m_stopbtn = new JButton();
		m_stopbtn.setName(STOP_BUTTON);
		m_stopbtn.setText(I18N.getLocalizedMessage("Stop"));
		m_stopbtn.setIcon(TSGuiToolbox.loadImage("stop16.gif"));
		// m_statusbar.addCell( m_stopbtn );

		setLayout(new BorderLayout());
		add(m_statusbar, BorderLayout.CENTER);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black),
				BorderFactory.createEmptyBorder(2, 2, 2, 2)));
		;

	}

	/**
	 * Sets the elapsed time in milliseconds
	 */
	public void setElapsedTime(long elapsedms) {
		int hours = (int) (elapsedms / 3600000);
		int mins = (int) (elapsedms / 60000 - hours * 60);
		int secs = (int) (elapsedms / 1000 - (hours * 3600 + mins * 60));

		// TSTimeField tf = (TSTimeField) m_statusbar.getCell( TIME_POS );
		TSCell tf = (TSCell) m_statusbar.getCell(TIME_POS);
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hours);
		c.set(Calendar.MINUTE, mins);
		c.set(Calendar.SECOND, secs);

		// DateFormat format = DateFormat.getTimeInstance();
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		tf.setText(format.format(c.getTime()));
		// tf.setHours( hours );
		// tf.setMinutes( mins );
		// tf.setSeconds( secs );
	}

	/**
	 * Sets the line number of the sql statement we are current executing in the
	 * status bar
	 */
	public void setLineNumber(int lineNumber) {
		String status = I18N.format("processing_line_1", new Integer(lineNumber));
		setText(STATUS_POS, status);
	}

	/**
	 * Sets the text for a given cell name on the status popup
	 * 
	 * @param cellName
	 *            the name of the cell whose text we wish to set
	 * @param txt
	 *            the text to set
	 */
	public void setText(String cellName, String txt) {

		if (cellName.equals(STATUS_POS)) {
			TSCell cell = (TSCell) m_statusbar.getCell(STATUS_POS);
			cell.setText(txt);

		}
	}

}
