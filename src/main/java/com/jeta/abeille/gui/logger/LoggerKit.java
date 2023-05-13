package com.jeta.abeille.gui.logger;

import java.util.Map;

import javax.swing.Action;

import org.netbeans.editor.MultiKeyBinding;

import com.jeta.abeille.gui.sql.SQLKit;

import com.jeta.foundation.gui.editor.macros.Macro;
import com.jeta.foundation.gui.editor.TSPlainKit;

/**
 * Override SQLKit so we can provide our own settings
 * 
 * @author Jeff Tassin
 */
public class LoggerKit extends TSPlainKit {
	/**
	 * ctor
	 */
	public LoggerKit() {
	}

	/**
	 * List all actions supported by this class.
	 */
	public static Action[] listDefaultActions() {
		return new Action[0];
	}

	/**
	 * Get the default bindings.
	 */
	public static MultiKeyBinding[] listDefaultKeyBindings() {
		return new MultiKeyBinding[0];
	}

	/**
	 * @return the list default macros supported by this kit
	 */
	public static Macro[] listDefaultMacros() {
		return new Macro[0];
	}

}
