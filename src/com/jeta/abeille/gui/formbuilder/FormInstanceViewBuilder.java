package com.jeta.abeille.gui.formbuilder;

import java.awt.Container;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.update.InstanceController;
import com.jeta.abeille.gui.update.InstanceFrame;
import com.jeta.abeille.gui.update.InstanceMetaData;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.update.InstanceViewBuilder;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.MenuTemplate;

import com.jeta.open.gui.framework.JETAController;

/**
 * This is a builder class that builds the view for the InstanceFrame and
 * FormBuilder
 * 
 * @author Jeff Tassin
 */
public class FormInstanceViewBuilder implements InstanceViewBuilder {
	/** the form model that determines the makeup (metadata) of the view */
	private FormModel m_model;

	/** the controller */
	private InstanceController m_controller;

	/** the view */
	private InstanceView m_view;

	/** the frame */
	private InstanceFrame m_frame;

	/**
	 * ctor
	 * 
	 * @param formModel
	 *            the underlying database connection
	 */
	public FormInstanceViewBuilder(FormModel formModel) {
		m_model = formModel;
	}

	public void configure() {
		assert (false);
	}

	/**
	 * Not used just yet
	 */
	public String getID() {
		assert (false);
		return null;
	}

	/**
	 * Creates the InstanceMetaData object for the InstanceView/Model
	 */
	public InstanceMetaData getMetaData() {
		return m_model.getInstanceMetaData();
	}

	/**
	 * Saves the instance meta data to the application state store.
	 */
	public JETAController getController(TSInternalFrame frame) {
		m_frame = (InstanceFrame) frame;
		if (m_controller == null) {
			FormInstanceView view = (FormInstanceView) getView();
			m_controller = new FormInstanceController(m_model, m_frame, view, (FormInstanceModel) view.getModel());
			// for handling popup menu actions
			view.setController(m_controller);
		}
		return m_controller;
	}

	/**
	 * @return the view for this instance data
	 */
	public InstanceView getView() {
		if (m_view == null) {
			FormInstanceModel model = new FormInstanceModel(m_model.getConnection(), getMetaData());
			m_view = new FormInstanceView(model);
		}
		return m_view;
	}

	/**
	 * @param frame
	 *            allows the builder to perform any initializations on the frame
	 *            such as set the title
	 */
	public void initializeFrame(TSInternalFrame frame) {
		InstanceFrame iframe = (InstanceFrame) frame;
		TableId tableid = m_model.getAnchorTable();
		if (tableid != null) {
			iframe.setTitle(m_model.getName() + " - " + m_model.getConnection().getShortId());
			iframe.setShortTitle(m_model.getName());
			iframe.setFrameIcon(FormBuilderFrame.m_frameicon);
		}

		MenuTemplate template = iframe.getMenuTemplate();
		MenuDefinition menu = new MenuDefinition("View");
		menu.add(iframe.i18n_createMenuItem("Query", FormNames.ID_SHOW_QUERY_PLAN, null));
		menu.add(iframe.i18n_createMenuItem("Insert Plan", FormNames.ID_SHOW_INSERT_PLAN, null));
		menu.add(iframe.i18n_createMenuItem("Update Plan", FormNames.ID_SHOW_UPDATE_PLAN, null));
		menu.add(iframe.i18n_createMenuItem("Delete Plan", FormNames.ID_SHOW_DELETE_PLAN, null));
		template.add(menu, 2);
	}

	public void save() {

	}

}
