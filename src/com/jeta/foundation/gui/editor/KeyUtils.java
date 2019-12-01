/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.editor.Utilities;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;

import com.jeta.foundation.xml.XMLUtils;

/**
 * This class maps a key event code to the VK string. It is used mainly to help
 * store/load key stroke to/from XML files
 * 
 * @author Jeff Tassin
 */
public class KeyUtils {
	private static HashMap m_keycodes;
	private static HashMap m_keytext;

	static {
		m_keycodes = new HashMap();
		m_keytext = new HashMap();

		m_keycodes.put("VK_1", new Integer(KeyEvent.VK_1));
		m_keycodes.put("VK_2", new Integer(KeyEvent.VK_2));
		m_keycodes.put("VK_3", new Integer(KeyEvent.VK_3));
		m_keycodes.put("VK_4", new Integer(KeyEvent.VK_4));
		m_keycodes.put("VK_5", new Integer(KeyEvent.VK_5));
		m_keycodes.put("VK_6", new Integer(KeyEvent.VK_6));
		m_keycodes.put("VK_7", new Integer(KeyEvent.VK_7));
		m_keycodes.put("VK_8", new Integer(KeyEvent.VK_8));
		m_keycodes.put("VK_9", new Integer(KeyEvent.VK_9));
		m_keycodes.put("VK_A", new Integer(KeyEvent.VK_A));
		m_keycodes.put("VK_ACCEPT", new Integer(KeyEvent.VK_ACCEPT));
		m_keycodes.put("VK_ADD", new Integer(KeyEvent.VK_ADD));
		m_keycodes.put("VK_AGAIN", new Integer(KeyEvent.VK_AGAIN));
		m_keycodes.put("VK_ALL_CANDIDATES", new Integer(KeyEvent.VK_ALL_CANDIDATES));
		m_keycodes.put("VK_ALPHANUMERIC", new Integer(KeyEvent.VK_ALPHANUMERIC));
		m_keycodes.put("VK_ALT", new Integer(KeyEvent.VK_ALT));
		m_keycodes.put("VK_ALT_GRAPH", new Integer(KeyEvent.VK_ALT_GRAPH));
		m_keycodes.put("VK_AMPERSAND", new Integer(KeyEvent.VK_AMPERSAND));
		m_keycodes.put("VK_ASTERISK", new Integer(KeyEvent.VK_ASTERISK));
		m_keycodes.put("VK_AT", new Integer(KeyEvent.VK_AT));
		m_keycodes.put("VK_B", new Integer(KeyEvent.VK_B));
		m_keycodes.put("VK_BACK_QUOTE", new Integer(KeyEvent.VK_BACK_QUOTE));
		m_keycodes.put("VK_BACK_SLASH", new Integer(KeyEvent.VK_BACK_SLASH));
		m_keycodes.put("VK_BACK_SPACE", new Integer(KeyEvent.VK_BACK_SPACE));
		m_keycodes.put("VK_BRACELEFT", new Integer(KeyEvent.VK_BRACELEFT));
		m_keycodes.put("VK_BRACERIGHT", new Integer(KeyEvent.VK_BRACERIGHT));
		m_keycodes.put("VK_C", new Integer(KeyEvent.VK_C));
		m_keycodes.put("VK_CANCEL", new Integer(KeyEvent.VK_CANCEL));
		m_keycodes.put("VK_CAPS_LOCK", new Integer(KeyEvent.VK_CAPS_LOCK));
		m_keycodes.put("VK_CIRCUMFLEX", new Integer(KeyEvent.VK_CIRCUMFLEX));
		m_keycodes.put("VK_CLEAR", new Integer(KeyEvent.VK_CLEAR));
		m_keycodes.put("VK_CLOSE_BRACKET", new Integer(KeyEvent.VK_CLOSE_BRACKET));
		m_keycodes.put("VK_CODE_INPUT", new Integer(KeyEvent.VK_CODE_INPUT));
		m_keycodes.put("VK_COLON", new Integer(KeyEvent.VK_COLON));
		m_keycodes.put("VK_COMMA", new Integer(KeyEvent.VK_COMMA));
		m_keycodes.put("VK_COMPOSE", new Integer(KeyEvent.VK_COMPOSE));
		m_keycodes.put("VK_CONTROL", new Integer(KeyEvent.VK_CONTROL));
		m_keycodes.put("VK_CONVERT", new Integer(KeyEvent.VK_CONVERT));
		m_keycodes.put("VK_COPY", new Integer(KeyEvent.VK_COPY));
		m_keycodes.put("VK_CUT", new Integer(KeyEvent.VK_CUT));
		m_keycodes.put("VK_D", new Integer(KeyEvent.VK_D));
		m_keycodes.put("VK_DEAD_ABOVEDOT", new Integer(KeyEvent.VK_DEAD_ABOVEDOT));
		m_keycodes.put("VK_DEAD_ABOVERING", new Integer(KeyEvent.VK_DEAD_ABOVERING));
		m_keycodes.put("VK_DEAD_ACUTE", new Integer(KeyEvent.VK_DEAD_ACUTE));
		m_keycodes.put("VK_DEAD_BREVE", new Integer(KeyEvent.VK_DEAD_BREVE));
		m_keycodes.put("VK_DEAD_CARON", new Integer(KeyEvent.VK_DEAD_CARON));
		m_keycodes.put("VK_DEAD_CEDILLA", new Integer(KeyEvent.VK_DEAD_CEDILLA));
		m_keycodes.put("VK_DEAD_CIRCUMFLEX", new Integer(KeyEvent.VK_DEAD_CIRCUMFLEX));
		m_keycodes.put("VK_DEAD_DIAERESIS", new Integer(KeyEvent.VK_DEAD_DIAERESIS));
		m_keycodes.put("VK_DEAD_DOUBLEACUTE", new Integer(KeyEvent.VK_DEAD_DOUBLEACUTE));
		m_keycodes.put("VK_DEAD_GRAVE", new Integer(KeyEvent.VK_DEAD_GRAVE));
		m_keycodes.put("VK_DEAD_IOTA", new Integer(KeyEvent.VK_DEAD_IOTA));
		m_keycodes.put("VK_DEAD_MACRON", new Integer(KeyEvent.VK_DEAD_MACRON));
		m_keycodes.put("VK_DEAD_OGONEK", new Integer(KeyEvent.VK_DEAD_OGONEK));
		m_keycodes.put("VK_DEAD_SEMIVOICED_SOUND", new Integer(KeyEvent.VK_DEAD_SEMIVOICED_SOUND));
		m_keycodes.put("VK_DEAD_TILDE", new Integer(KeyEvent.VK_DEAD_TILDE));
		m_keycodes.put("VK_DEAD_VOICED_SOUND", new Integer(KeyEvent.VK_DEAD_VOICED_SOUND));
		m_keycodes.put("VK_DECIMAL", new Integer(KeyEvent.VK_DECIMAL));
		m_keycodes.put("VK_DELETE", new Integer(KeyEvent.VK_DELETE));
		m_keycodes.put("VK_DIVIDE", new Integer(KeyEvent.VK_DIVIDE));
		m_keycodes.put("VK_DOLLAR", new Integer(KeyEvent.VK_DOLLAR));
		m_keycodes.put("VK_DOWN", new Integer(KeyEvent.VK_DOWN));
		m_keycodes.put("VK_E", new Integer(KeyEvent.VK_E));
		m_keycodes.put("VK_END", new Integer(KeyEvent.VK_END));
		m_keycodes.put("VK_ENTER", new Integer(KeyEvent.VK_ENTER));
		m_keycodes.put("VK_EQUALS", new Integer(KeyEvent.VK_EQUALS));
		m_keycodes.put("VK_ESCAPE", new Integer(KeyEvent.VK_ESCAPE));
		m_keycodes.put("VK_EURO_SIGN", new Integer(KeyEvent.VK_EURO_SIGN));
		m_keycodes.put("VK_EXCLAMATION_MARK", new Integer(KeyEvent.VK_EXCLAMATION_MARK));
		m_keycodes.put("VK_F", new Integer(KeyEvent.VK_F));
		m_keycodes.put("VK_F1", new Integer(KeyEvent.VK_F1));
		m_keycodes.put("VK_F10", new Integer(KeyEvent.VK_F10));
		m_keycodes.put("VK_F11", new Integer(KeyEvent.VK_F11));
		m_keycodes.put("VK_F12", new Integer(KeyEvent.VK_F12));
		m_keycodes.put("VK_F13", new Integer(KeyEvent.VK_F13));
		m_keycodes.put("VK_F14", new Integer(KeyEvent.VK_F14));
		m_keycodes.put("VK_F15", new Integer(KeyEvent.VK_F15));
		m_keycodes.put("VK_F16", new Integer(KeyEvent.VK_F16));
		m_keycodes.put("VK_F17", new Integer(KeyEvent.VK_F17));
		m_keycodes.put("VK_F18", new Integer(KeyEvent.VK_F18));
		m_keycodes.put("VK_F19", new Integer(KeyEvent.VK_F19));
		m_keycodes.put("VK_F2", new Integer(KeyEvent.VK_F2));
		m_keycodes.put("VK_F20", new Integer(KeyEvent.VK_F20));
		m_keycodes.put("VK_F21", new Integer(KeyEvent.VK_F21));
		m_keycodes.put("VK_F22", new Integer(KeyEvent.VK_F22));
		m_keycodes.put("VK_F23", new Integer(KeyEvent.VK_F23));
		m_keycodes.put("VK_F24", new Integer(KeyEvent.VK_F24));
		m_keycodes.put("VK_F3", new Integer(KeyEvent.VK_F3));
		m_keycodes.put("VK_F4", new Integer(KeyEvent.VK_F4));
		m_keycodes.put("VK_F5", new Integer(KeyEvent.VK_F5));
		m_keycodes.put("VK_F6", new Integer(KeyEvent.VK_F6));
		m_keycodes.put("VK_F7", new Integer(KeyEvent.VK_F7));
		m_keycodes.put("VK_F8", new Integer(KeyEvent.VK_F8));
		m_keycodes.put("VK_F9", new Integer(KeyEvent.VK_F9));
		m_keycodes.put("VK_FINAL", new Integer(KeyEvent.VK_FINAL));
		m_keycodes.put("VK_FIND", new Integer(KeyEvent.VK_FIND));
		m_keycodes.put("VK_FULL_WIDTH", new Integer(KeyEvent.VK_FULL_WIDTH));
		m_keycodes.put("VK_G", new Integer(KeyEvent.VK_G));
		m_keycodes.put("VK_GREATER", new Integer(KeyEvent.VK_GREATER));
		m_keycodes.put("VK_H", new Integer(KeyEvent.VK_H));
		m_keycodes.put("VK_HALF_WIDTH", new Integer(KeyEvent.VK_HALF_WIDTH));
		m_keycodes.put("VK_HELP", new Integer(KeyEvent.VK_HELP));
		m_keycodes.put("VK_HIRAGANA", new Integer(KeyEvent.VK_HIRAGANA));
		m_keycodes.put("VK_HOME", new Integer(KeyEvent.VK_HOME));
		m_keycodes.put("VK_I", new Integer(KeyEvent.VK_I));
		m_keycodes.put("VK_INPUT_METHOD_ON_OFF", new Integer(KeyEvent.VK_INPUT_METHOD_ON_OFF));
		m_keycodes.put("VK_INSERT", new Integer(KeyEvent.VK_INSERT));
		m_keycodes.put("VK_INVERTED_EXCLAMATION_MARK", new Integer(KeyEvent.VK_INVERTED_EXCLAMATION_MARK));
		m_keycodes.put("VK_J", new Integer(KeyEvent.VK_J));
		m_keycodes.put("VK_JAPANESE_HIRAGANA", new Integer(KeyEvent.VK_JAPANESE_HIRAGANA));
		m_keycodes.put("VK_JAPANESE_KATAKANA", new Integer(KeyEvent.VK_JAPANESE_KATAKANA));
		m_keycodes.put("VK_JAPANESE_ROMAN", new Integer(KeyEvent.VK_JAPANESE_ROMAN));
		m_keycodes.put("VK_K", new Integer(KeyEvent.VK_K));
		m_keycodes.put("VK_KANA", new Integer(KeyEvent.VK_KANA));
		m_keycodes.put("VK_KANA_LOCK", new Integer(KeyEvent.VK_KANA_LOCK));
		m_keycodes.put("VK_KANJI", new Integer(KeyEvent.VK_KANJI));
		m_keycodes.put("VK_KATAKANA", new Integer(KeyEvent.VK_KATAKANA));
		m_keycodes.put("VK_KP_DOWN", new Integer(KeyEvent.VK_KP_DOWN));
		m_keycodes.put("VK_KP_LEFT", new Integer(KeyEvent.VK_KP_LEFT));
		m_keycodes.put("VK_KP_RIGHT", new Integer(KeyEvent.VK_KP_RIGHT));
		m_keycodes.put("VK_KP_UP", new Integer(KeyEvent.VK_KP_UP));
		m_keycodes.put("VK_L", new Integer(KeyEvent.VK_L));
		m_keycodes.put("VK_LEFT", new Integer(KeyEvent.VK_LEFT));
		m_keycodes.put("VK_LEFT_PARENTHESIS", new Integer(KeyEvent.VK_LEFT_PARENTHESIS));
		m_keycodes.put("VK_LESS", new Integer(KeyEvent.VK_LESS));
		m_keycodes.put("VK_M", new Integer(KeyEvent.VK_M));
		m_keycodes.put("VK_META", new Integer(KeyEvent.VK_META));
		m_keycodes.put("VK_MINUS", new Integer(KeyEvent.VK_MINUS));
		m_keycodes.put("VK_MODECHANGE", new Integer(KeyEvent.VK_MODECHANGE));
		m_keycodes.put("VK_MULTIPLY", new Integer(KeyEvent.VK_MULTIPLY));
		m_keycodes.put("VK_N", new Integer(KeyEvent.VK_N));
		m_keycodes.put("VK_NONCONVERT", new Integer(KeyEvent.VK_NONCONVERT));
		m_keycodes.put("VK_NUM_LOCK", new Integer(KeyEvent.VK_NUM_LOCK));
		m_keycodes.put("VK_NUMBER_SIGN", new Integer(KeyEvent.VK_NUMBER_SIGN));
		m_keycodes.put("VK_NUMPAD0", new Integer(KeyEvent.VK_NUMPAD0));
		m_keycodes.put("VK_NUMPAD1", new Integer(KeyEvent.VK_NUMPAD1));
		m_keycodes.put("VK_NUMPAD2", new Integer(KeyEvent.VK_NUMPAD2));
		m_keycodes.put("VK_NUMPAD3", new Integer(KeyEvent.VK_NUMPAD3));
		m_keycodes.put("VK_NUMPAD4", new Integer(KeyEvent.VK_NUMPAD4));
		m_keycodes.put("VK_NUMPAD5", new Integer(KeyEvent.VK_NUMPAD5));
		m_keycodes.put("VK_NUMPAD6", new Integer(KeyEvent.VK_NUMPAD6));
		m_keycodes.put("VK_NUMPAD7", new Integer(KeyEvent.VK_NUMPAD7));
		m_keycodes.put("VK_NUMPAD8", new Integer(KeyEvent.VK_NUMPAD8));
		m_keycodes.put("VK_NUMPAD9", new Integer(KeyEvent.VK_NUMPAD9));
		m_keycodes.put("VK_O", new Integer(KeyEvent.VK_O));
		m_keycodes.put("VK_OPEN_BRACKET", new Integer(KeyEvent.VK_OPEN_BRACKET));
		m_keycodes.put("VK_P", new Integer(KeyEvent.VK_P));
		m_keycodes.put("VK_PAGE_DOWN", new Integer(KeyEvent.VK_PAGE_DOWN));
		m_keycodes.put("VK_PAGE_UP", new Integer(KeyEvent.VK_PAGE_UP));
		m_keycodes.put("VK_PASTE", new Integer(KeyEvent.VK_PASTE));
		m_keycodes.put("VK_PAUSE", new Integer(KeyEvent.VK_PAUSE));
		m_keycodes.put("VK_PERIOD", new Integer(KeyEvent.VK_PERIOD));
		m_keycodes.put("VK_PLUS", new Integer(KeyEvent.VK_PLUS));
		m_keycodes.put("VK_PREVIOUS_CANDIDATE", new Integer(KeyEvent.VK_PREVIOUS_CANDIDATE));
		m_keycodes.put("VK_PRINTSCREEN", new Integer(KeyEvent.VK_PRINTSCREEN));
		m_keycodes.put("VK_PROPS", new Integer(KeyEvent.VK_PROPS));
		m_keycodes.put("VK_Q", new Integer(KeyEvent.VK_Q));
		m_keycodes.put("VK_QUOTE", new Integer(KeyEvent.VK_QUOTE));
		m_keycodes.put("VK_QUOTEDBL", new Integer(KeyEvent.VK_QUOTEDBL));
		m_keycodes.put("VK_R", new Integer(KeyEvent.VK_R));
		m_keycodes.put("VK_RIGHT", new Integer(KeyEvent.VK_RIGHT));
		m_keycodes.put("VK_RIGHT_PARENTHESIS", new Integer(KeyEvent.VK_RIGHT_PARENTHESIS));
		m_keycodes.put("VK_ROMAN_CHARACTERS", new Integer(KeyEvent.VK_ROMAN_CHARACTERS));
		m_keycodes.put("VK_S", new Integer(KeyEvent.VK_S));
		m_keycodes.put("VK_SCROLL_LOCK", new Integer(KeyEvent.VK_SCROLL_LOCK));
		m_keycodes.put("VK_SEMICOLON", new Integer(KeyEvent.VK_SEMICOLON));
		m_keycodes.put("VK_SEPARATER", new Integer(KeyEvent.VK_SEPARATER));
		m_keycodes.put("VK_SHIFT", new Integer(KeyEvent.VK_SHIFT));
		m_keycodes.put("VK_SLASH", new Integer(KeyEvent.VK_SLASH));
		m_keycodes.put("VK_SPACE", new Integer(KeyEvent.VK_SPACE));
		m_keycodes.put("VK_STOP", new Integer(KeyEvent.VK_STOP));
		m_keycodes.put("VK_SUBTRACT", new Integer(KeyEvent.VK_SUBTRACT));
		m_keycodes.put("VK_T", new Integer(KeyEvent.VK_T));
		m_keycodes.put("VK_TAB", new Integer(KeyEvent.VK_TAB));
		m_keycodes.put("VK_U", new Integer(KeyEvent.VK_U));
		m_keycodes.put("VK_UNDEFINED", new Integer(KeyEvent.VK_UNDEFINED));
		m_keycodes.put("VK_UNDERSCORE", new Integer(KeyEvent.VK_UNDERSCORE));
		m_keycodes.put("VK_UNDO", new Integer(KeyEvent.VK_UNDO));
		m_keycodes.put("VK_UP", new Integer(KeyEvent.VK_UP));
		m_keycodes.put("VK_V", new Integer(KeyEvent.VK_V));
		m_keycodes.put("VK_W", new Integer(KeyEvent.VK_W));
		m_keycodes.put("VK_X", new Integer(KeyEvent.VK_X));
		m_keycodes.put("VK_Y", new Integer(KeyEvent.VK_Y));
		m_keycodes.put("VK_Z", new Integer(KeyEvent.VK_Z));

		// now map the other way
		Iterator iter = m_keycodes.keySet().iterator();
		while (iter.hasNext()) {
			String keytext = (String) iter.next();
			Integer keycode = (Integer) m_keycodes.get(keytext);
			m_keytext.put(keycode, keytext);
		}
	}

