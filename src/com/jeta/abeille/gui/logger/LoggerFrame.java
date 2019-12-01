package com.jeta.abeille.gui.logger;

import java.awt.BorderLayout;
import javax.swing.ImageIcon;

import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This frame displays the LoggerView that shows the logs for the application
 * 
 * @author Jeff Tassin
 */
public class LoggerFrame extends TSInternalFrame {
	/** the frame icon for this frame */
	public static ImageIcon FRAME_ICON;

	static {
		FRAME_ICON = TSGuiToolbox.loadImage("incors/16x16/scroll.png");
	}

	/**
	 * ctor
	 */
	public LoggerFrame() {
		super(I18N.getLocalizedMessage("Log View"));
		getContentPane().add(new LoggerView(), BorderLayout.CENTER);
		setFrameIcon(FRAME_ICON);
		setShortTitle(I18N.getLocalizedMessage("Log View"));
	}

	/**
	 * TSInternalFrame implementation.
	 */
	public void initializeModel(Object[] params) {

	}

}
