package com.jeta.foundation.gui.editor.macros;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;

import org.netbeans.editor.MultiKeyBinding;

import com.jeta.foundation.gui.editor.KeyBindingMgr;
import com.jeta.foundation.gui.editor.KeyUtils;
import com.jeta.foundation.gui.editor.KitSet;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.editor.TSKit;
import com.jeta.foundation.gui.editor.options.EditorOptionsModel;
import com.jeta.foundation.gui.editor.options.KeyBindingModel;
import com.jeta.foundation.gui.editor.options.KeyBindingWrapper;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the model for representing Macro objects in a JTable.
 * 
 * @author Jeff Tassin
 */
public class MacroMgrModel extends AbstractTableModel {
	/** an array of column names of the JTable */
	String[] m_colNames;

	/** this is the types of columns of the JTable */
	Class[] m_colTypes;

	/** the flattened out list of bindings for the editor kit hierarchy */
	private ArrayList m_data = new ArrayList();

	/** the key model we are working with */
	private KeyBindingModel m_keymodel;

	/** the editor options */
	private EditorOptionsModel m_editoroptions;

	/** column ids */
	public static final int NAME_COLUMN = 0;
	public static final int COMMAND_COLUMN = 1;

	/**
	 * Constructor
	 * 
	 * @param kitSet
	 *            the editor kits whose macros we are editing
	 */
	public MacroMgrModel(EditorOptionsModel editormodel, KeyBindingModel keymodel) {
		String[] values = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Command Sequence") };
		m_colNames = values;
		Class[] types = { Macro.class, String.class };
		m_colTypes = types;

		m_editoroptions = editormodel;
		m_keymodel = keymodel;

		Iterator iter = getKitSet().iterator();
		while (iter.hasNext()) {
			Class c = (Class) iter.next();
			MacroModel model = MacroMgr.getMacroModel(c);
			loadModel(model);
		}
	}

	/**
	 * Adds a macro to the model. This method does not check if macro already
	 * exists with the given name of the macro.
	 * 
	 * @param macro
	 *            the macro to add
	 */
	public void addMacro(Macro macro) {
		// if we are here, then just add to first model
		Class kitclass = null;

		Collection actions = macro.getActionNames();
		// Note: the logic that determines which kit class gets the macro is a
		// little tricky here.
		// Basically, we traverse the kitclass hierachy, starting with the
		// bottom-most descendant.
		// If that descendant kit contains ANY action in the macro, then the
		// macro is assigned to
		// that kit.
		Iterator iter = getKitSet().iterator();
		while (iter.hasNext()) {
			Class c = (Class) iter.next();

			KeyBindingModel keymodel = getKeyBindingModel();
			// now check all actions
			Iterator actioniter = actions.iterator();
			while (actioniter.hasNext()) {
				String actionname = (String) actioniter.next();
				if (contains(c, actionname) || keymodel.contains(c, actionname)) {
					kitclass = c;
					break;
				}
			}
		}

		if (kitclass == null) // if we are here, then just add to first model
			kitclass = TSKit.class;

		macro.setIcon(TSEditorUtils.getIcon(kitclass));
		macro.setKitClass(kitclass);
		m_data.add(macro);

		// tell all binding models that an action has been added
		Collection keymodels = m_editoroptions.getBindingModels();
		iter = keymodels.iterator();
		while (iter.hasNext()) {
			KeyBindingModel keymodel = (KeyBindingModel) iter.next();
			keymodel.addBinding(new KeyBindingWrapper(kitclass, KeyUtils.createBinding(macro.getName()), macro
					.getIcon()));
		}
		fireTableChanged(new TableModelEvent(this, getRowCount() - 1, getRowCount() - 1, 0, TableModelEvent.INSERT));
	}

	/**
	 * Iterates over the list of macros in this model and determines if a macro
	 * has the given name and given kit class
	 */
	private boolean contains(Class kitclass, String macroName) {
		for (int index = 0; index < getRowCount(); index++) {
			Macro macro = getRow(index);
			if (I18N.equals(macroName, macro.getName()) && kitclass == macro.getKitClass())
				return true;
		}
		return false;
	}

	/**
	 * Returns the kitclass for the given macro. Null is returned if the macro
	 * is not contained by this model
	 */
	private Class getKitClass(String macroName) {
		for (int index = 0; index < getRowCount(); index++) {
			Macro macro = getRow(index);
			if (I18N.equals(macroName, macro.getName()))
				return macro.getKitClass();
		}
		return null;
	}

