package com.jeta.foundation.gui.editor.macros;

import java.io.File;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import java.awt.Toolkit;

import javax.swing.Action;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.editor.KitSet;
import com.jeta.foundation.gui.editor.TSKit;
import com.jeta.foundation.gui.editor.TSEditorUtils;

import com.jeta.foundation.interfaces.resources.ResourceLoader;
import com.jeta.foundation.xml.XMLUtils;

/**
 * This class manages all macros in the system.
 * 
 * @author Jeff Tassin
 */
public class MacroMgr {
	public static final String MACROS_FILE = "macros" + File.separatorChar + "macros.xml";

	/** a map of macro models for the various kit classes */
	private static HashMap m_models;

	/** the set of default macro names (String objects). Users cannot edit these */
	// private static TreeMap m_defaultmacros = new TreeMap();

	static {
		m_models = new HashMap();

		// get file registered in macro directory. All macros are stored in a
		// single file
		loadMacros();
	}

	/**
	 * Clears all existing models
	 */
	static void clear() {
		m_models.clear();
	}

	/**
    */
	private static void loadMacros() {
		try {

			Document doc = null;
			ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
			if (loader != null) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				doc = builder.parse(loader.getInputStream(MACROS_FILE));
				doc.normalize();

				Element root = doc.getDocumentElement();
				NodeList nlist = root.getChildNodes();
				for (int nodeindex = 0; nodeindex < nlist.getLength(); nodeindex++) {
					Node item = nlist.item(nodeindex);
					if (item instanceof Element) {
						Element kitelement = (Element) item;
						MacroModel model = new MacroModel();
						model.readXML(kitelement);
						loadDefaults(model);

						m_models.put(model.getKitClass(), model);
					}
				}
			}
		} catch (java.io.FileNotFoundException fne) {

		} catch (Exception e) {
			// ignore here, there is nothing we can do if a problem occurs
			// except use default settings
			e.printStackTrace();
		}
	}

	/**
	 * This returns all defined macros (as
	 * org.netbeans.editor.ActionFactory.RunMacroAction objects ) for a given
	 * kit class and optionally, any superclasses.
	 * 
	 * @param kitSet
	 *            the set of editor kits whose actions we want to return
	 * @return the set of actions defined for a particular editor kit
	 */
	public static Action[] getMacroActions(KitSet kitSet) {
		try {
			Action[] result = new Action[0];
			Iterator iter = kitSet.iterator();
			while (iter.hasNext()) {
				Class kitclass = (Class) iter.next();
				MacroModel model = getMacroModel(kitclass);
				if (model != null) {
					Action[] modelactions = new Action[model.size()];
					for (int index = 0; index < model.size(); index++) {
						Macro macro = model.get(index);
						modelactions[index] = new RunMacroAction(macro);
					}

					Action[] temp = new Action[result.length + modelactions.length];
					System.arraycopy(result, 0, temp, 0, result.length);
					System.arraycopy(modelactions, 0, temp, result.length, modelactions.length);
					result = temp;
				}
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Action[0];
	}

	/*
	 * @param kitClass the editor kit class (e.g. BaseKit, ExtKit, SQLKit)
	 * 
	 * @return the MacroModel for the given kit class
	 */
	public static MacroModel getMacroModel(Class kitClass) {
		MacroModel model = (MacroModel) m_models.get(kitClass);
		if (model == null) {
			model = new MacroModel(kitClass);
			loadDefaults(model);
			m_models.put(model.getKitClass(), model);
		}
		return model;
	}

	/**
	 * @param kitClass
	 *            the editor kit class whose macro names we want to return
	 * @param bRecurse
	 *            set to true if you want to traverse the kitClass inheritance
	 *            hierarchy.
	 * @return the set of macro names defined for a particular editor kit
	 */
	public static String[] getMacroNames(Class kitClass, boolean bRecurse) {
		try {
			String[] result = new String[0];
			ArrayList names = new ArrayList();
			MacroModel model = getMacroModel(kitClass);
			for (int index = 0; index < model.size(); index++) {
				Macro macro = model.get(index);
				names.add(macro.getName());
			}

			result = (String[]) names.toArray(result);

			if (bRecurse && kitClass != TSKit.class) {
				String[] parentactions = getMacroNames(kitClass.getSuperclass(), true);
				String[] temp = new String[result.length + parentactions.length];
				System.arraycopy(result, 0, temp, 0, result.length);
				System.arraycopy(parentactions, 0, temp, result.length, parentactions.length);
				result = temp;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new String[0];

	}

	/**
	 * @return true if the given macro name is a default macro
	 */
	// private static boolean isDefault( String macroName )
	// {
	// return m_defaultmacros.containsKey( macroName );
	// }

	/**
	 * Searches the kit class inheritance hierarchy for the given macro name. If
	 * any model in the hierarchy contains the macro, the method returns true.
	 * 
	 * @param kitSet
	 *            the set of editor kits to search
	 * @param macroName
	 *            the name of the macro to search for.
	 * @return true if the given macro name is defined in the system
	 */
	public static boolean isDefined(KitSet kitSet, String macroName) {
		boolean bresult = false;

		Iterator iter = kitSet.iterator();
		while (iter.hasNext()) {
			Class c = (Class) iter.next();
			MacroModel model = getMacroModel(c);
			if (model.contains(macroName)) {
				bresult = true;
				break;
			}
		}
		return bresult;
	}

	/**
	 * Loads a the default macros for a given model. The kitClass must have the
	 * method listDefaultMacros defined and it must return an array of Macro
	 * objects.
	 */
	private static void loadDefaults(MacroModel model) {
		try {
			Macro[] defaultmacros = (Macro[]) TSEditorUtils.invokeClassMethod(model.getKitClass(), "listDefaultMacros");
			if (defaultmacros != null) {
				for (int i = 0; i < defaultmacros.length; i++) {
					Macro macro = (Macro) defaultmacros[i];
					if (!model.contains(macro.getName())) {
						model.addDefault(macro);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves all macros to the macros.xml file
	 */
	public static void save() {
		ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
		if (loader != null) {
			try {
				loader.createResource(MACROS_FILE);
				OutputStream os = loader.getOutputStream(MACROS_FILE);
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.newDocument();
				Element root = document.createElement("macros");
				document.appendChild(root);

				Iterator iter = m_models.keySet().iterator();
				while (iter.hasNext()) {
					Class kitclass = (Class) iter.next();
					MacroModel model = (MacroModel) m_models.get(kitclass);
					Element modelxml = model.writeXML(document);
					root.appendChild(modelxml);
					root.appendChild(document.createTextNode("\n"));
				}

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
	 * Sets the model for the given kit class
	 */
	static void setModel(Class kitClass, MacroModel model) {
		m_models.put(kitClass, model);
	}
}
