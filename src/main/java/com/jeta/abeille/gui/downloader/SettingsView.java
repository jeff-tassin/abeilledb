package com.jeta.abeille.gui.downloader;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.layouts.ColumnLayout;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This view allows the user to edit download settings
 * 
 * @author Jeff Tassin
 */
public class SettingsView extends TSPanel {

	private JTextField m_server;

	private JTextField m_port;

	private JCheckBox m_useproxy;

	/** component ids */
	public static final String ID_USE_PROXY = "download.settings.useproxy.box";
	public static final String ID_SERVER = "download.settings.server";
	public static final String ID_PORT = "download.settings.port";

	/**
	 * ctor
	 */
	public SettingsView() {
		setLayout(new BorderLayout());
		add(createComponents(), BorderLayout.NORTH);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10),
				BorderFactory.createTitledBorder(I18N.getLocalizedMessage("Network Settings"))));

		load();
	}

	/**
	 * Creates the panel that contains the gui components
	 */
	private JPanel createComponents() {

		m_useproxy = TSGuiToolbox.createCheckBox(I18N.getLocalizedMessage("Use HTTP Proxy"));
		m_useproxy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_server.setEnabled(m_useproxy.isSelected());
				m_port.setEnabled(m_useproxy.isSelected());
			}
		});

		m_server = new JTextField(15);
		m_server.setName(ID_SERVER);
		m_server.setEnabled(false);

		m_port = TSGuiToolbox.createIntegerTextField(6);
		m_port.setName(ID_PORT);
		m_port.setEnabled(false);

		JPanel opts_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		opts_panel.add(new JLabel(I18N.getLocalizedDialogLabel("Server")));
		opts_panel.add(Box.createHorizontalStrut(5));
		opts_panel.add(m_server);
		opts_panel.add(Box.createHorizontalStrut(10));
		opts_panel.add(new JLabel(I18N.getLocalizedDialogLabel("Port")));
		opts_panel.add(Box.createHorizontalStrut(5));
		opts_panel.add(m_port);

		JPanel panel = new JPanel(new ColumnLayout());
		panel.add(m_useproxy);
		panel.add(Box.createVerticalStrut(10));
		panel.add(opts_panel);

		panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 5));
		return panel;
	}

	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(7, 7);
	}

	public void load() {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		String use_proxy = userprops.getProperty(DownloadNames.ID_USE_PROXY, "false");
		m_useproxy.setSelected(Boolean.valueOf(use_proxy).booleanValue());
		m_server.setText(userprops.getProperty(DownloadNames.ID_SERVER, ""));
		m_port.setText(userprops.getProperty(DownloadNames.ID_PORT, ""));
		setEnabled(m_useproxy.isSelected());
		m_useproxy.setEnabled(true);
	}

	public void save() {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		userprops.setProperty(DownloadNames.ID_USE_PROXY, String.valueOf(m_useproxy.isSelected()));
		userprops.setProperty(DownloadNames.ID_PORT, TSUtils.fastTrim(m_port.getText()));
		userprops.setProperty(DownloadNames.ID_SERVER, TSUtils.fastTrim(m_server.getText()));

		Properties prop = System.getProperties();
		prop.put("http.proxySet", String.valueOf(m_useproxy.isSelected()));
		if (m_useproxy.isSelected()) {
			prop.put("http.proxyHost", TSUtils.fastTrim(m_server.getText()));
			prop.put("http.proxyPort", TSUtils.fastTrim(m_port.getText()));
		} else {
			prop.remove("http.proxyHost");
			prop.remove("http.proxyPort");
		}
	}

	public void setEnabled(boolean benabled) {
		m_server.setEnabled(benabled);
		m_port.setEnabled(benabled);
		m_useproxy.setEnabled(benabled);
	}

}
