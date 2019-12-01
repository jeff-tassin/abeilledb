package com.jeta.abeille.gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.interfaces.license.LicenseUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This view shows the about information for the product.
 * 
 * @author Jeff Tassin
 */
public class AboutView extends TSPanel {
	/** data model for this view */
	private AboutModel m_model;

	/** size of the splash screen image */
	private Dimension m_imagesize;

	/** the panel that contains the info labels */
	private JPanel m_infopanel;

	private JLabel m_image;

	private JLabel m_jetaware;

	/** about labels */
	private JLabel[] m_labels;

	/** about controls */
	private JComponent[] m_controls;

	private static Color m_jetablue = new Color(42, 61, 170);

	int Y_PADDING = 1;

	/**
	 * ctor
	 */
	public AboutView(AboutModel model) {
		m_model = model;
		createView();
		loadModel();
		revalidate();
		doLayout();
	}

	/**
	 * Creates the panel at the bottom of the view showing
	 * license/version/copyright information Licensed To: Serial Number:
	 * Version: www.jetaware.com Copyright 2002 Jeta Software Inc. Copyright �
	 * 2002 JETA Software Inc. All Rights Reserved
	 */
	private JPanel createInfoPanel() {
		JPanel panel = new InfoPanel(new InfoLayout());
		panel.setOpaque(true);
		panel.setBackground(Color.white);

		if (LicenseUtils.isEvaluation()) {
			m_labels = new JLabel[2];
			m_labels[0] = new JLabel(I18N.getLocalizedDialogLabel("License"));
			m_labels[1] = new JLabel(I18N.getLocalizedDialogLabel("Version"));

			m_controls = new JComponent[2];
			m_controls[0] = new JLabel(m_model.getLicenseType());
			m_controls[1] = new JLabel(m_model.getVersion());
		} else {
			m_labels = new JLabel[4];
			m_labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Licensed To"));
			m_labels[1] = new JLabel(I18N.getLocalizedDialogLabel("Serial Number"));
			m_labels[2] = new JLabel(I18N.getLocalizedDialogLabel("License"));
			m_labels[3] = new JLabel(I18N.getLocalizedDialogLabel("Version"));

			JLabel serial_no = new JLabel(m_model.getSerialNumber());

			m_controls = new JComponent[4];
			m_controls[0] = new JLabel(m_model.getLicensee());
			m_controls[1] = serial_no;
			m_controls[2] = new JLabel(m_model.getLicenseType());
			m_controls[3] = new JLabel(m_model.getVersion());
		}

		java.awt.Font f = javax.swing.UIManager.getFont("Table.font");
		for (int index = 0; index < m_labels.length; index++) {
			m_labels[index].setFont(f);
			m_controls[index].setFont(f);
			panel.add(m_labels[index]);
			panel.add(m_controls[index]);
		}

		panel.doLayout();
		return panel;
	}

	/**
	 * Creates the panel that contains the main image for the about
	 * 
	 * @return the panel
	 */
	private void createView() {
		ImageIcon icon = TSGuiToolbox.loadImage("jeta_about.png");
		m_imagesize = new Dimension(icon.getIconWidth(), icon.getIconHeight());
		setLayout(new AboutViewLayout());
		m_image = new JLabel(icon);
		m_image.setBorder(BorderFactory.createLineBorder(Color.black));

		add(m_image);
		m_infopanel = createInfoPanel();
		add(m_infopanel);

		ImageIcon bottomicon = TSGuiToolbox.loadImage("about_bottom.png");
		m_jetaware = new JLabel(bottomicon);
		m_jetaware.setBorder(BorderFactory.createLineBorder(Color.black));

		m_jetaware.setSize(new Dimension(400, 24));
		add(m_jetaware);

		setOpaque(true);
		setBackground(m_jetablue);
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		Dimension d = new Dimension(m_imagesize);
		d.height = d.height + m_infopanel.getPreferredSize().height + m_jetaware.getPreferredSize().height;
		// d.height = d.height + m_jetaware.getY() + m_jetaware.getHeight() +
		// 10;
		return d;
	}

	/**
	 * Loads the datamodel into the view
	 */
	private void loadModel() {

	}

	public class AboutViewLayout implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			m_image.setSize(new Dimension(parent.getWidth(), m_imagesize.height));
			// int x = (parent.getWidth() - m_imagesize.width)/2;
			// if ( x < 0 )
			int x = 0;
			m_image.setLocation(x, 0);

			m_infopanel.setLocation(0, m_image.getHeight());
			m_infopanel.setSize(new Dimension(parent.getWidth(), m_infopanel.getPreferredSize().height));

			// Font f = m_jetaware.getFont();
			// FontMetrics metrics = m_jetaware.getFontMetrics(f);

			// int jware_height = metrics.getHeight() + Y_PADDING + 10;
			int jware_height = m_jetaware.getHeight();
			m_jetaware.setLocation(0, m_infopanel.getY() + m_infopanel.getHeight());
			m_jetaware.setSize(new java.awt.Dimension(parent.getWidth(), jware_height));
			m_jetaware.setPreferredSize(m_jetaware.getSize());
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(50, 50);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return getPreferredSize();
		}

		public void removeLayoutComponent(Component comp) {
		}
	}

	public class InfoLayout implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			int max_label_width = 0;

			Font f = m_labels[0].getFont();
			FontMetrics metrics = m_labels[0].getFontMetrics(f);

			int x = 10;
			int y = 10;
			for (int index = 0; index < m_labels.length; index++) {
				JLabel label = m_labels[index];
				label.setSize(label.getPreferredSize());

				int label_width = metrics.stringWidth(label.getText());
				if (label_width > max_label_width)
					max_label_width = label_width;

				label.setLocation(x, y);
				JComponent comp = m_controls[index];
				comp.setLocation(x, y);
				y += label.getHeight() + Y_PADDING;

			}

			max_label_width += 20;
			for (int index = 0; index < m_labels.length; index++) {
				JComponent comp = m_controls[index];
				int comp_width = parent.getWidth() - max_label_width - 10;
				comp.setSize(new Dimension(comp_width, m_controls[0].getPreferredSize().height));
				comp.setLocation(max_label_width, comp.getY());
			}
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(50, 50);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return getPreferredSize();
		}

		public void removeLayoutComponent(Component comp) {
		}
	}

	public class InfoPanel extends JPanel {
		public InfoPanel(LayoutManager layout) {
			super(layout);
		}

		public Dimension getPreferredSize() {
			JLabel label = m_labels[0];
			return new Dimension(100, label.getHeight() * (m_labels.length + 1) + 10);
		}
	}
}
