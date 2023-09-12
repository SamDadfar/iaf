/*
   Copyright 2013, 2020 Nationale-Nederlanden, 2020, 2022-2023 WeAreFrank!

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package nl.nn.adapterframework.pipes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import nl.nn.adapterframework.configuration.ConfigurationException;
import nl.nn.adapterframework.core.PipeLineSession;
import nl.nn.adapterframework.core.PipeRunException;
import nl.nn.adapterframework.core.PipeRunResult;
import nl.nn.adapterframework.doc.ElementType;
import nl.nn.adapterframework.doc.ElementType.ElementTypes;
import nl.nn.adapterframework.stream.Message;
import nl.nn.adapterframework.util.XmlEncodingUtils;

/**
 * Replaces all occurrences of one string with another.
 *
 * @author Gerrit van Brakel
 * @since 4.2
 */
@ElementType(ElementTypes.TRANSLATOR)
public class ReplacerPipe extends FixedForwardPipe {

	private String find;
	private String replace;
	private String lineSeparatorSymbol = null;
	private boolean replaceNonXmlChars = false;
	private String replaceNonXmlChar = null;
	private boolean allowUnicodeSupplementaryCharacters = false;

	{
		setSizeStatistics(true);
	}

	@Override
	public void configure() throws ConfigurationException {
		super.configure();
//		if (StringUtils.isEmpty(getFind())) {
//			throw new ConfigurationException("cannot have empty find-attribute");
//		}
		if (StringUtils.isNotEmpty(getFind())) {
			if (getReplace() == null) {
				throw new ConfigurationException("cannot have a null replace-attribute");
			}
			log.info("finds [{}] replaces with [{}]", getFind(), getReplace());
			if (!StringUtils.isEmpty(getLineSeparatorSymbol())) {
				find = find != null ? find.replace(lineSeparatorSymbol, System.getProperty("line.separator")) : null;
				replace = replace != null ? replace.replace(lineSeparatorSymbol, System.getProperty("line.separator")) : null;
			}
		}
		if (isReplaceNonXmlChars()) {
			if (getReplaceNonXmlChar() != null) {
				if (getReplaceNonXmlChar().length() > 1) {
					throw new ConfigurationException("replaceNonXmlChar [" + getReplaceNonXmlChar() + "] has to be one character");
				}
			}
		}
	}

	@Override
	public PipeRunResult doPipe(Message message, PipeLineSession session) throws PipeRunException {
		try {
			InputStream input = message.asInputStream();
			if (input == null) {
				return new PipeRunResult(getSuccessForward(), new Message(new ByteArrayInputStream(new byte[0])));
			}
			try {
				InputStreamReader inputStreamReader = new InputStreamReader(input);
				 BufferedReader reader = new BufferedReader(inputStreamReader);
				 PipedOutputStream outputStream = new PipedOutputStream();
				 PipedInputStream modifiedInputStream = new PipedInputStream(outputStream);
				 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

				List<String> findPattern;
				List<Replacement> replacements = new ArrayList<>();
				int matchedIndex = 0;

				findPattern = StringUtils.isNotEmpty(getFind()) && getFind().contains("\n")
							  ? Arrays.asList(getFind().split("\n"))
							  : Collections.singletonList(getFind());
				String line;
				while ((line = reader.readLine()) != null) {
					if (findPattern.size() == 1) {
						line = line.replace(getFind(), getReplace());
						writeToOutput(writer, line);
					} else {
						Replacement findMatch = findPattern(line, findPattern.get(matchedIndex));
						if (findMatch != null) {
							if (matchedIndex == replacements.size()) {
								replacements.add(findMatch);
								matchedIndex++;
							}
							if (replacements.size() == findPattern.size()) {
								applyReplacements(replacements, writer);
								replacements.clear();
								matchedIndex = 0;
							}
						} else {
							if (!replacements.isEmpty()) {
								for (Replacement replacement : replacements) {
									writeToOutput(writer, replacement.line);
								}
							}
							replacements.clear();
							writeToOutput(writer, line);
						}
					}
				}
				if (!replacements.isEmpty()) {
					for (Replacement replacement : replacements) {
						writeToOutput(writer, replacement.line);
					}
				}
				writer.flush();
				return new PipeRunResult(getSuccessForward(), modifiedInputStream);
			} catch (IOException e) {
				throw new PipeRunException(this, "Error processing stream", e);
			}
		} catch (IOException e) {
			throw new PipeRunException(this, "cannot open stream", e);
		}
	}

