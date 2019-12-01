package com.jeta.abeille.gui.importer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.LinkedList;
import java.util.Iterator;

public class CountingColumnHandler implements ColumnHandler {
	private String m_mask;
	private LinkedList m_expressions = new LinkedList();
	private String m_tag = "@COUNT";
	private int m_offset = 0;

	public CountingColumnHandler(String mask) {
		m_mask = mask;
		try {
			Pattern pattern = Pattern.compile("@COUNT\\(((\\d+))\\)");
			Matcher matcher = pattern.matcher(mask);
			if (matcher.find()) {
				String pre = mask.substring(0, matcher.start());
				String post = mask.substring(matcher.end(), mask.length());

				System.out.println("pre = " + pre + "  post = " + post);

				if (pre != null && pre.length() > 0)
					m_expressions.add(pre);

				m_expressions.add(m_tag);

				if (post != null && post.length() > 0)
					m_expressions.add(post);

				m_offset = Integer.parseInt(matcher.group(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return the output for this handler
	 */
	public String getOutput(int row) {

		StringBuffer result = new StringBuffer();
		Iterator iter = m_expressions.iterator();
		while (iter.hasNext()) {
			String val = (String) iter.next();
			if (val == m_tag)
				result.append(String.valueOf(m_offset + row));
			else
				result.append(val);
		}
		return result.toString();
	}
}
