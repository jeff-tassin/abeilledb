package com.jeta.abeille.gui.procedures;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.database.procedures.ParameterDirection;
import com.jeta.abeille.database.procedures.ProcedureParameter;

import com.jeta.abeille.gui.update.ColumnSettings;
import com.jeta.abeille.gui.update.DefaultColumnHandler;
import com.jeta.abeille.gui.update.InstanceMetaData;

/**
 * This class defines the meta data used for the InstanceView to display
 * procedure inputs.
 * 
 * @author Jeff Tassin
 */
public class ProcedureParametersMetaData extends InstanceMetaData {
	/**
	 * The database connection
	 */
	private TSConnection m_connection;

	/**
	 * The parameters model
	 */
	private ProcedureModel m_model;

	/**
	 * ctor
	 */
	public ProcedureParametersMetaData(TSConnection conn, ProcedureModel model) {
		m_connection = conn;
		m_model = model;
		load(model);
	}

	/**
	 * @return an empty link model for now
	 */
	public LinkModel getLinkModel() {
		return new DefaultLinkModel();
	}

	public String getUID() {
		return "Procedure";
	}

	/**
	 * Loads from a table meta data
	 */
	protected void load(ProcedureModel model) {
		// @toto test this by changing table metadata against previously stored
		// information
		Collection params = model.getParameters();
		Iterator iter = params.iterator();
		while (iter.hasNext()) {
			ProcedureParameter param = (ProcedureParameter) iter.next();
			ParameterDirection direction = param.getDirection();
			if (direction == ParameterDirection.IN || direction == ParameterDirection.INOUT) {
				int datatype = param.getType();
				if (datatype > 0) {
					ColumnMetaData cmd = new ColumnMetaData(param.getName(), datatype, 0, null, 0);
					ColumnSettings defaultci = new ColumnSettings(cmd, true, new DefaultColumnHandler());
					addColumnSettings(defaultci);
				}
			}
		}
	}

	public void reset() {
		assert (false);
	}

}
