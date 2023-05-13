/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.print;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.print.Printable;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSStatusBar;
import com.jeta.foundation.gui.components.TSToolBarTemplate;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

public class PrintPreviewDialog extends TSDialog {
	/** combo box for setting the zoom factor */
	private JComboBox m_zoomcombo;

	/** the container that displays all the pages to be printed */
	private PrintPreview m_preview;

	private PageManager m_pagemanager = new PageManager();

	/** the status bar */
	private TSStatusBar m_statusbar;
	private TSCell m_maincell;
	public static final String ID_MAIN_CELL = "status.main.cell";

	/**
	 * ctor
	 */
	public PrintPreviewDialog(java.awt.Frame parent, boolean bModal) {
		super(parent, bModal);
		createComponents();
	}

	/**
	 * ctor
	 */
	public PrintPreviewDialog(java.awt.Dialog parent, boolean bModal) {
		super(parent, bModal);
		createComponents();
	}

	/**
	 * Creates the toolbar and PrintPreview
	 */
	private void createComponents() {
		setTitle(I18N.getLocalizedMessage("Print Preview"));
		createToolBar();
		createStatusBar();
		createView();
		setController(new PrintPreviewController(this));
		showCloseLink();
	}

	/**
	 * Create
	 */
	private void createView() {
		m_preview = new PrintPreview();
		JScrollPane ps = new JScrollPane(m_preview);
		setPrimaryPanel(ps);
	}

	/**
	 * creates a radiou button used for the toolbar
	 */
	private JComponent createRadio(String id, String image, String selimage, String tooltip) {
		JButton btn = new JButton(TSGuiToolbox.loadImage(image));
		btn.setToolTipText(I18N.getLocalizedMessage(tooltip));
		// btn.setSelectedIcon( TSGuiToolbox.loadImage( selimage ) );
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setPreferredSize(TSGuiToolbox.getToolbarButtonSize());
		btn.setMaximumSize(TSGuiToolbox.getToolbarButtonSize());

		setCommandId(btn, id);
		return btn;
	}

	/**
	 * Creates the status bar for this dialog
	 */
	private void createStatusBar() {
		m_statusbar = new TSStatusBar();

		m_maincell = new TSCell(ID_MAIN_CELL, "Pages: ###################");
		m_maincell.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		m_maincell.setMain(true);
		m_statusbar.addCell(m_maincell);
		getDialogContentPanel().add(m_statusbar, BorderLayout.SOUTH);
	}

