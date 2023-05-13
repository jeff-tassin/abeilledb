package com.jeta.abeille.gui.downloader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jeta.foundation.i18n.I18N;

public class TaskFactory {

	public static AbstractTask parse(byte[] xml) throws Exception {
		return parseDescriptor(new ByteArrayInputStream(xml));
	}

	public static AbstractTask parseDescriptor(InputStream istream) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(istream);

		Element root = document.getDocumentElement();
		root.normalize();
		return parseResource(root);
	}

	private static AbstractTask parseResource(Element element) throws Exception {
		TaskGroup grp = new TaskGroup();
		NodeList nlist = element.getChildNodes();
		for (int index = 0; index < nlist.getLength(); index++) {
			Node item = nlist.item(index);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) item;
				String elname = e.getNodeName();
				if (elname != null) {
					elname = elname.trim();
					if (elname.equalsIgnoreCase("task")) {
						grp.add(TaskFactory.parse(e));
					}
				}
			}
		}
		if (grp.size() == 0) {
			throw new Exception(I18N.getLocalizedMessage("Invalid Resource Descriptor"));
		}
		return grp;
	}

	public static AbstractTask parse(Element xml) throws Exception {
		NodeList nlist = xml.getChildNodes();

		String name = xml.getAttribute("name");
		String input = xml.getAttribute("input");
		String output = xml.getAttribute("output");
		String msg = xml.getAttribute("message");

		AbstractTask action = null;
		for (int index = 0; index < nlist.getLength(); index++) {
			Node item = nlist.item(index);
			if (item instanceof Element) {
				Element element = (Element) item;
				String elname = element.getNodeName();
				if (elname.equalsIgnoreCase("download")) {
					action = FileDownloader.parse(element);
					break;
				} else if (elname.equalsIgnoreCase("unzip")) {
					action = UnzipAction.parse(element);
					break;
				} else if (elname.equalsIgnoreCase("message")) {
					action = MessageAction.parse(element);
					break;
				}
			}
		}

		if (action == null) {
			throw new Exception(I18N.getLocalizedMessage("Invalid Resource Descriptor"));
		} else {
			action.setInput(input);
			action.setOutput(output);
			action.setName(name);
			action.setMessage(msg);
		}
		return action;
	}
}
