package com.jeta.abeille.gui.login;

import com.jeta.abeille.database.model.ConnectionInfo;

public interface ConnectionView {
	/** return the model for the view */
	public ConnectionInfo createConnectionModel();

	/** enables/disables the view */
	public void setEnabled(boolean enable);

	/** sets the model for the view */
	public void setModel(ConnectionInfo model);

}
