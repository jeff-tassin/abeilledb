/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.IntegerTextField;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.interfaces.resources.ResourceLoader;

import com.jeta.open.gui.utils.JETAToolbox;
import com.jeta.open.registry.JETARegistry;

public class TSGuiToolbox {
	/**
	 * Common purple color for components
	 */
	public static final Color COMPONENT_PURPLE = new Color(225, 225, 255);

	/** cache of images */
	private static Hashtable m_imagecache = new Hashtable();

	/** used for calculating average char widths */
	private static final String AVG_STR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * we keep this variable around to calculate window unit widths and heights.
	 * If the look and feel changes, we need to recalculate those values
	 */
	private static javax.swing.LookAndFeel m_lookandfeel;
	/**
	 * the unit width/height in pixels of a container. A container will
	 * generally set its preferred size by some factor of these unit values
	 * (e.g. 20*unitwidth, 15*unitheight )
	 */
	private static int m_unitwidth;
	private static int m_unitheight;
	/**
	 * a dimension object that we re-use to minimize the number of objects we
	 * create during getPreferredSize calls
	 */
	private static Dimension m_dimension = new Dimension();

	private static Dimension m_toolbar_button_dims = new Dimension(24, 24);

	/**
	 * Adds an array or collection of items to a combo box
	 */
	public static void addItems(JComboBox cbox, Object[] items) {
		assert (cbox != null);
		if (items == null || cbox == null)
			return;

		for (int index = 0; index < items.length; index++) {
			cbox.addItem(items[index]);
		}
	}

	/**
	 * Vertically aligns labels and text rows on a panel.
	 */
	public static JPanel alignLabelTextRows(GridBagLayout gridbag, Component[] labels, Component[] components,
			Insets insets) {

		assert (labels != null);
		assert (components != null);

		TSPanel textcontrolspane = new TSPanel();
		textcontrolspane.setLayout(gridbag);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		int numLabels = labels.length;

		if (insets == null)
			insets = new Insets(5, 5, 0, 5);

		Insets cbox_insets = new Insets(insets.top, 0, insets.bottom, insets.right);

		for (int i = 0; i < numLabels; i++) {
			if (components[i] instanceof JLabel)
				c.ipady = 5;
			else
				c.ipady = 0;

			if (TSUtils.isDebug()) {
				if (components[i] == null) {
					System.out.println("TSGuiToolbox.components[" + i + "] is null");
				}

				if (labels[i] == null) {
					System.out.println("TSGuiToolbox.labels[" + i + "] is null");
				}
			}

			c.gridy = i;
			c.insets = insets;
			c.gridx = 0;
			c.fill = GridBagConstraints.NONE; // reset to default
			c.weightx = 0.0; // reset to default
			gridbag.setConstraints(labels[i], c);
			textcontrolspane.add(labels[i]);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			c.gridx = 1;

			/*
			 * Component comp = components[i]; if ( comp instanceof
			 * JToggleButton ) c.insets = cbox_insets; else if ( comp instanceof
			 * JPanel ) { JPanel jpanel = (JPanel)comp; if (
			 * (jpanel.getComponentCount() > 0) && (jpanel.getComponent(0)
			 * instanceof JToggleButton ) ) { c.insets = cbox_insets; } }
			 */

			gridbag.setConstraints(components[i], c);
			textcontrolspane.add(components[i]);
		}
		return textcontrolspane;
	}

	/**
	 * Vertically aligns labels and text rows on a panel.
	 */
	public static JPanel alignLabelTextRows(GridBagLayout gridbag, JComponent[] labels, JComponent[] components) {
		return alignLabelTextRows(gridbag, toComponentArray(labels), toComponentArray(components), null);
	}

	/**
	 * Vertically aligns labels and text rows on a panel.
	 */
	public static JPanel alignLabelTextRows(GridBagLayout gridbag, Component[] labels, Component[] components) {
		return alignLabelTextRows(gridbag, labels, components, null);
	}

