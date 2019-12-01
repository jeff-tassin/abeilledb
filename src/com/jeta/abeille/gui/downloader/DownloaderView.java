package com.jeta.abeille.gui.downloader;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.layouts.ColumnLayout;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * Displays the download status in a progress bar.
 * 
 * [msg....] [progress bar]
 * 
 * @author Jeff Tassin
 */
public class DownloaderView extends TSPanel {
	/** displays a text message for the download stage */
	private JLabel m_status = new JLabel("Connecting...");

	/** displays the download progress */
	private JProgressBar m_progress = new JProgressBar();

	public DownloaderView() {
		setLayout(new BorderLayout());
		add(createView(), BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}

	/**
	 * creates the main view components
	 */
	private TSPanel createView() {
		ColumnLayout cl = new ColumnLayout();
		TSPanel panel = new TSPanel(cl);
		panel.add(m_status);
		panel.add(javax.swing.Box.createVerticalStrut(10));
		panel.add(m_progress);
		return panel;
	}

	public void updateStatus(String msg, int current_total, int content_len) {
		m_status.setText(msg);
		m_progress.setMinimum(0);
		m_progress.setMaximum(content_len);
		m_progress.setValue(current_total);
	}

}
