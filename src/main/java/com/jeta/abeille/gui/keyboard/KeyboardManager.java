package com.jeta.abeille.gui.keyboard;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.KeyStroke;

import com.jeta.abeille.gui.store.KeyBindings;
import com.jeta.abeille.gui.model.ModelViewFrame;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;
import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * Responsible for managing key shortcut assignments for frame windows.
 * 
 * @author Jeff Tassin
 */
public class KeyboardManager {
	public final static String COMPONENT_ID = "application.keyboard.manager";

	private KeyBindings m_bindings;
	private static KeyboardManager m_singleton;

	private KeyboardManager() {
		load();
	}

	private void assignDefault(String componentId, KeyStroke defKey) {
		if (!m_bindings.contains(componentId)) {
			m_bindings.assignKeyStroke(componentId, defKey);
		}
	}

	public static KeyboardManager getInstance() {
		if (m_singleton == null)
			m_singleton = new KeyboardManager();
		return m_singleton;
	}

	public void assignKeyStroke(String componentId, KeyStroke stroke) {
		m_bindings.assignKeyStroke(componentId, stroke);
	}

	public KeyStroke getKeyStroke(String componentId) {
		return m_bindings.getKeyStroke(componentId);
	}

	private void load() {
		try {
			ObjectStore os = (ObjectStore) ComponentMgr.lookup(ComponentNames.APPLICATION_STATE_STORE);
			m_bindings = (KeyBindings) os.load(KeyboardManager.COMPONENT_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/** load defaults */
		if (m_bindings == null || m_bindings.size() == 0) {
			m_bindings = new KeyBindings();
		}

		assignDefault(KeyBindingsNames.ID_MODEL_VIEW,
				KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
		assignDefault(KeyBindingsNames.ID_SQL_EDITOR,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
		assignDefault(KeyBindingsNames.ID_TABLE_PROPERTIES,
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
		assignDefault(KeyBindingsNames.ID_OBJECT_TREE,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
		assignDefault(KeyBindingsNames.ID_SYSTEM_INFO,
				KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
		assignDefault(KeyBindingsNames.ID_SWITCH_CONNECTIONS,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
	}

	public void save() {
		try {
			ObjectStore os = (ObjectStore) ComponentMgr.lookup(ComponentNames.APPLICATION_STATE_STORE);
			os.store(KeyboardManager.COMPONENT_ID, m_bindings);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
