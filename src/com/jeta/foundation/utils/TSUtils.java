/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.utils;

import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.text.JTextComponent;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;

import com.jeta.foundation.gui.java.AttributeWrapper;
import com.jeta.foundation.gui.java.StringWrapper;

public class TSUtils {

	/**
	 * let's cache an array of integers so we don't need to recreate the object
	 * everytime for JTables and the like
	 */
	private static Integer[] m_ints = new Integer[100];

	/**
	 * we provide the following ranges because the values from 2000-2100 and -1
	 * to -100 are used frequently in the application (e.g. see java.sql.Types
	 * constants )
	 */
	/** 2000 to 2100 */
	private static Integer[] m_2000ints = new Integer[100];

	/** -1 to -100 */
	private static Integer[] m_neg_ints = new Integer[100];

	final static String EMPTY_STR = "";

	static {
		for (int index = 0; index < m_ints.length; index++) {
			m_ints[index] = new Integer(index);
		}

		for (int index = 0; index < m_neg_ints.length; index++) {
			m_neg_ints[index] = new Integer(-(index + 1));
		}

		for (int index = 0; index < m_2000ints.length; index++) {
			m_2000ints[index] = new Integer(index + 2000);
		}

	}

	public static void copyFile(String dest_path, String src_path) throws IOException {
		if (dest_path == null)
			return;

		try {
			File f1 = new File(dest_path);
			File f2 = new File(src_path);

			if (f1.getCanonicalPath().equals(f2.getCanonicalPath())) {
				System.err.println("TSUtils.copyFile  dest and src are same.");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		FileInputStream fis = new FileInputStream(src_path);
		FileOutputStream fos = new FileOutputStream(dest_path);

		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		byte[] buff = new byte[1024];
		int numread = bis.read(buff);
		while (numread > 0) {
			bos.write(buff, 0, numread);
			numread = bis.read(buff);
		}

		bos.flush();
		bos.close();
		bis.close();
	}

	/**
	 * This method makes sure an ArrayList has at least sz elements. This
	 * guarantees that a call to list.size() will be valid for sz elements.
	 * 
	 * @param list
	 *            the list to add elements to
	 * @param sz
	 *            the size to ensure
	 */
	public static void ensureSize(ArrayList list, int sz) {
		if (list == null) {
			assert (false);
			return;
		}

		if (sz <= list.size())
			return;

		int old_size = list.size();
		for (int index = 0; index < (sz - old_size); index++) {
			list.add(null);
		}
	}

	/**
	 * let's cache an array of integers so we don't need to recreate the object
	 * everytime for JTables and the like
	 */
	public static Integer getInteger(int ival) {
		if (ival >= 0 && ival < m_ints.length) {
			return m_ints[ival];
		} else if (ival <= -1 && ival >= -100) {
			return m_neg_ints[-(ival + 1)];
		} else if (ival >= 2000 && ival <= 2099) {
			return m_2000ints[ival - 2000];
		} else {
			return new Integer(ival);
		}
	}

	public static long getLong(String lval) {
		if (lval == null || lval.length() == 0)
			return 0L;
		else
			return Long.parseLong(lval);
	}

	public static int getInt(String lval) {
		if (lval == null || lval.length() == 0)
			return 0;
		else
			return Integer.parseInt(lval);
	}

	private static void fail() {
		if (isDebug()) {
			System.out.println("assertion failed:");
			Throwable e = new Throwable();
			e.printStackTrace();
		}
	}

	public static void _assert(boolean b) {
		if (!b)
			fail();
	}

	public static void _assert(long lng) {
		if (lng == 0L)
			fail();
	}

	public static void _assert(double dbl) {
		if (dbl == 0.0)
			fail();
	}

	public static void _assert(Object ref) {
		if (ref == null)
			fail();
	}

	static char[] letters = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
			'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	/**
	 * Fills a buffer with the given character count times
	 * 
	 * @param c
	 *            the character to fill the string with
	 * @param count
	 *            the number of characters to place in the string
	 */
	public static void fillBuffer(StringBuffer buff, char c, int count) {
		for (int index = 0; index < count; index++) {
			buff.append(c);
		}
	}

	/**
	 * Creates a string that is filled with the given character count times
	 * 
	 * @param c
	 *            the character to fill the string with
	 * @param count
	 *            the number of characters to place in the string
	 * @return the created string
	 */
	public static String fillString(char c, int count) {
		StringBuffer buff = new StringBuffer(count);
		fillBuffer(buff, c, count);
		return buff.toString();
	}

	/**
	 * Creates a unique 8 digit UI.
	 */
	public static String createUID() {
		java.rmi.server.UID uid = new java.rmi.server.UID();
		// we need to strip off any non alphanumeric characters because we use
		// the UID as
		// file and directory names
		StringBuffer sbuff = new StringBuffer(uid.toString());
		for (int index = 0; index < sbuff.length(); index++) {
			char c = sbuff.charAt(index);
			if ((c < '0') || (c > '9' && c < 'A') || (c > 'Z' && c < 'a') || (c > 'z')) {
				int cindex = (int) (Math.random() * 26.0);
				sbuff.setCharAt(index, letters[cindex]);
			}
		}

		char c = sbuff.charAt(0);
		if (c < 65 || c > 90) {
			int cindex = (int) (Math.random() * 26.0);
			sbuff.setCharAt(0, letters[cindex]);
		}
		return sbuff.toString();
	}

	/**
	 * Performs a fast trim on a string. Note: if the string does not need
	 * triming, then a copy is NOT created and the original string is returned.
	 * Null is never returned from this method. If null is passed, an empty
	 * string is returned.
	 */
	public static String fastTrim(String str) {
		if (str == null)
			return EMPTY_STR;

		int len = str.length();
		if (len == 0)
			return str;

		if (str.charAt(0) == ' ' || str.charAt(len - 1) == ' ')
			return str.trim();
		else
			return str; // no leading/trailing spaces so just return the
						// original string
	}

	/**
	 * Helper method that returns the first character in the string. If the
	 * string is null or has zero length, a null character is returned.
	 */
	public static char getChar(String sVal) {
		if (sVal == null || sVal.length() == 0)
			return 0;
		else
			return sVal.charAt(0);
	}

	/**
	 * @return the file extension (the . is included) for a given file. If the
	 *         file does not have a file extension, an empty string is returned
	 */
	public static String getFileExtension(File f) {
		String ext = "";
		String fname = f.getName();
		if (fname != null) {
			int pos = fname.lastIndexOf('.');
			if (pos >= 0) {
				ext = fname.substring(pos, fname.length());
			}
		}
		return ext;
	}

	/**
	 * Debugging flag
	 */
	public static boolean isDebug() {
		try {
			// String result = System.getProperty("jeta1.debug");
			// return (result != null && result.equals("true"));
			return true;
		} catch (Exception e) {

		}
		return false;
	}

	/**
	 * Testing flag
	 */
	public static boolean isTest() {
		try {
			String result = System.getProperty("jeta1.test");
			return (result != null && result.equals("true"));
		} catch (Exception e) {

		}
		return false;
	}

	public static boolean isLinux() {
		try {
			String result = System.getProperty("os.name");
			if (result != null) {
				result = result.toLowerCase();
				if (result.indexOf("linux") >= 0)
					return true;
			}
		} catch (Exception e) {

		}
		return false;
	}

	/**
	 * @return true if currently running under webstart
	 */
	public static boolean isWebStart() {
		String jwsver = TSUtils.fastTrim(System.getProperty("javawebstart.version"));
		return (jwsver.length() > 0);
	}

	public static boolean isWindows() {
		try {
			String result = System.getProperty("os.name");
			if (result != null) {
				result = result.toLowerCase();
				if (result.indexOf("windows") >= 0)
					return true;
			}
		} catch (Exception e) {

		}
		return false;
	}

	/**
	 * Uses reflection to print the members of an object
	 */
	public static void print(Object obj) {
		if (obj == null) {
			System.out.println("--------------- printing object is NULL ----------- ");
		} else {
			System.out.println("--------------- printing object  " + obj.getClass().getName() + "@" + obj.hashCode()
					+ "   ---------");
			printAttributes(obj.getClass(), obj);
		}
	}

	/**
	 * Uses reflection to print the members of an object
	 */
	public static void printAttributes(Class c, Object obj) {
		if (c == Object.class)
			return;

		if (TSUtils.isDebug()) {
			try {
				java.lang.reflect.Field[] fields = c.getDeclaredFields();
				for (int index = 0; index < fields.length; index++) {
					java.lang.reflect.Field f = fields[index];
					f.setAccessible(true);
					Class ftype = f.getType();
					if (ftype.isPrimitive()) {
						AttributeWrapper wrapper = new AttributeWrapper(f, obj);
						System.out.println(wrapper.getName() + ":" + wrapper.getTypeName() + " = "
								+ wrapper.getDisplayValue());
					} else if (ftype == String.class) {
						StringWrapper wrapper = new StringWrapper(f, obj);
						System.out.println(wrapper.getName() + ":String  = " + wrapper.getDisplayValue());
					}
				}
				printAttributes(c.getSuperclass(), obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void printDebugMessage(String msg) {
		if (TSUtils.isDebug()) {
			 System.out.println(msg);
		}
	}

	public static void printMessage(String msg) {
		TSUtils.printDebugMessage(msg);
	}

	/**
	 * Dumps the stack trace to the console
	 */
	public static void printException(Throwable e) {
		printStackTrace(e);
		ComponentMgr.registerComponent(ComponentNames.ID_DEBUG_EXCEPTION_FLAG, e);
	}

	/**
	 * Dumps the stack trace to the console
	 */
	public static void printDebugException(Throwable e) {
		if (TSUtils.isDebug()) {
			printStackTrace(e);
			ComponentMgr.registerComponent(ComponentNames.ID_DEBUG_EXCEPTION_FLAG, e);
		}
	}

	/**
	 * Dumps the stack trace to the console
	 */
	public static void printStackTrace() {
		Exception e = new Exception("Stack Trace");
		printStackTrace(e);
	}

	/**
	 * Dumps the stack trace to the console
	 */
	public static void printStackTrace(Throwable e) {
		if (e != null) {
			if (TSUtils.isDebug()) {
				e.printStackTrace();
			}

			java.io.StringWriter sw = new java.io.StringWriter();
			java.io.PrintWriter pw = new java.io.PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			
			System.out.println(sw.toString());
		}
	}

	/**
	 * Safely saves the contents of a JTextComponent object to a temporary file
	 * and them does a file move.
	 * 
	 * @param path
	 *            the path to the directory in which to store the file. (Must
	 *            exist)
	 * @param filename
	 *            the name of the file to store
	 * @param txtComponent
	 *            the JTextComponent whose contents we wish to store
	 */
	public static void safeSaveFile(String path, String filename, JTextComponent txtComponent) throws IOException {
		String fname = path + File.separatorChar + filename;
		File f = new File(fname);
		safeSaveFile(f, txtComponent);
	}

	/**
	 * Safely saves the contents of a JTextComponent object to a temporary file
	 * and them does a file move.
	 * 
	 * @param file
	 *            the file to save to
	 * @param txtComponent
	 *            the JTextComponent whose contents we wish to store
	 */
	public static void safeSaveFile(File f, JTextComponent txtComponent) throws IOException {
		FileWriter writer = new FileWriter(f);
		txtComponent.write(writer);
	}

	/**
	 * Strips characters from a string.
	 * 
	 * @param str
	 *            the string to strip characters from
	 * @param stripChars
	 *            the set of characters to strip. Each character is evaluated
	 *            individually. So, if you passed "+-", then the following would
	 *            result: str = "+4343-A" return: "4343A"
	 * @return a new copy of the string with the given characters stripped out
	 */
	public static String strip(String str, String stripChars) {
		if (str == null || stripChars == null || stripChars.length() == 0)
			return str;

		StringBuffer result = new StringBuffer(str.length());
		for (int index = 0; index < str.length(); index++) {
			char c = str.charAt(index);
			if (stripChars.indexOf(c) < 0) {
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * Strips characters from a string.
	 * 
	 * @param str
	 *            the string to strip characters from
	 * @param stripChars
	 *            the set of characters to strip. Each character is evaluated
	 *            individually. So, if you passed "+-", then the following would
	 *            result: str = "+4343-A" return: "4343A"
	 * @return a new copy of the string with the given characters stripped out
	 */
	public static String strip(String str, char stripChar) {
		if (str == null)
			return str;

		StringBuffer result = new StringBuffer(str.length());
		for (int index = 0; index < str.length(); index++) {
			char c = str.charAt(index);
			if (c != stripChar) {
				result.append(c);
			}
		}
		return result.toString();
	}

	public static String toLowerCase(String sVal) {
		if (sVal == null)
			return null;
		else
			return sVal.toLowerCase();
	}

	public static String toUpperCase(String sVal) {
		if (sVal == null)
			return null;
		else
			return sVal.toUpperCase();
	}

	/**
	 * Removes all items in the list beyond and including the last_index
	 */
	public static void trim(ArrayList list, int last_index) {
		try {
			int trim_count = list.size() - last_index;
			for (int count = 0; count < trim_count; count++) {
				list.remove(list.size() - 1);
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * This method converts a standard wildcard search expression to a
	 * corresponding regular expression.
	 * 
	 * @param wildcard
	 *            the wildcard expression (e.g. *.java )
	 * @return the correpsonding regular expression
	 */
	public static String wildCardToRegex(String wildcard) {
		char lastchar = 0;
		StringBuffer regex = new StringBuffer();
		for (int index = 0; index < wildcard.length(); index++) {
			char c = wildcard.charAt(index);
			// require beginning of line matches
			if (index == 0 && c != '*')
				regex.append('^');

			if (c == '*') {
				if (lastchar != '*') {
					regex.append(".+");
					lastchar = '*';
				}
			} else if (c == '.') {
				lastchar = '.';
				regex.append("\\.");
			} else {
				regex.append(c);
				lastchar = c;
			}
		}
		return regex.toString();
	}
}
