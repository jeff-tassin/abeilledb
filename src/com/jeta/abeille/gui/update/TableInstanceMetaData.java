package com.jeta.abeille.gui.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.MultiColumnLink;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.ModelerModel;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class defines the meta data used for the InstanceView. All columns for a
 * given result (i.e. TableMetaData or ResultSetMetaData from a query) are
 * represented. This class also maintains the column settings for a given column
 * (e.g. whether the column is visible or not).
 * 
 * @author Jeff Tassin
 */
public class TableInstanceMetaData extends InstanceMetaData {
	private TSConnection m_connection;
	private TableId m_tableid;
	private TableMetaData m_tmd;

	/** the link model for this instance metadata */
	private LinkModel m_linkmodel;

	private final static String TABLE_INSTANCE_META_DATA = "table.instance.metadata";

	/**
	 * The stored column settings. We cache for performance
	 */
	private HashMap m_stored_settings;

	/**
	 * ctor
	 */
	public TableInstanceMetaData(TSConnection conn, TableId tableId) {
		this(conn, tableId, null);
	}

	/**
	 * ctor This constructor allows us to preset which columns will be shown to
	 * the user. It will still pull the column settings (i.e. handler
	 * definitions) from the user store for the given table, but the orderings
	 * and visible will be dictated by the passed in values
	 */
	public TableInstanceMetaData(TSConnection conn, TableId tableId, ColumnMetaData[] cols) {
		m_connection = conn;
		m_tableid = tableId;

		// load the model with the default settings
		if (tableId != null) {
			TableMetaData tmd = m_connection.getModel(tableId.getCatalog()).getTableEx(tableId,	TableMetaData.LOAD_FOREIGN_KEYS);
			m_tmd = tmd;
		} else {
			m_tmd = null;
		}
		load(m_tmd, cols);
	}

