package nl.nn.adapterframework.http.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import nl.nn.adapterframework.parameters.Parameter;
import nl.nn.adapterframework.testutil.MatchUtils;
import nl.nn.adapterframework.testutil.ParameterBuilder;
import nl.nn.adapterframework.testutil.TestFileUtils;
import nl.nn.adapterframework.testutil.threading.IsolatedThread;
import nl.nn.adapterframework.testutil.threading.RunInThreadRule;

public class OpenApiTest extends OpenApiTestBase {

	@Rule
	public RunInThreadRule runInThread = new RunInThreadRule();

	@Test
	@IsolatedThread
	public void simpleEndpointGetTest() throws Exception {
		String uri="/users";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "get", null)
			.setInputValidator("simple.xsd", null, "user", null)
			.addExit(200)
			.addExit(500)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
		String result = callOpenApi(uri);

		String expected = TestFileUtils.getTestFile("/OpenApi/simple.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void simpleEndpointPostTest() throws Exception {
		String uri="/simpleEndpointPostTest";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "post", null)
			.setInputValidator("simple.xsd", null, "user", null)
			.addExit(200)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
		String result = callOpenApi(uri);

		String expected = TestFileUtils.getTestFile("/OpenApi/simplePost.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void testChoiceWithComplexType() throws Exception {
		String uri="/transaction";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "post", null)
			.setInputValidator("transaction.xsd", "transaction", null, null)
			.addExit(200)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
		String result = callOpenApi(uri);

		String expected = TestFileUtils.getTestFile("/OpenApi/transaction.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void testChoiceWithSimpleType() throws Exception {
		String uri="/options";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "post", null)
			.setInputValidator("Options.xsd", "Options", null, null)
			.addExit(200)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
		String result = callOpenApi(uri);

		String expected = TestFileUtils.getTestFile("/OpenApi/Options.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void testMultipleChoices() throws Exception {
		String uri="/multipleChoices";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "post", null)
			.setInputValidator("multipleChoices.xsd", "EmbeddedChoice", null, null)
			.addExit(200)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
		String result = callOpenApi(uri);

		String expected = TestFileUtils.getTestFile("/OpenApi/multipleChoices.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void simpleEndpointPostWithEmptyExitTest() throws Exception {
		String uri="/simpleEndpointPostWithEmptyExitTest";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "post", null)
			.setInputValidator("simple.xsd", null, "user", null)
			.addExit(200)
			.addExit(500, null, true)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
		String result = callOpenApi(uri);

		String expected = TestFileUtils.getTestFile("/OpenApi/simplePostWithEmptyExit.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void simpleEndpointWithOperationIdTest() throws Exception {
		String uri="/simpleEndpointWithOperationIdTest";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "get envelope adapter description")
			.setListener(uri, "get", "operationId")
			.setInputValidator("envelope.xsd", "EnvelopeRequest", "EnvelopeResponse", null)
			.addExit(200)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
		String result = callOpenApi(uri);

		String expected = TestFileUtils.getTestFile("/OpenApi/envelope.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void simpleEndpointQueryParamTest() throws Exception {
		String uri="/simpleEndpointQueryParamTest";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());
		Parameter param = ParameterBuilder.create("parameter", "parameter").withSessionKey("parameter");

		new AdapterBuilder("myAdapterName", "get envelope adapter description")
			.setListener(uri, "get", null)
			.setInputValidator("envelope.xsd", "EnvelopeRequest", "EnvelopeResponse", param)
			.addExit(200)
			.build(true);

		new AdapterBuilder("myAdapterName", "get envelope adapter description")
			.setListener(uri, "post", null)
			.setInputValidator("envelope.xsd", "EnvelopeRequest", "EnvelopeResponse", param)
			.addExit(200)
			.build(true);

		assertEquals("more then 2 registered pattern found!", 2, dispatcher.findConfigForUri(uri).getMethods().size());
		String result = callOpenApi(uri);

		String expected = TestFileUtils.getTestFile("/OpenApi/envelopeQueryParam.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void pathParamQueryParamTest() throws Exception {
		String uri="/pathParamQueryParamTest";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());
		Parameter param = ParameterBuilder.create("parameter", "parameter").withSessionKey("parameter");

