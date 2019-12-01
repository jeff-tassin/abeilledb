package com.jeta.abeille.gui.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.jeta.foundation.gui.components.DateMask;
import com.jeta.foundation.gui.components.TimeMask;
import com.jeta.foundation.gui.components.TimeNames;
import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;

import com.jeta.foundation.gui.filechooser.TSFileChooserNames;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;
import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * Shows the general preferences
 * 
 * @author Jeff Tassin
 */
public class GeneralPreferencesView extends TSPanel {
	/** the data store for our properties */
	private TSUserProperties m_userprops;

	private JComboBox m_datemask;
	private JComboBox m_timemask;

	/** component ids */
	public static final String ID_HOME_FIELD = "home.field";
	public static final String ID_STANDARD_FILE_DIALOG_BOX = "use.standard.file.dialog.box";
	public static final String ID_IGNORE_CASE_BOX = "file.dialog.ignore.case";
	public static final String ID_TIME_MASK = "time.mask";
	public static final String ID_DATE_MASK = "date.mask";

	/**
	 * ctor
	 */
	public GeneralPreferencesView(TSUserProperties userprops) {
		m_userprops = userprops;
		initialize();
		loadData();
		setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
		setController(new GeneralPreferencesController());
	}

	JComboBox createDateMaskCombo() {
		JComboBox cbox = new JComboBox();
		cbox.setEditable(true);
		cbox.addItem("<examples>");

		String[] items = { "MMM dd, yyyy", "MMM.dd.yyyy", "yyyy.dd.MM", "MM-dd-yyyy", "MM/dd/yyyy" };

		String defaultmask = DateMask.getDefaultMask();

		cbox.addItem(defaultmask);

		for (int index = 0; index < items.length; index++) {
			if (!defaultmask.equals(items[index]))
				cbox.addItem(items[index]);
		}

		cbox.getEditor().setItem(defaultmask);

		return cbox;
	}

	JComboBox createTimeMaskCombo() {
		JComboBox cbox = new JComboBox();
		cbox.setEditable(true);
		cbox.addItem("<examples>");

		String[] items = { "hh:mm:ss a", "HH:mm:ss", "hh mm ss a", "HH mm ss" };

		String defaultmask = TimeMask.getDefaultMask();

		cbox.addItem(defaultmask);

		for (int index = 0; index < items.length; index++) {
			if (!defaultmask.equals(items[index]))
				cbox.addItem(items[index]);
		}

		cbox.getEditor().setItem(defaultmask);

		return cbox;
	}

	/**
	 * Creates and initializes the components in the view.
	 */
	private void initialize() {
		setLayout(new BorderLayout());

		JLabel[] labels = new JLabel[4];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Home Directory"));
		labels[1] = new JLabel("");
		labels[2] = new JLabel(I18N.getLocalizedDialogLabel("Date Mask"));
		labels[3] = new JLabel(I18N.getLocalizedDialogLabel("Time Mask"));

		JTextField homefield = new JTextField();
		homefield.setName(ID_HOME_FIELD);
		homefield.setEnabled(false);

		m_datemask = createDateMaskCombo();
		m_timemask = createTimeMaskCombo();

		JComponent[] controls = new JComponent[4];
		controls[0] = homefield;
		controls[1] = new JLabel("");
		controls[2] = m_datemask;
		controls[3] = m_timemask;

		com.jeta.foundation.gui.utils.ControlsAlignLayout layout = new com.jeta.foundation.gui.utils.ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_datemask, 20);
		layout.setMaxTextFieldWidth(m_timemask, 20);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, labels, controls);
		add(panel, BorderLayout.NORTH);

	}

	/**
	 * Loads the data from the user properties into the model
	 */
	private void loadData() {
		JTextField homefield = (JTextField) getComponentByName(ID_HOME_FIELD);
		homefield.setText(System.getProperty("abeille.home"));
	}

	void save() {
		String datemask = (String) m_datemask.getEditor().getItem();
		DateMask.setDefaultMask(datemask);

		String timemask = (String) m_timemask.getEditor().getItem();
		TimeMask.setDefaultMask(timemask);
	}

	/**
	 * The controller class for this view. This class just performs validation
	 * for now
	 */
	public class GeneralPreferencesController extends com.jeta.foundation.gui.components.TSController implements
			JETARule {
		public GeneralPreferencesController() {
			super(GeneralPreferencesView.this);
		}

		public RuleResult check(Object[] params) {
			String errormsg = null;
			String datemask = (String) m_datemask.getEditor().getItem();
			String timemask = (String) m_timemask.getEditor().getItem();

			DateMask dmask = DateMask.parse(datemask);
			if (dmask == null || !dmask.isValid()) {
				errormsg = I18N.getLocalizedMessage("Invalid Date Mask");
			}

			TimeMask tmask = TimeMask.parse(timemask);
			if (tmask == null || !tmask.isValid()) {
				errormsg = I18N.getLocalizedMessage("Invalid Time Mask");
			}

			if (errormsg == null) {
				return RuleResult.SUCCESS;
			} else {
				TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class,
						GeneralPreferencesView.this, true);
				dlg.initialize(errormsg);
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
				return RuleResult.FAIL;
			}
		}
	}
}
