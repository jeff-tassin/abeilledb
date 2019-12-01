package com.jeta.abeille.gui.formbuilder;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;

/**
 * This class traverses links in a link model and forms the paths for a sub
 * query for a given starting table.
 * 
 * @author Jeff Tassin
 */
public class SubQueryBuilder {

	public static SubQuery build(TableId startId, LinkModel linkModel, TableId anchorTable) {
		// System.out.println( "SubQuery.build  startId = " + startId );
		// iterate over all in/out links for startId util we get to a leaf
		SubQuery subquery = new SubQuery();
		TreeSet intables = new TreeSet();
		TreeSet outtables = new TreeSet();
		buildInLinks(startId, linkModel, anchorTable, subquery, intables, outtables);
		buildOutLinks(startId, linkModel, anchorTable, subquery, intables, outtables);
		// subquery.print();
		return subquery;
	}

	private static void buildInLinks(TableId tableId, LinkModel linkModel, TableId anchorTable, SubQuery subquery,
			TreeSet intables, TreeSet outtables) {
		if (intables.contains(tableId))
			return;

		// System.out.println( "buildInLinks:  " + tableId );
		intables.add(tableId);
		subquery.addTable(tableId);

		Collection inlinks = linkModel.getInLinks(tableId);
		Iterator iter = inlinks.iterator();
		while (iter.hasNext()) {
			Link link = (Link) iter.next();
			TableId srctable = link.getSourceTableId();
			if (!srctable.equals(anchorTable) && !subquery.contains(srctable)) {
				subquery.addLink(link);
				buildInLinks(srctable, linkModel, anchorTable, subquery, intables, outtables);
				buildOutLinks(srctable, linkModel, anchorTable, subquery, intables, outtables);
			}
		}
	}

	private static void buildOutLinks(TableId tableId, LinkModel linkModel, TableId anchorTable, SubQuery subquery,
			TreeSet intables, TreeSet outtables) {
		if (outtables.contains(tableId)) {
			// System.out.println( "SubQuery.buildOutLinks contains " + tableId
			// );
			return;
		}

		// System.out.println( "buildOutLinks:  " + tableId );
		outtables.add(tableId);
		subquery.addTable(tableId);

		Collection outlinks = linkModel.getOutLinks(tableId);
		Iterator iter = outlinks.iterator();
		while (iter.hasNext()) {
			Link link = (Link) iter.next();
			TableId desttable = link.getDestinationTableId();
			if (!desttable.equals(anchorTable) && !subquery.contains(desttable)) {
				subquery.addLink(link);
				buildOutLinks(desttable, linkModel, anchorTable, subquery, intables, outtables);
				buildInLinks(desttable, linkModel, anchorTable, subquery, intables, outtables);
			}
		}
	}

}