	/**
	 * @return the link model for this meta data
	 */
	public LinkModel getLinkModel() {
		if (m_linkmodel == null) {
			DefaultLinkModel linkmodel = new DefaultLinkModel();
			// the link model for table consists of the foreign key links as
			// well as the user defined global links
			Collection fkeys = m_tmd.getForeignKeys();
			Iterator iter = fkeys.iterator();
			while (iter.hasNext()) {
				// decompose each foreign key into individual links and add to
				// the link model
				MultiColumnLink fklink = new MultiColumnLink((DbForeignKey) iter.next());
				Collection links = fklink.getLinks();
				Iterator fiter = links.iterator();
				while (fiter.hasNext()) {
					linkmodel.addLink((Link) fiter.next());
				}
			}

			try {
				ModelerModel modeler = ModelerModel.getDefaultInstance(m_connection);
				if (modeler != null) {
					Collection c = modeler.getUserDefinedLinks();
					Iterator liter = c.iterator();
					while (liter.hasNext()) {
						Link link = (Link) liter.next();
						if (link.contains(m_tableid)) {
							// System.out.println("TableInstanceMetaData.getLinkModel: "
							// + m_tableid );
							// link.print();
							// System.out.println();
							linkmodel.addLink(link);
						}
					}
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}

			/** load all exported keys */
			m_connection.getTableEx(m_tmd.getTableId(), TableMetaData.LOAD_EXPORTED_KEYS);
			Collection exported_keys = m_tmd.getExportedKeys();
			iter = exported_keys.iterator();
			while (iter.hasNext()) {
				// MultiColumnLink mlink = new MultiColumnLink(
				// (DbForeignKey)iter.next(), true );
				MultiColumnLink mlink = new MultiColumnLink((DbForeignKey) iter.next());
				Collection links = mlink.getLinks();
				Iterator fiter = links.iterator();
				while (fiter.hasNext()) {
					Link link = (Link) fiter.next();
					linkmodel.addLink(link);
				}
			}

			m_linkmodel = linkmodel;
		}

		return m_linkmodel;
	}

	/**
	 * @return the key used to store the tables properties in the application
	 *         store
	 */
	private String getPropertiesKey() {
		String tableprops = TABLE_INSTANCE_META_DATA + "." + m_tableid.getFullyQualifiedName();
		return tableprops;
	}

	/**
	 * @return the underlying table id
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * @return the underlying table metadata
	 */
	public TableMetaData getTableMetaData() {
		return m_tmd;
	}

	/**
	 * @return a unique identifier for this model. If this is a table model,
	 *         then the UID is basically the schema.tablename. If this is a
	 *         query result, then the UID is the query string. If this is from a
	 *         saved query, then this is the UID of the saved query
	 */
	public String getUID() {
		return m_tmd.getFullyQualifiedName();
	}

	/**
	 * @return true if the given column name is a foreign key
	 */
	public boolean isLink(ColumnMetaData cmd) {
		LinkModel linkmodel = getLinkModel();
		Collection links = linkmodel.getLinks(m_tableid);
		Iterator iter = links.iterator();
		while (iter.hasNext()) {
			Link link = (Link) iter.next();
			if (link.contains(m_tableid, cmd.getColumnName())) {
				return true;
			}
			// if ( cmd.getColumnName().equals( .getDestinationColumn() ) )
			// return true;
		}
		return false;
	}

	/**
	 * @return true if the given column name is a primary key
	 */
	public boolean isPrimaryKey(ColumnMetaData cmd) {
		return m_tmd.isPrimaryKey(cmd.getColumnName());
	}

	/**
	 * @return the stored column settings for this table
	 */
	public ColumnSettings getStoredColumnSettings(String colName) {


		if (m_tmd == null)
			return null;

		if (m_stored_settings == null) {
			HashMap colsettings = null;
			ObjectStore os = m_connection.getObjectStore();
			String oskey = getPropertiesKey();

			try {
				// try to load saved preferences
				Object storeddata = os.load(oskey);
				if (storeddata instanceof ArrayList) {
					/**
					 * legacy support. Abeille versions < 1.4 store settings in
					 * an ArrayList and not a HashMap
					 */
					ArrayList sa = (ArrayList) storeddata;
					for (int index = 0; index < sa.size(); index++) {
						ColumnSettings cs = (ColumnSettings) sa.get(index);
						if (cs != null) {
							if (colsettings == null) {
								colsettings = new HashMap();
							}
							colsettings.put(cs.getColumnName(), cs);
						}
					}
				} else if (storeddata instanceof HashMap) {
					colsettings = (HashMap) storeddata;
				}
			} catch (java.io.IOException e) {
				// just ignore
			}
			m_stored_settings = colsettings;
		}

		if (m_stored_settings != null) {
			return (ColumnSettings) m_stored_settings.get(colName);
		} else {
			return null;
		}
	}

	/**
	 * Loads any stored settings from the object store
	 * 
	 * @param tmd
	 *            the table metadata object for the table we are associated with
	 * @param presetColumns
	 *            an array of settings for each column passed in by the caller.
	 *            For example, this is used when we have queried a single table
	 *            using the select * menu item. In the query results window the
	 *            user can re-arrange/hide the columns. This defines the
	 *            presets.
	 */
	public void load(TableMetaData tmd, ColumnMetaData[] presetColumns) {
		if (tmd == null) {
            return;
        }

		ArrayList<ColumnSettings> col_settings = new ArrayList();

		if (presetColumns != null && presetColumns.length > 0) {
			// first convert the presets to an array of default column settings
			for (int index = 0; index < presetColumns.length; index++) {
				ColumnMetaData cmd = presetColumns[index];
				if (tmd.getColumn(cmd.getColumnName()) != null) {
					ColumnSettings sci = getStoredColumnSettings(cmd.getColumnName());
					if (sci == null) {
						sci = new ColumnSettings(cmd, true, new DefaultColumnHandler());
					}
					sci.setVisible(true);
					col_settings.add(sci);
				}
			}
		}

		if (col_settings.size() == 0) {
			for (int index = 0; index < tmd.getColumnCount(); index++) {
				ColumnMetaData cmd = tmd.getColumn(index);
				ColumnSettings setting = getStoredColumnSettings(cmd.getColumnName());
				if (setting == null) {
					setting = new ColumnSettings(cmd, true, new DefaultColumnHandler());
				}
				col_settings.add(setting);

				// the storeddata does contain the column, now make sure the
				// type has not changed
				ColumnMetaData scmd = setting.getColumnMetaData();
				if (cmd.getType() != scmd.getType()) {
					// the column is found, but the data type has changed. so,
					// we need to reset
					// this column to default
					col_settings.set(index, new ColumnSettings(cmd, true, new DefaultColumnHandler()));
				}
			}
		}

        col_settings.sort((ColumnSettings c1, ColumnSettings c2)->c1.getModelIndex() - c2.getModelIndex());
		// now iterate over the stored data and make sure that all columns in
		// the stored data are present in the table metadata
		Iterator iter = col_settings.iterator();
		while (iter.hasNext()) {
			ColumnSettings setting = (ColumnSettings) iter.next();
			if (tmd.getColumn(setting.getColumnName()) == null) {
				// the table does not contain the given column so remove from
				// stored data
				iter.remove();
			}
		}


		// stored


		for (int index = 0; index < col_settings.size(); index++) {
			if (com.jeta.abeille.database.postgres.PostgresObjectStore.isShowOID(m_connection, m_tmd.getTableId()) && index == 0) {
				ColumnMetaData oidcmd = new ColumnMetaData("oid", java.sql.Types.INTEGER, "INTEGER", 0,
						m_tmd.getTableId(), false);
				ColumnSettings defaultci = new ColumnSettings(oidcmd, true, new DefaultColumnHandler());
				addColumnSettings(defaultci);
			}
			ColumnSettings ci = (ColumnSettings) col_settings.get(index);
			addColumnSettings(ci);
		}
	}

	/**
	 * Resets the model to the default table settings
	 */
	public void reset() {
		removeAll();
		// @toto test this by changing table metadata against previously stored
		// information
		for (int index = 0; index < m_tmd.getColumnCount(); index++) {
			if (com.jeta.abeille.database.postgres.PostgresObjectStore.isShowOID(m_connection, m_tmd.getTableId())
					&& index == 0) {
				ColumnMetaData oidcmd = new ColumnMetaData("oid", java.sql.Types.INTEGER, "INTEGER", 0,
						m_tmd.getTableId(), false);
				ColumnSettings defaultci = new ColumnSettings(oidcmd, true, new DefaultColumnHandler());
				addColumnSettings(defaultci);
			}

			ColumnMetaData cmd = m_tmd.getColumn(index);
			ColumnSettings defaultci = new ColumnSettings(cmd, true, new DefaultColumnHandler());
			addColumnSettings(defaultci);
		}

		m_stored_settings = null;

	}

	/**
	 * Saves the settings to the persistent store.
	 */
	public void saveSettings() {
		ObjectStore os = m_connection.getObjectStore();
		String oskey = getPropertiesKey();

		ColumnSettings[] csa = getColumnSettings();
		if (csa != null) {
			HashMap settings = new HashMap();
			for (int index = 0; index < csa.length; index++) {
				ColumnSettings cs = csa[index];
				cs.setModelIndex(index);
				settings.put(cs.getColumnName(), cs);
			}
			try {
				os.store(oskey, settings);
			} catch (java.io.IOException ioe) {
				// just ignore
			}
		}
		m_stored_settings = null;
	}


	public String toString() {
	   return  "Table metadata " + m_stored_settings;
    }

}
