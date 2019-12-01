package com.jeta.abeille.logger;

import java.io.File;
import java.io.IOException;

import java.sql.SQLException;

import java.util.Calendar;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;
import com.jeta.foundation.componentmgr.TSComponent;

import com.jeta.foundation.interfaces.resources.ResourceLoader;

/**
 * This class initializes the logging API for the application
 * 
 * @author Jeff Tassin
 */
public class DbLogger implements TSComponent {

	/**
	 * The name of the log file for the application
	 */
	private static String m_logfile;

	/**
	 * ctor
	 */
	public DbLogger() {
	}

	/**
	 * Helper method that sends a message to the logger
	 */
	public static void fine(String msg) {
		Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
		logger.fine(msg);
	}

	/**
	 * @return the name of the logfile for this application instance
	 */
	public static String getLogFileName() {
		return m_logfile;
	}

	/**
	 * Helper method that sends a message to the logger
	 */
	public static void log(SQLException e) {
		Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
		String msg = e.getLocalizedMessage();
		if (msg == null || msg.length() == 0) {
			logger.log(Level.WARNING, e.getMessage());
		} else {
			logger.log(Level.WARNING, msg);
		}
	}

	/**
	 * TSComponent implementation
	 */
	public void startup() {
		try {
			Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);

			ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
			if (loader != null) {
				Calendar cal = Calendar.getInstance();
				StringBuffer logfile = new StringBuffer();
				logfile.append("log");
				logfile.append(File.separatorChar);
				logfile.append("logfile.");
				logfile.append(cal.get(Calendar.MONTH) + 1);
				logfile.append('.');
				logfile.append(cal.get(Calendar.DAY_OF_MONTH));
				logfile.append('.');
				logfile.append(cal.get(Calendar.YEAR));
				logfile.append(".txt");
				loader.createResource(logfile.toString());

				// I really don't want to do this, but the FileHandler does not
				// take an OutputStream
				String home = loader.getHomeDirectory();
				logfile.insert(0, File.separatorChar);
				logfile.insert(0, home);

				m_logfile = logfile.toString();
				// only add file handler here.
				// other app components that get log messages will add their
				// owner handlers
				FileHandler handler = new FileHandler(m_logfile, true);
				handler.setFormatter(new DbFormatter());
				logger.addHandler(handler);
			}

			logger.setLevel(Level.FINE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * TSComponent implementation
	 */
	public void shutdown() {
	}

}
