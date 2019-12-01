/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.text.*;

import java.lang.ref.WeakReference;

import com.jeta.open.gui.utils.JETAToolbox;

/**
 * This is the popup component for TSComboBoxes and popup lists.
 * 
 * @author Jeff Tassin
 */
public class PopupList extends JPopupMenu implements ListSelectionListener {
	private SortedListModel m_model;
	private JList m_list;
	private LinkedList m_listeners = new LinkedList();
	private boolean m_bforwardSelectionEvent;

	/**
	 * The caller can set a comparison function for the list if needed. The
	 * default is to use a case insensitive comparision on the obj.toString
	 * values
	 */
	private Comparator m_comparator;

	/**
	 * we keep a reference to the combo box (can be null) so that we can notify
	 * when the popup is hidden
	 */
	private WeakReference m_cboxref;

	/**
	 * ctor
	 */
	public PopupList() {
		m_model = new SortedListModel();
		setLayout(new BorderLayout());
		m_list = new JList(m_model);

		JScrollPane scrollpane = new JScrollPane(m_list);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollpane, BorderLayout.CENTER);
		setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

		Font f = m_list.getFont();
		FontMetrics metrics = m_list.getFontMetrics(f);
		Rectangle2D rect = metrics.getStringBounds("###########################", this.getGraphics());
		Dimension d = new Dimension(rect.getBounds().width, rect.getBounds().height * 10);
		setMaximumSize(d);
		setPreferredSize(d);
		setSize(d);
		m_bforwardSelectionEvent = true;
		m_list.addListSelectionListener(this);
	}

	/**
	 * ctor
	 */
	public PopupList(TSComboBox cbox) {
		this();
		m_cboxref = new WeakReference(cbox);
	}

	public void addListSelectionListener(ListSelectionListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Compares an item in the list to a given text string. This method
	 * (current) calls compareToIgnoreCase and returns the result.
	 * 
	 * @param obj
	 *            the object in the list to compare to
	 * @param txt
	 *            the text to check for
	 * @return the same as String.compareToIgnoreCase
	 */
	int compareObject(Object obj, String txt) {
		if (m_comparator == null) {
			if (obj == null || txt == null)
				return -1;

			return txt.compareToIgnoreCase(obj.toString());
		} else {
			return m_comparator.compare(txt, obj);
		}
	}

	/**
	 * Gets the item in the list that matches the given text.
	 * 
	 * @param txt
	 *            the text to search for in the list
	 * @return the object that matches the given text. If no item matches, null
	 *         is returned.
	 */
	public Object getItem(String txt) {
		for (int index = 0; index < m_model.getSize(); index++) {
			Object obj = m_model.getElementAt(index);
			if (compareObject(obj, txt) == 0)
				return obj;
		}
		return null;
	}

	/**
	 * @return the text that is 'displayed' at the given index. This is
	 *         important if objects are placed in the list that have toString()
	 *         results that are different than what is displayed (e.g. TableId
	 *         with schema )
	 */
	public String getListText(int index) {
		Object listobject = m_model.getElementAt(index);
		if (listobject == null) {
			return "";
		} else {
			String listitem = listobject.toString();
			ListCellRenderer renderer = m_list.getCellRenderer();
			if (renderer != null) {
				Component comp = renderer.getListCellRendererComponent(m_list, listobject, index, false, false);
				if (comp instanceof JLabel) {
					JLabel label = (JLabel) comp;
					listitem = label.getText();
				}
			}
			return listitem;
		}
	}

	/**
	 * @return the JList component that makes up this popup
	 */
	public JList getList() {
		return m_list;
	}

	/**
	 * @return the data model that is the basis for the list
	 */
	public SortedListModel getModel() {
		return m_model;
	}

	public Object getSelectedValue() {
		return m_list.getSelectedValue();
	}

	public String getSelectedText() {
		// String listitem = getListText( index );
		return getListText(m_list.getSelectedIndex());

		// Object obj = m_list.getSelectedValue();
		// if ( obj != null )
		// return obj.toString();
		// else
		// return "";
	}

	/**
	 * @return true if the given text uniquely matches the currently selected
	 *         item in the list
	 */
	public boolean isValid(String txt) {
		return (compareObject(m_list.getSelectedValue(), txt) == 0);
	}

	public void removeListSelectionListener(ListSelectionListener listener) {
		m_listeners.remove(listener);
	}

	public void setModel(SortedListModel model) {
		m_model = model;
		m_list.setModel(m_model);
	}

	public void setLocation(int x, int y) {
		super.setLocation(x, y);
	}

	public void setRenderer(ListCellRenderer renderer) {
		m_list.setCellRenderer(renderer);
	}

	/**
	 * Selects the specified text in the list. If there are common matches to
	 * the given text, then the common text is returned. For example, if the
	 * list contains java, javax, javay and the user types ja, then the result
	 * would be java
	 * 
	 * @param text
	 *            the text to select
	 * 
	 */
	public PopupList.Result selectCommonText(String text) {
		selectText(text);

		int count = 0;
		String common = null;
		int pos = m_list.getSelectedIndex();
		for (int index = pos; index < m_model.getSize(); index++) {
			Object listobject = m_model.getElementAt(index);
			String listitem = getListText(index);
			int cmp = compareObject(listobject, text);

			if (cmp < 0) {
				if (listitem.length() >= text.length() && listitem.regionMatches(true, 0, text, 0, text.length())) {
					count++;
					if (common == null)
						common = listitem;
					else
						common = getCommonSubstring(common, listitem, text.length());
				}
			} else if (cmp > 0) {
				// the text is greater than the item found in the this, so we
				// are done because the list is sorted
				break;
			} else {
				// if the text equals an item in the list, then we are done
				count++;
				common = listitem;
				break;
			}
		}

		if (common == null)
			common = text;

		Result result = new Result();
		result.matches = count;
		result.completion = common;
		return result;
	}

	/**
	 * Compares two strings and finds the common substring between the two
	 * strings. The caller can pass in the position to start the search if it is
	 * known that the strings match up to that position.
	 */
	public String getCommonSubstring(String str1, String str2, int startPos) {
		assert (str1.regionMatches(true, 0, str2, 0, startPos));

		// set to startPos - 1 to handle the case when only 1 character has been
		// typed
		int endpos = startPos - 1;

		int len = Math.min(str1.length(), str2.length());
		for (int index = startPos; index < len; index++) {
			if (str2.charAt(index) == str1.charAt(index))
				endpos = index;
			else
				break;
		}
		String result = str1.substring(0, endpos + 1);
		return result;
	}

	/**
	 * Helper method to get the parent frame window. This is either a JDialog,
	 * JInternalFrame, or JFrame
	 */
	Component getParentFrame(Component invoker) {
		Component obj = invoker;
		while (obj != null) {
			if (obj instanceof javax.swing.JDialog)
				return obj;

			if (obj instanceof javax.swing.JInternalFrame)
				return obj;

			if (obj instanceof javax.swing.JFrame)
				return obj;

			obj = obj.getParent();
		}
		return null;
	}

	/**
	 * Selects the specified text in the list. If the text is not found, the
	 * next closest match 'below' that text is highlighted gray.
	 * 
	 * @param text
	 *            the text to select
	 * 
	 */
	public String selectText(String text) {
		m_bforwardSelectionEvent = false;

		if (text == null || text.length() == 0) {
			m_list.clearSelection();
			m_bforwardSelectionEvent = true;
			return "";
		}

		if (m_model.getSize() == 0)
			return "";

		String result = "";
		boolean bfound = false;
		int pos = 0;
		int listcount = m_model.getSize();
		for (int index = 0; index < listcount; index++) {
			Object listobject = m_model.getElementAt(index);
			String listitem = getListText(index);

			int cmp = compareObject(listobject, text);

			if (cmp < 0) {
				pos = index;
				if (listitem.length() >= text.length()
						&& listitem.substring(0, text.length()).compareToIgnoreCase(text) == 0)
					bfound = true;
				break;
			} else if (cmp > 0) {
				pos = index;
			} else {
				pos = index;
				bfound = true;
				break;
			}
		}

		if (bfound) {
			// we have a match
			// m_list.setBackground( new Color( 204, 204, 255 ) );
			m_list.setSelectionBackground(Color.black);
			m_list.setSelectionForeground(Color.white);

			result = getListText(pos);
		} else {
			if (pos > 0 && pos != (listcount - 1))
				pos--;
			// no match
			// m_list.setBackground( new Color( 204, 204, 255 ) );
			m_list.setSelectionBackground(new Color(204, 204, 255));
			m_list.setSelectionForeground(Color.gray);
			result = text;
		}

		// we do this because we want items above and below the selected item
		// to be visible as well
		int toppos = pos - 2;
		if (toppos <= 0)
			toppos = 0;
		m_list.ensureIndexIsVisible(toppos);
		int bottompos = pos + m_list.getVisibleRowCount() - 3;
		if (bottompos >= m_model.getSize())
			bottompos = m_model.getSize() - 1;
		m_list.ensureIndexIsVisible(bottompos);

		m_list.setSelectedIndex(pos);
		m_list.ensureIndexIsVisible(pos);

		m_bforwardSelectionEvent = true;
		return result;
	}

	/**
	 * The caller can set a comparison function for the list if needed. The
	 * default is to use a case insensitive comparision on the obj.toString
	 * values
	 */
	public void setComparator(Comparator comp) {
		m_comparator = comp;
	}

	public void setVisible(boolean bvis) {
		boolean isvisible = isVisible();
		super.setVisible(bvis);
		if (!bvis && m_cboxref != null && isvisible) {
			TSComboBox cbox = (TSComboBox) m_cboxref.get();
			if (cbox != null) {
				cbox.notifyPopupHidden(this);
			}
		}

		if (!isvisible && bvis) {
			int selindex = m_list.getSelectedIndex();
			if (selindex < 0) {
				if (m_model.getSize() > 0) {
					m_list.ensureIndexIsVisible(0);
				}
			} else
				m_list.ensureIndexIsVisible(selindex);
		}

	}

	/**
	 * Override show. There seems to be a bug when we invoke the popup in a
	 * component that is owned by a JDialog. In this case, if we convert the
	 * point to dialog coords and then call show, it seems to work.
	 */
	public void show(Component invoker, int x, int y) {
		// @todo check this in the final JDK1.4 release
		Component obj = getParentFrame(invoker);
		if (obj instanceof JDialog) {
			java.awt.Point pt = javax.swing.SwingUtilities.convertPoint(invoker, x, y, obj);
			super.show(obj, pt.x, pt.y);
		} else {
			super.show(invoker, x, y);
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (m_bforwardSelectionEvent) {
			Iterator iter = m_listeners.iterator();
			while (iter.hasNext()) {
				ListSelectionListener listener = (ListSelectionListener) iter.next();
				listener.valueChanged(e);
			}
		}
	}

	public void updateUI() {
		javax.swing.plaf.basic.BasicPopupMenuUI bui = new javax.swing.plaf.basic.BasicPopupMenuUI() {
			protected void installListeners() {
				// ignore
			}
		};
		setUI(bui);
	}

	/**
	 * This class is used to define the results of a select text operation. The
	 * main information returned is whether there was a match and how many items
	 * in the popup matched the search. This is important because if only one
	 * item matches a search, then the caller can finish the completion and hide
	 * the popup
	 */
	public static class Result {
		/**
		 * The number of matches
		 */
		public int matches;

		/**
		 * The extended completion
		 */
		public String completion;

	}
}
