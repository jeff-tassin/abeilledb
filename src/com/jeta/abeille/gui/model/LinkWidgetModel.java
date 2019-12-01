package com.jeta.abeille.gui.model;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.MultiColumnLink;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class manages the link widgets for a given model view/model
 * 
 * @author Jeff Tassin
 */
public class LinkWidgetModel implements LinkModel, JETAExternalizable {
	static final long serialVersionUID = 81265519954372960L;

	public static int VERSION = 1;

	/** our default model instance for managing the Link objects */
	private DefaultLinkModel m_linkmodel = new DefaultLinkModel();

	/** the LinkWidget object instances */
	private LinkedList m_widgets = new LinkedList();

	/**
	 * This is a list of foreign key links that were explicitly deleted by the
	 * user. We need this because we don't want to re-add foreign key links when
	 * the application is restarted if there were explictily deleted.
	 */
	private LinkedList m_deletedlinks = new LinkedList();

	/**
	 * Flag to indicate whether we should keep track of deleted links. The
	 * default is to remember those links that were deleted.
	 */
	private boolean m_rememberdeletedlinks = true;

	/**
	 * ctor only for serialization
	 */
	public LinkWidgetModel() {
	}

	/**
	 * Creates a single link widget for the given link. The widget is created
	 * only if both the source and destination table are visible
	 */
	public LinkWidget createLinkWidget(ModelViewModel tableModel, Link link) {
		if (link == null) {
			TSUtils.printDebugMessage("LinkWidgetModel.createLinkWidget link = null ");
			return null;
		}

		/** the user deleted this link explicitly, so don't add */
		if ((link.isUserDefined() || link instanceof MultiColumnLink) && m_rememberdeletedlinks
				&& m_deletedlinks.contains(link)) {
			TSUtils.printMessage("*********** global key link was deleted by user. skip automatic creation ***** ");
			// link.print();
			return null;
		}

		if (m_linkmodel.contains(link)) {
			TSUtils.printDebugMessage("LinkWidgetModel.createLinkWidget model already contains link");
			if (TSUtils.isDebug()) {
				link.print();
			}
		} else {
			TableWidget src = tableModel.getTableWidget(link.getSourceTableId());
			TableWidget dest = tableModel.getTableWidget(link.getDestinationTableId());
			if (src != null && dest != null) {
				LinkWidget linkwidget = new LinkWidget(null, src, dest, link);

				src.addLink(linkwidget);
				dest.addLink(linkwidget);

				m_linkmodel.addLink(link);
				m_widgets.add(linkwidget);
				// System.out.println( "linkwidgetmodel.createLinkWidget: " );
				// link.print();
				return linkwidget;
			}
		}

		return null;
	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		LinkWidgetModel model = new LinkWidgetModel();
		model.m_linkmodel = (DefaultLinkModel) m_linkmodel.clone();
		model.m_widgets = (LinkedList) m_widgets.clone();
		model.m_deletedlinks = (LinkedList) m_deletedlinks.clone();
		return model;
	}

	/**
	 * @return the collection of incoming links (Link objects) for the given
	 *         table. A link is defined by a foreign key for the given table
	 */
	public Collection getInLinks(TableId tableId) {
		return m_linkmodel.getInLinks(tableId);
	}

	/**
	 * @return all links for the given table
	 */
	public Collection getLinks(TableId tableId) {
		return m_linkmodel.getLinks(tableId);
	}

	/**
	 * @return the collection of link widgets in this view
	 */
	LinkedList getLinkWidgets() {
		return m_widgets;
	}

	/**
	 * @return the collection of outgoing links (Link objects) for the given
	 *         table. A link is defined by a table who has a foreign key
	 *         referenced to the given table's primary key
	 */
	public Collection getOutLinks(TableId tableId) {
		return m_linkmodel.getOutLinks(tableId);
	}

	/**
	 * @return a collection of table ids in this model
	 */
	public Collection getTables() {
		return m_linkmodel.getTables();
	}

	/**
	 * Dump model to console
	 */
	public void print() {
		// m_linkmodel.print();
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
		m_rememberdeletedlinks = brecall;
		if (!brecall)
			m_deletedlinks.clear();
	}

