/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.LinkedList;

import javax.swing.JEditorPane;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.editor.MultiKeyBinding;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.editor.TSEditorMgr;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the model for managing key bindings. A model is defined for various
 * types of editor key bindings. For example, we might have a model based on
 * emacs or vi. Note, this model applies to all editor kits in a hierarchy. For
 * example, we might have a SQLKit (for SQL) and an XMLKit (for xml). Each would
 * share bindings for the basic actions in this model. For example, to cut text,
 * we might have the binding CTRL+w in emacs. This binding would apply to all
 * editor kits. For those kits that define additional actions, the keybindings
 * for those actions would clearly only apply to those kits. This model manages
 * the bindings for all kits in a hierarchy (for a given editor model like emacs
 * or vi )
 * 
 * BaseKit - | | ExtKit | | | ------------------------------------- > Emacs, vi,
 * etc bindings | | | | | | SQLKit JavaKit XMLKit HTMLKit OtherKit | - This
 * model would handle a single node in the above hierarchy For example, if the
 * user were editing the bindings for the SQLKit, this model would represent
 * either the SQLKit, ExtKit, or BaseKit bindings. The set of
 * KitKeyBindingModels (from root to leaf) represents the key bindings for a
 * given editor.
 * 
 * @author Jeff Tassin
 */
public class KitKeyBindingModel implements Cloneable {
	String m_name; // this is the name of this binding
	ArrayList m_data = new ArrayList();
	Class m_kitclass;
	public static final String DEFAULT_BINDING = "Default";

	/**
	 * ctor
	 */
	public KitKeyBindingModel() {

	}

	/**
	 * ctor
	 * 
	 * @param editorName
	 *            the name for this binding
	 */
	public KitKeyBindingModel(String editorName, Class kitclass) {
		m_kitclass = kitclass;
		m_name = editorName;
	}

	/**
	 * Adds a binding object to the table for a given editor kit.
	 * 
	 * @param mk
	 *            the keybinding to add to the model
	 */
	public void add(MultiKeyBinding mk) {
		m_data.add(mk);
	}

	/**
	 * Adds an action with no keybinding assignment to the model
	 * 
	 * @param actionName
	 *            the name of the action to add
	 */
	public void addAction(String actionName) {
		add(KeyUtils.createBinding(actionName));
	}

