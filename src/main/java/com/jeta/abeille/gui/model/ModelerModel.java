package com.jeta.abeille.gui.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.lang.ref.WeakReference;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DbModelEvent;
import com.jeta.abeille.database.model.DbModelListener;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdComparator;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.components.SortedListModel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is used to display the set of tables that are currently in the
 * database as well as any tables that are currently being modeled but not yet
 * saved. This allows the user to see the new modeled tables in any drop down
 * combo boxes. It also acts as a central repository for newly modeled (but not
 * saved) tables. This allows us to have multiple views of a modeled table with
 * this as the common data.
 * 
 * @author Jeff Tassin
 */
public class ModelerModel implements TableSelectorModel, JETAExternalizable, DbModelListener {
	static final long serialVersionUID = -6397051609828344272L;

	public static int VERSION = 1;

	/** the database connection */
	private TSConnection m_connection;

	/**
	 * Date this model was created. Used for timing out model files for
	 * evaluations.
	 */
	private Calendar m_created_date;

	/**
	 * This is a hash of Schema (keys) to SortedListModels (values) for each
	 * catalog Each SortedListModel is a sorted list of tables for that schema
	 * This includes prototype tables as well.
	 */
	private transient TableCache m_tablecache = new TableCache();

	/**
	 * This is a hash of TableIds (keys) to unsaved TableMetaData objects
	 * (values)
	 */
	private HashMap m_prototypes = new HashMap();

	/** The list of globally defined links for this connection */
	private LinkedList m_userlinks = new LinkedList();

	/**
	 * This is a list of model listeners who want events when a table changes in
	 * the model
	 */
	private transient LinkedList m_listeners = new LinkedList();

	/**
	 * A list of default modelers for a given connection
	 */
	private static HashMap m_default_modelers = new HashMap();

	public static final String COMPONENT_ID = "jeta.ModelerModel";

	public ModelerModel() {
		m_created_date = Calendar.getInstance();
	}

	/**
	 * ctor
	 */
	public ModelerModel(TSConnection connection) {
		setConnection(connection);
		m_created_date = Calendar.getInstance();
	}

	/**
	 * Adds a globally defined link to this model
	 */
	public void addUserLink(Link link) {
		if (link != null && link.isUserDefined()) {
			if (!m_userlinks.contains(link)) {
				m_userlinks.add(link);
				fireEvent(new ModelerEvent(ModelerEvent.LINK_CREATED, link));
			} else {
				if (TSUtils.isDebug()) {
					System.out.println("Modeler already contains link");
					link.print();
					System.out.println();
				}
			}
		}
	}

