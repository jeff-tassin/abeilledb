package com.jeta.abeille.gui.sql;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JList;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.ext.CompletionQuery;

import com.jeta.abeille.database.model.ColumnMetaDataComparator;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdComparator;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.foundation.gui.components.PopupList;
import com.jeta.foundation.gui.components.SortedListModel;

/**
 * SQL completion support finder
 * 
 * @author Jeff Tassin
 */
public class SQLCompletionQuery implements CompletionQuery {
	/** The completion object that created this object */
	private SQLCompletion m_completion;

	/** This is the model of tables that we are allowed to provide completion on */
	private TableSelectorModel m_model;

	/**
	 * this is a special mode where the completion shows all tables in the
	 * database
	 */
	public static final String SHOW_TABLES = "show.tables";
	public static final String SHOW_COLUMNS = "show.columns";

	private String m_mode = SHOW_COLUMNS;

	/** the objects used for doing completions depending on the query mode */
	private TableCompletionQuery m_tablequery = null;
	private MetaDataCompletionQuery2 m_metadataquery = null;

	/** the last completion query reult */
	private SQLCompletionResult m_lastresult = null;

	/**
	 * ctor
	 */
	public SQLCompletionQuery(SQLCompletion completion, TableSelectorModel model) {
		m_completion = completion;
		m_model = model;

		m_tablequery = new TableCompletionQuery(completion, model);
		m_metadataquery = new MetaDataCompletionQuery2(completion, model);
	}

	/**
	 * @return the last result
	 */
	SQLCompletionResult getLastResult() {
		return m_lastresult;
	}

	/**
	 * CompletionQuery implementation
	 */
	public CompletionQuery.Result query(JTextComponent component, int offset, SyntaxSupport support) {
		boolean bresult = isShowTables();
		if (bresult) {
			m_lastresult = (SQLCompletionResult) m_tablequery.query(component, offset, support, false);
			PopupList plist = m_completion.getPopup();

			if (m_lastresult != null) {
				SortedListModel listmodel = (SortedListModel) plist.getModel();
				listmodel.setComparator(m_lastresult.getComparator());
				plist.setComparator(m_lastresult.getComparator());
			}
			return m_lastresult;
		} else {
			m_lastresult = (SQLCompletionResult) m_metadataquery.query(component, offset, support, false);
			if (m_lastresult != null) {
				PopupList plist = m_completion.getPopup();
				SortedListModel listmodel = (SortedListModel) plist.getModel();
				listmodel.setComparator(m_lastresult.getComparator());
				plist.setComparator(m_lastresult.getComparator());
			}
			return m_lastresult;
		}
	}

	public void reset() {
		setMode(SHOW_COLUMNS);
	}

	/**
	 * Sets the mode for the completion. If the mode is SHOW_TABLES, then the
	 * popup shows all tables in the database and allows the user to type a
	 * table name based on this data. Otherwise, the completion tries to located
	 * the table that is typed in the editor and fill the completion popup with
	 * the columns from that table.
	 */
	public void setMode(String mode) {
		if (mode != null && mode.equals(SHOW_TABLES))
			m_mode = SHOW_TABLES;
		else
			m_mode = SHOW_COLUMNS;
	}

	/**
	 * @return true if the SQLCompletion mode is to show all tables in the
	 *         system
	 */
	public boolean isShowTables() {
		return (m_mode != null && m_mode.equals(SHOW_TABLES));
	}

}
