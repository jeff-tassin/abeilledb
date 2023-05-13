package com.jeta.abeille.gui.model.export;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;

import com.jeta.foundation.gui.components.TextFieldwButtonPanel;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.layouts.TableLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;
import com.jeta.foundation.utils.TSUtils;

/**
 * Displays options for saving the ModelView to an image file. Currently, we
 * support SVG, JPEG, and PNG image formats.
 * 
 * @author Jeff Tassin
 */
public class SaveOptionsView extends TSPanel {
	private JRadioButton m_svg_radio;
	private JRadioButton m_png_radio;
	private JRadioButton m_jpg_radio;
	private JSlider m_jpg_slider;
	private JLabel m_jpg_quality_label;
	private JTextField m_file_field;

	public static final String IMAGE_FORMAT = "modelview.save.image.format";
	public static final String JPEG_QUALITY = "modelview.save.jpg.quality";
	public static final String FILE_NAME = "modelview.save.file.name";

	public SaveOptionsView() {
		createView();
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		loadSettings();
	}

	/**
	 * Creates the components for this view
	 */
	private void createView() {
		setLayout(new BorderLayout());
		add(createOptionsPanel(), BorderLayout.NORTH);
		add(createOutputPanel(), BorderLayout.SOUTH);
	}

	/**
	 * Creates the options panel
	 */
	private JPanel createOptionsPanel() {
		/** 3 rows x 6 columns */
		double size[][] = { { TableLayout.PREFERRED, 30, TableLayout.PREFERRED },
				{ TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED } };

		JPanel panel = new JPanel();
		panel.setLayout(new TableLayout(size));

		m_svg_radio = TSGuiToolbox.createRadioButton(I18N.getLocalizedMessage("SVG"));
		m_png_radio = TSGuiToolbox.createRadioButton(I18N.getLocalizedMessage("PNG"));
		m_jpg_radio = TSGuiToolbox.createRadioButton(I18N.getLocalizedMessage("JPEG"));

		ButtonGroup grp = new ButtonGroup();
		grp.add(m_svg_radio);
		grp.add(m_png_radio);
		grp.add(m_jpg_radio);

		ActionListener format_listener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_jpg_slider.setEnabled(m_jpg_radio.isSelected());
				m_jpg_quality_label.setEnabled(m_jpg_radio.isSelected());
			}
		};

		m_svg_radio.addActionListener(format_listener);
		m_png_radio.addActionListener(format_listener);
		m_jpg_radio.addActionListener(format_listener);

		JPanel q_panel = new JPanel();
		q_panel.setLayout(new BoxLayout(q_panel, BoxLayout.X_AXIS));
		m_jpg_quality_label = new JLabel(I18N.getLocalizedDialogLabel("Quality"));
		q_panel.add(m_jpg_quality_label);
		q_panel.add(Box.createHorizontalStrut(5));

		m_jpg_slider = new JSlider(0, 100);
		m_jpg_slider.setValue(80);
		m_jpg_slider.setPaintLabels(true);
		m_jpg_slider.setPaintTicks(true);
		m_jpg_slider.setMajorTickSpacing(25);
		m_jpg_slider.setEnabled(false);
		m_jpg_quality_label.setEnabled(false);

		q_panel.add(m_jpg_slider);

		panel.add(m_svg_radio, "0,0");
		panel.add(m_png_radio, "0,2");
		panel.add(m_jpg_radio, "0,4");

		panel.add(q_panel, "2,4");
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(I18N.getLocalizedMessage("Options")),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		return panel;
	}

	/**
	 * Create the panel that contains the file field.
	 */
	private JPanel createOutputPanel() {
		/** 3 rows x 6 columns */
		double size[][] = { { TableLayout.PREFERRED, 10, TableLayout.FILL }, { TableLayout.PREFERRED } };

		JPanel panel = new JPanel();
		panel.setLayout(new TableLayout(size));

		TextFieldwButtonPanel file_panel = new TextFieldwButtonPanel(TSGuiToolbox.loadImage("ellipsis16.gif"));
		m_file_field = file_panel.getTextField();
		JButton f_btn = file_panel.getButton();
		f_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				editFileLocation();
			}
		});

		panel.add(new JLabel(I18N.getLocalizedDialogLabel("File")), "0,0");
		panel.add(file_panel, "2,0");
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(I18N.getLocalizedMessage("Output")),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		return panel;
	}

	/**
	 * Invokes a file chooser dialog that allows the user to set the file name
	 */
	private void editFileLocation() {
		String current_file = getPath();
		try {
			if (current_file.length() > 0) {
				File path = new File(current_file);
				if (!path.isDirectory()) {
					current_file = TSUtils.fastTrim(path.getParent());
				}
			}
		} catch (Exception e) {

		}

		File f = null;
		JFileChooser chooser = null;
		if (current_file.length() > 0) {
			chooser = new JFileChooser(current_file);
			int returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				f = chooser.getSelectedFile();
			}
		} else {
			f = com.jeta.foundation.gui.filechooser.TSFileChooserFactory.showOpenDialog();
		}

		if (f != null) {
			String path = f.getPath();
			String ext = ".svg";
			if (isJPEG()) {
				ext = ".jpg";
			} else if (isPNG()) {
				ext = ".png";
			}

			/** make sure user provided the proper extension */
			String test_path = path.toLowerCase();
			int pos = test_path.lastIndexOf(ext);
			if (pos != test_path.length() - 4) {
				path += ext;
			}

			m_file_field.setText(path);
		}
	}

	/**
	 * @return the path entered in the file name field (includes the file name)
	 */
	public String getPath() {
		return TSUtils.fastTrim(m_file_field.getText());
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(6, 12);
	}

	/**
	 * @return the JPEG output quality
	 */
	public float getQuality() {
		float s_val = (float) m_jpg_slider.getValue();
		return s_val / 100.0f;
	}

	/**
	 * @return true if the image format is JPEG
	 */
	public boolean isJPEG() {
		return m_jpg_radio.isSelected();
	}

	/**
	 * @return true if the image format is PNG
	 */
	public boolean isPNG() {
		return m_png_radio.isSelected();
	}

	/**
	 * @return true if the image format is SVG
	 */
	public boolean isSVG() {
		return m_svg_radio.isSelected();
	}

	/**
	 * Loads the settings from the user properties store
	 */
	public void loadSettings() {
		m_png_radio.setSelected(false);
		m_jpg_radio.setSelected(false);
		m_svg_radio.setSelected(false);

		String format = TSUserPropertiesUtils.getString(IMAGE_FORMAT, "svg");
		if (format.equals("png"))
			m_png_radio.setSelected(true);
		else if (format.equals("jpeg")) {
			m_jpg_radio.setSelected(true);
			m_jpg_slider.setEnabled(true);
			m_jpg_quality_label.setEnabled(true);
		} else
			m_svg_radio.setSelected(true);

		m_jpg_slider.setValue(TSUserPropertiesUtils.getInteger(JPEG_QUALITY, 80));
		m_file_field.setText(TSUserPropertiesUtils.getString(FILE_NAME, ""));
	}

	/**
	 * Saves the settings to the user properties store
	 */
	public void saveSettings() {
		if (isSVG()) {
			TSUserPropertiesUtils.setString(IMAGE_FORMAT, "svg");
		} else if (isPNG()) {
			TSUserPropertiesUtils.setString(IMAGE_FORMAT, "png");
		} else if (isJPEG()) {
			TSUserPropertiesUtils.setString(IMAGE_FORMAT, "jpeg");
		}

		TSUserPropertiesUtils.setInteger(JPEG_QUALITY, m_jpg_slider.getValue());
		TSUserPropertiesUtils.setString(FILE_NAME, TSUtils.fastTrim(m_file_field.getText()));
	}
}
