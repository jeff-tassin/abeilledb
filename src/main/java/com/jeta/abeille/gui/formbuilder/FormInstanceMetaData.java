package com.jeta.abeille.gui.formbuilder;

import java.util.HashMap;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.update.ColumnSettings;
import com.jeta.abeille.gui.update.DefaultColumnHandler;
import com.jeta.abeille.gui.update.InstanceMetaData;

/**
 * This class defines the meta data used for the form builder. This class also
 * maintains the column settings for a given column (e.g. whether the column is
 * visible or not).
 * 
 * @author Jeff Tassin
 */
public class FormInstanceMetaData extends InstanceMetaData {
	private final static String FORM_INSTANCE_META_DATA = "form.instance.metadata";

	/** the table id of the anchor table */
	private TableId m_anchortable;

	/**
	 * ctor
	 */
	public FormInstanceMetaData() {

	}

	/**
	 * ctor
	 */
	public FormInstanceMetaData(ColumnSettings[] settings, TableId anchortable) {
		for (int index = 0; index < settings.length; index++) {
			addColumnSettings(settings[index]);
		}
		m_anchortable = anchortable;
	}

	/**
	 * Adds a column to the model using the default handler
	 */
	public void addColumn(ColumnMetaData cmd) {
		ColumnSettings settings = new ColumnSettings(cmd, true);
		int index = getIndex(cmd);
		if (index < 0) {
			addColumnSettings(settings);
		}
	}

	/**
	 * @return the anchor table for the form
	 */
	public TableId getAnchorTable() {
		return m_anchortable;
	}

	/**
	 * @return the link model for this meta data
	 */
	public LinkModel getLinkModel() {
		return new DefaultLinkModel();
	}

	/**
	 * @return the key used to store the tables properties in the application
	 *         store
	 */
	private String getPropertiesKey() {
		String props = FORM_INSTANCE_META_DATA;
		return props;
	}

	/**
	 * @return a unique identifier for this model. This is a query result, so
	 *         the UID is the query string. If this is from a saved query, then
	 *         this is the UID of the saved query
	 */
	public String getUID() {
		return "forminstancemodel.uid";
	}

	/**
	 * @return always return true since all columns in a form are 'links'
	 */
	public boolean isLink(ColumnMetaData cmd) {
		return true;
	}

	/**
	 * Loads the settings from the persistent store
	 */
	public void loadSettings() {
		assert (false);
	}

	/**
	 * Resets to the defaults
	 */
	public void reset() {
		// @toto test this by changing table metadata against previously stored
		// information
		for (int index = 0; index < getColumnCount(); index++) {
			ColumnSettings csi = getColumnSettings(index);
			ColumnSettings defaultcsi = new ColumnSettings(csi.getColumnMetaData(), true, new DefaultColumnHandler());
			setRow(index, defaultcsi);
		}
	}

	void setAnchorTable(TableId anchor) {
		m_anchortable = anchor;
	}

}