	private void writeToOutput(BufferedWriter writer, String line) throws IOException {
		if (isReplaceNonXmlChars()) {
			if (StringUtils.isEmpty(getReplaceNonXmlChar())) {
				line = XmlEncodingUtils.stripNonValidXmlCharacters(line, isAllowUnicodeSupplementaryCharacters());
			} else {
				line = XmlEncodingUtils.replaceNonValidXmlCharacters(line, getReplaceNonXmlChar().charAt(0), false, isAllowUnicodeSupplementaryCharacters());
			}
		}
		writer.write(line);
		writer.newLine();
	}

	private void applyReplacements(List<Replacement> replacements, BufferedWriter writer) throws IOException {
		StringBuilder modifiedLines = new StringBuilder();
		Replacement first = replacements.get(0);
		Replacement last = replacements.get(replacements.size() - 1);
		modifiedLines.append(first.line, 0, first.start);
		modifiedLines.append(getReplace());
		if (replacements.size() > 2) {
			modifiedLines.append(System.lineSeparator());
			modifiedLines.append(last.line, last.end, last.line.length());
		}
		writeToOutput(writer, modifiedLines.toString());
	}

	Replacement findPattern(String line, String pattern) {
		Replacement result = null;
		int startPos = 0;
		int index;
		if ((index = line.indexOf(pattern, startPos)) != -1) {
			result = new Replacement(line, index, index + pattern.length(), pattern);
		}
		return result;
	}

	private static class Replacement {
		int start;
		int end;
		String replace;
		String line;

		Replacement(String line, int start, int end, String replace) {
			this.start = start;
			this.end = end;
			this.replace = replace;
			this.line = line;
		}
	}

	/**
	 * Sets the string that is searched for.
	 */
	public void setFind(String find) {
		this.find = find;
	}

	public String getFind() {
		return find;
	}

	/**
	 * Sets the string that will replace each of the occurrences of the find-string.
	 */
	public void setReplace(String replace) {
		this.replace = replace;
	}

	public String getReplace() {
		return replace;
	}

	/**
	 * Sets the string the representation in find and replace of the line separator.
	 */
	public String getLineSeparatorSymbol() {
		return lineSeparatorSymbol;
	}

	/** sets the string the representation in find and replace of the line separator */
	public void setLineSeparatorSymbol(String string) {
		lineSeparatorSymbol = string;
	}

	/**
	 * Replace all non XML chars (not in the <a href="http://www.w3.org/TR/2006/REC-xml-20060816/#NT-Char">character range as specified by the XML specification</a>) with {@link XmlEncodingUtils#replaceNonValidXmlCharacters(String, char, boolean, boolean) replaceNonValidXmlCharacters}
	 *
	 * @ff.default false
	 */
	public void setReplaceNonXmlChars(boolean b) {
		replaceNonXmlChars = b;
	}

	public boolean isReplaceNonXmlChars() {
		return replaceNonXmlChars;
	}

	/**
	 * character that will replace each non valid xml character (empty string is also possible) (use &amp;#x00bf; for inverted question mark)
	 *
	 * @ff.default empty string
	 */
	public void setReplaceNonXmlChar(String replaceNonXmlChar) {
		this.replaceNonXmlChar = replaceNonXmlChar;
	}

	public String getReplaceNonXmlChar() {
		return replaceNonXmlChar;
	}

	/**
	 * Whether to allow Unicode supplementary characters (like a smiley) during {@link XmlEncodingUtils#replaceNonValidXmlCharacters(String, char, boolean, boolean) replaceNonValidXmlCharacters}
	 *
	 * @ff.default false
	 */
	public void setAllowUnicodeSupplementaryCharacters(boolean b) {
		allowUnicodeSupplementaryCharacters = b;
	}

	public boolean isAllowUnicodeSupplementaryCharacters() {
		return allowUnicodeSupplementaryCharacters;
	}


}
