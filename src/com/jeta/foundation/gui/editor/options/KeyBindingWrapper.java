package com.jeta.foundation.gui.editor.options;

import javax.swing.Icon;
import org.netbeans.editor.MultiKeyBinding;

import com.jeta.foundation.gui.editor.KeyUtils;

/**
 * Wrapper object for KeyBinding.
 * 
 * @author Jeff Tassin
 */
public class KeyBindingWrapper implements Comparable, Cloneable {
	/** the binding */
	private MultiKeyBinding m_binding;

	/** the icon for the editor kit */
	private Icon m_icon;

	/** the editor kit that defines the action/binding */
	private Class m_kitclass;

	/**
	 * flag that indicates if the given key binding is a macro
	 */
	private boolean m_macro;

	/**
    *
    */
	public KeyBindingWrapper(Class kitclass, MultiKeyBinding binding, Icon icon) {
		m_kitclass = kitclass;
		m_binding = binding;
		m_icon = icon;
	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		MultiKeyBinding binding = KeyUtils.cloneBinding(m_binding);
		KeyBindingWrapper wrapper = new KeyBindingWrapper(m_kitclass, binding, m_icon);
		return wrapper;
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object obj) {
		if (obj instanceof KeyBindingWrapper) {
			KeyBindingWrapper wrapper = (KeyBindingWrapper) obj;
			if (m_binding.actionName == null)
				return -1;
			else {
				Class kit = wrapper.m_kitclass;
				if (m_kitclass == kit)
					return m_binding.actionName.compareTo(wrapper.m_binding.actionName);
				else if (m_kitclass.isInstance(kit))
					return -1;
				else
					return 1;
			}
		} else
			return -1;
	}

	/**
	 * @return the action name for the binding
	 */
	public String getActionName() {
		return m_binding.actionName;
	}

	/**
	 * @return the actual binding
	 */
	public MultiKeyBinding getBinding() {
		return m_binding;
	}

	/**
	 * @return the class of the editor kit
	 */
	public Class getKitClass() {
		return m_kitclass;
	}

	/**
	 * @return the icon for this binding
	 */
	public Icon getIcon() {
		return m_icon;
	}

	/**
	 * Sets the action name for the binding
	 */
	public void setActionName(String actionName) {
		m_binding.actionName = actionName;
	}

	public String toString() {
		return m_binding.actionName;
	}
}
