package com.jeta.abeille.gui.procedures;

import java.awt.datatransfer.DataFlavor;

public class ProcedureFlavor {
	/** this is used to indentify a stored procedure */
	public static final DataFlavor STORED_PROCEDURE = new DataFlavor(
			com.jeta.abeille.database.procedures.StoredProcedure.class, "stored.procedure");

	/** this is used to indentify a procedure langauge */
	public static final DataFlavor PROCEDURE_LANGUAGE = new DataFlavor(
			com.jeta.abeille.database.procedures.ProcedureLanguage.class, "procedure.language");

}
