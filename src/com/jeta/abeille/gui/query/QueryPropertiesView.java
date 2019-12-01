package com.jeta.abeille.gui.query;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;

import com.jeta.abeille.gui.common.DefaultTableSelectorModel;
import com.jeta.abeille.gui.common.SchemaSelectorPanel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.forms.components.panel.FormPanel;

/**
 * This is a panel that allows the user to modify properties for a query
 * 
 * @author Jeff Tassin
 */
public class QueryPropertiesView extends FormPanel {

	public QueryPropertiesView(QueryModel model) {
		super("com/jeta/abeille/gui/query/queryOptions.jfrm");

		setSelected(QueryPropertiesNames.ID_DISTINCT, model.isDistinct());
		setSelected(QueryPropertiesNames.ID_QUALIFIED, model.isQualified());
		SchemaSelectorPanel spanel = (SchemaSelectorPanel) getComponentByName(QueryPropertiesNames.ID_SCHEMA_PANEL);
		assert (model.getConnection() != null);
		spanel.setModel(new DefaultTableSelectorModel(model.getConnection()));
		spanel.setCatalog(model.getCatalog());
		spanel.setSchema(model.getSchema());
	}

	public Catalog getCatalog() {
		SchemaSelectorPanel spanel = (SchemaSelectorPanel) getComponentByName(QueryPropertiesNames.ID_SCHEMA_PANEL);
		return spanel.getCatalog();
	}

	public Schema getSchema() {
		SchemaSelectorPanel spanel = (SchemaSelectorPanel) getComponentByName(QueryPropertiesNames.ID_SCHEMA_PANEL);
		return spanel.getSchema();
	}

	/**
	 * @return true if the qualified checkbox is selected
	 */
	public boolean isDistinct() {
		return getBoolean(QueryPropertiesNames.ID_DISTINCT);
	}

	/**
	 * @return true if the qualified checkbox is selected
	 */
	public boolean isQualified() {
		return getBoolean(QueryPropertiesNames.ID_QUALIFIED);
	}

}