		new AdapterBuilder("myAdapterName", "get envelope adapter description")
			.setListener(uri+"/{pattern}", "get", null)
			.setInputValidator("envelope.xsd", "EnvelopeRequest", "EnvelopeResponse", param)
			.addExit(200)
			.addExit(500)
			.addExit(403)
			.build(true);

		new AdapterBuilder("myAdapterName", "get envelope adapter description")
			.setListener(uri+"/{pattern}/sub/{path}", "post", null)
			.setInputValidator("envelope.xsd", "EnvelopeRequest", "EnvelopeResponse", param)
			.addExit(200)
			.addExit(500)
			.addExit(403)
			.build(true);

		assertEquals("more then 2 registered pattern found!", 2, dispatcher.findMatchingConfigsForUri(uri).size());
		String expected = TestFileUtils.getTestFile("/OpenApi/envelopePathParamQueryParam.json");

		String result = callOpenApi(uri+"/{pattern}");
		MatchUtils.assertJsonEquals(expected, result);

		String encodedResult = callOpenApi(uri+"/%7Bpattern%7D");
		MatchUtils.assertJsonEquals("Test should pass in escaped form!", expected, encodedResult);
	}

	@Test
	@IsolatedThread
	public void exitElementNamesTest() throws Exception {
		String uri="/envelope";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());
		Parameter param = ParameterBuilder.create("parameter", "parameter").withSessionKey("parameter");

		String responseRoot = "EnvelopeResponse,EnvelopeError403,EnvelopeError500";
		new AdapterBuilder("myAdapterName", "each exit have specific element name")
			.setListener(uri, "get", null)
			.setInputValidator("envelope.xsd", "EnvelopeRequest", responseRoot, param)
			.addExit(200,"EnvelopeResponse", false)
			.addExit(500,"EnvelopeError500", false)
			.addExit(403,"EnvelopeError403", false)
			.build(true);

		new AdapterBuilder("myAdapterName", "200 code will retrieve the ref from first of response root")
			.setListener(uri+"/test", "get", null)
			.setInputValidator("envelope.xsd", "EnvelopeRequest", responseRoot, param)
			.addExit(200,null,false)
			.addExit(500,"EnvelopeError500", false)
			.addExit(403,"EnvelopeError403", false)
			.build(true);

		new AdapterBuilder("myAdapterName", "no element name responseRoot will be used as source for refs")
			.setListener(uri+"/elementNames", "get", null)
			.setInputValidator("envelope.xsd", "EnvelopeRequest", responseRoot, param)
			.addExit(200)
			.addExit(500)
			.addExit(403)
			.build(true);

		new AdapterBuilder("myAdapterName", "403 empty exit")
			.setListener(uri+"/{pattern}/sub/{path}", "post", null)
			.setInputValidator("envelope.xsd", "EnvelopeRequest", responseRoot, param)
			.addExit(200)
			.addExit(500)
			.addExit(403, null, true)
			.build(true);

		assertEquals("more then 4 registered pattern found!", 4, dispatcher.findMatchingConfigsForUri(uri).size());
		String result = callOpenApi(uri);

		String expected = TestFileUtils.getTestFile("/OpenApi/envelopeExits.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void petStore() throws Exception {
		String uriBase="/pets";
		//Make sure all adapters have been registered on the dispatcher
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uriBase).size());

		new AdapterBuilder("listPets", "List all pets")
			.setListener(uriBase, "get", null)
			.setInputValidator("petstore.xsd", null, "Pets", null)
			.addExit(200)
			.addExit(500, "Error", false)
			.build(true);

		new AdapterBuilder("createPets", "Create a pet")
			.setListener(uriBase, "post", null)
			.setInputValidator("petstore.xsd", "Pet", "Pet", null)
			.addExit(201, null, true)
			.addExit(500, "Error", false)
			.build(true);

		new AdapterBuilder("showPetById", "Info for a specific pet")
			.setListener(uriBase+"/{petId}", "get", null)
			.setInputValidator("petstore.xsd", null, "Pet", null)
			.addExit(200)
			.addExit(500, "Error", false)
			.build(true);

		//getPets.start(getPets, postPet, getPet); //Async start

		// Thread.sleep(1200); //Adding a small timeout to fix async starting issues

		assertNotNull("unable to find DispatchConfig for uri [pets]", dispatcher.findConfigForUri(uriBase));
		assertEquals("not all listener uri [pets] are registered on the dispatcher", 2, dispatcher.findConfigForUri(uriBase).getMethods().size());
		assertNotNull("unable to find DispatchConfig for uri [pets/a]", dispatcher.findConfigForUri(uriBase+"/a"));
		assertEquals("listener uri [pets/a] not registered on dispatcher", 1, dispatcher.findConfigForUri(uriBase+"/a").getMethods().size());

		String result = callOpenApi(uriBase);

		String expected = TestFileUtils.getTestFile("/OpenApi/petstore.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void rootSchemaTest() throws Exception {
		String uri="/";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri+"users", "get", null)
			.setInputValidator("simple.xsd", null, "user", null)
			.addExit(200)
			.addExit(500)
			.build(true);

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri+"test", "get", null)
			.setInputValidator("simple.xsd", null, "user", null)
			.addExit(200)
			.addExit(500)
			.build(true);

		assertEquals("more then 2 registered pattern found!", 2, dispatcher.findMatchingConfigsForUri(uri).size());
		String result = callOpenApi(uri+"users");

		String expected = TestFileUtils.getTestFile("/OpenApi/simpleRoot.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void twoEndpointsOneWithoutValidatorTest() throws Exception {
		String uri="/path";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri+"/validator", "get", null)
			.setInputValidator("simple.xsd", null, "user", null)
			.addExit(200)
			.addExit(500)
			.build(true);

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri+"/noValidator", "get", null)
			.addExit(200)
			.addExit(500)
			.build(true);

		assertEquals("more then 2 registered pattern found!", 2, dispatcher.findMatchingConfigsForUri(uri).size());
		String result = callOpenApi(uri+"/validator");

		String expected = TestFileUtils.getTestFile("/OpenApi/noValidatorForOneEndpoint.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void parametersFromHeader() throws Exception {
		String uri="/headerparams";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "get", null, null)
			.setHeaderParams("envelopeId, envelopeType")
			.setInputValidator("simple.xsd", null, "user", null)
			.addExit(200)
			.addExit(500)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
		MockHttpServletRequest request = createRequest("GET", uri+"/openapi.json");
		request.addHeader("envelopeId", "dummy");
		request.addHeader("envelopeType", "dummyType");

		String result = service(request);

		String expected = TestFileUtils.getTestFile("/OpenApi/twoHeaderParams.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

//	@Test
//	@IsolatedThread
//	public void parametersFromCookie() throws Exception {
//		String uri="/cookieparams";
//		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
//		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());
//
//		new AdapterBuilder("myAdapterName", "description4simple-get")
//			.setListener(uri, "get", null, null)
//			.setCookieParams("envelopeId, envelopeType")
//			.setValidator("simple.xsd", null, "user", null)
//			.addExit(200)
//			.addExit(500)
//			.build(true);
//
//		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
//		MockHttpServletRequest request = new MockHttpServletRequest("GET", uri + "/openapi.json");
//		request.setServerName("dummy");
//		request.setPathInfo(uri + "/openapi.json");
//		Cookie[] cookies = {new Cookie("envelopeId", "dummy"), new Cookie("envelopeType", "dummyType")};
//		request.setCookies(cookies);
//
//		String result = service(request);
//
//		String expected = TestFileUtils.getTestFile("/OpenApi/cookieParams.json");
//		TestAssertions.assertEqualsIgnoreCRLF(expected, result);
//	}

//	@Test
//	@IsolatedThread
//	public void parametersFromCookieAndHeader() throws Exception {
//		String uri="/cookieplusheaderparams";
//		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
//		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());
//
//		new AdapterBuilder("myAdapterName", "description4simple-get")
//			.setListener(uri, "get", null, null)
//			.setHeaderParams("headerparam")
//			.setCookieParams("envelopeId, envelopeType")
//			.setValidator("simple.xsd", null, "user", null)
//			.addExit(200)
//			.addExit(500)
//			.build(true);
//
//		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
//		MockHttpServletRequest request = new MockHttpServletRequest("GET", uri + "/openapi.json");
//		request.setServerName("dummy");
//		request.setPathInfo(uri + "/openapi.json");
//		Cookie[] cookies = {new Cookie("envelopeId", "dummy"), new Cookie("envelopeType", "dummyType")};
//		request.setCookies(cookies);
//		request.addHeader("headerparam", "dummy");
//
//		String result = service(request);
//
//		String expected = TestFileUtils.getTestFile("/OpenApi/parametersFromCookieAndHeader.json");
//		TestAssertions.assertEqualsIgnoreCRLF(expected, result);
//	}

	@Test
	@IsolatedThread
	public void validatorParamFromHeaderNotQuery() throws Exception {
		String uri="/validatorParamFromHeaderNotQuery";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());
		Parameter param = ParameterBuilder.create("parameter", "parameter").withSessionKey("parameter");

		new AdapterBuilder("myAdapterName", "get envelope adapter description")
			.setListener(uri, "get", null, null)
			.setHeaderParams("parameter")
			.setInputValidator("envelope.xsd", "EnvelopeRequest", "EnvelopeResponse", param)
			.addExit(200)
			.build(true);

		assertEquals("more then 2 registered pattern found!", 1, dispatcher.findConfigForUri(uri).getMethods().size());
		MockHttpServletRequest request = createRequest("GET", uri+"/openapi.json");
		request.addHeader("parameter", "dummy");

		String result = service(request);

		String expected = TestFileUtils.getTestFile("/OpenApi/validatorParamFromHeaderNotQuery.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void messageIdHeaderTest() throws Exception {
		String uri="/messageIdHeaderTest";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "get envelope adapter description")
			.setListener(uri, "get", null, null)
			.setMessageIdHeader("x-message-id")
			.setInputValidator("envelope.xsd", "EnvelopeRequest", "EnvelopeResponse", null)
			.addExit(200)
			.build(true);


		assertEquals("more then 2 registered pattern found!", 1, dispatcher.findConfigForUri(uri).getMethods().size());
		MockHttpServletRequest request = createRequest("GET", uri+"/openapi.json");
		request.addHeader("x-message-id", "dummy");

		String result = service(request);

		String expected = TestFileUtils.getTestFile("/OpenApi/messageIdHeaderTest.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void testHeaderParamIsnotAddedAsQueryParam() throws Exception {
		String uri="/headerparams";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());
		Parameter p = ParameterBuilder.create("envelopeId", "envelopeType").withSessionKey("headers");
		p.setXpathExpression("/headers/header[@name='envelopeId']");

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "get", null, null)
			.setHeaderParams("envelopeId, envelopeType")
			.setInputValidator("simple.xsd", null, "user", p)
			.addExit(200)
			.addExit(500)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());
		MockHttpServletRequest request = createRequest("GET", uri+"/openapi.json");
		request.addHeader("envelopeId", "dummy");
		request.addHeader("envelopeType", "dummyType");

		String result = service(request);

		String expected = TestFileUtils.getTestFile("/OpenApi/twoHeaderParams.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void testOutputValidator() throws Exception {
		String uri="/outputValidator";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "get", null, null)
			.setOutputValidator("simple.xsd", "user")
			.addExit(200)
			.addExit(500)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());

		String result = callOpenApi(uri);
		String expected = TestFileUtils.getTestFile("/OpenApi/outputValidator.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void testInputOutputValidator() throws Exception {
		String uri="/outputValidator";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "post", null, null)
			.setInputValidator("envelope.xsd", "EnvelopeRequest", "EnvelopeResponse, EnvelopeError500", null)
			.setOutputValidator("simple.xsd", "user")
			.addExit(200)
			.addExit(500, "EnvelopeError500", false)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());

		String result = callOpenApi(uri);
		String expected = TestFileUtils.getTestFile("/OpenApi/inputOutputValidators.json");
		MatchUtils.assertJsonEquals(expected, result);
	}

	@Test
	@IsolatedThread
	public void testWithoutValidator() throws Exception {
		String uri="/noValidator";
		ApiServiceDispatcher dispatcher = ApiServiceDispatcher.getInstance();
		assertEquals("there are still registered patterns! Threading issue?", 0, dispatcher.findMatchingConfigsForUri(uri).size());

		new AdapterBuilder("myAdapterName", "description4simple-get")
			.setListener(uri, "get", null, null)
			.addExit(200)
			.addExit(500)
			.build(true);

		assertEquals("more then 1 registered pattern found!", 1, dispatcher.findMatchingConfigsForUri(uri).size());

		String result = callOpenApi(uri);
		String expected = TestFileUtils.getTestFile("/OpenApi/noValidator.json");
		MatchUtils.assertJsonEquals(expected, result);
	}
}