	/**
	 * Changes the key strokes for a given binding
	 * 
	 * @param binding
	 *            the binding object to change
	 * @param seq
	 *            the new set of keystrokes to assign to the binding.
	 */
	public static void changeBinding(MultiKeyBinding binding, KeyStroke[] seq) {
		if (seq == null || seq.length == 0) {
			binding.key = null;
			binding.keys = null;
		} else if (seq.length == 1) {
			binding.keys = null;
			binding.key = seq[0];
		} else {
			binding.key = null;
			binding.keys = seq;
		}
	}

	/**
	 * Creates a clone of a multkeybinding object
	 * 
	 * @param binding
	 *            the binding to make a clone of
	 * @return the newly created object
	 */
	public static MultiKeyBinding cloneBinding(MultiKeyBinding binding) {
		MultiKeyBinding clone = null;
		if (binding.keys == null)
			clone = new MultiKeyBinding(binding.key, binding.actionName);
		else
			clone = new MultiKeyBinding(binding.keys, binding.actionName);

		return clone;
	}

	/**
	 * Creates a key binding that has no keystrokes assigned
	 * 
	 * @param actionName
	 *            the name of the action/macro for the binding
	 */
	public static MultiKeyBinding createBinding(String actionName) {
		return new MultiKeyBinding((KeyStroke) null, actionName);
	}

