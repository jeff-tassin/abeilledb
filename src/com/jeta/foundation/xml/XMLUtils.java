/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.xml;

import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.i18n.I18NHelper;
import com.jeta.foundation.interfaces.resources.ResourceLoader;

/**
 * Helper class for manipulating JDCOM XML objects
 * 
 * @author Jeff Tassin
 */
public class XMLUtils {

	/**
	 * Finds the first child node of the given element that has the given tag
	 * name. <el> <childtag>value</childtag> </el>
	 * 
	 * @param el
	 *            the elemnt to search
	 * @param childtag
	 *            the tag name of the child
	 * @return the first child node that has the given tag name
	 */
	public static Node getChildNode(org.w3c.dom.Element el, String childtag) {
		Node result = null;
		NodeList nlist = el.getElementsByTagName(childtag);
		if (nlist.getLength() > 0) {
			result = nlist.item(0);
		}
		return result;
	}

	/**
	 * Finds the first child node of the given element that has the given tag
	 * name. If the child is found and has a single text value, that value is
	 * returned. <el> <childtag>value</childtag> </el>
	 * 
	 * @param el
	 *            the element to search for the given child
	 * @param childtag
	 *            the tag name for the child node whose value we wish to
	 *            retrieve.
	 * @return the child value. Null is returned if the child is not found in
	 *         the given element.
	 */
	public static String getChildText(org.w3c.dom.Element el, String childtag) {
		String result = null;
		org.w3c.dom.Element childel = (Element) getChildNode(el, childtag);
		if (childel != null) {
			result = XMLUtils.getNodeValue(childel);
		}
		return result;
	}

	/**
	 * @return the node value of the given element. This assumes that the
	 *         element has a single child node and that node is a is a
	 *         TEXT_NODE. e.g. <elem>text value</elem>
	 * 
	 *         Null is returned otherwise.
	 */
	public static String getNodeValue(Element elem) {
		Node e = elem.getFirstChild();

		String result = null;
		if (e != null && e.getNodeType() == Node.TEXT_NODE) {
			result = e.getNodeValue();
			if (result != null)
				result = result.trim();
		}

		return result;
	}

}