	/**
	 * Creates the toolbar for this dialog
	 */
	private void createToolBar() {
		enableToolBar();

		TSToolBarTemplate template = getToolBarTemplate();
		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_PRINT, "incors/16x16/printer.png", "Print"));
		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_PAGE_SETUP, "incors/16x16/printer_preferences.png",
				"Page Setup"));
		template.addSeparator();

		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_PREVIEW_ONE, "incors/16x16/document_plain.png",
				"Show One Page"));
		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_PREVIEW_TWO, "incors/16x16/documents.png",
				"Show Two Pages"));
		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_PREVIEW_FOUR, "incors/16x16/documents4.png",
				"Show Four Pages"));

		template.addSeparator();

		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_ZOOM_IN, "incors/16x16/zoom_in.png", "Zoom In"));
		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_ZOOM_OUT, "incors/16x16/zoom_out.png", "Zoom Out"));
		template.add(Box.createHorizontalStrut(5));

		JLabel label = new JLabel(I18N.getLocalizedDialogLabel("Scale"));
		String[] scales = { "10%", "25%", "50%", "100%" };
		m_zoomcombo = new JComboBox(scales);
		m_zoomcombo.setMaximumSize(m_zoomcombo.getPreferredSize());
		m_zoomcombo.setEditable(true);
		m_zoomcombo.getEditor().setItem(String.valueOf(getScale()) + "%");
		m_zoomcombo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String sscale = TSUtils.strip((String) m_zoomcombo.getEditor().getItem(), "%");
				try {
					int scale = Integer.parseInt(sscale);
					if (scale != getScale()) {
						setScale(scale);
					}
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}
		});

		template.add(label);
		template.add(Box.createHorizontalStrut(5));
		template.add(m_zoomcombo);

		template.addSeparator();
		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_FIRST, "incors/16x16/media_rewind.png", "First"));
		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_PREVIOUS, "incors/16x16/media_step_back.png", "Prev"));
		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_NEXT, "incors/16x16/media_step_forward.png", "Next"));
		template.add(i18n_createToolBarButton(PrintPreviewNames.ID_LAST, "incors/16x16/media_fast_forward.png", "Last"));

	}

	public void dispose() {
		m_preview.removeAll();
		m_preview = null;
		m_pagemanager = null;
		super.dispose();
	}

	public PageManager getPageManager() {
		return m_pagemanager;
	}

	/**
	 * @return the current page offset. The preview window can only show 1,2 or
	 *         4 pages at a time. The offset is the page number of the 'first'
	 *         page being shown in the preview window.
	 */
	public int getPageOffset() {
		return m_pagemanager.getPageOffset();
	}

	/**
	 * Sets the number of pages visible in the preview window
	 */
	public int getPagePreviewCount() {
		return m_pagemanager.getPagePreviewCount();
	}

	/**
	 * @return the preferred size for this dialog
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(10, 25);
	}

	/**
	 * @return the object responsible for rendering the print job
	 */
	public Printable getPrintable() {
		return m_pagemanager.getPrintable();
	}

	public PrintPreview getPrintPreview() {
		return m_preview;
	}

	/**
	 * @return the current zoom factor in percent (e.g. 10 = 10% )
	 */
	public int getScale() {
		return m_pagemanager.getScale();
	}

	/**
	 * @return the total number of pages
	 */
	public int getTotalPages() {
		return m_pagemanager.getTotalPages();
	}

	/**
	 * Sets the number of pages visible in the preview window
	 */
	public void setPagePreviewCount(int pageCount) {
		if (pageCount != m_pagemanager.getPagePreviewCount()) {
			m_pagemanager.setPagePreviewCount(pageCount);
			reset();
		}
	}

	/**
	 * Resets the view
	 */
	public void reset() {
		m_preview.removeAll();
		for (int page = 0; page < m_pagemanager.getPagePreviewCount(); page++) {
			PagePreview pagepreview = m_pagemanager.getPage(page + m_pagemanager.getPageOffset());
			if (pagepreview != null) {
				m_preview.add(pagepreview);
			}
		}
		m_preview.revalidate();
		Dimension d = m_preview.doLayoutInternal();
		m_preview.setPreferredSize(d);

		m_preview.repaint();

		int total_pages = getTotalPages();
		if (total_pages == 0) {
			m_maincell.setText(I18N.format("Page_x_of_N_2", TSUtils.getInteger(0), TSUtils.getInteger(0)));
		} else {
			m_maincell.setText(I18N.format("Page_x_of_N_2", TSUtils.getInteger(m_pagemanager.getPageOffset() + 1),
					TSUtils.getInteger(total_pages)));
		}
	}

	/**
	 * Sets the object responsible for rendering the print job
	 */
	public void setPrintable(Printable target) {
		m_pagemanager.setPrintable(target);
		reset();
		m_zoomcombo.getEditor().setItem(String.valueOf(getScale()) + "%");
	}

	public void setScale(int scale) {
		if (scale > 200)
			scale = 200;

		m_pagemanager.setScale(scale);

		int w = (int) (m_pagemanager.getPageWidth() * scale / 100.0f);
		int h = (int) (m_pagemanager.getPageHeight() * scale / 100.0f);
		Component[] comps = m_preview.getComponents();
		for (int k = 0; k < comps.length; k++) {
			if (!(comps[k] instanceof PagePreview))
				continue;
			PagePreview pp = (PagePreview) comps[k];
			pp.setScaledSize(w, h);
		}

		Dimension d = m_preview.doLayoutInternal();
		m_preview.setPreferredSize(d);

		m_preview.getParent().getParent().validate();
		m_zoomcombo.getEditor().setItem(String.valueOf(scale) + "%");
	}

}
