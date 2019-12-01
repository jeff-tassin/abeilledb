package com.jeta.abeille.gui.model;

import java.sql.SQLException;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.command.QueryTableCommand;
import com.jeta.abeille.gui.command.ModalCommandRunner;
import com.jeta.abeille.gui.utils.SQLErrorDialog;
import com.jeta.abeille.logger.DbLogger;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;

/**
 * This class is responsible for invoking a SQLResultsFrame when a user does a
 * select * on a table. This happens often in the application, so we have a
 * common class for this request.
 * 
 * @author Jeff Tassin
 */
public class QueryTableAction {
	public static void invoke(TSConnection connection, TableId tableid) {
		LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
		if (jlm.checkSessionTimeOut()) {
			int max_rows = TSUserPropertiesUtils.getInteger(TSConnection.ID_MAX_QUERY_ROWS, 500);

			QueryTableCommand cmd = new QueryTableCommand(connection, tableid, max_rows);
			ModalCommandRunner crunner = new ModalCommandRunner(connection, cmd);
			try {
				if (crunner.invoke() == ModalCommandRunner.COMPLETED) {
					TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
					TSInternalFrame frame = (TSInternalFrame) wsframe.createInternalFrame(
							com.jeta.abeille.gui.sql.SQLResultsFrame.class, false, connection.getId());
					Object[] params = new Object[4];
					params[0] = connection;
					params[1] = cmd.getResultSetReference();
					params[2] = null;
					params[3] = tableid;
					frame.initializeModel(params);
					frame.setSize(frame.getPreferredSize());
					wsframe.centerWindow(frame);
					wsframe.addWindow(frame);
				}
			} catch (SQLException e) {
				DbLogger.log(e);
				SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class,
						TSWorkspaceFrame.getInstance(), true);
				dlg.initialize(crunner.getError(), null, true);
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
			}
		}
	}

}
