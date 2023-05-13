package com.jeta.abeille.gui.sql;

import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatLayer;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatWriter;

/**
 * SQL indentation services are located here
 * 
 * @author Jeff Tassin
 */

public class SQLFormatter extends ExtFormatter {

	public SQLFormatter(Class kitClass) {
		super(kitClass);
	}

	protected boolean acceptSyntax(Syntax syntax) {
		return (syntax instanceof SQLSyntax);
	}

	public int[] getReformatBlock(JTextComponent target, String typedText) {
		return super.getReformatBlock(target, typedText);
	}

	protected void initFormatLayers() {

	}

	public FormatSupport createFormatSupport(FormatWriter fw) {
		return new SQLFormatSupport(fw);
	}

}