	/**
	 * @return all action names
	 */
	public Collection getActionNames() {
		return m_keymodel.getActionNames();
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
	 * @return the key binding model (a clone copy ) that this class updates
	 *         when macros are created/edited
	 */
	KeyBindingModel getKeyBindingModel() {
		return m_keymodel;
	}

	/**
	 * @return the kit class for this model
	 */
	public KitSet getKitSet() {
		return m_keymodel.getKitSet();
	}

	/**
	 * @return a collection of MacroModel objects. These models are instantiated
	 *         an populated in this call. Each model contains macros that are
	 *         assigned to a given editor kit.
	 */
	public Collection getMacroModels() {
		HashMap models = new HashMap();
		for (int index = 0; index < getRowCount(); index++) {
			Macro macro = getRow(index);
			Class kitclass = macro.getKitClass();
			assert (kitclass != null);
			MacroModel model = (MacroModel) models.get(kitclass);
			if (model == null) {
				model = new MacroModel(kitclass);
				models.put(kitclass, model);
			}
			model.add(macro);
		}
		return models.values();
	}

	/**
	 * @return the macro found at the given row
	 */
	public Macro getRow(int row) {
		Macro macro = (Macro) m_data.get(row);
		return macro;
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
		// "Macro Name", "Command Sequence"
		Macro macro = (Macro) m_data.get(row);
		if (column == NAME_COLUMN) // Name
			return macro;
		else if (column == COMMAND_COLUMN) // command sequence
			return macro.getCommand();
		else
			return "";
	}

	/**
	 * @return true if the given action name is defined. Simply forward the call
	 *         to the keybindingmodel because all macros are added there.
	 */
	public boolean isDefined(String actionName) {
		return m_keymodel.isDefined(actionName);
	}

	/**
	 * Loads the data model into the gui model
	 * 
	 * @param model
	 *            the model to load the key bindings from
	 * @param icon
	 *            the icon to associate with the actions
	 */
	void loadModel(MacroModel model) {
		// create a clone copy here
		MacroModel clone = (MacroModel) model.clone();
		for (int index = 0; index < clone.size(); index++) {
			Macro macro = clone.get(index);
			macro.setIcon(TSEditorUtils.getIcon(clone.getKitClass()));
			macro.setKitClass(clone.getKitClass());
			m_data.add(macro);
		}
	}

	/**
	 * Remove an object from the model
	 * 
	 * @return the removed object
	 */
	public Macro removeRow(int row) {
		Macro macro = (Macro) m_data.remove(row);
		// tell all binding models that an action has been deleted
		Collection keymodels = m_editoroptions.getBindingModels();
		Iterator iter = keymodels.iterator();
		while (iter.hasNext()) {
			KeyBindingModel keymodel = (KeyBindingModel) iter.next();
			keymodel.removeAction(macro.getName());
		}

		fireTableChanged(new TableModelEvent(this, row, row, 0, TableModelEvent.DELETE));
		return macro;
	}

	/**
	 * Saves the model
	 */
	public void save() {
		try {
			MacroMgr.clear();
			Collection c = getMacroModels();
			Iterator iter = c.iterator();
			while (iter.hasNext()) {
				MacroModel model = (MacroModel) iter.next();
				MacroMgr.setModel(model.getKitClass(), model);
			}
			MacroMgr.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called when the user changes a macro. The act of changing
	 * a macro may have resulted in changing the kitclass that it belongs to.
	 * For example, the user may have added actions that are defined in a
	 * different kit class than the original actions.
	 * 
	 * @param index
	 *            the index at which the orginal macro is s
	 */
	public void setMacro(int index, Macro macro) {
		// first, remove the old macro (at the index we are replacing ) from all
		// macro models
		// we need to do this because the macro may be invoking actions in
		// different editor kit classes.
		Macro oldmacro = getRow(index);

		Class kitclass = null;
		Iterator iter = getKitSet().iterator();

		while (iter.hasNext()) {
			Class c = (Class) iter.next();
			// now check all actions
			Collection actions = macro.getActionNames();
			Iterator actioniter = actions.iterator();
			while (actioniter.hasNext()) {
				String actionname = (String) actioniter.next();
				if (contains(c, actionname) || m_keymodel.contains(c, actionname)) {
					kitclass = c;
					break;
				}
			}
		}

		if (kitclass == null && oldmacro.getKitClass() == null) {
			macro.setKitClass(TSKit.class);
		} else {
			macro.setKitClass(oldmacro.getKitClass());
		}

		macro.setIcon(TSEditorUtils.getIcon(macro.getKitClass()));
		m_data.set(index, macro);

		String oldname = oldmacro.getName();
		String newname = macro.getName();

		if (!oldname.equalsIgnoreCase(newname)) {
			// tell all binding models that an action name has changed
			Collection keymodels = m_editoroptions.getBindingModels();
			iter = keymodels.iterator();
			while (iter.hasNext()) {
				KeyBindingModel keymodel = (KeyBindingModel) iter.next();
				keymodel.renameAction(macro.getName(), oldname);
			}
		}
		fireTableChanged(new TableModelEvent(this, index, index, 0, TableModelEvent.UPDATE));
	}

}
