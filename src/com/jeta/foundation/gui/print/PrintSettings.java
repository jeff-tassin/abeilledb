/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.print;

import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;

import com.jeta.foundation.i18n.I18N;

public class PrintSettings {
	private PageFormat m_page = null;
	private PrinterJob m_printer = null;
	private HashPrintRequestAttributeSet m_attributes = null;

	public PrintSettings() {

	}

	public PrinterJob getPrintJob() {
		if (m_printer == null) {
			m_printer = PrinterJob.getPrinterJob();
			m_page = m_printer.defaultPage();
		}
		return m_printer;
	}

	public PageFormat getPageFormat() {
		getPrintJob();
		return m_page;
	}

	public void setPageFormat(PageFormat page) {
		m_page = page;
	}

	public PrintRequestAttributeSet getAttributes() {
		if (m_attributes == null) {
			m_attributes = new HashPrintRequestAttributeSet();
		}
		return m_attributes;
	}
}
