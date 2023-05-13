package com.jeta.abeille.gui.formbuilder;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.update.InstanceMetaData;
import com.jeta.abeille.gui.update.InstanceModel;

/**
 * A specialization of InstanceModel that stores form state that is specific to
 * the form builder view
 * 
 * @author Jeff Tassin
 */
public class FormInstanceModel extends InstanceModel {
	/**
	 * ctor
	 */
	public FormInstanceModel(TSConnection connection, InstanceMetaData metadata) {
		super(connection, metadata);
	}

}