	/**
	 * Creates a multikeybinding object from a given XML element
	 * 
	 * @param bElement
	 *            the xml element that describes the binding
	 * @return the created multikeybinding object
	 * 
	 *         <binding> <action>default-typed</action> <keystrokes> <keystroke>
	 *         <modifiers> <modifier>SHIFT</modifier> </modifiers>
	 *         <keytext>VK_LEFT</keytext> </keystroke> </keystrokes> </binding>
	 */
	public static MultiKeyBinding createBinding(Element bElement) {
		ArrayList kstrokes = new ArrayList();
		String actionname = XMLUtils.getChildText(bElement, "action");
		Element ekeystrokes = (Element) XMLUtils.getChildNode(bElement, "keystrokes");
		if (ekeystrokes != null) {
			NodeList nlist = ekeystrokes.getElementsByTagName("keystroke");
			for (int index = 0; index < nlist.getLength(); index++) {
				Element ekstroke = (Element) nlist.item(index);
				KeyStroke keystroke = createKeyStroke(ekstroke);
				if (keystroke != null)
					kstrokes.add(keystroke);
			}
		}

		MultiKeyBinding binding = null;

		if (kstrokes.size() == 0)
			return new MultiKeyBinding((KeyStroke) null, actionname);
		else if (kstrokes.size() == 1)
			return new MultiKeyBinding((KeyStroke) kstrokes.get(0), actionname);
		else {
			KeyStroke[] keys = new KeyStroke[0];
			return new MultiKeyBinding((KeyStroke[]) kstrokes.toArray(keys), actionname);
		}

	}

