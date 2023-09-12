package nl.nn.adapterframework.pipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hamcrest.Matchers;
import org.junit.Test;

import nl.nn.adapterframework.configuration.ConfigurationException;
import nl.nn.adapterframework.core.PipeRunResult;


/**
 * ReplacerPipe Tester.
 *
 * @author <Sina Sen>
 */
public class ReplacerPipeTest extends PipeTestBase<ReplacerPipe> {

	@Override
	public ReplacerPipe createPipe() {
		return new ReplacerPipe();
	}

	@Test
	public void everythingNull() throws Exception {
		pipe.setFind("laa");

		ConfigurationException e = assertThrows(ConfigurationException.class, this::configurePipe);
		assertThat(e.getMessage(), Matchers.containsString("cannot have a null replace-attribute"));
	}

	@Test
	public void getFindEmpty() throws Exception {
		pipe.setFind("");
		configureAndStartPipe();
		PipeRunResult res = doPipe(pipe, "dsf", session);
		assertFalse(res.getPipeForward().getName().isEmpty());

	}

	@Test
	public void testConfigureWithSeperator() throws Exception {
		pipe.setFind("sina/murat/niels");
		pipe.setLineSeparatorSymbol("/");
		pipe.setReplace("yo");
		pipe.setAllowUnicodeSupplementaryCharacters(true);
		configureAndStartPipe();

		doPipe(pipe, pipe.getFind(), session);
		assertFalse(pipe.getFind().isEmpty());
	}

	@Test
	public void replaceNonXMLChar() throws Exception {
		pipe.setFind("test");
		pipe.setReplace("head");
		pipe.setReplaceNonXmlChar("l");
		pipe.setReplaceNonXmlChars(true);
		pipe.setAllowUnicodeSupplementaryCharacters(true);
		configureAndStartPipe();

		PipeRunResult res = doPipe(pipe, "<test>\bolo</test>/jacjac:)", session);
		assertEquals("<head>lolo</head>/jacjac:)", res.getResult().asString());
	}

	@Test
	public void replaceStringSuccess() throws Exception {
		pipe.setReplaceNonXmlChars(false);
		configureAndStartPipe();

		PipeRunResult res = doPipe(pipe, "\b", session);
		assertEquals("\b", res.getResult().asString());
	}

	@Test
	public void replaceNonXMLCharLongerThanOne() throws Exception {
		pipe.setFind("test");
		pipe.setReplace("head");
		pipe.setReplaceNonXmlChar("klkl");
		pipe.setReplaceNonXmlChars(true);

		ConfigurationException e = assertThrows(ConfigurationException.class, this::configurePipe);
		assertThat(e.getMessage(), Matchers.containsString("replaceNonXmlChar [klkl] has to be one character"));
	}

	@Test
	public void replacementWithInputStream() throws Exception {
		// Create the ReplacerPipe and set the find and replace values

		pipe.setFind("test\nto test replacerPipe\n");
		pipe.setReplace("<TEST>");
		String inputString = "Hello this is great test\nto test replacerPipe\nwhen it supports inputStream, replace test with <TEST>!";
		// Invoke the doPipe method
		PipeRunResult result = doPipe(pipe, new ByteArrayInputStream(inputString.getBytes()), session);
		InputStream modifiedInputStream = result.getResult().asInputStream();

		String modifiedContentString = readInputStreamAsString(modifiedInputStream);

		// Verify the result
		assertThat(modifiedContentString, Matchers.containsString("Hello this is great <TEST>\nwhen it supports inputStream, replace test with <TEST>!"));

	}

	@Test
	public void replacementInputStreamFromXMLFile() throws Exception {
		// Set the find and replace values
		pipe.setFind("</price>");
		pipe.setReplace("$</price>");
		pipe.setReplaceNonXmlChars(true);
		// Read file in an input stream
		String filePath = "src/test/resources/Pipes/books.xml";
		try {
			byte[] xmlBytes = readBytesFromFile(filePath);

			// Invoke the doPipe method
			InputStream input = new ByteArrayInputStream(xmlBytes);
			PipeRunResult result = doPipe(pipe, input, session);

			// Verify the result
			String actualResult = result.getResult().asString();
			assertThat(actualResult, Matchers.containsString("$</price>"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static byte[] readBytesFromFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		return Files.readAllBytes(path);
	}

	private String readInputStreamAsString(InputStream inputStream) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			StringBuilder result = new StringBuilder();
			String line;
			boolean firstLine = true;
			while ((line = reader.readLine()) != null) {
				if (!firstLine) {
					result.append("\n"); // Add newline before each line (except the first)
				}
				firstLine = false;
				result.append(line);
			}
			return result.toString();
		}
	}
}



