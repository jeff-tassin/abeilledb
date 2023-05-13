package com.jeta.abeille.gui.login.help;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.io.File;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This view displays default JDBC configuration information for a given
 * database: driver, port, URL, JAR file
 * 
 * @author Jeff Tassin
 */
public class ConnectionHelpView extends TSPanel {
	/**
	 * ctor
	 * 
	 * @param help_resource
	 *            a path to an HTML file that contains help information.
	 */
	public ConnectionHelpView(String help_resource) {
		setLayout(new BorderLayout());
		try {
			JEditorPane editor = new JEditorPane();
			editor.setEditorKit(new HTMLEditorKit());

			URL url = ClassLoader.getSystemResource(help_resource);
			editor.setPage(url);
			JScrollPane scroll = new JScrollPane(editor);
			add(scroll, BorderLayout.CENTER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(10, 14);
	}
}
