/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Action;
import javax.swing.text.JTextComponent;

import com.jeta.foundation.gui.editor.macros.MacroMgr;

/**
 * This class represents a set of editor kits
 * 
 * @author Jeff Tassin
 */
public class KitSet {
	private LinkedList m_kits = new LinkedList();

	public KitSet(Class kitClass) {
		this(kitClass, null);
	}

	/**
	 * ctor: Represents a set of kits along with a frame class that may define
	 * its own actions as well
	 */
	public KitSet(Class kitClass, Class kitFrameClass) {
		Class c = kitClass;
		while (c != null) {
			// we reverse the order of the kits in the list, from most specific
			// to most general
			// this allows specialized kits to override key bindings in base
			// kits.
			m_kits.addFirst(c);
			if (c == TSKit.class)
				break;

			c = c.getSuperclass();
		}

		if (kitFrameClass != null)
			m_kits.addFirst(kitFrameClass);
	}

	/**
	 * This returns all defined actions (including macros) for a set of given
	 * kits
	 * 
	 * @param kitSet
	 *            the set of editor kits whose actions we want to return
	 * @return the set of actions defined for an editor kit set
	 */
	public Action[] getActions() {
		try {
			Action[] result = null;
			try {
				result = MacroMgr.getMacroActions(this);
			} catch (Exception e) {
				e.printStackTrace();
				result = new Action[0];
			}

			Iterator iter = iterator();
			while (iter.hasNext()) {
				Class kitclass = (Class) iter.next();
				Action[] actions = (Action[]) TSEditorUtils.invokeClassMethod(kitclass, "listDefaultActions");
				if (actions != null) {
					Action[] temp = new Action[result.length + actions.length];
					System.arraycopy(result, 0, temp, 0, result.length);
					System.arraycopy(actions, 0, temp, result.length, actions.length);
					result = temp;
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Action[0];
	}

	/**
	 * @return the first class in the set
	 */
	public Class getFirst() {
		return (Class) m_kits.getFirst();
	}

	/**
	 * This returns all defined keybindings for a given set of editor kits
	 * 
	 * @param kitSet
	 *            the set of editor kits whose key bindingd we want to return
	 * @return the set of bindings defined for an editor kit set
	 */
	public JTextComponent.KeyBinding[] getKeyBindings() {
		JTextComponent.KeyBinding[] result = new JTextComponent.KeyBinding[0];
		Iterator iter = iterator();
		while (iter.hasNext()) {
			Class kitclass = (Class) iter.next();
			KitKeyBindingModel model = KeyBindingMgr.getBindingModel(KeyBindingMgr.getActiveEditor(), kitclass);
			if (model != null) {
				JTextComponent.KeyBinding[] modelbindings = model.getBindings();
				if (modelbindings != null) {

					JTextComponent.KeyBinding[] temp = new JTextComponent.KeyBinding[result.length
							+ modelbindings.length];
					System.arraycopy(result, 0, temp, 0, result.length);
					System.arraycopy(modelbindings, 0, temp, result.length, modelbindings.length);
					result = temp;
				}
			} else {
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$BindingModel is null for: " + kitclass);
			}
		}
		return result;
	}

	/**
	 * @return an iterator to the kit classes (Class objects) in this set
	 */
	public Iterator iterator() {
		return m_kits.iterator();
	}

	public int size() {
		return m_kits.size();
	}
}
