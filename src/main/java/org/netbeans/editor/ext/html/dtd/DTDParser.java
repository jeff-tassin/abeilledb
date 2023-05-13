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
package org.netbeans.editor.ext.html.dtd;

import java.io.Reader;
import java.io.PushbackReader;
import java.io.IOException;
import java.util.*;

import org.netbeans.editor.ext.html.WeakHashSet;

/**
 * !!! Includes !!!! String->DTD.Element
 * 
 * @author Petr Nejedly
 * @version 0.2
 */
class DTDParser extends Object {

	// The provider used to provide the Readers for this DTD.
	private ReaderProvider provider = null;

	// Asks for Reader for given DTD.
	private Reader getReader(String identifier, String fileName) {
		if (provider == null)
			return null;
		return provider.getReaderForIdentifier(identifier, fileName);
	}

	/**
	 * Weak set for holding already created strings, to not create more
	 * instances of the same string
	 */
	private WeakHashSet stringCache = new WeakHashSet(131, 0.75f);

	/** Weak set of attributes - helps sharing common attributes */
	private WeakHashSet attributes = new WeakHashSet(23, 0.75f);

	/** Weak set of models */
	private WeakHashSet models = new WeakHashSet(131, 0.75f);

	/** Weak set of Contents */
	private WeakHashSet contents = new WeakHashSet(131, 0.75f);

	/**
	 * Temporal storage of all ContentLeafs that needs to get their elements
	 * filled in at the end of parsing
	 */
	Set leafs = new HashSet(131, 0.75f);

	/**
	 * Map of all character references. Mapping is String name -> DTD.CharRef
	 * instance
	 */
	private SortedMap charRefs = new TreeMap();

	/**
	 * Map holding partially completed instances of Element. Mapping is String
	 * name -> DTD.Element instance
	 */
	private SortedMap elementMap = new TreeMap();

	/**
	 * Map holding entities during creation of DTD. Mapping is String name ->
	 * String content. This map should not be used for direct put(..), because
	 * entities are defined by first declaration and can not be overriden.
	 */
	private Map entityMap = new HashMap();

	public DTD createDTD(ReaderProvider provider, String identifier, String fileName) throws WrongDTDException {
		this.provider = provider;

		Reader reader = getReader(identifier, fileName);
		if (reader == null)
			throw new WrongDTDException("Can't open Reader for public identifier " + identifier);
		try {
			parseDTD(new PushbackReader(reader, 1024 * 128));
		} catch (IOException e) {
			throw new WrongDTDException("IOException during parsing: " + e.getMessage());
		}

		// fixup includes and excludes of all elements
		for (Iterator it = elementMap.values().iterator(); it.hasNext();) {
			DTD.Element elem = (DTD.Element) it.next();
			ContentModelImpl cm = (ContentModelImpl) elem.getContentModel();

			Set newIncs = new HashSet();
			for (Iterator incIter = cm.included.iterator(); incIter.hasNext();) {
				Object oldElem;
				Object subElem = oldElem = incIter.next();
				if (subElem instanceof String) {
					subElem = elementMap.get(((String) subElem).toUpperCase());
				}
				if (subElem == null) {
					throw new WrongDTDException("'" + oldElem + "' element referenced from " + elem.getName()
							+ " not found throughout the DTD.");
				}
				newIncs.add(subElem);
			}
			cm.included = newIncs;

			Set newExcs = new HashSet();
			for (Iterator excIter = cm.excluded.iterator(); excIter.hasNext();) {
				Object oldElem;
				Object subElem = oldElem = excIter.next();
				if (subElem instanceof String) {
					subElem = elementMap.get(((String) subElem).toUpperCase());
				}
				if (subElem == null) {
					throw new WrongDTDException("'" + oldElem + "' element referenced from " + elem.getName()
							+ " not found throughout the DTD.");
				}
				newExcs.add(subElem);
			}
			cm.excluded = newExcs;
			cm.hashcode = cm.content.hashCode() + 2 * cm.included.hashCode() + 3 * cm.excluded.hashCode();
		}

		// fixup content leafs
		for (Iterator it = leafs.iterator(); it.hasNext();) {
			ContentLeafImpl leaf = (ContentLeafImpl) it.next();
			leaf.elem = (DTD.Element) elementMap.get(leaf.elemName);
		}

		return new DTDImpl(identifier, elementMap, charRefs);
	}

	/**
	 * Method for adding new entities to their map. Obeys the rule that entity,
	 * once defined, can not be overriden
	 */
	void addEntity(String name, String content) {
		if (entityMap.get(name) == null)
			entityMap.put(name, content);
	}

	/**
	 * Method for adding new entities to their map. Obeys the rule that entity,
	 * once defined, can not be overriden
	 */
	void addPublicEntity(String name, String identifier, String file) throws WrongDTDException {
		if (entityMap.get(name) == null) {

			StringBuffer sb = new StringBuffer();
			char[] buffer = new char[16384];
			Reader r = getReader(identifier, file);
			try {
				int len;
				while ((len = r.read(buffer)) >= 0) {
					sb.append(buffer, 0, len);
				}
			} catch (IOException e) {
				throw new WrongDTDException("Error reading included public entity " + name + " - " + e.getMessage());
			}

			entityMap.put(name, sb.toString());
		}
	}

	DTD.Value createValue(String name) {
		return new ValueImpl((String) stringCache.put(name));
	}

	/** Creates new or lookups old ContentModel with given properites */
	DTD.ContentModel createContentModel(DTD.Content content, Set included, Set excluded) {

		DTD.ContentModel cm = new ContentModelImpl(content, included, excluded);
		return (DTD.ContentModel) models.put(cm);
	}

	/** Creates new or lookups old ContentLeaf with given properites */
	DTD.Content createContentLeaf(String name) {
		DTD.Content c = new ContentLeafImpl(name);
		c = (DTD.Content) contents.put(c);
		leafs.add(c); // remember for final fixup
		return c;
	}

	/** Creates new or lookups old ContentNode with given properites */
	DTD.Content createContentNode(char type, DTD.Content subContent) {
		return (DTD.Content) contents.put(new UnaryContentNodeImpl(type, subContent));
	}

	/** Creates new or lookups old ContentNode with given properites */
	DTD.Content createContentNode(char type, DTD.Content[] subContent) {
		return (DTD.Content) contents.put(new MultiContentNodeImpl(type, subContent));
	}

