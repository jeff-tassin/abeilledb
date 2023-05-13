package com.jeta.abeille.gui.store;

import java.io.IOException;

import java.util.HashMap;

import javax.swing.KeyStroke;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * KeyBinding assignments for application windows
 */
public class KeyBindings implements JETAExternalizable {
	static final long serialVersionUID = 4339218111481280475L;

	public static int VERSION = 1;

	private HashMap m_bindings = new HashMap();

	public KeyBindings() {
	}

	public void assignKeyStroke(String componentId, KeyStroke stroke) {
		m_bindings.put(componentId, new KeyStrokeInfo(stroke));
	}

	public boolean contains(String componentId) {
		return m_bindings.containsKey(componentId);
	}

	public KeyStroke getKeyStroke(String componentId) {
		KeyStrokeInfo info = (KeyStrokeInfo) m_bindings.get(componentId);
		if (info != null)
			return info.getKeyStroke();
		else
			return null;
	}

	public int size() {
		return m_bindings.size();
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_bindings = (HashMap) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_bindings);
	}

}
