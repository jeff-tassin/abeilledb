package com.jeta.abeille.gui.main.options;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

class SearchPathStyles {
	DefaultStyledDocument doc;
	StyleContext styles;
	Hashtable runAttr;

	SearchPathStyles(DefaultStyledDocument doc, StyleContext styles) {
		this.doc = doc;
		this.styles = styles;
		runAttr = new Hashtable();
	}

	void loadDocument() {
		createStyles();
		for (int i = 0; i < data.length; i++) {
			Paragraph p = data[i];
			addParagraph(p);
		}
	}

	void addParagraph(Paragraph p) {
		try {
			Style s = null;
			for (int i = 0; i < p.data.length; i++) {
				Run run = p.data[i];
				s = (Style) runAttr.get(run.attr);
				doc.insertString(doc.getLength(), run.content, s);
			}

			// set logical style
			Style ls = styles.getStyle(p.logical);
			doc.setLogicalStyle(doc.getLength() - 1, ls);
			doc.insertString(doc.getLength(), "\n", null);
		} catch (BadLocationException e) {
			System.err.println("Internal error: " + e);
		}
	}

	void createStyles() {
		// no attributes defined
		Style s = styles.addStyle(null, null);
		runAttr.put("none", s);
		// s = styles.addStyle(null, null);
		// StyleConstants.setItalic(s, true);
		// StyleConstants.setForeground(s, new Color(153,153,102));
		// runAttr.put("cquote", s); // catepillar quote

		// s = styles.addStyle(null, null);
		// StyleConstants.setItalic(s, true);
		// StyleConstants.setForeground(s, new Color(51,102,153));
		// runAttr.put("aquote", s); // alice quote

		Style def = styles.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(def, "SansSerif");

		// Title
		Style sty = styles.addStyle("title", def);
		StyleConstants.setBold(sty, true);

		// normal
		sty = styles.addStyle("normal", def);
		StyleConstants.setLeftIndent(sty, 10);
		StyleConstants.setRightIndent(sty, 10);
		StyleConstants.setFontFamily(sty, "SansSerif");
		StyleConstants.setSpaceAbove(sty, 0);
		StyleConstants.setSpaceBelow(sty, 0);
	}

	static class Paragraph {
		Paragraph(String logical, Run[] data) {
			this.logical = logical;
			this.data = data;
		}

		String logical;
		Run[] data;
	}

	static class Run {
		Run(String attr, String content) {
			this.attr = attr;
			this.content = content;
		}

		String attr;
		String content;
	}

	Paragraph[] data = new Paragraph[] {
			new Paragraph("title", new Run[] { new Run("none", "Search Path:") }),
			new Paragraph("normal",
					new Run[] { new Run("none", "Defines the path (catalog.schema) used by the table") }),
			new Paragraph("normal",
					new Run[] { new Run("none", "properties dialog and SQL completion. This does not") }),
			new Paragraph("normal", new Run[] { new Run("none", "set the current schema in the database.") }), };
}
