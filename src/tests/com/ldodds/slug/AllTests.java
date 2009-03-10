package com.ldodds.slug;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.ldodds.slug");
		//$JUnit-BEGIN$
		suite.addTest(com.ldodds.slug.framework.config.AllTests.suite());
		suite.addTest(com.ldodds.slug.http.filter.AllTests.suite());
		suite.addTest(com.ldodds.slug.util.AllTests.suite());
		//$JUnit-END$
		return suite;
	}

}