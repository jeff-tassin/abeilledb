package com.jeta.abeille.gui.downloader;

import org.w3c.dom.Element;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.interfaces.utils.Base64;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.xml.XMLUtils;

public class EncodedValue {
	private String m_value;

	/*
	 * ctor
	 */
	private EncodedValue(String path) {
		m_value = path;
	}

	public String getDecodedValue() {
		return m_value;
	}

	public static EncodedValue parse(Element path_el) throws Exception {
		String base64 = XMLUtils.getChildText(path_el, "base64");
		String spec = XMLUtils.getChildText(path_el, "spec");
		if (base64 == null || spec == null) {
			throw new Exception(I18N.getLocalizedMessage("Invalid Resource Descriptor"));
		} else {
			if (base64.equalsIgnoreCase("true")) {
				Base64 decoder = (Base64) ComponentMgr.lookup(Base64.COMPONENT_ID);
				byte[] path = decoder.decode(spec);
				spec = new String(path);
			}
			return new EncodedValue(spec);
		}
	}

	public void print() {
		System.out.println(m_value);
	}

}
