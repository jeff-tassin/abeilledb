package com.jeta.abeille.gui.sql;

import org.netbeans.editor.ext.ExtSettingsNames;

/**
 * Names of the sql editor settings.
 * 
 * @author Jeff Tassin
 */

public class SQLSettingsNames extends ExtSettingsNames {

	public static final String REUSERESULTS = "reuse.results";
	public static final String USE_DEFAULT_RENDERER = "use.default.sql.results.renderer";
	public static final String SQLHISTORY = "sql.history";

	/**
	 * Whether insert extra space before the parenthesis or not. Values:
	 * java.lang.Boolean instances Effect: function(a) becomes (when set to
	 * true) function (a)
	 */
	public static final String JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS = "java-format-space-before-parenthesis"; // NOI18N

	/**
	 * Whether insert space after the comma inside the parameter list. Values:
	 * java.lang.Boolean instances Effect: function(a,b) becomes (when set to
	 * true) function(a, b)
	 * 
	 */
	public static final String JAVA_FORMAT_SPACE_AFTER_COMMA = "java-format-space-after-comma"; // NOI18N

	/**
	 * Whether insert extra new-line before the compound bracket or not. Values:
	 * java.lang.Boolean instances Effect: if (test) { function(); } becomes
	 * (when set to true) if (test) { function(); }
	 */
	public static final String JAVA_FORMAT_NEWLINE_BEFORE_BRACE = "java-format-newline-before-brace"; // NOI18N

	/**
	 * Add one more space to the begining of each line in the multi-line comment
	 * if it's not already there. Values: java.lang.Boolean Effect: For example
	 * in java:
	 * 
	 * /* this is * multiline comment *\/
	 * 
	 * becomes (when set to true)
	 * 
	 * /* this is * multiline comment *\/
	 */
	public static final String JAVA_FORMAT_LEADING_SPACE_IN_COMMENT = "java-format-leading-space-in-comment"; // NOI18N

	/**
	 * Whether the '*' should be added at the new line in comment.
	 */
	public static final String JAVA_FORMAT_LEADING_STAR_IN_COMMENT = "java-format-leading-star-in-comment"; // NOI18N

}
