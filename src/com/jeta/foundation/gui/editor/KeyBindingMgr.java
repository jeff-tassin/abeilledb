/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.HashMap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.MultiKeymap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.TSComponent;
import com.jeta.foundation.componentmgr.ComponentNames;

import com.jeta.foundation.gui.editor.macros.Macro;
import com.jeta.foundation.gui.editor.macros.MacroMgr;
import com.jeta.foundation.gui.editor.macros.MacroModel;
import com.jeta.foundation.gui.editor.options.EditorOptionsModel;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.interfaces.resources.ResourceLoader;

import com.jeta.foundation.xml.XMLUtils;

/**
 * This class manages all key bindings in the system. It is used to create and
 * delete bindings as well.
 * 
 * @author Jeff Tassin
 */
public class KeyBindingMgr implements TSComponent {
	/**
	 * the collection of bindings (HashMap objects) that are associated with a
	 * given editor name (i.e. emacs, vi ) Each HashMap in the models hash
	 * contains the bindings for a particular editor kit
	 * 
	 * m_editorModels<String> emacs -> HashMap<KitKeyBindingModel> { BaseKit,
	 * ExtKit, SQLKit, HTMLKit, ... } vi -> HashMap<KitKeyBindingModel> {
	 * BaseKit, ExtKit, SQLKit, HTMLKit, ... }
	 */
	public static HashMap m_editorModels = new HashMap();

	/** the current key bindings for the application */
	public static String m_activeeditor;

	/** all key bindings for the application */
	public static KitKeyBindingModel m_globalmodel;

	/** identifiers */
	public static final String KEYBINDINGS_DIR = "keybindings";

	static {
		m_activeeditor = KitKeyBindingModel.DEFAULT_BINDING;
	}

	/**
	 * Deletes the given bindings from the cache as well as from disk
	 */
	public static void deleteBindings(String editorName) throws IOException {
		m_editorModels.remove(editorName);
		String resource = KeyBindingMgr.getResourceName(editorName);
		// System.out.println( "KeyBindingMgr.deleteBindings:  " + editorName );
		ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
		loader.deleteResource(resource);
	}

	/**
	 * @return the name of the active bindings for the application
	 */
	public static String getActiveEditor() {
		return m_activeeditor;
	}

	/**
	 * Gets the key binding model for the given kit for the current editor
	 * 
	 * @param kitClass
	 *            the editor kit class (e.g. BaseKit, ExtKit, SQLKit)
	 * @return the KitKeyBindingModel that has the given name. Null is returned
	 *         if the name is not found.
	 */
	public static KitKeyBindingModel getBindingModel(Class kitClass) {
		return getBindingModel(m_activeeditor, kitClass);
	}

	/**
	 * @param editorName
	 *            the name of the editor (e.g. default, emacs, vi)
	 * @param kitClass
	 *            the editor kit class (e.g. BaseKit, ExtKit, SQLKit)
	 * @return the KitKeyBindingModel that has the given name. Null is returned
	 *         if the name is not found.
	 */
	public static KitKeyBindingModel getBindingModel(String editorName, Class kitClass) {
		assert (editorName != null);
		KitKeyBindingModel model = null;
		HashMap kits = (HashMap) m_editorModels.get(editorName);
		if (kits == null) {
			// first try to load any saved models from disk
			// System.out.println( "loading custom models for: " + editorName );
			loadCustomModels(editorName);
			kits = (HashMap) m_editorModels.get(editorName);
			if (kits == null) {
				// System.out.println( "no custom models for " + editorName +
				// ". Loading default settings" );
				// if we tried to load from disk and still not found, then fall
				// back to
				// default bindings for this kit
				model = loadDefaultModel(kitClass);
			} else {
				model = (KitKeyBindingModel) kits.get(kitClass);
				if (model == null) // we loaded from disk, but this particular
									// kit was not found in the file
				{
					// System.out.println( "kit was not found in binding file: "
					// + editorName );
					model = loadDefaultModel(kitClass); // so go to default
														// settings
				}
			}
		} else {
			model = (KitKeyBindingModel) kits.get(kitClass);
			if (model == null) {
				// System.out.println(
				// "getBindingModel loading default model for " + editorName );
				model = loadDefaultModel(kitClass);
			}
		}

		return model;
	}

