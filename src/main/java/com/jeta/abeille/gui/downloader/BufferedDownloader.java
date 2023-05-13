package com.jeta.abeille.gui.downloader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.HashMap;

import com.jeta.foundation.i18n.I18N;

/**
 * This downloader gets content from a url and stores the information in a byte
 * array
 * 
 * @author Jeff Tassin
 */
public class BufferedDownloader extends AbstractDownloader {
	/** buffer to store dowloaded data */
	private ByteArrayOutputStream m_bos = new ByteArrayOutputStream();

	private String m_prop_name;

	public BufferedDownloader(String url, String prop_name, ActionListener listener) {
		super(url, listener);
		m_prop_name = prop_name;
		setMessage(I18N.getLocalizedMessage("Connecting"));
	}

	/**
	 * AbstractDownloader override
	 */
	public synchronized void processBlock(byte[] data, int block_size, int total, int content_len) throws IOException {
		m_bos.write(data, 0, block_size);
	}

	/**
	 * @return the data in the byte array
	 */
	public synchronized byte[] getData() {
		return m_bos.toByteArray();
	}

	/**
	 * @return the buffer's contents into a string, translating bytes into
	 *         characters according to the platform's default character encoding
	 */
	public synchronized String getString() {
		return m_bos.toString();
	}

	public void invoke(HashMap props) throws Exception {
		super.invoke(props);
		if (m_prop_name != null) {
			props.put(m_prop_name, getData());
		}
	}

	public void updateListeners(int read_so_far, int content_len) {
		DownloadEvent evt = new DownloadEvent(this, DownloadEvent.STATUS);
		evt.setStatus(0, 0);
		updateListeners(evt);
	}
}
