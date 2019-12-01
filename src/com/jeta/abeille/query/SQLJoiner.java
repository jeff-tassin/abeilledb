package com.jeta.abeille.query;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;

/**
 * This class is responsible for determining the valid links in a given set of
 * tables, links, and contraints for a SQL query.
 * 
 * @author Jeff Tassin
 */
public class SQLJoiner {

	/**
	 * Builds a set of joins between a set of given tables.
	 * 
	 * @param linkModel
	 *            the object that defines the links in the given graph
	 * @param tables
	 *            the set of tables (TableId objects) in the graph
	 * @param constraints
	 *            the set of tables that have defined constraints (TableIds)
	 * @param reportables
	 *            the set of tables that have columns in the reportabls
	 *            (TableIds)
	 * @return the links that define the joins between the tables
	 */
	public static Collection buildJoins(LinkModel linkModel, Collection tables, Collection constraints,
			Collection reportables) {
		if (tables.size() <= 1) // for a single table there are no joins
			return new LinkedList();

		// System.out.println(" --------------------- buildJoins start -------------------- "
		// );

		// first step is to put all constraints and reportables into a fast
		// lookup container
		TreeSet markernodes = new TreeSet();
		Iterator iter = constraints.iterator();
		while (iter.hasNext()) {
			markernodes.add((TableId) iter.next());
		}

		iter = reportables.iterator();
		while (iter.hasNext()) {
			markernodes.add((TableId) iter.next());
		}

		// now build the list of valid paths
		LinkedList paths = new LinkedList();
		iter = tables.iterator();
		while (iter.hasNext()) {
			TableId startnode = (TableId) iter.next();
			// System.out.println( "SQLJoiner  table = " + startnode );
			Path path = new Path();
			buildPath(linkModel, startnode, path, paths);
		}

		// System.out.println("---------------raw paths start--------------" );
		// iter = paths.iterator();
		// while(iter.hasNext() )
		// {
		// Path path = (Path)iter.next();
		// path.print();
		// }
		// System.out.println("---------------raw paths end--------------" );

		// now iterate over all paths and trim the end nodes that are not
		// explicitly in the markernodes
		// also, the last step is to remove repeated links
		LinkedList results = new LinkedList();
		TreeSet unique_links = new TreeSet();
		Iterator pathiter = paths.iterator();
		while (pathiter.hasNext()) {
			Path path = (Path) pathiter.next();
			trimPath(path, markernodes);

			Collection links = path.getLinks();
			Iterator liter = links.iterator();
			while (liter.hasNext()) {
				PathLink plink = (PathLink) liter.next();
				Link link = plink.getLink();
				if (!unique_links.contains(link)) {
					unique_links.add(link);
					results.add(link);
				}
			}
		}

		// let's dump out the results now

		// System.out.println(
		// "--------- SQLJoiner printing results --------------" );
		// iter = results.iterator();
		// while( iter.hasNext() )
		// {
		// Link link = (Link)iter.next();
		// link.print();
		// System.out.println();
		// }

		return results;
	}

	/**
	 * Starts traversing the link model and builds linear (non-branching) paths
	 * from the model. Circular paths are detected and stopped at the point of
	 * cicularity
	 */
	public static void buildPath(LinkModel linkModel, TableId node, Path path, LinkedList paths) {
		Collection links = getLinks(linkModel, node);
		// System.out.println( "buildPath  node = " + node +
		// "  outlinks.size = " + links.size() );
		Iterator niter = links.iterator();
		while (niter.hasNext()) {
			Link nextlink = (Link) niter.next();
			if (path.contains(nextlink)) {
				// then we have a circular path and we are done
				// System.out.println( "buildPath detected cicularpath: " );
				// nextlink.print();
				paths.add((Path) path.clone());
			} else {
				TableId nexttable = nextlink.getDestinationTableId();
				if (node.equals(nexttable))
					nexttable = nextlink.getSourceTableId();

				PathLink plink = new PathLink(node, nexttable, nextlink);
				Path newpath = (Path) path.clone();
				newpath.add(plink);
				buildPath(linkModel, nexttable, newpath, paths);
			}
		}
		paths.add(path);
	}

	/**
	 * @returns the set of all links( Link objects) that are connected (either
	 *          in or out connections) to the given table.
	 */
	public static Collection getLinks(LinkModel linkModel, TableId tableId) {
		LinkedList results = new LinkedList();
		results.addAll(linkModel.getInLinks(tableId));
		results.addAll(linkModel.getOutLinks(tableId));
		return results;
	}

	public static void trimPath(Path path, TreeSet markernodes) {
		if (path.size() == 0)
			return;

		// System.out.println( "--------- trimming path --------" );
		// path.print();
		for (int index = 0; index < path.size(); index++) {
			PathLink link = path.getFirst();

			if (link != null && !markernodes.contains(link.getStartId())) {
				// System.out.println( "path removing first" );
				path.removeFirst();
			}

			link = path.getLast();
			if (link != null) {
				// System.out.print( " last link: " );
				// link.print();
				// System.out.println( "    endid = " + link.getEndId() );
				// System.out.println( "markernodes.contains endid: " +
				// markernodes.contains( link.getEndId() ) );
				if (link != null && !markernodes.contains(link.getEndId()))
					path.removeLast();
			}
		}

		// if the path size is 1, then BOTH destination and source tables must
		// be in the markernodes
		if (path.size() == 1) {
			PathLink plink = path.getFirst();
			if (!markernodes.contains(plink.getStartId()) || !markernodes.contains(plink.getEndId()))
				path.removeFirst();
		}

		// path.print();
		// System.out.println( "------------- trim complete ------" );
	}

}
