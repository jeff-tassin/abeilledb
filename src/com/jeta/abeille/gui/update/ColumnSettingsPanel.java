package com.jeta.abeille.gui.update;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSController;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * This is a popup dialog that allows the user to directly edit the option
 * attributes for a table
 * 
 * @author Jeff Tassin
 */
public class ColumnSettingsPanel extends TSPanel {
	/** the actual name of the column */
	private JTextField m_colname;

	/** the name as displayed on the form. If null, then the column name is used */
	private JTextField m_displayname;

	private JTextField m_order;
	private JCheckBox m_visible;
	private JCheckBox m_autoheight;
	private JComboBox m_handlers;
	private int m_maxorder; // the maximum value allowed for the order field
	private TSPanel m_handlercontainer; // panel that holds components specific
										// for each type of handler

	private ColumnHandler m_handler; // this is the handler chosen by the user
										// to handle column data
	private TSPanel m_configpanel; // this is the panel used to configure the
									// handler (also it is created by the
									// handler)

	/**
	 * this is the original settings object that we are editing the settings
	 * for. we keep it around so that when we create a new ColumnSettings
	 * object, we can just use this object as the basis for some
	 * non-configurable setting
	 */
	private final ColumnSettings m_originalsettings;

	/**
	 * ctor
	 * 
	 * @param info
	 *            the option info we use to initialize the controls
	 * @param order
	 *            the order in which to show the specified column in the
	 *            instance view. Order is not part of the ColumnSettings class,
	 *            so we need to pass it in as a parameter. Order is managed by
	 *            the OptionsGuiModel and Controller
	 * @param maxOrder
	 *            the maximum value that the order value an take on
	 */
	public ColumnSettingsPanel(ColumnSettings info, int order, int maxOrder) {
		m_originalsettings = info;
		initialize(info, order, maxOrder);

		// ColumnSettingsPanelController defined at bottom of this class
		setController(new ColumnSettingsPanelController(this));
	}