	/**
	 * Creates a keystroke object form a given XML element
	 * 
	 * @param KElement
	 *            the xml element that represents the keystroke
	 * @return the keystroke object <keystroke> <modifiers>
	 *         <modifier>SHIFT</modifier> </modifiers>
	 *         <keytext>VK_LEFT</keytext> </keystroke>
	 */
	public static KeyStroke createKeyStroke(Element kElement) {
		String keytext = XMLUtils.getChildText(kElement, "keytext");
		int keycode = KeyUtils.getKeyCode(keytext);

		int modifiers = 0;
		Element emods = (Element) XMLUtils.getChildNode(kElement, "modifiers");
		if (emods != null) {
			NodeList nlist = emods.getElementsByTagName("modifier");
			for (int index = 0; index < nlist.getLength(); index++) {
				Element emod = (Element) nlist.item(index);
				String mod_txt = XMLUtils.getNodeValue(emod);

				modifiers |= getModifier(mod_txt);
			}
		}

		if (keycode == 0 || keycode == KeyEvent.VK_UNDEFINED)
			return null;
		else
			return KeyStroke.getKeyStroke(keycode, modifiers);
	}

	/**
	 * Create xml element that represents keystroke modifiers: ctrl, alt, meta,
	 * alt_graph
	 */
	private static Element createXML(Document doc, int keymodifiers) {
		Element modifiers = doc.createElement("modifiers");

		if ((keymodifiers & KeyEvent.ALT_GRAPH_MASK) != 0) {
			Element mod = doc.createElement("modifier");
			mod.appendChild(doc.createTextNode("ALT_GRAPH"));
			modifiers.appendChild(mod);
			modifiers.appendChild(doc.createTextNode("\n"));
		}

		if ((keymodifiers & KeyEvent.ALT_MASK) != 0) {
			Element mod = doc.createElement("modifier");
			mod.appendChild(doc.createTextNode("ALT"));
			modifiers.appendChild(mod);
			modifiers.appendChild(doc.createTextNode("\n"));
		}

		if ((keymodifiers & KeyEvent.CTRL_MASK) != 0) {
			Element mod = doc.createElement("modifier");
			mod.appendChild(doc.createTextNode("CTRL"));
			modifiers.appendChild(mod);
			modifiers.appendChild(doc.createTextNode("\n"));
		}

		if ((keymodifiers & KeyEvent.META_MASK) != 0) {
			Element mod = doc.createElement("modifier");
			mod.appendChild(doc.createTextNode("META"));
			modifiers.appendChild(mod);
			modifiers.appendChild(doc.createTextNode("\n"));
		}

		if ((keymodifiers & KeyEvent.SHIFT_MASK) != 0) {
			Element mod = doc.createElement("modifier");
			mod.appendChild(doc.createTextNode("SHIFT"));
			modifiers.appendChild(mod);
			modifiers.appendChild(doc.createTextNode("\n"));
		}

		return modifiers;
	}

