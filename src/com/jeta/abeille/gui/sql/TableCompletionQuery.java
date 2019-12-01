package com.jeta.abeille.gui.sql;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.ext.CompletionQuery;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.gui.common.TableSelectorModel;

/**
 * Table completion support finder
 * 
 * @author Jeff Tassin
 */
public class TableCompletionQuery implements CompletionQuery {
	/** The completion object that created this object */
	private SQLCompletion m_completion;

	/** This is the model of tables that we are allowed to provide completion on */
	private TableSelectorModel m_model;

	/**
	 * ctor
	 */
	public TableCompletionQuery(SQLCompletion completion, TableSelectorModel model) {

		m_completion = completion;
		m_model = model;
	}

	/**
	 * Builds the list of items to be displayed in the popup. This is simply the
	 * list of tables in the database.
	 * 
	 * @param processor
	 *            the token processor that contains the parsed tokens that we
	 *            need to analyze
	 */
	private TableCompletionResult buildCompletionResult(JTextComponent component, int offset,
			SQLTokenProcessor processor) {
		SQLToken tablenametok = null; // this can be partially completed

		TokenID lasttoken = null;
		int count = 0;
		LinkedList list = processor.getTokens();
		ListIterator iter = list.listIterator(list.size());
		while (iter.hasPrevious()) {
			SQLToken token = (SQLToken) iter.previous();

			TokenID tokenid = token.getTokenID();
			if (tokenid == SQLTokenContext.DOT) {
				if (count == 0 || (count == 1 && lasttoken == SQLTokenContext.IDENTIFIER))
					lasttoken = tokenid;
				else
					break; // invalid state
			} else if (tokenid == SQLTokenContext.IDENTIFIER) {
				if (count == 0) {
					tablenametok = token;
					lasttoken = tokenid;
				} else {
					break;
				}
			} else if (tokenid == SQLTokenContext.WHITESPACE) {
				break;
			}
			count++;
		}

		if (tablenametok == null)
			return new TableCompletionResult(component, m_completion, m_model, new SQLToken(offset));
		else
			return new TableCompletionResult(component, m_completion, m_model, tablenametok);
	}

	public CompletionQuery.Result query(JTextComponent component, int offset, SyntaxSupport support) {
		return query(component, offset, support, false);
	}

	/**
	 * Perform the query on the given component. The query usually gets the
	 * component's document, the caret position and searches back to find the
	 * last command start. Then it inspects the text up to the caret position
	 * and returns the result.
	 * 
	 * @param component
	 *            the component to use in this query.
	 * @param offset
	 *            position in the component's document to which the query will
	 *            be performed. Usually it's a caret position.
	 * @param support
	 *            syntax-support that will be used during resolving of the
	 *            query.
	 * @param sourceHelp
	 *            whether the help is retrieved to open the source file. The
	 *            query behavior is slightly modified if this flag is true
	 * @return result of the query or null if there's no result.
	 */
	public CompletionQuery.Result query(JTextComponent component, int offset, SyntaxSupport support, boolean sourceHelp) {
		BaseDocument doc = (BaseDocument) component.getDocument();
		SQLSyntaxSupport sup = (SQLSyntaxSupport) support.get(SQLSyntaxSupport.class);
		TableCompletionResult result = null;

		try {
			// find last separator position
			int lastSepOffset = sup.getLastCommandSeparator(offset);

			SQLTokenProcessor processor = new SQLTokenProcessor();
			sup.tokenizeText(processor, lastSepOffset + 1, offset, true);
			result = buildCompletionResult(component, offset, processor);

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		return result;
	}

}
