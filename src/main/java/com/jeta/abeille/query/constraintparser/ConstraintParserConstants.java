/* Generated By:JavaCC: Do not edit this line. ConstraintParserConstants.java */
package com.jeta.abeille.query.constraintparser;

public interface ConstraintParserConstants {

	int EOF = 0;
	int EOL = 4;
	int AND = 5;
	int OR = 6;
	int BOOLEAN = 7;
	int INTEGER_LITERAL = 8;
	int FLOATING_POINT_LITERAL = 9;
	int EXPONENT = 10;
	int STRING_LITERAL = 11;
	int DOT = 12;
	int LESS = 13;
	int LESSEQUAL = 14;
	int GREATER = 15;
	int GREATEREQUAL = 16;
	int EQUAL = 17;
	int NOTEQUAL = 18;
	int NOTEQUAL2 = 19;
	int OPENPAREN = 20;
	int CLOSEPAREN = 21;
	int QUESTIONMARK = 22;
	int AT = 23;
	int IS_NULL = 24;
	int LIKE = 25;
	int ID = 26;
	int LETTER = 27;
	int DIGIT = 28;

	int DEFAULT = 0;

	String[] tokenImage = { "<EOF>", "\" \"", "\"\\r\"", "\"\\t\"", "\"\\n\"", "<AND>", "<OR>", "<BOOLEAN>",
			"<INTEGER_LITERAL>", "<FLOATING_POINT_LITERAL>", "<EXPONENT>", "<STRING_LITERAL>", "\".\"", "\"<\"",
			"\"<=\"", "\">\"", "\">=\"", "\"=\"", "\"!=\"", "\"<>\"", "\"(\"", "\")\"", "\"?\"", "\"@\"",
			"\"IS NULL\"", "\"LIKE\"", "<ID>", "<LETTER>", "<DIGIT>", };

}