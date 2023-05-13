package com.jeta.abeille.gui.downloader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This downloader gets content from a url and stores the information in a file
 * 
 * @author Jeff Tassin
 */
public class FileDownloader extends AbstractDownloader {
	private OutputStream m_ostream;

	private String m_file_name;

	/**
	 * ctor
	 */
	public FileDownloader(String url, String output_name) throws IOException {
		super(url);
		m_file_name = output_name;
	}

	/**
	 * Called by AbstractDownloader when downloading has completed.
	 */
	protected void downloadCompleted() throws IOException {
		super.downloadCompleted();
		m_ostream.flush();
		m_ostream.close();
	}

	public void invoke(HashMap props) throws Exception {
		String save_to_dir = (String) props.get(getInput());
		if (save_to_dir == null) {
			throw new IllegalArgumentException("Target directory not specified in FileDownloader");
		}

		String temp_path = save_to_dir + File.separatorChar + m_file_name + ".tmp";
		FileOutputStream fos = new FileOutputStream(temp_path);
		m_ostream = new BufferedOutputStream(fos);
		String path = save_to_dir + File.separatorChar + m_file_name;

		props.put(getOutput(), path);
		super.invoke(props);

		TSUtils.copyFile(path, temp_path);
	}

	public static FileDownloader parse(Element de) throws Exception {
		NodeList nlist = de.getElementsByTagName("value");
		if (nlist.getLength() > 0) {
			Node item = nlist.item(0);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) item;
				EncodedValue p = EncodedValue.parse(e);

				nlist = de.getElementsByTagName("output");
				if (nlist.getLength() > 0) {
					item = nlist.item(0);
					if (item.getNodeType() == Node.ELEMENT_NODE) {
						e = (Element) item;
						String output = e.getAttribute("name");
						if (output != null) {
							return new FileDownloader(p.getDecodedValue(), output);
						}
					}
				}
			}
		}

		throw new Exception(I18N.getLocalizedMessage("Invalid Resource Descriptor"));
	}

	/**
	 * AbstractDownloader override
	 */
	public synchronized void processBlock(byte[] data, int block_size, int total, int content_len) throws IOException {
		m_ostream.write(data, 0, block_size);
		int tk = total / 1000;
		int clk = content_len / 1000;
		setMessage(I18N.format("downloaded_so_far_2", String.valueOf(tk) + "KB", String.valueOf(clk) + "KB"));
	}
}