	/**
	 * Creates an XML element that represents a keystroke
	 */
	private static Element createXML(Document doc, KeyStroke keystroke) {
		Element e = doc.createElement("keystroke");
		if (keystroke != null) {
			Element mod = createXML(doc, keystroke.getModifiers());
			Element keytext = doc.createElement("keytext");

			keytext.appendChild(doc.createTextNode(KeyUtils.getKeyText(keystroke.getKeyCode())));
			e.appendChild(mod);
			e.appendChild(doc.createTextNode("\n"));
			e.appendChild(keytext);
			e.appendChild(doc.createTextNode("\n"));
		}
		return e;
	}

	/**
	 * Creates an XML element that represents a key binding
	 * 
	 * @param binding
	 *            the key binding to convert to xml
	 */
	public static Element createXML(Document doc, MultiKeyBinding binding) {
		Element el = doc.createElement("binding");
		Element action = doc.createElement("action");

		action.appendChild(doc.createTextNode(binding.actionName));
		el.appendChild(action);
		el.appendChild(doc.createTextNode("\n"));

		Element keystrokes = doc.createElement("keystrokes");
		if (binding.keys == null) {
			KeyStroke key = binding.key;
			keystrokes.appendChild(createXML(doc, key));
		} else {
			KeyStroke[] keys = binding.keys;
			for (int index = 0; index < keys.length; index++) {
				keystrokes.appendChild(createXML(doc, keys[index]));
			}
		}

		el.appendChild(keystrokes);
		el.appendChild(doc.createTextNode("\n"));
		return el;
	}

