package com.jeta.abeille.gui.downloader;

import java.io.InputStream;
import java.io.IOException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URL;
import java.net.URLConnection;

import java.util.HashMap;
import java.util.Properties;

import javax.swing.SwingUtilities;

import com.jeta.foundation.utils.TSUtils;

/**
 * This downloader gets content from a url and stores the information in a byte
 * array
 * 
 * @author Jeff Tassin
 */
public abstract class AbstractDownloader extends AbstractTask {
	private String m_url;
	private InputStream m_downloadstream;

	public AbstractDownloader(String url) {
		m_url = url;
	}

	public AbstractDownloader(String url, ActionListener listener) {
		super(listener);
		m_url = url;
	}

	public void cancel() {
		super.cancel();
		try {
			if (m_downloadstream != null) {
				m_downloadstream.close();
			}
		} catch (Exception e) {

		}
	}

	public String getUrl() {
		return m_url;
	}

	protected void downloadCompleted() throws IOException {
		updateListeners(new DownloadEvent(AbstractDownloader.this, DownloadEvent.COMPLETED));
	}

	public void processBlock(byte[] data, int block_size, int total, int content_len) throws IOException {

	}

	public void invoke(HashMap props) throws Exception {
		URL url = new URL(m_url);
		if (TSUtils.isDebug()) {
			System.out.println("AbstractDownloader... ");
			System.out.println(" url: " + m_url);
			Properties prop = System.getProperties();
			System.out.println(" http.proxySet = " + prop.get("http.proxySet"));
			System.out.println(" http.proxyHost = " + prop.get("http.proxyHost"));
			System.out.println(" http.proxyPort = " + prop.get("http.proxyPort"));
		}

		URLConnection conn = url.openConnection();
		int content_len = conn.getContentLength();

		m_downloadstream = conn.getInputStream();
		byte[] buff = new byte[1024];
		int numread = m_downloadstream.read(buff);
		if (numread == 0) {
			throw new Exception("Document contains no data");
		}

		int total = 0;
		while (numread > 0) {
			total += numread;
			updateListeners(total, content_len);
			processBlock(buff, numread, total, content_len);
			numread = m_downloadstream.read(buff);
		}
		m_downloadstream.close();
		downloadCompleted();
	}

	public void updateListeners(int read_so_far, int content_len) {
		DownloadEvent evt = new DownloadEvent(AbstractDownloader.this, DownloadEvent.STATUS);
		evt.setStatus(read_so_far, content_len);
		updateListeners(evt);
	}
}
