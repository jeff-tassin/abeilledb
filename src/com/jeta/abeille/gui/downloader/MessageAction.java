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

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

public class MessageAction extends AbstractTask {
	private String m_message;
	private String m_type;

	/**
	 * ctor
	 */
	private MessageAction(String mtype, String msg) {
		m_type = mtype;
		m_message = msg;
	}

	public static MessageAction parse(Element msge) throws Exception {
		String mtype = msge.getAttribute("type");
		if (mtype == null)
			mtype = "html";

		NodeList nlist = msge.getElementsByTagName("value");
		if (nlist.getLength() > 0) {
			Node item = nlist.item(0);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) item;
				EncodedValue value = EncodedValue.parse(e);
				return new MessageAction(mtype, value.getDecodedValue());
			}
		}
		throw new Exception(I18N.getLocalizedMessage("Invalid Resource Descriptor"));
	}

	/**
	 * DownloaderAction implementation
	 */
	public void invoke(HashMap props) throws Exception {
		if ("html".equalsIgnoreCase(m_type)) {
			showHTMLMessage();
		} else {
			throw new Exception(I18N.getLocalizedMessage("Invalid Resource Descriptor"));
		}
	}

	private void showHTMLMessage() {
		Runnable gui_update = new Runnable() {
			public void run() {
				JEditorPane editor = new JEditorPane();
				editor.setEditorKit(new HTMLEditorKit());
				editor.setText(m_message);

				TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, (java.awt.Frame) null, true);
				dlg.setTitle(I18N.getLocalizedMessage("Message"));
				dlg.setPrimaryPanel(new JScrollPane(editor));
				TSGuiToolbox.setReasonableWindowSize(dlg, TSGuiToolbox.getWindowDimension(10, 15));
				dlg.getOkButton().setVisible(false);
				dlg.showCenter();
			}
		};
		SwingUtilities.invokeLater(gui_update);
	}

}