	/**
	 * @param keyText
	 *            the text string representing the key code (e.g. VK_Q )
	 * @return the key code for a given VK_ text string
	 */
	public static int getKeyCode(String keyText) {
		Integer kc = (Integer) m_keycodes.get(keyText);
		if (kc != null)
			return kc.intValue();
		else
			return 0;
	}

	/**
	 * @return the modifier value given its text name
	 * 
	 */
	public static int getModifier(String modName) {
		if (modName == null)
			return 0;
		else if (modName.equals("ALT_GRAPH"))
			return KeyEvent.ALT_GRAPH_MASK;
		else if (modName.equals("ALT"))
			return KeyEvent.ALT_MASK;
		else if (modName.equals("CTRL"))
			return KeyEvent.CTRL_MASK;
		else if (modName.equals("META"))
			return KeyEvent.META_MASK;
		else if (modName.equals("SHIFT"))
			return KeyEvent.SHIFT_MASK;
		else
			return 0;
	}

	/**
	 * @return the VK_ key text for a given key code
	 */
	public static String getKeyText(int keyCode) {
		return (String) m_keytext.get(new Integer(keyCode));
	}

	/**
	 * Helper method to print the contents of a binding to System.out
	 * 
	 * @param binding
	 *            the binding to print
	 */
	public static void print(MultiKeyBinding binding) {
		System.out.print(binding.actionName);
		System.out.print("  ");
		if (binding.keys != null)
			System.out.println(Utilities.keySequenceToString(binding.keys));
		else {
			if (binding.key == null)
				System.out.println(" null");
			else {
				KeyStroke[] sequence = new KeyStroke[1];
				sequence[0] = binding.key;
				System.out.println(Utilities.keySequenceToString(sequence));
			}
		}
	}

