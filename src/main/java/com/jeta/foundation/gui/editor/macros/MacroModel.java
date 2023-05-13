package com.jeta.foundation.gui.editor.macros;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This is the model for all macros for a given editor kit
 * 
 * @author Jeff Tassin
 */
public class MacroModel implements Cloneable {
	/** an array of macros that belong to the given editor kit */
	ArrayList m_data = new ArrayList();

	/** the editor kit class */
	Class m_kitclass;

	/**
	 * ctor
	 */
	public MacroModel() {

	}

	/**
	 * ctor
	 */
	public MacroModel(Class kitClass) {
		m_kitclass = kitClass;
	}

	/**
	 * Adds a macor object to this model
	 * 
	 * @param macro
	 *            the macro to add to the model
	 */
	public void add(Macro macro) {
		m_data.add(macro);
	}

	/**
	 * Adds a default macro to this model.
	 */
	public void addDefault(Macro macro) {
		if (macro == null)
			return;

		m_data.add(0, macro);
	}

	/**
	 * Creates a copy of this binding
	 */
	public Object clone() {
		MacroModel model = new MacroModel();
		model.m_data = new ArrayList(); // ArrayList.clone only does a shallow
										// copy, so we need to manually do it
										// here
		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			Macro macro = (Macro) iter.next();
			model.add((Macro) macro.clone());
		}

		model.m_kitclass = m_kitclass;
		return model;
	}

	/**
	 * @return true if the model contains the named macro
	 */
	public boolean contains(String macroName) {
		for (int index = 0; index < size(); index++) {
			Macro macro = get(index);
			if (I18N.equals(macroName, macro.getName()))
				return true;
		}
		return false;
	}

	/**
	 * @return true if the model contains the named macro
	 */
	public Macro get(String macroName) {
		for (int index = 0; index < size(); index++) {
			Macro macro = get(index);
			if (I18N.equals(macroName, macro.getName()))
				return macro;
		}
		return null;
	}

	/**
	 * @param index
	 *            the index of the macro to get
	 * @return the macro object at the given table row
	 */
	public Macro get(int index) {
		return (Macro) m_data.get(index);
	}

	/**
	 * @return the underlying collection of macros
	 */
	public Collection getData() {
		return m_data;
	}

	/**
	 * @return the editor kit class for this model
	 */
	public Class getKitClass() {
		return m_kitclass;
	}

	/**
	 * @return a map representation of this model the map is the collection of
	 *         macros. The map key is the macro name, and the map value is the
	 *         macro command sequence (as a String object)
	 */
	public Map getMacroMap() {
		HashMap macromap = new HashMap();
		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			Macro macro = (Macro) iter.next();
			macromap.put(macro.getName(), macro.getCommand());
		}
		return macromap;
	}

	/**
	 * Loads this model from an xml document
	 */
	public void readXML(Element kitElement) throws ClassNotFoundException {
		String classname = kitElement.getNodeName();
		m_kitclass = Class.forName(classname);
		NodeList nlist = kitElement.getElementsByTagName("Macro");
		for (int index = 0; index < nlist.getLength(); index++) {
			Element macroelement = (Element) nlist.item(index);
			Macro m = new Macro();
			m.readXML(macroelement);
			add(m);
		}
	}

	/**
	 * Remove an object from the model
	 * 
	 * @param index
	 *            the index of the item to remove
	 * @return the removed object
	 */
	public Macro remove(int index) {
		Macro data = get(index);
		m_data.remove(index);
		return data;
	}

	/**
	 * Clears the model of data
	 */
	public void removeAll() {
		m_data.clear();
	}

	/**
	 * Removes the macro with the given name
	 */
	public void removeMacro(String macroName) {
		for (int index = 0; index < m_data.size(); index++) {
			Macro macro = get(index);
			if (I18N.equals(macroName, macro.getName()))
				remove(index);
		}
	}

	/**
	 * Sets the macro at the given index. If the index is greater than the size
	 * of the current set, then the macro is appended to the end. If the index
	 * is within the bounds of the set, any existing element is overwritten.
	 * 
	 * @param index
	 *            the index of the macro to set
	 * @param macro
	 *            the macro to set
	 */
	public void set(int index, Macro macro) {
		if (index < 0)
			index = 0;

		if (index >= m_data.size())
			m_data.add(macro);
		else
			m_data.set(index, macro);
	}

	/**
	 * @return the number of macros in this model
	 */
	public int size() {
		return m_data.size();
	}

	/**
	 * Iterates over all macros in the model. If a macro invokes the
	 * macro(oldName) then the caller macro is told to update its list. This
	 * method does not change the name of the macro whose name is being changed,
	 * only the macros that call the macro(oldName) being changed.
	 */
	public void updateMacroName(String newName, String oldName) {
		for (int index = 0; index < size(); index++) {
			Macro macro = get(index);
			macro.updateMacroName(newName, oldName);
		}
	}

	/**
	 * Saves this model to an xml Element
	 */
	public Element writeXML(Document doc) {

		Element myelement = doc.createElement(m_kitclass.getName());
		myelement.appendChild(doc.createTextNode("\n"));
		for (int index = 0; index < size(); index++) {
			Macro m = get(index);
			myelement.appendChild(m.writeXML(doc));
			myelement.appendChild(doc.createTextNode("\n"));
		}
		return myelement;
	}

}
