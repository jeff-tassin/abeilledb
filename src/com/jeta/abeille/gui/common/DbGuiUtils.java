package com.jeta.abeille.gui.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.CatalogComparator;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.ColumnMetaDataComparator;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.DataTypeInfoComparator;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.SchemaComparator;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.gui.components.PopupList;
import com.jeta.foundation.gui.components.SortedListModel;
import com.jeta.foundation.gui.components.TSComboBox;

import com.jeta.foundation.utils.TSUtils;

/**
 * Some helper methods that are frequently used
 * 
 * @author Jeff Tassin
 */
public class DbGuiUtils {

	/**
	 * Creates a combo box with the standard SQL datatypes as well as a renderer
	 * for those data types
	 */
	public static TSComboBox createDataTypeCombo(TSConnection connection) {
		TSComboBox datatypecombo = new TSComboBox();
		PopupList list = datatypecombo.getPopupList();
		list.setRenderer(new MetaDataPopupRenderer(connection));
		SortedListModel listmodel = new SortedListModel();
		listmodel.setComparator(new DataTypeInfoComparator());

		TSDatabase dbimpl = (TSDatabase) connection.getImplementation(TSDatabase.COMPONENT_ID);
		Collection dtypes = dbimpl.getSupportedTypes();
		Iterator iter = dtypes.iterator();
		while (iter.hasNext()) {
			DataTypeInfo di = (DataTypeInfo) iter.next();
			listmodel.add(di);
		}
		list.setModel(listmodel);
		datatypecombo.setSelectedItem(DbUtils.getJDBCTypeName(java.sql.Types.VARCHAR));
		return datatypecombo;
	}

	/**
	 * Creates a combo box filled with the catalogs from the current connection
	 * 
	 * @param defaultCatalog
	 *            the default catalog
	 * @param catalogs
	 *            a collection of opened catalogs in the connection
	 */
	public static TSComboBox createCatalogsCombo(Catalog defaultCatalog, Collection catalogs) {
		TSComboBox catalogscombo = new TSComboBox();
		PopupList slist = catalogscombo.getPopupList();
		slist.setRenderer(new MetaDataPopupRenderer(null));
		SortedListModel listmodel = slist.getModel();
		listmodel.setComparator(new CatalogComparator());

		Iterator iter = catalogs.iterator();
		while (iter.hasNext()) {
			Catalog catalog = (Catalog) iter.next();
			listmodel.add(catalog);
		}

		if (defaultCatalog != null)
			catalogscombo.setSelectedItem(defaultCatalog);

		if (catalogs.size() == 1) {
			catalogscombo.setEnabled(false);
		}
		return catalogscombo;
	}

	/**
	 * Creates a combo box filled with the catalogs from the current connection
	 * 
	 * @param conn
	 *            the connection whose schemas we wish to show in the combo
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public static TSComboBox createCatalogsCombo(TSConnection conn) {
		return DbGuiUtils.createCatalogsCombo(conn.getDefaultCatalog(), conn.getCatalogs());
	}

	/**
	 * Creates a combo box filled with schemas from the current connection
	 * 
	 * @param conn
	 *            the connection whose schemas we wish to show in the combo
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public static TSComboBox createSchemasCombo(TSConnection conn, Catalog catalog) {
		TSComboBox schemascombo = new TSComboBox();
		PopupList slist = schemascombo.getPopupList();
		slist.setRenderer(MetaDataPopupRenderer.createInstance(conn));
		SortedListModel listmodel = slist.getModel();
		listmodel.setComparator(new SchemaComparator());

		Collection schemas = conn.getModel(catalog).getSchemas();
		Iterator iter = schemas.iterator();
		while (iter.hasNext()) {
			Schema sch = (Schema) iter.next();
			listmodel.add(sch);
		}

		Schema currentschema = conn.getCurrentSchema();
		if (currentschema != null)
			schemascombo.setSelectedItem(currentschema);

		if (schemas.size() == 1) {
			schemascombo.setEnabled(false);
			schemascombo.setSelectedItem(Schema.VIRTUAL_SCHEMA);
		}

		return schemascombo;
	}

	public static ImageIcon getIcon(ColumnMetaData cmd, TSConnection tsconn) {
		ImageIcon result = DbUtils.getIcon(0);
		DataTypeInfo typeinfo = DbUtils.getDataTypeInfo(tsconn, cmd.getTypeName(), false);
		if (cmd.isAutoIncrement()) {
			result = DbUtils.AUTO_INCREMENT_ICON;
		} else {
			int datatype = cmd.getType();
			if (typeinfo == null && datatype == 0) {
				/** get the general icon because the type is unknown */
				result = DbUtils.getIcon(0);
			} else {
				if (DbUtils.isJDBCType(datatype)) {
					result = DbUtils.getIcon(datatype);
				} else {
					if (typeinfo == null) {
						result = DbUtils.getIcon(0);
					} else if (datatype == 0) {
						result = typeinfo.getIcon();
					} else {
						result = DbUtils.getIcon(0);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Loads a combo box with the columns in a given table
	 */
	public static void loadColumnsCombo(TSConnection conn, TableId tableId, TSComboBox cbox) {
		try {

			PopupList slist = cbox.getPopupList();
			SortedListModel listmodel = slist.getModel();
			slist.setRenderer(MetaDataPopupRenderer.createInstance(conn));
			listmodel.setComparator(new ColumnMetaDataComparator());
			listmodel.clear();
			TableMetaData tmd = conn.getTable(tableId);
			if (tmd != null) {
				Collection columns = tmd.getColumns();
				Iterator iter = columns.iterator();
				while (iter.hasNext()) {
					ColumnMetaData cmd = (ColumnMetaData) iter.next();
					listmodel.add(cmd);
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Loades a combo box filled with the tables for a given schema from the
	 * current connection
	 */
	public static void loadTablesCombo(TSConnection conn, Catalog catalog, Schema schema, TSComboBox cbox) {
		TreeSet treeset = conn.getModel(catalog).getTables(schema);
		if (treeset != null) {
			SortedListModel listmodel = new SortedListModel(treeset);
			PopupList list = cbox.getPopupList();
			list.setModel(listmodel);
		}
	}

}
