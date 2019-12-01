package com.jeta.abeille.gui.sql;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.text.JTextComponent;
import javax.swing.text.Document;

import com.jeta.abeille.database.model.TSConnection;

import org.netbeans.editor.ext.CompletionQuery;
import com.jeta.foundation.gui.components.PopupList;

/**
 * This class contains the items used to populate the popup list. It is returned
 * as the result of a query to the CompletionQuery class
 */
public class SQLCompletionResult implements CompletionQuery.Result {
	/**
	 * The main completion object
	 */
	private SQLCompletion m_completion;

	/**
	 * This list of tokens that make up the items in the popup
	 */
	private LinkedList m_data = new LinkedList();

	/** the partially typed completion */
	private SQLToken m_partialToken;

	/**
	 * The editor we are currently handling completions for
	 */
	private JTextComponent m_editor;

	private Comparator m_comparator;

	/**
	 * ctor
	 */
	public SQLCompletionResult(JTextComponent editor, SQLCompletion completion, SQLToken partialToken) {
		m_completion = completion;
		m_editor = editor;
		m_partialToken = partialToken;
	}

	public SQLCompletionResult(JTextComponent editor, SQLCompletion completion, Collection data, SQLToken partialToken,
			Comparator comp) {
		this(editor, completion, partialToken);
		if (data != null)
			m_data.addAll(data);
		m_comparator = comp;
		assert (comp != null);
	}

	/**
	 * Adds an item to the completion list
	 */
	public void addItem(Object item) {
		m_data.add(item);
	}

	/**
	 * @return the comparator
	 */
	public Comparator getComparator() {
		assert (m_comparator != null);
		return m_comparator;
	}

	public TSConnection getConnection() {
		return m_completion.getConnection();
	}

	/**
	 * Get the list with the items satisfying the query. The list must always be
	 * non-null. If there are no data it will have a zero size.
	 * 
	 * @return List of objects implementing ResultItem.
	 */
	public List getData() {
		return m_data;
	}

	/**
	 * @return the partially typed string that is used to select from the combo
	 *         list
	 */
	public SQLToken getPartialCompletion() {
		return m_partialToken;
	}

	/** Get the title describing the result or null if there's no title. */
	public String getTitle() {
		return null;
	}

	/**
	 * Substitute the text in the document if the user picks the item from the
	 * data with the given index either by pressing ENTER or doubleclicking the
	 * item by mouse.
	 * 
	 * @param dataIndex
	 *            current selected item index in the current data list. It can
	 *            be used for making the substitution.
	 * @param shift
	 *            indicates request for some kind of different behaviour, means
	 *            that e.g. user hold shift while pressing ENTER.
	 * @return whether the text was substituted or not
	 */
	public boolean substituteText(int dataIndex, boolean shift) {
		// the user pressed enter, so lets substitute the currently selected
		// item
		if (m_partialToken != null) {
			PopupList popup = m_completion.getPopup();
			String result = popup.selectText(m_partialToken.getToken());
			try {
				Document doc = m_editor.getDocument();
				doc.remove(m_partialToken.getDocumentPos(), m_partialToken.getToken().length());
				doc.insertString(m_partialToken.getDocumentPos(), result, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		m_completion.setPaneVisible(false); // we're done
		return true;
	}

	/**
	 * Substitute the text that is common for all the data entries. This is used
	 * to update the document with the common text when the user presses the TAB
	 * key.
	 * 
	 * @param dataIndex
	 *            current selected item index in the current data list. Although
	 *            normally it shouldn't be necessary for making the
	 *            substitution, the completion implementations can use it for
	 *            customized behavior.
	 * @return whether the text was substituted or not
	 */
	public boolean substituteCommonText(int dataIndex) {

		// user hit tab key, so let's try to complete
		if (m_partialToken != null) {
			PopupList popup = m_completion.getPopup();

			PopupList.Result presult = popup.selectCommonText(m_partialToken.getToken());
			try {
				Document doc = m_editor.getDocument();
				doc.remove(m_partialToken.getDocumentPos(), m_partialToken.getToken().length());
				doc.insertString(m_partialToken.getDocumentPos(), presult.completion, null);
				if (presult.matches <= 1)
					m_completion.setPaneVisible(false); // we're done
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
