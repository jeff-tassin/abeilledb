package com.jeta.foundation.gui.editor.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;

// netbeans imports
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.Utilities;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.foundation.gui.editor.FrameKit;
import com.jeta.foundation.gui.editor.KeyBindingMgr;
import com.jeta.foundation.gui.editor.KitKeyBindingModel;
import com.jeta.foundation.gui.editor.KitInfo;
import com.jeta.foundation.gui.editor.KitSet;
import com.jeta.foundation.gui.editor.TSEditorMgr;
import com.jeta.foundation.gui.editor.TSKit;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This is the model for representing MultiKeyBinding objects in a JTable. These
 * objects bind keystrokes to application action objects.
 * 
 * @author Jeff Tassin
 */
public class KeyBindingModel extends AbstractTableModel implements Cloneable {
	/** an array of column names of the GUI table */
	private String[] m_colNames;

	/** this is the types of columns of the GUI table */
	private Class[] m_colTypes;

	/** editor kits that we are working with */
	private KitSet m_kitset;

	/** flattened out list of bindings for the editor kit hierarchy */
	private ArrayList m_data = new ArrayList();

	/** the name of the bindings we are interested in */
	private String m_editorName;

	/** the icon for the base kit */
	private static ImageIcon m_basekitIcon;

	/** flag that indicates if this is a newly created model */
	private boolean m_new = false;

	static {
		m_basekitIcon = TSGuiToolbox.loadImage("incors/16x16/document.png");
	}

	// generic initialization
	{
		String[] values = { I18N.getLocalizedMessage("Action"), I18N.getLocalizedMessage("Key Stroke") };
		m_colNames = values;
		Class[] types = { KeyBindingWrapper.class, String.class };
		m_colTypes = types;
	}

	/**
	 * For clone
	 */
	private KeyBindingModel() {
		// no op
	}

