package com.cse454.nel.kbp;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class KB {
	public Map<String, KbEntity> entityMap = new HashMap<String, KbEntity>(); 
	public Map<String, String> lookupMap = new HashMap<String, String>();
	
	public void init() {
		System.out.println("Reading KB");
		for (int i = KbpConstants.kbFileStart; i < KbpConstants.kbFileEnd; ++i) {
			System.out.print("reading "+i+"...\r");
			readFromNew(KbpConstants.kbPath
					+ String.format(KbpConstants.kbFileFormat, i));
		}
		
		try
		{
			// Create file
			FileWriter fstream = new FileWriter("entityLookup.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			
			for(Entry<String, String> entry : lookupMap.entrySet()) {
			    String key = entry.getKey();
			    String value = entry.getValue();
			    out.write(key+"\t"+value+"\n");
			}
			
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		System.out.println("Finished KB");
	}

	public void readFromNew(String filename) {
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new File(filename));
			NodeList nodeList = doc.getElementsByTagName("entity");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				KbEntity entity = new KbEntity();
				NamedNodeMap attributes = node.getAttributes();
				
				entity.wikiTitle = attributes.getNamedItem("wiki_title")
						.getTextContent();
				entity.kbId = attributes.getNamedItem("id").getTextContent();
				
				lookupMap.put(entity.kbId, entity.wikiTitle);
			}
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
	
	public void readFrom(String filename) {
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new File(filename));
			NodeList nodeList = doc.getElementsByTagName("entity");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				KbEntity entity = new KbEntity();
				NamedNodeMap attributes = node.getAttributes();
				entity.wikiTitle = attributes.getNamedItem("wiki_title")
						.getTextContent();
				entity.type = KbEntity.EntityType.valueOf(attributes
						.getNamedItem("type").getTextContent());
				entity.kbId = attributes.getNamedItem("id").getTextContent();
				entity.name = attributes.getNamedItem("name").getTextContent();
				NodeList children = node.getChildNodes();
				for (int j = 0; j < children.getLength(); ++j) {
					// TODO process facts

					// wiki text
					if (children.item(j).getNodeName().equals("wiki_text")) {
						entity.wikiText = children.item(j).getTextContent();
					}
				}
				entityMap.put(entity.name,entity);
			}
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
}
