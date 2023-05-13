package com.jeta.abeille.gui.downloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jeta.foundation.i18n.I18N;

public class UnzipAction extends AbstractTask {
	private EncodedValue m_path;

	private String m_output;

	/**
	 * ctor
	 */
	private UnzipAction(EncodedValue p, String output) {
		m_path = p;
		m_output = output;
	}

	public static UnzipAction parse(Element unzipe) throws Exception {
		NodeList nlist = unzipe.getElementsByTagName("value");
		if (nlist.getLength() > 0) {
			Node item = nlist.item(0);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) item;
				EncodedValue p = EncodedValue.parse(e);
				nlist = unzipe.getElementsByTagName("output");
				if (nlist.getLength() > 0) {
					item = nlist.item(0);
					if (item.getNodeType() == Node.ELEMENT_NODE) {
						e = (Element) item;
						String output = e.getAttribute("name");
						if (output != null) {
							return new UnzipAction(p, output);
						}
					}
				}
			}
		}

		throw new Exception(I18N.getLocalizedMessage("Invalid Resource Descriptor"));
	}

	/**
	 * DownloaderAction implementation
	 */
	public void invoke(HashMap props) throws Exception {
		String src = (String) props.get(getInput());
		if (src == null) {
			throw new IllegalArgumentException("input file not specified in unzip action");
		}

		File file = new File(src);
		if (file.isFile()) {

			JarFile jar = new JarFile(src);
			ZipEntry entry = jar.getEntry(m_path.getDecodedValue());

			String output = file.getParent() + File.separatorChar + m_output;
			InputStream istream = jar.getInputStream(entry);
			// System.out.println( "unzip action...   src: " + src + "  dest: "
			// + output );
			saveFile(output, istream, (int) entry.getSize());
			props.put(getOutput(), output);
			istream.close();
		} else {
			throw new FileNotFoundException(src);
		}

	}

	public void saveFile(String fname, InputStream src_stream, int content_len) throws IOException {
		File f = new File(fname);
		FileOutputStream fos = new FileOutputStream(f);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		float total = 0.0f;
		byte[] buff = new byte[1024];
		int numread = src_stream.read(buff);
		while (numread > 0) {
			bos.write(buff, 0, numread);
			total += (float) numread;
			float pread = total / (float) content_len;
			numread = src_stream.read(buff);
		}
		bos.flush();
		bos.close();
	}

}