	/**
	 * @return a collection of binding names (String objects) Each name
	 *         represents a defined mapping. You create a KitKeyBindingModel
	 *         object based on the name (i.e. Default, Emacs, vi, etc.)
	 */
	public static Collection getEditorNames() {
		LinkedList results = new LinkedList();
		results.add(KitKeyBindingModel.DEFAULT_BINDING);
		try {
			ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
			String[] filenames = loader.listFiles(KEYBINDINGS_DIR, "\\.xml$");
			for (int index = 0; index < filenames.length; index++) {
				// System.out.println( "keybindingmgr found file: " +
				// filenames[index] );
				String fname = filenames[index];
				int pos = fname.lastIndexOf(".xml");
				fname = fname.substring(0, pos);

				if (!KitKeyBindingModel.DEFAULT_BINDING.equals(fname))
					results.add(fname);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * @return the keybindings that are global to the application (e.g. Ctrl-h
	 *         for help)
	 */
	public static KitKeyBindingModel getGlobalModel() {
		return m_globalmodel;
	}

	/**
	 * @return the file name where the given editor is stored. The KeyBindings
	 *         subdirectory is also included. The result is a relative path.
	 */
	public static String getResourceName(String editorName) {
		StringBuffer buff = new StringBuffer();
		buff.append(KEYBINDINGS_DIR);
		buff.append(File.separatorChar);
		buff.append(editorName);
		buff.append(".xml");
		return buff.toString();
	}

	/**
	 * Searches the kit class inheritance hierarchy for the given action name.
	 * If any model in the hierarchy contains the action, the method returns
	 * true.
	 * 
	 * @param kitSet
	 *            the kit set that we want to search
	 * @param actionName
	 *            the name of the action to search for.
	 * @return true if the given action name is defined in the system
	 */
	public static boolean isDefined(KitSet kitSet, String actionName) {
		boolean bresult = false;

		Iterator iter = kitSet.iterator();
		while (iter.hasNext()) {
			Class c = (Class) iter.next();
			KitKeyBindingModel model = getBindingModel(m_activeeditor, c);
			if (model.contains(actionName)) {
				bresult = true;
				break;
			}
		}
		return bresult;
	}

	/**
	 * Loads a the default bindings for a given model
	 */
	public static KitKeyBindingModel loadDefaultModel(Class kitClass) {
		KitKeyBindingModel model = new KitKeyBindingModel(KitKeyBindingModel.DEFAULT_BINDING, kitClass);

		// now get all actions supported by kit
		try {
			// get all actions
			HashMap actionmap = new HashMap();

			Action[] actions = (Action[]) TSEditorUtils.invokeClassMethod(kitClass, "listDefaultActions");
			if (actions != null) {
				for (int index = 0; index < actions.length; index++) {
					Action action = actions[index];
					actionmap.put(action.getValue(Action.NAME), action.getValue(Action.NAME));
				}
			}

			// we need to include all defined macros here, eventhough they
			// probably won't have bindings
			// if we don't add the macros, the user will not be able to see them
			// when editing bindings
			// derived from the default settings
			MacroModel macromodel = MacroMgr.getMacroModel(kitClass);
			for (int index = 0; index < macromodel.size(); index++) {
				Macro macro = macromodel.get(index);
				actionmap.put(macro.getName(), macro.getName());
			}

			// get all keybindings for the kit
			MultiKeyBinding[] bindings = (MultiKeyBinding[]) TSEditorUtils.invokeClassMethod(kitClass,
					"listDefaultKeyBindings");
			if (bindings != null) {
				for (int index = bindings.length - 1; index >= 0; index--) {
					MultiKeyBinding b = (MultiKeyBinding) bindings[index];
					// remove the corresponding action from the action map
					actionmap.remove(b.actionName);
					model.add(b);
				}
			}

			// now load any actions that don't have key bindings. This allows
			// user to
			// reassign bindings for those
			Iterator iter = actionmap.keySet().iterator();
			while (iter.hasNext()) {
				String actionname = (String) iter.next();
				MultiKeyBinding binding = new MultiKeyBinding((KeyStroke) null, actionname);
				model.add(binding);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// model.print();

		setBindingModel(KitKeyBindingModel.DEFAULT_BINDING, kitClass, model);
		return model;
	}

	/**
	 * Loads a custom model from a file (for all kits in the file)
	 * 
	 * @param editorName
	 *            the name of the editor (file) to load from the key bindings
	 *            directory
	 */
	private static void loadCustomModels(String editorName) {
		try {
			// @todo we need to handle the case where new macros have been added
			// as well as
			// gracefully recover if the user manually deleted actions from the
			// file,
			// or a particular action entry was invalid
			Document doc = loadKeyBindingsFile(editorName);
			Element root = doc.getDocumentElement();

			NodeList nlist = root.getChildNodes();
			// System.out.println( "loadCustomModels   for " + editorName +
			// "   root length = " + nlist.getLength() );
			for (int nodeindex = 0; nodeindex < nlist.getLength(); nodeindex++) {
				Node item = nlist.item(nodeindex);
				if (item instanceof Element) {
					Element element = (Element) item;
					// String elname = element.getNodeName();
					KitKeyBindingModel model = new KitKeyBindingModel(editorName, null);
					model.readXML(element);

					// add all actions that are defined but for some reason are
					// not in the file
					// perhaps they were manually deleted
					Class kitclass = model.getKitClass();
					// System.out.println(
					// "KeyBindingMgr.loadCustomModels   class = " + kitclass );
					Action[] actions = (Action[]) TSEditorUtils.invokeClassMethod(kitclass, "listDefaultActions");
					for (int index = 0; index < actions.length; index++) {
						Action action = actions[index];
						String actionname = (String) action.getValue(Action.NAME);
						if (!model.contains(actionname))
							model.addAction(actionname);
					}

					// add all macro actions that are defined but are not in the
					// file
					MacroModel macromodel = MacroMgr.getMacroModel(kitclass);
					for (int index = 0; index < macromodel.size(); index++) {
						Macro macro = macromodel.get(index);
						if (!model.contains(macro.getName()))
							model.addAction(macro.getName());
					}

					// System.out.println( "setting binding model for " +
					// editorName + "  kit = " + model.getKitClass() );
					setBindingModel(editorName, model.getKitClass(), model);
				}
			}

			// the xml file stores bindings for all kits, so let's read each kit
			// and load it
			/*
			 * List kitlist = root.getChildren(); Iterator iter =
			 * kitlist.iterator(); while( iter.hasNext() ) { Element kitelement
			 * = (Element)iter.next(); KitKeyBindingModel model = new
			 * KitKeyBindingModel( editorName, null ); model.readXML( kitelement
			 * );
			 * 
			 * // add all actions that are defined but for some reason are not
			 * in the file // perhaps they were manually deleted Class kitclass
			 * = model.getKitClass(); Action[] actions =
			 * (Action[])TSEditorUtils.invokeClassMethod( kitclass,
			 * "listDefaultActions" ); for( int index=0; index < actions.length;
			 * index++ ) { Action action = actions[index]; String actionname =
			 * (String)action.getValue( Action.NAME ); if ( !model.contains(
			 * actionname ) ) model.addAction( actionname ); }
			 * 
			 * // add all macro actions that are defined but are not in the file
			 * MacroModel macromodel = MacroMgr.getMacroModel( kitclass ); for(
			 * int index=0; index < macromodel.size(); index++ ) { Macro macro =
			 * macromodel.get( index ); if ( !model.contains( macro.getName() )
			 * ) model.addAction( macro.getName() ); }
			 * 
			 * setBindingModel( editorName, model.getKitClass(), model ); }
			 */

		} catch (Exception e) {
			// ignore here, there is nothing we can do if a problem occurs
			// except use default settings
		}
	}

	/**
	 * Loads the connection definitions from the key bindings xml file (located
	 * in the KEYBINDINGS_DIR )
	 * 
	 * @param editorName
	 *            the name of the editor to load (without path information)
	 * @return an XML Document object based on the file information. Null is
	 *         returned if an error occurs
	 */
	private static Document loadKeyBindingsFile(String editorName) throws ParserConfigurationException, IOException,
			SAXException {
		Document result = null;
		ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
		if (loader != null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			result = builder.parse(loader.getInputStream(KeyBindingMgr.getResourceName(editorName)));
			result.normalize();
		}
		return result;
	}

	/**
	 * Saves all kits and their bindings for the current editor
	 */
	public static void saveEditorBindings() throws IOException {
		saveEditorBindings(m_activeeditor);
	}

	/**
	 * Saves all kits and their bindings for a given editor
	 */
	public static void saveEditorBindings(String editorName) throws IOException {
		LinkedList results = new LinkedList();
		ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
		if (loader != null) {
			String filename = KeyBindingMgr.getResourceName(editorName);
			loader.createResource(filename);
			OutputStream os = loader.getOutputStream(filename);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.newDocument();
				Element root = document.createElement(editorName);
				document.appendChild(root);

				HashMap kits = (HashMap) m_editorModels.get(editorName);
				if (kits != null) {
					Iterator iter = kits.keySet().iterator();
					while (iter.hasNext()) {
						Class kitclass = (Class) iter.next();
						KitKeyBindingModel model = (KitKeyBindingModel) kits.get(kitclass);
						if (model != null) {
							Element modelxml = model.writeXML(document);
							root.appendChild(modelxml);
							root.appendChild(document.createTextNode("\n"));
						}
					}
				}

				/**
				 * this may be a hack. still not sure why you need to explicitly
				 * add new lines using JAXP
				 */
				// XMLUtils.addNewlines( document, root );

				TransformerFactory tfactory = TransformerFactory.newInstance();
				Transformer serializer = tfactory.newTransformer();
				DOMSource ds = new DOMSource(document);

				serializer.transform(ds, new StreamResult(os));
				os.flush();
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sets the active binding for the application
	 * 
	 * @param model
	 *            the bindings to set
	 */
	public static void setActiveEditor(String editorName) {
		m_activeeditor = editorName;
	}

	/**
	 * Sets the key binding model for the given editor and class
	 */
	public static void setBindingModel(String editorName, Class kitClass, KitKeyBindingModel model) {
		HashMap kits = (HashMap) m_editorModels.get(editorName);
		if (kits == null) {
			kits = new HashMap();
			m_editorModels.put(editorName, kits);
		}
		kits.put(kitClass, model);
	}

	/**
	 * Sets the keybindings that are global to the application (e.g. Ctrl-h for
	 * help)
	 * 
	 * @param model
	 *            the model that represents the application bindings
	 */
	public static void setGlobalModel(KitKeyBindingModel model) {
		m_globalmodel = model;
	}

	/**
	 * Sets the key bindings on the given key editor. Key bindings are an
	 * application wide setting, so we simply get the current bindings from the
	 * KeyBindingsMgr
	 * 
	 * @param editor
	 *            the editor to set. Note, this editor must have an EditorKit
	 *            derived from BaseKit
	 */
	public static void setKeyBindings(JEditorPane editor, KitSet kitSet) {
		Class kitclass = editor.getEditorKit().getClass();
		// System.out.println( "KeyBindingMgr.setKeyBindings....   kitclass = "
		// + kitclass );
		MultiKeymap keymap = new MultiKeymap("");
		Action[] actions = kitSet.getActions();
		JTextComponent.KeyBinding[] keys = kitSet.getKeyBindings();
		keymap.load(keys, actions);
		editor.setKeymap(keymap);
	}

	/**
	 * TSComponent implementation. Gets called at startup We simply get the
	 * active binding.
	 */
	public void startup() {
		EditorOptionsModel editormodel = new EditorOptionsModel();
		String activebindingname = editormodel.getActiveBindings();

		String activeeditor = null;
		Collection editors = getEditorNames();
		Iterator iter = editors.iterator();
		while (iter.hasNext()) {
			// make sure the selected editor bindings exist, if not, use default
			String editorname = (String) iter.next();
			if (I18N.equals(editorname, activebindingname)) {
				activeeditor = editorname;
				break;
			}
		}

		if (activeeditor == null)
			activeeditor = KitKeyBindingModel.DEFAULT_BINDING;

		m_activeeditor = activeeditor;
	}

	/**
	 * TSComponent implementation
	 */
	public void shutdown() {
	}

}