	/**
	 * Constructor Loads the default settings
	 */
	public KeyBindingModel(KitSet kitSet) {
		try {
			m_kitset = kitSet;

			// now let's load the data
			Iterator iter = m_kitset.iterator();
			while (iter.hasNext()) {
				Class c = (Class) iter.next();
				KitKeyBindingModel model = KeyBindingMgr.loadDefaultModel(c);
				Icon icon = getIcon(c);
				loadModel(model, icon);
			}
			Collections.sort(m_data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor
	 */
	public KeyBindingModel(String editorName, KitSet kitSet) {
		try {
			m_kitset = kitSet;
			m_editorName = editorName;

			// now let's load the data
			Iterator iter = m_kitset.iterator();
			while (iter.hasNext()) {
				Class c = (Class) iter.next();
				KitKeyBindingModel model = KeyBindingMgr.getBindingModel(m_editorName, c);

				Icon icon = getIcon(c);
				loadModel(model, icon);
			}

			Collections.sort(m_data);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Adds the given binding to the model
	 */
	public void addBinding(KeyBindingWrapper wrapper) {
		m_data.add(wrapper);
		fireTableChanged(new TableModelEvent(this, getRowCount() - 1, getRowCount() - 1, 0, TableModelEvent.INSERT));
	}

	/**
	 * This adds a model to the current set of models. This is mainly used to
	 * allow the user to edit actions that are not necessarily part of a kit
	 * hierarchy. We encounter this situation when we have actions that are part
	 * of a frame window that don't necessarily have meaning to a text component
	 * (e.g. the command to switch buffers or to print)
	 */
	private void addModel(KitKeyBindingModel model, Icon icon) {
		loadModel(model, icon);
	}

	/**
	 * Adds a binding object to the table
	 */
	public void addRow(Class kitclass, MultiKeyBinding mk) {
		Icon icon = getIcon(kitclass);
		addBinding(new KeyBindingWrapper(kitclass, mk, icon));
	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		KeyBindingModel model = new KeyBindingModel();
		model.m_kitset = m_kitset;

		model.m_data = new ArrayList();
		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			KeyBindingWrapper wrapper = (KeyBindingWrapper) iter.next();
			model.m_data.add(wrapper.clone());
		}

		model.m_editorName = m_editorName;
		model.m_new = m_new;
		return model;
	}

	/**
	 * @return true if the given action name/kit class is defined
	 */
	public boolean contains(Class kitclass, String actionName) {
		if (actionName == null || kitclass == null)
			return false;

		for (int index = 0; index < getRowCount(); index++) {
			KeyBindingWrapper wrapper = getRow(index);
			if (actionName.equalsIgnoreCase(wrapper.getActionName()) && kitclass == wrapper.getKitClass())
				return true;
		}

		return false;
	}

	/**
	 * @return all action names
	 */
	public Collection getActionNames() {
		TreeSet names = new TreeSet();
		for (int index = 0; index < getRowCount(); index++) {
			KeyBindingWrapper wrapper = getRow(index);
			names.add(wrapper.getActionName());
		}
		return names;
	}

	/**
	 * @return the name of the editor for this model
	 */
	public String getBindingName() {
		return m_editorName;
	}

	/**
	 * @return the number of columns in the table
	 */
	public int getColumnCount() {
		return m_colNames.length;
	}

	/**
	 * @param column
	 *            the colum index
	 * @return the column name at the given column index
	 */
	public String getColumnName(int column) {
		return m_colNames[column];
	}

	/**
	 * @param column
	 *            the colum index
	 * @return the column class at the given column index
	 */
	public Class getColumnClass(int column) {
		return m_colTypes[column];
	}

	/**
	 * @return a new instance of the key binding model
	 */
	public KitKeyBindingModel getBindingModel(Class kitclass) {
		KitKeyBindingModel result = new KitKeyBindingModel(getBindingName(), kitclass);
		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			KeyBindingWrapper wrapper = (KeyBindingWrapper) iter.next();
			if (wrapper.getKitClass() == kitclass)
				result.add(wrapper.getBinding());
		}
		return result;
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
		for (int index = 0; index < getRowCount(); index++) {
			KeyBindingWrapper wrapper = getRow(index);
			MultiKeyBinding binding = wrapper.getBinding();
			if (actionName == null && binding.actionName == null)
				results.add(binding);
			else if (actionName != null && actionName.equals(binding.actionName))
				results.add(binding);
		}

		MultiKeyBinding[] bindings = new MultiKeyBinding[0];
		return (MultiKeyBinding[]) results.toArray(bindings);
	}

	/**
	 * @return the small icon that is used for the given class
	 */
	private static Icon getIcon(Class kitClass) {
		Icon icon = null;

		if (kitClass == TSKit.class) {
			icon = m_basekitIcon;
		} else if (kitClass == FrameKit.class) {
			icon = TSGuiToolbox.loadImage("incors/16x16/window_earth.png");
		} else {
			KitInfo kitinfo = TSEditorMgr.getKitInfo(kitClass);
			if (kitinfo != null)
				icon = kitinfo.getIcon();
		}

		return icon;
	}

	/**
	 * @return the top level editor kit class for this model
	 */
	public KitSet getKitSet() {
		return m_kitset;
	}

	/**
	 * @param row
	 *            the table row
	 * @return the key binding information at the given table row
	 */
	public KeyBindingWrapper getRow(int row) {
		KeyBindingWrapper wrapper = (KeyBindingWrapper) m_data.get(row);
		return wrapper;
	}

	/**
	 * @return the number of rows in the table
	 */
	public int getRowCount() {
		return m_data.size();
	}

	/**
	 * @return the Object at the given row and column
	 * @param row
	 *            the row which to get the object from
	 * @param column
	 *            the column which to get the object from
	 */
	public Object getValueAt(int row, int column) {
		// "Key Stroke", "Action"
		KeyBindingWrapper wrapper = (KeyBindingWrapper) m_data.get(row);
		if (column == 0) // Name
			return wrapper;
		else if (column == 1) // description
		{
			MultiKeyBinding binding = wrapper.getBinding();
			if (binding.keys != null)
				return Utilities.keySequenceToString(binding.keys);
			else {
				// convert simple KeyStroke to KeyStroke[1]
				if (binding.key == null)
					return null;

				KeyStroke[] sequence = new KeyStroke[1];
				sequence[0] = binding.key;
				return Utilities.keySequenceToString(sequence);
			}
		}
		return "";
	}

	/**
	 * @return true if the given action name is defined
	 */
	public boolean isDefined(String actionName) {
		if (actionName == null)
			return false;

		for (int index = 0; index < getRowCount(); index++) {
			KeyBindingWrapper wrapper = getRow(index);
			if (actionName.equalsIgnoreCase(wrapper.getActionName()))
				return true;
		}

		return false;
	}

	/**
	 * @return the flag that indicates if this is a newly created model (one
	 *         that hasn't been saved yet )
	 */
	public boolean isNew() {
		return m_new;
	}

	/**
	 * Loads the data model into the gui model
	 * 
	 * @param model
	 *            the model to load the key bindings from
	 * @param icon
	 *            the icon to associate with the actions
	 */
	void loadModel(KitKeyBindingModel model, Icon icon) {
		// System.out.println( "KeyBindingModel  loadModel" );
		// model.print();
		for (int index = 0; index < model.size(); index++)
			m_data.add(new KeyBindingWrapper(model.getKitClass(), model.get(index), icon));
	}

	/**
	 * Remove an action from the model
	 * 
	 * @return the removed object
	 */
	public void removeAction(String actionName) {
		if (actionName == null)
			return;

		for (int index = 0; index < getRowCount(); index++) {
			KeyBindingWrapper wrapper = getRow(index);
			if (actionName.equalsIgnoreCase(wrapper.getActionName())) {
				removeRow(index);
				index--;
			}
		}
	}

	/**
	 * Remove an object from the model
	 * 
	 * @return the removed object
	 */
	public KeyBindingWrapper removeRow(int row) {
		KeyBindingWrapper wrapper = (KeyBindingWrapper) m_data.remove(row);
		fireTableChanged(new TableModelEvent(this, row, row, 0, TableModelEvent.DELETE));
		return wrapper;
	}

	/**
	 * Iterates over the list of all actions/bindings in the model. If an action
	 * name is equal to the oldname, then we change to newname.
	 * 
	 * @param newname
	 *            the new name that we change to
	 * @param oldname
	 *            the old name we are changing
	 */
	public void renameAction(String newname, String oldname) {
		for (int index = 0; index < getRowCount(); index++) {
			KeyBindingWrapper wrapper = getRow(index);
			if (oldname.equalsIgnoreCase(wrapper.getActionName()))
				wrapper.setActionName(newname);
		}
	}

	/**
	 * Sets the name of the editor for this model
	 */
	public void setBindingName(String bname) {
		m_editorName = bname;
	}

	/**
	 * Sets the flag that indicates that this is a new model. This tells the
	 * view/controller that the user can change the editor name.
	 */
	void setNew(boolean bnew) {
		m_new = bnew;
	}

}
