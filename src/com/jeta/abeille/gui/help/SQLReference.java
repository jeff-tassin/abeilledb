package com.jeta.abeille.gui.help;

import java.net.URL;

import java.util.Collection;

public interface SQLReference {
	public boolean supportsContent(SQLReferenceType objtype);

	public URL getContent(SQLReferenceType objtype);

	public URL getContent(String url);

	/**
	 * Returns a collection of SQLHelpEntry objects.
	 */
	public Collection getHelpEntries();

}
