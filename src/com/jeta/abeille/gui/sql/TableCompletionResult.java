package com.jeta.abeille.gui.sql;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.JTextComponent;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableNameComparator;
import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.foundation.gui.components.SortedListModel;

/**
 * This class contains the items used to populate the popup list. It is returned
 * as the result of a query to the CompletionQuery class
 */
public class TableCompletionResult extends SQLCompletionResult {

	private static Comparator m_comparator = new TableNameComparator();

	/**
	 * ctor
	 */
	public TableCompletionResult(JTextComponent editor, SQLCompletion completion, TableSelectorModel tableselector,
			SQLToken partialToken) {

		super(editor, completion, partialToken);
		Catalog cat = tableselector.getCurrentCatalog();
		initialize(tableselector, cat, tableselector.getCurrentSchema(cat));
	}

	public TableCompletionResult(JTextComponent editor, SQLCompletion completion, TableSelectorModel tableselector,
			Catalog catalog, Schema schema, SQLToken partialToken) {
		super(editor, completion, partialToken);

		initialize(tableselector, catalog, schema);
	}

	private void initialize(TableSelectorModel tableselector, Catalog catalog, Schema schema) {
		try {
			SortedListModel list = tableselector.getTables(catalog, schema);
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				TableId tableid = (TableId) iter.next();
				addItem(tableid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the comparator
	 */
	public Comparator getComparator() {
		return m_comparator;
	}
}
