package com.jeta.abeille.gui.security.mysql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.foundation.gui.components.TSController;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * Controller for the MySQLUserView class
 * 
 * @author Jeff Tassin
 */
public class MySQLUserViewController extends TSController {
	/** the view we are handling events for */
	private MySQLUserView m_view;

	/**
	 * ctor
	 */
	public MySQLUserViewController(MySQLUserView view) {
		super(null);
		// m_view = view;
	}

}
