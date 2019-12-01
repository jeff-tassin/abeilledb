package com.jeta.abeille.gui.update;

import javax.swing.JPanel;

import com.jeta.abeille.database.model.ColumnMetaData;

import com.jeta.foundation.gui.components.TSPanel;

/**
 * This interface defines how column components should be created/handled for
 * various column types. Because we allow the user to customize the
 * InstanceView, we need to have polymorphic column handling capability. For
 * example, the user may specify that a particular column is always an XML file.
 * We would then have an XMLHandler for that column that knows how to handle,
 * edit xml files.
 * 
 * @author Jeff Tassin
 */
public interface ColumnHandler {
	/**
	 * Creates the component used to handle data for this column in the
	 * InstanceView
	 */
	public InstanceComponent createComponent(ColumnMetaData cmd, InstanceView view);

	/**
	 * Creates the panel/controller that is used to configure this handler.
	 * 
	 * @return the panel with the correct layout and controller
	 */
	public TSPanel createConfigurationPanel();

	/**
	 * @return the name of this handler. This is the name used to display in
	 *         various controls, so it should support I18N
	 */
	public String getName();

	/**
	 * Reads the user input from the given panel. This panel should be the one
	 * created by the call to createConfigurationPanel().
	 */
	void readInput(TSPanel panel);

}
