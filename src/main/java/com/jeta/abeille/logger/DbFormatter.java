package com.jeta.abeille.logger;

import java.text.MessageFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.jeta.foundation.utils.TSUtils;

/**
 * Special formatter for the DbLogger. Simply formats the logrecord as follows:
 * [time] message \n
 * 
 * @author Jeff Tassin
 */
public class DbFormatter extends Formatter {
	private Date dat = new Date();
	private MessageFormat formatter;
	private Object args[] = new Object[1];
	private final static String format = "{0,time}";

	public String format(LogRecord record) {
		String msg = record.getMessage();
		// if ( msg.equals("\n") )
		// return "";

		StringBuffer text = new StringBuffer();
		// Minimize memory allocations here.
		if (!msg.equals("\n")) {
			dat.setTime(record.getMillis());
			args[0] = dat;
			if (formatter == null) {
				formatter = new MessageFormat(format);
			}
			formatter.format(args, text, null);

			text.insert(0, "[");
			text.append("] ");
		}

		/**
		 * if the message being sent to the logger has multiple lines, we need
		 * to prepend some padding to all the lines other than the first so that
		 * they will be flush with the first line (because of the timestamp):
		 * ex:
		 * 
		 * [10:11:23] This is a multiline logged message. We need to pad this
		 * line so it lines up with the first.
		 * 
		 */
		String padding = TSUtils.fillString(' ', text.length());
		boolean bfirst = true;
		StringTokenizer st = new StringTokenizer(msg, "\n");
		while (st.hasMoreTokens()) {
			String newline = st.nextToken();
			if (bfirst) {
				text.append(newline);
				bfirst = false;
			} else {
				text.append('\n');
				text.append(padding);
				text.append(newline);
			}
		}

		if (bfirst) {
			text.append(msg);
		}

		if (record.getLevel() == Level.FINE || record.getLevel() == Level.WARNING)
			text.append('\n');

		return text.toString();
	}
}
