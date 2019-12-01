package com.jeta.abeille.gui.procedures;

import java.awt.Container;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.StoredProcedure;

import com.jeta.abeille.gui.update.InstanceMetaData;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.update.InstanceViewBuilder;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSInternalFrame;

import com.jeta.open.gui.framework.JETAController;

/**
 * This is a builder class that builds the view for the InstanceFrame and
 * Procedure Input View
 * 
 * @author Jeff Tassin
 */
public class ProcedureInputViewBuilder implements InstanceViewBuilder {
	/** the database connection */
	private TSConnection m_connection;

	/** the data model */
	private ProcedureModel m_model;

	/** the metadata that describes the data for the InstanceView */
	private ProcedureParametersMetaData m_metadata;

	private InstanceView m_view;

	/**
	 * ctor
	 * 
	 * @param formModel
	 *            the underlying database connection
	 */
	public ProcedureInputViewBuilder(TSConnection connection, ProcedureModel model) {
		m_connection = connection;
		m_model = model;
	}

	public void configure() {
		assert (false);
	}

	/**
	 * Not used just yet
	 */
	public String getID() {
		return m_model.getProcedure().getKey().toString();
	}

	/**
	 * Creates the InstanceMetaData object for the InstanceView/Model
	 */
	public InstanceMetaData getMetaData() {
		if (m_metadata == null) {
			m_metadata = new ProcedureParametersMetaData(m_connection, m_model);
		}
		return m_metadata;
	}

	/**
	 * Saves the instance meta data to the application state store.
	 */
	public JETAController getController(TSInternalFrame frame) {
		return null;
	}

	/**
	 * @return the view for this instance data
	 */
	public InstanceView getView() {
		if (m_view == null) {
			InstanceModel model = new InstanceModel(m_connection, getMetaData());
			m_view = new InstanceView(model);
		}
		return m_view;
	}

	/**
	 * @param frame
	 *            allows the builder to perform any initializations on the frame
	 *            such as set the title
	 */
	public void initializeFrame(TSInternalFrame frame) {

	}

	public void save() {
		assert (false);
	}

}