	/**
	 * Vertically aligns labels and text rows on a panel.
	 */
	public static JPanel alignLabelTextRows(Component[] labels, Component[] components, Insets insets) {
		GridBagLayout gridbag = new GridBagLayout();
		return alignLabelTextRows(gridbag, labels, components, insets);
	}

	/**
	 * Vertically aligns labels and text rows on a panel.
	 */
	public static JPanel alignLabelTextRows(Component[] labels, Component[] components) {
		GridBagLayout gridbag = new GridBagLayout();
		return alignLabelTextRows(gridbag, labels, components, null);
	}

	/**
	 * Vertically aligns labels and text rows on a panel.
	 */
	public static JPanel alignLabelTextRows(JComponent[] labels, JComponent[] components) {
		GridBagLayout gridbag = new GridBagLayout();
		return alignLabelTextRows(gridbag, toComponentArray(labels), toComponentArray(components), null);
	}

	/**
	 * Vertically aligns labels and text rows on a panel.
	 */
	public static JPanel alignLabelTextRows(JComponent[] labels, JComponent[] components, Insets insets) {
		GridBagLayout gridbag = new GridBagLayout();
		return alignLabelTextRows(gridbag, toComponentArray(labels), toComponentArray(components), insets);
	}

	/**
	 * Converts an Array of JComponents to an array of Components
	 */
	private static Component[] toComponentArray(JComponent[] components) {
		Component[] result = new Component[components.length];
		for (int index = 0; index < components.length; index++) {
			result[index] = components[index];
		}
		return result;
	}

	/**
	 * Calculates the width of an average string of text.
	 * 
	 * @param comp
	 *            the component whose font metrics will determine the width
	 * @param numCharacters
	 *            the number of characters to use to calculate the length
	 */
	public static int calculateAverageTextWidth(Component comp, int numCharacters) {
		if (comp == null)
			return 0;

		Font f = comp.getFont();
		FontMetrics metrics = comp.getFontMetrics(f);
		return metrics.stringWidth(AVG_STR) * numCharacters / AVG_STR.length();
	}

	/**
	 * Sets the size of the dimension to some reasonable value. If the given
	 * dimension is larger than the screen size, then we set the window to 80%
	 * of the screen size. Otherwise, we leave it alone
	 * 
	 * @param window
	 *            the window whose size we are setting
	 * @param d
	 *            the dimension of the window to set
	 */
	public static void calculateReasonableComponentSize(Dimension d) {
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		if (d.width > screensize.width)
			d.width = screensize.width * 8 / 10;

		if (d.height > screensize.height)
			d.height = screensize.height * 8 / 10;
	}