	/**
	 * Helper method to print the contents of a keymap to System.out
	 * 
	 * @param kmap
	 *            the keymap to print
	 */
	public static void print(Keymap kmap) {
		Action[] actions = kmap.getBoundActions();
		for (int index = 0; index < actions.length; index++) {
			Action action = actions[index];
			KeyStroke[] kstrokes = kmap.getKeyStrokesForAction(action);
			if (kstrokes == null || kstrokes.length == 0) {
				System.out.println("  action: " + action.getValue(Action.NAME) + "   keystroke = " + null);
			} else {
				String kstr = Utilities.keySequenceToString(kstrokes);
				System.out.println("  action: " + action.getValue(Action.NAME) + "   keystroke = " + kstr);
			}
		}
	}

	public static void print(JTextComponent.KeyBinding[] bindings) {
		for (int index = 0; index < bindings.length; index++) {
			JTextComponent.KeyBinding binding = bindings[index];
			System.out.println("  " + binding.actionName + "   " + binding.key);
		}
	}

	/**
	 * Prints the keybindings found in the netbeans settings cache
	 */
	public static void printSettings(Class kitClass) {
		// get all keybindings for the kit
		System.out.println("............ printing settings for " + kitClass.getName() + "  ......... ");
		ArrayList kv = (ArrayList) Settings.getValue(kitClass, SettingsNames.KEY_BINDING_LIST);
		if (kv == null)
			System.out.println("      null ");
		else {
			for (int i = kv.size() - 1; i >= 0; i--) {
				MultiKeyBinding b = (MultiKeyBinding) kv.get(i);
				KeyUtils.print(b);
			}
		}
		System.out.println("............ done .................. ");
	}
}
