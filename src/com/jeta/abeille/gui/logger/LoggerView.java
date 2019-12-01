package com.jeta.abeille.gui.logger;

import java.awt.BorderLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Utilities;

import com.jeta.abeille.logger.DbFormatter;
import com.jeta.abeille.logger.DbLogger;

import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSStatusBar;

import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays log messages for the application in a JEditorPane.
 * 
 * @author Jeff Tassin
 */
public class LoggerView extends TSPanel {
	/** the status bar */
	private TSStatusBar m_statusbar;

	/** the editor for this view */
	private JEditorPane m_editor;

	public static final String ID_LOG_EDITOR = "log.editor";
	public static final String MAIN_CELL = "main.status.cell";

	public static final int MAX_LINES = 1000;

	public LoggerView() {
		initialize();
		Logger logger = Logger.getLogger(com.jeta.foundation.componentmgr.ComponentNames.APPLICATION_LOGGER);
		if (logger != null) {
			// add our specialized handler here
			ViewHandler handler = new ViewHandler();
			handler.setFormatter(new DbFormatter());
			logger.addHandler(handler);
		}
	}

	/**
	 * Appends a log message to the view
	 */
	public void addLogRecord(String msg) {
		try {
			if (TSUtils.isDebug()) {
				System.out.println(msg);
			}
			JEditorPane editor = getEditor();
			Document doc = editor.getDocument();
			doc.insertString(doc.getLength(), msg, null);
			// if the editor gets full (i.e. has more lines than allowed), then
			// remove the first line
			int linenum = Utilities.getLineOffset((BaseDocument) doc, doc.getLength() - 1);
			if (linenum >= MAX_LINES) {
				int endpos = Utilities.getRowStartFromLineOffset((BaseDocument) doc, 1);
				doc.remove(0, endpos);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @return the underlying editor for the view
	 */
	JEditorPane getEditor() {
		return m_editor;
	}

	/**
	 * Creates the components and initializes the view. Loads any existing
	 * messages from the current log file.
	 */
	private void initialize() {
		m_editor = TSEditorUtils.createEditor(new LoggerKit());
		JComponent comp = TSEditorUtils.getExtComponent(m_editor);
		m_editor.setName(ID_LOG_EDITOR);
		m_editor.setEditable(false);
		setLayout(new BorderLayout());
		add(comp, BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		m_statusbar = new TSStatusBar();
		TSCell cell1 = new TSCell(MAIN_CELL, "#####");
		cell1.setMain(true);
		m_statusbar.addCell(cell1);
		add(m_statusbar, BorderLayout.SOUTH);
		cell1.setText(DbLogger.getLogFileName());
		cell1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

		Settings.setValue(LoggerKit.class, SettingsNames.LINE_NUMBER_VISIBLE, Boolean.FALSE);
		Settings.setValue(LoggerKit.class, SettingsNames.TEXT_LIMIT_LINE_VISIBLE, Boolean.FALSE);
		TSEditorUtils.showStatusBar(m_editor, false);

		// loadLogFile( DbLogger.getLogFileName() );
	}

	/**
	 * Loads the existing log into the view
	 */
	private void loadLogFile(String fileName) {
		try {
			Calendar c = Calendar.getInstance();
			long start = c.getTimeInMillis();

			File f = new File(fileName);
			FileReader freader = new FileReader(f);
			BufferedReader reader = new BufferedReader(freader);
			String str = reader.readLine();
			while (str != null) {
				addLogRecord(str + "\n");
				str = reader.readLine();
			}

			c = Calendar.getInstance();
			long currtime = c.getTimeInMillis();
			long delta = currtime - start;
			// long seconds = delta/1000;
			// System.out.println( "load time: " + delta );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handler for LoggerView. Inserts logrecords into the editor document.
	 */
	public class ViewHandler extends Handler {
		public void close() {
		}

		public void flush() {
		}

		public void publish(LogRecord logrec) {
			final LogRecord record = logrec;
			Runnable gui_update = new Runnable() {
				public void run() {
					if (isLoggable(record)) {
						String msg = getFormatter().format(record);
						addLogRecord(msg);
					}
				}
			};
			SwingUtilities.invokeLater(gui_update);
		}
	}

}
