package com.jeta.foundation.gui.editor.options;

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.foundation.componentmgr.ComponentMgr;

import com.jeta.foundation.gui.editor.KeyBindingMgr;
import com.jeta.foundation.gui.editor.KitKeyBindingModel;
import com.jeta.foundation.gui.editor.KitSet;
import com.jeta.foundation.gui.editor.macros.MacroMgrModel;

import com.jeta.foundation.interfaces.userprops.TSUserProperties;

/**
 * This class provides a level of abstract for editor options.
 * 
 * @author Jeff Tassin
 */
public class EditorOptionsModel {
	/**
	 * the kit set. We pass this in because the user might be editing bindings
	 * for a specific kit. For example, the Java editor will have different
	 * commands/key bindings than the sql editor.
	 */
	private KitSet m_kitset;

	/** the name of the active key bindings (e.g. default, emacs ) */
	private String m_activebindings;

	/**
	 * flag that indicates if line numbers are visible to the left of the editor
	 */
	private boolean m_showlinenumbers = false;

	/**
	 * a map of key binding names (Strings, keys) to KeyBindingModel objects (
	 * values )
	 */
	private HashMap m_keymodels = new HashMap();

	/**
	 * this is the list of editors that were selected to be deleted by the user
	 * We perform the actual deletion (as a batch) when the EditorOptionsView is
	 * closed
	 */
	private LinkedList m_markedfordelete = new LinkedList();

	/** the macro model */
	private MacroMgrModel m_macromodel;

	/** identifiers */
	public static final String ACTIVE_BINDING_NAME = "jeta.editor.activebinding";
	public static final String SHOW_LINE_NUMBERS = "jeta.editor.showlinenumbers";

	/**
	 * This ctor only loads the user preferences (Not the actual key bindings )
	 */
	public EditorOptionsModel() {
		loadProperties();
	}

	/**
	 * ctor
	 * 
	 * @param kitset
	 *            the kit set of the editor we are settings preferences for
	 */
	public EditorOptionsModel(KitSet kitset) {
		m_kitset = kitset;
		loadProperties();
		loadBindings();
	}

	/**
	 * Places the given editor bindings in the delete list. The bindings file is
	 * deleted when the EditorOptionsView is closed
	 */
	void deleteBindings(String editor) {
		m_markedfordelete.add(editor);
		m_keymodels.remove(editor);
	}

	/**
	 * @return the active keyboard bindings
	 */
	public String getActiveBindings() {
		if (m_activebindings == null)
			return KitKeyBindingModel.DEFAULT_BINDING;
		else
			return m_activebindings;
	}

	/**
	 * @return a collection of key binding names (String objects)
	 */
	public Collection getBindingNames() {
		return m_keymodels.keySet();
	}

	/**
	 * @return the set of all bindings models. This will instantiate all of the
	 *         KeyBindingModel objects if they are not already instantiated.
	 */
	public Collection getBindingModels() {
		LinkedList result = new LinkedList();
		Collection bnames = getBindingNames();
		Iterator iter = bnames.iterator();
		while (iter.hasNext()) {
			String bname = (String) iter.next();
			result.add(getKeyBindingModel(bname));
		}

		return result;
	}

	/**
	 * @return the key binding model for a given editor. If the editor is not
	 *         found, null is returned.
	 */
	public KeyBindingModel getKeyBindingModel(String editor) {
		if (m_keymodels.containsKey(editor)) {
			KeyBindingModel model = (KeyBindingModel) m_keymodels.get(editor);
			if (model == null) {
				// System.out.println(
				// "EditorOptionsModel.creating new key binding gui model for: "
				// + editor );
				model = new KeyBindingModel(editor, m_kitset);
				m_keymodels.put(editor, model);
			}
			return model;
		} else
			return null;
	}

	/**
	 * @return the kit set for the editor we are working with
	 */
	public KitSet getKitSet() {
		return m_kitset;
	}

	/**
	 * @return the macro model.
	 */
	public MacroMgrModel getMacroModel() {
		if (m_macromodel == null) {
			m_macromodel = new MacroMgrModel(this, getKeyBindingModel(getActiveBindings()));
		}
		return m_macromodel;
	}

