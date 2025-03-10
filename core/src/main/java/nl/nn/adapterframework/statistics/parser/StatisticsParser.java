/*
   Copyright 2013 Nationale-Nederlanden

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
package nl.nn.adapterframework.statistics.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;

import nl.nn.adapterframework.configuration.ConfigurationException;
import nl.nn.adapterframework.statistics.ItemList;
import nl.nn.adapterframework.util.EncapsulatingReader;
import nl.nn.adapterframework.util.LogUtil;
import nl.nn.adapterframework.util.XmlBuilder;

/**
 * Parser for statistics files generated by the framework.
 * 
 * @author  Gerrit van Brakel
 * @since   4.9.10
 */
public class StatisticsParser {
	protected Logger log = LogUtil.getLogger(this);

	private final String ROOT_ELEM_NAME="root";

	private String name;
	private String targetAdaptername;
	private String currentTimestamp;
	private String targetTimestamp;
	private boolean skip=false;

	private Map data = new LinkedHashMap();
	private Set adapters = new LinkedHashSet();
	private List timestamps = new LinkedList();

	public StatisticsParser(String targetAdaptername, String targetTimestamp) {
		super();
		this.targetAdaptername=targetAdaptername;
		this.targetTimestamp=targetTimestamp;
	}

	public void registerRecord(SummaryRecord record) {
		if (!skip) {
			String tag=record.getName();
			if (!adapters.contains(tag)) {
				adapters.add(tag);
			}
			if (StringUtils.isNotEmpty(targetAdaptername)) {
				if (!targetAdaptername.equals(tag)) {
					return;
				}
				if (StringUtils.isEmpty(targetTimestamp)) {
					tag=currentTimestamp;
				}
			}
			SummaryRecord current=(SummaryRecord)data.get(tag);
			if (current==null) {
				data.put(tag,record);
			} else {
				current.addRecord(record);
			}
		}
	}

	public  void digestStatistics(String filename) throws ConfigurationException, FileNotFoundException {
		digestStatistics(new FileReader(filename),filename);
	}

	public  void digestStatistics(Reader reader, String sysid) throws ConfigurationException {

		Reader fileReader = new EncapsulatingReader(reader, "<"+ROOT_ELEM_NAME+">", "</"+ROOT_ELEM_NAME+">", false);
		Digester digester = new Digester();
		digester.setUseContextClassLoader(true);

		// push config on the stack
		digester.push(this);

		try {
//			String prefix="/"+ROOT_ELEM_NAME+"/";
			String prefix="*/";
			digester.addSetProperties(prefix+"statisticsCollection"); // timestamp info
			digester.addSetProperties(prefix+"statisticsCollection/statgroup"); // instance info
			digester.addObjectCreate (prefix+"statisticsCollection/statgroup/statgroup",SummaryRecord.class); // adapterinfo
			digester.addSetProperties(prefix+"statisticsCollection/statgroup/statgroup");
			digester.addSetNext      (prefix+"statisticsCollection/statgroup/statgroup","registerRecord");
			digester.addObjectCreate (prefix+"statisticsCollection/statgroup/statgroup/stat/interval/item",Item.class); // adapterinfo
			digester.addSetProperties(prefix+"statisticsCollection/statgroup/statgroup/stat/interval/item");
			digester.addSetNext      (prefix+"statisticsCollection/statgroup/statgroup/stat/interval/item","registerItem");

			InputSource is= new InputSource(fileReader);

			digester.parse(is);

		} catch (Exception e) {
			// wrap exception to be sure it gets rendered via the IbisException-renderer
			throw new ConfigurationException("error during parsing of file ["+sysid +"]", e);
		}
	}

	public XmlBuilder toXml() {
		DecimalFormat df=new DecimalFormat(ItemList.ITEM_FORMAT_TIME);
		DecimalFormat pf=new DecimalFormat(ItemList.ITEM_FORMAT_PERC);

		XmlBuilder result=new XmlBuilder("overview");
		result.addAttribute("instance",getName());
		XmlBuilder adaptersXml=new XmlBuilder("adapters");
		result.addSubElement(adaptersXml);
		if (StringUtils.isNotEmpty(targetAdaptername)) {
			adaptersXml.addAttribute("targetAdapter", targetAdaptername);
		}
		for (Iterator it=adapters.iterator();it.hasNext();) {
			String adapter=(String)it.next();
			XmlBuilder adapterXml=new XmlBuilder("adapter");
			adapterXml.addAttribute("name",adapter);
			adaptersXml.addSubElement(adapterXml);
		}
		XmlBuilder timestampsXml=new XmlBuilder("timestamps");
		result.addSubElement(timestampsXml);
		if (StringUtils.isNotEmpty(targetTimestamp)) {
			timestampsXml.addAttribute("targetTimestamp", targetTimestamp);
		}
		for (Iterator it=timestamps.iterator();it.hasNext();) {
			String timestamp=(String)it.next();
			XmlBuilder timestampXml=new XmlBuilder("timestamp");
			timestampXml.addAttribute("value",timestamp);
			timestampsXml.addSubElement(timestampXml);
		}
		XmlBuilder dataXml=new XmlBuilder("data");
		result.addSubElement(dataXml);
		for (Iterator it=data.keySet().iterator();it.hasNext();) {
			String name=(String)it.next();
			SummaryRecord record=(SummaryRecord)data.get(name);
			dataXml.addSubElement(record.toXml(name,df,pf));
		}
		return result;
	}

	public void setName(String name) {
		if (StringUtils.isNotEmpty(getName()) && !getName().equals(name)) {
			log.warn("name in file ["+name+"] does not match current name ["+getName()+"]");
		}
		this.name = name;
	}
	public String getName() {
		return name;
	}

	public void setTimestamp(String timestamp) {
		timestamps.add(timestamp);
		if (StringUtils.isNotEmpty(targetTimestamp)) {
			skip=!targetTimestamp.equals(timestamp);
		}
		currentTimestamp=timestamp;
//		try {
//			Date ts = DateUtils.parseAnyDate(timestamp);
//			timestamps.add(ts);
//		} catch (CalendarParserException e) {
//			log.warn("could not parse timestamp from ["+timestamp+"]",e);
//		}
	}

}
