package com.jeta.abeille.gui.update;

import java.awt.Container;

import com.jeta.abeille.database.model.LinkModel;

import com.jeta.foundation.gui.components.TSInternalFrame;

import com.jeta.open.gui.framework.JETAController;

/**
 * This is an abstract factory class for creating InstanceViews and their
 * controller classes
 * 
 * @author Jeff Tassin
 */
public interface InstanceViewBuilder {
	/**
	 * Invoked with the user wants to configure the view options. This generally
	 * launches a dialog that allows to user to configure the instance view
	 */
	public void configure();

	/**
	 * @return an identifier that identifies the frame so that if the user wants
	 *         to launch the same frame again, we can locate it
	 */
	public String getID();

	/**
	 * @return the InstanceView that will be displayed in the frame
	 */
	public InstanceView getView();

	/**
	 * @param parentFrame
	 *            the parent window that contains the view
	 * @return the controller that is used to handler user events
	 */
	public JETAController getController(TSInternalFrame parentFrame);

	/**
	 * @param frame
	 *            allows the builder to perform any initializations on the frame
	 *            such as set the title
	 */
	public void initializeFrame(TSInternalFrame frame);

	/**
	 * Saves any configuration settings for the current view
	 */
	public void save();

}