	/**
	 * @return the flag indicating if the line numbers should be displayed in
	 *         the editor
	 */
	public boolean isShowLineNumbers() {
		return m_showlinenumbers;
	}

	/**
	 * Loads the model from the data store
	 */
	private void loadBindings() {
		Collection bindings = KeyBindingMgr.getEditorNames();
		Iterator iter = bindings.iterator();
		while (iter.hasNext()) {
			// let's assign null to the binding name for now so we don't have to
			// instantiate
			// every KeyBindingGui model here. If the caller requests a
			// KeyBindingGui model later
			// and it is in the m_keymodels map (but null), then we will
			// instantiate it then.
			m_keymodels.put((String) iter.next(), null);
		}
	}

	/**
	 * Loads the user properties
	 */
	private void loadProperties() {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		m_showlinenumbers = Boolean.valueOf(userprops.getProperty(SHOW_LINE_NUMBERS, "false")).booleanValue();
		m_activebindings = userprops.getProperty(ACTIVE_BINDING_NAME);
	}

	/**
	 * Saves the options to the user properties store. Also, any editors that
	 * were marked for deletion are removed from persitent store.
	 */
	public void save() {
		try {
			System.out.println("EditorOptionsModel.save............");
			TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
			userprops.setProperty(ACTIVE_BINDING_NAME, m_activebindings);
			userprops.setProperty(SHOW_LINE_NUMBERS, String.valueOf(m_showlinenumbers));

			// let's delete all bindings marked for deletion
			TreeSet delete_chk = new TreeSet(String.CASE_INSENSITIVE_ORDER);
			delete_chk.addAll(m_keymodels.keySet());
			Iterator iter = m_markedfordelete.iterator();
			while (iter.hasNext()) {
				String delete_binding = (String) iter.next();
				if (!delete_chk.contains(delete_binding)) {
					// ok to delete. Otherwise, the user deleted the binding and
					// recreated a new binding with the same name
					try {
						KeyBindingMgr.deleteBindings(delete_binding);
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}

			Collection bindings = m_keymodels.keySet();
			iter = bindings.iterator();
			while (iter.hasNext()) {
				String bindingname = (String) iter.next();

				KeyBindingModel model = (KeyBindingModel) m_keymodels.get(bindingname);
				// the model can be null here
				if (model != null)
					saveModel(model);
			}

			if (m_macromodel != null)
				m_macromodel.save();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			m_markedfordelete.clear();
		}
	}

	/**
	 * Saves the given gui model to the keybindingmgr as well as to persitent
	 * store.
	 */
	void saveModel(KeyBindingModel guimodel) throws IOException {
		// System.out.println( "EditorOptionsModel.saveModel: " +
		// guimodel.getBindingName() );
		Iterator iter = m_kitset.iterator();
		while (iter.hasNext()) {
			Class c = (Class) iter.next();
			// System.out.println( "saving binding settings: " + c.getName() );
			KitKeyBindingModel model = guimodel.getBindingModel(c);
			if (model != null) {
				// model.print();
				KeyBindingMgr.setBindingModel(guimodel.getBindingName(), c, model);
			} else {
				System.out.println(">>>>>>>>>>>>>>>>>> ERROR.  KeyBindingComboPanel.editBindings->save: model is null");
			}
		}
		// save the bindings to disk
		KeyBindingMgr.saveEditorBindings(guimodel.getBindingName());
	}

	/**
	 * Sets the active keyboard bindings
	 */
	public void setActiveBindings(String bindings) {
		m_activebindings = bindings;
	}

	/**
	 * Sets the key binding model for a given editor.
	 */
	public void setKitKeyBindingModel(String editor, KeyBindingModel model) {
		m_keymodels.put(editor, model);
	}

	/**
	 * Sets the flag indicating if the line numbers should be displayed in the
	 * editor
	 */
	public void setShowLineNumbers(boolean bshow) {
		m_showlinenumbers = bshow;
	}

}
