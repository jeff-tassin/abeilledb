/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Enumeration;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class is used for running unit tests while an application is running.
 * Various parts of the code will periodically run units tests at the end of
 * important user operations. It is assumed that the test suite (i.e.part of the
 * JUnit framework) is already defined in the test.jeta.abeille package
 * hierarchy. Tests should only be run if the debug flag is enabled.
 * 
 * @author Jeff Tassin
 */

public class JETATestFactory {
	public static void runTest(TestCase testcase) {
		TestResult result = new TestResult();

		TestSuite suite = new TestSuite(testcase.getClass().getName());
		suite.addTest(testcase);
		suite.run(result);
		if (result.wasSuccessful()) {
			// System.out.println( suite.getName() + " passed!!!" );
		} else {
			System.out.println("*******************" + suite.getName() + " failed!!!");
			Enumeration enum1 = result.failures();
			while (enum1.hasMoreElements()) {
				TestFailure t = (TestFailure) enum1.nextElement();
				System.out.println(t.failedTest().getClass() + " failed: " + t.toString());
			}

			enum1 = result.errors();
			while (enum1.hasMoreElements()) {
				TestFailure t = (TestFailure) enum1.nextElement();
				System.out.println(t.failedTest().getClass() + " failed: " + t.toString());
				t.thrownException().printStackTrace();
			}
		}
	}

	/**
	 * Instantiates the unit test suite and invokes the runTest( Object[]
	 * params) method
	 * 
	 * @param testCase
	 *            the classname of the test suite class to run
	 * @param param1
	 *            a single argument to pass to the test case
	 */
	public static void runTest(String testCase, Object param1) {
		Object[] params = new Object[1];
		params[0] = param1;
		runTest(testCase, params);
	}

	/**
	 * Instantiates the unit test suite and invokes the runTest( Object[]
	 * params) method
	 * 
	 * @param testCase
	 *            the classname of the test suite class to run
	 * @param param1
	 *            a single argument to pass to the test case
	 */
	public static void runTest(String testCase, Object param1, Object param2) {
		Object[] params = new Object[2];
		params[0] = param1;
		params[1] = param2;
		runTest(testCase, params);
	}

	/**
	 * Instantiates the unit test suite and invokes the runTest( Object[]
	 * params) method
	 * 
	 * @param testCase
	 *            the classname of the test suite class to run
	 * @param params
	 *            an array of arguments to pass to the test suite. The test
	 *            suite defines which arguments it will accept in the runTest
	 *            method.
	 */
	public static void runTest(String testCase, Object[] params) {
		try {
			if (TSUtils.isTest()) {
				Class testclass = Class.forName(testCase);

				Class[] paramtypes = new Class[1];
				paramtypes[0] = Object[].class;
				Constructor ctor = testclass.getConstructor(paramtypes);

				Object[] ctor_params = new Object[1];
				ctor_params[0] = params;
				TestCase testcase = (TestCase) ctor.newInstance(ctor_params);
				runTest(testcase);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
