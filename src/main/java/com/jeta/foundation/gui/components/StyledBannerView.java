/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.utils.TSUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This view contains a styled text area and a JPanel container arranged as
 * follows:
 * 
 * [ styled text area ] [ question icon][ JPanel ]
 * 
 * The text area is used to display a help message that is relevant to the
 * JPanel we are displaying. The user provides the message to display as well as
 * the JPanel.
 * 
 * @author Jeff Tassin
 */
public class StyledBannerView extends TSPanel {
	public StyledBannerView(Collection paragraphs, JPanel content) {
		// Create a reusable CellConstraints instance.
		CellConstraints cc = new CellConstraints();

		int top_sz = paragraphs.size() * 50 / 4;
		StringBuffer row_spec = new StringBuffer();
		row_spec.append("fill:");
		row_spec.append(top_sz);
		row_spec.append("dlu, 3px, fill:pref, 10px");

		// FormLayout layout = new FormLayout( "pref:grow",
		// "fill:50dlu, 3px, fill:pref, 10px" );

		FormLayout layout = new FormLayout("pref:grow", row_spec.toString());

		setLayout(layout);

		add(buildTextPanel(paragraphs), cc.xy(1, 1));
		add(TSGuiToolbox.createSeparator(null), cc.xy(1, 2));
		add(createBottomPanel(content), cc.xy(1, 3));
	}

	private JComponent buildTextPanel(Collection banner) {
		StyleContext sc = new StyleContext();
		DefaultStyledDocument doc = new DefaultStyledDocument(sc);
		BannerStyles bs = new BannerStyles(doc, sc, banner);
		JTextPane p = new MyTextPane(doc);
		p.setEditable(false);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(p, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Creates the panel at the bottom of the view
	 */
	protected JPanel createBottomPanel(JPanel content) {
		JPanel panel = new JPanel();
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("10px, pref, 12px, left:pref:grow, 12dlu", "top:10dlu, pref");

		panel.setLayout(layout);

		JLabel label = new JLabel();
		label.setIcon(javax.swing.UIManager.getIcon("OptionPane.questionIcon"));
		panel.add(label, cc.xy(2, 2, "l,t"));
		panel.add(content, cc.xy(4, 2));
		return panel;
	}

	private static Paragraph[] PARAS_TYPE = new Paragraph[0];

	/**
	 * Creates a Paragraph array from a text string that separates each line
	 * with a \n
	 * 
	 * @return a Collection of Paragraph objects.
	 */
	public static Collection createParagraphs(String logical, String pText) {
		ArrayList paragraphs = new ArrayList();
		java.util.StringTokenizer st = new java.util.StringTokenizer(pText, "\n");
		while (st.hasMoreTokens()) {
			String runtext = st.nextToken();
			Run run = new Run("none", runtext);
			Paragraph p = new Paragraph(logical, run);
			paragraphs.add(p);
		}
		return paragraphs;
	}

	/**
	 * Override JTextPane so we can prevent sizing too small so that the text
	 * word-wraps.
	 */
	public static class MyTextPane extends JTextPane {
		public MyTextPane(DefaultStyledDocument doc) {
			super(doc);
		}

		public void setBounds(int x, int y, int width, int height) {
			Dimension size = this.getPreferredSize();
			super.setBounds(x, y, Math.max(size.width, width), height);
		}
	}

	public static class BannerStyles {
		private StyleContext m_styles;
		private Hashtable m_runattr;

		BannerStyles(DefaultStyledDocument doc, StyleContext styles, Collection data) {
			m_styles = styles;
			m_runattr = new Hashtable();

			createStyles();
			Iterator iter = data.iterator();
			while (iter.hasNext()) {
				Paragraph p = (Paragraph) iter.next();
				addParagraph(doc, p);
			}
		}

		void addParagraph(DefaultStyledDocument doc, Paragraph p) {
			try {
				Style s = null;
				for (int i = 0; i < p.data.length; i++) {
					Run run = p.data[i];
					s = (Style) m_runattr.get(run.attr);
					doc.insertString(doc.getLength(), run.content, s);
				}

				// set logical style
				Style ls = m_styles.getStyle(p.logical);
				doc.setLogicalStyle(doc.getLength() - 1, ls);
				doc.insertString(doc.getLength(), "\n", null);
			} catch (BadLocationException e) {
				TSUtils.printException(e);
			}
		}

		void createStyles() {
			Style def = m_styles.getStyle(StyleContext.DEFAULT_STYLE);
			StyleConstants.setFontFamily(def, "SansSerif");

			// Title
			Style sty = m_styles.addStyle("title", def);
			StyleConstants.setBold(sty, true);

			// normal
			sty = m_styles.addStyle("normal", def);
			StyleConstants.setLeftIndent(sty, 10);
			StyleConstants.setRightIndent(sty, 10);
			StyleConstants.setFontFamily(sty, "SansSerif");
			StyleConstants.setSpaceAbove(sty, 0);
			StyleConstants.setSpaceBelow(sty, 0);
		}
	}

	public static class Paragraph {
		public Paragraph(String logical, Run run) {
			this.logical = logical;
			data = new Run[1];
			data[0] = run;
		}

		public Paragraph(String logical, Run[] data) {
			this.logical = logical;
			this.data = data;
		}

		String logical;
		Run[] data;
	}

	public static class Run {
		public Run(String attr, String content) {
			this.attr = attr;
			this.content = content;
		}

		String attr;
		String content;
	}
}