	/**
	 * This method centers and sizes a frame window on the screen. The caller
	 * must pass the x and y percentages of screen width/height.
	 */
	public static void centerFrame(Window frame, float xpctWidth, float ypctWidth) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int frame_width = (int) (screenSize.width * xpctWidth);
		int frame_height = (int) (screenSize.height * ypctWidth);
		int left = (screenSize.width - frame_width) / 2;
		int top = (screenSize.height - frame_height) / 2;
		frame.setBounds(left, top, frame_width, frame_height);
	}

	/**
	 * This method centers a window window on the screen. The caller must pass
	 * the x and y percentages of screen width/height.
	 */
	public static void centerWindow(Window frame) {
		float width = (float) frame.getWidth();
		float height = (float) frame.getHeight();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		float pctwidth = width / (float) screenSize.getWidth();
		float pctheight = height / (float) screenSize.getHeight();
		centerFrame(frame, pctwidth, pctheight);
	}

	/**
	 * This method centers a window window and changes the width on the screen.
	 * The caller must pass the x percentages of screen width. The height
	 * remains unchanged
	 */
	public static void centerWindowChangeWidth(Window frame, float xpctWidth) {
		float height = (float) frame.getHeight();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		float pctheight = height / (float) screenSize.getHeight();
		centerFrame(frame, xpctWidth, pctheight);
	}

	/**
	 * Copies to the clipboard the given string
	 */
	public static void copyToClipboard(String str) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = kit.getSystemClipboard();
		StringSelection ss = new StringSelection(str);
		clipboard.setContents(ss, ss);
	}

	/**
	 * Create a check box that has no left padding. This is used when creating
	 * dialogs and we want checkboxs to be left aligned with other controls such
	 * as text fields. If we use the default border, the checkbox is generally
	 * offset by about 5 pixels.
	 */
	public static JCheckBox createCheckBox(String label) {
		JCheckBox tbox = new JCheckBox(label);
		java.awt.Insets i = tbox.getInsets();
		tbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(i.top, 0, i.bottom, i.right));
		return tbox;
	}

	/**
	 * Create a radio box that has no left padding. This is used when creating
	 * dialogs and we want radioboxs to be left aligned with other controls such
	 * as text fields. If we use the default border, the radiobox is generally
	 * offset by about 5 pixels.
	 */
	public static JRadioButton createRadioButton(String label) {
		JRadioButton rbtn = new JRadioButton(label);
		java.awt.Insets i = rbtn.getInsets();
		rbtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(i.top, 0, i.bottom, i.right));
		return rbtn;
	}

	/**
	 * This method creates a dialog of the type class. It assumes that the
	 * dialog will have constructors that have the following form: ctor( JDialog
	 * owner, boolean bmodal ) ctor( Frame owner, boolean bmodal )
	 * 
	 * This method interogates the owner object. If this object is a JDialog or
	 * Frame, then we call the appropriate constructor for the class. If this
	 * object is a JComponent, then we get the objects owner window and
	 * determine if that is a JDialog or Frame.
	 * 
	 * @param dlgclass
	 *            the class of the dialog we want to create
	 * @param owner
	 *            the owner of the new dialog. This object can be indirectly an
	 *            owner. For example, you can pass in a JPanel as the owner.
	 *            This method will detect this and get the appropriate owner of
	 *            the JPanel
	 * @param bModel
	 *            true if you want the dialog to be modal
	 * 
	 */
	public static Dialog createDialog(Class dlgclass, Component owner, boolean bModal) {
		if (owner == null) {
			Object frame = JETARegistry.lookup(JETAToolbox.APPLICATION_FRAME);
			if (frame instanceof Component)
				owner = (Component) frame;
			else {
				System.err.println("TSGuiToolbox.createDialog parent is null");
			}
		}

		if (owner == null && TSUtils.isDebug()) {
			Exception ee = new Exception();
			ee.printStackTrace();
		}

		Class[] cparams = new Class[2];
		Object[] params = new Object[2];

		if (owner instanceof Dialog) {
			cparams[0] = Dialog.class;
			params[0] = owner;
		} else if (owner instanceof Frame) {
			cparams[0] = Frame.class;
			params[0] = owner;
		} else {
			if (owner == null) {
				cparams[0] = Frame.class;
				params[0] = null;
			} else {
				Window win = SwingUtilities.getWindowAncestor(owner);
				if (win instanceof Dialog) {
					cparams[0] = Dialog.class;
					params[0] = win;
				} else if (win instanceof Frame) {
					cparams[0] = Frame.class;
					params[0] = win;
				} else {
					cparams[0] = Frame.class;
					params[0] = null;
				}
			}
		}
		cparams[1] = boolean.class;
		params[1] = Boolean.valueOf(bModal);

		try {
			Constructor ctor = dlgclass.getConstructor(cparams);
			return (Dialog) ctor.newInstance(params);
		} catch (Exception e) {
			System.out.println("Unable to construct dialg   parent class: " + cparams[0] + "  owner = " + owner);
			e.printStackTrace();
		}
		return null;
	}

	public static Dialog createDialog(Class dlgclass, TSInternalFrame owner, boolean bModal) {
		if (owner == null)
			return createDialog(dlgclass, (Component) null, bModal);
		else
			return createDialog(dlgclass, owner.getDelegate(), bModal);
	}

	/**
	 * Creates a menu item for this popup
	 * 
	 * @param itemText
	 *            the text to show for the menu item
	 * @param actionCmd
	 *            the name of the action that is fired when the menu item is
	 *            selected
	 * @param keyStroke
	 *            the keyboard accelerator
	 */
	public static JMenuItem createMenuItem(String itemText, String actionCmd, KeyStroke keyStroke) {
		JMenuItem item = new JMenuItem(itemText);
		item.setActionCommand(actionCmd);
		item.setName(actionCmd);
		if (keyStroke != null)
			item.setAccelerator(keyStroke);
		return item;
	}

	/**
	 * This creates a text field that only accepts numbers
	 */
	public static JTextField createNumericTextField() {
		return new IntegerTextField();
	}

	/**
	 * This creates a text field that only accepts numbers
	 */
	public static JTextField createIntegerTextField(int width) {
		return new IntegerTextField(width);
	}

	public static JComponent createSeparator(String text) {
		com.jgoodies.forms.factories.DefaultComponentFactory factory = com.jgoodies.forms.factories.DefaultComponentFactory
				.getInstance();
		return factory.createSeparator(text);
	}

	/**
	 * This method creates a simple toolbar button without a border and focus.
	 * It assumes the button is 16x16 and only displays an image.
	 * 
	 * @param imageName
	 *            the name of the button image
	 */
	public static JButton createToolBarButton(String imageName) {
		return createToolBarButton(imageName, null, null);
	}

	/**
	 * This method creates a simple toolbar button without a border and focus.
	 * It assumes the button is 16x16 and only displays an image.
	 * 
	 * @param imageName
	 *            the name of the button image
	 * @param buttonId
	 *            an id for the button (can be null )
	 * @param toolTip
	 *            the tool tip for the button (can be null)
	 */
	public static JButton createToolBarButton(String imageName, String buttonId, String toolTip) {
		JButton btn = new JButton(TSGuiToolbox.loadImage(imageName));
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setContentAreaFilled(false);

		if (buttonId != null)
			btn.setName(buttonId);

		if (toolTip != null) {
			btn.setToolTipText(toolTip);
		}
		return btn;
	}

	/**
	 * @return the standard size for toolbar buttons in the application
	 *         (currenty 16x16 plus some padding, this results to 24x24)
	 */
	public static Dimension getToolbarButtonSize() {
		return m_toolbar_button_dims;
	}

	/**
	 * This method returns a dimension for use in getPreferredSize. In our
	 * application, a dialog or window is sized based on a multiple of the
	 * getUnitWidth and getUnitHeight. This is a helper method that allows
	 * callers to quickly get a preferredSize for a container. It reuses a
	 * Dimension object so be aware of this
	 * 
	 * @param widthFactor
	 *            a factor to multiple times the getWindowUnitWidth value. This
	 *            determines the width in the returned dimension.
	 * @param heightFactor
	 *            a factor to multiple times the getWindowUnitHeight value. This
	 *            determines the height in the returned dimension.
	 * 
	 */
	public static Dimension getWindowDimension(int widthFactor, int heightFactor) {
		m_dimension.width = widthFactor * getWindowUnitWidth();
		m_dimension.height = heightFactor * getWindowUnitHeight();
		return m_dimension;
	}

	/**
	 * This method returns a common 'unit' of size in pixels. Containers use
	 * this as a basis for determining their preferred with. The calculation is
	 * based on the font size. This must be so because all Swing component sizes
	 * (e.g. JButton width/height, JTextField height, JLabel width/height) all
	 * depend on the current system font.
	 */
	public static int getWindowUnitHeight() {
		if (m_lookandfeel != javax.swing.UIManager.getLookAndFeel() || m_unitheight == 0) {
			// we will use the height of a single text field
			JTextField txtfield = new JTextField("foo");
			Dimension d = txtfield.getPreferredSize();
			m_unitheight = d.height;
		}
		return m_unitheight;
	}

	/**
	 * This method returns a common 'unit' of size in pixels. Containers use
	 * this as a basis for determining their preferred with. The calculation is
	 * based on the font size. This must be so because all Swing component sizes
	 * (e.g. JButton width/height, JTextField height, JLabel width/height) all
	 * depend on the current system font.
	 */
	public static int getWindowUnitWidth() {
		if (m_lookandfeel != javax.swing.UIManager.getLookAndFeel() || m_unitwidth == 0) {
			// we will use an average char width of 10 characters of a
			// JTextField font
			JTextField txtfield = new JTextField();
			m_unitwidth = TSGuiToolbox.calculateAverageTextWidth(txtfield, 10);
		}

		return m_unitwidth;
	}

	public static final String WINDOWS_LF = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

	/**
	 * @return true if we are currently in the windows look and feel. This L&F
	 *         has different focus issues with the popup
	 */
	public static boolean isWindowsLookAndFeel() {
		return (WINDOWS_LF.equals(javax.swing.UIManager.getLookAndFeel().getClass().getName()));
	}

	/**
	 * Loads an image from disk. The image is loaded relative to the application
	 * directory.
	 * 
	 * @todo we need to cache these images
	 */
	public static ImageIcon loadImage(String imageName) {
		if (imageName != null) {
			ImageIcon icon = (ImageIcon) m_imagecache.get(imageName);
			if (icon == null) {
				try {
					ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
					assert (loader != null);
					icon = loader.loadImage(imageName);
				} catch (Exception e) {
					TSUtils.printException(e);
				}

				if (icon == null) {
					icon = new ImageIcon();
				}
				m_imagecache.put(imageName, icon);
			}
			return icon;
		} else {
			assert (false);
			return new ImageIcon();
		}
	}

	/**
	 * Sets the border that is used by a JTextField on the given component
	 * 
	 * @param comp
	 *            the component whose border we want to set
	 */
	public static void setTextFieldBorder(JComponent comp) {
		String prefix = "TextField";
		comp.setBorder(UIManager.getBorder(prefix + ".border"));
	}

	public static int showConfirmDialog(String message, String caption, int opt) {
		return JOptionPane.showConfirmDialog(null, message, caption, opt);
	}

	/**
	 * Shows an error dialog on the screen
	 */
	public static void showErrorDialog(String message) {
		showErrorDialog(null, message, I18N.getLocalizedMessage("Error"));
	}

	/**
	 * Shows an error dialog on the screen
	 */
	public static void showErrorDialog(Component parent, String message, String caption) {
		JOptionPane.showMessageDialog(null, message, caption, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Sets the size of the given window to the passed in dimension. However, if
	 * the given dimension is larger than the screen size, then we set the
	 * window to 80% of the screen size
	 * 
	 * @param window
	 *            the window whose size we are setting
	 * @param d
	 *            the dimension of the window to set
	 */
	public static void setReasonableWindowSize(Component window, Dimension d) {
		calculateReasonableComponentSize(d);
		window.setSize(d);
	}

	/**
	 * This method is used because in some places in the code, the requested
	 * focus for a given component is not granted or is ignored or overrided. By
	 * simulating a mouse click for these components, we can get around the
	 * request focus issue
	 */
	public static void simulateMouseClick(Component comp, int x, int y) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		java.awt.EventQueue queue = toolkit.getSystemEventQueue();
		MouseEvent evt = new MouseEvent(comp, MouseEvent.MOUSE_PRESSED, 0, 0, x, y, 1, false);
		queue.postEvent(evt);
		evt = new MouseEvent(comp, MouseEvent.MOUSE_RELEASED, 0, 0, x, y, 1, false);
		queue.postEvent(evt);
		evt = new MouseEvent(comp, MouseEvent.MOUSE_CLICKED, 0, 0, x, y, 1, false);
		queue.postEvent(evt);
	}

}
