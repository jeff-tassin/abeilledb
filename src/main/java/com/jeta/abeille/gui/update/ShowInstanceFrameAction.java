package com.jeta.abeille.gui.update;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.interfaces.license.LicenseManager;

/**
 * This is a generic action that launches the instance frame for a given table;
 * 
 * @author Jeff Tassin
 */
public class ShowInstanceFrameAction implements ActionListener {
	/**
	 * ctor
	 */
	public ShowInstanceFrameAction() {

	}

	public void actionPerformed(ActionEvent evt) {
		// no op
	}

	public static TSInternalFrame launchFrame(TSConnection connection, InstanceViewBuilder builder,
			InstanceFrameLauncher launcher) {
		try {
			TSInternalFrame frame = TSWorkspaceFrame.getInstance().createInternalFrame(
					com.jeta.abeille.gui.update.InstanceFrame.class, false, connection.getId());

			Object[] params = null;
			if (launcher == null) {
				params = new Object[1];
			} else {
				params = new Object[2];
				params[1] = launcher;
			}

			params[0] = builder;
			frame.initializeModel(params);
			// @todo set to a reasonable size/location based on the current
			// workspace size
			frame.setSize(475, 400);
			TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();

			boolean bcenter = true;
			/**
			 * if the window that is launching the frame is an InstanceFrame,
			 * then let's set the new frame position relative to the launching
			 * frame
			 */
			if (launcher != null) {
				Object source = launcher.getSource();
				if (source instanceof InstanceFrame) {
					InstanceFrame lframe = (InstanceFrame) source;
					int OFFSET = 40;
					int[][] offsets = { { OFFSET, -OFFSET }, { OFFSET, OFFSET }, { -OFFSET, -OFFSET },
							{ -OFFSET, OFFSET } };
					Dimension d = wsframe.getWorkspaceSize();
					for (int index = 0; index < offsets.length; index++) {
						int x = offsets[index][0] + lframe.getX();
						int y = offsets[index][1] + lframe.getY();

						if ((x + lframe.getWidth() < d.width) && (y + lframe.getHeight() < d.height) && x >= 0
								&& y >= 0) {
							frame.setLocation(x, y);
							bcenter = false;
							break;
						}
					}
				}
			}

			if (bcenter)
				wsframe.centerWindow(frame);

			wsframe.addWindow(frame);

			return frame;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Specialized classes should override
	 */
	protected TSInternalFrame showFrame(TSConnection connection, InstanceViewBuilder builder) {
		return launchFrame(connection, builder, null);
	}

	/**
	 * Lauches the instance frame for a table id
	 */
	public static TSInternalFrame showFrame(TSConnection connection, TableId tableId) {
		TableInstanceViewBuilder builder = new TableInstanceViewBuilder(connection, tableId);
		InstanceFrameCache icache = InstanceFrameCache.getInstance(connection);
		return icache.launchFrame(connection, builder);
	}

}
