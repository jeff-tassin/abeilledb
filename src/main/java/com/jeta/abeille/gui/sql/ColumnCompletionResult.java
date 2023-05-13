package com.jeta.abeille.gui.sql;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.JTextComponent;
import javax.swing.text.Document;

import org.netbeans.editor.ext.CompletionQuery;

import com.jeta.foundation.gui.components.PopupList;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.ColumnMetaDataComparator;
import com.jeta.abeille.database.model.DbObjectNameComparator;
import com.jeta.abeille.database.model.TableMetaData;

/**
 * This class contains the items used to populate the popup list. It is returned
 * as the result of a query to the CompletionQuery class
 */
public class ColumnCompletionResult extends SQLCompletionResult {
	private static Comparator m_comparator = new DbObjectNameComparator();

	/**
	 * ctor
	 */
	public ColumnCompletionResult(JTextComponent editor, SQLCompletion completion, TableMetaData tmd,
			SQLToken columnToken) {
		super(editor, completion, columnToken);

		Collection c = tmd.getColumns();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			addItem(cmd);
		}

	}

	/**
	 * @return the comparator
	 */
	public Comparator getComparator() {
		return m_comparator;
	}

}
