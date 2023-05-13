/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.html;

import java.util.*;

import javax.swing.text.*;

import org.netbeans.editor.ext.*;

/**
 * SyntaxElement is class internal to HTML Completion engine, it is used during
 * the analysis of the HTML code.
 * 
 * It is an element of the dynamically created chain of other SyntaxElements.
 * The access to it is done through the HTMLSyntaxSupport, which also takes care
 * of dynamically extending it when needed.
 * 
 * @author Petr Nejedly
 * @version 1.0
 */
class SyntaxElement {

	public static final int TYPE_COMMENT = 0;
	public static final int TYPE_DECLARATION = 1;
	public static final int TYPE_ERROR = 2;
	public static final int TYPE_TEXT = 3;
	public static final int TYPE_TAG = 4;
	public static final int TYPE_ENDTAG = 5;

	private HTMLSyntaxSupport support;
	private SyntaxElement previous;
	private SyntaxElement next;

	int offset;
	int length;
	int type;

	/** Creates new SyntaxElement */
	public SyntaxElement(HTMLSyntaxSupport support, int from, int to, int type) {
		this.support = support;
		this.offset = from;
		this.length = to - from;
		this.type = type;
	}

	public int getElementOffset() {
		return offset;
	}

	public int getElementLength() {
		return length;
	}

	public int getType() {
		return type;
	}

	public String getText() {
		try {
			return support.getDocument().getText(offset, length);
		} catch (BadLocationException exc) {
			// this could happen only when in inconsistent state
			throw new ConcurrentModificationException("SyntaxElement in inconsistent state"); // NOI18N
		}
	}

	public SyntaxElement getPrevious() throws BadLocationException {
		if (previous == null) {
			previous = support.getPreviousElement(offset);
			if (previous != null)
				previous.next = this;
		}
		return previous;
	}

	public SyntaxElement getNext() throws BadLocationException {
		if (next == null) {
			next = support.getNextElement(offset + length);
			if (next != null)
				next.previous = this;
		}
		return next;
	}

	public String toString() {
		return "Element(" + type + ")[" + offset + "," + (offset + length - 1) + "]"; // NOI18N
	}

	/**
	 * Declaration models SGML declaration with emphasis on &lt;!DOCTYPE
	 * declaration, as other declarations are not allowed inside HTML. It
	 * represents unknown/broken declaration or either public or system DOCTYPE
	 * declaration.
	 */
	static class Declaration extends SyntaxElement {
		private String root;
		private String publicID;
		private String file;

		/**
		 * Creates a model of SGML declaration with some properties of DOCTYPE
		 * declaration.
		 * 
		 * @param doctypeRootElement
		 *            the name of the root element for a DOCTYPE. Can be null to
		 *            express that the declaration is not DOCTYPE declaration or
		 *            is broken.
		 * @param doctypePI
		 *            public identifier for this DOCTYPE, if available. null for
		 *            system doctype or other/broken declaration.
		 * @param doctypeFile
		 *            system identifier for this DOCTYPE, if available. null
		 *            otherwise.
		 */
		public Declaration(HTMLSyntaxSupport support, int from, int to, String doctypeRootElement, String doctypePI,
				String doctypeFile) {
			super(support, from, to, TYPE_DECLARATION);
			root = doctypeRootElement;
			publicID = doctypePI;
			file = doctypeFile;
		}

		/**
		 * @return the name of the root element for a DOCTYPE declaration or
		 *         null if the declatarion is not DOCTYPE or is broken.
		 */
		public String getRootElement() {
			return root;
		}

		/**
		 * @return a public identifier of the PUBLIC DOCTYPE declaration or null
		 *         for SYSTEM DOCTYPE and broken or other declaration.
		 */
		public String getPublicIdentifier() {
			return publicID;
		}

		/**
		 * @return a system identifier of both PUBLIC and SYSTEM DOCTYPE
		 *         declaration or null for PUBLIC declaration with system
		 *         identifier not specified and broken or other declaration.
		 */
		public String getDoctypeFile() {
			return file;
		}

	}

	static class Named extends SyntaxElement {
		String name;

		public Named(HTMLSyntaxSupport support, int from, int to, int type, String name) {
			super(support, from, to, type);
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			return super.toString() + " - \"" + name + '"'; // NOI18N
		}
	}

	static class Tag extends SyntaxElement.Named {
		Collection attribs;

		public Tag(HTMLSyntaxSupport support, int from, int to, String name, Collection attribs) {
			super(support, from, to, TYPE_TAG, name);
			this.attribs = attribs;
		}

		public Collection getAttributes() {
			return attribs;
		}

		public String toString() {
			StringBuffer ret = new StringBuffer(super.toString());
			ret.append(" - {"); // NOI18N

			for (Iterator i = attribs.iterator(); i.hasNext();) {
				ret.append(i.next());
				ret.append(", "); // NOI18N
			}

			ret.append("}"); // NOI18N
			return ret.toString();
		}
	}

}