	DTD.Element createElement(String name, DTD.ContentModel cm, boolean optStart, boolean optEnd) {
		DTD.Element retVal = new ElementImpl(name, cm, optStart, optEnd, new TreeMap());
		return retVal;
	}

	/** Creates new or lookups old attribute with given properites */
	DTD.Attribute createAttribute(String name, int type, String baseType, String typeHelper, String defaultMode,
			SortedMap values) {
		DTD.Attribute attr = new AttributeImpl(name, type, (String) stringCache.put(baseType),
				(String) stringCache.put(typeHelper), (String) stringCache.put(defaultMode), values);
		return (DTD.Attribute) attributes.put(attr);
	}

	/** Adds given instance of DTD.Attribute to Element named elemName */
	void addAttrToElement(String elemName, DTD.Attribute attr) throws WrongDTDException {
		ElementImpl elem = (ElementImpl) elementMap.get(elemName.toUpperCase());
		if (elem == null)
			throw new WrongDTDException("Attribute definition for unknown Element \"" + elemName + "\".");
		elem.addAttribute(attr);
	}

	void createAddCharRef(String name, char value) {
		DTD.CharRef ref = new CharRefImpl(name, value);
		charRefs.put(name, ref);
	}

	private boolean isNameChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' || c == ':';
	}

	/*----------------------------------------------------------------------------*/
	/*----------------------------- Parsing routines ---------------------------- */
	/*----------------------------------------------------------------------------*/
	private static final int DTD_INIT = 0;
	private static final int DTD_LT = 1; // after '<'
	private static final int DTD_EXC = 2; // after "<!"
	private static final int DTD_MINUS = 3; // after "<!-"
	private static final int DTD_ACOMMENT = 4; // after comment was parsed,
												// awaiting '>'

	private void parseDTD(PushbackReader in) throws IOException, WrongDTDException {
		int state = DTD_INIT;
		for (;;) {
			int i = in.read();
			if (i == -1) {
				break;
			}
			switch (state) {
			case DTD_INIT:
				switch (i) {
				case '<':
					state = DTD_LT;
					break;
				case '%':
					parseEntityReference(in);
					break; // Stay in DTD_INIT
				}
				break;

			case DTD_LT:
				if (i != '!')
					throw new WrongDTDException("Unexpected char '" + (char) i + "' after '<'");
				state = DTD_EXC;
				break;

			case DTD_EXC:
				switch (i) {
				case '-':
					state = DTD_MINUS;
					break;
				case '[':
					parseOptional(in);
					state = DTD_INIT;
					break;
				default:
					in.unread(i);
					parseMarkup(in);
					state = DTD_INIT;
					break;
				}
				break;

			case DTD_MINUS:
				if (i != '-')
					throw new WrongDTDException("Unexpected char '" + (char) i + "' after \"<!-\"");
				parseComment(in);
				state = DTD_ACOMMENT;
				break;

			case DTD_ACOMMENT:
				if (i != '>')
					throw new WrongDTDException("Unexpected char '" + (char) i + "' after comment");
				state = DTD_INIT;
				break;

			}
		}
		if (state != DTD_INIT)
			throw new WrongDTDException("Premature end of DTD"); // NOI18N
	}

	/**
	 * Parser that reads the markup type after <!. Recognizes ENTITY, ELEMENT
	 * and ATTLIST markup and forwards their processing to proper parser. It
	 * gets the control just after starting "<!" and releases after eating final
	 * '>'
	 */
	private void parseMarkup(PushbackReader in) throws IOException, WrongDTDException {
		StringBuffer sb = new StringBuffer();
		for (;;) {
			int i = in.read();
			if (i == -1)
				throw new WrongDTDException("Premature end of DTD"); // NOI18N
																		// EOF
			if (i == ' ')
				break;
			sb.append((char) i); // next char of name
		}

		String markup = sb.toString();

		if ("ENTITY".equals(markup)) {
			parseEntityDefinition(in);
		} else if ("ELEMENT".equals(markup)) {
			parseElement(in);
		} else if ("ATTLIST".equals(markup)) {
			parseAttlist(in);
		} else
			throw new WrongDTDException("Wrong DTD markup <!" + markup);
	}

	private static final int PED_INIT = 0;
	private static final int PED_PERCENT = 1;
	private static final int PED_CHAR = 2;
	private static final int PED_NAME = 3;
	private static final int PED_ANAME = 4;
	private static final int PED_VAL = 5;
	private static final int PED_TYPE = 6;
	private static final int PED_AVAL = 7;
	private static final int PED_AVAL_M = 8;
	private static final int PED_ATYPE = 9;
	private static final int PED_ID = 10;
	private static final int PED_AID = 11;
	private static final int PED_FILE = 12;
	private static final int PED_AFILE = 13;
	private static final int PED_AFILE_M = 14;
	private static final int PED_ACHAR = 15;
	private static final int PED_CH_TYPE = 16;
	private static final int PED_CH_ATYPE = 17;
	private static final int PED_CH_QUOT = 18;

	/* TODO: Parsing fo character references */
	private void parseEntityDefinition(PushbackReader in) throws IOException, WrongDTDException {
		int state = PED_INIT;
		StringBuffer name = new StringBuffer();
		StringBuffer value = new StringBuffer();
		StringBuffer type = new StringBuffer();
		StringBuffer identifier = new StringBuffer();

		for (;;) {
			int i = in.read();
			if (i == -1)
				throw new WrongDTDException("Premature end of DTD"); // NOI18N
																		// EOF
			switch (state) {
			case PED_INIT:
				if (Character.isWhitespace((char) i))
					break;
				if (i == '%')
					state = PED_PERCENT;
				else {
					name.append((char) i);
					state = PED_CHAR;
				}
				break;

			case PED_PERCENT:
				if (Character.isWhitespace((char) i))
					break;
				name.append((char) i);
				state = PED_NAME;
				break;

			case PED_NAME:
				if (Character.isWhitespace((char) i)) {
					state = PED_ANAME;
				} else {
					name.append((char) i);
				}
				break;

			case PED_ANAME:
				if (Character.isWhitespace((char) i))
					break;
				if (i == '"')
					state = PED_VAL;
				else {
					in.unread(i);
					state = PED_TYPE;
				}
				break;

			case PED_VAL:
				if (i == '"') {
					addEntity(name.toString(), value.toString());
					state = PED_AVAL;
				} else {
					value.append((char) i);
				}
				break;

			case PED_AVAL:
				if (i == '>') {
					return;
				}
				if (i == '-')
					state = PED_AVAL_M;
				break;

			case PED_AVAL_M:
				if (i == '-')
					parseComment(in);
				state = PED_AVAL;
				break;

			case PED_TYPE:
				if (Character.isWhitespace((char) i)) {
					if (type.toString().equals("PUBLIC")) {
						state = PED_ATYPE;
					} else {
						throw new WrongDTDException("Unexpected entity type \"" + type + "\".");
					}
				} else {
					type.append((char) i);
				}
				break;

			case PED_ATYPE:
				if (Character.isWhitespace((char) i))
					break;
				if (i == '"') {
					state = PED_ID;
					break;
				}
				throw new WrongDTDException("Unexpected char '" + (char) i + "' in PUBLIC entity.");

			case PED_ID:
				if (i == '"') {
					state = PED_AID;
				} else {
					identifier.append((char) i);
				}
				break;

			case PED_AID:
				if (Character.isWhitespace((char) i))
					break;
				if (i == '"') {
					state = PED_FILE;
					break;
				}
				if (i == '>') {
					addPublicEntity(name.toString(), identifier.toString(), null);
					return;
				}
				throw new WrongDTDException("Unexpected char '" + (char) i + "' in PUBLIC entity.");

			case PED_FILE:
				if (i == '"') {
					state = PED_AFILE;
				} else {
					value.append((char) i);
				}
				break;

			case PED_AFILE:
				if (Character.isWhitespace((char) i))
					break;
				if (i == '-') {
					state = PED_AFILE_M;
					break;
				}
				if (i == '>') {
					addPublicEntity(name.toString(), identifier.toString(), value.toString());
					return;
				}
				throw new WrongDTDException("Unexpected char '" + (char) i + "' in PUBLIC entity.");

			case PED_AFILE_M:
				if (i == '-') {
					parseComment(in);
					state = PED_FILE;
					break;
				}
				throw new WrongDTDException("Unexpected sequence \"-" + (char) i + "\" in in PUBLIC entity.");

			case PED_CHAR:
				if (Character.isWhitespace((char) i)) {
					state = PED_ACHAR;
				} else {
					name.append((char) i);
				}
				break;

			case PED_ACHAR:
				if (Character.isWhitespace((char) i))
					break;
				else {
					type.append((char) i);
					state = PED_CH_TYPE;
				}
				break;

			case PED_CH_TYPE:
				if (Character.isWhitespace((char) i)) {
					if (type.toString().equals("CDATA")) {
						state = PED_ATYPE;
						state = PED_CH_ATYPE;
					} else {
						throw new WrongDTDException("Unexpected entity type \"" + type + "\".");
					}
				} else {
					type.append((char) i);
				}
				break;

			case PED_CH_ATYPE:
				if (Character.isWhitespace((char) i))
					break;
				else if (i == '"') {
					state = PED_CH_QUOT;
				} else {
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in entity.");
				}
				break;

			case PED_CH_QUOT:
				if (i == '"') {
					value.delete(0, 2);
					value.deleteCharAt(value.length() - 1);
					int code = Integer.parseInt(value.toString());
					createAddCharRef(name.toString(), (char) code);
					state = PED_AVAL;
				} else {
					value.append((char) i);
				}
			}

		}
	}

	private static final int GR_INIT = 0;
	private static final int GR_NAME = 1;
	private static final int GR_ANAME = 2;

	/**
	 * Parse group of names separated by '|' character and optional spaces
	 * 
	 * @return List of Strings containing names
	 */
	private List parseGroup(PushbackReader in) throws IOException, WrongDTDException {
		int state = GR_INIT;
		StringBuffer name = new StringBuffer();
		List list = new ArrayList();

		for (;;) {
			int i = in.read();
			if (i == -1)
				throw new WrongDTDException("Premature end of DTD"); // NOI18N
																		// EOF
			switch (state) {
			case GR_INIT:
				if (Character.isWhitespace((char) i))
					break;
				if (i == '%') {
					parseEntityReference(in);
				} else {
					name.append((char) i);
					state = GR_NAME;
				}
				break;

			case GR_NAME:
				if (isNameChar((char) i)) {
					name.append((char) i);
					break;
				}
				switch (i) {
				case ')':
					list.add(name.toString());
					return list;
				case '|':
					list.add(name.toString());
					name.setLength(0);
					state = GR_INIT;
					break;
				default:
					if (Character.isWhitespace((char) i)) {
						list.add(name.toString());
						name.setLength(0);
						state = GR_ANAME;
						break;
					} else {
						throw new WrongDTDException("Unexpected char '" + (char) i + "' in group definition.");
					}
				}
				break;

			case GR_ANAME:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case ')':
					return list;
				case '|':
					state = GR_INIT;
					break;
				default:
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in group definition.");
				}
				break;
			}
		}

	}

	private static final int EL_INIT = 0;
	private static final int EL_NAME = 1;
	private static final int EL_ANAME = 2;
	private static final int EL_ASTART = 3;
	private static final int EL_ACONTENT = 4;
	private static final int EL_PLUS = 5;
	private static final int EL_MINUS = 6;

	/**
	 * parse the whole element(s) definition including content model. Create
	 * corresponding instances of DTD.Element filled with proper informations.
	 * Make the same content models and their contents shared across the DTD
	 */
	private void parseElement(PushbackReader in) throws IOException, WrongDTDException {
		int state = EL_INIT;
		StringBuffer name = new StringBuffer();
		List list = null;
		boolean optStart = false;
		boolean optEnd = false;
		DTD.Content content = null;
		Set inSet = new HashSet();
		Set exSet = new HashSet();

		for (;;) {
			int i = in.read();
			if (i == -1)
				break;
			switch (state) {
			case EL_INIT:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case '(':
					list = parseGroup(in);
					state = EL_ANAME;
					break;
				case '%':
					parseEntityReference(in);
					break; // Stay in EL_INIT
				default:
					name.append((char) i);
					state = EL_NAME;
					break;
				}
				break;

			case EL_NAME:
				if (Character.isWhitespace((char) i)) {
					state = EL_ANAME;
					list = new ArrayList();
					list.add(name.toString());
				} else {
					name.append((char) i);
				}
				break;

			case EL_ANAME:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case 'O':
					optStart = true; // fall fhrough
				case '-':
					state = EL_ASTART;
					break;
				default:
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in ELEMENT optStart definition.");
				}
				break;

			case EL_ASTART:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case 'O':
					optEnd = true; // fall fhrough
				case '-':
					content = parseContent(in);
					state = EL_ACONTENT;
					break;
				default:
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in ELEMENT optEnd definition.");
				}
				break;

			case EL_ACONTENT:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case '+':
					state = EL_PLUS;
					break;
				case '-':
					state = EL_MINUS;
					break;
				case '>':
					DTD.ContentModel cm = createContentModel(content, inSet, exSet);
					for (Iterator iter = list.iterator(); iter.hasNext();) {
						String key = ((String) iter.next()).toUpperCase();
						elementMap.put(key, createElement(key, cm, optStart, optEnd));
					}
					return;
				default:
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in ELEMENT definition.");
				}
				break;

			case EL_PLUS:
				if (i == '(') {
					state = EL_ACONTENT;
					inSet.addAll(parseGroup(in));
				} else {
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in ELEMENT definition.");
				}
				break;

			case EL_MINUS:
				switch (i) {
				case '(':
					state = EL_ACONTENT;
					List l = parseGroup(in);
					exSet.addAll(l);
					break;
				case '-':
					state = EL_ACONTENT;
					parseComment(in);
					break;
				default:
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in ELEMENT definition.");
				}
				break;
			}
		}

		// XXX
	}

	private static final int CO_INIT = 0;
	private static final int CO_NAME = 1;
	private static final int CO_AMODEL = 2;
	private static final int CO_AND = 3;
	private static final int CO_OR = 4;
	private static final int CO_SEQ = 5;
	private static final int CO_AGROUP = 6;

	/**
	 * This automata would parse content model definitions and return them as a
	 * Content instance of root of generated CM tree
	 */
	private DTD.Content parseContent(PushbackReader in) throws IOException, WrongDTDException {
		int state = EL_INIT;
		StringBuffer name = new StringBuffer();
		ArrayList list = null;
		DTD.Content content = null;

		for (;;) {
			int i = in.read();
			if (i == -1)
				break;
			switch (state) {
			case CO_INIT:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case '%':
					parseEntityReference(in);
					break; // Stay in CO_INIT
				case '(':
					content = parseContent(in);
					state = CO_AMODEL;
					break;
				default:
					name.append((char) i);
					state = CO_NAME;
					break;
				}
				break;

			case CO_NAME:
				if (isNameChar((char) i)) {
					name.append((char) i);
				} else {
					switch (i) {
					case '?':
					case '+':
					case '*':
						DTD.Content leaf = createContentLeaf(name.toString());
						return createContentNode((char) i, leaf);

					default:
						in.unread(i);
						return createContentLeaf(name.toString());
					}
				}
				break;

			case CO_AMODEL:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case '&':
					list = new ArrayList();
					list.add(content);
					list.add(parseContent(in));
					state = CO_AND;
					break;
				case '|':
					list = new ArrayList();
					list.add(content);
					list.add(parseContent(in));
					state = CO_OR;
					break;
				case ',':
					list = new ArrayList();
					list.add(content);
					list.add(parseContent(in));
					state = CO_SEQ;
					break;
				case ')':
					state = CO_AGROUP;
					break;
				default:
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in ELEMENT optEnd definition.");
				}
				break;

			case CO_AND:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case '&':
					list.add(parseContent(in));
					break;
				case ')':
					content = createContentNode('&', (DTD.Content[]) list.toArray(new DTD.Content[0]));
					state = CO_AGROUP;
					break;
				default:
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in ContentModel definition.");
				}
				break;

			case CO_OR:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case '|':
					list.add(parseContent(in));
					break;
				case ')':
					content = createContentNode('|', (DTD.Content[]) list.toArray(new DTD.Content[0]));
					state = CO_AGROUP;
					break;
				default:
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in ContentModel definition.");
				}
				break;

			case CO_SEQ:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case ',':
					list.add(parseContent(in));
					break;
				case ')':
					content = createContentNode(',', (DTD.Content[]) list.toArray(new DTD.Content[0]));
					state = CO_AGROUP;
					break;
				default:
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in ContentModel definition.");
				}
				break;

			case CO_AGROUP:
				if (Character.isWhitespace((char) i))
					return content;
				switch (i) {
				case '?':
				case '+':
				case '*':
					return createContentNode((char) i, content);
				default:
					in.unread(i);
					return content;
				}
			}
		}

		throw new WrongDTDException("Premature end of DTD"); // NOI18N EOF

	}

	private static final int ATT_INIT = 0;
	private static final int ATT_NAME = 1;
	private static final int ATT_ANAME = 2;
	private static final int ATT_ANAME_M = 3;
	private static final int ATT_VAR = 4;
	private static final int ATT_AVAR = 5;
	private static final int ATT_TYPE = 6;
	private static final int ATT_ATYPE = 7;
	private static final int ATT_MODE = 8;

	private void parseAttlist(PushbackReader in) throws IOException, WrongDTDException {
		int state = ATT_INIT;
		StringBuffer name = new StringBuffer();
		List list = null; // List of tag names for which are these attribs
		StringBuffer attr = new StringBuffer(); // name of attribute
		List values = null; // (list of possible values
		StringBuffer type = new StringBuffer(); // OR the type of attribute )
		String typeHelper = null; // AND name of entity
		StringBuffer mode = new StringBuffer(); // default mode of this attrib
		for (;;) {
			int i = in.read();
			if (i == -1)
				break;
			switch (state) {
			case ATT_INIT:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case '%':
					parseEntityReference(in);
					break; // Stay in ATT_INIT
				case '(':
					list = parseGroup(in);
					state = ATT_ANAME;
					break;
				default:
					name.append((char) i);
					state = ATT_NAME;
					break;
				}
				break;

			case ATT_NAME:
				if (Character.isWhitespace((char) i)) {
					list = new ArrayList();
					list.add(name.toString());
					state = ATT_ANAME;
					break;
				}
				name.append((char) i);
				break;

			case ATT_ANAME:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case '%':
					parseEntityReference(in);
					break; // Stay in ATT_ANAME
				case '-':
					state = ATT_ANAME_M;
					break;
				case '>':
					return;
				default:
					attr.append((char) i);
					state = ATT_VAR;
					break;
				}
				break;

			case ATT_ANAME_M:
				if (i == '-') {
					parseComment(in); // skip the comment
					state = ATT_ANAME;
				} else {
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in ATTLIST definition.");
				}
				break;

			case ATT_VAR:
				if (Character.isWhitespace((char) i)) {
					state = ATT_AVAR;
					break;
				}
				attr.append((char) i);
				break;

			case ATT_AVAR:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case '%':
					typeHelper = parseEntityReference(in);
					break; // Stay in ATT_AVAR
				case '(':
					values = parseGroup(in);
					state = ATT_ATYPE;
					break;
				default:
					type.append((char) i);
					state = ATT_TYPE;
					break;
				}
				break;

			case ATT_TYPE:
				if (Character.isWhitespace((char) i)) {
					state = ATT_ATYPE;
					break;
				}
				type.append((char) i);
				break;

			case ATT_ATYPE:
				if (Character.isWhitespace((char) i))
					break;
				switch (i) {
				case '%':
					parseEntityReference(in);
					break; // Stay in ATT_ATYPE
				default:
					mode.append((char) i);
					state = ATT_MODE;
					break;
				}
				break;

			case ATT_MODE:
				if (Character.isWhitespace((char) i)) {
					// Create attr and add it to all tags
					DTD.Attribute a = null;

					if (values == null) { // HOTSPOT for internation of
											// strings!!!
						a = createAttribute(attr.toString(), DTD.Attribute.TYPE_BASE, type.toString(), typeHelper,
								mode.toString(), null);
					} else if (values.size() == 1) {
						a = createAttribute(attr.toString(), DTD.Attribute.TYPE_BOOLEAN, null, typeHelper,
								mode.toString(), null);
					} else {
						SortedMap vals = new TreeMap();
						for (Iterator iter = values.iterator(); iter.hasNext();) {
							String valName = (String) iter.next();
							vals.put(valName, createValue(valName));
						}
						a = createAttribute(attr.toString(), DTD.Attribute.TYPE_SET, null, typeHelper, mode.toString(),
								vals);
					}
					for (Iterator iter = list.iterator(); iter.hasNext();) {
						addAttrToElement((String) iter.next(), a);
					}

					typeHelper = null;
					attr.setLength(0);
					type.setLength(0);
					mode.setLength(0);
					values = null;

					state = ATT_ANAME;
					break;
				}
				mode.append((char) i);
				break;
			}
		}
	}

	private static final int OPT_INIT = 0;
	private static final int OPT_PROCESS = 1;
	private static final int OPT_APROCESS = 2;
	private static final int OPT_CONTENT = 3;
	private static final int OPT_BRAC1 = 4;
	private static final int OPT_BRAC2 = 5;

	/**
	 * Parser that takes care of conditional inclusion/exclusion of part of DTD.
	 * Gets the control just after "<!["
	 */
	private void parseOptional(PushbackReader in) throws IOException, WrongDTDException {
		int state = OPT_INIT;
		StringBuffer process = new StringBuffer();
		StringBuffer content = new StringBuffer();
		boolean ignore = false;

		for (;;) {
			int i = in.read();
			if (i == -1)
				break; // EOF
			switch (state) {
			case OPT_INIT:
				if (Character.isWhitespace((char) i))
					break;
				if (i == '%') {
					parseEntityReference(in);
					break;
				}
				process.append((char) i);
				state = OPT_PROCESS;
				break;

			case OPT_PROCESS:
				if (Character.isWhitespace((char) i)) {
					String s = process.toString();
					if ("IGNORE".equals(s))
						ignore = true;
					else if (!"INCLUDE".equals(s))
						throw new WrongDTDException("Unexpected processing instruction " + s);
					state = OPT_APROCESS;
				} else {
					process.append((char) i);
				}
				break;

			case OPT_APROCESS:
				if (Character.isWhitespace((char) i))
					break;
				if (i == '[')
					state = OPT_CONTENT;
				else
					throw new WrongDTDException("Unexpected char '" + (char) i + "' in processing instruction.");
				break;

			case OPT_CONTENT:
				if (i == ']')
					state = OPT_BRAC1;
				else
					content.append((char) i);
				break;

			case OPT_BRAC1:
				if (i == ']')
					state = OPT_BRAC2;
				else {
					content.append(']').append((char) i);
					state = OPT_CONTENT;
				}
				break;

			case OPT_BRAC2:
				if (Character.isWhitespace((char) i))
					break;
				if (i == '>') {
					if (!ignore)
						in.unread(content.toString().toCharArray());
					return;
				}
				throw new WrongDTDException("Unexpected char '" + (char) i + "' in processing instruction.");
			}
		}

	}

	private static final int COMM_TEXT = 0; // anywhere in text
	private static final int COMM_DASH = 1; // after '-'

	/** Parser that eats everything until two consecutive dashes (inclusive) */
	private void parseComment(PushbackReader in) throws IOException, WrongDTDException {
		int state = COMM_TEXT;
		for (;;) {
			int i = in.read();
			if (i == -1)
				break; // EOF
			switch (state) {
			case COMM_TEXT:
				if (i == '-')
					state = COMM_DASH;
				break;
			case COMM_DASH:
				if (i == '-')
					return; // finished eating comment
				state = COMM_TEXT;
				break;
			}
		}
		throw new WrongDTDException("Premature end of DTD"); // NOI18N
	}

	/**
	 * Parser that reads the name of entity reference and replace it with the
	 * content of that entity (using the pushback capability of input). It gets
	 * the control just after starting '%'
	 * 
	 * @returns the name of reference which was replaced.
	 */
	private String parseEntityReference(PushbackReader in) throws IOException, WrongDTDException {
		StringBuffer sb = new StringBuffer();
		for (;;) {
			int i = in.read();
			if (i == -1)
				break; // EOF
			if (isNameChar((char) i)) {
				sb.append((char) i); // next char of name
			} else {
				String entValue = (String) entityMap.get(sb.toString()); // get
																			// the
																			// entity
																			// content
				if (entValue == null)
					throw new WrongDTDException("No such entity: \"" + sb + "\"");

				if (i != ';')
					in.unread(i);
				in.unread(entValue.toCharArray()); // push it back to stream
				return sb.toString();
			}
		}
		throw new WrongDTDException("Premature end of DTD"); // NOI18N
	}

	public static class WrongDTDException extends Exception {
		public WrongDTDException(String reason) {
			super(reason);
		}
	}

	/*----------------------------------------------------------------------------*/
	/*---------- Implementation of classes this factory uses as results ----------*/
	/*----------------------------------------------------------------------------*/

	/** Implementation of the DTD which this DTDcreator works as factory for. */
	private static class DTDImpl implements DTD {
		private String id;
		private SortedMap elements;
		private SortedMap charRefs;

		DTDImpl(String identifier, SortedMap elements, SortedMap charRefs) {
			this.id = identifier;
			this.elements = elements;
			this.charRefs = charRefs;
		}

		/** Identify this instance of DTD */
		public String getIdentifier() {
			return id;
		}

		/** Get List of all Elements whose names starts with given prefix */
		public List getElementList(String prefix) {
			List l = new ArrayList();
			prefix = prefix == null ? "" : prefix.toUpperCase();
			Iterator i = elements.tailMap(prefix).entrySet().iterator();

			while (i.hasNext()) {
				Map.Entry entry = (Map.Entry) i.next();
				if (((String) entry.getKey()).startsWith(prefix)) {
					l.add(entry.getValue());
				} else { // we're getting data from SortedSet, so when any
					break; // entry fails, all remaining entry would fail.
				}
			}

			return l;
		}

		/** Get the Element of given name. */
		public DTD.Element getElement(String name) {
			return (DTD.Element) elements.get(name);
		}

		/** Get List of all CharRefs whose aliases starts with given prefix. */
		public List getCharRefList(String prefix) {
			List l = new ArrayList();
			Iterator i = charRefs.tailMap(prefix).entrySet().iterator();

			while (i.hasNext()) {
				Map.Entry entry = (Map.Entry) i.next();
				if (((String) entry.getKey()).startsWith(prefix)) {
					l.add(entry.getValue());
				} else { // we're getting data from SortedSet, so when any
					break; // entry fails, all remaining entry would fail.
				}
			}

			return l;
		}

		/** Get the CharRef of given name */
		public DTD.CharRef getCharRef(String name) {
			return (DTD.CharRef) charRefs.get(name);
		}

		public String toString() {
			return super.toString() + "[id=" + id + ", elements=" + elements + ",charRefs=" + charRefs + "]"; // NOI18N
		}
	}

	/** Implementation of Element used by this DTDcreator. */
	private static class ElementImpl implements DTD.Element {

		private String name;
		private DTD.ContentModel model;
		private boolean optStart;
		private boolean optEnd;
		private SortedMap attributes; // these are sorted just by name
		private DTD dtd;

		ElementImpl(String name, DTD.ContentModel model, boolean optStart, boolean optEnd, SortedMap attributes) {
			this.name = name;
			this.model = model;
			this.optStart = optStart;
			this.optEnd = optEnd;
			this.attributes = attributes;
		}

		/** Get the name of this Element */
		public String getName() {
			return name;
		}

		/** Shorthand to resolving if content model of this Element is EMPTY */
		public boolean isEmpty() {
			if (optEnd && model.getContent() instanceof DTD.ContentLeaf)
				return true;
			// && ((DTD.ContentLeaf)model.getContent()).getName().equals(
			// "EMPTY" ) ) return true;
			return false;
		}

		/** Tells if this Element has optional Start Tag. */
		public boolean hasOptionalStart() {
			return optStart;
		}

		/** Tells if this Element has optional End Tag. */
		public boolean hasOptionalEnd() {
			return optEnd;
		}

		/**
		 * Get the List of Attributes of this Element, which starts with given
		 * <CODE>prefix</CODE>.
		 */
		public List getAttributeList(String prefix) {
			TreeSet set = new TreeSet(new Comparator() {
				public int compare(Object o1, Object o2) {
					if (isRequired(o1) && !isRequired(o2))
						return -1;
					if (!isRequired(o1) && isRequired(o2))
						return 1;
					return ((DTD.Attribute) o1).getName().compareTo(((DTD.Attribute) o2).getName());
				}

				private final boolean isRequired(Object o) {
					return ((DTD.Attribute) o).getDefaultMode().equals(DTD.Attribute.MODE_REQUIRED);
				}
			});
			prefix = prefix.toLowerCase();
			Iterator i = attributes.tailMap(prefix).entrySet().iterator();

			while (i.hasNext()) {
				Map.Entry entry = (Map.Entry) i.next();
				if (((String) entry.getKey()).startsWith(prefix)) {
					set.add(entry.getValue());
				} else { // we're getting data from SortedSet, so when any
					break; // entry fails, all remaining entry would fail.
				}
			}
			return new ArrayList(set);
		}

		/** Get the Attribute of given name. */
		public DTD.Attribute getAttribute(String name) {
			return (DTD.Attribute) attributes.get(name);
		}

		void addAttribute(DTD.Attribute attr) {
			attributes.put(attr.getName(), attr);
		}

		/** Get the content model of this Element */
		public DTD.ContentModel getContentModel() {
			return model;
		}

		public String toString() {
			return super.toString() + "[" + name + (optStart ? " O" : " -") + (optEnd ? " O " : " - ") + model
					+ " attribs=" + attributes + "]"; // NOI18
		}
	}

	/** */
	public static class AttributeImpl implements DTD.Attribute {

		private String name;
		private int type;
		private String baseType;
		private String typeHelper;
		private String defaultMode;
		private SortedMap values;
		private int hashcode;

		public AttributeImpl(String name, int type, String baseType, String typeHelper, String defaultMode,
				SortedMap values) {
			this.name = name;
			this.type = type;
			this.baseType = baseType;
			this.typeHelper = typeHelper;
			this.defaultMode = defaultMode;
			this.values = values;
			hashcode = name.hashCode() * (type + 1) * (baseType == null ? 1 : baseType.hashCode())
					+ (typeHelper == null ? 1 : typeHelper.hashCode()) + defaultMode.hashCode()
					+ (values == null ? 1 : values.hashCode());
		}

		/** @return name of this attribute */
		public String getName() {
			return name;
		}

		/** @return type of this attribute */
		public int getType() {
			return type;
		}

		public String getBaseType() {
			return baseType;
		}

		/**
		 * The last entity name through which was this Attribute's type defined.
		 */
		public String getTypeHelper() {
			return typeHelper;
		}

		/** This method is used to obtain default value information. */
		public String getDefaultMode() {
			return defaultMode;
		}

		/** Shorthand for determining if defaultMode is "#REQUIRED" */
		public boolean isRequired() {
			return defaultMode.equals(MODE_REQUIRED);
		}

		/**
		 * The way how to obtain possible values for TYPE_SET Attributes
		 * 
		 * @param prefix
		 *            required prefix, or <CODE>null</CODE>, if all possible
		 *            values are required.
		 * @return List of Values starting with prefix, from this attribute if
		 *         it is of TYPE_SET. For other types, it doesn't make a sense
		 *         and returns null.
		 */
		public List getValueList(String prefix) {
			if (type != TYPE_SET)
				return null;

			if (prefix == null)
				prefix = "";
			else
				prefix = prefix.toLowerCase();

			List retVal = new ArrayList();
			Iterator i = values.tailMap(prefix).entrySet().iterator();

			while (i.hasNext()) {
				Map.Entry entry = (Map.Entry) i.next();
				if (((String) entry.getKey()).startsWith(prefix)) {
					retVal.add(entry.getValue());
				} else { // we're getting data from SortedSet, so when any
					break; // entry fails, all remaining entry would fail.
				}
			}
			return retVal;
		}

		/** Get the value of given name. */
		public DTD.Value getValue(String name) {
			return (DTD.Value) values.get(name);
		}

		public String toString() {
			if (type == TYPE_SET) {
				return name + " " + values + "[" + typeHelper + "] " + defaultMode; // NOI18
			} else if (type == TYPE_BOOLEAN) {
				return name + " (" + name + ")[" + typeHelper + "] " + defaultMode; // NOI18
			} else {
				return name + " " + baseType + "[" + typeHelper + "] " + defaultMode; // NOI18
			}
		}

		public int hashCode() {
			return hashcode;
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof AttributeImpl))
				return false;
			AttributeImpl a = (AttributeImpl) obj;
			return (hashcode == a.hashcode && name.equals(a.name) && type == a.type
					&& (baseType == a.baseType || baseType != null && baseType.equals(a.baseType))
					&& (typeHelper == a.typeHelper || typeHelper != null && typeHelper.equals(a.typeHelper))
					&& defaultMode.equals(a.defaultMode) && (values == a.values || values != null
					&& values.equals(a.values)));
		}
	}

	private static class ValueImpl implements DTD.Value {
		String name;

		ValueImpl(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof ValueImpl))
				return false;
			return name.equals(((ValueImpl) obj).name);
		}

		public int hashCode() {
			return name.hashCode();
		}

		public String toString() {
			return name;
		}
	}

	private static class CharRefImpl implements DTD.CharRef {
		private String name;
		private char value;

		CharRefImpl(String name, char value) {
			this.name = name;
			this.value = value;
		}

		/** @return alias to this CharRef */
		public String getName() {
			return name;
		}

		/** @return the character this alias is for */
		public char getValue() {
			return value;
		}

		public String toString() {
			return name + "->'" + value + "'(&#" + (int) value + ";)";
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof CharRefImpl))
				return false;
			return name.equals(((CharRefImpl) obj).name) && value == ((CharRefImpl) obj).value;
		}

		public int hashCode() {
			return name.hashCode() * value;
		}
	}

	/** The implementation of ContentModel. It is immutable */
	private static class ContentModelImpl implements DTD.ContentModel {
		int hashcode;
		DTD.Content content;
		Set included;
		Set excluded;

		public ContentModelImpl(DTD.Content content, Set included, Set excluded) {
			this.content = content;
			this.included = included;
			this.excluded = excluded;
			hashcode = content.hashCode() + 2 * included.hashCode() + 3 * excluded.hashCode();
		}

		/** @return the Content tree part of this model */
		public DTD.Content getContent() {
			return content;
		}

		/** @return Set of Element names which are recursively included. */
		public Set getIncludes() {
			return included;
		}

		/** @return Set of Element names which are recursively excluded. */
		public Set getExcludes() {
			return excluded;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer(content.toString());

			if (!included.isEmpty()) {
				sb.append(" +(");
				Iterator i = included.iterator();
				for (;;) {
					sb.append(((DTD.Element) i.next()).getName());
					if (i.hasNext())
						sb.append("|");
					else
						break;
				}
				sb.append(")");
			}

			if (!excluded.isEmpty()) {
				sb.append(" -(");
				Iterator i = excluded.iterator();
				for (;;) {
					sb.append(((DTD.Element) i.next()).getName());
					if (i.hasNext())
						sb.append("|");
					else
						break;
				}
				sb.append(")");
			}

			return sb.toString();
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof ContentModelImpl))
				return false;
			ContentModelImpl cmi = (ContentModelImpl) obj;
			return content.equals(cmi.content) && included.equals(cmi.included) && excluded.equals(cmi.excluded);
		}

		public int hashCode() {
			return hashcode;
		}

	}

	/**
	 * ContentLeaf is leaf of content tree, matches just one Element name
	 * (String)
	 */
	static class ContentLeafImpl implements DTD.ContentLeaf {
		String elemName;
		DTD.Element elem;

		public ContentLeafImpl(String name) {
			this.elemName = name;
		}

		/** get the name of leaf Element */
		public String getName() {
			return elemName;
		}

		public DTD.Element getElement() {
			return elem;
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof ContentLeafImpl))
				return false;
			return elemName.equals(((ContentLeafImpl) obj).elemName);
		}

		public int hashCode() {
			return elemName.hashCode();
		}

		public String toString() {
			return elemName;
		}

		/** ContentLeaf can't be discarded as it hac no operation associated */
		public boolean isDiscardable() {
			return false;
		}

		/**
		 * ContentLeaf is either reduced to EMPTY_CONTENT or doesn't match at
		 * all
		 */
		public DTD.Content reduce(String elementName) {
			if (elemName.equals(elementName))
				return EMPTY_CONTENT;
			return null;
		}

		public Set getPossibleElements() {
			Set s = new HashSet();
			s.add(elem);
			return s;
		}
	}

	/** ContentNode is node of content tree */
	private static class UnaryContentNodeImpl implements DTD.ContentNode {
		int hashcode;
		char type;
		DTD.Content content;

		/* Constructor for unary ContentNodes */
		public UnaryContentNodeImpl(char type, DTD.Content content) {
			// sanity check:
			if (type != '*' && type != '?' && type != '+') {
				throw new IllegalArgumentException("Unknown unary content type '" + type + "'");
			}

			this.type = type;
			this.content = content;
			hashcode = type + content.hashCode();
		}

		/** This is node, always return false */
		public boolean isLeaf() {
			return false;
		}

		/** Get the operator for this node */
		public char getType() {
			return type;
		}

		/** Get the content of this node */
		public DTD.Content[] getContent() {
			return new DTD.Content[] { content };
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof UnaryContentNodeImpl))
				return false;
			return type == ((UnaryContentNodeImpl) obj).type && content.equals(((UnaryContentNodeImpl) obj).content);
		}

		public int hashCode() {
			return hashcode;
		}

		public String toString() {
			return content.toString() + type;
		}

		public boolean isDiscardable() {
			if (type == '*' || type == '?')
				return true;
			// The only remaining type, don't check: if( type == '+' )
			return content.isDiscardable();
		}

		public DTD.Content reduce(String elementName) {
			DTD.Content sub = content.reduce(elementName);
			if (sub == null)
				return null;
			if (sub == EMPTY_CONTENT) {
				if (type == '?')
					return EMPTY_CONTENT;
				if (type == '*')
					return this;
				// '+' is the last one: if( type == '+' )
				// we fullfilled the '+' rule, remainder is '*'
				return new UnaryContentNodeImpl('*', content);
			}
			if (type == '?')
				return sub;
			DTD.Content second = (type == '*') ? this : new UnaryContentNodeImpl('*', content);
			return new MultiContentNodeImpl(',', new DTD.Content[] { sub, second });
		}

		public Set getPossibleElements() {
			return content.getPossibleElements();
		}

	}

	/** ContentNodeImpl is n-ary node of content tree */
	private static class MultiContentNodeImpl implements DTD.ContentNode {
		int hashcode;
		char type;
		DTD.Content[] content;

		/* Constructor for n-ary ContentNodes */
		public MultiContentNodeImpl(char type, DTD.Content[] content) {
			// sanity check:
			if (type != '|' && type != '&' && type != ',') {
				throw new IllegalArgumentException("Unknown n-ary content type '" + type + "'");
			}

			this.type = type;
			this.content = content;
			hashcode = type;
			for (int i = 0; i < content.length; i++) {
				hashcode += content[i].hashCode();
			}
		}

		/** This is node, always return false */
		public boolean isLeaf() {
			return false;
		}

		/** Get the operator for this node */
		public char getType() {
			return type;
		}

		/** Get the content of this node */
		public DTD.Content[] getContent() {
			return content;
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof MultiContentNodeImpl))
				return false;
			return type == ((MultiContentNodeImpl) obj).type
					&& Arrays.equals(content, ((MultiContentNodeImpl) obj).content);
		}

		public String toString() {
			StringBuffer sb = new StringBuffer("(");
			for (int i = 0; i < content.length; i++) {
				sb.append(content[i].toString());
				if (i + 1 < content.length)
					sb.append(type);
			}
			sb.append(')');
			return sb.toString();
		}

		public int hashCode() {
			return hashcode;
		}

		public boolean isDiscardable() {
			if (type == '&' || type == ',') {
				for (int i = 0; i < content.length; i++) {
					if (!content[i].isDiscardable())
						return false;
				}
				return true;
			}
			// The only remaining type, don't check: if( type == '|' )
			for (int i = 0; i < content.length; i++) {
				if (content[i].isDiscardable())
					return true;
			}
			return false;
		}

		public DTD.Content reduce(String elementName) {

			if (type == '|') {
				for (int index = 0; index < content.length; index++) {
					DTD.Content sub = content[index].reduce(elementName);
					if (sub != null)
						return sub;
				}
				return null;
			} else if (type == ',') {
				// everything before index doesn't match and is discardable
				int index = 0;

				while (index < content.length) {
					DTD.Content sub = content[index].reduce(elementName);
					// element of sequence still don't match and isn't
					// discardable:
					if (sub == null && !content[index].isDiscardable())
						return null;

					// Element matches fully:
					if (sub == EMPTY_CONTENT) {
						int newLen = content.length - index - 1;
						if (newLen > 1) { // resulting sequence contains 2+
											// elements
							DTD.Content[] newSub = new DTD.Content[newLen];
							System.arraycopy(content, index + 1, newSub, 0, newLen);
							return new MultiContentNodeImpl(',', newSub);
						} else { // resulting sequence is one-item only
							return content[index + 1];
						}
					}

					// Element matches and is modified
					if (sub != null) {
						int newLen = content.length - index;
						if (newLen > 1) { // resulting sequence contains 2+
											// elements
							DTD.Content[] newSub = new DTD.Content[newLen];
							System.arraycopy(content, index + 1, newSub, 1, newLen - 1);
							newSub[0] = sub;
							return new MultiContentNodeImpl(',', newSub);
						} else { // resulting sequence is one modified item only
							return sub;
						}
					}
					index++; // discard the first element and try again
				}

				return null; // Doesn't match at all
			} else { // only '&' remains: if( type == '&' ) {
				for (int index = 0; index < content.length; index++) {
					DTD.Content sub = content[index].reduce(elementName);
					if (sub == EMPTY_CONTENT) {
						int newLen = content.length - 1;
						if (newLen > 1) {
							DTD.Content[] newSub = new DTD.Content[newLen];
							System.arraycopy(content, 0, newSub, 0, index);
							if (index < newSub.length) {
								System.arraycopy(content, index + 1, newSub, index, newLen - index);
							}
							return new MultiContentNodeImpl('&', newSub);
						} else {
							return content[1 - index];
						}
					}
					if (sub != null) {
						DTD.Content right;
						if (content.length > 1) {
							int newLen = content.length - 1;
							DTD.Content[] newSub = new DTD.Content[newLen];
							System.arraycopy(content, 0, newSub, 0, index);
							if (index < newSub.length) {
								System.arraycopy(content, index + 1, newSub, index, newLen - index);
							}
							right = new MultiContentNodeImpl('&', newSub);
						} else {
							right = content[1 - index];
						}
						return new MultiContentNodeImpl(',', new DTD.Content[] { sub, right });
					}
				}
				return null;

			}
		}

		public Set getPossibleElements() {
			Set retVal = new HashSet(11);

			if (type == '|' || type == '&') {
				for (int index = 0; index < content.length; index++)
					retVal.addAll(content[index].getPossibleElements());

			} else { // only ',' remains if( type == ',' ) {}
				int index = 0;
				while (index < content.length) {
					retVal.addAll(content[index].getPossibleElements());
					if (!content[index].isDiscardable())
						break;
					index++;
				}
			}
			return retVal;
		}
	}

}
