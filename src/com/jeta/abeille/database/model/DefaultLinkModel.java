package com.jeta.abeille.database.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class is responsible for managing links between database tables. A link
 * is defined by a primary key - foreign key relationship. These links are
 * created automatically by the database model and are managed here.
 * Furthermore, the user can create 'user-defined' links used for the query
 * tools. This allows the user to automatically join tables using these
 * artifical links.
 * 
 * @author Jeff Tassin
 */
public class DefaultLinkModel implements LinkModel {
	/**
	 * This is a hash map that is keyed on TableId. The value is a LinkedList of
	 * incoming Link objects for a given table
	 */
	private HashMap m_inlinks = new HashMap();

	/**
	 * This is a hash map that is keyed on TableId. The value is a LinkedList of
	 * outgoing Link objects for a given table
	 */
	private HashMap m_outlinks = new HashMap();

	/**
	 * Add the given link assignment assignment
	 */
	public synchronized void addLink(Link link) {
		// System.out.print( "DefaultLinkModel addLink: " );
		// link.print();
		if (addInLink(link.getDestinationTableId(), link))
			addOutLink(link.getSourceTableId(), link);
	}

	/**
	 * Adds a link to the in map
	 */
	private boolean addInLink(TableId tableid, Link link) {
		LinkedList links = (LinkedList) m_inlinks.get(tableid);
		if (links == null) {
			links = new LinkedList();
			m_inlinks.put(tableid, links);
		}

		boolean bresult = true;
		if (links.contains(link)) {
			if (!link.isSelfReference()) {
				TSUtils.printMessage(">>>>>>>>>>>>>>>>>>>>>>>>>>>  Error:  link model already contains link: ");
				link.print();
				Exception e = new Exception();
				e.printStackTrace();
			}
			bresult = false;
		} else {
			links.add(link);
		}
		return bresult;
	}

	/**
	 * Adds a given link to the out map
	 */
	private void addOutLink(TableId tableid, Link link) {
		LinkedList links = (LinkedList) m_outlinks.get(tableid);
		if (links == null) {
			links = new LinkedList();
			m_outlinks.put(tableid, links);
		}

		if (links.contains(link)) {
			System.out.print("Error:  link model already contains link: ");
			link.print();
			tableid.print();
			assert (false);
		}

		links.add(link);
	}

	/**
	 * @return true if this model contains the given link
	 */
	public synchronized boolean contains(Link link) {
		boolean bresult = false;
		TableId destid = link.getDestinationTableId();
		LinkedList links = (LinkedList) m_inlinks.get(destid);
		if (links != null && links.contains(link)) {
			TableId srcid = link.getSourceTableId();
			links = (LinkedList) m_outlinks.get(srcid);
			if (links.contains(link))
				bresult = true;
			else {
				assert (false);
			}
		}

		return bresult;

	}

	/**
	 * Cloneable implementation
	 */
	public synchronized Object clone() {
		DefaultLinkModel model = new DefaultLinkModel();
		model.m_inlinks = (HashMap) m_inlinks.clone();
		model.m_outlinks = (HashMap) m_outlinks.clone();
		return model;
	}

	/**
	 * @return the collection of incoming links (Link objects) for the given
	 *         table. A link is defined by a foreign key for the given table
	 */
	public synchronized Collection getInLinks(TableId tableId) {
		LinkedList list = (LinkedList) m_inlinks.get(tableId);
		if (list == null) {
			list = new LinkedList();
			m_inlinks.put(tableId, list);
		}
		return list;
	}

	/**
	 * @return all links for the given table
	 */
	public synchronized Collection getLinks(TableId tableId) {
		LinkedList result = new LinkedList();
		result.addAll(getInLinks(tableId));
		result.addAll(getOutLinks(tableId));
		return result;
	}

	/**
	 * @return the collection of outgoing links (Link objects) for the given
	 *         table. A link is defined by a table who has a foreign key
	 *         referenced to the given table's primary key
	 */
	public synchronized Collection getOutLinks(TableId tableId) {
		LinkedList list = (LinkedList) m_outlinks.get(tableId);
		if (list == null) {
			list = new LinkedList();
			m_outlinks.put(tableId, list);
		}
		return list;
	}

	/**
	 * @return a collection of table ids in this model
	 */
	public synchronized Collection getTables() {
		return m_inlinks.keySet();
	}

	/**
	 * Prints the model to the console
	 */
	public void print() {
		System.out.println("----------- printing defaultlinkmodel  --------- ");
		System.out.println("----------- printing in links  --------- ");
		Collection links = m_inlinks.keySet();
		Iterator iter = links.iterator();
		while (iter.hasNext()) {
			TableId tableid = (TableId) iter.next();
			LinkedList list = (LinkedList) m_inlinks.get(tableid);
			Iterator liter = list.iterator();
			while (liter.hasNext()) {
				Link link = (Link) liter.next();
				System.out.print(tableid + ":  ");
				link.print();
				System.out.println("");
			}
		}

		System.out.println("----------- printing out links  --------- ");

		links = m_outlinks.keySet();
		iter = links.iterator();
		while (iter.hasNext()) {
			TableId tableid = (TableId) iter.next();
			LinkedList list = (LinkedList) m_outlinks.get(tableid);
			Iterator liter = list.iterator();
			while (liter.hasNext()) {
				Link link = (Link) liter.next();
				System.out.print(tableid + ":  ");
				link.print();
				System.out.println("");
			}
		}

		if (links.size() == 0)
			System.out.println("      no links ");

		// we don't need to print the in links because they are exactly the
		// same as the in links
	}

	/**
	 * Removes the given link from this model
	 */
	public synchronized void removeLink(Link link) {
		Iterator iter = m_outlinks.values().iterator();
		while (iter.hasNext()) {
			LinkedList list = (LinkedList) iter.next();
			list.remove(link);
		}

		iter = m_inlinks.values().iterator();
		while (iter.hasNext()) {
			LinkedList list = (LinkedList) iter.next();
			list.remove(link);
		}
	}

	/**
	 * Removes all links that directly reference the given table from the model
	 * 
	 * @param tableId
	 *            the id of the table whose in/out links we wish to remove
	 */
	public synchronized void removeTable(TableId tableId) {
		m_inlinks.remove(tableId);
		m_outlinks.remove(tableId);

		// now remove any links from any tables that referenced the given table
		Iterator iter = m_outlinks.values().iterator();
		while (iter.hasNext()) {
			LinkedList list = (LinkedList) iter.next();
			Iterator liter = list.iterator();
			while (liter.hasNext()) {
				Link link = (Link) liter.next();
				if (link.references(tableId))
					liter.remove();
			}
		}

		iter = m_inlinks.values().iterator();
		while (iter.hasNext()) {
			LinkedList list = (LinkedList) iter.next();
			Iterator liter = list.iterator();
			while (liter.hasNext()) {
				Link link = (Link) liter.next();
				if (link.references(tableId))
					liter.remove();
			}
		}
	}

}
