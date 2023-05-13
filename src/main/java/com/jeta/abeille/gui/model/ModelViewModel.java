package com.jeta.abeille.gui.model;

import java.awt.event.MouseEvent;

import java.awt.Dimension;
import java.awt.Rectangle;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

import java.lang.ref.WeakReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.MultiColumnLink;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.componentmgr.TSNotifier;
import com.jeta.foundation.utils.EmptyCollection;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the a model for the 'canvas' that graphically shows table properties
 * and their relationships to other tables. Only the tables that a visible on
 * the canvas are in the model.
 * 
 * @author Jeff Tassin
 */
public class ModelViewModel implements JETAExternalizable {
	static final long serialVersionUID = 4246191655060579270L;

	public static int VERSION = 1;

	public static final String COMPONENT_ID = "model.view.model.id";

	/**
	 * The name of this view
	 */
	private String m_viewName;

	/**
	 * This is a hashmap of TableWidget objects keyed on TableId
	 */
	private HashMap m_tablewidgets = new HashMap();

	/**
	 * This is the canvas size of the view
	 */
	private Dimension m_canvasSize;

	/**
	 * The LinkWidgetModel
	 */
	private LinkWidgetModel m_linkmodel;

	/**
	 * An array of ModelViewModelListeners that want events from this object
	 */
	private transient LinkedList m_listeners = new LinkedList();

	/** flag indicating if we've been initialized */
	private transient boolean m_initialized = false;

	/**
	 * The underlying modeler
	 */
	private transient ModelerModel m_modeler;

	private transient TSConnection m_tsconn;

	/**
	 * Default ctor for serialization
	 */
	public ModelViewModel() {
		// default canvas size
		m_canvasSize = new Dimension(1600, 1600);
		m_linkmodel = new LinkWidgetModel();
	}

	/**
	 * ctor
	 */
	public ModelViewModel(String viewName, ModelerModel modeler, TSConnection tsconn) {
		this();
		m_viewName = viewName;
		m_modeler = modeler;
		m_tsconn = tsconn;
		assert (m_linkmodel != null);
	}

	/**
	 * Adds a listener to this model
	 */
	public void addListener(ModelViewModelListener listener) {
		if (listener != null)
			m_listeners.add(new WeakReference(listener));
	}

	/**
	 * Creates a table widget for the given id and adds it to the model. Any
	 * views are notified of the added table
	 */
	public TableWidget addTable(TableId tableId, int x, int y) {
		TableWidgetModel model = new TableWidgetModel(getModeler(), tableId);
		TableWidget widget = new TableWidget(model);
		widget.setLocation(x, y);
		addWidget(widget);
		return widget;
	}

	/**
	 * Creates a table widget for the given id and adds it to the model. Any
	 * views are notified of the added table
	 */
	public TableWidget addTable(TableId tableId, Rectangle rect) {
		TableWidgetModel model = new TableWidgetModel(getModeler(), tableId);
		TableWidget widget = new TableWidget(model);
		widget.setBounds(rect);
		widget.setLocation(rect.x, rect.y);
		addWidget(widget);
		return widget;
	}

	/**
	 * Adds a TableWidget for the given table to this view at the given
	 * coordinates
	 * 
	 * @param w
	 *            the table widget object to create a representation for on the
	 *            canvas
	 * @param x
	 *            the x coordinate on the canvas to place the widget
	 * @param y
	 *            the y coordinate on the canvas to place the widget
	 * @return the link widgets (LinkWidget objects) that were automatically
	 *         created as a result of the table being added to the model
	 */
	private Collection addWidget(TableWidget w) {

		LinkedList results = new LinkedList();
		TableId tableid = w.getTableId();
		assert (tableid != null);
		// TableMetaData tmd = w.getTableMetaData();
		if (!m_tablewidgets.containsKey(tableid)) {
			m_tablewidgets.put(tableid, w);
			results.addAll(createIncomingLinkWidgets(w));
			results.addAll(createOutgoingLinkWidgets(w));
			// System.out.println( "ModelViewModel.addWidget 2" );
			fireEvent(new ModelViewModelEvent(ModelViewModelEvent.TABLE_ADDED, tableid));
		} else {
			System.out.println(">>>>>> ModelViewModel  already contains table: " + tableid);
		}
		return results;
	}