	/**
	 * Creates a copy of this binding
	 */
	public Object clone() {
		KitKeyBindingModel model = new KitKeyBindingModel();
		model.m_kitclass = m_kitclass;
		model.m_data = new ArrayList();

		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			MultiKeyBinding binding = (MultiKeyBinding) iter.next();
			MultiKeyBinding clone = KeyUtils.cloneBinding(binding);
			model.add(clone);
		}
		return model;
	}

	/**
	 * @param actionName
	 *            the name of the action to search for
	 * @return true if this model contains the named action
	 */
	public boolean contains(String actionName) {
		for (int index = 0; index < size(); index++) {
			MultiKeyBinding binding = get(index);
			if (I18N.equals(binding.actionName, actionName))
				return true;
		}
		return false;
	}

	/**
	 * @return the underlying collection of bindings
	 */
	public Collection getData() {
		ArrayList results = new ArrayList();
		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			MultiKeyBinding binding = (MultiKeyBinding) iter.next();
			if (binding.key != null || binding.keys != null)
				results.add(binding);
			else if (binding.actionName.equals("default-typed")) // you must
																	// have
																	// default-typed
																	// or there
																	// will be
																	// problems
				results.add(binding);
		}
		return results;
	}

	/**
	 * @param index
	 *            the index of the binding to get
	 * @return the key binding information at the given table row
	 */
	public MultiKeyBinding get(int index) {
		return (MultiKeyBinding) m_data.get(index);
	}

	/**
	 * @return all bindings in the model
	 */
	public MultiKeyBinding[] getBindings() {
		ArrayList results = new ArrayList();
		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			MultiKeyBinding binding = (MultiKeyBinding) iter.next();
			if (binding.key != null || binding.keys != null)
				results.add(binding);
			else if (binding.actionName.equals("default-typed")) // you must
																	// have
																	// default-typed
																	// or there
																	// will be
																	// problems
				results.add(binding);
		}
		return (MultiKeyBinding[]) results.toArray(new MultiKeyBinding[0]);
	}

	/**
	 * Returns the set of key bindings that are mapped to the given action name.
	 * If no bindings are found, an empty array is returned.
	 * 
	 * @param actionName
	 *            the action name
	 */
	public MultiKeyBinding[] getBindings(String actionName) {
		ArrayList results = new ArrayList();
		for (int index = 0; index < size(); index++) {
			MultiKeyBinding binding = get(index);
			if (actionName == null && binding.actionName == null)
				results.add(binding);
			else if (actionName != null && actionName.equals(binding.actionName))
				results.add(binding);
		}

		MultiKeyBinding[] bindings = new MultiKeyBinding[0];
		return (MultiKeyBinding[]) results.toArray(bindings);
	}

	/**
	 * @return the kit class for this model
	 */
	public Class getKitClass() {
		return m_kitclass;
	}

	/**
	 * There is a default binding model in the system. This method returns true,
	 * if this model instance represents the default model
	 */
	public boolean isDefault() {
		return (m_name.equals(DEFAULT_BINDING));
	}

	/**
	 * Prints the contents of this model to System.out
	 */
	public void print() {

		System.out.println(">>>>>>>>>>>>> Printing KitKeyBindingModel >>>>>>>>>>>>>> ");
		System.out.println("  editor = " + m_name + "   class = " + m_kitclass.getName());

		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			MultiKeyBinding binding = (MultiKeyBinding) iter.next();
			KeyUtils.print(binding);
		}
		System.out.println(">>>>>>>>>>>>>  Done  >>>>>>>>>>>>>> ");
	}

	/**
	 * Loads this model from an xml element
	 * 
	 */
	public void readXML(Element kitElement) throws ClassNotFoundException {
		String classname = kitElement.getNodeName();

		m_kitclass = Class.forName(classname);

		// System.out.println("KitKeyBindingModel.readXML   class= " +
		// m_kitclass.getName() );
		NodeList nlist = kitElement.getElementsByTagName("binding");
		for (int index = 0; index < nlist.getLength(); index++) {
			Element binding = (Element) nlist.item(index);
			MultiKeyBinding multikey = KeyUtils.createBinding(binding);
			add(multikey);
		}
	}

	/**
	 * Remove an object from the model
	 * 
	 * @param index
	 *            the index of the item to remove
	 * @return the removed object
	 */
	public MultiKeyBinding remove(int index) {
		MultiKeyBinding binding = get(index);
		m_data.remove(index);
		return binding;
	}

	/**
	 * Remove an object from the model
	 * 
	 * @param binding
	 *            the binding to remove
	 * @return the removed object. (null if the object is not found)
	 */
	public MultiKeyBinding remove(MultiKeyBinding binding) {
		for (int index = 0; index < m_data.size(); index++) {
			MultiKeyBinding mk = get(index);
			if (binding.equals(mk)) {
				remove(index);
				return binding;
			}
		}
		return null;
	}

	/**
	 * Remove all bindings from the model for the given action name
	 * 
	 * @param actionName
	 *            the binding to remove
	 * @return the removed bindings.
	 */
	public MultiKeyBinding[] removeBindings(String actionName) {
		ArrayList results = new ArrayList();
		for (int index = 0; index < size(); index++) {
			MultiKeyBinding binding = get(index);
			if (I18N.equals(actionName, binding.actionName))
				results.add(remove(index));
		}

		MultiKeyBinding[] bindings = new MultiKeyBinding[0];
		return (MultiKeyBinding[]) results.toArray(bindings);
	}

	/**
	 * Renames the action(oldName) to the newName
	 * 
	 * @param newName
	 *            the new name for the action
	 * @param oldName
	 *            the old name for the action
	 */
	public void renameAction(String newName, String oldName) {
		for (int index = 0; index < size(); index++) {
			MultiKeyBinding binding = get(index);
			if (I18N.equals(binding.actionName, oldName))
				binding.actionName = newName;
		}
	}

	/**
	 * Sets the binding at the given index. If the index is greater than the
	 * size of the current set, then the binding is appended to the end. If the
	 * index is within the bounds of the set, any existing element is
	 * overwritten.
	 * 
	 * @param index
	 *            the index of the binding to get
	 * @param binding
	 *            the binding to set
	 */
	public void set(int index, MultiKeyBinding binding) {
		m_data.set(index, binding);
	}

	/**
	 * @return the number of actions/bindings in this model
	 */
	public int size() {
		return m_data.size();
	}

	/**
	 * Saves this model to an xml Element
	 * 
	 * @return the xml element that represents this model
	 */
	public Element writeXML(Document doc) {
		Element myelement = doc.createElement(m_kitclass.getName());
		for (int index = 0; index < size(); index++) {
			MultiKeyBinding binding = get(index);
			Element belement = KeyUtils.createXML(doc, binding);
			myelement.appendChild(belement);
			myelement.appendChild(doc.createTextNode("\n"));
		}
		return myelement;
	}
}
