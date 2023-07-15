package com.jeta.abeille.gui.sql;

import com.jeta.abeille.gui.common.MetaDataPopupRenderer;
import com.jeta.foundation.gui.components.PopupList;
import com.jeta.foundation.gui.components.SortedListModel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.open.gui.utils.JETAToolbox;
import org.netbeans.editor.ext.CompletionPane;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.CompletionView;
import org.netbeans.editor.ext.ExtEditorUI;

import javax.swing.*;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * This is the popup for handling SQL completions This class implements both the
 * Pane and View interfaces. It simply delegates the calls to our PopupList
 * object
 * 
 * @author Jeff Tassin
 */
public class SQLCompletionPopup implements CompletionView, CompletionPane {
	private PopupList m_popup = new PopupList();

	private ExtEditorUI m_exteditorUI; // the UI for the editor

	private SQLCompletion m_completion;

	/**
	 * ctor
	 */
	public SQLCompletionPopup(ExtEditorUI exteditorUI, SQLCompletion completion) {
		m_exteditorUI = exteditorUI;
		m_completion = completion;

		if (JETAToolbox.isOSX()) {
			m_popup.setFocusable(false);
		}

		m_popup.setRenderer(MetaDataPopupRenderer.createRenderer(completion.getConnection()));
		SortedListModel listmodel = new SortedListModel();
		m_popup.setModel(listmodel);

		m_popup.getList().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					SQLCompletionQuery query = (SQLCompletionQuery) m_completion.getQuery();
					SQLCompletionResult result = query.getLastResult();
					if (result != null) {
						JTextComponent editor = m_exteditorUI.getComponent();
						Document doc = editor.getDocument();
						SQLToken partial = result.getPartialCompletion();
						if (partial == null) {
						} else {
							doc.remove(partial.getDocumentPos(), partial.getToken().length());
							doc.insertString(partial.getDocumentPos(), m_popup.getSelectedText(), null);
						}
					}
				} catch (Exception ex) {
					TSUtils.printException(ex);
				}
				setVisible(false);
			}
		});

	}

	/**
	 * Move the selected index down by one. This typically gets called when the
	 * user pressed the up arrow key CompletionView implementation
	 */
	public void down() {
		int lastInd = getModel().getSize() - 1;
		if (lastInd >= 0) {
			JList list = getList();
			list.setSelectedIndex(Math.min(getSelectedIndex() + 1, lastInd));
			list.ensureIndexIsVisible(getSelectedIndex());
		}
	}

	/**
	 * Go to the first item in the data item list. The <tt>getSelectedIndex</tt>
	 * must reflect the change. CompletionView implementation
	 */
	public void begin() {
		if (getModel().getSize() > 0) {
			JList list = getList();
			list.setSelectedIndex(0);
			list.ensureIndexIsVisible(0);
		}
	}

	/**
	 * Go to the last item in the data item list. The <tt>getSelectedIndex</tt>
	 * must reflect the change. CompletionView implementation
	 */
	public void end() {
		int lastInd = getModel().getSize() - 1;
		if (lastInd >= 0) {
			JList list = getList();
			list.setSelectedIndex(lastInd);
			list.ensureIndexIsVisible(lastInd);
		}
	}

	/**
	 * @return the underlying text editor component
	 */
	public JTextComponent getEditor() {
		return m_exteditorUI.getComponent();
	}

	/**
	 * @return the underlying JList component for this popup
	 */
	JList getList() {
		return m_popup.getList();
	}

	/**
	 * @return the popup list
	 */
	public PopupList getPopup() {
		return m_popup;
	}

	/**
	 * @return the data model for the popup
	 */
	SortedListModel getModel() {
		return m_popup.getModel();
	}

	/**
	 * Get the index of the currently selected item. CompletionView
	 * implementation
	 */
	public int getSelectedIndex() {
		return getList().getSelectedIndex();
	}

	/**
	 * CompletionPane implementation
	 * 
	 * @return true if the popup is visible
	 */
	public boolean isVisible() {
		return m_popup.isVisible();
	}

	/**
	 * Go up one page in the data item list. The <tt>getSelectedIndex</tt> must
	 * reflect the change. CompletionView implementation
	 */
	public void pageUp() {
		if (getModel().getSize() > 0) {
			JList list = getList();
			int pageSize = Math.max(list.getLastVisibleIndex() - list.getFirstVisibleIndex(), 0);
			int firstInd = Math.max(list.getFirstVisibleIndex() - pageSize, 0);
			int ind = Math.max(list.getSelectedIndex() - pageSize, firstInd);

			list.ensureIndexIsVisible(firstInd);
			list.setSelectedIndex(ind);
			list.ensureIndexIsVisible(ind);
		}
	}

	/**
	 * Go down one page in the data item list. The <tt>getSelectedIndex</tt>
	 * must reflect the change. CompletionView implementation
	 */
	public void pageDown() {
		int lastInd = getModel().getSize() - 1;
		if (lastInd >= 0) {
			JList list = getList();
			int pageSize = Math.max(list.getLastVisibleIndex() - list.getFirstVisibleIndex(), 0);
			lastInd = Math.max(Math.min(list.getLastVisibleIndex() + pageSize, lastInd), 0);
			int ind = Math.max(Math.min(list.getSelectedIndex() + pageSize, lastInd), 0);

			list.ensureIndexIsVisible(lastInd);
			list.setSelectedIndex(ind);
			list.ensureIndexIsVisible(ind);
		}
	}

	/**
	 * Possibly refresh the look after either the view was changed or title was
	 * changed or both. CompletionPane implementation
	 */
	public void refresh() {

	}

	/**
	 * Popupulate the list with the given results CompletionView implementation
	 */
	public void setResult(CompletionQuery.Result result) {
		if (result != null) {
			SortedListModel model = m_popup.getModel();
			model.clear();
			List list = result.getData();
			model.addAll(list.toArray());

			SQLCompletionResult sqlresult = (SQLCompletionResult) result;
			SQLToken token = sqlresult.getPartialCompletion();
			if (token == null) {
				m_popup.selectText("");
			} else {
				m_popup.selectText(token.getToken());
			}

		}
	}

	/**
	 * CompletionPane implementation
	 */
	public void setTitle(String title) {

	}
	public void setVisible(boolean visible){
		// no-op
	}

	/**
	 * CompletionPane implementation Show/Hide the popup on the screen
	 */
	public void setVisible_back(boolean visible) {



		if (visible) {
			try {
				JTextComponent editor = getEditor();
				Caret caret = editor.getCaret();
				int startpos = caret.getDot();

				Rectangle r = editor.modelToView(startpos);
				Font f = editor.getFont();
				FontMetrics metrics = editor.getFontMetrics(f);
				int popx = r.x;
				int popy = r.y + metrics.getHeight();
				m_popup.show(editor, popx, popy);
				JList list = m_popup.getList();
				if (list.getModel().getSize() > 0) {
					list.setSelectedIndex(-1);
					list.ensureIndexIsVisible(0);
				}

				editor.requestFocus();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			JTextComponent editor = getEditor();
			// the lost focus event on our focus listener will cause the popup
			// to hide itself
			// editor.requestFocus();
			m_popup.setVisible(false);

			try {

				/**
				 * This ugly piece of code is because of a focus problem after
				 * the popup is hidden. It seems the BasicPopupMenuUI sticks
				 * around. Any subsequent events such as a mouse press will
				 * cause the BasicPopupMenuUI to transfer focus back to the
				 * editor. So, if the user does a popup in the SQL editor and
				 * then presses the run button on the toolbar, the focus will be
				 * transfered back to the editor and the run button will never
				 * get the event. see:
				 * javax.swing.plaf.basic.BasicPopupMenuUI$MenuKeyboardHelper
				 * .stateChanged(BasicPopupMenuUI.java:857) We get around this
				 * by simulated a mouse press event after the popup is hidden
				 */
				Caret caret = editor.getCaret();
				int startpos = caret.getDot();
				Rectangle r = editor.modelToView(startpos);
				TSGuiToolbox.simulateMouseClick(editor, r.x, r.y);
			} catch (Exception e) {
				e.printStackTrace();
			}

			m_completion.reset();
		}
	}

	/**
	 * Move the selected index up by one This typically gets called when the
	 * user pressed the up arrow key CompletionView implementation
	 */
	public void up() {
		if (getModel().getSize() > 0) {
			JList list = getList();
			list.setSelectedIndex(getSelectedIndex() - 1);
			list.ensureIndexIsVisible(getSelectedIndex());
		}
	}

	public void ensureIndexIsVisible() {
		JList list = getList();
		int index = getSelectedIndex();
		if (index >= 0) {
			list.ensureIndexIsVisible(index);
		}
	}
}
