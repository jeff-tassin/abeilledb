package com.jeta.abeille.gui.sql;

import javax.swing.JList;

import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.CompletionView;
import org.netbeans.editor.ext.CompletionPane;
import org.netbeans.editor.ext.ExtEditorUI;

import com.jeta.abeille.database.model.ColumnMetaDataComparator;
import com.jeta.abeille.database.model.TableIdComparator;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.MetaDataPopupRenderer;
import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.foundation.gui.components.PopupList;
import com.jeta.foundation.gui.components.SortedListModel;

import com.jeta.open.gui.utils.JETAToolbox;

/**
 * SQL completion query specifications
 * 
 * @author Jeff Tassin
 */
public class SQLCompletion extends Completion {
	private SQLCompletionPopup m_pane;

	/**
	 * This is the set of tables that we use for completions
	 */
	private TableSelectorModel m_model;

	/** the completion query object */
	private SQLCompletionQuery m_completionquery = null;

	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public SQLCompletion(ExtEditorUI extEditorUI, TableSelectorModel model, TSConnection conn) {
		super(extEditorUI);
		m_model = model;
		m_connection = conn;
	}

	/**
	 * Override Completion.createQuery This creates the class that is
	 * responsible for parsing the document and returning the results to be used
	 * in the popup.
	 */
	protected CompletionQuery createQuery() {

		if (m_completionquery == null)
			m_completionquery = new SQLCompletionQuery(this, m_model);

		return m_completionquery;
	}

	/**
	 * Override Completion.createView so we can provide our own view This method
	 * gets called after we have done a CompletionQuery.
	 * 
	 * The returned class is the popup
	 */
	protected CompletionView createView() {
		// the SQLCompletionPopup is both the view and the pane
		getPane();
		return m_pane;
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * Override Completion.getPane because it creates a ScrollCompletionPane
	 * instance (org.netbeans.editor.ext.ScrollCompletionPane). This class has a
	 * bug that prevents the popup from displaying correctly when the editor is
	 * not the only child in the frame window. Plus, we want a little tighter
	 * control here.
	 */
	public CompletionPane getPane() {
		if (m_pane == null) {
			m_pane = new SQLCompletionPopup(extEditorUI, this);
		}
		return m_pane;
	}

	/**
	 * @return the popup list
	 */
	public PopupList getPopup() {
		return ((SQLCompletionPopup) getPane()).getPopup();
	}

	/**
	 * Return true when the pane exists and is visible. This is the preferred
	 * method of testing the visibility of the pane instead of
	 * <tt>getPane().isVisible()</tt> that forces the creation of the pane.
	 */
	public boolean isPaneVisible() {
		return (m_pane != null && m_pane.isVisible());
	}

	public void reset() {
		if (m_completionquery != null) {
			m_completionquery.reset();
		}
	}

	/**
	 * Set the visibility of the view. This method should be used mainly for
	 * hiding the completion pane. If used with visible set to true it calls the
	 * <tt>popup(false)</tt>. I need to override from Completion because the
	 * completion class has a problem with the design if you don't use the
	 * ScrollCompletionPane class.
	 */
	public void setPaneVisible(boolean visible) {
		super.setPaneVisible(visible);
		if (!visible) {
			cancelRequest();
			invalidateLastResult();
			if (m_pane != null) {
				m_pane.setVisible(false);
			}
		}
	}

}
