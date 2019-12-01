package com.jeta.abeille.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Enumeration;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class is used for running unit tests while Abeille is running. Various
 * parts of the code will periodically run units tests at the end of important
 * user operations. It is assumed that the test suite (i.e.part of the JUnit
 * framework) is already defined in the test.jeta.abeille package hierarchy.
 * Tests should only be run if the debug flag is enabled.
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
			System.out.println(suite.getName() + " passed!!!");
		} else {
			System.out.println("*******************" + suite.getName() + " failed!!!");
			Enumeration enumval = result.failures();
			while (enumval.hasMoreElements()) {
				TestFailure t = (TestFailure) enumval.nextElement();
				System.out.println(t.failedTest().getClass() + " failed: " + t.toString());
			}

			enumval = result.errors();
			while (enumval.hasMoreElements()) {
				TestFailure t = (TestFailure) enumval.nextElement();
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
