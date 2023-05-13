/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.print;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

public class PrintUtils {
	private static PageFormat m_page = null;
	private static PrinterJob m_printer = null;

	private static PrintSettings m_printsettings = null;

	public static boolean showSetupDialog() {
		PrintSettings settings = getPrintSettings();
		/*
		 * PageFormat page = settings.getPageFormat(); page =
		 * settings.getPrintJob().pageDialog( page ); if ( page != null ) {
		 * settings.setPageFormat( page ); return true; } else { return false; }
		 */

		PrinterJob pj = settings.getPrintJob();
		PageFormat pf = pj.pageDialog(settings.getAttributes());
		if (pf != null)
			settings.setPageFormat(pf);

		return (pf != null);
	}

	public static PrintSettings getPrintSettings() {
		if (m_printsettings == null) {
			m_printsettings = new PrintSettings();
		}
		return m_printsettings;
	}

	public static PrinterJob showPrintDialog(Printable printable) {
		/*
		 * PrinterJob prnJob = getPrintSettings().getPrintJob(); if (
		 * prnJob.printDialog() ) { return prnJob; } else { return null; }
		 */
		PrintSettings settings = getPrintSettings();

		PrinterJob pj = settings.getPrintJob();
		pj.setPrintable(printable);
		if (pj.printDialog(settings.getAttributes())) {
			return pj;
		} else {
			return null;
		}
	}

}