	/**
	 * Adds a listener that wants events from this classes
	 */
	public void addListener(ModelerListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Adds a table prototype (i.e. and usaved table) to this model. This does
	 * not add anyting to the database. Any tables that are currently being
	 * modeled should be addeded here
	 */
	public boolean addTablePrototype(TableMetaData tmd) {
		boolean badded = false;
		TableId tableid = tmd.getTableId();
		if (!contains(tableid)) {
			SortedListModel model = getTables(tableid.getCatalog(), tableid.getSchema());
			if (model != null) {
				model.add(tableid);
			}
			m_prototypes.put(tableid, tmd);
			badded = true;
			fireEvent(new ModelerEvent(ModelerEvent.TABLE_CREATED, tableid));
		}
		return badded;
	}

	/**
	 * Clears our local cache of existing tables. We cache these tables for
	 * performance. This is because we need to include no only the existing
	 * tables but also the prototype type tables in any sets of tables we
	 * return.
	 */
	private void clearTableCache() {
		if (m_tablecache != null) {
			m_tablecache.clear();
		}
	}

	public Calendar getCreatedDate() {
		assert (m_created_date != null);
		return m_created_date;
	}

	/**
	 * @return creates a modeler instance associated with the given connection
	 */
	public static ModelerModel getDefaultInstance(TSConnection connection) {
		if (m_default_modelers == null)
			m_default_modelers = new HashMap();

		WeakReference wref = (WeakReference) m_default_modelers.get(connection);
		if (wref == null || wref.get() == null) {
			wref = new WeakReference(new ModelerModel(connection));
			m_default_modelers.put(connection, wref);
		}
		return (ModelerModel) wref.get();
	}

	/**
	 * @return true if this model contains the given tableid
	 */
	public boolean contains(TableId tableid) {
		return (getTable(tableid) != null);
	}

	/**
	 * Creates a new table name given the table id. Basically, this method
	 * creates the new table name as 'copy of tablename'. It determines if there
	 * are any other tables with that name, if so, it appends a count to the
	 * 'copy of tablename'.
	 * 
	 * @return the new table name. Null is returned if the table cannot be
	 *         created - this would be a really weird failure though
	 */
	String createTableName(TableId tableid) {
		for (int index = 0; index < 1000; index++) {
			String tablename = null;
			if (index == 0)
				tablename = I18N.format("copy_of_tablename_1", tableid.getTableName());
			else
				tablename = I18N.format("copy_of_tablename_2", new Integer(index), tableid.getTableName());

			TableId newid = (TableId) tableid.changeName(tablename);
			if (!contains(newid)) {
				return tablename;
			}
		}
		return null;
	}

	/**
	 * Called when a user wants to drop a table from the modeler. Removes a
	 * table from this selector as well as any reference foreign keys. Note: An
	 * event is NOT fired here.
	 */
	public void dropPrototypeSilent(TableId tableId) {
		removePrototypeSilent(tableId);
		/**
		 * remove any foreign keys in any other prototypes that have links to
		 * this table
		 */
		Iterator iter = m_prototypes.values().iterator();
		while (iter.hasNext()) {
			TableMetaData tmd = (TableMetaData) iter.next();
			tmd.removeForeignKey(tableId);
		}
	}

	/**
	 * Called when we get an event from the DbModel
	 */
	public void eventFired(DbModelEvent evt) {
		if (evt.getID() == DbModelEvent.TABLE_CREATED) {
			clearTableCache();
		} else if (evt.getID() == DbModelEvent.VIEW_CREATED) {
			clearTableCache();
		} else if (evt.getID() == DbModelEvent.TABLE_RENAMED) {
			clearTableCache();
			tableRenamed((TableId) evt.getParameter(1), evt.getTableId());
		} else if (evt.getID() == DbModelEvent.TABLE_DELETED) {
			clearTableCache();
			tableDeleted(evt.getTableId());
		} else if (evt.getID() == DbModelEvent.TABLE_CHANGED) {
			if (TSUtils.isDebug()) {
				// System.out.println(
				// "ModelerModel got DbModelEvent.TABLE_CHANGED: " +
				// evt.getTableId() );
			}
			fireEvent(new ModelerEvent(ModelerEvent.TABLE_CHANGED, evt.getTableId()));
		} else if (evt.getID() == DbModelEvent.SCHEMA_LOADED) {
			// the model has been reloaded, so let's remove any cached tables
			clearTableCache();
			// TSUtils.printMessage( "ModelerModel.eventFired MODEL_LOADED: " +
			// evt.getCatalog() );
			fireEvent(new ModelerEvent(ModelerEvent.CATALOG_CHANGED, evt.getCatalog()));
		}
	}

	/**
	 * Override finalize so we can remove any listeners from the DbModel
	 */
	public void finalize() throws Throwable {
		m_connection.removeModelListener(this);
		super.finalize();
	}

	/**
	 * Notifies all ModelerListeners that an event occurred
	 */
	protected void fireEvent(ModelerEvent evt) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			ModelerListener listener = (ModelerListener) iter.next();
			listener.eventFired(evt);
		}
	}

	/**
    */
	public Collection getCatalogs() {
		return m_connection.getCatalogs();
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the default schema
	 */
	public Schema getCurrentSchema(Catalog cat) {
		return m_connection.getCurrentSchema(cat);
	}

	/**
	 * @return the underlying database model
	 */
	DbModel getDbModel(Catalog cat) {
		return m_connection.getModel(cat);
	}

	/**
	 * @return the current catalog
	 */
	public Catalog getCurrentCatalog() {
		return m_connection.getCurrentCatalog();
	}

	/**
	 * @return the default catalog
	 */
	public Catalog getDefaultCatalog() {
		return m_connection.getDefaultCatalog();
	}

	/**
	 * @return a table metadata object for the given table name only if it is a
	 *         prototype table
	 */
	public TableMetaData getPrototype(TableId tableid) {
		return (TableMetaData) m_prototypes.get(tableid);
	}

	/**
	 * @return the set of prototype tables (TableIds) in this modeler
	 */
	public Collection getPrototypes() {
		return m_prototypes.keySet();
	}

	/**
	 * TableSelectorModel implementation
	 * 
	 * @return the collection of available schemas in this model.
	 */
	public Collection getSchemas(Catalog catalog) {
		return getDbModel(catalog).getSchemas();
	}

	/**
	 * TableSelectorModel implementation
	 * 
	 * @return the selected tables for the given schema
	 */
	public SortedListModel getTables(Catalog catalog, Schema schema) {
		SortedListModel model = (SortedListModel) m_tablecache.get(catalog, schema);
		if (model == null) {
			TreeSet set = getDbModel(catalog).getTables(schema);
			if (set == null) {
				// this could happen if the schema is invalid
				// set = new TreeSet();
				set = new TreeSet(new TableIdComparator(m_connection, false));
			} else {
				// we clone here becase we well be adding unsaved tables to the
				// set
				TreeSet cloneset = new TreeSet(new TableIdComparator(m_connection, false));
				cloneset.addAll(set);
				set = cloneset;
				// set = (TreeSet)set.clone();
			}
			model = new SortedListModel(set);

			// add unsaved tables to the model
			Set tableids = m_prototypes.keySet();
			Iterator iter = tableids.iterator();
			while (iter.hasNext()) {
				TableId tableid = (TableId) iter.next();
				if (catalog.equals(tableid.getCatalog()) && schema.equals(tableid.getSchema())) {
					model.add(tableid);
				}
			}

			m_tablecache.put(catalog, schema, model);
		}

		return model;
	}

	/**
	 * @return a table metadata object for the given table name
	 */
	public TableMetaData getTable(TableId tableid) {
		if (tableid == null)
			return null;

		TableMetaData tmd = (TableMetaData) m_prototypes.get(tableid);

		if (tmd == null) {
			tmd = getDbModel(tableid.getCatalog()).getTable(tableid);
		}
		return tmd;
	}

	/**
	 * When a table is first loaded by the system as startup, only the table
	 * columns are loaded and not the foreign keys. This is done to speed up
	 * loading. So, we need to make sure the foreign keys are loaded by calling
	 * getTableEx
	 * 
	 * @param tableid
	 *            the id of the table to load. If the table is a prototype, it
	 *            is loaded directly from this model. Otherwise, the call is
	 *            passed to the DbModel
	 * @param flag
	 *            a flag (defined in DbModel) that indicates which table
	 *            information should be loaded if not present (e.g. foreign
	 *            keys).
	 */
	public TableMetaData getTableEx(TableId tableid, int flag) {
		if (tableid == null)
			return null;

		TableMetaData tmd = (TableMetaData) m_prototypes.get(tableid);

		if (tmd == null) {
			tmd = getDbModel(tableid.getCatalog()).getTableEx(tableid, flag);
		}
		return tmd;

	}

	/**
	 * @return the user defined global links (Link Objects)
	 */
	public Collection getUserDefinedLinks() {
		return m_userlinks;
	}

	/**
	 * @return true if the given table is has not been saved yet
	 */
	public boolean isPrototype(TableId tableId) {
		return (m_prototypes.get(tableId) != null);
	}

	/**
	 * Dump the contents of the model to the console
	 */
	public void print() {
		TSUtils.printMessage(" -------------------- ModelerModel.print ------------------- ");
		Iterator iter = m_prototypes.keySet().iterator();
		while (iter.hasNext()) {
			TableId protoid = (TableId) iter.next();
			TableMetaData tmd = (TableMetaData) m_prototypes.get(protoid);
			if (tmd == null) {
				TSUtils.printMessage("Modeler prototypes have an error.  Null table meta data for key: "
						+ protoid.getFullyQualifiedName());
			} else {
				if (protoid.equals(tmd.getTableId())) {
					TSUtils.printMessage("Modeler prototype table ok: " + protoid.getFullyQualifiedName());
				} else {
					TSUtils.printMessage("Modeler prototypes have an error.  Table Metadata id does not equal proto id: "
							+ protoid.getFullyQualifiedName()
							+ "   tmd.id: "
							+ tmd.getTableId().getFullyQualifiedName());
				}
			}
		}
		TSUtils.printMessage(" -------------------- ModelerModel.print ------------------- ");
	}

	/**
	 * Override serialize so we can initialize properly
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		m_tablecache = new TableCache();
		m_listeners = new LinkedList();
		m_connection.addModelListener(this);

		if (m_userlinks == null)
			m_userlinks = new LinkedList();
	}

	/**
	 * Removes a globally defined link from this model
	 */
	public void removeUserLink(Link link) {
		m_userlinks.remove(link);
		fireEvent(new ModelerEvent(ModelerEvent.LINK_DELETED, link));
	}

	/**
	 * Removes a listener that was previously added
	 */
	public void removeListener(ModelerListener listener) {
		m_listeners.remove(listener);
	}

	/**
	 * Removes a table from this selector. This does not add anyting to the
	 * database. Any tables that are currently being modeled should be addeded
	 * here. Note: An event is NOT fired here.
	 */
	public void removePrototypeSilent(TableId tableId) {
		if (TSUtils.isDebug()) {
			System.out.println("ModelerModel.removePrototypeSilent: " + tableId);
		}
		m_prototypes.remove(tableId);

		SortedListModel model = getTables(tableId.getCatalog(), tableId.getSchema());
		model.removeElement(tableId);
		clearTableCache();
	}

	/**
	 * Removes a table from this selector. This does not add anyting to the
	 * database. Any tables that are currently being modeled should be addeded
	 * here. Note: A remove event IS fired here.
	 */
	public void removePrototype(TableId tableId) {
		dropPrototypeSilent(tableId);
		tableDeleted(tableId);
	}

	/**
	 * Saves the modeler to the object store
	 */
	public void save(boolean flush) {
		ObjectStore os = (ObjectStore) m_connection.getObjectStore();
		try {
			os.store(COMPONENT_ID, this);
			if (flush)
				os.flush(COMPONENT_ID);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * The given table was changed (but still has same name/schema). This method
	 * fires a ModelerEvent
	 * 
	 * @param tmd
	 *            the new table metadata
	 */
	public void tableChanged(TableMetaData tmd) {
		TableId tableid = tmd.getTableId();
		if (isPrototype(tableid)) {
			// simply update the metadata in the prototype
			m_prototypes.put(tableid, tmd);
		}
		// now fire the event
		fireEvent(new ModelerEvent(ModelerEvent.TABLE_CHANGED, tableid));
	}

	/**
	 * The given table was deleted/dropped from the database This method fires a
	 * ModelerEvent
	 * 
	 * @param tmd
	 *            the new table metadata
	 */
	private void tableDeleted(TableId tableId) {
		// simply fire the event
		clearTableCache();
		fireEvent(new ModelerEvent(ModelerEvent.TABLE_DELETED, tableId));
	}

	/**
	 * The given table was renamed. Change in the model and notify any listeners
	 * This method fires a ModelerEvent
	 * 
	 * @param newId
	 *            the new table id
	 * @param metadata
	 *            the existing table metadata
	 */
	public Collection tableRenamed(TableId newId, TableId oldId) {
		clearTableCache();
		// System.out.println( "ModelerModel.tableRenamed  newId = " +
		// newId.getFullyQualifiedName() + "   oldId = " +
		// oldId.getFullyQualifiedName() );

		// we need this to prevent concurrent modification of widgets in model
		// while iterating
		LinkedList updates = new LinkedList();

		Collection tables = m_prototypes.values();
		Iterator iter = tables.iterator();
		while (iter.hasNext()) {
			TableMetaData tmd = (TableMetaData) iter.next();
			Collection fkeys = tmd.getForeignKeys();
			Iterator fiter = fkeys.iterator();
			while (fiter.hasNext()) {
				DbForeignKey fkey = (DbForeignKey) fiter.next();
				if (oldId.equals(fkey.getReferenceTableId())) {
					fkey.setReferenceTableId(newId);
					updates.add(tmd.getTableId());
				}
			}
		}

		TableMetaData tmd = (TableMetaData) m_prototypes.remove(oldId);
		if (tmd != null) {
			tmd.setTableId(newId);
			m_prototypes.put(newId, tmd);
		}

		fireEvent(new ModelerEvent(newId, oldId));
		return updates;
	}

	/**
	 * This is a map of catalog objects to schemas -> sortedlistmodels catalog1
	 * --> HashMap (schema-->sortedlistmodel) catalog2 --> HashMap
	 * (schema-->sortedlistmodel) ... catalogN --> HashMap
	 * (schema-->sortedlistmodel)
	 * 
	 */
	static class TableCache {
		/**
		 * Map if catalog objects ( keys ) to HashMap objects (values). The
		 * HashMap objects are a map of schema objects (keys ) to
		 * SortedListModels (values )
		 */
		private HashMap m_catalogs = new HashMap();

		public SortedListModel get(Catalog catalog, Schema schema) {
			HashMap shash = (HashMap) m_catalogs.get(catalog);
			if (shash == null) {
				return null;
			} else {
				return (SortedListModel) shash.get(schema);
			}

		}

		public void put(Catalog catalog, Schema schema, SortedListModel model) {
			HashMap shash = (HashMap) m_catalogs.get(catalog);
			if (shash == null) {
				shash = new HashMap();
				m_catalogs.put(catalog, shash);
			}

			shash.put(schema, model);
		}

		public void clear() {
			m_catalogs.clear();
		}
	}

	public void setConnection(TSConnection tsconn) {
		m_connection = tsconn;
		m_connection.removeModelListener(this);
		/**
		 * you must do this here for the default case when the modeler has not
		 * be read from persitent store
		 */
		m_connection.addModelListener(this);

		if (m_default_modelers == null)
			m_default_modelers = new HashMap();

		WeakReference wref = (WeakReference) m_default_modelers.get(tsconn);
		if (wref == null || wref.get() == null)
			m_default_modelers.put(tsconn, new WeakReference(this));

	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_prototypes = (HashMap) in.readObject();
		m_userlinks = (LinkedList) in.readObject();
		m_created_date = (Calendar) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_prototypes);
		out.writeObject(m_userlinks);
		out.writeObject(m_created_date);
	}

}