	/**
	 * Removes all link/link widget objects that are related to the given table
	 * id
	 */
	public void removeTable(ModelViewModel tableModel, TableId tableId) {
		// now remove all in links for the table widget that we are removing
		Iterator linkiter = getInLinks(tableId).iterator();
		while (linkiter.hasNext()) {
			Link link = (Link) linkiter.next();
			TableWidget src = tableModel.getTableWidget(link.getSourceTableId());
			TableWidget dest = tableModel.getTableWidget(link.getDestinationTableId());
			if (src != null)
				src.removeLink(link);
			if (dest != null)
				dest.removeLink(link);

		}

		// now remove all out links for the table widget that we are removing
		linkiter = getOutLinks(tableId).iterator();
		while (linkiter.hasNext()) {
			Link link = (Link) linkiter.next();
			TableWidget src = tableModel.getTableWidget(link.getSourceTableId());
			TableWidget dest = tableModel.getTableWidget(link.getDestinationTableId());
			if (src != null)
				src.removeLink(link);
			if (dest != null)
				dest.removeLink(link);
		}

		// now remove the links from the model
		Iterator iter = m_widgets.iterator();
		while (iter.hasNext()) {
			LinkWidget lw = (LinkWidget) iter.next();
			Link link = lw.getLink();
			if (link.references(tableId)) {
				iter.remove();
				m_linkmodel.removeLink(link);
			}
		}

		// now remove from deletedfk links
		iter = m_deletedlinks.iterator();
		while (iter.hasNext()) {
			Link link = (Link) iter.next();
			if (link.references(tableId)) {
				iter.remove();
			}
		}

	}

	/**
	 * Removes the link (widget) from this model.
	 */
	public void removeLinkWidget(ModelViewModel model, Link link) {
		if (link == null)
			return;

		LinkedList remove = new LinkedList();
		Iterator iter = m_widgets.iterator();
		while (iter.hasNext()) {
			LinkWidget lw = (LinkWidget) iter.next();
			if (link.equals(lw.getLink()))
				remove.add(lw);
		}

		iter = remove.iterator();
		while (iter.hasNext()) {
			removeLinkWidget(model, (LinkWidget) iter.next());
		}
	}

	/**
	 * Removes the link widget from this model.
	 */
	public void removeLinkWidget(ModelViewModel model, LinkWidget linkwidget) {
		if (linkwidget == null)
			return;

		Link link = linkwidget.getLink();

		TableWidget src = model.getTableWidget(linkwidget.getSourceTableId());
		if (src != null)
			src.removeLink(link);

		TableWidget dest = model.getTableWidget(linkwidget.getDestinationTableId());
		if (dest != null)
			dest.removeLink(link);

		m_linkmodel.removeLink(link);

		m_widgets.remove(linkwidget);

		if ((link.isUserDefined() || link instanceof MultiColumnLink) && m_rememberdeletedlinks) {
			// System.out.println(
			// "---------------- deleting foreign key link -----------  " );
			// make sure it is not already in the list
			// link.print();
			assert (!m_deletedlinks.contains(link));
			m_deletedlinks.add(link);
		}

		// System.out.println(
		// "----------------- LinkWidgetModel.removeLinkWidget ------------------- "
		// );
		// link.print();
		// print();
	}

	/**
	 * Removes all links that directly reference the given table from the model
	 * 
	 * @param tableId
	 *            the id of the table whose in/out links we wish to remove
	 */
	public void removeTable(TableId tableId) {
		assert (false);
	}

	/**
	 * This is a temporary hack to get around the way the ModelViewModel is
	 * loaded from disk
	 */
	void setDeletedLinks(LinkWidgetModel lmodel) {
		m_deletedlinks = lmodel.m_deletedlinks;
		if (m_deletedlinks == null)
			m_deletedlinks = new LinkedList();
	}

	/**
	 * This is for debugging purposes only
	 */
	public void validateModel() {
		if (TSUtils.isDebug()) {
			Iterator iter = m_widgets.iterator();
			while (iter.hasNext()) {
				LinkWidget linkwidget = (LinkWidget) iter.next();
				assert (linkwidget.isVisible());
				assert (m_linkmodel.contains(linkwidget.getLink()));
			}
		}
	}

	/**
	 * Provide our own serialization handling because we only store user defined
	 * links
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int version = in.readInt();
		// read in user defined links

		// LinkedList userlinks = (LinkedList)in.readObject();
		// Iterator iter = userlinks.iterator();
		// while( iter.hasNext() )
		// {
		// simply add the links to the link model, later, the modelviewmodel
		// will
		// ask this model to create the links widgets for all of its tables
		// m_linkmodel.addLink( (Link)iter.next() );
		// }

		m_deletedlinks = (LinkedList) in.readObject();
		// System.out.println(
		// "LinkWidgetModel.readExternal Deletefk links size = " +
		// m_deletedlinks.size() );
	}

	/**
	 * Provide our own serialization handling because we only want to store user
	 * defined links
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		// write out user defined links

		// 7-17-04 remove this since the modeler now manages user defined links

		// LinkedList userlinks = new LinkedList();
		// Iterator iter = m_widgets.iterator();
		// while( iter.hasNext() )
		// {
		// LinkWidget linkwidget = (LinkWidget)iter.next();
		// if ( linkwidget.isUserDefinedLocal() )
		// {
		// userlinks.add( linkwidget.getLink() );
		// }
		// }
		// out.writeObject( userlinks );

		out.writeObject(m_deletedlinks);
		// System.out.println(
		// ">>>>>>>>>>> LinkWidgetModel.writeExternal   m_deletefklinks.size = "
		// + m_deletedlinks.size() );
	}

}
