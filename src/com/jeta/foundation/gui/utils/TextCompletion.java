/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.utils;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class defines helper methods used by various completion classes
 * 
 * @author Jeff Tassin
 */
public class TextCompletion {

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
	public static int compareObject(Object obj, String txt) {
		return txt.compareToIgnoreCase(obj.toString());
	}

	/**
	 * Selects the specified text in the list. If there are common matches to
	 * the given text, then the common text is returned. For example, if the
	 * list contains java, javax, javay and the user types ja, then the result
	 * would be java
	 * 
	 * @param model
	 *            a sorted collection of objects to search
	 * @param text
	 *            the text to select
	 * 
	 */
	public static TextCompletion.Result selectCommonText(Collection model, String text) {
		if (text == null) {
			DefaultResult result = new DefaultResult(0, null);
			return result;
		}

		int count = 0;
		String common = null;
		Iterator iter = model.iterator();
		while (iter.hasNext()) {
			Object listobject = iter.next();
			String listitem = listobject.toString();
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

		Result result = new DefaultResult(count, common);
		return result;
	}

	/**
	 * Compares two strings and finds the common substring between the two
	 * strings. The caller can pass in the position to start the search if it is
	 * known that the strings match up to that position.
	 */
	public static String getCommonSubstring(String str1, String str2, int startPos) {
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
	 * This interface is used to define the results of a select text operation.
	 * The main information returned is whether there was a match and how many
	 * items in the collection matched the search. This is important because if
	 * only one item matches a search, then the caller can finish the completion
	 */
	public interface Result {
		/** return the number of matches */
		public int getNumberMatches();

		/** @return the extended completion string */
		public String getCompletion();
	}

	public static class DefaultResult implements Result {
		/** the number of matches in the result */
		private int m_matches;

		/** the completion */
		private String m_completion;

		/**
		 * ctor
		 */
		public DefaultResult(int matches, String completion) {
			m_matches = matches;
			m_completion = completion;
		}

		/** @return the number of matches */
		public int getNumberMatches() {
			return m_matches;
		}

		/** @return the extended completion string */
		public String getCompletion() {
			return m_completion;
		}

	}
}
