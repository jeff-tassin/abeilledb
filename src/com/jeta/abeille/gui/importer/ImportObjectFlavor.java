package com.jeta.abeille.gui.importer;

import java.awt.datatransfer.DataFlavor;

public class ImportObjectFlavor {
	/** for dragging a source column object */
	public static final DataFlavor SOURCE_COLUMN = new DataFlavor(SourceColumn.class,
			"jeta.abeille.gui.importer.sourcecolumn");
}
