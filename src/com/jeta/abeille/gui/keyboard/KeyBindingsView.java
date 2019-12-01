package com.jeta.abeille.gui.keyboard;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.TreeMap;

import javax.swing.KeyStroke;
import javax.swing.JTextField;

import com.jeta.forms.components.panel.FormPanel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

/**
 * Shows the key short cuts preferences
 * 
 * @author Jeff Tassin
 */
public class KeyBindingsView extends TSPanel {
	/** the data store for our properties */
	private TSUserProperties m_userprops;

	private FormPanel m_form;

	/**
	 * Map of component names to key bindings
	 */
	private TreeMap m_bindings = new TreeMap();

	private KeyAssignmentListener m_key_listener = new KeyAssignmentListener();

	/**
	 * ctor
	 */
	public KeyBindingsView(TSUserProperties userprops) {
		setLayout(new BorderLayout());
		m_form = new FormPanel("com/jeta/abeille/gui/keyboard/keyShortcuts.jfrm");
		add(m_form, BorderLayout.CENTER);

		m_userprops = userprops;
		loadData();

		m_form.getComponentByName(KeyBindingsNames.ID_MODEL_VIEW).addKeyListener(m_key_listener);
		m_form.getComponentByName(KeyBindingsNames.ID_SQL_EDITOR).addKeyListener(m_key_listener);
		m_form.getComponentByName(KeyBindingsNames.ID_TABLE_PROPERTIES).addKeyListener(m_key_listener);
		m_form.getComponentByName(KeyBindingsNames.ID_OBJECT_TREE).addKeyListener(m_key_listener);
		m_form.getComponentByName(KeyBindingsNames.ID_SYSTEM_INFO).addKeyListener(m_key_listener);
		m_form.getComponentByName(KeyBindingsNames.ID_SWITCH_CONNECTIONS).addKeyListener(m_key_listener);
	}

	private void keySequenceInputFieldKeyTyped(java.awt.event.KeyEvent evt) {
		evt.consume();
	}

	private void keySequenceInputFieldKeyReleased(java.awt.event.KeyEvent evt) {
		evt.consume();
	}

	private void keySequenceInputFieldKeyPressed(java.awt.event.KeyEvent evt) {
		evt.consume();
		JTextField txtfield = (JTextField) evt.getComponent();
		String modif = KeyEvent.getKeyModifiersText(evt.getModifiers());
		if (isModifier(evt.getKeyCode())) {
			txtfield.setText(modif);
		} else {
			KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(evt);
			setBinding(txtfield.getName(), stroke);
		}
	}

	private boolean isModifier(int keyCode) {
		return (keyCode == KeyEvent.VK_ALT) || (keyCode == KeyEvent.VK_ALT_GRAPH) || (keyCode == KeyEvent.VK_CONTROL)
				|| (keyCode == KeyEvent.VK_SHIFT) || (keyCode == KeyEvent.VK_META);
	}

	/**
	 * Loads the data from the user properties into the model
	 */
	private void loadData() {
		KeyboardManager kmgr = KeyboardManager.getInstance();
		setBinding(KeyBindingsNames.ID_MODEL_VIEW, kmgr.getKeyStroke(KeyBindingsNames.ID_MODEL_VIEW));
		setBinding(KeyBindingsNames.ID_SQL_EDITOR, kmgr.getKeyStroke(KeyBindingsNames.ID_SQL_EDITOR));
		setBinding(KeyBindingsNames.ID_TABLE_PROPERTIES, kmgr.getKeyStroke(KeyBindingsNames.ID_TABLE_PROPERTIES));
		setBinding(KeyBindingsNames.ID_OBJECT_TREE, kmgr.getKeyStroke(KeyBindingsNames.ID_OBJECT_TREE));
		setBinding(KeyBindingsNames.ID_SYSTEM_INFO, kmgr.getKeyStroke(KeyBindingsNames.ID_SYSTEM_INFO));
		setBinding(KeyBindingsNames.ID_SWITCH_CONNECTIONS, kmgr.getKeyStroke(KeyBindingsNames.ID_SWITCH_CONNECTIONS));
	}

	private void setBinding(String compName, KeyStroke keystroke) {
		setText(compName, org.netbeans.editor.Utilities.keyStrokeToString(keystroke));
		m_bindings.put(compName, keystroke);
	}

	public void save() {
		KeyboardManager kmgr = KeyboardManager.getInstance();
		kmgr.assignKeyStroke(KeyBindingsNames.ID_MODEL_VIEW, (KeyStroke) m_bindings.get(KeyBindingsNames.ID_MODEL_VIEW));
		kmgr.assignKeyStroke(KeyBindingsNames.ID_SQL_EDITOR, (KeyStroke) m_bindings.get(KeyBindingsNames.ID_SQL_EDITOR));
		kmgr.assignKeyStroke(KeyBindingsNames.ID_TABLE_PROPERTIES,
				(KeyStroke) m_bindings.get(KeyBindingsNames.ID_TABLE_PROPERTIES));
		kmgr.assignKeyStroke(KeyBindingsNames.ID_OBJECT_TREE,
				(KeyStroke) m_bindings.get(KeyBindingsNames.ID_OBJECT_TREE));
		kmgr.assignKeyStroke(KeyBindingsNames.ID_SYSTEM_INFO,
				(KeyStroke) m_bindings.get(KeyBindingsNames.ID_SYSTEM_INFO));
		kmgr.assignKeyStroke(KeyBindingsNames.ID_SWITCH_CONNECTIONS,
				(KeyStroke) m_bindings.get(KeyBindingsNames.ID_SWITCH_CONNECTIONS));
	}

	private class KeyAssignmentListener extends KeyAdapter {
		public void keyTyped(KeyEvent evt) {
			keySequenceInputFieldKeyTyped(evt);
		}

		public void keyPressed(KeyEvent evt) {
			keySequenceInputFieldKeyPressed(evt);
		}

		public void keyReleased(KeyEvent evt) {
			keySequenceInputFieldKeyReleased(evt);
		}
	}

}
