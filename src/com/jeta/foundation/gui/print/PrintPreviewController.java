/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.print;

import java.awt.Component;
import java.awt.Cursor;

import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.utils.TSUtils;

public class PrintPreviewController extends TSController {
	/** the dialog we are controlling */
	private PrintPreviewDialog m_dialog;

	/** the page manager */
	private PageManager m_pagemanager;

	private static final int ZOOM_DELTA = 10;

	public PrintPreviewController(PrintPreviewDialog dialog) {
		super(dialog);
		m_dialog = dialog;
		m_pagemanager = dialog.getPageManager();

		assignAction(PrintPreviewNames.ID_PRINT, new PrintAction());
		assignAction(PrintPreviewNames.ID_PAGE_SETUP, new PageSetupAction());
		assignAction(PrintPreviewNames.ID_PREVIEW_ONE, new PreviewOnePageAction());
		assignAction(PrintPreviewNames.ID_PREVIEW_TWO, new PreviewTwoPagesAction());
		assignAction(PrintPreviewNames.ID_PREVIEW_FOUR, new PreviewFourPagesAction());
		assignAction(PrintPreviewNames.ID_ZOOM_IN, new ZoomInAction());
		assignAction(PrintPreviewNames.ID_ZOOM_OUT, new ZoomOutAction());

		assignAction(PrintPreviewNames.ID_PREVIOUS, new PreviousAction());
		assignAction(PrintPreviewNames.ID_NEXT, new NextAction());
		assignAction(PrintPreviewNames.ID_FIRST, new FirstAction());
		assignAction(PrintPreviewNames.ID_LAST, new LastAction());
	}

	private class FirstAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_pagemanager.setPageOffset(0);
			m_dialog.reset();
		}
	}

	private class LastAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			int page_count = m_pagemanager.getPagePreviewCount();
			int total_pages = m_pagemanager.getTotalPages();
			int offset = total_pages - total_pages % page_count;
			if (offset >= total_pages)
				offset = total_pages - page_count;

			m_pagemanager.setPageOffset(offset);
			m_dialog.reset();
		}
	}

	private class NextAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int offset = m_pagemanager.getPageOffset();
			offset += m_pagemanager.getPagePreviewCount();
			if (offset < m_pagemanager.getTotalPages()) {
				m_pagemanager.setPageOffset(offset);
				m_dialog.reset();
			}
		}
	}

	/**
	 * Invokes the page setup dialog
	 */
	private class PageSetupAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (PrintUtils.showSetupDialog()) {
				m_pagemanager.clear();
				/**
				 * changing the page format may have reduced the number of pages
				 * so, let's make sure the current offset is valid
				 */
				if (m_pagemanager.getPageOffset() >= m_pagemanager.getTotalPages()) {
					invokeAction(PrintPreviewNames.ID_LAST);
				} else {
					m_dialog.reset();
				}
			}
		}
	}

	private class PreviousAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int offset = m_pagemanager.getPageOffset();
			offset -= m_pagemanager.getPagePreviewCount();
			if (offset >= 0) {
				m_pagemanager.setPageOffset(offset);
				m_dialog.reset();
			}
		}
	}

	private class PrintAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				// PrinterJob prnJob =
				// PrintUtils.getPrintSettings().getPrintJob();
				// if ( prnJob.printDialog() )
				PrinterJob prnJob = PrintUtils.showPrintDialog(m_pagemanager.getPrintable());
				if (prnJob != null) {
					m_dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					prnJob.print(PrintUtils.getPrintSettings().getAttributes());
					m_dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					m_dialog.cmdOk();
				}
			} catch (PrinterException ex) {
				TSUtils.printException(ex);
			}
		}
	}

	private class PreviewOnePageAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_pagemanager.setPagePreviewCount(1);
			m_dialog.reset();
		}
	}

	private class PreviewTwoPagesAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			int offset = m_pagemanager.getPageOffset();
			offset -= offset % 2;
			m_pagemanager.setPageOffset(offset);
			m_pagemanager.setPagePreviewCount(2);
			m_dialog.reset();
		}
	}

	private class PreviewFourPagesAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			int offset = m_pagemanager.getPageOffset();
			offset -= offset % 4;
			m_pagemanager.setPageOffset(offset);
			m_pagemanager.setPagePreviewCount(4);
			m_dialog.reset();
		}
	}

	private class ZoomInAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int scale = m_dialog.getScale();
			scale += ZOOM_DELTA;
			if (scale > 150)
				scale = 150;
			m_dialog.setScale(scale);
		}
	}

	private class ZoomOutAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int scale = m_dialog.getScale();
			scale -= ZOOM_DELTA;
			if (scale < 10)
				scale = 10;
			m_dialog.setScale(scale);
		}
	}

}