	/**
	 * Changes the name of a table.
	 * 
	 * @param newId
	 *            the new table id
	 * @param oldId
	 *            the old table id
	 */
	void changeTableName(TableId newId, TableId oldId) {
		TableWidget w = (TableWidget) m_tablewidgets.get(oldId);
		if (w != null) {
			TableMetaData tmd = w.getTableMetaData();
			tmd.setTableId(newId);
			m_tablewidgets.remove(oldId);
			m_tablewidgets.put(newId, w);
		}
	}

	/**
	 * @return true if this model contains the given table
	 */
	public boolean contains(TableId tableid) {
		return m_tablewidgets.containsKey(tableid);
	}

	/**
	 * Creates a LinkWidget for the given link to the view. If the link widget
	 * is successfully created, it is returned, otherwise, null is returned.
	 * This method will fail if either of the source or destination tables is
	 * not in the view
	 */
	public LinkWidget createLinkWidget(Link link) {
		return m_linkmodel.createLinkWidget(this, link);
	}

	/**
	 * Creates the link widgets for incoming links for the given table.
	 * Basically, this iterates over all the foreign keys for the given table
	 * and creates a link if the referenced table exists. Note: the table must
	 * already be added to this model for the call to succeed.
	 * 
	 * @param w
	 *            the table widget whose links to other tables we wish to create
	 * @return the widgets created for the given table
	 */
	Collection createIncomingLinkWidgets(TableWidget w) {
		TableId createtableid = w.getTableId();

		LinkedList results = new LinkedList();
		TableMetaData createtmd = w.getTableMetaData();
		if (createtmd != null) {
			Collection fkeys = createtmd.getForeignKeys();
			Iterator iter = fkeys.iterator();
			// first create links for this table's foreign keys
			while (iter.hasNext()) {
				DbForeignKey fkey = (DbForeignKey) iter.next();

				TableWidget pkw = getTableWidget(fkey.getReferenceTableId());
				if (pkw != null) {
					Link link = new MultiColumnLink(fkey);
					LinkWidget lw = m_linkmodel.createLinkWidget(this, link);
					if (lw != null)
						results.add(lw);
				}
			}

			try {
				Collection globallinks = getUserDefinedLinks();
				Iterator gliter = globallinks.iterator();
				while (gliter.hasNext()) {
					Link link = (Link) gliter.next();
					if (createtableid.equals(link.getDestinationTableId())) {
						TableWidget pkw = getTableWidget(link.getSourceTableId());
						if (pkw != null) {
							LinkWidget lw = m_linkmodel.createLinkWidget(this, link);

							if (lw != null) {
								results.add(lw);

								/**
								 * this is needed for the query builder so
								 * custom links can be imported from the model
								 * view when pasting
								 */
								ModelerModel modeler = getModeler();
								if (modeler != null && link.isUserDefined())
									modeler.addUserLink(link);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return results;
	}

	/**
	 * Creates the link widgets for outgoing links for the given table. This
	 * iterates over all tables in the view and finds foreign keys in those
	 * tables that reference the given table. Notes: the table must already be
	 * added to this model for the call to succeed
	 * 
	 * @param w
	 *            the table widget whose outgoing links to other tables we wish
	 *            to create
	 * @return the link widgets created for the given table
	 */
	Collection createOutgoingLinkWidgets(TableWidget w) {
		// now we need to find all tables on the view that have foreign keys to
		// this table
		LinkedList results = new LinkedList();
		TableId createid = w.getTableId();
		TableMetaData createtmd = w.getTableMetaData();
		Collection tables = getTableWidgets();
		Iterator iter = tables.iterator();
		while (iter.hasNext()) {
			TableWidget tw = (TableWidget) iter.next();
			TableId id = tw.getTableId();
			if (!id.equals(createid)) {
				TableMetaData tmd = tw.getTableMetaData();
				if (tmd != null) {
					Collection fkeys = tmd.getForeignKeys();
					Iterator fiter = fkeys.iterator();
					// first create links for this table's foreign keys
					while (fiter.hasNext()) {
						DbForeignKey fkey = (DbForeignKey) fiter.next();
						if (fkey.getReferenceTableId().equals(createid)) {
							Link link = new MultiColumnLink(fkey);
							LinkWidget lw = m_linkmodel.createLinkWidget(this, link);
							if (lw != null)
								results.add(lw);
						}
					}
				}
			}
		}

		try {
			Collection globallinks = getUserDefinedLinks();
			Iterator gliter = globallinks.iterator();
			while (gliter.hasNext()) {
				Link link = (Link) gliter.next();
				if (createid.equals(link.getSourceTableId())) {
					TableWidget pkw = getTableWidget(link.getDestinationTableId());
					if (pkw != null) {
						LinkWidget lw = m_linkmodel.createLinkWidget(this, link);
						if (lw != null) {
							results.add(lw);
							/**
							 * this is needed for the query builder so custom
							 * links can be imported from the model view when
							 * pasting
							 */
							ModelerModel modeler = getModeler();
							if (modeler != null && link.isUserDefined())
								modeler.addUserLink(link);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return results;
	}

	/**
	 * Notifies all ModelViewModelListeners that an event occurred
	 */
	protected void fireEvent(ModelViewModelEvent evt) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			WeakReference wref = (WeakReference) iter.next();
			ModelViewModelListener listener = (ModelViewModelListener) wref.get();
			if (listener == null) {
				iter.remove();
			} else {
				listener.eventFired(evt);
			}
		}
	}

	protected Collection getUserDefinedLinks() {
		ModelerModel modeler = getModeler();
		if (modeler != null) {
			return modeler.getUserDefinedLinks();
		} else
			return EmptyCollection.getInstance();
	}

	/**
	 * @return the size of the canvas
	 */
	public Dimension getCanvasSize() {
		return m_canvasSize;
	}

	/**
	 * @return the database connection for this model
	 */
	public TSConnection getConnection() {
		return m_tsconn;
	}

	/**
	 * @return the incoming links (Link objects) for a given table This includes
	 *         links defined by foreign keys as well as user defined query links
	 */
	public Collection getInLinks(TableId tableid) {
		return m_linkmodel.getInLinks(tableid);
	}

	/**
	 * @return the outgoing links for a given table. This includes links defined
	 *         by foreign keys as well as user defined query links
	 */
	public Collection getOutLinks(TableId tableid) {
		return m_linkmodel.getOutLinks(tableid);
	}

	/**
	 * @return the model that contains all the links for this view. Note that
	 *         this method will decompose MultiColumnLinks into their individual
	 *         link components. So, a foreign key that has multiple columns,
	 *         will end up with multiple links corresponding to the number of
	 *         columsn in the ForeignKey.
	 */
	public LinkModel getLinkModel() {
		DefaultLinkModel model = new DefaultLinkModel();
		Collection linkwidgets = getLinkWidgets();
		Iterator iter = linkwidgets.iterator();
		while (iter.hasNext()) {
			LinkWidget lw = (LinkWidget) iter.next();
			Link link = lw.getLink();
			if (link instanceof MultiColumnLink) {
				Collection c = ((MultiColumnLink) link).getLinks();
				Iterator liter = c.iterator();
				while (liter.hasNext()) {
					link = (Link) liter.next();
					model.addLink(link);
				}
			} else {
				model.addLink(link);
			}
		}

		// model.print();
		return model;
	}

	/**
	 * @return the collection of link widgets in this view
	 */
	public LinkedList getLinkWidgets() {
		return m_linkmodel.getLinkWidgets();
	}

	/**
	 * @return a link model that contains only links that are given in the
	 *         collection of tables.
	 */
	public LinkModel getSpecifiedLinkModel(Collection tables) {
		TreeSet tset = new TreeSet(tables);
		LinkModel linkmodel = getLinkModel();

		LinkedList link_tables = new LinkedList(linkmodel.getTables());
		Iterator iter = link_tables.iterator();
		while (iter.hasNext()) {
			TableId tableid = (TableId) iter.next();

			Collection inlinks = new LinkedList(linkmodel.getInLinks(tableid));
			Iterator liter = inlinks.iterator();
			while (liter.hasNext()) {
				Link link = (Link) liter.next();
				if (!tset.contains(link.getSourceTableId())) {
					linkmodel.removeTable(link.getSourceTableId());
				}

				if (!tset.contains(link.getDestinationTableId())) {
					linkmodel.removeTable(link.getDestinationTableId());
				}
			}

			Collection outlinks = new LinkedList(linkmodel.getOutLinks(tableid));
			liter = outlinks.iterator();
			while (liter.hasNext()) {
				Link link = (Link) liter.next();
				if (!tset.contains(link.getSourceTableId())) {
					linkmodel.removeTable(link.getSourceTableId());
				}

				if (!tset.contains(link.getDestinationTableId())) {
					linkmodel.removeTable(link.getDestinationTableId());
				}
			}
		}

		return linkmodel;
	}

	/**
	 * Return the modeler that is associated with this view
	 */
	public ModelerModel getModeler() {
		return m_modeler;
	}

	/**
	 * @return the number of table widgets in the model
	 */
	public int getTableWidgetCount() {
		return m_tablewidgets.size();
	}

	/**
	 * @return the set of table widgets (TableWidget objects) that is currently
	 *         in this model
	 */
	public Collection getTableWidgets() {
		return m_tablewidgets.values();
	}

	/**
	 * @return the collection of tables (TableId objects) that are currently in
	 *         the model
	 */
	public Collection getTables() {
		LinkedList results = new LinkedList();
		Collection widgets = getTableWidgets();
		Iterator iter = widgets.iterator();
		while (iter.hasNext()) {
			TableWidget w = (TableWidget) iter.next();
			results.add(w.getTableId());
		}
		return results;
	}

	/**
	 * @return the table widget for the givent table id
	 */
	public TableWidget getTableWidget(TableId id) {
		return (TableWidget) m_tablewidgets.get(id);
	}

	/**
	 * @return the name of this view
	 */
	public String getViewName() {
		return TSUtils.fastTrim(m_viewName);
	}

	/**
	 * You must initialize the table widgets after the model has been
	 * deserialized. This is so the table widgets can get the most current table
	 * metadata from the database model and properly initialize themselves
	 */
	public void initialize(ModelerModel modeler) {
		if (!m_initialized) {
			m_modeler = modeler;
			assert (modeler.getConnection() != null);
			m_tsconn = modeler.getConnection();

			Collection tables = getTableWidgets();
			Iterator iter = tables.iterator();
			while (iter.hasNext()) {
				TableWidget widget = (TableWidget) iter.next();
				widget.getModel().setModeler(getModeler());
				try {
					widget.reset();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			iter = tables.iterator();
			// now iterate over tables and create all the links
			while (iter.hasNext()) {
				// we only need to call createIncomingLinkWidgets because all
				// tables
				// are already on the view, so all the links will be succesfully
				// created
				try {
					createIncomingLinkWidgets((TableWidget) iter.next());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			m_initialized = true;
		} else {
			assert (false);
		}
	}

	/**
	 * In some situations, when the user explicitly removes an existing link
	 * from the view, we need to keep track of this. This is used in the Form
	 * Builder and Query Builder. The reason is the next time the view is
	 * displayed, we don't want to automatically create links for foreign keys
	 * and global links for those links that were previously removed by the
	 * user. However, in the main ModelView, we don't want that behavior. When a
	 * link is removed from a view in the Modeler, we don't need to store the
	 * link
	 */
	public void rememberDeletedLinks(boolean brecall) {
		m_linkmodel.rememberDeletedLinks(brecall);
	}

	/**
	 * Removes the given link widget from the table
	 */
	public void removeLinkWidget(LinkWidget link) {
		m_linkmodel.removeLinkWidget(this, link);
	}

	/**
	 * Removes the given link widget from the table
	 */
	public void removeLinkWidget(Link link) {
		m_linkmodel.removeLinkWidget(this, link);
	}

	/**
	 * Removes a registered listener from this model
	 */
	public void removeListener(ModelViewModelListener listener) {
		if (listener != null) {
			Iterator iter = m_listeners.iterator();
			while (iter.hasNext()) {
				WeakReference wref = (WeakReference) iter.next();
				ModelViewModelListener wl = (ModelViewModelListener) wref.get();
				if (wl == null || wl == listener) {
					iter.remove();
				}
			}
		}
	}

	/**
	 * Removes the given table widget from the canvas. This also removes all
	 * links connected to that table
	 */
	public void removeWidget(TableWidget w) {
		if (w == null)
			return;

		TableId tableid = w.getTableId();
		// System.out.println( "ModelViewModel.removeWidget: " + tableid );

		// remove the links
		m_linkmodel.removeTable(this, tableid);
		// now remove the widget itself
		m_tablewidgets.remove(tableid);

		fireEvent(new ModelViewModelEvent(ModelViewModelEvent.TABLE_REMOVED, w));
	}

	/**
	 * Sets the name of this view. This is currently used in the view tab in the
	 * ModelViewFrame.
	 */
	public void setViewName(String viewName) {
		m_viewName = viewName;
	}

	/**
	 * Used for debugging
	 */
	public void validateModel() {
		Collection c = m_tablewidgets.keySet();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			TableId id = (TableId) iter.next();
			TableWidget w = (TableWidget) m_tablewidgets.get(id);
			if (!id.equals(w.getTableId())) {
				// System.out.println( "*ERROR*   id = " + id + "  widget = " +
				// w.getTableId() );
				assert (false);
			}
		}

		m_linkmodel.validateModel();
		// System.out.println( "modelviewmodel validated" );
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire JComponent hierarchy for this class
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int version = in.readInt();
		m_listeners = new LinkedList();

		// we store the table meta data, the links, and our bounds
		m_viewName = (String) in.readObject();

		m_tablewidgets = new HashMap();

		Collection c = (Collection) in.readObject();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			TableWidgetState tw_state = (TableWidgetState) iter.next();
			TableWidget tw = new TableWidget(tw_state);
			m_tablewidgets.put(tw.getTableId(), tw);
		}

		m_canvasSize = (Dimension) in.readObject();

		/** Now do some initializations and checks on the read in data */

		LinkWidgetModel linkmodel = (LinkWidgetModel) in.readObject();
		// @todo this is a hack, I need to provide a better solution later
		m_linkmodel.setDeletedLinks(linkmodel);
		Collection intables = linkmodel.getTables();
		Iterator titer = intables.iterator();
		while (titer.hasNext()) {
			TableId tableid = (TableId) titer.next();
			Collection inlinks = linkmodel.getInLinks(tableid);
			Iterator liter = inlinks.iterator();
			while (liter.hasNext()) {
				Link link = (Link) liter.next();
				createLinkWidget(link);
			}
		}
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire JComponent hierarchy for this class
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		// we store the table meta data, the links, and our bounds
		out.writeObject(m_viewName);

		/**
		 * manually store the table widgets because we don't want to store
		 * JComponents in a file due to differences between serialVersionUIDS
		 * from VM to VM. For example, IBM's VM has different serialVersionUIDs
		 * for JComponent than Sun's VM. So, you can't read in a serialized
		 * TableWidget that is stored with Sun VM into Abeille that is running
		 * in IBM's VM
		 */
		LinkedList tw_state_store = new LinkedList();

		Collection c = m_tablewidgets.values();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			TableWidget tw = (TableWidget) iter.next();
			tw_state_store.add(new TableWidgetState(tw));
		}

		out.writeObject(tw_state_store);

		// we don't explictly write out the database links (i.e. foriegn keys ),
		// since they
		// are automatically generated by the TableMetaData when the table is
		// loaded and added to the view
		out.writeObject(m_canvasSize);

		out.writeObject(m_linkmodel);
	}

}
