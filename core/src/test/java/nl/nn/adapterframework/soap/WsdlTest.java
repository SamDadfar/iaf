package nl.nn.adapterframework.soap;

import static nl.nn.adapterframework.testutil.MatchUtils.assertXmlEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import nl.nn.adapterframework.configuration.Configuration;
import nl.nn.adapterframework.core.Adapter;
import nl.nn.adapterframework.core.IValidator;
import nl.nn.adapterframework.core.PipeLine;
import nl.nn.adapterframework.http.WebServiceListener;
import nl.nn.adapterframework.pipes.XmlValidator;
import nl.nn.adapterframework.pipes.XmlValidatorTest;
import nl.nn.adapterframework.receivers.Receiver;
import nl.nn.adapterframework.testutil.TestFileUtils;
import nl.nn.adapterframework.util.XmlUtils;
import nl.nn.adapterframework.validation.AbstractXmlValidator;
import nl.nn.adapterframework.validation.JavaxXmlValidator;
import nl.nn.adapterframework.validation.XercesXmlValidator;


/**
 * @author Michiel Meeuwissen
 */
@RunWith(value = Parameterized.class)
public class WsdlTest {

	private final  Class<AbstractXmlValidator> implementation;

	public WsdlTest(Class<AbstractXmlValidator> implementation) {
		this.implementation = implementation;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][]{
			{XercesXmlValidator.class},
			{JavaxXmlValidator.class}
		};
		return Arrays.asList(data);
	}


	@Test
	public void basic() throws Exception {
		PipeLine simple = mockPipeLine(
				getXmlValidatorInstance("a", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd"),
				getXmlValidatorInstance("b", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd"), "urn:webservice1", "Test1");
		WsdlGenerator wsdl = new WsdlGenerator(simple);
		wsdl.init();
		test(wsdl, "WsdlTest/webservice1.test.wsdl");
	}

	@Test
	public void basicMultipleRootTagsAllowed() throws Exception {
		PipeLine simple = mockPipeLine(
				getXmlValidatorInstance("a,x,y", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd"),
				getXmlValidatorInstance("b,p,q", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd"), "urn:webservice1", "Test1");
		WsdlGenerator wsdl = new WsdlGenerator(simple);
		wsdl.init();
		test(wsdl, "WsdlTest/webservice1.test.wsdl");
	}

	@Test
	public void basicMixed() throws Exception {
		XmlValidator inputValidator=getXmlValidatorInstance("a", "b", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd");
		IValidator outputValidator=inputValidator.getResponseValidator();
		PipeLine simple = mockPipeLine(inputValidator, outputValidator, "urn:webservice1", "Test1");
		WsdlGenerator wsdl = new WsdlGenerator(simple);
		wsdl.init();
		test(wsdl, "WsdlTest/webservice1.test.wsdl");
	}

	@Test
	public void includeXsdInWsdl() throws Exception {
		PipeLine simple = mockPipeLine(
				getXmlValidatorInstance("a", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd"),
				getXmlValidatorInstance("b", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd"), "urn:webservice1", "IncludeXsds");

		WsdlGenerator wsdl = new WsdlGenerator(simple);
		wsdl.setUseIncludes(true);
		wsdl.init();
		test(wsdl, "WsdlTest/includexsds.test.wsdl");
	}


	@Test
	public void includeXsdInWsdlMixed() throws Exception {
		XmlValidator inputValidator=getXmlValidatorInstance("a", "b", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd");
		IValidator outputValidator=inputValidator.getResponseValidator();
		PipeLine simple = mockPipeLine(inputValidator, outputValidator, "urn:webservice1", "IncludeXsds");

		WsdlGenerator wsdl = new WsdlGenerator(simple);
		wsdl.setUseIncludes(true);
		wsdl.init();
		test(wsdl, "WsdlTest/includexsds.test.wsdl");
	}


	@Test
	@Ignore("not finished, but would fail, you must specify root tag now.")
	public void noroottagAndInclude() throws Exception {
		PipeLine simple = mockPipeLine(
				getXmlValidatorInstance(null, "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd"),
				getXmlValidatorInstance("b", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd"), "urn:webservice1", "TestRootTag");

		WsdlGenerator wsdl = new WsdlGenerator(simple);
		wsdl.setUseIncludes(true);
		test(wsdl, "WsdlTest/noroottag.test.wsdl");
	}

	@Test
	public void noroottag() throws Exception {
		PipeLine simple = mockPipeLine(
			getXmlValidatorInstance(null, "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd"),
			getXmlValidatorInstance("b", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd"), "urn:webservice1", "TestRootTag");
		WsdlGenerator wsdl = new WsdlGenerator(simple);
		wsdl.init();
		test(wsdl, "WsdlTest/noroottag.test.wsdl");
	}

	@Test
	public void noroottagMixed() throws Exception {
		XmlValidator inputValidator=getXmlValidatorInstance(null, "b", "WsdlTest/test.xsd", "urn:webservice1 WsdlTest/test.xsd");
		IValidator outputValidator=inputValidator.getResponseValidator();
		PipeLine simple = mockPipeLine(inputValidator, outputValidator, "urn:webservice1", "TestRootTag");
		WsdlGenerator wsdl = new WsdlGenerator(simple);
		wsdl.init();
		test(wsdl, "WsdlTest/noroottag.test.wsdl");
	}




	@Test
	public void wubCalculateQuoteAndPolicyValuesLifeRetail() throws Exception {
		PipeLine pipe = mockPipeLine(
			getXmlValidatorInstance("CalculationRequest", null, null,
				"http://wub2nn.nn.nl/CalculateQuoteAndPolicyValuesLifeRetail " +
						"WsdlTest/CalculateQuoteAndPolicyValuesLifeRetail/xsd/CalculationRequestv2.1.xsd"),
			getXmlValidatorInstance("CalculationResponse", null, null,
				"http://wub2nn.nn.nl/CalculateQuoteAndPolicyValuesLifeRetail_response " +
						"WsdlTest/CalculateQuoteAndPolicyValuesLifeRetail/xsd/CalculationRespons.xsd"),
			"http://wub2nn.nn.nl/CalculateQuoteAndPolicyValuesLifeRetail", "WsdlTest/CalculateQuoteAndPolicyValuesLifeRetail");
		WsdlGenerator wsdl = new WsdlGenerator(pipe);
		wsdl.init();
		wsdl.setUseIncludes(true);
		test(wsdl, "WsdlTest/CalculateQuoteAndPolicyValuesLifeRetail.test.wsdl");
	}

	@Test
	public void wubCalculateQuoteAndPolicyValuesLifeRetailMixed() throws Exception {
		XmlValidator inputValidator=getXmlValidatorInstance("CalculationRequest", "CalculationResponse", null,
				"http://wub2nn.nn.nl/CalculateQuoteAndPolicyValuesLifeRetail WsdlTest/CalculateQuoteAndPolicyValuesLifeRetail/xsd/CalculationRequestv2.1.xsd "+
				"http://wub2nn.nn.nl/CalculateQuoteAndPolicyValuesLifeRetail_response  WsdlTest/CalculateQuoteAndPolicyValuesLifeRetail/xsd/CalculationRespons.xsd");
		inputValidator.configure();
		IValidator outputValidator = inputValidator.getResponseValidator();
		PipeLine pipe = mockPipeLine(inputValidator, outputValidator,
			"http://wub2nn.nn.nl/CalculateQuoteAndPolicyValuesLifeRetail", "WsdlTest/CalculateQuoteAndPolicyValuesLifeRetail");
		WsdlGenerator wsdl = new WsdlGenerator(pipe);
		wsdl.init();
		wsdl.setUseIncludes(true);
		test(wsdl, "WsdlTest/CalculateQuoteAndPolicyValuesLifeRetail.test.wsdl");
	}

	@Test
	public void wubFindIntermediary() throws Exception {
		PipeLine pipe = mockPipeLine(
			getXmlValidatorInstance("FindIntermediaryREQ", null, null,
				"http://wub2nn.nn.nl/FindIntermediary WsdlTest/FindIntermediary/xsd/XSD_FindIntermediary_v1.1_r1.0.xsd"),
			getXmlValidatorInstance("FindIntermediaryRLY", null, null,
				"http://wub2nn.nn.nl/FindIntermediary WsdlTest/FindIntermediary/xsd/XSD_FindIntermediary_v1.1_r1.0.xsd"),
			"http://wub2nn.nn.nl/FindIntermediary", "WsdlTest/FindIntermediary");
		WsdlGenerator wsdl = new WsdlGenerator(pipe);
		wsdl.init();
		wsdl.setUseIncludes(true);
		assertTrue(wsdl.isUseIncludes());
		test(wsdl, "WsdlTest/FindIntermediary.test.wsdl");
		zip(wsdl);
		// assertEquals(2, wsdl.getXSDs(true).size()); TODO?
	}

	@Test
	public void wubFindIntermediaryMixed() throws Exception {
		XmlValidator inputValidator=getXmlValidatorInstance("FindIntermediaryREQ", "FindIntermediaryRLY", null,
						"http://wub2nn.nn.nl/FindIntermediary WsdlTest/FindIntermediary/xsd/XSD_FindIntermediary_v1.1_r1.0.xsd");
		IValidator outputValidator = inputValidator.getResponseValidator();
		PipeLine pipe = mockPipeLine(inputValidator, outputValidator, "http://wub2nn.nn.nl/FindIntermediary", "WsdlTest/FindIntermediary");
		WsdlGenerator wsdl = new WsdlGenerator(pipe);
		wsdl.setUseIncludes(true);
		wsdl.init();
		assertTrue(wsdl.isUseIncludes());
		test(wsdl, "WsdlTest/FindIntermediary.test.wsdl");
		zip(wsdl);
		// assertEquals(2, wsdl.getXSDs(true).size()); TODO?
	}

	protected void test(WsdlGenerator wsdl, String testWsdl) throws Exception {
		wsdl.setDocumentation("test");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wsdl.wsdl(out, "Test");
		String result = new String(out.toByteArray());

		String expected = TestFileUtils.getTestFile("/"+testWsdl);
		assertXmlEquals(expected, result);
	}

	protected void zip(WsdlGenerator wsdl) throws IOException, Exception {
		File dir = new File(System.getProperty("java.io.tmpdir") + File.separator + "zipfiles");
		File zipFile = new File(dir, wsdl.getName() + ".zip");
		zipFile.getParentFile().mkdirs();
		System.out.println("Creating " + zipFile.getAbsolutePath());
		wsdl.zip(new FileOutputStream(zipFile), "http://myserver/");
	}

	protected XmlValidator getXmlValidatorInstance(String rootTag, String schema, String schemaLocation) throws Exception {
		return getXmlValidatorInstance(rootTag, null, schema, schemaLocation);
	}

	protected XmlValidator getXmlValidatorInstance(String rootTag, String responseRootTag, String schema, String schemaLocation) throws Exception {
		XmlValidator validator = XmlValidatorTest.getUnconfiguredValidator(schemaLocation, implementation);
		validator.setSchema(schema);
		validator.setRoot(rootTag);
		if (responseRootTag!=null) {
			validator.setResponseRoot(responseRootTag);
		}
		validator.configure();
		validator.start();
		return validator;
	}

	protected PipeLine mockPipeLine(IValidator inputValidator, IValidator outputValidator, String targetNamespace, String adapterName) {
		PipeLine simple = mock(PipeLine.class);
		when(simple.getInputValidator()).thenReturn(inputValidator);
		when(simple.getOutputValidator()).thenReturn(outputValidator);
		Adapter adp = mock(Adapter.class);
		when(simple.getAdapter()).thenReturn(adp);
		Configuration cfg = mock(Configuration.class);
		when(simple.getAdapter().getConfiguration()).thenReturn(cfg);
		final Receiver receiverBase = mock(Receiver.class);
		WebServiceListener listener = new WebServiceListener();
		listener.setServiceNamespaceURI(targetNamespace);
		when(receiverBase.getListener()).thenReturn(listener);
		when(adp.getReceivers()).thenAnswer(new Answer<Iterable<Receiver>>() {
			public Iterable<Receiver> answer(InvocationOnMock invocation) throws Throwable {
				return Arrays.asList(receiverBase);
			}
		});
		when(adp.getName()).thenReturn(adapterName);
		when(cfg.getClassLoader()).thenReturn(this.getClass().getClassLoader());
		when(adp.getConfigurationClassLoader()).thenReturn(this.getClass().getClassLoader());
		when(simple.getConfigurationClassLoader()).thenReturn(this.getClass().getClassLoader());
		return simple;
	}

	static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory docBuilderFactory = XmlUtils.getDocumentBuilderFactory();
		docBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		return docBuilder;
	}


}
