package gov.nist.toolkit.session.server.conformanceTest;

import gov.nist.toolkit.actorfactory.SiteServiceManager;
import gov.nist.toolkit.actortransaction.client.ATFactory.ActorType;
import gov.nist.toolkit.envSetting.EnvSetting;
import gov.nist.toolkit.results.client.AssertionResult;
import gov.nist.toolkit.results.client.Result;
import gov.nist.toolkit.results.client.SiteSpec;
import gov.nist.toolkit.session.server.Session;
import gov.nist.toolkit.session.server.serviceManager.XdsTestServiceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class MesaTestTest {
	String mesaTestSession = "bill";
	SiteSpec siteSpec = new SiteSpec("pub", ActorType.REGISTRY, null);
	Map<String, Object> params2 = new HashMap<String, Object>();
	boolean stopOnFirstFailure = true;
	String sessionId = "MySession";
	Session session = new Session(new File("/Users/bmajur/workspace/toolkit/xdstools2/war"), SiteServiceManager.getSiteServiceManager(), sessionId);
	EnvSetting es = new EnvSetting(sessionId, "NA2012");
	List<String> sections = new ArrayList<String>();
	Map<String, String> params = new HashMap<String, String>();

	SiteSpec siteSpec() {
		SiteSpec ss = new SiteSpec("red", ActorType.REGISTRY, null);
		ss.isTls = false;
		return ss;
	}

	@Before
	public void before() {
		System.setProperty("XDSCodesFile", "/Users/bmajur/tmp/toolkit2/environment/NA2013/codes.xml");
	}

	//	@Test
	public void buildTestData() {
		siteSpec.isTls = false;
		String testName = "12346";
		ParamBuilder pbuilder = new ParamBuilder();
		pbuilder.withParam("$patientid$", "25d5fe7674a443d^^^&1.3.6.1.4.1.21367.2009.1.2.300&ISO");
		params.put("$patientid$", "25d5fe7674a443d^^^&1.3.6.1.4.1.21367.2009.1.2.300&ISO");		
		new XdsTestServiceManager(session).runMesaTest(
				mesaTestSession, 
				siteSpec, 
				testName, 
				sections, 
				params, 
				params2, 
				stopOnFirstFailure);
	}

	//	@Test
	public void verifyTestData() {
		String testName = "11901";
		siteSpec.isTls = false;
		params.put("$patientid$", "25d5fe7674a443d^^^&1.3.6.1.4.1.21367.2009.1.2.300&ISO");		
		new XdsTestServiceManager(session).runMesaTest(mesaTestSession, siteSpec, testName, sections, params, params2, stopOnFirstFailure);
	}

	//	@Test
	public void twoStepTest() {
		String testName = "11966";
		ParamBuilder pbuilder = new ParamBuilder();
		pbuilder.withParam("$patientid$", "25d5fe7674a443d^^^&1.3.6.1.4.1.21367.2009.1.2.300&ISO");
		List<Result> results = new XdsTestServiceManager(session).runMesaTest(
				mesaTestSession, 
				siteSpec(), 
				testName, 
				sections, 
				pbuilder.getSParms(), 
				pbuilder.getOParms(), 
				stopOnFirstFailure);
		boolean pass = true;
		for (Result result : results) {
			if (!result.passed())
				pass = false;
		}
		if (!pass) System.out.println(testName + ": failed");
	}

	// non-tls register transaction tests
	@Test
	public void registerTests() {
		ParamBuilder pbuilder = new ParamBuilder();
		pbuilder.withParam("$patientid$", "25d5fe7674a443d^^^&1.3.6.1.4.1.21367.2009.1.2.300&ISO");
		String[] testsx = { "11992" };
		String[] tests = { "12346", "11901", "11966",
				"11990", "11991", "11992", "11993", "11994", "11995", "11996",
				"11997", "11998", "11999", "12000", "12001", "12004", "12084", 
				"12323", "12326", "12327", "12370" };
		int failures = 0;
		int ran = 0;
		class TestResult { String testNum; List<AssertionResult> msgs = new ArrayList<AssertionResult>(); TestResult(String testnum) { testNum = testnum; } }
		List<TestResult> testResults = new ArrayList<TestResult>();
		for (String testName : tests) {
			TestResult testResult = new TestResult(testName);
			List<Result> results = new XdsTestServiceManager(session).runMesaTest(
					mesaTestSession, 
					siteSpec(), 
					testName, 
					sections, 
					pbuilder.getSParms(), 
					pbuilder.getOParms(), 
					stopOnFirstFailure);
			boolean pass = true;
			for (Result result : results) {
				if (!result.passed()) {
					pass = false;
					testResult.msgs.addAll(result.getFailedAssertions());
					testResults.add(testResult);
				}
			}
			ran++;
			if (!pass) { System.out.println(testName + ": failed"); failures++; }
		}
		System.out.println("\n========================\ntests: " + ran + " ran " + failures + " failed\n\n");
		for (TestResult tr : testResults) {
			System.out.println(tr.testNum + ": " + lines(tr.msgs.get(0).assertion, 1));
		}
		System.out.println("\n========================\n");
	}
	
	// This parses the wierd results from the test engine  so that only the error message shows
	String lines(String content, int numberOfLines) {
		if (content == null) return content;
		String[] lines = content.split("\n");
		StringBuffer buf = new StringBuffer();
		for (int i=1; i<lines.length && i-1<numberOfLines; i++) {
			if (i > 0) buf.append("\n");
			buf.append(lines[i]);
		}
		return buf.toString();
	}
}