	/**
	 * Creates the panel that holds the common controls at the top of the panel
	 */
	private JPanel createControlsPanel(ColumnSettings info, int order) {
		m_colname = new JTextField();
		m_colname.setEditable(false);
		m_displayname = new JTextField();

		m_order = new JTextField();
		m_visible = new JCheckBox(I18N.getLocalizedMessage("Show"));
		m_autoheight = new JCheckBox(I18N.getLocalizedMessage("Automatically increase size"));

		m_colname.setText(info.getColumnName());
		m_displayname.setText(info.getDisplayName());
		m_visible.setSelected(info.isVisible());
		m_order.setText(String.valueOf(order));
		m_order.setEditable(false);
		m_autoheight.setSelected(info.isAutoHeight());

		JComponent[] controls = new JComponent[4];
		controls[0] = m_colname;
		controls[1] = m_displayname;
		controls[2] = m_order;
		controls[3] = m_visible;
		// controls[3] = m_autoheight;

		JLabel[] labels = new JLabel[4];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Column Name"));
		labels[1] = new JLabel(I18N.getLocalizedDialogLabel("Display Name"));
		labels[2] = new JLabel(I18N.getLocalizedDialogLabel("Order"));
		labels[3] = new JLabel(I18N.getLocalizedDialogLabel("Column"));
		// labels[3] = new JLabel( "" );

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_order, 4);
		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, labels, controls);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * Creates the panel used to manage the data handler assignments for a
	 * column
	 */
	private JPanel createDataHandlerPanel() {
		JPanel outer_panel = new JPanel(new BorderLayout());
		outer_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		m_handlercontainer = new TSPanel(new BorderLayout());
		m_handlercontainer.setBorder(BorderFactory.createTitledBorder(m_handlercontainer.getBorder(), "Data Handler"));

		m_handlers = new JComboBox();
		m_handlers.setName(ID_HANDLER_COMBO);

		Collection handlers = ColumnHandlerFactory.getHandlerNames();
		Iterator iter = handlers.iterator();
		while (iter.hasNext()) {
			m_handlers.addItem((String) iter.next());
		}

		JComponent[] controls = new JComponent[1];
		controls[0] = m_handlers;
		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Type"));
		JPanel panel = TSGuiToolbox.alignLabelTextRows(labels, controls);

		m_handlercontainer.add(panel, BorderLayout.NORTH);
		outer_panel.add(m_handlercontainer, BorderLayout.CENTER);

		return outer_panel;
	}

	/**
	 * @return the option info that is represented by this dialog
	 */
	public ColumnSettings getColumnSettings() {
		ColumnSettings info = new ColumnSettings(m_originalsettings);
		info.setVisible(m_visible.isSelected());
		m_handler.readInput(m_configpanel);
		info.setColumnHandler(m_handler);
		info.setDisplayName(m_displayname.getText());
		// info.setAutoHeight( m_autoheight.isSelected() );
		return info;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return Integer.parseInt(m_order.getText());
	}

	/**
	 * @return the preferred size for this dialog
	 */
	public Dimension getPreferredSize() {
		Dimension d = m_colname.getPreferredSize();
		JButton dummy = new JButton("test");
		d.width = dummy.getPreferredSize().width;
		d.width *= 8;
		d.height *= 20;
		return d;
	}

	/**
	 * Initializes this dialog.
	 * 
	 * @param info
	 *            the option info we use to initialize the controls
	 * @param order
	 *            the order in which to show the specified column in the
	 *            instance view. Order is not part of the ColumnSettings class,
	 *            so we need to pass it in as a parameter. Order is managed by
	 *            the OptionsGuiModel and Controller
	 * @param maxOrder
	 *            the maximum value that the order value an take on
	 */
	public void initialize(ColumnSettings info, int order, int maxOrder) {
		m_maxorder = maxOrder;
		setLayout(new BorderLayout());
		add(createControlsPanel(info, order), BorderLayout.NORTH);
		add(createDataHandlerPanel(), BorderLayout.CENTER);

		setHandler(info.getColumnHandler());
	}

	/**
	 * Sets the handler (with default values) to the given handler name
	 * 
	 * @param handlerName
	 *            the name of the handler type to set
	 */
	void setHandler(String handlerName) {
		ColumnHandler handler = null;
		try {
			handler = ColumnHandlerFactory.createHandler(handlerName);
		} catch (Exception e) {
			e.printStackTrace();
			// if an error occurs, just use the default column handler
			handler = new DefaultColumnHandler();
		}

		setHandler(handler);
	}

	/**
	 * Sets the type of handler used for a column in the instance view
	 * 
	 * @param handler
	 *            the handler to set
	 */
	void setHandler(ColumnHandler handler) {
		m_handler = handler;
		m_handlers.setSelectedItem(handler.getName());

		// we need to do a repaint here because the handlerpanel my decrease in
		// size
		// when we remove/add components. The repaint here will invalidate the
		// entire
		// panel, so that if the panel decreases size, it will be painted
		// properly
		m_handlercontainer.repaint();

		m_handlercontainer.removeAll();

		// m_configpanel = m_handler.createConfigurationPanel( m_handlers );
		m_configpanel = new TSPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.WEST;

		m_configpanel.add(new JLabel(I18N.getLocalizedDialogLabel("Type")), c);

		c.gridx = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		m_configpanel.add(m_handlers, c);

		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5, 5, 5, 5);

		// System.out.println( "handler type: " + handler.getClass().getName()
		// );
		TSPanel confpanel = handler.createConfigurationPanel();
		assert (confpanel != null);
		m_configpanel.add(confpanel, c);

		m_handlercontainer.add(m_configpanel, BorderLayout.NORTH);

		// you need to call validate here because the panel will not display the
		// new components that may have been added
		m_handlercontainer.validate();
	}

	/**
	 * Checks input values.
	 * 
	 * @return a message string if the input values are invalid.
	 */
	String validateInput() {
		// TSController controller = m_configpanel.getController();
		// if ( controller != null )
		// return controller.validate();
		// else
		return null;
	}

	// ////////////////////////////////////////////////////////////////////////
	// names for components on this dialog
	public static final String ID_HANDLER_COMBO = "handlercombo";

	/**
	 * Controller for this dialog
	 */
	public static class ColumnSettingsPanelController extends TSController implements JETARule {
		private ColumnSettingsPanel m_panel;

		/**
		 * ctor
		 */
		public ColumnSettingsPanelController(ColumnSettingsPanel panel) {
			super(panel);
			m_panel = panel;

			// install a listener on the type combobox. When the user selects a
			// different column handler, then lets set it on the panel
			final JComboBox handlerbox = (JComboBox) m_panel.getComponentByName(ID_HANDLER_COMBO);
			handlerbox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					m_panel.setHandler((String) handlerbox.getSelectedItem());
				}
			});
		}

		/**
		 * TSController implementation of check
		 */
		public RuleResult check(Object[] params) {
			return new RuleResult(m_panel.validateInput());
		}
	}
}
