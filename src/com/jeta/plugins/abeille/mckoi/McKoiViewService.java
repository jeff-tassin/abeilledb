package com.jeta.plugins.abeille.mckoi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.View;
import com.jeta.abeille.database.model.ViewService;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.command.SQLCommand;
import com.jeta.foundation.i18n.I18N;

import com.jeta.plugins.abeille.generic.GenericViewService;

/**
 * This is the generic implementation for managing database views
 * 
 * @author Jeff Tassin
 */
public class McKoiViewService extends GenericViewService {

	/**
	 * ctor
	 */
	public McKoiViewService(TSConnection conn) {
		super(conn);
	}

	/**
	 * @return the view for the corresponding id. Null is returned if the view
	 *         cannot be found.
	 */
	public View getView(DbObjectId viewid) throws SQLException {
		if (viewid == null)
			return null;

		// select * from SYS_INFO.sUSRView where sUSRView.schema = 'SYS_JDBC'
		// and sUSRView.name = 'Tables';
		StringBuffer sqlbuff = new StringBuffer();
		sqlbuff.append("select * from SYS_INFO.sUSRView where sUSRView.schema = '");
		sqlbuff.append(viewid.getSchema().getName());
		sqlbuff.append("' and sUSRView.name = '");
		sqlbuff.append(viewid.getObjectName());
		sqlbuff.append("'");
		View view = null;
		Connection conn = getConnection().getMetaDataConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sqlbuff.toString());
			if (rset.next()) {
				byte[] bdata = DbUtils.getBinaryData(rset, "query");
				String definition = new String(bdata);
				view = new View(viewid);
				view.setDefinition(definition);
			}
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			if (stmt != null)
				stmt.close();

			conn.commit();
		}
		return view;
	}

}
