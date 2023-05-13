package com.jeta.abeille.gui.sql;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.abeille.gui.update.ColumnSettings;
import com.jeta.abeille.gui.update.DefaultColumnHandler;
import com.jeta.abeille.gui.update.InstanceMetaData;
import com.jeta.abeille.gui.update.TableInstanceMetaData;

import com.jeta.foundation.i18n.I18N;

/**
 * This class defines the meta data used for the InstanceView for a query result
 * from a SQL command.
 * 
 * @author Jeff Tassin
 */
public class QueryInstanceMetaData extends InstanceMetaData {
	private TSConnection m_tsconn;

	private ResultSetReference m_resultref;

	private JTable m_table;

	private DefaultLinkModel m_linkmodel = new DefaultLinkModel();

	/**
	 * ctor
	 */
	public QueryInstanceMetaData(JTable table, TSConnection tsconn, ResultSetReference ref, SQLSettings settings)
			throws SQLException {
		m_table = table;
		m_tsconn = tsconn;
		m_resultref = ref;
		load(settings);
	}

	/**
	 * @return an empty link model for now
	 */
	public LinkModel getLinkModel() {
		return m_linkmodel;
	}

	/**
	 * @return a unique identifier for this model. If this is a table model,
	 *         then the UID is basically the schema.tablename. If this is a
	 *         query result, then the UID is the query string. If this is from a
	 *         saved query, then this is the UID of the saved query
	 */
	public String getUID() {
		return "test";
	}

	/**
	 * @return true if the given column name is a foreign key
	 */
	public boolean isLink(ColumnMetaData cmd) {
		boolean bresult = false;

		TableId tableid = cmd.getParentTableId();
		if (tableid != null) {
			String tablename = tableid.getTableName();
			if (tablename != null && tablename.length() > 0) {
				TableMetaData tmd = m_tsconn.getTable(tableid);
				if (tmd != null) {
					bresult = (tmd.getColumn(cmd.getColumnName()) != null);
				}
			}
		}

		return bresult;
	}

	/**
	 * @return true if the given column name is a primary key
	 */
	public boolean isPrimaryKey(ColumnMetaData cmd) {
		return false;
	}

	/**
	 * Loads from the result set metadata
	 */
	private void load(SQLSettings settings) throws SQLException {
		ResultSetMetaData metadata = m_resultref.getMetaData();
		ColumnMetaData[] cols = DbUtils.createColumnMetaData(m_tsconn, null, metadata);

		for (int index = 0; index < cols.length; index++) {
			ColumnMetaData cmd = cols[index];
			// for a query the destination table in the link is undefined, so
			// let's just set to the same table as the source
			Link link = new Link(cmd.getParentTableId(), cmd.getColumnName(), cmd.getParentTableId(),
					cmd.getColumnName());
			m_linkmodel.addLink(link);
		}

		ArrayList storeddata = (ArrayList) settings.getInstanceViewSettings();

		// put the stored column settings in a hash map keyed to the model index
		HashMap stored_map = new HashMap();
		if (storeddata != null) {
			for (int index = 0; index < storeddata.size(); index++) {
				ColumnSettings ci = (ColumnSettings) storeddata.get(index);
				stored_map.put(new Integer(ci.getModelIndex()), ci);
			}
		}

		/**
		 * a map of TableInstanceMetaData objects. The query can have columns
		 * from multiple tables, so when building the form view, we want to load
		 * the settings for that table. So, we cache the settings here
		 */
		HashMap timd_cache = new HashMap();

		TableColumnModel colmodel = m_table.getColumnModel();
		for (int index = 0; index < colmodel.getColumnCount(); index++) {
			TableColumn tablecol = colmodel.getColumn(index);
			int modelindex = tablecol.getModelIndex();
			if (modelindex >= 0 && modelindex < cols.length) {
				ColumnMetaData cmd = cols[modelindex];
				ColumnSettings ci = null;

				/**
				 * first, try to get the column settings that were set for the
				 * table (in the Table Form View)
				 */
				TableId parent_id = cmd.getParentTableId();
				if (parent_id != null) {
					TableInstanceMetaData timd = (TableInstanceMetaData) timd_cache.get(parent_id);
					if (timd == null) {
						timd = new TableInstanceMetaData(m_tsconn, parent_id);
						timd_cache.put(parent_id, timd);
					}
					ColumnSettings scs = timd.getStoredColumnSettings(cmd.getColumnName());
					System.out.println("QueryInstanceMetaData  column: " + cmd + "  parent_id = " + parent_id
							+ "   settings: " + scs);
					if (scs != null && (cmd.getType() == scs.getType())) {
						ci = scs;
						ci.setModelIndex(modelindex);
					}
				}

				if (ci == null) {
					/**
					 * check if the stored column settings has the same column
					 * name and model index as the table model columns
					 */
					ci = (ColumnSettings) stored_map.get(new Integer(modelindex));
					if (ci != null) {
						ColumnMetaData stored_cmd = ci.getColumnMetaData();
						if (!I18N.equals(cmd.getColumnName(), stored_cmd.getColumnName())
								|| !(cmd.getType() == stored_cmd.getType())) {
							ci = null;
						}
					}

					if (ci == null) {
						ci = new ColumnSettings(cmd, true, new DefaultColumnHandler());
						ci.setModelIndex(modelindex);
					}
				}

				String alias = cmd.getAlias();
				if (alias != null && alias.trim().length() > 0)
					ci.setDisplayName(alias);

				addColumnSettings(ci);
			}
		}
	}

	/**
	 * Resets to the defaults
	 */
	public void reset() {

	}

	/**
	 * Saves the settings to the persistent store.
	 */
	public void saveSettings(SQLSettings settings) {
		ArrayList tdata = new ArrayList(Arrays.asList(getColumnSettings()));
		settings.setInstanceViewSettings(tdata);
	}

}
