/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

public class PageManager {
	/** the width of each printed page */
	private int m_pagewidth;

	/** the height of each printed page */
	private int m_pageheight;

	/** the object that renders the print job */
	private Printable m_printable;

	/**
	 * The current zoom scale in percent. So a value of 10 would be 10% zoom.
	 * This is static because we want to remember the scale setting from preview
	 * to preview
	 */
	private static int m_scale = 25;

	/**
	 * The number of pages we currently display in the view. This is static
	 * because we want to remember the page count setting from preview to
	 * preview
	 */
	private static int m_pagecount = 2;

	/**
	 * the offset of the first page in the view. Since we only display 1, 2, or
	 * 4 pages at a time, the first page in the view will be some factor of 1,
	 * 2, or 4
	 */
	private int m_offset = 0;

	/**
	 * A collection of PagePreview objects that corresponds to the current pages
	 * visible on the preview window We only allow viewing of 1000 total pages
	 */
	private PagePreview[] m_pages = new PagePreview[1000];

	/**
	 * ctor
	 */
	public PageManager() {
	}

	/**
	 * ctor
	 */
	public PageManager(Printable printable) {
		m_printable = printable;
	}

	/**
	 * Clears all cached pages. This is generally called when the user changes
	 * the format of a page such as portrait -> lanscape.
	 */
	public void clear() {
		for (int index = 0; index < m_pages.length; index++) {
			m_pages[index] = null;
		}
	}

	/**
	 * the offset of the first page in the view. Since we only display 1, 2, or
	 * 4 pages at a time, the first page in the view will be some factor of 1,
	 * 2, or 4
	 */
	public int getPageOffset() {
		return m_offset;
	}

	/**
	 * Creates a page preview object
	 * 
	 * @return the page preview for the given page index. If there is no page
	 *         for the given index, null is returned.
	 */
	public PagePreview getPage(int pageIndex) {
		PagePreview preview = m_pages[pageIndex];
		if (preview == null) {
			preview = new PagePreview();
			if (getPage(pageIndex, preview) == Printable.PAGE_EXISTS) {
				m_pages[pageIndex] = preview;
			} else {
				preview = null;
			}
		}
		return preview;
	}

	/**
	 * Creates a page preview object
	 * 
	 * @return the page preview for the given page index. If there is no page
	 *         for the given index, null is returned.
	 */
	private int getPage(int pageIndex, PagePreview pagepreview) {
		PrintSettings settings = PrintUtils.getPrintSettings();
		PageFormat pageFormat = settings.getPageFormat();
		if (pageFormat.getHeight() == 0 || pageFormat.getWidth() == 0) {
			System.err.println("Unable to determine default page size");
			return 0;
		}

		m_pagewidth = (int) (pageFormat.getWidth());
		m_pageheight = (int) (pageFormat.getHeight());

		// System.out.println( "pagemanager.getPage width: " + m_pagewidth );
		// System.out.println( "pagemanager.getPage height: " + m_pageheight );

		int scale = getScale();
		/**
		 * updates the combo box scale to the current value - just in case it is
		 * different
		 */
		setScale(scale);
		int w = (int) (m_pagewidth * scale / 100);
		int h = (int) (m_pageheight * scale / 100);

		int result = 0;
		/** now add each page to the page preview */
		try {
			if (pagepreview == null) {
				/** here we simply want to know if the given page exists */
				result = m_printable.print(null, pageFormat, pageIndex);
			} else {
				BufferedImage img = new BufferedImage(m_pagewidth, m_pageheight, BufferedImage.TYPE_INT_RGB);
				Graphics g = img.getGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, m_pagewidth, m_pageheight);
				result = m_printable.print(g, pageFormat, pageIndex);
				if (result == Printable.PAGE_EXISTS) {
					pagepreview.setImage(w, h, img);
				}
			}
		} catch (PrinterException e) {
			e.printStackTrace();
			System.err.println("Printing error: " + e.toString());
		}
		return result;
	}

	/** the number of pages we currently display in the view */
	public int getPagePreviewCount() {
		return m_pagecount;
	}

	public int getPageWidth() {
		return m_pagewidth;
	}

	public int getPageHeight() {
		return m_pageheight;
	}

	/**
	 * @return the object responsible for rendering the print job
	 */
	public Printable getPrintable() {
		return m_printable;
	}

	/**
	 * @return the current zoom factor in percent (e.g. 10 = 10% )
	 */
	public int getScale() {
		return m_scale;
	}

	/**
	 * @return the total number of pages
	 */
	public int getTotalPages() {
		for (int index = 0; index < 1000; index++) {
			if (getPage(index, null) != Printable.PAGE_EXISTS)
				return index;
		}
		return 1000;
	}

	/**
	 * Sets the page offset. The preview window can only show 1,2 or 4 pages at
	 * a time. The offset is the page number of the 'first' page being shown in
	 * the preview window.
	 */
	public void setPageOffset(int offset) {
		if (offset < 0)
			offset = 0;

		m_offset = offset;
	}

	/**
	 * Sets the number of pages visible in the preview window
	 */
	public void setPagePreviewCount(int pageCount) {
		m_pagecount = pageCount;
	}

	/**
	 * Sets the object responsible for rendering the print job
	 */
	public void setPrintable(Printable target) {
		m_printable = target;
	}

	public void setScale(int scale) {
		m_scale = scale;
	}

}
